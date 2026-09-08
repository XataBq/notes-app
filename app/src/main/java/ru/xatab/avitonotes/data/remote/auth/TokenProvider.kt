package ru.xatab.avitonotes.data.remote.auth

import java.util.UUID
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.HttpException
import ru.xatab.avitonotes.data.remote.api.OAuthApi
import ru.xatab.avitonotes.data.remote.dto.AccessToken
import ru.xatab.avitonotes.domain.model.AppException
import ru.xatab.avitonotes.domain.model.FailureKind

class TokenProvider(
    private val api: OAuthApi,
    private val key: String,
    private val scope: String,
    private val clock: () -> Long = System::currentTimeMillis,
) {
    private val mutex = Mutex()
    private var cached: AccessToken? = null
    suspend fun token(): String = mutex.withLock {
        if (key.isBlank()) throw AppException(FailureKind.NOT_CONFIGURED)
        val current = cached

        if (current != null && expiryMillis(current.expiresAt) - clock() > 60_000) return@withLock current.token
        val response = api.token("Basic ${key.removePrefix("Basic ").trim()}", UUID.randomUUID().toString(), scope)
        if (response.token.isBlank() ||
            expiryMillis(response.expiresAt) <= clock()
        ) {
            throw AppException(FailureKind.INVALID_RESPONSE)
        }
        cached = response
        response.token
    }

    private fun expiryMillis(value: Long) = if (value < 100_000_000_000L) value * 1000 else value
    suspend fun <T> authorized(block: suspend (String) -> T): T {
        val first = token()
        return try {
            block("Bearer $first")
        } catch (error: HttpException) {
            if (error.code() != 401) throw error
            mutex.withLock { if (cached?.token == first) cached = null }
            block("Bearer ${token()}")
        }
    }
}
