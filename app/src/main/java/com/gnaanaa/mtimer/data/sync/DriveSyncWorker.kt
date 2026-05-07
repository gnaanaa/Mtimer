package com.gnaanaa.mtimer.data.sync

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.gnaanaa.mtimer.MainActivity
import com.gnaanaa.mtimer.R
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class DriveSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val driveSync: DriveSync
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val (presets, sessions) = driveSync.syncPresets()
            if (presets > 0 || sessions > 0) {
                showSyncNotification(presets, sessions)
            }
            Result.success()
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    private fun showSyncNotification(presets: Int, sessions: Int) {
        val channelId = "cloud_sync_channel"
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            channelId,
            "Cloud Sync",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications for cloud backup and restore"
        }
        notificationManager.createNotificationChannel(channel)

        val intent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 10, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val message = buildString {
            if (presets > 0) append("$presets presets ")
            if (presets > 0 && sessions > 0) append("and ")
            if (sessions > 0) append("$sessions sessions ")
            append("restored from cloud.")
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Cloud Sync Complete")
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(1002, notification)
    }

    companion object {
        private const val UNIQUE_WORK_NAME = "DriveSyncWork"

        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequestBuilder<DriveSyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                UNIQUE_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }
}
