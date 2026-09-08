package ru.xatab.avitonotes.data.local.database

import androidx.room.*
import ru.xatab.avitonotes.data.local.dao.NoteDao
import ru.xatab.avitonotes.data.local.dao.TaskDao
import ru.xatab.avitonotes.data.local.entity.NoteEntity
import ru.xatab.avitonotes.data.local.entity.TaskEntity

@Database(entities = [NoteEntity::class, TaskEntity::class], version = 2, exportSchema = true)
abstract class NotesDatabase : RoomDatabase() {
    abstract fun notes(): NoteDao
    abstract fun tasks(): TaskDao
}
