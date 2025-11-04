package com.example.cryptotracker.data.repository
import android.content.Context
import androidx.room.Room
import com.example.cryptotracker.data.db.AppDatabase
import com.example.cryptotracker.data.db.CoinEntity
import com.example.cryptotracker.data.model.Coin
import com.example.cryptotracker.data.network.RetrofitInstance

class CoinRepository(context: Context) {
    private val db = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "crypto_db"
    )
        .fallbackToDestructiveMigration(false)
        .build()
    private val dao = db.coinDao()

    suspend fun fetchCoins(): List<Coin> {
        return RetrofitInstance.api.getMarketData()
    }

    suspend fun getFavorites(): List<CoinEntity> = dao.getAll()
    suspend fun addFavorite(coin: CoinEntity) = dao.insert(coin)
    suspend fun removeFavorite(coin: CoinEntity) = dao.delete(coin)
}