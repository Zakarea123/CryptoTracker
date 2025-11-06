package com.example.cryptotracker.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

// Interface managing price alerts, handles insert, delete, and query operations for alerts
@Dao
interface AlertDao {
    @Query("SELECT * FROM alerts")
    suspend fun getAll(): List<AlertEntity>

    @Query("SELECT * FROM alerts WHERE coinId = :coinId LIMIT 1")
    suspend fun getByCoinId(coinId: String): AlertEntity?

    // Insert or update alert ( Prevents dupelicates)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(alert: AlertEntity)

    @Query("DELETE FROM alerts WHERE coinId = :coinId")
    suspend fun deleteByCoinId(coinId: String)

    // Update status for an alert (triggered = True/ False)
    @Query("UPDATE alerts SET triggered = :triggered WHERE id = :id")
    suspend fun updateTriggered(id: Int, triggered: Boolean)

    //Update an existing alert record, example ( modify the target price or reset 'triggered' value)
    @Update
    suspend fun updateAlert(alert: AlertEntity)
}