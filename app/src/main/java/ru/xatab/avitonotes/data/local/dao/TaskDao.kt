package ru.xatab.avitonotes.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.xatab.avitonotes.data.local.entity.TaskEntity

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY completed ASC, createdAt DESC, id DESC")
    fun observe(): Flow<List<TaskEntity>>

    @Insert suspend fun insert(task: TaskEntity): Long

    @Query("UPDATE tasks SET completed = :completed WHERE id = :id")
    suspend fun setCompleted(id: Long, completed: Boolean): Int

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun delete(id: Long)
}
