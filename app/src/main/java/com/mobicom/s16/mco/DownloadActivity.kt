package com.mobicom.s16.mco

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mobicom.s16.mco.data.remote.api.RetrofitClient
import com.mobicom.s16.mco.data.remote.dto.CardsResponse
import com.mobicom.s16.mco.databinding.ActivityDownloadBinding
import com.mobicom.s16.mco.domain.model.Card
import com.mobicom.s16.mco.util.CardCacheManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DownloadActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDownloadBinding

    private val allCards = mutableListOf<Card>()
    private val pageSize = 250
    private var currentPage = 1
    private val maxRetries = 3
    private val failedPages = mutableSetOf<Int>()
    private var downloadMissingOnly = false
    private var existingCardsMap = mutableMapOf<String, Card>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDownloadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        downloadMissingOnly = intent.getBooleanExtra("missingOnly", false)
        Log.d("DownloadActivity", "DownloadActivity started, missingOnly=$downloadMissingOnly")

        if (downloadMissingOnly) {
            val cachedCards = CardCacheManager.loadCardsFromCache(this)
            allCards.addAll(cachedCards)
            existingCardsMap = cachedCards.associateBy { it.name }.toMutableMap()
        }

       startDownload()
    }

    private fun startDownload() {
        binding.statusText.text = "Downloading cards..."
        binding.progressBar.visibility = View.VISIBLE
        downloadNextPage()
    }

    private fun downloadNextPage(retryCount: Int = 0) {
        Log.d("DownloadActivity", "Requesting page $currentPage (Retry: $retryCount)")

        RetrofitClient.api.getCards(currentPage, pageSize)
            .enqueue(object : Callback<CardsResponse> {
                override fun onResponse(call: Call<CardsResponse>, response: Response<CardsResponse>) {
                    if (response.isSuccessful) {
                        val apiCards = response.body()?.data ?: emptyList()
                        Log.d("DownloadActivity", "Page $currentPage: Fetched ${apiCards.size} cards")

                        val newCards = apiCards.mapNotNull { apiCard ->
                            val card = Card(
                                name = apiCard.name,
                                set = apiCard.set.name ?: "Unknown",
                                hp = apiCard.hp ?: "N/A",
                                supertype = apiCard.supertype ?: "N/A",
//                                firstAttack = apiCard.attacks?.firstOrNull()?.name ?: "None",
//                                price = apiCard.tcgplayer?.prices?.holofoil?.market?.toString() ?: "N/A",
                                imageUrl = apiCard.images.large ?: "",
                                rarity = apiCard.rarity ?: "Unknown"
                            )
                            if (!downloadMissingOnly || !existingCardsMap.containsKey(card.name)) {
                                card
                            } else null
                        }

                        allCards.addAll(newCards)

                        if (apiCards.size < pageSize) {
                            finishDownload()
                        } else {
                            currentPage++
                            downloadNextPage()
                        }
                    } else {
                        Log.e("DownloadActivity", "Page $currentPage failed with ${response.code()}")
                        if (response.code() == 404) {
                            finishDownload()
                        } else if (retryCount < maxRetries) {
                            downloadNextPage(retryCount + 1)
                        } else {
                            failedPages.add(currentPage)
                            currentPage++
                            downloadNextPage()
                        }
                    }
                }

                override fun onFailure(call: Call<CardsResponse>, t: Throwable) {
                    Log.e("DownloadActivity", "Page $currentPage download failed: ${t.message}")
                    if (retryCount < maxRetries) {
                        downloadNextPage(retryCount + 1)
                    } else {
                        failedPages.add(currentPage)
                        currentPage++
                        downloadNextPage()
                    }
                }
            })
    }

    private fun finishDownload() {
        Log.d("DownloadActivity", "Download complete: ${allCards.size} cards.")
        if (failedPages.isNotEmpty()) {
            Log.w("DownloadActivity", "Failed pages: $failedPages")
        }

        CardCacheManager.saveCardsToCache(this, allCards)

        binding.progressBar.visibility = View.GONE
        binding.statusText.text = "Download complete: ${allCards.size} cards"
        Toast.makeText(this, "Download complete!", Toast.LENGTH_SHORT).show()

        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
