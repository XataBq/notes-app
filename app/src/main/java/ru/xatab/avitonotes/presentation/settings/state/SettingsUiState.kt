package ru.xatab.avitonotes.presentation.settings.state

import ru.xatab.avitonotes.domain.model.AppSettings
import ru.xatab.avitonotes.domain.model.TokenBalance
import ru.xatab.avitonotes.presentation.common.state.LoadState
import ru.xatab.avitonotes.presentation.common.text.UiText

data class SettingsUiState(
    val settings: AppSettings = AppSettings(),
    val balance: LoadState<List<TokenBalance>> = LoadState.Loading,
    val error: UiText? = null,
)
