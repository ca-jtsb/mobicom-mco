package com.mobicom.s16.mco.domain.model

data class Card(
    val id: String = "",
    val name: String = "",
    val supertype: String = "",
    val subtypes: List<String>? = null,
    val hp: String? = null,
    val types: List<String>? = null,
    val evolvesFrom: String? = null,
    val evolvesTo: List<String>? = null,
    val attacks: List<Attack>? = null,
    val abilities: List<Ability>? = null,
    val weaknesses: List<Weakness>? = null,
    val resistances: List<Resistance>? = null,
    val retreatCost: List<String>? = null,
    val convertedRetreatCost: Int? = null,
    val set: String = "",
    val setId: String = "",
    val setSeries: String = "",
    val number: String = "",
    val artist: String? = null,
    val rarity: String? = null,
    val flavorText: String? = null,
    val imageUrl: String = "",
    val imageUrlLarge: String = "",
    val price: Double = 0.0,
    val priceSource: String = "tcgplayer" // or "cardmarket"
)