package ru.xatab.avitonotes.data.remote.api

import retrofit2.http.*
import ru.xatab.avitonotes.data.remote.dto.AccessToken

interface OAuthApi {
    @FormUrlEncoded
    @POST("api/v2/oauth")
    suspend fun token(
        @Header("Authorization") authorization: String,
        @Header("RqUID") requestId: String,
        @Field("scope") scope: String,
    ): AccessToken
}
