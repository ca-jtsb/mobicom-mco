package com.mobicom.s16.mco

import PokemonAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.mobicom.s16.mco.data.remote.firebase.FirestoreRepository
import com.mobicom.s16.mco.databinding.FragmentTabRecyclerBinding
import com.mobicom.s16.mco.domain.model.Card

class WishlistTabFragment : Fragment(), FilterableTab {

    private var _binding: FragmentTabRecyclerBinding? = null
    private val binding get() = _binding!!

    private var wishlistCards: List<Card> = emptyList()
    private lateinit var adapter: PokemonAdapter

    private var currentSet: String? = null
    private var currentType: String? = null
    private var currentRarity: String? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTabRecyclerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)

        adapter = PokemonAdapter(emptyList(), "WISHLIST")
        binding.recyclerView.adapter = adapter

        val user = FirebaseAuth.getInstance().currentUser
        Log.d("WishlistTabFragment", "Current user: ${user?.uid ?: "null"}")

        FirestoreRepository.getUserWishlistCards(
            onResult = { cards ->
                wishlistCards = cards
                Log.d("WishlistTabFragment", "Loaded ${cards.size} cards from Firestore")

                // Apply filters now that cards are available
                refreshAdapter()
            },
            onError = { e -> e.printStackTrace() }
        )
    }

    override fun onResume() {
        super.onResume()
        FirestoreRepository.getUserWishlistCards(
            onResult = { cards ->
                wishlistCards = cards
                Log.d("ArchiveTabFragment", "Loaded ${cards.size} archived cards from Firestore")

                // Apply filters now that cards are available
                refreshAdapter()
            },
            onError = { e ->
                Log.e("ArchiveTabFragment", "Error loading archived cards", e)
            }
        )
    }

    private fun refreshAdapter() {
        val filtered = wishlistCards.filter { card ->
            (currentSet == null || card.set.equals(currentSet, ignoreCase = true)) &&
                    (currentType == null || card.supertype.equals(currentType, ignoreCase = true)) &&
                    (currentRarity == null || card.rarity?.equals(currentRarity, ignoreCase = true) == true)
        }
        adapter.updateData(filtered)
    }

    override fun applyFilters(set: String?, type: String?, rarity: String?) {
        Log.d("WishlistFilter", "Applying filters: set=$set, type=$type, rarity=$rarity")
        currentSet = set
        currentType = type
        currentRarity = rarity
        refreshAdapter()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun getWishlistCards(): List<Card> = wishlistCards

}
