package com.mobicom.s16.mco

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.mobicom.s16.mco.databinding.CardinfoPageBinding
import androidx.core.graphics.toColorInt


class CardInfoActivity : AppCompatActivity() {
    private lateinit var binding: CardinfoPageBinding
    private lateinit var cardToArchive: com.mobicom.s16.mco.domain.model.Card
    private var sourceTab: String? = null
    private var isFromArchive: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = CardinfoPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupDummyLineChart(binding.priceChart)
        binding.imgBack.setOnClickListener {
            finish()
        }

        // Get card data from intent extras and populate views
        populateCardData()

        sourceTab = intent.getStringExtra("SOURCE_TAB")
        isFromArchive = sourceTab == "ARCHIVE"

        if (isFromArchive) {
            binding.btnArchive.text = "REMOVE CARD"
            binding.btnArchive.backgroundTintList = ColorStateList.valueOf("#f44336".toColorInt())
            binding.btnWishlist.text = "MOVE TO WISHLIST"
        }else{
            binding.btnWishlist.text = "REMOVE CARD"
            binding.btnWishlist.backgroundTintList = ColorStateList.valueOf("#f44336".toColorInt())
            binding.btnArchive.text = "MOVE TO ARCHIVE"
        }
    }

    private fun populateCardData() {
        // Basic card information
        val cardName = intent.getStringExtra("CARD_NAME") ?: ""
        val imageUrl = intent.getStringExtra("CARD_IMAGE_URL") ?: ""
        val imageUrlLarge = intent.getStringExtra("CARD_IMAGE_URL_LARGE") ?: ""
        val set = intent.getStringExtra("CARD_SET") ?: ""
        val setSeries = intent.getStringExtra("CARD_SET_SERIES") ?: ""
        val price = intent.getDoubleExtra("CARD_PRICE", 0.0)
        val priceSource = intent.getStringExtra("CARD_PRICE_SOURCE") ?: ""
        val number = intent.getStringExtra("CARD_NUMBER") ?: ""
        val rarity = intent.getStringExtra("CARD_RARITY") ?: ""
        val supertype = intent.getStringExtra("CARD_SUPERTYPE") ?: ""
        val hp = intent.getStringExtra("CARD_HP") ?: ""
        val artist = intent.getStringExtra("CARD_ARTIST") ?: ""
        val flavorText = intent.getStringExtra("CARD_FLAVOR_TEXT") ?: ""

        // Arrays
        val types = intent.getStringArrayListExtra("CARD_TYPES") ?: arrayListOf()
        val subtypes = intent.getStringArrayListExtra("CARD_SUBTYPES") ?: arrayListOf()

        // Evolution
        val evolvesFrom = intent.getStringExtra("CARD_EVOLVES_FROM") ?: ""
        val evolvesTo = intent.getStringArrayListExtra("CARD_EVOLVES_TO") ?: arrayListOf()

        // Abilities
        val abilityName = intent.getStringExtra("CARD_ABILITY_NAME") ?: ""
        val abilityText = intent.getStringExtra("CARD_ABILITY_TEXT") ?: ""
        val abilityType = intent.getStringExtra("CARD_ABILITY_TYPE") ?: ""

        // Attacks
        val attack1Name = intent.getStringExtra("CARD_ATTACK1_NAME") ?: ""
        val attack1Damage = intent.getStringExtra("CARD_ATTACK1_DAMAGE") ?: ""
        val attack1Text = intent.getStringExtra("CARD_ATTACK1_TEXT") ?: ""
        val attack1Cost = intent.getIntExtra("CARD_ATTACK1_COST", 0)
        val attack1CostTypes = intent.getStringArrayListExtra("CARD_ATTACK1_COST_TYPES") ?: arrayListOf()

        val attack2Name = intent.getStringExtra("CARD_ATTACK2_NAME") ?: ""
        val attack2Damage = intent.getStringExtra("CARD_ATTACK2_DAMAGE") ?: ""
        val attack2Text = intent.getStringExtra("CARD_ATTACK2_TEXT") ?: ""
        val attack2Cost = intent.getIntExtra("CARD_ATTACK2_COST", 0)
        val attack2CostTypes = intent.getStringArrayListExtra("CARD_ATTACK2_COST_TYPES") ?: arrayListOf()

        // Weakness and Resistance
        val weaknessType = intent.getStringExtra("CARD_WEAKNESS_TYPE") ?: ""
        val weaknessValue = intent.getStringExtra("CARD_WEAKNESS_VALUE") ?: ""
        val resistanceType = intent.getStringExtra("CARD_RESISTANCE_TYPE") ?: ""
        val resistanceValue = intent.getStringExtra("CARD_RESISTANCE_VALUE") ?: ""

        // Retreat cost
        val retreatCost = intent.getIntExtra("CARD_RETREAT_COST", 0)
        val retreatCostTypes = intent.getStringArrayListExtra("CARD_RETREAT_COST_TYPES") ?: arrayListOf()

        // Populate basic information
        binding.tvCardName.text = cardName

        // Create a more detailed set display
        val setDisplay = if (setSeries.isNotEmpty()) {
            "$setSeries: $set"
        } else {
            set
        }
        binding.tvSeries.text = setDisplay

        // Format price with source information
        binding.tvPrice.text = when {
            price > 0 -> {
                val priceText = "$%.2f".format(price)
                if (priceSource.isNotEmpty()) "$priceText ($priceSource)" else priceText
            }
            else -> "Price not available"
        }

        // Load card image (prefer large image for detail view)
        val displayImageUrl = if (imageUrlLarge.isNotEmpty()) imageUrlLarge else imageUrl
        Glide.with(this)
            .load(displayImageUrl)
            .into(binding.imgCard)

        // Populate detailed information
        binding.tvNum.text = number
        binding.tvRarity.text = rarity

        // Display primary type
        binding.tvType.text = types.firstOrNull() ?: supertype
        binding.tvHP.text = hp

        // Display stage (first subtype)
        binding.tvStage.text = subtypes.firstOrNull() ?: ""

        // Handle abilities
        if (abilityName.isNotEmpty()) {
            binding.tvAbility.text = abilityName
            binding.tvAbilityDesc.text = abilityText
        } else {
            hideAbilitySection()
        }

        // Handle first attack
        if (attack1Name.isNotEmpty()) {
            val attack1DisplayName = if (attack1Damage.isNotEmpty()) {
                "$attack1Name ($attack1Damage)"
            } else {
                attack1Name
            }
            binding.tvAttack1.text = attack1DisplayName
            binding.tvAttack1Desc.text = attack1Text
        } else {
            hideAttack1Section()
        }

        // Handle second attack
        if (attack2Name.isNotEmpty()) {
            val attack2DisplayName = if (attack2Damage.isNotEmpty()) {
                "$attack2Name ($attack2Damage)"
            } else {
                attack2Name
            }
            binding.tvAttack2.text = attack2DisplayName
            binding.tvAttack3Desc.text = attack2Text
        } else {
            hideAttack2Section()
        }

        // Handle weakness
        if (weaknessType.isNotEmpty()) {
            binding.tvWeakness.text = "$weaknessType$weaknessValue"
        } else {
            binding.tvWeakness.text = "None"
        }

        // Handle resistance
        if (resistanceType.isNotEmpty()) {
            binding.tvResistance.text = "$resistanceType$resistanceValue"
        } else {
            binding.tvResistance.text = "None"
        }

        // Handle retreat cost
        binding.tvRetreat.text = retreatCost.toString()


        cardToArchive = com.mobicom.s16.mco.domain.model.Card(
            name = cardName,
            supertype = supertype,
            subtypes = subtypes,
            hp = hp,
            types = types,
            evolvesFrom = evolvesFrom,
            evolvesTo = evolvesTo,
            attacks = listOfNotNull(
                if (attack1Name.isNotEmpty()) com.mobicom.s16.mco.domain.model.Attack(
                    name = attack1Name,
                    text = attack1Text,
                    damage = attack1Damage,
                    cost = attack1CostTypes
                ) else null,
                if (attack2Name.isNotEmpty()) com.mobicom.s16.mco.domain.model.Attack(
                    name = attack2Name,
                    text = attack2Text,
                    damage = attack2Damage,
                    cost = attack2CostTypes
                ) else null
            ),
            abilities = if (abilityName.isNotEmpty()) listOf(
                com.mobicom.s16.mco.domain.model.Ability(
                    name = abilityName,
                    text = abilityText,
                    type = abilityType
                )
            ) else null,
            weaknesses = if (weaknessType.isNotEmpty()) listOf(
                com.mobicom.s16.mco.domain.model.Weakness(
                    type = weaknessType,
                    value = weaknessValue
                )
            ) else null,
            resistances = if (resistanceType.isNotEmpty()) listOf(
                com.mobicom.s16.mco.domain.model.Resistance(
                    type = resistanceType,
                    value = resistanceValue
                )
            ) else null,
            retreatCost = retreatCostTypes,
            convertedRetreatCost = retreatCost,
            set = set,
            setId = "", // Fill if you have it
            setSeries = setSeries,
            number = number,
            artist = artist,
            rarity = rarity,
            flavorText = flavorText,
            imageUrl = imageUrl,
            imageUrlLarge = imageUrlLarge,
            price = price,
            priceSource = priceSource
        )


        binding.btnArchive.setOnClickListener {
            //val btnText = binding.btnArchive.text
            if(isFromArchive){
                removeFromArchive()
            }else{
                archiveCard()
            }
        }

        binding.btnWishlist.setOnClickListener {
            //val btnText = binding.btnArchive.text
            if(isFromArchive){
                wishlistCard()
            }else{
                removeFromWishlist()
            }
        }

    }


    private fun hideAbilitySection() {
        binding.tvAbilityLbl.visibility = View.GONE
        binding.tvAbility.visibility = View.GONE
        binding.tvAbilityDesc.visibility = View.GONE
    }

    private fun hideAttack1Section() {
        binding.tvAttack1Lbl.visibility = View.GONE
        binding.tvAttack1.visibility = View.GONE
        binding.tvAttack1Desc.visibility = View.GONE
    }

    private fun hideAttack2Section() {
        binding.tvAttack2Lbl.visibility = View.GONE
        binding.tvAttack2.visibility = View.GONE
        binding.tvAttack3Desc.visibility = View.GONE
    }

    private fun archiveCard() {
        com.mobicom.s16.mco.data.remote.firebase.FirestoreRepository.archiveCard(
            card = cardToArchive,
            onSuccess = {
                val resultIntent = intent.apply {
                    putExtra("ARCHIVED_CARD_ID", cardToArchive.id) // Add ID in intent if needed
                }
                setResult(RESULT_OK, resultIntent)
                finish() // Close activity or show a toast/snackbar
                Toast.makeText(this, "Card successfully saved", Toast.LENGTH_SHORT).show()
            },
            onFailure = {
                it.printStackTrace()
                // Optionally show error message
            }
        )
    }

    private fun wishlistCard() {
        com.mobicom.s16.mco.data.remote.firebase.FirestoreRepository.wishlistCard(
            card = cardToArchive,
            onSuccess = {
                val resultIntent = intent.apply {
                    putExtra("ARCHIVED_CARD_ID", cardToArchive.id) // Add ID in intent if needed
                }
                setResult(RESULT_OK, resultIntent)
                finish() // Close activity or show a toast/snackbar
                Toast.makeText(this, "Card successfully saved", Toast.LENGTH_SHORT).show()
            },
            onFailure = {
                it.printStackTrace()
                // Optionally show error message
            }
        )
    }

    private fun removeFromArchive(){
        com.mobicom.s16.mco.data.remote.firebase.FirestoreRepository.removeCardFromArchive(
            card = cardToArchive,
            onSuccess = {
                val resultIntent = intent.apply {
                    putExtra("ARCHIVED_CARD_ID", cardToArchive.id) // Add ID in intent if needed
                }
                setResult(RESULT_OK, resultIntent)
                finish() // Close activity or show a toast/snackbar
                Toast.makeText(this, "Card successfully removed", Toast.LENGTH_SHORT).show()
            },
            onFailure = {
                it.printStackTrace()
                // Optionally show error message
            }
        )
    }

    private fun removeFromWishlist(){
        com.mobicom.s16.mco.data.remote.firebase.FirestoreRepository.removeCardFromWishlist(
            card = cardToArchive,
            onSuccess = {
                val resultIntent = intent.apply {
                    putExtra("ARCHIVED_CARD_ID", cardToArchive.id) // Add ID in intent if needed
                }
                setResult(RESULT_OK, resultIntent)
                finish() // Close activity or show a toast/snackbar
                Toast.makeText(this, "Card successfully removed", Toast.LENGTH_SHORT).show()
            },
            onFailure = {
                it.printStackTrace()
                // Optionally show error message
            }
        )
    }
}



fun setupDummyLineChart(lineChart: LineChart) {
    // Dummy data entries (x: days, y: price)
    val entries = listOf(
        Entry(1f, 120f),
        Entry(2f, 122f),
        Entry(3f, 118f),
        Entry(4f, 135f),
        Entry(5f, 149f)
    )

    val dataSet = LineDataSet(entries, "Price History").apply {
        color = Color.parseColor("#8c55b0")
        valueTextColor = Color.BLACK
        lineWidth = 2f
        circleRadius = 4f
        setCircleColor(Color.parseColor("#8c55b0"))
        setDrawFilled(true)
        fillColor = Color.parseColor("#E8DAF5")
    }

    val lineData = LineData(dataSet)

    lineChart.data = lineData

    // Styling
    lineChart.axisRight.isEnabled = false
    lineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
    lineChart.description.isEnabled = false
    lineChart.setTouchEnabled(false)
    lineChart.legend.isEnabled = false
    lineChart.invalidate() // Refresh the chart
}