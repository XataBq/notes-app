package ru.xatab.avitonotes.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable data class SpeechResponse(val result: List<String> = emptyList(), val status: Int)
