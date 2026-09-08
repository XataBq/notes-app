package ru.xatab.avitonotes.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.xatab.avitonotes.domain.model.Task

interface TaskRepository {
    fun observe(): Flow<List<Task>>
    suspend fun add(title: String): Long
    suspend fun setCompleted(id: Long, completed: Boolean)
    suspend fun delete(id: Long)
}
