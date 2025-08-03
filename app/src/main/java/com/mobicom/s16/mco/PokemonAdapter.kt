import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mobicom.s16.mco.CardInfoActivity
import com.mobicom.s16.mco.databinding.CardRowBinding
import com.mobicom.s16.mco.domain.model.Card

class PokemonAdapter(private var cards: List<Card>, private var tab: String) :
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
            val intent = Intent(holder.itemView.context, CardInfoActivity::class.java).apply {
                // Basic card info
                putExtra("CARD_ID", card.id)
                putExtra("CARD_NAME", card.name)
                putExtra("CARD_IMAGE_URL", card.imageUrl)
                putExtra("CARD_IMAGE_URL_LARGE", card.imageUrlLarge)

                // Set information
                putExtra("CARD_SET", card.set)
                putExtra("CARD_SET_ID", card.setId)
                putExtra("CARD_SET_SERIES", card.setSeries)

                // Price information
                putExtra("CARD_PRICE", card.price)
                putExtra("CARD_PRICE_SOURCE", card.priceSource)

                // Card details
                putExtra("CARD_NUMBER", card.number)
                putExtra("CARD_RARITY", card.rarity ?: "")
                putExtra("CARD_SUPERTYPE", card.supertype)
                putExtra("CARD_HP", card.hp ?: "")
                putExtra("CARD_ARTIST", card.artist ?: "")
                putExtra("CARD_FLAVOR_TEXT", card.flavorText ?: "")

                // Types and subtypes
                putStringArrayListExtra("CARD_TYPES", ArrayList(card.types ?: emptyList()))
                putStringArrayListExtra("CARD_SUBTYPES", ArrayList(card.subtypes ?: emptyList()))

                // Evolution
                putExtra("CARD_EVOLVES_FROM", card.evolvesFrom ?: "")
                putStringArrayListExtra("CARD_EVOLVES_TO", ArrayList(card.evolvesTo ?: emptyList()))

                // Abilities
                card.abilities?.let { abilities ->
                    if (abilities.isNotEmpty()) {
                        putExtra("CARD_ABILITY_NAME", abilities[0].name)
                        putExtra("CARD_ABILITY_TEXT", abilities[0].text)
                        putExtra("CARD_ABILITY_TYPE", abilities[0].type ?: "")
                    }
                }

                // Attacks
                card.attacks?.let { attacks ->
                    if (attacks.isNotEmpty()) {
                        putExtra("CARD_ATTACK1_NAME", attacks[0].name)
                        putExtra("CARD_ATTACK1_DAMAGE", attacks[0].damage ?: "")
                        putExtra("CARD_ATTACK1_TEXT", attacks[0].text ?: "")
                        putExtra("CARD_ATTACK1_COST", attacks[0].convertedEnergyCost ?: 0)
                        putStringArrayListExtra("CARD_ATTACK1_COST_TYPES", ArrayList(attacks[0].cost ?: emptyList()))
                    }

                    if (attacks.size > 1) {
                        putExtra("CARD_ATTACK2_NAME", attacks[1].name)
                        putExtra("CARD_ATTACK2_DAMAGE", attacks[1].damage ?: "")
                        putExtra("CARD_ATTACK2_TEXT", attacks[1].text ?: "")
                        putExtra("CARD_ATTACK2_COST", attacks[1].convertedEnergyCost ?: 0)
                        putStringArrayListExtra("CARD_ATTACK2_COST_TYPES", ArrayList(attacks[1].cost ?: emptyList()))
                    }
                }

                // Weaknesses
                card.weaknesses?.let { weaknesses ->
                    if (weaknesses.isNotEmpty()) {
                        putExtra("CARD_WEAKNESS_TYPE", weaknesses[0].type)
                        putExtra("CARD_WEAKNESS_VALUE", weaknesses[0].value)
                    }
                }

                // Resistances
                card.resistances?.let { resistances ->
                    if (resistances.isNotEmpty()) {
                        putExtra("CARD_RESISTANCE_TYPE", resistances[0].type)
                        putExtra("CARD_RESISTANCE_VALUE", resistances[0].value)
                    }
                }

                // Retreat cost
                putExtra("CARD_RETREAT_COST", card.convertedRetreatCost ?: 0)
                putStringArrayListExtra("CARD_RETREAT_COST_TYPES", ArrayList(card.retreatCost ?: emptyList()))

                putExtra("SOURCE_TAB", tab)
            }

            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = cards.size

    fun updateData(newCards: List<Card>) {
        cards = newCards
        notifyDataSetChanged()
    }
}