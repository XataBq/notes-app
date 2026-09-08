package ru.xatab.avitonotes.data.local.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.xatab.avitonotes.domain.model.AccentColor
import ru.xatab.avitonotes.domain.model.AppSettings
import ru.xatab.avitonotes.domain.model.ThemeMode
import ru.xatab.avitonotes.domain.repository.SettingsRepository

class PreferencesRepository(private val store: DataStore<Preferences>) : SettingsRepository {
    private val layoutKey = stringPreferencesKey("note_layout")
    override suspend fun setNoteLayout(layout: ru.xatab.avitonotes.domain.model.NoteLayout) {
        store.edit { it[layoutKey] = layout.name }
    }
    private val themeKey = stringPreferencesKey("theme")
    private val accentKey = stringPreferencesKey("accent")
    override val settings: Flow<AppSettings> = store.data.map { preferences ->
        AppSettings(
            ThemeMode.entries.find { it.name == preferences[themeKey] } ?: ThemeMode.SYSTEM,
            AccentColor.entries.find { it.name == preferences[accentKey] } ?: AccentColor.BLUE,
            ru.xatab.avitonotes.domain.model.NoteLayout.entries.find { it.name == preferences[layoutKey] }
                ?: ru.xatab.avitonotes.domain.model.NoteLayout.LIST,
        )
    }

    override suspend fun setTheme(theme: ThemeMode) {
        store.edit { it[themeKey] = theme.name }
    }

    override suspend fun setAccent(accent: AccentColor) {
        store.edit { it[accentKey] = accent.name }
    }

    override suspend fun reset() {
        store.edit {
            it.remove(themeKey)
            it.remove(accentKey)
        }
    }
}
