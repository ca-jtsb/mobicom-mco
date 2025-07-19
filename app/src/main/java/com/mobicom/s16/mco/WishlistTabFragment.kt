package com.mobicom.s16.mco

import PokemonAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.mobicom.s16.mco.databinding.FragmentTabRecyclerBinding
import com.mobicom.s16.mco.domain.model.Card

class WishlistTabFragment : Fragment(), FilterableTab {

    private var _binding: FragmentTabRecyclerBinding? = null
    private val binding get() = _binding!!

    private var wishlistCards: List<Card> = emptyList() // placeholder empty list for now
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

        // wishlistCards = CardCacheManager.loadWishlistFromCache(requireContext())
        wishlistCards = emptyList() // placeholder empty list for now

        adapter = PokemonAdapter(wishlistCards)
        binding.recyclerView.adapter = adapter
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
