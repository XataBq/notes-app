package ru.xatab.avitonotes.data.remote.error

import java.io.IOException
import java.net.SocketTimeoutException
import javax.net.ssl.SSLException
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import ru.xatab.avitonotes.domain.model.AppException
import ru.xatab.avitonotes.domain.model.FailureKind

internal suspend fun <T> remoteCall(block: suspend () -> T): T = try {
    block()
} catch (cancelled: CancellationException) {
    throw cancelled
} catch (known: AppException) {
    throw known
} catch (error: Exception) {
    val kind = when (error) {
        is HttpException -> when (error.code()) {
            401 -> FailureKind.AUTH
            403 -> FailureKind.FORBIDDEN
            429 -> FailureKind.RATE_LIMIT
            in 500..599 -> FailureKind.SERVER
            else -> FailureKind.INVALID_RESPONSE
        }
        is SocketTimeoutException -> FailureKind.TIMEOUT
        is SSLException -> FailureKind.TLS
        is SerializationException -> FailureKind.INVALID_RESPONSE
        is IOException -> FailureKind.NETWORK
        else -> FailureKind.INVALID_RESPONSE
    }
    throw AppException(kind, error)
}
