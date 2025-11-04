package com.example.cryptotracker.data.db
import androidx.room.Database
import androidx.room.RoomDatabase

// initializes the Room database structure
// Version 2 because of updated entities
@Database(entities = [CoinEntity::class, AlertEntity::class], version = 6)
abstract class AppDatabase : RoomDatabase() {
    abstract fun coinDao(): CoinDao
    abstract fun alertDao(): AlertDao
}