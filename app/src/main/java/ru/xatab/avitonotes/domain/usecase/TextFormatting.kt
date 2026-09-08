package ru.xatab.avitonotes.domain.usecase

import ru.xatab.avitonotes.domain.model.TextMark
import ru.xatab.avitonotes.domain.model.TextStyle

object TextFormatting {
    fun normalize(marks: List<TextMark>, length: Int): List<TextMark> = TextStyle.entries.flatMap { style ->
        val sorted = marks.filter { it.style == style }
            .map { it.copy(start = it.start.coerceIn(0, length), end = it.end.coerceIn(0, length)) }
            .filter { it.start < it.end }.sortedBy { it.start }
        val merged = mutableListOf<TextMark>()
        sorted.forEach { mark ->
            val last = merged.lastOrNull()
            if (last != null && mark.start <= last.end) {
                merged[merged.lastIndex] = last.copy(end = maxOf(last.end, mark.end))
            } else {
                merged += mark
            }
        }
        merged
    }

    fun covers(marks: List<TextMark>, start: Int, end: Int, style: TextStyle): Boolean =
        start < end && marks.any { it.style == style && it.start <= start && it.end >= end }

    fun toggle(marks: List<TextMark>, start: Int, end: Int, style: TextStyle, length: Int): List<TextMark> {
        val from = minOf(start, end).coerceIn(0, length)
        val to = maxOf(start, end).coerceIn(0, length)
        if (from == to) return marks
        val current = normalize(marks, length)
        if (!covers(current, from, to, style)) return normalize(current + TextMark(from, to, style), length)
        return normalize(
            current.flatMap { mark ->
                if (mark.style != style || mark.end <= from || mark.start >= to) {
                    listOf(mark)
                } else {
                    buildList {
                        if (mark.start < from) add(mark.copy(end = from))
                        if (mark.end > to) add(mark.copy(start = to))
                    }
                }
            },
            length,
        )
    }

    fun edit(old: String, new: String, marks: List<TextMark>, typingStyles: Set<TextStyle>? = null): List<TextMark> {
        if (old == new) return marks
        var prefix = 0
        while (prefix < minOf(old.length, new.length) && old[prefix] == new[prefix]) prefix++
        var suffix = 0
        while (suffix < minOf(old.length, new.length) - prefix &&
            old[old.lastIndex - suffix] == new[new.lastIndex - suffix]
        ) {
            suffix++
        }
        val oldEnd = old.length - suffix
        val newEnd = new.length - suffix
        val delta = new.length - old.length
        val active = typingStyles ?: marks.filter { it.start < prefix && it.end >= prefix }.map { it.style }.toSet()
        val adjusted = marks.flatMap { mark ->
            buildList {
                if (mark.start < prefix) add(mark.copy(end = minOf(mark.end, prefix)))
                if (mark.end > oldEnd) add(mark.copy(start = maxOf(mark.start, oldEnd) + delta, end = mark.end + delta))
            }
        }
        val inserted = if (newEnd > prefix) active.map { TextMark(prefix, newEnd, it) } else emptyList()
        return normalize(adjusted + inserted, new.length)
    }
}
