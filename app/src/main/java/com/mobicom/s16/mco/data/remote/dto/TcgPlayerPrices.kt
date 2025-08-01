package com.mobicom.s16.mco.data.remote.dto

data class TcgPlayerPrices(
    val holofoil: PriceInfo? = null,
    val reverseHolofoil: PriceInfo? = null,
    val normal: PriceInfo? = null,
    val `1stEditionHolofoil`: PriceInfo? = null,
    val `1stEditionNormal`: PriceInfo? = null
)