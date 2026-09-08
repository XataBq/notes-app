package ru.xatab.avitonotes.data.local.entity

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.xatab.avitonotes.domain.model.TextMark
import ru.xatab.avitonotes.domain.usecase.TextFormatting

object TextMarksCodec {
    private val json = Json { ignoreUnknownKeys = true }

    fun encode(marks: List<TextMark>, length: Int): String =
        json.encodeToString(TextFormatting.normalize(marks, length))

    fun decode(value: String, length: Int): List<TextMark> = runCatching {
        TextFormatting.normalize(json.decodeFromString<List<TextMark>>(value), length)
    }.getOrDefault(emptyList())
}
