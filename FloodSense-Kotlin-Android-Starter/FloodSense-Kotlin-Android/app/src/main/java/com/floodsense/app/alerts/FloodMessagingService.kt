package com.floodsense.app.alerts

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FloodMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        CitizenPushRegistrar.saveToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title
            ?: message.data["title"]
            ?: "Flood alert"
        val body = message.notification?.body
            ?: message.data["body"]
            ?: return
        val alertId = message.data["alertId"] ?: message.messageId ?: title
        val emergency = message.data["decision"] == "ISSUE_ALERT"
        if (CitizenAlertNotifier.showIfNew(applicationContext, alertId, title, body) && emergency) {
            EmergencySmsDispatcher.notifyContacts(title, body, alertId = alertId)
        }
    }
}
