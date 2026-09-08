package ru.xatab.avitonotes.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.xatab.avitonotes.data.local.entity.NoteEntity

@Dao
interface NoteDao {
    @Query(
        """
        SELECT * FROM notes WHERE instr(searchTitle, :query) > 0
        ORDER BY
            CASE WHEN :oldest THEN createdAt END ASC,
            CASE WHEN :oldest THEN id END ASC,
            CASE WHEN NOT :oldest THEN createdAt END DESC,
            CASE WHEN NOT :oldest THEN id END DESC
    """,
    )
    fun pages(query: String, oldest: Boolean): androidx.paging.PagingSource<Int, NoteEntity>

    @Query("SELECT * FROM notes ORDER BY createdAt DESC, id DESC")
    fun observe(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun get(id: Long): NoteEntity?

    @Insert suspend fun insert(note: NoteEntity): Long

    @Update suspend fun update(note: NoteEntity): Int

    @Delete suspend fun delete(note: NoteEntity)
}
