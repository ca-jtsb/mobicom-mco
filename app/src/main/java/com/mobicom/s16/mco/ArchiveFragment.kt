// ArchiveFragment.kt
package com.mobicom.s16.mco

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.mobicom.s16.mco.databinding.FragmentArchiveBinding

class ArchiveFragment : Fragment() {

    private var _binding: FragmentArchiveBinding? = null
    private val binding get() = _binding!!

    private lateinit var pagerAdapter: CardTabPagerAdapter
    private var currentSet: String? = null
    private var currentType: String? = null
    private var currentRarity: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArchiveBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pagerAdapter = CardTabPagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = if (position == 0) "Archive" else "Wishlist"
        }.attach()

        binding.ivFilter.setOnClickListener {
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


        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val currentFragment = getCurrentTabFragment()
                if (currentFragment is FilterableTab) {
                    currentFragment.applyFilters(currentSet, currentType, currentRarity)
                }
            }
        })

        // TODO: Search function here
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun getCurrentTabFragment(): Fragment? {
        return childFragmentManager.fragments
            .firstOrNull { it.isVisible }
    }
}
