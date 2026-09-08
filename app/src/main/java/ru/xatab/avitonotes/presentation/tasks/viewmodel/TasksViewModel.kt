package ru.xatab.avitonotes.presentation.tasks.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.xatab.avitonotes.di.ApplicationScope
import ru.xatab.avitonotes.domain.model.SortOrder
import ru.xatab.avitonotes.domain.model.Task
import ru.xatab.avitonotes.domain.model.TaskFilter
import ru.xatab.avitonotes.domain.repository.GigaChatRepository
import ru.xatab.avitonotes.domain.repository.SpeechRepository
import ru.xatab.avitonotes.domain.repository.TaskRepository
import ru.xatab.avitonotes.domain.repository.VoiceRecorder
import ru.xatab.avitonotes.domain.usecase.AddTask
import ru.xatab.avitonotes.domain.usecase.CreateVoiceTask
import ru.xatab.avitonotes.domain.usecase.selectTasks
import ru.xatab.avitonotes.presentation.common.state.VoiceState
import ru.xatab.avitonotes.presentation.common.text.userMessage
import ru.xatab.avitonotes.presentation.common.voice.VoiceInput
import ru.xatab.avitonotes.presentation.tasks.state.TaskInput
import ru.xatab.avitonotes.presentation.tasks.state.TasksUiState

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val repository: TaskRepository,
    private val saved: SavedStateHandle,
    recorder: VoiceRecorder,
    speech: SpeechRepository,
    giga: GigaChatRepository,
    @ApplicationScope cleanupScope: CoroutineScope,
) : ViewModel() {
    private var allTasks = emptyList<Task>()
    private val _state = MutableStateFlow(
        TasksUiState(
            query = saved["query"] ?: "",
            appliedQuery = saved["applied"] ?: "",
            filter = TaskFilter.valueOf(saved["filter"] ?: TaskFilter.ALL.name),
            order = SortOrder.valueOf(saved["sort"] ?: SortOrder.NEWEST.name),
            input = saved.get<String>("draft")?.let { TaskInput.Text(it) } ?: TaskInput.Idle,
        ),
    )
    val state = _state.asStateFlow()
    val voice = VoiceInput(viewModelScope, cleanupScope, recorder, { value ->
        _state.update { it.copy(input = if (value == VoiceState.Idle) TaskInput.Idle else TaskInput.Voice(value)) }
    }) { audio ->
        CreateVoiceTask(speech, giga, repository)(audio)
        showAdded()
    }

    private var observation: Job? = null
    init {
        reload()
    }

    fun reload() {
        observation?.cancel()
        _state.update { it.copy(loading = true, loadError = null) }
        observation = viewModelScope.launch {
            repository.observe().catch { error ->
                _state.update { it.copy(loading = false, loadError = error.userMessage()) }
            }
                .collect {
                    allTasks = it
                    refresh()
                }
        }
    }

    private fun refresh() {
        _state.update { it.copy(tasks = selectTasks(allTasks, it.appliedQuery, it.filter, it.order), loading = false) }
    }

    fun query(value: String) {
        saved["query"] = value
        _state.update { it.copy(query = value) }
        if (value.isEmpty()) search()
    }

    fun search() {
        saved["applied"] = state.value.query.trim()
        _state.update { it.copy(appliedQuery = it.query.trim()) }
        refresh()
    }

    fun sort(value: SortOrder) {
        saved["sort"] = value.name
        _state.update { it.copy(order = value) }
        refresh()
    }

    fun filter(value: TaskFilter) {
        saved["filter"] = value.name
        _state.update { it.copy(filter = value) }
        refresh()
    }

    fun beginText() {
        if (state.value.input !=
            TaskInput.Idle
        ) {
            return
        }
        saved["draft"] = ""
        _state.update { it.copy(input = TaskInput.Text()) }
    }

    fun editText(value: String) {
        saved["draft"] = value
        _state.update { it.copy(input = TaskInput.Text(value)) }
    }

    fun cancelText() {
        if ((state.value.input as? TaskInput.Text)?.saving ==
            true
        ) {
            return
        }
        saved.remove<String>("draft")
        _state.update { it.copy(input = TaskInput.Idle) }
    }

    fun add() {
        val input = state.value.input as? TaskInput.Text ?: return
        if (input.saving) return
        if (input.title.isBlank()) {
            _state.update { it.copy(input = input.copy(error = true)) }
            return
        }
        _state.update { it.copy(input = input.copy(saving = true)) }
        viewModelScope.launch {
            try {
                AddTask(repository)(input.title)
                showAdded()
                saved.remove<String>("draft")
                _state.update { it.copy(input = TaskInput.Idle) }
            } catch (
                error: Exception,
            ) {
                _state.update { it.copy(input = input.copy(saving = false), error = error.userMessage()) }
            }
        }
    }

    private fun showAdded() {
        saved["query"] = ""
        saved["applied"] = ""
        saved["filter"] = TaskFilter.ALL.name
        _state.update { it.copy(query = "", appliedQuery = "", filter = TaskFilter.ALL) }
        refresh()
    }

    fun toggle(task: Task, completed: Boolean) {
        if (task.id in state.value.updating) return
        _state.update { it.copy(updating = it.updating + task.id) }
        viewModelScope.launch {
            try {
                repository.setCompleted(task.id, completed)
            } catch (
                error: Exception,
            ) {
                _state.update { it.copy(error = error.userMessage()) }
            } finally {
                _state.update { it.copy(updating = it.updating - task.id) }
            }
        }
    }

    fun error(message: ru.xatab.avitonotes.presentation.common.text.UiText) {
        _state.update { it.copy(error = message) }
    }

    fun requestDelete(task: Task) {
        if (task.id in state.value.updating) return
        _state.update { it.copy(pendingDeletion = task) }
    }

    fun dismissDeletion() {
        _state.update { it.copy(pendingDeletion = null) }
    }

    fun confirmDeletion() {
        val task = state.value.pendingDeletion ?: return
        if (task.id in state.value.updating) return
        _state.update { it.copy(pendingDeletion = null, updating = it.updating + task.id) }
        viewModelScope.launch {
            try {
                repository.delete(task.id)
            } catch (error: Exception) {
                _state.update { it.copy(error = error.userMessage()) }
            } finally {
                _state.update { it.copy(updating = it.updating - task.id) }
            }
        }
    }

    fun dismissError() {
        _state.update { it.copy(error = null) }
    }

    override fun onCleared() {
        voice.close()
    }
}
