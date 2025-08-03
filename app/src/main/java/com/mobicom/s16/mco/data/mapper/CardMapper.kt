// File: data/mapper/CardMapper.kt
package com.mobicom.s16.mco.data.mapper

import com.mobicom.s16.mco.data.remote.dto.AbilityDto
import com.mobicom.s16.mco.data.remote.dto.ApiCard
import com.mobicom.s16.mco.data.remote.dto.AttackDto
import com.mobicom.s16.mco.data.remote.dto.ResistanceDto
import com.mobicom.s16.mco.data.remote.dto.WeaknessDto
import com.mobicom.s16.mco.domain.model.Ability
import com.mobicom.s16.mco.domain.model.Attack
import com.mobicom.s16.mco.domain.model.Card
import com.mobicom.s16.mco.domain.model.Resistance
import com.mobicom.s16.mco.domain.model.Weakness

// Extension function to convert API DTO to domain model
fun ApiCard.toDomainModel(): Card {
    // Extract the best available price
    val (price, source) = extractMarketPrice()

    return Card(
        id = this.id,
        name = this.name,
        supertype = this.supertype,
        subtypes = this.subtypes,
        hp = this.hp,
        types = this.types,
        evolvesFrom = this.evolvesFrom,
        evolvesTo = this.evolvesTo,
        attacks = this.attacks?.map { it.toDomainModel() },
        abilities = this.abilities?.map { it.toDomainModel() },
        weaknesses = this.weaknesses?.map { it.toDomainModel() },
        resistances = this.resistances?.map { it.toDomainModel() },
        retreatCost = this.retreatCost,
        convertedRetreatCost = this.convertedRetreatCost,
        set = this.set.name,
        setId = this.set.id,
        setSeries = this.set.series,
        number = this.number,
        artist = this.artist,
        rarity = this.rarity,
        flavorText = this.flavorText,
        imageUrl = this.images.small,
        imageUrlLarge = this.images.large,
        price = price,
        priceSource = source
    )
}

fun AttackDto.toDomainModel(): Attack {
    return Attack(
        name = this.name,
        cost = this.cost,
        convertedEnergyCost = this.convertedEnergyCost,
        damage = this.damage,
        text = this.text
    )
}

fun AbilityDto.toDomainModel(): Ability {
    return Ability(
        name = this.name,
        text = this.text,
        type = this.type
    )
}

fun WeaknessDto.toDomainModel(): Weakness {
    return Weakness(
        type = this.type,
        value = this.value
    )
}

fun ResistanceDto.toDomainModel(): Resistance {
    return Resistance(
        type = this.type,
        value = this.value
    )
}

// Helper function to extract the best available price
private fun ApiCard.extractBestPrice(): Pair<Double, String> {
    // Try TCGPlayer first
    this.tcgplayer?.prices?.let { prices ->
        val price = prices.holofoil?.market
            ?: prices.reverseHolofoil?.market
            ?: prices.normal?.market
            ?: prices.holofoil?.mid
            ?: prices.reverseHolofoil?.mid
            ?: prices.normal?.mid

        if (price != null && price > 0) {
            return Pair(price, "TCGPlayer")
        }
    }

    // Fallback to CardMarket
//    this.cardmarket?.prices?.let { prices ->
//        val price = prices.averageSellPrice
//            ?: prices.trendPrice
//            ?: prices.lowPrice
//
//        if (price != null && price > 0) {
//            return Pair(price, "CardMarket")
//        }
//    }

    return Pair(0.0, "")
}


private fun ApiCard.extractMarketPrice(): Pair<Double, String> {
    this.tcgplayer?.prices?.let { prices ->
        val marketPrice = prices.holofoil?.market
            ?: prices.reverseHolofoil?.market
            ?: prices.normal?.market

        if (marketPrice != null && marketPrice > 0) {
            return Pair(marketPrice, "TCGPlayer")
        }
    }

    return Pair(0.0, "")
}


// Extension function to convert lists
fun List<ApiCard>.toDomainModel(): List<Card> {
    return this.map { it.toDomainModel() }
}