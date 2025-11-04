package com.example.cryptotracker.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface AlertDao {
    @Query("SELECT * FROM alerts")
    suspend fun getAll(): List<AlertEntity>

    @Query("SELECT * FROM alerts WHERE coinId = :coinId LIMIT 1")
    suspend fun getByCoinId(coinId: String): AlertEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(alert: AlertEntity)

    @Query("DELETE FROM alerts WHERE coinId = :coinId")
    suspend fun deleteByCoinId(coinId: String)

    @Query("UPDATE alerts SET triggered = :triggered WHERE id = :id")
    suspend fun updateTriggered(id: Int, triggered: Boolean)

    @Update
    suspend fun updateAlert(alert: AlertEntity)
}