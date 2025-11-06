package com.example.cryptotracker.data.network
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Object sets up Retrofit itself, handles HTTP requests and JSON parsing
object RetrofitInstance {
    private const val BASE_URL = "https://api.coingecko.com/api/v3/"

    val api: CoinGeckoApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CoinGeckoApi::class.java)
    }
}