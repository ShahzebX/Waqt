package com.example.waqt.repository

import com.example.waqt.database.PrayerDao
import com.example.waqt.database.entities.PrayerEntity
import com.example.waqt.network.AladhanApi

class PrayerRepository(
    private val api: AladhanApi,
    private val prayerDao: PrayerDao
) {
    suspend fun getCachedPrayers(date: String): List<PrayerEntity> {
        return prayerDao.getPrayersByDate(date)
    }
}
