package com.example.cryptotracker.data.network
import com.example.cryptotracker.data.model.Coin
import retrofit2.http.GET
import retrofit2.http.Query

// Retrofit API interface for communicating with the CoinGecko REST API
// Defines endpoints and query parameters
interface CoinGeckoApi {
    @GET("coins/markets")
    suspend fun getMarketData(
        @Query("vs_currency") currency: String = "usd",
        @Query("order") order: String = "market_cap_desc",
        @Query("per_page") perPage: Int = 20,
        @Query("page") page: Int = 1,
        @Query("sparkline") sparkline: Boolean = false
    ): List<Coin>

    // Query example ( https://api.coingecko.com/api/v3/coins/markets?vs_currency=usd&order=market_cap_desc&per_page=20&page=1)
}