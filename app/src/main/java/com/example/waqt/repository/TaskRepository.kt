package com.example.waqt.repository

import com.example.waqt.database.TaskDao
import com.example.waqt.database.entities.TaskEntity
import com.example.waqt.model.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TaskRepository(
    private val taskDao: TaskDao
) {
    fun observeTasksForDate(date: String): Flow<List<Task>> {
        return taskDao.getTasksForDate(date).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun addQuickFocusTask(title: String, date: String) {
        taskDao.insertTask(
            TaskEntity(
                title = title,
                subject = "",
                slotStart = PLACEHOLDER_SLOT,
                slotEnd = PLACEHOLDER_SLOT,
                date = date,
                isDone = false,
                priority = PRIORITY_LOW
            )
        )
    }

    suspend fun setTaskDone(taskId: Int, isDone: Boolean) {
        taskDao.updateTaskDone(taskId, isDone)
    }

    private fun TaskEntity.toDomain(): Task {
        return Task(
            id = id,
            title = title,
            subject = subject,
            slotStart = slotStart,
            slotEnd = slotEnd,
            date = date,
            isDone = isDone,
            priority = priority
        )
    }

    companion object {
        const val PLACEHOLDER_SLOT = "--"
        const val PRIORITY_LOW = 1
    }
}
