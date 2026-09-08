package ru.xatab.avitonotes.domain.model

enum class FailureKind {
    EMPTY_TITLE,
    NOT_FOUND,
    NETWORK,
    TIMEOUT,
    AUTH,
    FORBIDDEN,
    RATE_LIMIT,
    SERVER,
    INVALID_RESPONSE,
    NOT_CONFIGURED,
    RECORDING,
    EMPTY_AUDIO,
    IMAGE,
    STORAGE,
    TLS,
}
