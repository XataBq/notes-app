package ru.xatab.avitonotes.data.local.repository

import kotlinx.coroutines.flow.map
import ru.xatab.avitonotes.data.local.dao.TaskDao
import ru.xatab.avitonotes.data.local.entity.TaskEntity
import ru.xatab.avitonotes.domain.model.AppException
import ru.xatab.avitonotes.domain.model.FailureKind
import ru.xatab.avitonotes.domain.repository.TaskRepository
import ru.xatab.avitonotes.domain.usecase.requireTitle

class RoomTaskRepository(private val dao: TaskDao, private val clock: () -> Long = System::currentTimeMillis) :
    TaskRepository {
    override fun observe() = dao.observe().map { rows -> rows.map { it.toDomain() } }
    override suspend fun add(title: String) = dao.insert(TaskEntity(title = requireTitle(title), createdAt = clock()))
    override suspend fun setCompleted(id: Long, completed: Boolean) {
        if (dao.setCompleted(id, completed) != 1) throw AppException(FailureKind.NOT_FOUND)
    }

    override suspend fun delete(id: Long) = dao.delete(id)
}
