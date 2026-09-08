package ru.xatab.avitonotes.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable data class BalanceEntry(val usage: String, val value: Long)
