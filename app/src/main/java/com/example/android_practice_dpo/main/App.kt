package com.example.android_practice_dpo.main

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.room.Room
import com.example.android_practice_dpo.main.data.PhotoDatabase
import dagger.hilt.android.HiltAndroidApp

const val DOWNLOAD_NOTIFICATION_CHANNEL = "download_channel"

@HiltAndroidApp
class App : Application() {
    lateinit var photoDatabase: PhotoDatabase

    override fun onCreate() {
        super.onCreate()

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