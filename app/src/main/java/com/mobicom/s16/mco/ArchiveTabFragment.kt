package com.mobicom.s16.mco

import PokemonAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.mobicom.s16.mco.databinding.FragmentTabRecyclerBinding
import com.mobicom.s16.mco.domain.model.Card

class ArchiveTabFragment : Fragment(), FilterableTab {

    private var _binding: FragmentTabRecyclerBinding? = null
    private val binding get() = _binding!!

    private var allCards: List<Card> = emptyList()
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

        allCards = emptyList()
        adapter = PokemonAdapter(allCards)
        binding.recyclerView.adapter = adapter

        Log.d("ArchiveTabFragment", "Cache loading is disabled.")
    }

    override fun applyFilters(set: String?, type: String?, rarity: String?) {
        if (!::adapter.isInitialized) {
            Log.w("ArchiveTabFragment", "applyFilters called before adapter was initialized.")
            return
        }

        val filtered = allCards.filter { card ->
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


//class ArchiveTabFragment : Fragment(), FilterableTab {
//
//    private var _binding: FragmentTabRecyclerBinding? = null
//    private val binding get() = _binding!!
//
//    private lateinit var allCards: List<Card>
//    private lateinit var adapter: PokemonAdapter
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        _binding = FragmentTabRecyclerBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
//
//        // Use coroutine to load cache asynchronously
//        viewLifecycleOwner.lifecycleScope.launch {
//            withContext(Dispatchers.IO) {
//                allCards = CardCacheManager.loadCardsFromCache(requireContext())
//            }
//            adapter = PokemonAdapter(allCards)
//            binding.recyclerView.adapter = adapter
//        }
//    }
//
//    override fun applyFilters(set: String?, type: String?, rarity: String?) {
//        if (!::allCards.isInitialized) {
//            Log.w("ArchiveTabFragment", "applyFilters called before allCards was initialized.")
//            return
//        }
//
//        val filtered = allCards.filter { card ->
//            (set == null || card.set.equals(set, ignoreCase = true)) &&
//                    (type == null || card.supertype.equals(type, ignoreCase = true)) &&
//                    (rarity == null || card.rarity.equals(rarity, ignoreCase = true))
//        }
//        adapter.updateData(filtered)
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//}