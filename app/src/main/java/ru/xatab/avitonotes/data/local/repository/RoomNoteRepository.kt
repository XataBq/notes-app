package ru.xatab.avitonotes.data.local.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.map
import androidx.room.withTransaction
import java.util.Locale
import kotlinx.coroutines.flow.map
import ru.xatab.avitonotes.data.local.database.NotesDatabase
import ru.xatab.avitonotes.data.local.entity.NoteEntity
import ru.xatab.avitonotes.data.local.entity.TextMarksCodec
import ru.xatab.avitonotes.domain.model.AppException
import ru.xatab.avitonotes.domain.model.FailureKind
import ru.xatab.avitonotes.domain.model.Note
import ru.xatab.avitonotes.domain.model.SortOrder
import ru.xatab.avitonotes.domain.repository.ImageStorage
import ru.xatab.avitonotes.domain.repository.NoteRepository
import ru.xatab.avitonotes.domain.usecase.requireTitle

class RoomNoteRepository(private val db: NotesDatabase, private val images: ImageStorage) : NoteRepository {
    override fun pages(query: String, order: SortOrder) = Pager(
        config = PagingConfig(pageSize = 20, initialLoadSize = 20, prefetchDistance = 5, enablePlaceholders = false),
        pagingSourceFactory = { dao.pages(query.trim().lowercase(Locale.ROOT), order == SortOrder.OLDEST) },
    ).flow.map { page -> page.map { it.toDomain() } }

    private val dao get() = db.notes()
    override fun observe() = dao.observe().map { rows -> rows.map { it.toDomain() } }
    override suspend fun get(id: Long) = dao.get(id)?.toDomain()
    override suspend fun save(note: Note): Long {
        val entity = NoteEntity(
            id = note.id,
            title = requireTitle(note.title),
            text = note.text,
            imagePath = note.imagePath,
            createdAt = note.createdAt,
            marks = TextMarksCodec.encode(note.marks, note.text.length),
        )
        var previousImage: String? = null
        val id = db.withTransaction {
            if (note.id == 0L) {
                dao.insert(entity)
            } else {
                val previous = dao.get(note.id) ?: throw AppException(FailureKind.NOT_FOUND)
                previousImage = previous.imagePath
                if (dao.update(entity.copy(createdAt = previous.createdAt)) !=
                    1
                ) {
                    throw AppException(FailureKind.NOT_FOUND)
                }
                note.id
            }
        }

        if (previousImage != note.imagePath) images.delete(previousImage)
        return id
    }

    override suspend fun delete(note: Note) {
        val image = db.withTransaction {
            val actual = dao.get(note.id) ?: return@withTransaction null
            dao.delete(actual)
            actual.imagePath
        }
        images.delete(image)
    }
}
