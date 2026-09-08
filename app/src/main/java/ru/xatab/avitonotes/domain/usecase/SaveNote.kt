package ru.xatab.avitonotes.domain.usecase

import ru.xatab.avitonotes.domain.model.Note
import ru.xatab.avitonotes.domain.repository.NoteRepository

class SaveNote(private val repository: NoteRepository) {
    suspend operator fun invoke(note: Note) = repository.save(note.copy(title = requireTitle(note.title)))
}
