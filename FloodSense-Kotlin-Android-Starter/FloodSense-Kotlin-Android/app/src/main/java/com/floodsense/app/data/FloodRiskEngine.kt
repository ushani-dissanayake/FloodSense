package com.floodsense.app.data

/**
 * On-device copy of the water/rain tree. Synthetic CSV accuracy is leakage;
 * field checks use Irrigation Department / DMC stages (see field_validation.json).
 *
 * Risk uses the exported decision tree (same rules as the ESP32).
 * Anomaly uses the Isolation Forest feature bounds plus station health.
 * Automated decisions choose ISSUE_ALERT, WATCH, FLAG_SENSOR, or NORMAL.
 */
object FloodRiskEngine {
    const val HIGH_RAINFALL_MM = 200.0
    /** 200 ml in a 250 ml demo beaker, mapped onto the 0–10 m probe scale: 200/250 * 10. */
    const val HIGH_WATER_M = 8.0
    const val MEDIUM_RAINFALL_MM = 59.97
    const val MEDIUM_WATER_M = 4.97

    @Volatile
    var highRainfallMm: Double = HIGH_RAINFALL_MM

    @Volatile
    var mediumRainfallMm: Double = MEDIUM_RAINFALL_MM

    fun evaluate(station: Station): FloodPrediction {
        val riskClass = classifyRisk(station.waterLevel, station.rainfall)
        val risk = riskClass.toRiskLevel()
        val reasons = anomalyReasons(station)
        val anomaly = reasons.isNotEmpty()
        val probability = floodProbability(station.waterLevel, station.rainfall, risk)
        val decision = decide(riskClass, anomaly, reasons)
        return FloodPrediction(
            riskLevel = risk,
            probability = probability,
            confidence = if (probability >= 0.75f) "High" else "Moderate",
            anomaly = anomaly,
            anomalyReasons = reasons,
            decision = decision,
            action = decision.action,
            source = "On-device tree"
        )
    }

    fun apply(station: Station): Station {
        val prediction = evaluate(station)
        return station.copy(riskLevel = prediction.riskLevel, prediction = prediction)
    }

    fun classifyRisk(waterLevel: Double, rainfall: Double): Int {
        if (rainfall > highRainfallMm) return 2
        if (waterLevel > HIGH_WATER_M) return 2
        if (rainfall > mediumRainfallMm) return 1
        if (waterLevel > MEDIUM_WATER_M) return 1
        return 0
    }

    fun floodProbability(waterLevel: Double, rainfall: Double, risk: RiskLevel): Float {
        return when (risk) {
            RiskLevel.HIGH -> {
                val over = maxOf(rainfall - highRainfallMm, waterLevel - HIGH_WATER_M, 0.0)
                (0.82f + (over / 80.0).toFloat() * 0.17f).coerceIn(0.80f, 0.99f)
            }
            RiskLevel.MEDIUM -> {
                val over = maxOf(rainfall - mediumRainfallMm, waterLevel - MEDIUM_WATER_M, 0.0)
                (0.48f + (over / 60.0).toFloat() * 0.28f).coerceIn(0.40f, 0.79f)
            }
            RiskLevel.LOW -> {
                val toward = (waterLevel / MEDIUM_WATER_M + rainfall / mediumRainfallMm) / 2.0
                (0.08f + toward.toFloat() * 0.26f).coerceIn(0.05f, 0.39f)
            }
        }
    }

    fun autoAlert(station: Station): Pair<AdminAlert, FloodAlert>? {
        val prediction = station.prediction
        if (prediction.decision != ModelDecision.ISSUE_ALERT &&
            prediction.decision != ModelDecision.FLAG_SENSOR
        ) {
            return null
        }
        val high = prediction.decision == ModelDecision.ISSUE_ALERT
        val title = if (high) "Automated flood alert" else "Sensor anomaly detected"
        val message = if (high) {
            "Model risk is HIGH (${(prediction.probability * 100).toInt()}%). ${prediction.action}"
        } else {
            prediction.anomalyReasons.firstOrNull() ?: prediction.action
        }
        val admin = AdminAlert(
            id = "auto-${station.id}",
            title = title,
            stationName = station.name,
            message = message,
            time = "Just now",
            recipients = if (high) 426 else 12,
            severity = if (high) AlertSeverity.CRITICAL else AlertSeverity.WARNING,
            delivery = AlertDelivery.DELIVERED
        )
        val citizen = FloodAlert(
            id = "auto-${station.id}",
            title = title,
            stationName = station.name,
            message = message,
            time = "Just now",
            riskLevel = if (high) RiskLevel.HIGH else RiskLevel.MEDIUM
        )
        return admin to citizen
    }

    private fun anomalyReasons(station: Station): List<String> {
        val reasons = mutableListOf<String>()
        if (!station.online) reasons += "Station offline — no fresh reading"
        if (station.waterLevel < 0.0 || station.waterLevel > 10.0) {
            reasons += "Water level outside trained range 0–10 m"
        }
        if (station.rainfall < 0.0 || station.rainfall > 300.0) {
            reasons += "Rainfall outside trained range 0–300 mm"
        }
        if (station.ultrasonicDistance < 0.0 || station.ultrasonicDistance > 400.0) {
            reasons += "Ultrasonic distance outside range 0–400 cm"
        }
        val battery = station.batteryVoltage
        if (battery != null && battery < 3.30) {
            reasons += "Battery voltage low (${"%.2f".format(battery)} V)"
        }
        val solar = station.solarVoltage
        if (solar != null && (solar < 0.0 || solar > 12.0)) {
            reasons += "Solar voltage outside range 0–12 V"
        }
        if (station.chartValues.size >= 2) {
            val rise = station.chartValues.last() - station.chartValues[station.chartValues.lastIndex - 1]
            if (rise > 0.8f) reasons += "Rapid water-level rise detected"
        }
        return reasons
    }

    private fun decide(riskClass: Int, anomaly: Boolean, reasons: List<String>): ModelDecision {
        val sensorFault = reasons.any {
            it.contains("outside trained range") ||
                it.contains("offline", ignoreCase = true) ||
                it.contains("Battery voltage low") ||
                it.contains("Solar voltage outside")
        }
        return when {
            riskClass == 2 -> ModelDecision.ISSUE_ALERT
            sensorFault -> ModelDecision.FLAG_SENSOR
            riskClass == 1 || anomaly -> ModelDecision.WATCH
            else -> ModelDecision.NORMAL
        }
    }

    private fun Int.toRiskLevel(): RiskLevel = when (this) {
        2 -> RiskLevel.HIGH
        1 -> RiskLevel.MEDIUM
        else -> RiskLevel.LOW
    }
}
