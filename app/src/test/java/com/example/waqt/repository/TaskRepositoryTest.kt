package com.example.waqt.repository

import com.example.waqt.database.TaskDao
import com.example.waqt.database.entities.TaskEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class TaskRepositoryTest {
    @Test
    fun `addQuickFocusTask inserts and observes task`() = runTest {
        val dao = FakeTaskDao()
        val repository = TaskRepository(dao)

        repository.addQuickFocusTask(title = "Read Quran", date = "2026-05-16")

        val tasks = repository.observeTasksForDate("2026-05-16").first()
        assertEquals(1, tasks.size)
        assertEquals("Read Quran", tasks.first().title)
        assertFalse(tasks.first().isDone)
    }
}

private class FakeTaskDao : TaskDao {
    private val store = mutableListOf<TaskEntity>()
    private val flow = MutableStateFlow<List<TaskEntity>>(emptyList())

    override suspend fun insertTask(task: TaskEntity): Long {
        val saved = task.copy(id = store.size + 1)
        store.add(saved)
        flow.value = store.toList()
        return saved.id.toLong()
    }

    override fun getTasksForDate(date: String): Flow<List<TaskEntity>> = flow

    override suspend fun updateTaskDone(id: Int, isDone: Boolean): Int {
        val index = store.indexOfFirst { it.id == id }
        if (index < 0) return 0
        store[index] = store[index].copy(isDone = isDone)
        flow.value = store.toList()
        return 1
    }
}
