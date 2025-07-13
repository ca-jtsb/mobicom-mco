package com.mobicom.s16.mco

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mobicom.s16.mco.databinding.CardRowBinding
import com.mobicom.s16.mco.domain.model.Card

class PokemonViewHolder(private val binding: CardRowBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bindData(card: Card) {
//        binding.txtFakeadexName.text = card.name
//        binding.txtFakeadexHp.text = "HP: ${card.hp}"
//        binding.txtFakeadexSet.text = "Set: ${card.set}"
//        binding.txtFakeadexType.text = "Type: ${card.supertype}"
//        binding.txtFakeadexAttack.text = "Attack: ${card.firstAttack}"
//        binding.txtFakeadexPrice.text = "Price: \$${card.price}"

        Glide.with(binding.root.context)
            .load(card.imageUrl)
            .into(binding.imgCard)
    }
}
