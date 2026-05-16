package com.example.waqt.worker

import com.example.waqt.model.Prayer

interface PrayerNotificationScheduler {
    suspend fun schedule(prayers: List<Prayer>)
}

object NoOpPrayerNotificationScheduler : PrayerNotificationScheduler {
    override suspend fun schedule(prayers: List<Prayer>) = Unit
}
