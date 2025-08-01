package com.mobicom.s16.mco.data.remote.dto

data class TcgPlayer(
    val url: String,
    val updatedAt: String,
    val prices: TcgPlayerPrices
)