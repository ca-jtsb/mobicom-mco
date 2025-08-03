package com.mobicom.s16.mco.data.remote.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mobicom.s16.mco.domain.model.Attack
import com.mobicom.s16.mco.domain.model.Card
import com.mobicom.s16.mco.domain.model.Resistance
import com.mobicom.s16.mco.domain.model.Weakness

object FirestoreRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun addDummyCardToWishlist() {
        val user = auth.currentUser
        if (user == null) {
            Log.e("FirestoreRepo", "User not logged in")
            return
        }

        val dummyCards = listOf(
            Card(
                id = "hgss4-1",
                name = "Aggron",
                supertype = "Pokémon",
                subtypes = listOf("Stage 2"),
                hp = "140",
                types = listOf("Metal"),
                evolvesFrom = "Lairon",
                evolvesTo = null,
                attacks = listOf(
                    Attack("Second Strike", listOf("Metal", "Metal", "Colorless"), 3, "40", "If the Defending Pokémon already has any damage counters on it, this attack does 40 damage plus 40 more damage."),
                    Attack("Guard Claw", listOf("Metal", "Metal", "Colorless", "Colorless"), 4, "60", "During your opponent's next turn, any damage done to Aggron by attacks is reduced by 20 (after applying Weakness and Resistance).")
                ),
                abilities = null,
                weaknesses = listOf(Weakness("Fire", "×2")),
                resistances = listOf(Resistance("Psychic", "-20")),
                retreatCost = listOf("Colorless", "Colorless", "Colorless", "Colorless"),
                convertedRetreatCost = 4,
                set = "HS—Triumphant",
                setId = "hgss4",
                setSeries = "HeartGold & SoulSilver",
                number = "1",
                artist = "Kagemaru Himeno",
                rarity = "Rare Holo",
                flavorText = "You can tell its age by the length of its iron horns. It claims an entire mountain as its territory.",
                imageUrl = "https://images.pokemontcg.io/hgss4/1.png",
                imageUrlLarge = "https://images.pokemontcg.io/hgss4/1_hires.png",
                price = 4.20,
                priceSource = "tcgplayer"
            ),
            Card(
                id = "xy5-1",
                name = "Weedle",
                supertype = "Pokémon",
                subtypes = listOf("Basic"),
                hp = "50",
                types = listOf("Grass"),
                evolvesFrom = null,
                evolvesTo = listOf("Kakuna"),
                attacks = listOf(
                    Attack("Multiply", listOf("Grass"), 1, "", "Search your deck for Weedle and put it onto your Bench. Shuffle your deck afterward.")
                ),
                abilities = null,
                weaknesses = listOf(Weakness("Fire", "×2")),
                resistances = null,
                retreatCost = listOf("Colorless"),
                convertedRetreatCost = 1,
                set = "Primal Clash",
                setId = "xy5",
                setSeries = "XY",
                number = "1",
                artist = "Midori Harada",
                rarity = "Common",
                flavorText = "Its poison stinger is very powerful. Its bright-colored body is intended to warn off its enemies.",
                imageUrl = "https://images.pokemontcg.io/xy5/1.png",
                imageUrlLarge = "https://images.pokemontcg.io/xy5/1_hires.png",
                price = 0.19,
                priceSource = "tcgplayer"
            )
            // Add more cards as needed…
        )

