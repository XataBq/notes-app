package ru.xatab.avitonotes

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.io.File
import java.util.UUID
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import ru.xatab.avitonotes.data.local.database.NotesDatabase
import ru.xatab.avitonotes.data.local.repository.PreferencesRepository
import ru.xatab.avitonotes.data.local.repository.RoomNoteRepository
import ru.xatab.avitonotes.data.local.repository.RoomTaskRepository
import ru.xatab.avitonotes.domain.model.AccentColor
import ru.xatab.avitonotes.domain.model.AppSettings
import ru.xatab.avitonotes.domain.model.Note
import ru.xatab.avitonotes.domain.model.ThemeMode
import ru.xatab.avitonotes.domain.repository.ImageStorage

@RunWith(AndroidJUnit4::class)
class StorageTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test fun notesAndTaskStatusSurviveDatabaseReopen() = runBlocking {
        val name = "storage-test-${UUID.randomUUID()}.db"
        val removed = mutableListOf<String?>()
        val images = object : ImageStorage {
            override suspend fun import(source: String) = source
            override suspend fun delete(path: String?) {
                removed += path
            }
        }
        var db = Room.databaseBuilder(context, NotesDatabase::class.java, name).build()
        try {
            val notes = RoomNoteRepository(db, images)
            val id = notes.save(Note(0, "  План  ", "Текст", "old-image", 100))
            notes.save(Note(id, "Новый план", "Правки", "new-image", 999))
            assertTrue(removed.contains("old-image"))
            val tasks = RoomTaskRepository(db.tasks()) { 200 }
            val taskId = tasks.add("Задача")
            tasks.setCompleted(taskId, true)
            db.close()
            db = Room.databaseBuilder(context, NotesDatabase::class.java, name).build()
            val reopened = RoomNoteRepository(db, images)
            val note = reopened.observe().first().single()
            assertEquals("Новый план", note.title)
            assertEquals(100L, note.createdAt)
            assertEquals("new-image", note.imagePath)
            assertTrue(RoomTaskRepository(db.tasks()).observe().first().single().completed)
            reopened.delete(note)
            assertTrue(reopened.observe().first().isEmpty())
            assertTrue(removed.contains("new-image"))
        } finally {
            db.close()
            context.deleteDatabase(name)
        }
    }

    @Test fun appearanceSurvivesStoreReopenAndReset() = runBlocking {
        val file = File(context.cacheDir, "settings-${UUID.randomUUID()}.preferences_pb")
        var scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        try {
            var repo = PreferencesRepository(PreferenceDataStoreFactory.create(scope = scope) { file })
            repo.setTheme(ThemeMode.DARK)
            repo.setAccent(AccentColor.GREEN)
            scope.coroutineContext[Job]!!.cancelAndJoin()
            scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            repo = PreferencesRepository(PreferenceDataStoreFactory.create(scope = scope) { file })
            assertEquals(AppSettings(ThemeMode.DARK, AccentColor.GREEN), repo.settings.first())
            repo.reset()
            assertEquals(AppSettings(), repo.settings.first())
        } finally {
            scope.coroutineContext[Job]!!.cancelAndJoin()
            file.delete()
        }
    }
}
