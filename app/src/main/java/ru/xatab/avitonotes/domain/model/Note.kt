package ru.xatab.avitonotes.domain.model

data class Note(
    val id: Long = 0,
    val title: String,
    val text: String = "",
    val imagePath: String? = null,
    val createdAt: Long,
    val marks: List<TextMark> = emptyList(),
)
