package com.example.cryptotracker.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

// Represents alerts table in the database
@Entity(tableName = "alerts", indices = [Index(value = ["coinId"], unique = true)])
data class AlertEntity(
    @PrimaryKey(autoGenerate = true) val id: Int= 0,
    val coinId: String,
    val coinName: String,
    val targetPrice: Double,
    val triggered: Boolean = false ,
    val alertType: String = "ABOVE"
)
