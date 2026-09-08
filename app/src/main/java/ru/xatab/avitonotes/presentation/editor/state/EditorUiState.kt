package ru.xatab.avitonotes.presentation.editor.state

import ru.xatab.avitonotes.domain.model.TextMark
import ru.xatab.avitonotes.domain.model.TextStyle
import ru.xatab.avitonotes.presentation.common.state.VoiceState
import ru.xatab.avitonotes.presentation.common.text.UiText

data class EditorUiState(
    val title: String = "",
    val text: String = "",
    val marks: List<TextMark> = emptyList(),
    val typingStyles: Set<TextStyle>? = null,

    val imagePath: String? = null,

    val loading: Boolean = true,
    val saving: Boolean = false,
    val importing: Boolean = false,
    val saved: Boolean = false,
    val missing: Boolean = false,

    val error: UiText? = null,
    val titleError: Boolean = false,
    val voice: VoiceState = VoiceState.Idle,
    val loadError: UiText? = null,
)
