package com.example.waqt.repository

import com.example.waqt.database.TaskDao
import com.example.waqt.database.entities.TaskEntity
import kotlinx.coroutines.flow.Flow

class TaskRepository(
    private val taskDao: TaskDao
) {
    fun getTasksForDate(date: String): Flow<List<TaskEntity>> {
        return taskDao.getTasksForDate(date)
    }

    suspend fun insertTask(task: TaskEntity): Long {
        return taskDao.insertTask(task)
    }
}
