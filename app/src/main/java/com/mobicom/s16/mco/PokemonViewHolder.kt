package com.mobicom.s16.mco

import androidx.recyclerview.widget.RecyclerView
import com.mobicom.s16.mco.databinding.CardRowBinding

class PokemonViewHolder(private val viewBinding: CardRowBinding): RecyclerView.ViewHolder(viewBinding.root) {

    fun bindData(model: PokemonModel) {
        /*this.viewBinding.txtFakeadexName.text = model.name
        this.viewBinding.txtFakeadexDesc.text = model.desc
        this.viewBinding.txtFakeadexLocationValue.text = model.location
        this.viewBinding.txtFakeadexSpeciesValue.text = model.specie*/
        this.viewBinding.imgCard.setImageResource(model.imageId)
    }
}