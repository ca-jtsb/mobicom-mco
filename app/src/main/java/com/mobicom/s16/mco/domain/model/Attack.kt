package com.mobicom.s16.mco.domain.model

data class Attack(
    val name: String,
    val cost: List<String>? = null,
    val convertedEnergyCost: Int? = null,
    val damage: String? = null,
    val text: String? = null
)