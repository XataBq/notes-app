package ru.xatab.avitonotes.domain.model

data class Task(val id: Long = 0, val title: String, val completed: Boolean = false, val createdAt: Long)
