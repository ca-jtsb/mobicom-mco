package com.mobicom.s16.mco.data.remote.api

import com.mobicom.s16.mco.data.remote.dto.CardsResponse
import com.mobicom.s16.mco.data.remote.dto.SingleCardResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

interface PokemonTcgApi {
    @Headers("X-Api-Key: 311afd9e-7f7a-4e7e-ab12-51f2ecba9973")
    @GET("cards")
    fun getCards(@Query("q") query: String): Call<CardsResponse>

    @GET("cards/{id}")
    fun getCardById(
        @Path("id") id: String
    ): Call<SingleCardResponse>

}