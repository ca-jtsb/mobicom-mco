package com.mobicom.s16.mco

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mobicom.s16.mco.databinding.FilterPopupBinding
import com.mobicom.s16.mco.domain.model.Card

class CardFilterDialog(
    private val cards: List<Card>,
    private val defaultSet: String? = null,
    private val defaultType: String? = null,
    private val defaultRarity: String? = null,
    private val onFilterSelected: (String?, String?, String?) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: FilterPopupBinding? = null
    private val binding get() = _binding!!

    private var selectedSet: String? = null
    private var selectedType: String? = null
    private var selectedRarity: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FilterPopupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        val cards = CardCacheManager.loadCardsFromCache(requireContext())
        val sets = cards.mapNotNull { it.set }.distinct().sorted()
        val types = cards.mapNotNull { it.supertype }.distinct().sorted()
        val rarities = cards.mapNotNull { it.rarity }.distinct().sorted()

        setupSpinner(binding.spinnerSet, sets, defaultSet) { selectedSet = it }
        setupSpinner(binding.spinnerCardType, types, defaultType) { selectedType = it }
        setupSpinner(binding.spinnerRarity, rarities, defaultRarity) { selectedRarity = it }

        binding.btnApply.setOnClickListener {
            onFilterSelected(selectedSet, selectedType, selectedRarity)
            dismiss()
        }
    }

     private fun setupSpinner(spinner: Spinner, options: List<String>, defaultValue: String?, onSelected: (String?) -> Unit) {
         val items = listOf("All") + options
         val adapter = ArrayAdapter(requireContext(), R.layout.spinner_dropdown_item, items)
         spinner.adapter = adapter

         val defaultPosition = defaultValue?.let { items.indexOf(it) }?.takeIf { it >= 0 } ?: 0
         spinner.setSelection(defaultPosition)

         spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
             override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                 onSelected(if (position == 0) null else items[position])
             }
             override fun onNothingSelected(parent: AdapterView<*>) {}
         }
     }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        onFilterSelected(selectedSet, selectedType, selectedRarity)
    }
}