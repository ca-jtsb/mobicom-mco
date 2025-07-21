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

        adapter = PokemonAdapter(emptyList())
        binding.recyclerView.adapter = adapter
        val user = FirebaseAuth.getInstance().currentUser
        Log.d("WishlistTabFragment", "Current user: ${user?.uid ?: "null"}")
        // 🔁 Fetch Firestore cards AFTER adapter is set
        FirestoreRepository.getUserWishlistCards(
            onResult = { cards ->
                wishlistCards = cards
                adapter.updateData(cards)
            },
            onError = { e ->
                e.printStackTrace()
                // Optional: show error UI
            }
        )
    }

    override fun applyFilters(set: String?, type: String?, rarity: String?) {
        val filtered = wishlistCards.filter { card ->
            (set == null || card.set.equals(set, ignoreCase = true)) &&
                    (type == null || card.supertype.equals(type, ignoreCase = true)) &&
                    (rarity == null || card.rarity.equals(rarity, ignoreCase = true))
        }
        adapter.updateData(filtered)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
