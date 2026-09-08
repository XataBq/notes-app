package ru.xatab.avitonotes.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable data class ChatMessage(val role: String, val content: String)
