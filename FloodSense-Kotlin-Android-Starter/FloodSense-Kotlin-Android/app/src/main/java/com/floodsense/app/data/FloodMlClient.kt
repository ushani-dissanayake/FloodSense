package com.floodsense.app.data

import android.util.Log
import com.floodsense.app.BuildConfig
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Calls the local FastAPI Random Forest at [BuildConfig.ML_API_BASE].
 * Emulator: http://10.0.2.2:8000  Phone: http://<your-laptop-LAN-ip>:8000
 */
object FloodMlClient {
    private const val TAG = "FloodMl"
    val enabled: Boolean get() = BuildConfig.ML_API_BASE.isNotBlank()

    fun predict(station: Station): FloodPrediction? {
        if (!enabled) return null
        val connection = (URL("${BuildConfig.ML_API_BASE}/predict").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
            connectTimeout = 2500
            readTimeout = 2500
            doOutput = true
        }
        val body = JSONObject()
            .put("water_level", station.waterLevel)
            .put("rainfall", station.rainfall)
            .put("ultrasonic_distance", station.ultrasonicDistance)
            .put("online", station.online)
            .toString()
        return try {
            connection.outputStream.use { it.write(body.toByteArray()) }
            if (connection.responseCode !in 200..299) return null
            val raw = connection.inputStream.bufferedReader().use { it.readText() }
            parse(JSONObject(raw))
        } catch (error: Exception) {
            Log.w(TAG, "RF API unavailable: ${error.message}")
            null
        } finally {
            connection.disconnect()
        }
    }

    fun merge(local: FloodPrediction, remote: FloodPrediction): FloodPrediction {
        val reasons = (remote.anomalyReasons + local.anomalyReasons).distinct()
        return remote.copy(
            anomaly = remote.anomaly || local.anomaly,
            anomalyReasons = reasons
        )
    }

    private fun parse(json: JSONObject): FloodPrediction {
        val risk = when (json.optString("risk_level").uppercase()) {
            "HIGH" -> RiskLevel.HIGH
            "MEDIUM" -> RiskLevel.MEDIUM
            else -> RiskLevel.LOW
        }
        val decision = when (json.optString("decision").uppercase()) {
            "ISSUE_ALERT" -> ModelDecision.ISSUE_ALERT
            "WATCH" -> ModelDecision.WATCH
            "FLAG_SENSOR" -> ModelDecision.FLAG_SENSOR
            else -> ModelDecision.NORMAL
        }
        val reasons = json.optJSONArray("anomaly_reasons")?.let { array ->
            List(array.length()) { array.optString(it) }.filter { it.isNotBlank() }
        } ?: emptyList()
        return FloodPrediction(
            riskLevel = risk,
            probability = json.optDouble("probability", 0.12).toFloat(),
            confidence = json.optString("confidence", "Moderate"),
            anomaly = json.optBoolean("anomaly"),
            anomalyReasons = reasons,
            decision = decision,
            action = json.optString("action", decision.action),
            source = json.optString("source", "Random Forest")
        )
    }
}
