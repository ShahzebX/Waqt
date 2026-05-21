package com.example.waqt.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.waqt.database.entities.TaskEntity
import kotlinx.coroutines.flow.Flow
import kotlin.jvm.JvmSuppressWildcards

@JvmSuppressWildcards
@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Query("SELECT * FROM tasks WHERE date = :date ORDER BY slotStart ASC, id ASC")
    fun getTasksForDate(date: String): Flow<List<TaskEntity>>

    @Query("UPDATE tasks SET isDone = :isDone WHERE id = :id")
    suspend fun updateTaskDone(id: Int, isDone: Boolean): Int
}
