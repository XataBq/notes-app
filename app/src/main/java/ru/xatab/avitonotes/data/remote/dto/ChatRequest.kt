package ru.xatab.avitonotes.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable data class ChatRequest(
    val model: String = "GigaChat",
    val messages: List<ChatMessage>,
    val stream: Boolean = false,
    @SerialName("max_tokens") val maxTokens: Int = 128,
)
