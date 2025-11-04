package com.example.cryptotracker.data.model


// Api call retrieves this information
data class Coin(
    val id: String,
    val symbol: String,
    val name: String,
    val image: String,
    val current_price: Double,
    val market_cap: Long,
    val price_change_percentage_24h: Double
)
