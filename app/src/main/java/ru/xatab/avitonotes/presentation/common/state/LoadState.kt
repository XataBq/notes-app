package ru.xatab.avitonotes.presentation.common.state

import ru.xatab.avitonotes.presentation.common.text.UiText

sealed interface LoadState<out T> {
    data object Loading : LoadState<Nothing>
    data class Ready<T>(val value: T) : LoadState<T>
    data class Error(val message: UiText) : LoadState<Nothing>
}
