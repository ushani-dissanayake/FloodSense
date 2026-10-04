package com.floodsense.app.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface FloodRepository {
    val stations: StateFlow<List<Station>>
    val readings: StateFlow<List<SensorReading>>
    val alerts: StateFlow<List<FloodAlert>>
    val preparednessItems: StateFlow<List<PreparednessItem>>
    val devices: StateFlow<List<SensorDevice>>
    val managedUsers: StateFlow<List<ManagedUser>>
    val adminAlerts: StateFlow<List<AdminAlert>>

    fun setPreparednessCompleted(itemId: String, completed: Boolean)
    fun addStation(station: Station)
    fun updateStation(station: Station)
    fun addDevice(device: SensorDevice)
    fun addManagedUser(user: ManagedUser)
    fun addAdminAlert(alert: AdminAlert)
    fun publishCitizenIssueAlert(alert: FloodAlert) {}
    fun setSirenCommand(stationId: String, on: Boolean) {}
}

class DemoFloodRepository : FloodRepository {
    private val scoredStations = DemoData.stations.map(FloodRiskEngine::apply)
    private val automated = scoredStations.mapNotNull(FloodRiskEngine::autoAlert)

    private val _stations = MutableStateFlow(scoredStations)
    override val stations: StateFlow<List<Station>> = _stations.asStateFlow()

    private val _readings = MutableStateFlow<List<SensorReading>>(emptyList())
    override val readings: StateFlow<List<SensorReading>> = _readings.asStateFlow()

    private val _alerts = MutableStateFlow(
        automated.map { it.second } + DemoData.alerts.filter { alert ->
            automated.none { it.second.stationName == alert.stationName }
        }
    )
    override val alerts: StateFlow<List<FloodAlert>> = _alerts.asStateFlow()

    private val _preparednessItems = MutableStateFlow(DemoData.preparednessItems)
    override val preparednessItems: StateFlow<List<PreparednessItem>> = _preparednessItems.asStateFlow()

    private val _devices = MutableStateFlow(DemoData.devices)
    override val devices: StateFlow<List<SensorDevice>> = _devices.asStateFlow()

    private val _managedUsers = MutableStateFlow(DemoData.managedUsers)
    override val managedUsers: StateFlow<List<ManagedUser>> = _managedUsers.asStateFlow()

    private val _adminAlerts = MutableStateFlow(
        automated.map { it.first } + DemoData.adminAlerts.filter { alert ->
            automated.none { it.first.stationName == alert.stationName }
        }
    )
    override val adminAlerts: StateFlow<List<AdminAlert>> = _adminAlerts.asStateFlow()

    override fun setPreparednessCompleted(itemId: String, completed: Boolean) {
        _preparednessItems.value = _preparednessItems.value.map { item ->
            if (item.id == itemId) item.copy(completed = completed) else item
        }
    }

    override fun addStation(station: Station) {
        val exists = _stations.value.any { current ->
            current.id.equals(station.id, ignoreCase = true) ||
                current.name.equals(station.name.trim(), ignoreCase = true)
        }
        if (exists) return
        val scored = FloodRiskEngine.apply(station)
        _stations.value = _stations.value + scored
        FloodRiskEngine.autoAlert(scored)?.let { (admin, citizen) ->
            _adminAlerts.value = listOf(admin) + _adminAlerts.value
            _alerts.value = listOf(citizen) + _alerts.value
        }
    }

    override fun updateStation(station: Station) {
        val scored = FloodRiskEngine.apply(station)
        _stations.value = _stations.value.map { if (it.id == scored.id) scored else it }
    }

    override fun addDevice(device: SensorDevice) {
        _devices.value = _devices.value + device
    }

    fun syncDevice(station: Station) {
        val health = if (station.online) DeviceHealth.ONLINE else DeviceHealth.OFFLINE
        val lastSeen = station.statusLabel()
        val deviceId = station.deviceId.ifBlank { station.id }
        val updated = _devices.value.map { device ->
            val match = device.id == deviceId || device.id == station.id ||
                station.name.contains(device.stationName, ignoreCase = true)
            if (!match) device else device.copy(
                health = health,
                lastSeen = lastSeen,
                signal = if (station.online) "Strong" else "None",
                stationName = station.name,
                batteryVoltage = station.batteryVoltage,
                solarVoltage = station.solarVoltage
            )
        }
        _devices.value = if (updated.any { it.id == deviceId || it.id == station.id }) {
            updated
        } else {
            updated + SensorDevice(
                id = deviceId,
                type = "Water + rain node",
                stationName = station.name,
                health = health,
                signal = if (station.online) "Strong" else "None",
                lastSeen = lastSeen,
                batteryVoltage = station.batteryVoltage,
                solarVoltage = station.solarVoltage
            )
        }
    }

    override fun addManagedUser(user: ManagedUser) {
        _managedUsers.value = _managedUsers.value + user
    }

    override fun addAdminAlert(alert: AdminAlert) {
        _adminAlerts.value = listOf(alert) + _adminAlerts.value
        _alerts.value = listOf(
            FloodAlert(
                id = alert.id,
                title = alert.title,
                stationName = alert.stationName,
                message = alert.message,
                time = alert.time,
                riskLevel = when (alert.severity) {
                    AlertSeverity.CRITICAL -> RiskLevel.HIGH
                    AlertSeverity.WARNING -> RiskLevel.MEDIUM
                    else -> RiskLevel.LOW
                }
            )
        ) + _alerts.value
    }
}
