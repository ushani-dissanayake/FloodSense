package com.floodsense.app.alerts

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.floodsense.app.MainActivity
import com.floodsense.app.R

object CitizenAlertNotifier {
    const val CHANNEL_ID = "flood_alerts"
    private const val PREFS = "floodsense_alerts"
    private const val LAST_ID = "last_shown_alert_id"

    fun ensureChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Flood alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "HIGH risk ISSUE_ALERT notifications for citizens"
            enableVibration(true)
        }
        manager.createNotificationChannel(channel)
    }

    fun showIfNew(context: Context, alertId: String, title: String, body: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (alertId.isNotBlank() && prefs.getString(LAST_ID, null) == alertId) {
            return false
        }
        if (alertId.isNotBlank()) {
            prefs.edit().putString(LAST_ID, alertId).apply()
        }
        ensureChannel(context)
        val launch = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_flood)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(launch)
            .build()
        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            NotificationManagerCompat.from(context).notify(alertId.hashCode(), notification)
        }
        return true
    }
}
