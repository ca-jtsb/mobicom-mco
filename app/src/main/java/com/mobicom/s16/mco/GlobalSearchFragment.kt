package com.mobicom.s16.mco

import PokemonAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.mobicom.s16.mco.data.remote.database.PokemonRepository
import com.mobicom.s16.mco.databinding.FragmentSearchBinding
import com.mobicom.s16.mco.domain.model.Card
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GlobalSearchFragment : Fragment() {

    private lateinit var binding: FragmentSearchBinding
    private lateinit var adapter: PokemonAdapter

    companion object {
        private const val ARG_QUERY = "query"

        fun newInstance(query: String): GlobalSearchFragment {
            val fragment = GlobalSearchFragment()
            val args = Bundle()
            args.putString(ARG_QUERY, query)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val query = arguments?.getString(ARG_QUERY) ?: ""

        adapter = PokemonAdapter(emptyList(), "GlobalSearch")
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.itemAnimator = DefaultItemAnimator()
        binding.recyclerView.adapter = adapter

        // Show loading
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE

        lifecycleScope.launch {
            val results: List<Card> = withContext(Dispatchers.IO) {
                try {
                    PokemonRepository.searchCardsByName(query)
                } catch (e: Exception) {
                    emptyList()
                }
            }

            // Hide loading
            binding.progressBar.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE

            adapter.updateData(results)
        }
    }
}
