package ru.xatab.avitonotes.domain.repository

interface VoiceRecorder {
    suspend fun start()
    suspend fun stop(): ByteArray
    suspend fun cancel()
}
