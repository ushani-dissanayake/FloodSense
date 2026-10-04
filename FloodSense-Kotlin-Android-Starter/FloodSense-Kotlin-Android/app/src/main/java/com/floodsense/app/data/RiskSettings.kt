package com.floodsense.app.data

import android.content.Context

object RiskSettings {
    private const val PREFS = "floodsense_thresholds"
    private const val MEDIUM_RAIN = "medium_rainfall_mm"
    private const val HIGH_RAIN = "high_rainfall_mm"

    fun load(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        FloodRiskEngine.mediumRainfallMm = prefs.getFloat(
            MEDIUM_RAIN,
            FloodRiskEngine.MEDIUM_RAINFALL_MM.toFloat()
        ).toDouble()
        FloodRiskEngine.highRainfallMm = prefs.getFloat(
            HIGH_RAIN,
            FloodRiskEngine.HIGH_RAINFALL_MM.toFloat()
        ).toDouble()
    }

    fun save(context: Context, mediumRainfallMm: Double, highRainfallMm: Double) {
        val medium = mediumRainfallMm.coerceIn(10.0, 199.0)
        val high = highRainfallMm.coerceIn(medium + 1.0, 400.0)
        FloodRiskEngine.mediumRainfallMm = medium
        FloodRiskEngine.highRainfallMm = high
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putFloat(MEDIUM_RAIN, medium.toFloat())
            .putFloat(HIGH_RAIN, high.toFloat())
            .apply()
    }
}
