package ru.xatab.avitonotes.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable data class AccessToken(
    @SerialName(
        "access_token",
    ) val token: String,
    @SerialName("expires_at") val expiresAt: Long,
)
