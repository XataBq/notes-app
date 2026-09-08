package ru.xatab.avitonotes.domain.repository

interface ImageStorage {
    suspend fun import(source: String): String
    suspend fun delete(path: String?)
}
