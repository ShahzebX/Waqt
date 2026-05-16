package com.example.waqt.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.waqt.database.entities.PrayerEntity

@Dao
interface PrayerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrayers(prayers: List<PrayerEntity>)

    @Query("SELECT * FROM prayers WHERE date = :date ORDER BY epochMs ASC")
    suspend fun getPrayersByDate(date: String): List<PrayerEntity>
}
