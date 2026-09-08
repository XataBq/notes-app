package ru.xatab.avitonotes.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.util.Locale

object NotesMigrations {
    val FROM_1_TO_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE notes ADD COLUMN marks TEXT NOT NULL DEFAULT '[]'")
            db.execSQL("ALTER TABLE notes ADD COLUMN searchTitle TEXT NOT NULL DEFAULT ''")
            db.query("SELECT id, title FROM notes").use { cursor ->
                while (cursor.moveToNext()) {
                    db.execSQL(
                        "UPDATE notes SET searchTitle = ? WHERE id = ?",
                        arrayOf<Any>(cursor.getString(1).lowercase(Locale.ROOT), cursor.getLong(0)),
                    )
                }
            }
            db.execSQL("CREATE INDEX IF NOT EXISTS index_notes_createdAt_id ON notes(createdAt, id)")
        }
    }
}
