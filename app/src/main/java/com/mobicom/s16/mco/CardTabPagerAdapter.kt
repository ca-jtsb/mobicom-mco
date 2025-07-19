package com.mobicom.s16.mco

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class CardTabPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ArchiveTabFragment()
            1 -> WishlistTabFragment()
            else -> throw IllegalArgumentException("Invalid tab position")
        }
    }
}

// Optional interface for filterable tabs
interface FilterableTab {
    fun applyFilters(set: String?, type: String?, rarity: String?)
}