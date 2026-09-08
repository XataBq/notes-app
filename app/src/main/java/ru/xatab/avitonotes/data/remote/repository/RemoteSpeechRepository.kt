package ru.xatab.avitonotes.data.remote.repository

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import ru.xatab.avitonotes.data.remote.api.SpeechApi
import ru.xatab.avitonotes.data.remote.auth.TokenProvider
import ru.xatab.avitonotes.data.remote.error.remoteCall
import ru.xatab.avitonotes.domain.model.AppException
import ru.xatab.avitonotes.domain.model.FailureKind
import ru.xatab.avitonotes.domain.repository.SpeechRepository

class RemoteSpeechRepository(private val api: SpeechApi, private val tokens: TokenProvider) : SpeechRepository {
    override suspend fun recognize(audio: ByteArray): String = remoteCall {
        if (audio.size < 3200) throw AppException(FailureKind.EMPTY_AUDIO)
        if (audio.size > 2_000_000) throw AppException(FailureKind.RECORDING)
        val body = audio.toRequestBody("audio/x-pcm;bit=16;rate=16000".toMediaType())
        val response = tokens.authorized { api.recognize(it, body) }
        if (response.status != 200) throw AppException(FailureKind.INVALID_RESPONSE)
        response.result.joinToString(" ").trim().takeIf { it.isNotBlank() }
            ?: throw AppException(FailureKind.INVALID_RESPONSE)
    }
}
