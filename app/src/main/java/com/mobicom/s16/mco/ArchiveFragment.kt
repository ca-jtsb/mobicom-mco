// ArchiveFragment.kt
package com.mobicom.s16.mco

import PokemonAdapter
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
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

        // Toggle search bar visibility
        binding.ivSearchIcon.setOnClickListener {
            binding.etSearchBar.isVisible = true
            binding.ivSearchIcon.isVisible = false
            binding.etSearchBar.requestFocus()
        }

        // Hide search bar on DONE press
        binding.etSearchBar.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                binding.etSearchBar.isVisible = false
                binding.ivSearchIcon.isVisible = true
                true
            } else false
        }

        // Text change = perform search
        binding.etSearchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString()
                val currentFragment = getCurrentTabFragment()
                if (currentFragment is FilterableTab) {
                    currentFragment.searchCards(query)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Filter button
        binding.ivFilter.setOnClickListener {
            val currentFragment = getCurrentTabFragment()

            if (currentFragment is WishlistTabFragment) {
                val wishlistCards = currentFragment.getWishlistCards()

                val dialog = CardFilterDialog(
                    cards = wishlistCards, // only from wishlist
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


        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val currentFragment = getCurrentTabFragment()
                if (currentFragment is FilterableTab) {
                    currentFragment.applyFilters(currentSet, currentType, currentRarity)
                }
            }
        })

        binding.etSearchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString()
                val currentFragment = getCurrentTabFragment()
                if (currentFragment is FilterableTab) {
                    currentFragment.searchCards(query)
                }

                // Show "search for global results" when typing
                binding.tvGlobalSearchPrompt.isVisible = query.isNotBlank()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.tvGlobalSearchPrompt.setOnClickListener {
            val query = binding.etSearchBar.text.toString()
            if (query.isNotBlank()) {
                binding.progressBar.isVisible = true
                binding.viewPager.isVisible = false
                binding.tabLayout.isVisible = false
                binding.globalResultsRecyclerView.isVisible = false // initially hidden

                lifecycleScope.launch {
                    val cards: List<Card> = withContext(Dispatchers.IO) {
                        try {
                            retryIO(times = 3, delayTime = 1500L) {
                                PokemonRepository.searchCardsByName(query)
                            }
                        } catch (e: Exception) {
                            println("❌ Final failure after retries: ${e.message}")
                            emptyList()
                        }
                    }

                    binding.progressBar.isVisible = false
                    binding.globalResultsRecyclerView.isVisible = true
                    globalAdapter.updateData(cards)
                }



            }
        }

    }

    private fun getCurrentTabFragment(): Fragment? {
        return childFragmentManager.fragments
            .firstOrNull { it.isVisible }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}
