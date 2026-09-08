package ru.xatab.avitonotes.domain.repository

interface SpeechRepository {
    suspend fun recognize(audio: ByteArray): String
}
