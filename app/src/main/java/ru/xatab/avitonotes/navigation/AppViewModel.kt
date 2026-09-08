package ru.xatab.avitonotes.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import ru.xatab.avitonotes.domain.model.AppSettings
import ru.xatab.avitonotes.domain.repository.SettingsRepository

@HiltViewModel
class AppViewModel @Inject constructor(repository: SettingsRepository) : ViewModel() {
    private val _ready = MutableStateFlow(false)
    val ready = _ready.asStateFlow()
    val settings = repository.settings
        .catch { emit(AppSettings()) }
        .onEach { _ready.value = true }
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())
}
