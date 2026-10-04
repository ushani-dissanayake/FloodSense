package com.floodsense.app.alerts

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging

object CitizenPushRegistrar {
    const val CITIZEN_TOPIC = "floodsense_citizens"

    fun start(context: Context) {
        val app = context.applicationContext
        CitizenAlertNotifier.ensureChannel(app)
        TextLkSmsClient.bind(app)
        FirebaseMessaging.getInstance().subscribeToTopic(CITIZEN_TOPIC)
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            saveToken(token)
        }
        listenForIssueAlerts(app)
        loadSmsConfig()
    }

    private fun loadSmsConfig() {
        FirebaseDatabase.getInstance()
            .getReference("FloodSystem/sms_config")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    TextLkSmsClient.remoteToken =
                        snapshot.child("api_token").getValue(String::class.java).orEmpty()
                    TextLkSmsClient.remoteSenderId =
                        snapshot.child("sender_id").getValue(String::class.java).orEmpty()
                    TextLkSmsClient.viaFunction =
                        snapshot.child("via_function").getValue(Boolean::class.java) == true
                    val remoteUsed = snapshot.child("sends_used").getValue(Int::class.java) ?: 0
                    TextLkSmsClient.syncUsage(remoteUsed)
                }

                override fun onCancelled(error: DatabaseError) = Unit
            })
    }

    fun saveToken(token: String) {
        if (token.isBlank()) return
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "pending"
        val safeKey = token.takeLast(48).replace("[^A-Za-z0-9]".toRegex(), "_")
        FirebaseDatabase.getInstance()
            .getReference("FloodSystem/citizen_tokens/$safeKey")
            .setValue(
                mapOf(
                    "token" to token,
                    "uid" to uid,
                    "topic" to CITIZEN_TOPIC,
                    "updatedAt" to System.currentTimeMillis()
                )
            )
    }

    private fun listenForIssueAlerts(context: Context) {
        FirebaseDatabase.getInstance()
            .getReference("FloodSystem/issue_alert")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val decision = snapshot.child("decision").value as? String ?: return
                    if (decision != "ISSUE_ALERT") return
                    val title = snapshot.child("title").value as? String ?: "Flood alert"
                    val body = snapshot.child("body").value as? String ?: return
                    val id = snapshot.child("id").value as? String ?: body
                    if (CitizenAlertNotifier.showIfNew(context, id, title, body)) {
                        EmergencySmsDispatcher.notifyContacts(title, body, alertId = id)
                    }
                }

                override fun onCancelled(error: DatabaseError) = Unit
            })
    }
}
