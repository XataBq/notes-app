package ru.xatab.avitonotes.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import ru.xatab.avitonotes.BuildConfig
import ru.xatab.avitonotes.R
import ru.xatab.avitonotes.data.remote.api.GigaApi
import ru.xatab.avitonotes.data.remote.api.OAuthApi
import ru.xatab.avitonotes.data.remote.api.SpeechApi
import ru.xatab.avitonotes.data.remote.auth.TokenProvider
import ru.xatab.avitonotes.data.remote.client.ApiClient
import ru.xatab.avitonotes.data.remote.client.ApiEndpoints
import ru.xatab.avitonotes.data.remote.repository.RemoteGigaChatRepository
import ru.xatab.avitonotes.data.remote.repository.RemoteSpeechRepository
import ru.xatab.avitonotes.domain.repository.GigaChatRepository
import ru.xatab.avitonotes.domain.repository.SpeechRepository

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun apiClient(): ApiClient = ApiClient(ApiClient.httpClient(), ApiClient.serializer())

    @Provides
    @Singleton
    fun oauth(client: ApiClient): OAuthApi = client.create(ApiEndpoints.OAUTH, OAuthApi::class.java)

    @Provides
    @Singleton
    fun gigaApi(client: ApiClient): GigaApi = client.create(ApiEndpoints.GIGA_CHAT, GigaApi::class.java)

    @Provides
    @Singleton
    fun speechApi(client: ApiClient): SpeechApi = client.create(ApiEndpoints.SALUTE_SPEECH, SpeechApi::class.java)

    @Provides
    @Singleton
    fun giga(api: GigaApi, oauth: OAuthApi, @ApplicationContext context: Context): GigaChatRepository =
        RemoteGigaChatRepository(
            api,
            TokenProvider(oauth, BuildConfig.GIGACHAT_AUTH_KEY, BuildConfig.GIGACHAT_SCOPE),
            context.getString(R.string.giga_task_prompt),
        )

    @Provides
    @Singleton
    fun speech(api: SpeechApi, oauth: OAuthApi): SpeechRepository = RemoteSpeechRepository(
        api,
        TokenProvider(oauth, BuildConfig.SALUTE_AUTH_KEY, BuildConfig.SALUTE_SCOPE),
    )
}
