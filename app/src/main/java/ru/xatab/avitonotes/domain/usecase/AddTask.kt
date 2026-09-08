package ru.xatab.avitonotes.domain.usecase

import ru.xatab.avitonotes.domain.repository.TaskRepository

class AddTask(private val repository: TaskRepository) {
    suspend operator fun invoke(title: String) = repository.add(requireTitle(title))
}
