package ru.xatab.avitonotes.data.local.entity

import androidx.room.*
import ru.xatab.avitonotes.domain.model.Task

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val completed: Boolean = false,
    val createdAt: Long,
) {
    fun toDomain() = Task(id, title, completed, createdAt)
}
