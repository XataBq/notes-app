package ru.xatab.avitonotes.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable data class BalanceDto(val balance: List<BalanceEntry>)
