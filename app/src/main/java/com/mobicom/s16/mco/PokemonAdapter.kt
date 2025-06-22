package com.mobicom.s16.mco

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mobicom.s16.mco.databinding.CardRowBinding

class PokemonAdapter(private val data: ArrayList<PokemonModel>): RecyclerView.Adapter<PokemonViewHolder>()  {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PokemonViewHolder{
        val itemViewBinding: CardRowBinding = CardRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PokemonViewHolder(itemViewBinding)
    }

    override fun onBindViewHolder(holder: PokemonViewHolder, position: Int) {
        holder.bindData(this.data.get(position))
    }

    override fun getItemCount(): Int {
        return data.size
    }
}