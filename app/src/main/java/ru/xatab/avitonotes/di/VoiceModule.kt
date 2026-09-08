package ru.xatab.avitonotes.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import ru.xatab.avitonotes.data.audio.PcmVoiceRecorder
import ru.xatab.avitonotes.domain.repository.VoiceRecorder

@Module
@InstallIn(ViewModelComponent::class)
object VoiceModule {
    @Provides
    @ViewModelScoped
    fun recorder(@ApplicationContext context: Context): VoiceRecorder = PcmVoiceRecorder(context)
}
