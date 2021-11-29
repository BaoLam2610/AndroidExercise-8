package com.example.musicappmvvm

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.musicappmvvm.utils.Constants.CHANNEL_ID

class MusicApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Now Playing Song",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Important to showing song"
            channel.setSound(null, null)
//        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}