package com.floodsense.app.data

import androidx.compose.ui.graphics.Color

@Suppress("MagicNumber")
enum class RiskLevel(val label: String, val color: Color) {
    LOW("Low", Color(0xFF19A44A)),
    MEDIUM("Medium", Color(0xFFFF8A00)),
    HIGH("High", Color(0xFFF44336))
}

enum class ThemeMode { SYSTEM, LIGHT, DARK }

enum class AppRole { USER, ADMIN }

enum class AccountRole { CITIZEN, ADMIN }

enum class SafetyStatus(val label: String) {
    SAFE("Safe"),
    WATCH("Watch"),
    AT_RISK("At Risk"),
    ACTIVE("Active")
}

enum class DeviceHealth { ONLINE, OFFLINE, MAINTENANCE, CALIBRATION_DUE }

enum class AlertSeverity { INFO, WATCH, WARNING, CRITICAL }

enum class AlertDelivery { DELIVERED, ACTIVE, MONITORING, RESOLVED }

enum class ModelDecision(val label: String, val action: String) {
    ISSUE_ALERT("Issue alert", "Notify citizens immediately and keep the station on watch."),
    WATCH("Watch", "Continue monitoring. Prepare alerts if water or rain keeps rising."),
    FLAG_SENSOR("Flag sensor", "Check the ESP32, power, and calibration."),
    NORMAL("Normal", "No automated action required.")
}

data class FloodPrediction(
    val riskLevel: RiskLevel = RiskLevel.LOW,
    val probability: Float = 0.12f,
    val confidence: String = "Moderate",
    val anomaly: Boolean = false,
    val anomalyReasons: List<String> = emptyList(),
    val decision: ModelDecision = ModelDecision.NORMAL,
    val action: String = ModelDecision.NORMAL.action,
    val source: String = "On-device tree"
)

data class UserProfile(
    val fullName: String = "FloodSense User",
    val email: String = "user@email.com",
    val phone: String = "+94 77 123 4567",
    val district: String = "Kalutara District"
)

data class SensorReading(
    val timestampMillis: Long,
    val waterLevel: Double,
    val rainfall: Double,
    val ultrasonicDistance: Double? = null,
    val floodRisk: Int = 0,
    val stationId: String = ""
)

data class Station(
    val id: String,
    val name: String,
    val district: String,
    val waterLevel: Double,
    val rainfall: Double,
    val ultrasonicDistance: Double,
    val online: Boolean,
    val riskLevel: RiskLevel,
    val latitude: Double,
    val longitude: Double,
    val chartValues: List<Float>,
    val warningLevel: Double = 2.50,
    val criticalLevel: Double = 3.20,
    val deviceId: String = "",
    val batteryVoltage: Double? = null,
    val solarVoltage: Double? = null,
    val signal: String = "Strong",
    val lastSeenMillis: Long? = null,
    val sirenCommand: Boolean = false,
    val sirenMute: Boolean = false,
    val sirenActive: Boolean = false,
    val prediction: FloodPrediction = FloodPrediction(),
    val sourceLabel: String = ""
)

fun Station.statusLabel(nowMillis: Long = System.currentTimeMillis()): String {
    val seen = lastSeenMillis ?: return if (online) "Online" else "Offline"
    val ago = (nowMillis - seen).coerceAtLeast(0L)
    val relative = when {
        ago < 15_000L -> "just now"
        ago < 60_000L -> "${ago / 1000L}s ago"
        ago < 3_600_000L -> "${ago / 60_000L} min ago"
        else -> "${ago / 3_600_000L}h ago"
    }
    return if (online) "Online · $relative" else "Offline · last seen $relative"
}

fun formatVolts(volts: Double?): String =
    volts?.let { "${"%.2f".format(it)} V" } ?: "--"

fun Station.powerSummary(): String {
    val parts = buildList {
        batteryVoltage?.let { add("Batt ${"%.2f".format(it)} V") }
        solarVoltage?.let { add("Solar ${"%.2f".format(it)} V") }
    }
    return parts.joinToString(" · ").ifBlank { "No voltage reading" }
}

data class SensorDevice(
    val id: String,
    val type: String,
    val stationName: String,
    val health: DeviceHealth,
    val signal: String,
    val lastSeen: String,
    val batteryVoltage: Double? = null,
    val solarVoltage: Double? = null
)

data class ManagedUser(
    val id: String,
    val name: String,
    val email: String,
    val district: String,
    val role: AccountRole,
    val status: SafetyStatus,
    val townCity: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null
) {
    val mapsUrl: String?
        get() {
            val lat = latitude ?: return null
            val lon = longitude ?: return null
            return "https://www.google.com/maps/search/?api=1&query=$lat,$lon"
        }
}

data class AdminAlert(
    val id: String,
    val title: String,
    val stationName: String,
    val message: String,
    val time: String,
    val recipients: Int,
    val severity: AlertSeverity,
    val delivery: AlertDelivery
)

data class FloodAlert(
    val id: String,
    val title: String,
    val stationName: String,
    val message: String,
    val time: String,
    val riskLevel: RiskLevel,
    val unread: Boolean = true
)

data class PreparednessItem(
    val id: String,
    val title: String,
    val description: String,
    val completed: Boolean
)

data class NotificationSettings(
    val criticalAlerts: Boolean = true,
    val risingWaterAlerts: Boolean = true,
    val rainfallAlerts: Boolean = true,
    val stationOfflineAlerts: Boolean = false,
    val vibration: Boolean = true
)

data class EmergencyContact(
    val name: String,
    val phone: String,
    val relationship: String
)
