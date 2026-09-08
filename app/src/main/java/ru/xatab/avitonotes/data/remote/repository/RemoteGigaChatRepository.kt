package ru.xatab.avitonotes.data.remote.repository

import ru.xatab.avitonotes.data.remote.api.GigaApi
import ru.xatab.avitonotes.data.remote.auth.TokenProvider
import ru.xatab.avitonotes.data.remote.dto.ChatMessage
import ru.xatab.avitonotes.data.remote.dto.ChatRequest
import ru.xatab.avitonotes.data.remote.error.remoteCall
import ru.xatab.avitonotes.domain.model.AppException
import ru.xatab.avitonotes.domain.model.FailureKind
import ru.xatab.avitonotes.domain.model.TokenBalance
import ru.xatab.avitonotes.domain.repository.GigaChatRepository

class RemoteGigaChatRepository(
    private val api: GigaApi,
    private val tokens: TokenProvider,
    private val taskPrompt: String,
) : GigaChatRepository {
    override suspend fun balance() = remoteCall {
        tokens.authorized { api.balance(it) }.balance.map { TokenBalance(it.usage, it.value) }
    }

    override suspend fun makeTask(text: String): String = remoteCall {
        val response = tokens.authorized { token ->
            api.chat(
                token,
                ChatRequest(
                    messages = listOf(
                        ChatMessage(
                            "system",
                            taskPrompt,
                        ),
                        ChatMessage("user", text),
                    ),
                ),
            )
        }
        response.choices.firstOrNull()?.message?.content?.trim()?.takeIf { it.isNotBlank() }
            ?: throw AppException(FailureKind.INVALID_RESPONSE)
    }
}
