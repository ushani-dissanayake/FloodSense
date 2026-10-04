package com.floodsense.app.data

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executors
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Live ESP32 nodes live under FloodSystem/stations/{stationId}.
 * The old FloodSystem root keys are still read as Seethawaka for older sketches.
 */
class FirebaseFloodRepository(
    private val demo: DemoFloodRepository = DemoFloodRepository()
) : FloodRepository {
    override val stations: StateFlow<List<Station>> = demo.stations
    private val _readings = MutableStateFlow<List<SensorReading>>(emptyList())
    override val readings: StateFlow<List<SensorReading>> = _readings.asStateFlow()
    override val alerts: StateFlow<List<FloodAlert>> = demo.alerts
    override val preparednessItems: StateFlow<List<PreparednessItem>> = demo.preparednessItems
    override val devices: StateFlow<List<SensorDevice>> = demo.devices
    override val managedUsers: StateFlow<List<ManagedUser>> = demo.managedUsers
    override val adminAlerts: StateFlow<List<AdminAlert>> = demo.adminAlerts

    private val lastRisk = mutableMapOf<String, RiskLevel>()
    private val lastOnline = mutableMapOf<String, Boolean>()
    private val historyByStation = mutableMapOf<String, List<SensorReading>>()
    private val liveStationIds = mutableSetOf<String>()
    private val lastSeenByStation = mutableMapOf<String, Long>()
    private val handler = Handler(Looper.getMainLooper())
    private val mlExecutor = Executors.newSingleThreadExecutor()
    private val lastMlKey = mutableMapOf<String, String>()
    private val staleCheck = object : Runnable {
        override fun run() {
            refreshStaleStations()
            handler.postDelayed(this, STALE_CHECK_MS)
        }
    }

    init {
        handler.postDelayed(staleCheck, STALE_CHECK_MS)
        val root = FirebaseDatabase.getInstance().getReference("FloodSystem")
        root.child("stations").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach { child ->
                    val id = child.key ?: return@forEach
                    applyStationNode(id, child)
                }
                publishReadings()
            }

            override fun onCancelled(error: DatabaseError) = Unit
        })
        listenLegacyRoot(root)
    }

    private fun listenLegacyRoot(root: DatabaseReference) {
        var water: Double? = null
        var rain: Double? = null
        var ultrasonic: Double? = null
        fun publishLegacy() {
            if (LEGACY_STATION_ID in liveStationIds) return
            val liveWater = water ?: return
            val liveRain = rain ?: return
            applyLiveValues(LEGACY_STATION_ID, liveWater, liveRain, ultrasonic, emptyList(), null)
            publishReadings()
        }
        listOf("water_level", "rainfall", "ultrasonic_distance").forEach { key ->
            root.child(key).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val value = snapshot.asNumber() ?: return
                    when (key) {
                        "water_level" -> water = value
                        "rainfall" -> rain = value
                        else -> ultrasonic = value
                    }
                    publishLegacy()
                }

                override fun onCancelled(error: DatabaseError) = Unit
            })
        }
        root.child("history").orderByKey().limitToLast(360)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (LEGACY_STATION_ID in liveStationIds) return
                    val history = snapshot.children.mapNotNull { it.toReading(LEGACY_STATION_ID) }
                    if (history.isNotEmpty()) historyByStation[LEGACY_STATION_ID] = history
                    publishLegacy()
                }

                override fun onCancelled(error: DatabaseError) = Unit
            })
    }

    private fun applyStationNode(stationId: String, snapshot: DataSnapshot) {
        val water = snapshot.number("water_level") ?: return
        val rain = snapshot.number("rainfall") ?: return
        val history = snapshot.child("history").children.mapNotNull { child ->
            child.toReading(stationId)
        }
        liveStationIds.add(stationId)
        val lastSeen = snapshot.number("last_seen").toEpochMillis()
            ?: history.maxOfOrNull { it.timestampMillis }?.takeIf { it >= PLAUSIBLE_EPOCH_MS }
        applyLiveValues(
            stationId = stationId,
            water = water,
            rain = rain,
            ultrasonic = snapshot.number("ultrasonic_distance"),
            history = history,
            name = snapshot.child("name").getValue(String::class.java),
            lastSeenMillis = lastSeen,
            batteryVoltage = snapshot.number("battery_voltage"),
            solarVoltage = snapshot.number("solar_voltage"),
            sirenCommand = snapshot.child("siren_command").asBoolean(),
            sirenMute = snapshot.child("siren_mute").asBoolean(),
            sirenActive = snapshot.child("siren_active").asBoolean()
        )
    }

    private fun applyLiveValues(
        stationId: String,
        water: Double,
        rain: Double,
        ultrasonic: Double?,
        history: List<SensorReading>,
        name: String?,
        lastSeenMillis: Long? = null,
        batteryVoltage: Double? = null,
        solarVoltage: Double? = null,
        sirenCommand: Boolean = false,
        sirenMute: Boolean = false,
        sirenActive: Boolean = false
    ) {
        if (history.isNotEmpty()) historyByStation[stationId] = history
        val seen = lastSeenMillis ?: System.currentTimeMillis()
        lastSeenByStation[stationId] = seen
        val current = stationOrCreate(stationId, name)
        val chart = historyByStation[stationId].orEmpty().map { it.waterLevel.toFloat() }.ifEmpty {
            (current.chartValues + water.toFloat()).takeLast(24)
        }
        val online = isFresh(seen)
        publishStation(
            current.copy(
                waterLevel = water,
                rainfall = rain,
                ultrasonicDistance = ultrasonic ?: current.ultrasonicDistance,
                online = online,
                chartValues = chart,
                name = name?.takeIf { it.isNotBlank() } ?: current.name,
                lastSeenMillis = seen,
                signal = if (online) "Strong" else "None",
                batteryVoltage = batteryVoltage ?: current.batteryVoltage,
                solarVoltage = solarVoltage ?: current.solarVoltage,
                sirenCommand = sirenCommand,
                sirenMute = sirenMute,
                sirenActive = sirenActive
            )
        )
    }

    private fun publishStation(station: Station) {
        val local = FloodRiskEngine.apply(station)
        demo.updateStation(local)
        demo.syncDevice(local)
        val fingerprint = "${"%.2f".format(local.waterLevel)}|${"%.1f".format(local.rainfall)}|${local.online}"
        if (lastMlKey[local.id] == fingerprint) {
            val current = demo.stations.value.firstOrNull { it.id == local.id } ?: local
            finishPublish(current)
            return
        }
        if (!FloodMlClient.enabled) {
            finishPublish(local)
            return
        }
        mlExecutor.execute {
            val remote = FloodMlClient.predict(local)
            handler.post {
                val scored = if (remote != null) {
                    lastMlKey[local.id] = fingerprint
                    local.copy(
                        riskLevel = remote.riskLevel,
                        prediction = FloodMlClient.merge(local.prediction, remote)
                    )
                } else {
                    local
                }
                demo.updateStation(scored)
                demo.syncDevice(scored)
                finishPublish(scored)
            }
        }
    }

    private fun finishPublish(scored: Station) {
        val previous = lastRisk[scored.id]
        if (scored.prediction.decision == ModelDecision.ISSUE_ALERT && previous != RiskLevel.HIGH) {
            FloodRiskEngine.autoAlert(scored)?.let { (admin, citizen) ->
                demo.addAdminAlert(admin)
                publishIssueAlert(citizen)
            }
        }
        val wasOnline = lastOnline[scored.id]
        if (wasOnline == true && !scored.online) {
            publishOfflineAlert(scored)
        }
        lastRisk[scored.id] = scored.riskLevel
        lastOnline[scored.id] = scored.online
    }

    private fun refreshStaleStations() {
        val now = System.currentTimeMillis()
        demo.stations.value.forEach { station ->
            val seen = lastSeenByStation[station.id] ?: return@forEach
            val online = now - seen < STALE_AFTER_MS
            if (station.online == online && station.lastSeenMillis == seen) return@forEach
            publishStation(station.copy(online = online, lastSeenMillis = seen, signal = if (online) "Strong" else "None"))
        }
    }

    private fun isFresh(lastSeenMillis: Long): Boolean =
        System.currentTimeMillis() - lastSeenMillis < STALE_AFTER_MS

    private fun publishOfflineAlert(station: Station) {
        demo.addAdminAlert(
            AdminAlert(
                id = "offline-${station.id}-${System.currentTimeMillis()}",
                title = "Station offline",
                stationName = station.name,
                message = "No ESP32 update for ${STALE_AFTER_MS / 1000} seconds. Last reading is held. Check power or Wi-Fi.",
                time = "Just now",
                recipients = 12,
                severity = AlertSeverity.WARNING,
                delivery = AlertDelivery.MONITORING
            )
        )
    }

    private fun stationOrCreate(stationId: String, name: String?): Station {
        demo.stations.value.firstOrNull { it.id == stationId }?.let { return it }
        val catalog = FieldLogCatalog.station(stationId)
        val created = Station(
            id = stationId,
            name = name?.takeIf { it.isNotBlank() } ?: catalog?.name ?: prettyId(stationId),
            district = catalog?.district ?: "Live node",
            waterLevel = 0.0,
            rainfall = 0.0,
            ultrasonicDistance = 0.0,
            online = false,
            riskLevel = RiskLevel.LOW,
            latitude = catalog?.latitude ?: 0.0,
            longitude = catalog?.longitude ?: 0.0,
            chartValues = emptyList(),
            deviceId = stationId
        )
        demo.addStation(created)
        return demo.stations.value.first { it.id == stationId }
    }

    private fun publishReadings() {
        _readings.value = historyByStation.values.flatten().sortedBy { it.timestampMillis }
    }

    override fun setPreparednessCompleted(itemId: String, completed: Boolean) =
        demo.setPreparednessCompleted(itemId, completed)

    override fun addStation(station: Station) = demo.addStation(station)

    override fun updateStation(station: Station) = demo.updateStation(station)

    override fun addDevice(device: SensorDevice) = demo.addDevice(device)

    override fun addManagedUser(user: ManagedUser) = demo.addManagedUser(user)

    override fun addAdminAlert(alert: AdminAlert) {
        demo.addAdminAlert(alert)
        if (alert.severity == AlertSeverity.CRITICAL) {
            publishCitizenIssueAlert(
                FloodAlert(
                    id = alert.id,
                    title = alert.title,
                    stationName = alert.stationName,
                    message = alert.message,
                    time = alert.time,
                    riskLevel = RiskLevel.HIGH
                )
            )
        }
    }

    override fun publishCitizenIssueAlert(alert: FloodAlert) = publishIssueAlert(alert)

    override fun setSirenCommand(stationId: String, on: Boolean) {
        val id = stationId.trim()
        if (id.isBlank()) return
        FirebaseDatabase.getInstance()
            .getReference("FloodSystem/stations/$id")
            .updateChildren(
                mapOf(
                    "siren_command" to on,
                    "siren_mute" to !on,
                    "siren_updated_at" to ServerValue.TIMESTAMP
                )
            )
        demo.stations.value.firstOrNull { it.id == id }?.let { current ->
            demo.updateStation(current.copy(sirenCommand = on, sirenMute = !on))
        }
    }

    private fun publishIssueAlert(alert: FloodAlert) {
        FirebaseDatabase.getInstance()
            .getReference("FloodSystem/issue_alert")
            .setValue(
                mapOf(
                    "id" to alert.id,
                    "title" to alert.title,
                    "body" to alert.message,
                    "stationName" to alert.stationName,
                    "decision" to ModelDecision.ISSUE_ALERT.name,
                    "createdAt" to ServerValue.TIMESTAMP
                )
            )
    }

    private fun DataSnapshot.toReading(stationId: String): SensorReading? {
        val waterLevel = number("water_level") ?: return null
        val keyMillis = key?.toLongOrNull()?.let { seconds ->
            if (seconds < 10_000_000_000L) seconds * 1000L else seconds
        }
        val fieldMillis = number("timestamp")?.toLong()?.let { value ->
            if (value < 10_000_000_000L) value * 1000L else value
        }
        val timestampMillis = fieldMillis ?: keyMillis ?: return null
        return SensorReading(
            timestampMillis = timestampMillis,
            waterLevel = waterLevel,
            rainfall = number("rainfall") ?: 0.0,
            ultrasonicDistance = number("ultrasonic_distance"),
            floodRisk = number("flood_risk")?.toInt() ?: 0,
            stationId = stationId
        )
    }

    private fun DataSnapshot.number(key: String): Double? = child(key).asNumber()

    private fun DataSnapshot.asBoolean(): Boolean {
        return when (val raw = value) {
            is Boolean -> raw
            is Number -> raw.toInt() != 0
            is String -> raw.equals("true", ignoreCase = true) || raw == "1"
            else -> false
        }
    }

    private fun DataSnapshot.asNumber(): Double? {
        val raw = value ?: return null
        return when (raw) {
            is Number -> raw.toDouble()
            is String -> raw.toDoubleOrNull()
            else -> null
        }
    }

    private fun prettyId(id: String): String =
        id.replace('-', ' ').replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

    private fun Double?.toEpochMillis(): Long? {
        val raw = this?.toLong() ?: return null
        return when {
            raw >= PLAUSIBLE_EPOCH_MS -> raw
            raw >= PLAUSIBLE_EPOCH_SEC -> raw * 1000L
            else -> null
        }
    }

    private companion object {
        const val LEGACY_STATION_ID = "kelani-seethawaka"
        const val STALE_AFTER_MS = 30_000L
        const val STALE_CHECK_MS = 10_000L
        const val PLAUSIBLE_EPOCH_MS = 1_700_000_000_000L
        const val PLAUSIBLE_EPOCH_SEC = 1_700_000_000L
    }
}
