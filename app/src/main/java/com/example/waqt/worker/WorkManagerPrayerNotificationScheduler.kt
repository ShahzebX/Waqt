package com.example.waqt.worker

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.waqt.model.Prayer
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.concurrent.TimeUnit

class WorkManagerPrayerNotificationScheduler(
    context: Context
) : PrayerNotificationScheduler {
    private val appContext = context.applicationContext
    private val workManager = WorkManager.getInstance(appContext)

    override suspend fun schedule(prayers: List<Prayer>) {
        prayers.forEach { prayer ->
            val triggerAtEpochMs = prayer.toEpochMillisOrNull() ?: return@forEach
            val delayMs = triggerAtEpochMs - System.currentTimeMillis() - tenMinutesMs
            if (delayMs <= 0L) return@forEach

            val request = OneTimeWorkRequestBuilder<PrayerNotificationWorker>()
                .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                .setInputData(
                    workDataOf(
                        PrayerNotificationWorker.PrayerNameKey to prayer.name,
                        PrayerNotificationWorker.PrayerTimeKey to prayer.time
                    )
                )
                .build()

            val uniqueWorkName = "prayer_notification_${prayer.date}_${prayer.name}"
            workManager.enqueueUniqueWork(uniqueWorkName, ExistingWorkPolicy.REPLACE, request)
        }
    }

    private fun Prayer.toEpochMillisOrNull(): Long? {
        return try {
            val localDate = LocalDate.parse(date)
            val localTime = LocalTime.parse(time, prayerTimeFormatter)
            LocalDateTime.of(localDate, localTime)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        } catch (exception: DateTimeParseException) {
            null
        }
    }

    private companion object {
        val prayerTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("H:mm")
        const val tenMinutesMs = 10 * 60 * 1000L
    }
}
