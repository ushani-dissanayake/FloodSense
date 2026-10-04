package com.floodsense.app.data

import android.content.Context
import org.json.JSONObject
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class FieldLogSample(
    val timestamp: String,
    val waterLevel: Double,
    val rainfall: Double,
    val observedRisk: Int,
    val window: String = ""
)

data class FieldLogStation(
    val id: String,
    val name: String,
    val gauge: String,
    val river: String,
    val district: String,
    val districts: List<String> = emptyList(),
    val latitude: Double,
    val longitude: Double,
    val alertM: Double,
    val minorM: Double,
    val majorM: Double,
    val source: String,
    val latest: FieldLogSample,
    val floodPeak: FieldLogSample,
    val waterHistory: List<Float>,
    val historyLabels: List<String>
)

object FieldLogCatalog {
    private val colomboZone: ZoneId = ZoneId.of("Asia/Colombo")
    private val displayFormat: DateTimeFormatter =
        DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", Locale.UK).withZone(colomboZone)

    @Volatile
    private var stationsById: Map<String, FieldLogStation> = emptyMap()

    val stations: List<FieldLogStation>
        get() = stationsById.values.toList()

    fun load(context: Context) {
        if (stationsById.isNotEmpty()) return
        synchronized(this) {
            if (stationsById.isNotEmpty()) return
            val text = context.assets.open(ASSET).bufferedReader().use { it.readText() }
            val root = JSONObject(text)
            val array = root.getJSONArray("stations")
            val parsed = LinkedHashMap<String, FieldLogStation>()
            for (index in 0 until array.length()) {
                val station = parseStation(array.getJSONObject(index))
                parsed[station.id] = station
            }
            stationsById = parsed
        }
    }

    fun station(id: String): FieldLogStation? = stationsById[id]

    fun idsForDistrict(districtName: String): List<String> {
        val district = SriLankaDistricts.resolve(districtName).name
        return stationsById.values.filter { it.covers(district) }.map { it.id }
    }

    fun idsForHomeDistrict(districtName: String): List<String> {
        val district = SriLankaDistricts.resolve(districtName).name
        return stationsById.values
            .filter { it.fromKalu1000 && it.covers(district) }
            .map { it.id }
    }

    val homeDistrictNames: Set<String>
        get() = stationsById.values
            .filter { it.fromKalu1000 }
            .flatMap { it.coveredNames() }
            .toSet()

    fun formatTimestamp(iso: String): String {
        return runCatching { displayFormat.format(Instant.parse(iso)) }
            .getOrElse { iso }
    }

    fun toEpochMillis(iso: String): Long? =
        runCatching { Instant.parse(iso).toEpochMilli() }.getOrNull()

    private fun parseStation(obj: JSONObject): FieldLogStation {
        val latest = obj.getJSONObject("latest")
        val peak = obj.getJSONObject("floodPeak")
        val history = obj.getJSONArray("waterHistory")
        val labels = obj.getJSONArray("historyLabels")
        return FieldLogStation(
            id = obj.getString("id"),
            name = obj.getString("name"),
            gauge = obj.getString("gauge"),
            river = obj.getString("river"),
            district = obj.optString("district"),
            districts = obj.optJSONArray("districts")?.let { array ->
                (0 until array.length()).map { array.getString(it) }
            }.orEmpty(),
            latitude = obj.getDouble("lat"),
            longitude = obj.getDouble("lon"),
            alertM = obj.getDouble("alertM"),
            minorM = obj.getDouble("minorM"),
            majorM = obj.getDouble("majorM"),
            source = obj.optString("source"),
            latest = parseSample(latest),
            floodPeak = parseSample(peak),
            waterHistory = (0 until history.length()).map { history.getDouble(it).toFloat() },
            historyLabels = (0 until labels.length()).map { labels.getString(it) }
        )
    }

    private fun parseSample(obj: JSONObject): FieldLogSample = FieldLogSample(
        timestamp = obj.getString("timestamp"),
        waterLevel = obj.getDouble("waterLevel"),
        rainfall = obj.getDouble("rainfall"),
        observedRisk = obj.optInt("observedRisk"),
        window = obj.optString("window")
    )

    private const val ASSET = "district_field_logs.json"
}

private val FieldLogStation.fromKalu1000: Boolean
    get() = source.contains("Kalu_Ganga_1000_Records")

private fun FieldLogStation.coveredNames(): List<String> =
    if (districts.isNotEmpty()) districts else listOf(district)

private fun FieldLogStation.covers(districtName: String): Boolean =
    coveredNames().any { it == districtName }

object DistrictOverview {
    fun correspondingIds(districtName: String, liveStations: List<Station> = emptyList()): List<String> {
        val district = SriLankaDistricts.resolveHome(districtName)
        val official = FieldLogCatalog.idsForHomeDistrict(district.name)
        val extra = liveStations
            .filter { it.id in official || (it.district == district.name && FieldLogCatalog.station(it.id) != null) }
            .map { it.id }
        return (official + extra).distinct()
    }

