package ru.xatab.avitonotes.domain.model

class AppException(val kind: FailureKind, cause: Throwable? = null) : Exception(kind.name, cause)
