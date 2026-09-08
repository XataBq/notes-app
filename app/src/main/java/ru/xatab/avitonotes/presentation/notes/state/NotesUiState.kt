package ru.xatab.avitonotes.presentation.notes.state

import ru.xatab.avitonotes.domain.model.Note
import ru.xatab.avitonotes.domain.model.NoteLayout
import ru.xatab.avitonotes.domain.model.SortOrder
import ru.xatab.avitonotes.presentation.common.text.UiText

data class NotesUiState(
    val query: String = "",
    val appliedQuery: String = "",

    val order: SortOrder = SortOrder.NEWEST,
    val layout: NoteLayout = NoteLayout.LIST,

    val deleting: Boolean = false,
    val deletingIds: Set<Long> = emptySet(),
    val pendingDeletion: Note? = null,

    val error: UiText? = null,
)
