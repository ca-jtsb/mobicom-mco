package com.mobicom.s16.mco.data.remote.firebase

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mobicom.s16.mco.domain.model.Card

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
                name = "Aggron",
                set = "HS—Triumphant",
                hp = "140",
                supertype = "Pokémon",
//                firstAttack = "Metal Claw",
//                price = "2.75",
                imageUrl = "https://images.pokemontcg.io/hgss4/1_hires.png",
                rarity = "Rare Holo"
            ),
            Card(
                name = "Weedle",
                set = "Primal Clash",
                hp = "50",
                supertype = "Pokémon",
//                firstAttack = "String Shot",
//                price = "0.20",
                imageUrl = "https://images.pokemontcg.io/xy5/1_hires.png",
                rarity = "Common"
            ),
            Card(
                name = "Ampharos (Platinum)",
                set = "Platinum",
                hp = "130",
                supertype = "Pokémon",
//                firstAttack = "Gigavolt",
//                price = "1.95",
                imageUrl = "https://images.pokemontcg.io/pl1/1_hires.png",
                rarity = "Rare Holo"
            ),
            Card(
                name = "Ampharos (Secret Wonders)",
                set = "Secret Wonders",
                hp = "130",
                supertype = "Pokémon",
//                firstAttack = "Jamming",
//                price = "2.50",
                imageUrl = "https://images.pokemontcg.io/dp3/1_hires.png",
                rarity = "Rare Holo"
            ),
            Card(
                name = "Altaria",
                set = "HS—Triumphant",
                hp = "90",
                supertype = "Pokémon",
//                firstAttack = "Sing",
//                price = "1.25",
                imageUrl = "https://images.pokemontcg.io/hgss4/2_hires.png",
                rarity = "Rare Holo"
            )
        )

        for (card in dummyCards) {
            val cardMap = hashMapOf(
                "name" to card.name,
                "set" to card.set,
                "hp" to card.hp,
                "supertype" to card.supertype,
//                "firstAttack" to card.firstAttack,
//                "price" to card.price,
                "imageUrl" to card.imageUrl,
                "rarity" to card.rarity
            )

            firestore.collection("users")
                .document(user.uid)
                .collection("wishlist")
                .document(card.name.lowercase().replace(" ", "-").replace("(", "").replace(")", "") + "-card")
                .set(cardMap)
                .addOnSuccessListener {
                    Log.d("FirestoreRepo", "Added dummy card: ${card.name}")
                }
                .addOnFailureListener { e ->
                    Log.e("FirestoreRepo", "Failed to add card: ${card.name}", e)
                }
        }
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
                        null
                    }
                }
                onResult(cards)
            }
            .addOnFailureListener { e ->
                onError(e)
            }
    }
}
