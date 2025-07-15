package com.mobicom.s16.mco

import PokemonAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.mobicom.s16.mco.databinding.FragmentArchiveBinding
import com.mobicom.s16.mco.util.CardCacheManager

class ArchiveFragment : Fragment() {

    private var _binding: FragmentArchiveBinding? = null
    private val binding get() = _binding!!

    private lateinit var pokemonAdapter: PokemonAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArchiveBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.archiveRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)

        val cachedCards = CardCacheManager.loadCardsFromCache(requireContext())

        if (cachedCards.isNotEmpty()) {
            pokemonAdapter = PokemonAdapter(cachedCards)
            binding.archiveRecyclerView.adapter = pokemonAdapter
            Log.d("ArchiveFragment", "Loaded ${cachedCards.size} cards from cache")
        } else {
            Log.d("ArchiveFragment", "No cards found in cache")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
