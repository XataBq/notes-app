package ru.xatab.avitonotes.presentation.common.text

import kotlinx.coroutines.CancellationException
import ru.xatab.avitonotes.R
import ru.xatab.avitonotes.domain.model.AppException
import ru.xatab.avitonotes.domain.model.FailureKind
import ru.xatab.avitonotes.presentation.common.text.UiText

fun Throwable.userMessage(): UiText {
    if (this is CancellationException) throw this
    return when ((this as? AppException)?.kind) {
        FailureKind.EMPTY_TITLE -> UiText(R.string.error_empty_title)
        FailureKind.NOT_FOUND -> UiText(R.string.error_not_found)
        FailureKind.NETWORK -> UiText(R.string.error_network)
        FailureKind.TIMEOUT -> UiText(R.string.error_timeout)
        FailureKind.AUTH -> UiText(R.string.error_auth)
        FailureKind.FORBIDDEN -> UiText(R.string.error_forbidden)
        FailureKind.RATE_LIMIT -> UiText(R.string.error_rate_limit)
        FailureKind.SERVER -> UiText(R.string.error_server)
        FailureKind.INVALID_RESPONSE -> UiText(R.string.error_invalid_response)
        FailureKind.NOT_CONFIGURED -> UiText(R.string.error_not_configured)
        FailureKind.RECORDING -> UiText(R.string.error_recording)
        FailureKind.EMPTY_AUDIO -> UiText(R.string.error_empty_audio)
        FailureKind.IMAGE -> UiText(R.string.error_image)
        FailureKind.TLS -> UiText(R.string.error_tls)
        else -> UiText(R.string.error_storage)
    }
}