    fun stationsFor(districtName: String, liveStations: List<Station>): List<Station> {
        return correspondingIds(districtName, liveStations).mapNotNull { id ->
            resolveStation(districtName, id, liveStations)
        }
    }

    fun station(
        districtName: String,
        liveStations: List<Station>,
        stationId: String? = null
    ): Station? {
        val ids = correspondingIds(districtName, liveStations)
        val requested = stationId?.takeIf { it in ids } ?: ids.firstOrNull()
        return requested?.let { resolveStation(districtName, it, liveStations) }
    }

    fun otherStations(
        districtName: String,
        liveStations: List<Station>,
        selectedStationId: String? = null
    ): List<Station> {
        val skip = selectedStationId ?: SriLankaDistricts.resolveHome(districtName).stationId
        return stationsFor(districtName, liveStations).filterNot { it.id == skip }
    }

    fun allHomeStations(liveStations: List<Station>): List<Station> =
        SriLankaDistricts.home
            .flatMap { district -> stationsFor(district.name, liveStations) }
            .distinctBy { it.id }

    fun nearestStation(
        latitude: Double,
        longitude: Double,
        liveStations: List<Station>
    ): Station? = allHomeStations(liveStations).minByOrNull { station ->
        distanceKm(latitude, longitude, station.latitude, station.longitude)
    }

    fun distanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthKm = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
            kotlin.math.cos(Math.toRadians(lat1)) *
            kotlin.math.cos(Math.toRadians(lat2)) *
            kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2)
        return 2 * earthKm * kotlin.math.asin(kotlin.math.sqrt(a))
    }

    fun fieldLog(districtName: String, stationId: String? = null): FieldLogStation? {
        val id = stationId ?: SriLankaDistricts.resolveHome(districtName).stationId
        return FieldLogCatalog.station(id)
    }

    private fun resolveStation(districtName: String, stationId: String, liveStations: List<Station>): Station? {
        val district = SriLankaDistricts.resolveHome(districtName)
        val live = liveStations.firstOrNull { it.id == stationId }
            ?: liveStations.singleOrNull { it.online && it.lastSeenMillis != null }
        val log = FieldLogCatalog.station(stationId)
        val liveFresh = live != null && live.online && live.lastSeenMillis != null
        return when {
            live != null && liveFresh -> live.copy(
                id = stationId,
                name = log?.name ?: live.name,
                district = district.name
            )
            log != null -> log.toStation(district.name, live)
            live != null -> live.copy(district = district.name)
            else -> null
        }
    }

    fun alerts(
        districtName: String,
        alerts: List<FloodAlert>,
        station: Station?
    ): List<FloodAlert> {
        val district = SriLankaDistricts.resolveHome(districtName)
        val names = listOfNotNull(station?.name, district.name, FieldLogCatalog.station(district.stationId)?.name)
        return alerts.filter { alert ->
            names.any { name ->
                alert.stationName.equals(name, ignoreCase = true) ||
                    alert.stationName.contains(name.removeSuffix(" District"), ignoreCase = true) ||
                    alert.message.contains(name.removeSuffix(" District"), ignoreCase = true)
            }
        }
    }
}

fun FieldLogStation.toStation(districtName: String, live: Station? = null): Station {
    val datasetMillis = FieldLogCatalog.toEpochMillis(latest.timestamp)
    val draft = Station(
        id = id,
        name = name,
        district = districtName,
        waterLevel = latest.waterLevel,
        rainfall = latest.rainfall,
        ultrasonicDistance = live?.ultrasonicDistance ?: 0.0,
        online = live?.online == true && live.lastSeenMillis != null,
        riskLevel = RiskLevel.LOW,
        latitude = live?.latitude?.takeIf { it != 0.0 } ?: latitude,
        longitude = live?.longitude?.takeIf { it != 0.0 } ?: longitude,
        chartValues = waterHistory.ifEmpty { live?.chartValues.orEmpty() },
        warningLevel = alertM,
        criticalLevel = minorM,
        deviceId = live?.deviceId.orEmpty(),
        batteryVoltage = live?.batteryVoltage,
        solarVoltage = live?.solarVoltage,
        signal = if (live?.online == true) live.signal else "Dataset",
        lastSeenMillis = live?.lastSeenMillis ?: datasetMillis,
        sirenCommand = live?.sirenCommand ?: false,
        sirenMute = live?.sirenMute ?: false,
        sirenActive = live?.sirenActive ?: false,
        sourceLabel = if (live?.online == true && live.lastSeenMillis != null) "Live ESP32" else "Irrigation dataset"
    )
    val scored = FloodRiskEngine.apply(draft.copy(online = true))
    return scored.copy(
        online = draft.online,
        sourceLabel = draft.sourceLabel,
        lastSeenMillis = draft.lastSeenMillis,
        prediction = scored.prediction.copy(
            source = "Field dataset + on-device tree",
            anomalyReasons = scored.prediction.anomalyReasons.filterNot {
                it.contains("offline", ignoreCase = true)
            }
        )
    )
}
