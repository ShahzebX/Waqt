package com.example.waqt.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.waqt.database.entities.PrayerEntity
import com.example.waqt.database.entities.TaskEntity

@Database(
    entities = [PrayerEntity::class, TaskEntity::class],
    version = 1,
    exportSchema = false
)
abstract class WaqtDatabase : RoomDatabase() {
    abstract fun prayerDao(): PrayerDao
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var instance: WaqtDatabase? = null

        fun getInstance(context: Context): WaqtDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    WaqtDatabase::class.java,
                    "waqt.db"
                ).build().also { instance = it }
            }
        }
    }
}
