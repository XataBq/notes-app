package ru.xatab.avitonotes.data.remote.api

import okhttp3.RequestBody
import retrofit2.http.*
import ru.xatab.avitonotes.data.remote.dto.SpeechResponse

interface SpeechApi {
    @POST("./speech:recognize")
    suspend fun recognize(@Header("Authorization") token: String, @Body audio: RequestBody): SpeechResponse
}
