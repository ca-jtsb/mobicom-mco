
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mobicom.s16.mco.CardInfoActivity
import com.mobicom.s16.mco.databinding.CardRowBinding
import com.mobicom.s16.mco.domain.model.Card

class PokemonAdapter(private var cards: List<Card>) :
    RecyclerView.Adapter<PokemonAdapter.CardViewHolder>() {

    inner class CardViewHolder(val binding: CardRowBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val binding = CardRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val card = cards[position]
        Glide.with(holder.binding.imgCard.context)
            .load(card.imageUrl)
            .into(holder.binding.imgCard)

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, CardInfoActivity::class.java)
            // TODO: pass card details
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = cards.size

    fun updateData(newCards: List<Card>) {
        cards = newCards
        notifyDataSetChanged()
    }
}

