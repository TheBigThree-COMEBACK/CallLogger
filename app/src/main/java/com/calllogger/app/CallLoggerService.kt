package com.calllogger.app
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class CallLoggerService : Service() {
    private val CHANNEL_ID = "CallLoggerChannel"

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Call Logger", NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
        startForeground(1001, NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Call Logger Active")
            .setContentText("Recording calls")
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .setOngoing(true)
            .build())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int) = START_STICKY
    override fun onBind(intent: Intent?): IBinder? = null
}
