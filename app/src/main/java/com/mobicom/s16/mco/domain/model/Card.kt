package com.mobicom.s16.mco.domain.model

data class Card(
    val name: String,
    val set: String,
    val hp: String,
    val supertype: String,
    val firstAttack: String,
    val price: String,
    val imageUrl: String,
    val rarity: String?
)