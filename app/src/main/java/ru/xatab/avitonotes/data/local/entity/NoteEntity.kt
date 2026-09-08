package ru.xatab.avitonotes.data.local.entity

import androidx.room.*
import ru.xatab.avitonotes.domain.model.Note

@Entity(tableName = "notes", indices = [Index(value = ["createdAt", "id"])])
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val text: String,
    val imagePath: String?,
    val createdAt: Long,
    @ColumnInfo(defaultValue = "'[]'") val marks: String = "[]",
    @ColumnInfo(defaultValue = "''") val searchTitle: String = title.lowercase(java.util.Locale.ROOT),
) {
    fun toDomain() = Note(id, title, text, imagePath, createdAt, TextMarksCodec.decode(marks, text.length))
}
