package com.mobicom.s16.mco

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.mobicom.s16.mco.data.remote.api.RetrofitClient
import com.mobicom.s16.mco.data.remote.dto.SingleCardResponse
import com.mobicom.s16.mco.databinding.ActivityMainBinding
import com.mobicom.s16.mco.domain.model.Card
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, ArchiveFragment())
                .commit()
            setFabSelected(false)
            binding.bottomNavigationView.selectedItemId = R.id.archive
        }

        binding.fab.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, ScannerFragment())
                .commit()
            setFabSelected(true)
            binding.bottomNavigationView.menu.findItem(R.id.scanner).isChecked = true
        }

        binding.bottomNavigationView.setOnItemSelectedListener { menuItem ->
            setFabSelected(false)
            when (menuItem.itemId) {
                R.id.archive -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, ArchiveFragment())
                        .commit()
                    true
                }
                R.id.profile -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, ProfileFragment())
                        .commit()
                    true
                }
                else -> false
            }
        }


        RetrofitClient.api.getCardById("hgss4-1").enqueue(object : Callback<SingleCardResponse> {
            override fun onResponse(call: Call<SingleCardResponse>, response: Response<SingleCardResponse>) {
                if (response.isSuccessful) {
                    val apiCard = response.body()?.data

                    if (apiCard != null) {
                        Log.d("PokemonData", "Fetched card by ID: ${apiCard.name}")

                        val card = Card(
                            name = apiCard.name,
                            set = apiCard.set.name ?: "Unknown Set",
                            hp = apiCard.hp ?: "N/A",
                            supertype = apiCard.supertype ?: "N/A",
                            firstAttack = apiCard.attacks?.firstOrNull()?.name ?: "None",
                            price = apiCard.tcgplayer?.prices?.holofoil?.market?.toString() ?: "N/A",
                            imageUrl = apiCard.images.large ?: ""
                        )

                        Log.d("PokemonData", "Mapped card: $card")
                    } else {
                        Log.e("PokemonData", "No card found in response")
                    }
                } else {
                    Log.e("PokemonData", "API error in getCardById: ${response.code()} - ${response.message()}")
                }
            }

            override fun onFailure(call: Call<SingleCardResponse>, t: Throwable) {
                Log.e("PokemonData", "Network error in getCardById", t)
            }
        })



    }

    private fun setFabSelected(isSelected: Boolean) {
        if (isSelected) {
//            binding.fab.backgroundTintList = getColorStateList(R.color.fab_selected_color)
            binding.fab.setImageResource(R.drawable.camera_svgrepo_com__4_)
        } else {
//            binding.fab.backgroundTintList = getColorStateList(R.color.fab_normal_color)
            binding.fab.setImageResource(R.drawable.camera)
        }
    }

}
