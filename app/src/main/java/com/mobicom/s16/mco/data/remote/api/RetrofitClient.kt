package com.mobicom.s16.mco.data.remote.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)  // default is 10s
        .readTimeout(60, TimeUnit.SECONDS)     // default is 10s
        .writeTimeout(60, TimeUnit.SECONDS)    // default is 10s
        .retryOnConnectionFailure(true)        // auto retry on network failure
        .build()

    val api: PokemonTcgApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.pokemontcg.io/v2/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PokemonTcgApi::class.java)
    }
}