package ru.xatab.avitonotes.domain.usecase

import ru.xatab.avitonotes.domain.model.Note
import ru.xatab.avitonotes.domain.model.SortOrder

fun selectNotes(notes: List<Note>, query: String, order: SortOrder): List<Note> = notes
    .filter { it.title.contains(query.trim(), ignoreCase = true) }
    .sortedWith(
        if (order == SortOrder.NEWEST) {
            compareByDescending<Note> { it.createdAt }.thenByDescending { it.id }
        } else {
            compareBy<Note> { it.createdAt }.thenBy { it.id }
        },
    )
