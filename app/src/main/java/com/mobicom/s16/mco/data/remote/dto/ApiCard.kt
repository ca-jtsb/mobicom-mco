package com.mobicom.s16.mco.data.remote.dto


data class ApiCard(
    val id: String,
    val name: String,
    val supertype: String,
    val subtypes: List<String>? = null,
    val hp: String? = null,
    val types: List<String>? = null,
    val evolvesFrom: String? = null,
    val evolvesTo: List<String>? = null,
    val attacks: List<AttackDto>? = null,
    val abilities: List<AbilityDto>? = null,
    val weaknesses: List<WeaknessDto>? = null,
    val resistances: List<ResistanceDto>? = null,
    val retreatCost: List<String>? = null,
    val convertedRetreatCost: Int? = null,
    val set: SetInfo,
    val number: String,
    val artist: String? = null,
    val rarity: String? = null,
    val flavorText: String? = null,
    val nationalPokedexNumbers: List<Int>? = null,
    val legalities: Legalities? = null,
    val images: Images,
    val tcgplayer: TcgPlayer? = null,
//    val cardmarket: CardMarket? = null
)