        for (card in dummyCards) {
            firestore.collection("users")
                .document(user.uid)
                .collection("wishlist")
                .document(card.id)
                .set(card)
                .addOnSuccessListener {
                    Log.d("FirestoreRepo", "Added dummy card: ${card.name}")
                }
                .addOnFailureListener { e ->
                    Log.e("FirestoreRepo", "Failed to add card: ${card.name}", e)
                }
        }

//        for (card in dummyCards) {
//            val cardMap = hashMapOf(
//                "id" to card.id,
//                "name" to card.name,
//                "supertype" to card.supertype,
//                "subtypes" to card.subtypes,
//                "hp" to card.hp,
//                "types" to card.types,
//                "evolvesFrom" to card.evolvesFrom,
//                "evolvesTo" to card.evolvesTo,
//                "attacks" to card.attacks?.map { mapOf("name" to it.name, "cost" to it.cost, "convertedEnergyCost" to it.convertedEnergyCost, "damage" to it.damage, "text" to it.text) },
//                "abilities" to card.abilities?.map { mapOf("name" to it.name, "text" to it.text, "type" to it.type) },
//                "weaknesses" to card.weaknesses?.map { mapOf("type" to it.type, "value" to it.value) },
//                "resistances" to card.resistances?.map { mapOf("type" to it.type, "value" to it.value) },
//                "retreatCost" to card.retreatCost,
//                "convertedRetreatCost" to card.convertedRetreatCost,
//                "set" to card.set,
//                "setId" to card.setId,
//                "setSeries" to card.setSeries,
//                "number" to card.number,
//                "artist" to card.artist,
//                "rarity" to card.rarity,
//                "flavorText" to card.flavorText,
//                "imageUrl" to card.imageUrl,
//                "imageUrlLarge" to card.imageUrlLarge,
//                "price" to card.price,
//                "priceSource" to card.priceSource
//            )
//
//            firestore.collection("users")
//                .document(user.uid)
//                .collection("wishlist")
//                .document(card.id)
//                .set(cardMap)
//                .addOnSuccessListener {
//                    Log.d("FirestoreRepo", "Added dummy card: ${card.name}")
//                }
//                .addOnFailureListener { e ->
//                    Log.e("FirestoreRepo", "Failed to add card: ${card.name}", e)
//                }
//        }

    }

    fun getUserWishlistCards(onResult: (List<Card>) -> Unit, onError: (Exception) -> Unit) {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("users")
            .document(userId)
            .collection("wishlist")
            .get()
            .addOnSuccessListener { result ->
                val cards = result.mapNotNull { doc ->
                    try {
                        doc.toObject(Card::class.java)
                    } catch (e: Exception) {
                        Log.e("FirestoreRepo", "Error parsing card document", e)
                        null
                    }
                }
                onResult(cards)
            }
            .addOnFailureListener { e ->
                onError(e)
            }
    }

    fun getUserArchivedCards(onResult: (List<Card>) -> Unit, onError: (Exception) -> Unit) {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("users")
            .document(userId)
            .collection("archive")
            .get()
            .addOnSuccessListener { result ->
                val cards = result.mapNotNull { doc ->
                    try {
                        doc.toObject(Card::class.java)
                    } catch (e: Exception) {
                        Log.e("FirestoreRepo", "Error parsing archived card document", e)
                        null
                    }
                }
                onResult(cards)
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreRepo", "Failed to fetch archived cards", e)
                onError(e)
            }
    }



    fun archiveCard(card: Card, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            onFailure(Exception("User not logged in"))
            return
        }

        val userDoc = firestore.collection("users").document(user.uid)
        val cardId = card.id.ifBlank {
            card.name.lowercase()
                .replace(" ", "-")
                .replace("(", "")
                .replace(")", "") + "-card-" + card.number + "-" +
                    card.set.lowercase()
                        .replace(" ", "-")
                        .replace("(", "")
                        .replace(")", "")
        }

        val wishlistRef = userDoc.collection("wishlist").document(cardId)
        val archiveRef = userDoc.collection("archive").document(cardId)

        archiveRef.set(card)
            .addOnSuccessListener {
                wishlistRef.delete()
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onFailure(it) }
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun wishlistCard(card: Card, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            onFailure(Exception("User not logged in"))
            return
        }

        val userDoc = firestore.collection("users").document(user.uid)
        val cardId = card.id.ifBlank {
            card.name.lowercase()
                .replace(" ", "-")
                .replace("(", "")
                .replace(")", "") + "-card-" + card.number + "-" +
                card.set.lowercase()
                    .replace(" ", "-")
                    .replace("(", "")
                    .replace(")", "")
        }

        val wishlistRef = userDoc.collection("wishlist").document(cardId)
        val archiveRef = userDoc.collection("archive").document(cardId)

        wishlistRef.set(card)
            .addOnSuccessListener {
                archiveRef.delete()
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onFailure(it) }
            }
            .addOnFailureListener { onFailure(it) }
    }

    fun removeCardFromWishlist(card: Card, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            onFailure(Exception("User not logged in"))
            return
        }

        val cardId = card.id.ifBlank {
            card.name.lowercase()
                .replace(" ", "-")
                .replace("(", "")
                .replace(")", "") + "-card-" + card.number + "-" +
                    card.set.lowercase()
                        .replace(" ", "-")
                        .replace("(", "")
                        .replace(")", "")
        }

        Log.d("FirestoreRepo", "removed dummy card: ${cardId}")

        val wishlistRef = firestore.collection("users")
            .document(user.uid)
            .collection("wishlist")
            .document(cardId)

        wishlistRef.delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener {
                Log.e("FirestoreRepo", "Failed to delete card", it)
                onFailure(it)
            }
    }

    fun removeCardFromArchive(card: Card, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            onFailure(Exception("User not logged in"))
            return
        }

        val userDoc = firestore.collection("users").document(user.uid)
        val cardId = card.id.ifBlank {
            card.name.lowercase()
                .replace(" ", "-")
                .replace("(", "")
                .replace(")", "") + "-card-" + card.number + "-" +
                    card.set.lowercase()
                        .replace(" ", "-")
                        .replace("(", "")
                        .replace(")", "")
        }

        val archiveRef = userDoc.collection("archive").document(cardId)

        archiveRef.delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailure(it) }
    }

}
