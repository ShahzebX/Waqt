package com.example.waqt.model

data class Task(
    val id: Int = 0,
    val title: String,
    val subject: String,
    val slotStart: String,
    val slotEnd: String,
    val date: String,
    val isDone: Boolean = false,
    val priority: Int = 1
)
