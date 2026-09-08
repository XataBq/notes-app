package ru.xatab.avitonotes.domain.usecase

import ru.xatab.avitonotes.domain.model.SortOrder
import ru.xatab.avitonotes.domain.model.Task
import ru.xatab.avitonotes.domain.model.TaskFilter

fun selectTasks(tasks: List<Task>, query: String, filter: TaskFilter, order: SortOrder): List<Task> = tasks
    .filter { it.title.contains(query.trim(), ignoreCase = true) }
    .filter { filter == TaskFilter.ALL || it.completed == (filter == TaskFilter.COMPLETED) }
    .sortedWith(
        compareBy<Task> { it.completed }.then(
            if (order == SortOrder.NEWEST) {
                compareByDescending<Task> { it.createdAt }.thenByDescending { it.id }
            } else {
                compareBy<Task> { it.createdAt }.thenBy { it.id }
            },
        ),
    )
