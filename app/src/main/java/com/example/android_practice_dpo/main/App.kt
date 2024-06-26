package com.example.android_practice_dpo.main

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.room.Room
import com.example.android_practice_dpo.main.data.PhotoDatabase

const val DOWNLOAD_NOTIFICATION_CHANNEL = "download_channel"

class App : Application() {
    lateinit var photoDatabase: PhotoDatabase
    var appIsRunning: Boolean = false

    override fun onCreate() {
        super.onCreate()
        appIsRunning = true

        photoDatabase = Room.databaseBuilder(
            applicationContext,
            PhotoDatabase::class.java,
            "photoDb.db"
        )
            .build()

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                DOWNLOAD_NOTIFICATION_CHANNEL,
                "File download",
                NotificationManager.IMPORTANCE_HIGH
            )

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}