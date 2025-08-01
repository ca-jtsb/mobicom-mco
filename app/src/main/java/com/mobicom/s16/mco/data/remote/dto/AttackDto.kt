package com.mobicom.s16.mco.data.remote.dto

data class AttackDto(
    val name: String,
    val cost: List<String>? = null,
    val convertedEnergyCost: Int? = null,
    val damage: String? = null,
    val text: String? = null
)