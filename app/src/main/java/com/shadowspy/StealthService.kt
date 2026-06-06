package com.shadowspy

import android.app.Service
import android.content.Intent
import android.os.IBinder

class StealthService : Service() {

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // TODO: Implement persistence, keylogger, screen capture, C2, etc.
        return START_STICKY
    }
}