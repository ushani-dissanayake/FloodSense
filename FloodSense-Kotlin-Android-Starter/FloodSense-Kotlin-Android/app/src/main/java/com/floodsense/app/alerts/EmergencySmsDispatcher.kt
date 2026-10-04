package com.floodsense.app.alerts

import android.util.Log
import com.floodsense.app.data.EmergencyContact
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction

/**
 * Texts stored emergency contacts for HIGH / ISSUE_ALERT only.
 * Recipients come from [FloodSystem/sms_recipients] (mirrored when a citizen
 * saves contacts) so an admin-issued Critical alert can still reach them.
 * Capped at two SMS for the text.lk free plan. A Realtime Database claim
 * stops two open devices from both spending the quota.
 */
object EmergencySmsDispatcher {
    private const val TAG = "EmergencySms"
    private const val MAX_RECIPIENTS = 2
    private const val RECIPIENTS_PATH = "FloodSystem/sms_recipients"
    private const val CLAIM_PATH = "FloodSystem/sms_config/last_sms_alert_id"

    @Volatile
    private var lastAlertId: String = ""

    fun notifyContacts(
        title: String,
        body: String,
        contacts: List<EmergencyContact> = emptyList(),
        alertId: String = ""
    ) {
        if (alertId.isNotBlank() && alertId == lastAlertId) {
            Log.i(TAG, "SMS already queued for $alertId")
            return
        }
        if (TextLkSmsClient.remaining() <= 0) {
            Log.i(TAG, "SMS quota exhausted (${TextLkSmsClient.MAX_SENDS} demo messages)")
            return
        }
        if (alertId.isNotBlank()) lastAlertId = alertId
        val message = buildMessage(title, body)
        if (contacts.isNotEmpty()) {
            claimThenDispatch(alertId, contacts, message)
            return
        }
        loadAllContacts { loaded ->
            if (loaded.isNotEmpty()) {
                claimThenDispatch(alertId, loaded, message)
            } else {
                Log.w(
                    TAG,
                    "No emergency mobiles found. Add a 07XXXXXXXX contact in Profile → Emergency contacts, then rebuild and retry."
                )
            }
        }
    }

    private fun claimThenDispatch(
        alertId: String,
        contacts: List<EmergencyContact>,
        message: String
    ) {
        if (alertId.isBlank()) {
            dispatch(contacts, message)
            return
        }
        FirebaseDatabase.getInstance()
            .getReference(CLAIM_PATH)
            .runTransaction(object : Transaction.Handler {
                override fun doTransaction(current: MutableData): Transaction.Result {
                    val existing = current.getValue(String::class.java)
                    if (!existing.isNullOrBlank() && existing == alertId) {
                        return Transaction.abort()
                    }
                    current.value = alertId
                    return Transaction.success(current)
                }

                override fun onComplete(
                    error: DatabaseError?,
                    committed: Boolean,
                    currentData: DataSnapshot?
                ) {
                    if (error == null && !committed) {
                        Log.i(TAG, "SMS already claimed for $alertId")
                        return
                    }
                    if (error != null) {
                        Log.w(TAG, "SMS claim failed (${error.message}); sending anyway")
                    }
                    dispatch(contacts, message)
                }
            })
    }

    private fun loadAllContacts(onLoaded: (List<EmergencyContact>) -> Unit) {
        val database = FirebaseDatabase.getInstance()
        database.getReference(RECIPIENTS_PATH).get()
            .addOnSuccessListener { recipients ->
                val mirrored = recipients.children.flatMap { uidNode -> contactsFrom(uidNode) }
                if (mirrored.isNotEmpty()) {
                    onLoaded(mirrored)
                    return@addOnSuccessListener
                }
                loadFromUsers(database, onLoaded)
            }
            .addOnFailureListener {
                Log.w(TAG, "Could not load sms_recipients: ${it.message}")
                loadFromUsers(database, onLoaded)
            }
    }

    private fun loadFromUsers(
        database: FirebaseDatabase,
        onLoaded: (List<EmergencyContact>) -> Unit
    ) {
        database.getReference("users").get()
            .addOnSuccessListener { users ->
                val loaded = users.children.flatMap { user ->
                    contactsFrom(user.child("emergency_contacts"))
                }
                if (loaded.isNotEmpty()) {
                    onLoaded(loaded)
                    return@addOnSuccessListener
                }
                loadCurrentUserFallback(database, onLoaded)
            }
            .addOnFailureListener {
                Log.w(TAG, "Could not load users for SMS: ${it.message}")
                loadCurrentUserFallback(database, onLoaded)
            }
    }

    private fun loadCurrentUserFallback(
        database: FirebaseDatabase,
        onLoaded: (List<EmergencyContact>) -> Unit
    ) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid.isNullOrBlank()) {
            onLoaded(emptyList())
            return
        }
        database.getReference("users/$uid").get()
            .addOnSuccessListener { snap ->
                val contacts = contactsFrom(snap.child("emergency_contacts"))
                if (contacts.isNotEmpty()) {
                    onLoaded(contacts)
                    return@addOnSuccessListener
                }
                val phone = snap.child("phone").getValue(String::class.java).orEmpty()
                if (TextLkSmsClient.toRecipient(phone) != null) {
                    Log.i(TAG, "No emergency contacts; using signed-in account phone")
                    onLoaded(listOf(EmergencyContact("Account", phone, "Self")))
                } else {
                    onLoaded(emptyList())
                }
            }
            .addOnFailureListener {
                Log.w(TAG, "Could not load emergency contacts")
                onLoaded(emptyList())
            }
    }

    private fun contactsFrom(snap: DataSnapshot): List<EmergencyContact> {
        return snap.children.mapNotNull { child ->
            val phone = child.child("phone").getValue(String::class.java)
                ?: child.key
                ?: return@mapNotNull null
            val name = child.child("name").getValue(String::class.java) ?: "Contact"
            EmergencyContact(
                name = name,
                phone = phone,
                relationship = child.child("relationship").getValue(String::class.java) ?: "Contact"
            )
        }
    }

    private fun dispatch(contacts: List<EmergencyContact>, message: String) {
        val recipients = contacts
            .mapNotNull { TextLkSmsClient.toRecipient(it.phone) }
            .distinct()
            .take(minOf(MAX_RECIPIENTS, TextLkSmsClient.remaining()))
        if (recipients.isEmpty()) {
            Log.w(TAG, "Emergency contacts were saved but none are valid +94 mobiles")
            return
        }
        TextLkSmsClient.sendToMany(recipients, message)
        Log.i(TAG, "Queued ${recipients.size} emergency SMS via text.lk")
    }

    private fun buildMessage(title: String, body: String): String {
        val text = "EMERGENCY FloodSense: $title. $body"
        return text.take(160)
    }
}
