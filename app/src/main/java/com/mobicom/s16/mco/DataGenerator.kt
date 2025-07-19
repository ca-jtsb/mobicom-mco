package com.mobicom.s16.mco

import com.mobicom.s16.mco.model.PokemonModel

class DataGenerator {
    companion object {
        fun loadData(): ArrayList<PokemonModel> {
            val data = ArrayList<PokemonModel>()
            data.add(
                PokemonModel(
                    "Clefairy",
                    R.drawable.card1
                )
            )
            data.add(
                PokemonModel(
                    "Sylveon",
                    R.drawable.card2
                )
            )
            data.add(
                PokemonModel(
                    "Gardevoir",
                    R.drawable.card3
                )
            )
            data.add(
                PokemonModel(
                    "Magikarp",
                    R.drawable.card4
                )
            )

            return data
        }
    }
}