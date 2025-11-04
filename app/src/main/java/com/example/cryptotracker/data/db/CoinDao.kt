package com.example.cryptotracker.data.db
import androidx.room.*

// Interface to retrieve, Delete and insert favorite coins
@Dao
interface CoinDao {
    @Query("SELECT * FROM favorite_coins")
    suspend fun getAll(): List<CoinEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(coin: CoinEntity)

    @Delete
    suspend fun delete(coin: CoinEntity)
}