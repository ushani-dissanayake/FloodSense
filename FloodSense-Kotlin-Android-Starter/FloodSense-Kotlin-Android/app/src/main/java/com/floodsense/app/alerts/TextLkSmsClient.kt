package com.floodsense.app.alerts

import android.content.Context
import android.util.Log
import com.floodsense.app.BuildConfig
import com.floodsense.app.data.AuthRepository
import com.google.firebase.database.FirebaseDatabase
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors
import kotlin.math.max

/**
 * [text.lk SMS API](https://text.lk/docs/sms-api-endpoints/)
 *
 * Token order: BuildConfig (`textlk.api.token` in local.properties), then
 * `FloodSystem/sms_config/api_token` in RTDB. Sender defaults to FloodSense.
 *
 * Free-plan guard: at most [MAX_SENDS] messages are sent for the whole demo.
 */
object TextLkSmsClient {
    const val SEND_ENABLED = true
    const val MAX_SENDS = 5
    private const val ENDPOINT = "https://app.text.lk/api/v3/sms/send"
    private const val DEFAULT_SENDER = "FloodSense"
    private const val TAG = "TextLkSms"
    private const val PREFS = "floodsense_sms"
    private const val KEY_USED = "sends_used"
    private val io = Executors.newSingleThreadExecutor()

    @Volatile
    var remoteToken: String = ""

    @Volatile
    var remoteSenderId: String = DEFAULT_SENDER

    /** When true, Cloud Function owns SMS after FCM. The app does not send. */
    @Volatile
    var viaFunction: Boolean = false

    @Volatile
    var sendsUsed: Int = 0
        private set

    private var appContext: Context? = null

    fun remaining(): Int = (MAX_SENDS - sendsUsed).coerceAtLeast(0)

    fun bind(context: Context) {
        appContext = context.applicationContext
        val stored = appContext!!.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY_USED, 0)
        sendsUsed = max(sendsUsed, stored)
    }

    fun syncUsage(remoteUsed: Int) {
        sendsUsed = max(sendsUsed, remoteUsed.coerceAtLeast(0))
        persistLocal()
    }

    fun sendPlain(recipient: String, message: String, apiToken: String = resolveToken()): Boolean {
        if (!SEND_ENABLED) return false
        if (remaining() <= 0) {
            Log.i(TAG, "SMS quota exhausted ($MAX_SENDS demo messages)")
            return false
        }
        val token = apiToken.trim()
        val dest = toRecipient(recipient)
        if (dest == null) {
            Log.w(TAG, "SMS skipped: number is not a valid Sri Lanka mobile")
            return false
        }
        if (message.isBlank() || token.isBlank()) {
            if (token.isBlank()) Log.w(TAG, "SMS skipped: no text.lk token in local.properties or sms_config")
            return false
        }
        Log.i(TAG, "Sending SMS to …${dest.takeLast(4)} sender=${resolveSender()}")
        val connection = (URL(ENDPOINT).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            setRequestProperty("Authorization", "Bearer $token")
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
            connectTimeout = 15_000
            readTimeout = 15_000
            doOutput = true
        }
        val body = JSONObject()
            .put("recipient", dest)
            .put("sender_id", resolveSender())
            .put("type", "plain")
            .put("message", message.take(160))
            .toString()
        return try {
            connection.outputStream.use { it.write(body.toByteArray()) }
            val code = connection.responseCode
            val raw = (if (code in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader()?.readText().orEmpty()
            val ok = isSuccess(code, raw)
            if (ok) recordSuccess() else Log.w(TAG, "SMS failed HTTP $code $raw")
            ok
        } catch (error: Exception) {
            Log.w(TAG, "SMS failed: ${error.message}")
            false
        } finally {
            connection.disconnect()
        }
    }

    fun sendToMany(recipients: List<String>, message: String) {
        io.execute {
            var sent = 0
            for (dest in recipients.distinct()) {
                if (remaining() <= 0) break
                if (sendPlain(dest, message)) sent++
            }
            Log.i(TAG, "Sent $sent emergency SMS; remaining ${remaining()} of $MAX_SENDS")
        }
    }

    fun sendPlainAsync(recipient: String, message: String) {
        sendToMany(listOf(recipient), message)
    }

    fun toRecipient(raw: String): String? {
        val e164 = AuthRepository.normalizePhone(raw) ?: return null
        val digits = e164.filter(Char::isDigit)
        return digits.takeIf { it.startsWith("94") && it.length == 11 }
    }

    private fun isSuccess(code: Int, raw: String): Boolean {
        if (code !in 200..299) return false
        if (raw.isBlank()) return true
        return try {
            val json = JSONObject(raw)
            when (val status = json.opt("status")) {
                is Boolean -> status
                is String -> status.equals("success", true) || status == "1" || status.equals("true", true)
                is Number -> status.toInt() == 1
                else -> !json.has("error") && !json.has("errors")
            }
        } catch (_: Exception) {
            true
        }
    }

    private fun recordSuccess() {
        sendsUsed += 1
        persistLocal()
        FirebaseDatabase.getInstance()
            .getReference("FloodSystem/sms_config")
            .updateChildren(
                mapOf(
                    "sends_used" to sendsUsed,
                    "max_sends" to MAX_SENDS
                )
            )
    }

    private fun persistLocal() {
        appContext?.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            ?.edit()
            ?.putInt(KEY_USED, sendsUsed)
            ?.apply()
    }

    private fun resolveToken(): String =
        BuildConfig.TEXTLK_API_TOKEN.trim().ifBlank { remoteToken.trim() }

    private fun resolveSender(): String {
        val configured = BuildConfig.TEXTLK_SENDER_ID.trim().ifBlank { remoteSenderId.trim() }
        return configured.take(11).ifBlank { DEFAULT_SENDER }
    }
}
