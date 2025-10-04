package com.calllogger.app
import android.content.*
import android.provider.ContactsContract
import android.telephony.TelephonyManager
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class CallReceiver : BroadcastReceiver() {
    private var lastState = TelephonyManager.CALL_STATE_IDLE
    private var callStartTime = 0L
    private var isIncoming = false
    private var savedNumber: String? = null

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_NEW_OUTGOING_CALL) {
            savedNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER)
            isIncoming = false
        } else if (intent.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            when (intent.getStringExtra(TelephonyManager.EXTRA_STATE)) {
                TelephonyManager.EXTRA_STATE_RINGING -> {
                    isIncoming = true
                    savedNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                    lastState = TelephonyManager.CALL_STATE_RINGING
                }
                TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                    if (lastState == TelephonyManager.CALL_STATE_RINGING) {
                        callStartTime = System.currentTimeMillis()
                    }
                    lastState = TelephonyManager.CALL_STATE_OFFHOOK
                }
                TelephonyManager.EXTRA_STATE_IDLE -> {
                    val duration = if (callStartTime > 0) (System.currentTimeMillis() - callStartTime) / 1000 else 0
                    val type = if (lastState == TelephonyManager.CALL_STATE_RINGING) "MISSED" 
                              else if (isIncoming) "INCOMING" else "OUTGOING"
                    saveCallLog(context, savedNumber, type, duration)
                    lastState = TelephonyManager.CALL_STATE_IDLE
                    callStartTime = 0L
                }
            }
        }
    }

    private fun saveCallLog(context: Context, phoneNumber: String?, callType: String, duration: Long) {
        if (phoneNumber == null) return
        val contactName = context.contentResolver.query(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI.buildUpon().appendPath(phoneNumber).build(),
            arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME), null, null, null
        )?.use { if (it.moveToFirst()) it.getString(0) else null }
        
        val logEntry = JSONObject().apply {
            put("phone_number", phoneNumber)
            put("contact_name", contactName ?: "Unknown")
            put("call_type", callType)
            put("duration_seconds", duration)
            put("timestamp", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))
        }
        
        val folder = File(context.getExternalFilesDir(null), "CallHistory")
        if (!folder.exists()) folder.mkdirs()
        File(folder, "call_logs.json").appendText("$logEntry\n")
    }
}
