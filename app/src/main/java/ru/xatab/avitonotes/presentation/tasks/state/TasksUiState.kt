package ru.xatab.avitonotes.presentation.tasks.state

import ru.xatab.avitonotes.domain.model.SortOrder
import ru.xatab.avitonotes.domain.model.Task
import ru.xatab.avitonotes.domain.model.TaskFilter
import ru.xatab.avitonotes.presentation.common.text.UiText

data class TasksUiState(
    val tasks: List<Task> = emptyList(),
    val loading: Boolean = true,
    val query: String = "",
    val appliedQuery: String = "",
    val filter: TaskFilter = TaskFilter.ALL,
    val order: SortOrder = SortOrder.NEWEST,
    val input: TaskInput = TaskInput.Idle,
    val updating: Set<Long> = emptySet(),
    val pendingDeletion: Task? = null,
    val error: UiText? = null,
    val loadError: UiText? = null,
)
