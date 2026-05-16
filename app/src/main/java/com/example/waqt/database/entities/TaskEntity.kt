package com.example.waqt.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val subject: String,
    val slotStart: String,
    val slotEnd: String,
    val date: String,
    val isDone: Boolean = false,
    val priority: Int = 1
)
