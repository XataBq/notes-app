package ru.xatab.avitonotes.domain

import org.junit.Assert.*
import org.junit.Test
import ru.xatab.avitonotes.domain.model.AppException
import ru.xatab.avitonotes.domain.model.Note
import ru.xatab.avitonotes.domain.model.SortOrder
import ru.xatab.avitonotes.domain.model.Task
import ru.xatab.avitonotes.domain.model.TaskFilter
import ru.xatab.avitonotes.domain.usecase.requireTitle
import ru.xatab.avitonotes.domain.usecase.selectNotes
import ru.xatab.avitonotes.domain.usecase.selectTasks

class SelectionTest {
    @Test fun `Cyrillic search is case insensitive and searches only titles`() {
        val notes = listOf(Note(1, "Купить КОФЕ", createdAt = 10), Note(2, "Чай", text = "кофе", createdAt = 20))
        assertEquals(listOf(1L), selectNotes(notes, "кофе", SortOrder.NEWEST).map { it.id })
        assertEquals(listOf(2L, 1L), selectNotes(notes, "", SortOrder.NEWEST).map { it.id })
        assertEquals(listOf(1L, 2L), selectNotes(notes, "", SortOrder.OLDEST).map { it.id })
    }

    @Test fun `completed tasks stay below active for both sort orders`() {
        val tasks =
            listOf(
                Task(1, "кофе 1", true, 40),
                Task(2, "кофе 2", false, 30),
                Task(3, "КОФЕ 3", false, 20),
                Task(4, "чай", false, 50),
                Task(5, "кофе 5", true, 10),
            )
        assertEquals(listOf(2L, 3L, 1L, 5L), selectTasks(tasks, "кофе", TaskFilter.ALL, SortOrder.NEWEST).map { it.id })
        assertEquals(listOf(3L, 2L, 5L, 1L), selectTasks(tasks, "кофе", TaskFilter.ALL, SortOrder.OLDEST).map { it.id })
        assertEquals(listOf(5L, 1L), selectTasks(tasks, "кофе", TaskFilter.COMPLETED, SortOrder.OLDEST).map { it.id })
        assertEquals(listOf(2L, 3L), selectTasks(tasks, "кофе", TaskFilter.ACTIVE, SortOrder.NEWEST).map { it.id })
    }

    @Test fun `empty title rejected and whitespace normalized`() {
        assertEquals("Идея", requireTitle("  Идея \n"))
        assertThrows(AppException::class.java) { requireTitle(" \n\t") }
    }

    @Test fun `equal timestamps have deterministic order`() {
        val notes = listOf(Note(3, "A", createdAt = 1), Note(2, "B", createdAt = 1))
        assertEquals(listOf(2L, 3L), selectNotes(notes, "", SortOrder.OLDEST).map { it.id })
    }
}
