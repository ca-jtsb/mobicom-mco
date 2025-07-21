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

        val dummyCard = Card(
            name = "Pikachu",
            set = "Base Set",
            hp = "60",
            supertype = "Pokémon",
            firstAttack = "Thunder Jolt",
            price = "1.50",
            imageUrl = "https://images.pokemontcg.io/basep/1_hires.png",
            rarity = "Common"
        )

        val cardMap = hashMapOf(
            "name" to dummyCard.name,
            "set" to dummyCard.set,
            "hp" to dummyCard.hp,
            "supertype" to dummyCard.supertype,
            "firstAttack" to dummyCard.firstAttack,
            "price" to dummyCard.price,
            "imageUrl" to dummyCard.imageUrl,
            "rarity" to dummyCard.rarity
        )

        firestore.collection("users")
            .document(user.uid)
            .collection("wishlist")
            .document("pikachu-card")
            .set(cardMap)
            .addOnSuccessListener {
                Log.d("FirestoreRepo", "Dummy card added to wishlist")
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreRepo", "Failed to add dummy card", e)
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
                        val card = doc.toObject(Card::class.java)
                        Log.d("FirestoreRepo", "Successfully parsed card: ${card.name}")
                        card
                    } catch (e: Exception) {
                        Log.e("FirestoreRepo", "Failed to parse card: ${doc.id}", e)
                        null
                    }
                }
                onResult(cards)
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreRepo", "Failed to fetch wishlist cards", e)
                onError(e)
            }
    }
}
