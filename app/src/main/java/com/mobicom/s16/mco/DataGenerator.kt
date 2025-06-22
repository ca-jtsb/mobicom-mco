package com.mobicom.s16.mco

class DataGenerator {
    companion object {
        fun loadData(): ArrayList<PokemonModel> {
            val data = ArrayList<PokemonModel>()
            data.add(
                PokemonModel(
                    "Clefairy",
                    R.drawable.card1)
            )
            data.add(
                PokemonModel(
                    "Sylveon",
                    R.drawable.card2)
            )
            data.add(
                PokemonModel(
                    "Gardevoir",
                    R.drawable.card3)
            )
            data.add(
                PokemonModel(
                    "Magikarp",
                    R.drawable.card4)
            )

            return data
        }
    }
}