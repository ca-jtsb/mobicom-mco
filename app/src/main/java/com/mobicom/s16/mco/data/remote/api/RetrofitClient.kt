package com.mobicom.s16.mco.data.remote.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "https://api.pokemontcg.io/v2/"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS) // Increase connect timeout
        .readTimeout(30, TimeUnit.SECONDS)    // Increase read timeout
        .writeTimeout(30, TimeUnit.SECONDS)   // Increase write timeout
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client) // attach custom timeout client
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: PokemonTcgApi = retrofit.create(PokemonTcgApi::class.java)
}
