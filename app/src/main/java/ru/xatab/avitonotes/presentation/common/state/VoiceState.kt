package ru.xatab.avitonotes.presentation.common.state

import ru.xatab.avitonotes.presentation.common.text.UiText

sealed interface VoiceState {
    data object Idle : VoiceState
    data class Recording(val seconds: Int = 0) : VoiceState
    data object Processing : VoiceState
    data class Error(val message: UiText) : VoiceState
}
val VoiceState.busy get() = this is VoiceState.Recording || this is VoiceState.Processing
