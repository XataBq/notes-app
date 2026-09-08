package ru.xatab.avitonotes.presentation.tasks.state

import ru.xatab.avitonotes.presentation.common.state.VoiceState

sealed interface TaskInput {
    data object Idle : TaskInput
    data class Text(val title: String = "", val error: Boolean = false, val saving: Boolean = false) : TaskInput
    data class Voice(val state: VoiceState) : TaskInput
}
