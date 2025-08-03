package com.mobicom.s16.mco

import PokemonAdapter
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.mobicom.s16.mco.data.remote.database.PokemonRepository
import com.mobicom.s16.mco.databinding.FragmentArchiveBinding
import com.mobicom.s16.mco.domain.model.Card
import com.mobicom.s16.mco.util.retryIO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ArchiveFragment : Fragment() {

    private var _binding: FragmentArchiveBinding? = null
    private val binding get() = _binding!!
    private lateinit var globalAdapter: PokemonAdapter
    private lateinit var pagerAdapter: CardTabPagerAdapter

    private var currentSet: String? = null
    private var currentType: String? = null
    private var currentRarity: String? = null

    private var globalSet: String? = null
    private var globalType: String? = null
    private var globalRarity: String? = null

    private var globalSearchResults: List<Card> = emptyList()
    private var inGlobalSearchMode = false

    private val backCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            exitGlobalSearchMode()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireActivity().onBackPressedDispatcher.addCallback(this, backCallback)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArchiveBinding.inflate(inflater, container, false)
        globalAdapter = PokemonAdapter(emptyList(), "GlobalSearch")
        binding.globalResultsRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        binding.globalResultsRecyclerView.adapter = globalAdapter
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pagerAdapter = CardTabPagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = if (position == 0) "Archive" else "Wishlist"
        }.attach()

        binding.ivSearchIcon.setOnClickListener {
            binding.etSearchBar.isVisible = true
            binding.ivSearchIcon.isVisible = false
            binding.etSearchBar.requestFocus()
        }

        binding.etSearchBar.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                binding.etSearchBar.isVisible = false
                binding.ivSearchIcon.isVisible = true
                true
            } else false
        }

        binding.etSearchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString()

                if (inGlobalSearchMode) {
                    searchGlobalResults(query)
                } else {
                    (getCurrentTabFragment() as? FilterableTab)?.searchCards(query)
                }

                binding.tvGlobalSearchPrompt.isVisible = query.isNotBlank()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.tvGlobalSearchPrompt.setOnClickListener {
            val query = binding.etSearchBar.text.toString()
            if (query.isNotBlank()) {
                inGlobalSearchMode = true
                backCallback.isEnabled = true

                binding.progressBar.isVisible = true
                binding.viewPager.isVisible = false
                binding.tabLayout.isVisible = false
                binding.globalResultsRecyclerView.isVisible = false
                binding.tvGlobalSearchPrompt.isVisible = false

                lifecycleScope.launch {
                    val cards: List<Card> = withContext(Dispatchers.IO) {
                        try {
                            retryIO(times = 3, delayTime = 1500L) {
                                PokemonRepository.searchCardsByName(query)
                            }
                        } catch (e: Exception) {
                            emptyList()
                        }
                    }

                    binding.progressBar.isVisible = false
                    binding.globalResultsRecyclerView.isVisible = true
                    globalSearchResults = cards
                    applyGlobalFilters()
                }
            }
        }

        binding.ivFilter.setOnClickListener {
            if (inGlobalSearchMode) {
                val dialog = CardFilterDialog(
                    cards = globalSearchResults,
                    defaultSet = globalSet,
                    defaultType = globalType,
                    defaultRarity = globalRarity
                ) { set, type, rarity ->
                    globalSet = set
                    globalType = type
                    globalRarity = rarity
                    applyGlobalFilters()
                }

                dialog.show(parentFragmentManager, "GlobalFilterDialog")
            } else {
                val currentFragment = getCurrentTabFragment()
                if (currentFragment is FilterableTab) {
                    val cards = when (currentFragment) {
                        is WishlistTabFragment -> currentFragment.getWishlistCards()
                        is ArchiveTabFragment -> currentFragment.getArchivedCards()
                        else -> emptyList()
                    }

                    val dialog = CardFilterDialog(
                        cards = cards,
                        defaultSet = currentSet,
                        defaultType = currentType,
                        defaultRarity = currentRarity
                    ) { set, type, rarity ->
                        currentSet = set
                        currentType = type
                        currentRarity = rarity
                        currentFragment.applyFilters(set, type, rarity)
                    }

                    dialog.show(parentFragmentManager, "FilterDialog")
                }
            }
        }

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val currentFragment = getCurrentTabFragment()
                if (currentFragment is FilterableTab) {
                    currentFragment.applyFilters(currentSet, currentType, currentRarity)
                }
            }
        })
    }

    private fun applyGlobalFilters() {
        val filtered = globalSearchResults.filter { card ->
            (globalSet == null || card.set == globalSet) &&
                    (globalType == null || card.types?.contains(globalType!!) == true) &&
                    (globalRarity == null || card.rarity == globalRarity)
        }
        globalAdapter.updateData(filtered)
    }

    private fun searchGlobalResults(query: String) {
        lifecycleScope.launch {
            val results = withContext(Dispatchers.IO) {
                try {
                    retryIO(times = 3, delayTime = 1500L) {
                        PokemonRepository.searchCardsByName(query)
                    }
                } catch (e: Exception) {
                    emptyList()
                }
            }
            globalSearchResults = results
            applyGlobalFilters()
        }
    }

    private fun exitGlobalSearchMode() {
        inGlobalSearchMode = false
        backCallback.isEnabled = false

        binding.viewPager.isVisible = true
        binding.tabLayout.isVisible = true
        binding.globalResultsRecyclerView.isVisible = false
        binding.tvGlobalSearchPrompt.isVisible = false
        binding.etSearchBar.setText("")
    }

    private fun getCurrentTabFragment(): Fragment? {
        return childFragmentManager.fragments.firstOrNull { it.isVisible }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
