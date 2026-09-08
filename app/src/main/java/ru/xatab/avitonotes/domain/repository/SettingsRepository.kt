package ru.xatab.avitonotes.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.xatab.avitonotes.domain.model.AccentColor
import ru.xatab.avitonotes.domain.model.AppSettings
import ru.xatab.avitonotes.domain.model.ThemeMode

interface SettingsRepository {
    suspend fun setNoteLayout(layout: ru.xatab.avitonotes.domain.model.NoteLayout)
    val settings: Flow<AppSettings>
    suspend fun setTheme(theme: ThemeMode)
    suspend fun setAccent(accent: AccentColor)
    suspend fun reset()
}
