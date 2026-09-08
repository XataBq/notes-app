package ru.xatab.avitonotes.domain.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.xatab.avitonotes.domain.model.Note

interface NoteRepository {
    fun pages(query: String, order: ru.xatab.avitonotes.domain.model.SortOrder): Flow<PagingData<Note>>
    fun observe(): Flow<List<Note>>
    suspend fun get(id: Long): Note?
    suspend fun save(note: Note): Long
    suspend fun delete(note: Note)
}
