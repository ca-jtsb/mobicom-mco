package com.mobicom.s16.mco

import com.mobicom.s16.mco.domain.model.Card

interface FilterableTab {
    fun searchCards(query: String)
    fun applyFilters(set: String?, type: String?, rarity: String?)
    fun showGlobalResults(cards: List<Card>)
}