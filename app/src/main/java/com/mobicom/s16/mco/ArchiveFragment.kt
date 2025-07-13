package com.mobicom.s16.mco

import PokemonAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.mobicom.s16.mco.data.remote.api.RetrofitClient
import com.mobicom.s16.mco.data.remote.dto.CardsResponse
import com.mobicom.s16.mco.databinding.FragmentArchiveBinding
import com.mobicom.s16.mco.domain.model.Card
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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

        RetrofitClient.api.getCards().enqueue(object : Callback<CardsResponse> {
            override fun onResponse(call: Call<CardsResponse>, response: Response<CardsResponse>) {
                if (response.isSuccessful) {
                    val apiCards = response.body()?.data ?: emptyList()
                    val cards = apiCards.map { apiCard ->
                        Card(
                            name = apiCard.name,
                            set = apiCard.set.name ?: "Unknown",
                            hp = apiCard.hp ?: "N/A",
                            supertype = apiCard.supertype ?: "N/A",
                            firstAttack = apiCard.attacks?.firstOrNull()?.name ?: "None",
                            price = apiCard.tcgplayer?.prices?.holofoil?.market?.toString() ?: "N/A",
                            imageUrl = apiCard.images.large ?: ""
                        )
                    }
                    pokemonAdapter = PokemonAdapter(cards)
                    binding.archiveRecyclerView.adapter = pokemonAdapter
                } else {
                    Log.e("ArchiveFragment", "API call unsuccessful")
                }
            }

            override fun onFailure(call: Call<CardsResponse>, t: Throwable) {
                Log.e("ArchiveFragment", "Failed to fetch cards: ${t.message}")
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
