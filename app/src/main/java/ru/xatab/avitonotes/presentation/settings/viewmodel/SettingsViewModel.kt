package ru.xatab.avitonotes.presentation.settings.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.xatab.avitonotes.domain.model.AccentColor
import ru.xatab.avitonotes.domain.model.ThemeMode
import ru.xatab.avitonotes.domain.repository.GigaChatRepository
import ru.xatab.avitonotes.domain.repository.SettingsRepository
import ru.xatab.avitonotes.presentation.common.state.LoadState
import ru.xatab.avitonotes.presentation.common.text.userMessage
import ru.xatab.avitonotes.presentation.settings.state.SettingsUiState

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository,
    private val giga: GigaChatRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(SettingsUiState())
    val state = _state.asStateFlow()
    private var refreshing = false
    init {
        viewModelScope.launch {
            repository.settings.catch { error -> _state.update { it.copy(error = error.userMessage()) } }
                .collect { settings -> _state.update { it.copy(settings = settings) } }
        }
        balance()
    }

    fun balance() {
        if (refreshing) return
        refreshing = true
        _state.update { it.copy(balance = LoadState.Loading) }
        viewModelScope.launch {
            try {
                val data = giga.balance()
                _state.update { it.copy(balance = LoadState.Ready(data)) }
            } catch (
                error: Exception,
            ) {
                _state.update { it.copy(balance = LoadState.Error(error.userMessage())) }
            } finally {
                refreshing = false
            }
        }
    }

    fun theme(value: ThemeMode) = update { repository.setTheme(value) }
    fun accent(value: AccentColor) = update { repository.setAccent(value) }
    fun reset() = update { repository.reset() }
    private fun update(block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
            } catch (
                error: Exception,
            ) {
                _state.update { it.copy(error = error.userMessage()) }
            }
        }
    }

    fun dismissError() {
        _state.update { it.copy(error = null) }
    }
}
