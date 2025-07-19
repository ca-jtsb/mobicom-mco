// ArchiveFragment.kt
package com.mobicom.s16.mco

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import com.mobicom.s16.mco.databinding.FragmentArchiveBinding

class ArchiveFragment : Fragment() {

    private var _binding: FragmentArchiveBinding? = null
    private val binding get() = _binding!!

    private lateinit var pagerAdapter: CardTabPagerAdapter

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
            val dialog = CardFilterDialog { set, type, rarity ->
                (pagerAdapter.createFragment(binding.viewPager.currentItem) as? FilterableTab)?.applyFilters(set, type, rarity)
            }
            dialog.show(parentFragmentManager, "FilterDialog")
        }

        // Search behavior will be implemented later for both local and global
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
