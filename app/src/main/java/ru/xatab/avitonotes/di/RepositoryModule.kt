package ru.xatab.avitonotes.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import ru.xatab.avitonotes.data.local.database.NotesDatabase
import ru.xatab.avitonotes.data.local.repository.LocalImageStorage
import ru.xatab.avitonotes.data.local.repository.PreferencesRepository
import ru.xatab.avitonotes.data.local.repository.RoomNoteRepository
import ru.xatab.avitonotes.data.local.repository.RoomTaskRepository
import ru.xatab.avitonotes.domain.repository.ImageStorage
import ru.xatab.avitonotes.domain.repository.NoteRepository
import ru.xatab.avitonotes.domain.repository.SettingsRepository
import ru.xatab.avitonotes.domain.repository.TaskRepository

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun images(@ApplicationContext context: Context): ImageStorage = LocalImageStorage(context)

    @Provides
    @Singleton
    fun notes(database: NotesDatabase, images: ImageStorage): NoteRepository = RoomNoteRepository(database, images)

    @Provides
    @Singleton
    fun tasks(database: NotesDatabase): TaskRepository = RoomTaskRepository(database.tasks())

    @Provides
    @Singleton
    fun settings(store: DataStore<Preferences>): SettingsRepository = PreferencesRepository(store)
}
