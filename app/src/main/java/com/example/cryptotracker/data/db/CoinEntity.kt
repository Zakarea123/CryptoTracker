package com.example.cryptotracker.data.db
import androidx.room.Entity
import androidx.room.PrimaryKey

// Represents the favorites table in the database
@Entity(tableName = "favorite_coins")
data class CoinEntity(
    @PrimaryKey val id: String,
    val name: String,
    val symbol: String,
    val price: Double,
    val image: String
)
