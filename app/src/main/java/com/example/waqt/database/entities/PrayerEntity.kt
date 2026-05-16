package com.example.waqt.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prayers")
data class PrayerEntity(
    @PrimaryKey val id: String,
    val name: String,
    val time: String,
    val date: String,
    val epochMs: Long
)
