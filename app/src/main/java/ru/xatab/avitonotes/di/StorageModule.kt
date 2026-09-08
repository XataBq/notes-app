package ru.xatab.avitonotes.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import ru.xatab.avitonotes.data.local.database.NotesDatabase
import ru.xatab.avitonotes.data.local.database.NotesMigrations

private val Context.appearanceStore by preferencesDataStore("appearance")

@Module
@InstallIn(SingletonComponent::class)
object StorageModule {
    @Provides
    @Singleton
    fun database(@ApplicationContext context: Context): NotesDatabase =
        Room.databaseBuilder(context, NotesDatabase::class.java, "avito-notes.db")
            .addMigrations(NotesMigrations.FROM_1_TO_2).build()

    @Provides
    @Singleton
    fun preferences(@ApplicationContext context: Context): DataStore<Preferences> = context.appearanceStore

    @Provides
    @Singleton
    @ApplicationScope
    fun applicationScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
}
