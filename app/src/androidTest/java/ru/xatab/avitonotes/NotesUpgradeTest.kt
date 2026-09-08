package ru.xatab.avitonotes

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.paging.PagingSource
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import ru.xatab.avitonotes.data.local.database.NotesDatabase
import ru.xatab.avitonotes.data.local.database.NotesMigrations
import ru.xatab.avitonotes.data.local.entity.NoteEntity
import ru.xatab.avitonotes.data.local.entity.TextMarksCodec
import ru.xatab.avitonotes.domain.model.TextMark
import ru.xatab.avitonotes.domain.model.TextStyle
import ru.xatab.avitonotes.presentation.editor.share.NoteShareIntentFactory

@RunWith(AndroidJUnit4::class)
class NotesUpgradeTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test fun migrationPreservesOldNotesAndSupportsCyrillicSearch() = runBlocking {
        val name = "migration-${System.nanoTime()}.db"
        context.openOrCreateDatabase(name, Context.MODE_PRIVATE, null).use { old ->
            old.execSQL(
                "CREATE TABLE notes (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title TEXT NOT NULL, text TEXT NOT NULL, imagePath TEXT, createdAt INTEGER NOT NULL)",
            )
            old.execSQL(
                "CREATE TABLE tasks (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title TEXT NOT NULL, completed INTEGER NOT NULL, createdAt INTEGER NOT NULL)",
            )
            old.execSQL("INSERT INTO notes VALUES (1, 'МОСКВА', 'Старый текст', NULL, 42)")
            old.execSQL("INSERT INTO tasks VALUES (1, 'Старая задача', 1, 43)")
            old.version = 1
        }
        val db = Room.databaseBuilder(context, NotesDatabase::class.java, name)
            .addMigrations(NotesMigrations.FROM_1_TO_2).build()
        try {
            val old = db.notes().get(1)!!.toDomain()
            assertEquals("Старый текст", old.text)
            assertEquals(42L, old.createdAt)
            assertTrue(old.marks.isEmpty())
            val page = db.notes().pages("моск", false).load(PagingSource.LoadParams.Refresh(null, 20, false))
            assertEquals(listOf(1L), (page as PagingSource.LoadResult.Page).data.map { it.id })
            assertEquals(
                1L,
                db.openHelper.readableDatabase.query("SELECT COUNT(*) FROM tasks WHERE completed = 1").use {
                    it.moveToFirst()
                    it.getLong(0)
                },
            )
            val marks = listOf(TextMark(0, 6, TextStyle.BOLD), TextMark(3, 10, TextStyle.UNDERLINE))
            db.notes().update(db.notes().get(1)!!.copy(marks = TextMarksCodec.encode(marks, old.text.length)))
            assertEquals(marks, db.notes().get(1)!!.toDomain().marks)
        } finally {
            db.close()
            context.deleteDatabase(name)
        }
    }

    @Test fun roomLoadsSeparatePagesWithStableOrderAndLiteralSearch() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context, NotesDatabase::class.java).build()
        try {
            repeat(65) { index ->
                db.notes().insert(NoteEntity(title = "Заметка $index", text = "", imagePath = null, createdAt = 100L))
            }
            db.notes().insert(NoteEntity(title = "100% _готово_", text = "", imagePath = null, createdAt = 101L))
            val source = db.notes().pages("заметка", false)
            val first = source.load(PagingSource.LoadParams.Refresh(null, 20, false)) as PagingSource.LoadResult.Page
            assertEquals(20, first.data.size)
            assertEquals(65L, first.data.first().id)
            val next = source.load(
                PagingSource.LoadParams.Append(first.nextKey!!, 20, false),
            ) as PagingSource.LoadResult.Page
            assertEquals(20, next.data.size)
            assertTrue(first.data.map { it.id }.intersect(next.data.map { it.id }.toSet()).isEmpty())
            assertEquals(45L, next.data.first().id)
            val oldest = db.notes().pages(
                "заметка",
                true,
            ).load(PagingSource.LoadParams.Refresh(null, 20, false)) as PagingSource.LoadResult.Page
            assertEquals(1L, oldest.data.first().id)
            val literal = db.notes().pages(
                "% _",
                false,
            ).load(PagingSource.LoadParams.Refresh(null, 20, false)) as PagingSource.LoadResult.Page
            assertEquals(1, literal.data.size)
            assertEquals("100% _готово_", literal.data.single().title)
        } finally {
            db.close()
        }
    }

    @Test fun shareIncludesTextStylesAndGrantsOnlyTheAttachedImage() {
        val factory = NoteShareIntentFactory(context)
        val marks =
            listOf(
                TextMark(0, 4, TextStyle.BOLD),
                TextMark(5, 10, TextStyle.ITALIC),
                TextMark(0, 10, TextStyle.UNDERLINE),
            )
        val text = factory.create("План", "Дело номер", marks, null)
        assertEquals(Intent.ACTION_SEND, text.action)
        assertEquals("text/plain", text.type)
        assertEquals("План\n\nДело номер", text.getCharSequenceExtra(Intent.EXTRA_TEXT).toString())
        val html = text.getStringExtra(Intent.EXTRA_HTML_TEXT)!!
        assertTrue(html.contains("<b>") && html.contains("<i>") && html.contains("<u>"))
        val directory = File(context.filesDir, "note-images").apply { mkdirs() }
        val image = File.createTempFile("share-test-", ".image", directory)
        val bitmap = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888)
        image.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
        try {
            val intent = factory.create("План", "Текст", emptyList(), image.path)
            assertEquals("image/png", intent.type)
            assertEquals("content", intent.clipData!!.getItemAt(0).uri.scheme)
            assertTrue(intent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0)
            val outside = factory.create("План", "", emptyList(), context.cacheDir.path)
            assertFalse(outside.hasExtra(Intent.EXTRA_STREAM))
        } finally {
            image.delete()
        }
    }
}
