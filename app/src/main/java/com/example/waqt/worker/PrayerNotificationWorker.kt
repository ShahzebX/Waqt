package com.example.waqt.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.waqt.R
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class PrayerNotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val prayerName = inputData.getString(PrayerNameKey) ?: return Result.failure()
        val prayerTime = inputData.getString(PrayerTimeKey) ?: return Result.failure()

        if (!applicationContext.canPostNotifications()) {
            return Result.success()
        }

        ensureNotificationChannel()

        val notification = NotificationCompat.Builder(applicationContext, NotificationChannelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("$prayerName in 10 minutes")
            .setContentText("Time: $prayerTime — wrap up your current task.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(applicationContext)
            .notify(prayerName.hashCode(), notification)

        return Result.success()
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            NotificationChannelId,
            NotificationChannelName,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = NotificationChannelDescription
        }
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE)
            as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun Context.canPostNotifications(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        const val PrayerNameKey = "prayer_name"
        const val PrayerTimeKey = "prayer_time"
        private const val NotificationChannelId = "waqt_prayer_notifications"
        private const val NotificationChannelName = "Prayer reminders"
        private const val NotificationChannelDescription =
            "Reminders before each prayer time."
    }
}
