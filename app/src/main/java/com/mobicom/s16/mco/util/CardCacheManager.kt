package com.mobicom.s16.mco.util

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mobicom.s16.mco.domain.model.Card
import java.io.File

object CardCacheManager {
    private const val CACHE_FILE_NAME = "cards_cache.json"
    private const val TOTAL_EXPECTED_CARDS = 19155

    fun isCacheAvailable(context: Context): Boolean {
        val file = File(context.filesDir, CACHE_FILE_NAME)
        val exists = file.exists()
        Log.d("CardCacheManager", "Checking cache file at: ${file.absolutePath}, exists: $exists")
        return exists
    }

    fun saveCardsToCache(context: Context, cards: List<Card>) {
        val json = Gson().toJson(cards)
        File(context.filesDir, CACHE_FILE_NAME).writeText(json)
        Log.d("CardCacheManager", "Saved ${cards.size} cards to cache")
    }

    fun loadCardsFromCache(context: Context): List<Card> {
        val file = File(context.filesDir, CACHE_FILE_NAME)
        if (!file.exists()) {
            Log.d("CardCacheManager", "Cache file does not exist, returning empty list")
            return emptyList()
        }
        val json = file.readText()
        val type = object : TypeToken<List<Card>>() {}.type
        val cards = Gson().fromJson<List<Card>>(json, type)
        Log.d("CardCacheManager", "Loaded ${cards.size} cards from cache")
        return cards
    }

    fun getCachedCardCount(context: Context): Int {
        val file = File(context.filesDir, CACHE_FILE_NAME)
        if (!file.exists()) return 0
        return loadCardsFromCache(context).size
    }

    fun isCacheComplete(context: Context): Boolean {
        return getCachedCardCount(context) >= TOTAL_EXPECTED_CARDS
    }
}
