package ru.xatab.avitonotes.domain.repository

import ru.xatab.avitonotes.domain.model.TokenBalance

interface GigaChatRepository {
    suspend fun balance(): List<TokenBalance>
    suspend fun makeTask(text: String): String
}
