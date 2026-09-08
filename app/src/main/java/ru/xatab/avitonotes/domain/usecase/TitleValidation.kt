package ru.xatab.avitonotes.domain.usecase

import ru.xatab.avitonotes.domain.model.AppException
import ru.xatab.avitonotes.domain.model.FailureKind

fun requireTitle(value: String): String = value.trim().ifEmpty { throw AppException(FailureKind.EMPTY_TITLE) }
