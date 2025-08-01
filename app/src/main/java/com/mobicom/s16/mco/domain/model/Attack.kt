package com.mobicom.s16.mco.domain.model

data class Attack(
    val name: String = "",
    val cost: List<String>? = emptyList(),
    val convertedEnergyCost: Int? = 0,
    val damage: String? = "",
    val text: String? = ""
)