package ru.xatab.avitonotes.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class TextMark(val start: Int, val end: Int, val style: TextStyle)
