package com.mobicom.s16.mco.data.remote.database

import com.mobicom.s16.mco.data.mapper.toDomainModel
import com.mobicom.s16.mco.data.remote.api.RetrofitClient
import com.mobicom.s16.mco.domain.model.Card

object PokemonRepository {
    suspend fun searchCardsByName(name: String): List<Card> {
        return try {
            val query = "name:\"$name\""
            println("🔍 [DEBUG] Performing card search with query: $query")

            val call = RetrofitClient.api.searchCardByNameAndNumber(query)
            val response = call.execute()

            println("✅ [DEBUG] HTTP Response Code: ${response.code()}")

            if (response.isSuccessful) {
                val body = response.body()
                println("📦 [DEBUG] Raw response body: ${body?.data?.size} cards found.")

                val domainCards = body?.data?.toDomainModel()
                println("🎴 [DEBUG] Converted to domain model: ${domainCards?.size ?: 0} cards")

                domainCards ?: emptyList()
            } else {
                println("❌ [DEBUG] Search failed - Code: ${response.code()}")
                println("❌ [DEBUG] Error body: ${response.errorBody()?.string()}")
                emptyList()
            }
        } catch (e: Exception) {
            println("💥 [DEBUG] Exception during card search: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }
}
