package ru.xatab.avitonotes.presentation.notes.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.xatab.avitonotes.domain.model.Note
import ru.xatab.avitonotes.domain.model.NoteLayout
import ru.xatab.avitonotes.domain.model.SortOrder
import ru.xatab.avitonotes.domain.repository.NoteRepository
import ru.xatab.avitonotes.domain.repository.SettingsRepository
import ru.xatab.avitonotes.presentation.common.text.userMessage
import ru.xatab.avitonotes.presentation.notes.state.NotesUiState

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class NotesViewModel @Inject constructor(
    private val repository: NoteRepository,
    private val settings: SettingsRepository,
    private val saved: SavedStateHandle,
) : ViewModel() {
    private val _state = MutableStateFlow(
        NotesUiState(
            query = saved["query"] ?: "",
            appliedQuery = saved["applied"] ?: "",
            order = SortOrder.valueOf(saved["sort"] ?: SortOrder.NEWEST.name),
        ),
    )
    val state = _state.asStateFlow()
    val notes = state.map { it.appliedQuery to it.order }.distinctUntilChanged()
        .flatMapLatest { (query, order) -> repository.pages(query, order) }
        .cachedIn(viewModelScope)

    init {
        settings.settings.onEach { value -> _state.update { it.copy(layout = value.noteLayout) } }
            .catch { error -> _state.update { it.copy(error = error.userMessage()) } }
            .launchIn(viewModelScope)
    }

    fun query(value: String) {
        saved["query"] = value
        _state.update { it.copy(query = value) }
        if (value.isEmpty()) search()
    }

    fun search() {
        val query = state.value.query.trim()
        saved["applied"] = query
        _state.update { it.copy(appliedQuery = query) }
    }

    fun sort(value: SortOrder) {
        saved["sort"] = value.name
        _state.update { it.copy(order = value) }
    }

    fun toggleLayout() {
        val layout = if (state.value.layout == NoteLayout.LIST) NoteLayout.GRID else NoteLayout.LIST
        _state.update { it.copy(layout = layout) }
        viewModelScope.launch {
            try {
                settings.setNoteLayout(layout)
            } catch (error: Exception) {
                _state.update { it.copy(error = error.userMessage()) }
            }
        }
    }

    fun toggleDeletion() {
        _state.update { it.copy(deleting = !it.deleting, pendingDeletion = null) }
    }

    fun requestDelete(note: Note) {
        if (state.value.deleting && note.id !in state.value.deletingIds) {
            _state.update { it.copy(pendingDeletion = note) }
        }
    }

    fun dismissDeletion() {
        _state.update { it.copy(pendingDeletion = null) }
    }

    fun confirmDeletion() {
        val note = state.value.pendingDeletion ?: return
        if (!state.value.deleting || note.id in state.value.deletingIds) return
        _state.update { it.copy(pendingDeletion = null, deletingIds = it.deletingIds + note.id) }
        viewModelScope.launch {
            try {
                repository.delete(note)
            } catch (error: Exception) {
                _state.update { it.copy(error = error.userMessage()) }
            } finally {
                _state.update { it.copy(deletingIds = it.deletingIds - note.id) }
            }
        }
    }

    fun dismissError() {
        _state.update { it.copy(error = null) }
    }
}
