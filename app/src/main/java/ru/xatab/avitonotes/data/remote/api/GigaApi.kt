package ru.xatab.avitonotes.data.remote.api

import retrofit2.http.*
import ru.xatab.avitonotes.data.remote.dto.BalanceDto
import ru.xatab.avitonotes.data.remote.dto.ChatRequest
import ru.xatab.avitonotes.data.remote.dto.ChatResponse

interface GigaApi {
    @GET("balance")
    suspend fun balance(@Header("Authorization") token: String): BalanceDto

    @POST("chat/completions")
    suspend fun chat(@Header("Authorization") token: String, @Body request: ChatRequest): ChatResponse
}
