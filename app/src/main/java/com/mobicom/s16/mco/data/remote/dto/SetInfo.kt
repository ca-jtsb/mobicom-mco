package com.mobicom.s16.mco.data.remote.dto

data class SetInfo(
    val id: String,
    val name: String,
    val series: String,
    val printedTotal: Int,
    val total: Int,
    val legalities: Legalities? = null,
    val ptcgoCode: String? = null,
    val releaseDate: String,
    val updatedAt: String,
    val images: SetImages
)