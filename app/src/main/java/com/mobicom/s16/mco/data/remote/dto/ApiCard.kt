package com.mobicom.s16.mco.data.remote.dto


data class ApiCard(
    val id: String,
    val name: String,
    val supertype: String?,
    val number: Number?,
    val hp: String?,
    val attacks: List<Attack>?,
    val set: SetInfo,
    val images: Images,
    val tcgplayer: TcgPlayer?
)