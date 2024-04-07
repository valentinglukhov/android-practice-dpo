package com.example.android_practice_dpo.main.adapter

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.FileProvider
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.android_practice_dpo.R
import com.example.android_practice_dpo.main.DOWNLOAD_NOTIFICATION_CHANNEL
import com.example.android_practice_dpo.main.api.FileDownloadApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.random.Random

private const val PHOTO_PATH = "photo_path"
private const val PHOTO_QUERY = "photo_query"
private const val PHOTO_ID = "photo_id"

class DownloadWorker(
    private val context: Context,
    private val workerParameters: WorkerParameters,
) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        val photoPath = workerParameters.inputData.getString(PHOTO_PATH)
        val photoQuery = workerParameters.inputData.getString(PHOTO_QUERY)
        val photoId = workerParameters.inputData.getString(PHOTO_ID)
        val response = FileDownloadApi.instance.downloadPhoto(path = photoPath!!,
            query = photoQuery!!
        )
        response.body()?.let { body ->
            return withContext(Dispatchers.IO) {
                val file = File(context.cacheDir, "${photoId}.jpg")
                val outputStream = FileOutputStream(file)
                outputStream.use { stream ->
                    try {
                        stream.write(body.bytes())
                    } catch (e: IOException) {
                        return@withContext Result.failure(
                            workDataOf(WorkerKeys.ERROR_MSG to e.localizedMessage)
                        )
                    }
                }
                val uri = FileProvider.getUriForFile(context, context.applicationContext.packageName + ".provider", file)
                createNotification(uri)
                Result.success(
                    workDataOf(
                        WorkerKeys.IMAGE_URI to uri.toString()
                    )
                )
            }
        }
        if (!response.isSuccessful) {
            if (response.code().toString().startsWith("5")) {
                return Result.retry()
            }
            return Result.failure(
                workDataOf(WorkerKeys.ERROR_MSG to "Network error")
            )
        }
        return Result.failure(
            workDataOf(WorkerKeys.ERROR_MSG to "Unknown error")
        )
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    fun createNotification(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        Log.d("UNSPLASH_DEBUG", "ImageUri$uri")
        intent.setDataAndType(uri, "image/*")
        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        } else {
            PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        val notification = NotificationCompat.Builder(context, DOWNLOAD_NOTIFICATION_CHANNEL)
            .setSmallIcon(R.drawable.icon_download)
            .setContentTitle(context.getString(R.string.photo_downloaded))
            .setContentText(context.getString(R.string.click_to_open_downloaded_photo))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(context).notify(Random.nextInt(), notification)
        }
    }
}

object WorkerKeys {
    const val ERROR_MSG = "error_message"
    const val IMAGE_URI = "image_uri"
}