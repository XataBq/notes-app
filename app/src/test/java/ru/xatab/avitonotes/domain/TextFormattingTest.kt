package ru.xatab.avitonotes.domain

import org.junit.Assert.*
import org.junit.Test
import ru.xatab.avitonotes.domain.model.TextMark
import ru.xatab.avitonotes.domain.model.TextStyle.*
import ru.xatab.avitonotes.domain.usecase.TextFormatting

class TextFormattingTest {
    @Test fun `overlapping styles stay independent and adjacent ranges merge`() {
        val marks = listOf(TextMark(0, 3, BOLD), TextMark(3, 6, BOLD), TextMark(2, 5, ITALIC))
        assertEquals(listOf(TextMark(0, 6, BOLD), TextMark(2, 5, ITALIC)), TextFormatting.normalize(marks, 6))
    }

    @Test fun `removing style from selection splits a range without dropping other styles`() {
        val marks = listOf(TextMark(0, 10, BOLD), TextMark(2, 8, UNDERLINE))
        assertEquals(
            listOf(TextMark(0, 3, BOLD), TextMark(7, 10, BOLD), TextMark(2, 8, UNDERLINE)),
            TextFormatting.toggle(marks, 7, 3, BOLD, 10),
        )
    }

    @Test fun `typing inside styled text extends style and shifts following ranges`() {
        val marks = listOf(TextMark(0, 4, BOLD), TextMark(5, 8, ITALIC))
        assertEquals(
            listOf(TextMark(0, 6, BOLD), TextMark(7, 10, ITALIC)),
            TextFormatting.edit("План дня", "Пл++ан дня", marks),
        )
    }

    @Test fun `deletion crossing a range keeps the remaining styled text`() {
        assertEquals(
            listOf(TextMark(1, 3, UNDERLINE)),
            TextFormatting.edit(
                "abcdef",
                "abef",
                listOf(TextMark(1, 5, UNDERLINE)),
            ),
        )
    }

    @Test fun `explicit plain typing interrupts style and does not swallow emoji offsets`() {
        assertEquals(
            listOf(TextMark(0, 1, BOLD), TextMark(3, 4, BOLD)),
            TextFormatting.edit("ab", "a🙂b", listOf(TextMark(0, 2, BOLD)), emptySet()),
        )
    }

    @Test fun `formatting the next input applies to replacement and normalizes invalid bounds`() {
        assertEquals(listOf(TextMark(0, 3, ITALIC)), TextFormatting.edit("old", "new", emptyList(), setOf(ITALIC)))
        assertEquals(
            listOf(TextMark(0, 3, BOLD)),
            TextFormatting.normalize(
                listOf(
                    TextMark(-5, 8, BOLD),
                    TextMark(8, 2, UNDERLINE),
                ),
                3,
            ),
        )
    }
}
