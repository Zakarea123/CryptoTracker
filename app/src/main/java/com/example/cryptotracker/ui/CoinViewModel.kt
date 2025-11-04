package com.example.cryptotracker.ui

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cryptotracker.R
import com.example.cryptotracker.data.db.AlertEntity
import com.example.cryptotracker.data.db.CoinEntity
import com.example.cryptotracker.data.model.Coin
import com.example.cryptotracker.data.repository.CoinRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class CoinViewModel(private val repository: CoinRepository) : ViewModel() {

    // Coins
    private val _coins = MutableStateFlow<List<Coin>>(emptyList())
    val coins = _coins.asStateFlow()

    // Screen Lodaing ?
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // Favorite Coins
    private val _favorites = MutableStateFlow<Set<String>>(emptySet())
    val favorites = _favorites.asStateFlow()
    // Favorite List of coins
    private val _favoriteList = MutableStateFlow<List<CoinEntity>>(emptyList())
    val favoriteList = _favoriteList.asStateFlow()

    // Alerts
    private val _alerts = MutableStateFlow<Map<String, AlertEntity>>(emptyMap())
    val alerts = _alerts.asStateFlow()


    fun loadCoins() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val result = repository.fetchCoins()
                println("✅ API returned ${result.size} coins")
                _coins.value = result
                println("🎯 ViewModel coins updated: ${_coins.value.size}")
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }




    fun toggleFavorite(coin: Coin) {
        viewModelScope.launch {
            val current = _favorites.value.toMutableSet()
            if (coin.id in current) {
                repository.removeFavorite(
                    CoinEntity(
                        id = coin.id,
                        name = coin.name,
                        symbol = coin.symbol,
                        price = coin.current_price,
                        image = coin.image

                    )
                )
                repository.deleteAlertFor(coin.id)
                current.remove(coin.id)
                println("⭐ Removed ${coin.name} from favorites")
            } else {
                repository.addFavorite(
                    CoinEntity(
                        id = coin.id,
                        name = coin.name,
                        symbol = coin.symbol,
                        price = coin.current_price,
                        image = coin.image

                    )
                )
                current.add(coin.id)
                println("⭐ Saved ${coin.name} to favorites")
            }
            _favorites.value = current
        }
    }





    fun loadFavorites() {
        viewModelScope.launch {
            _favoriteList.value = repository.getFavorites()
        }
    }




    fun removeFavorite(coin: CoinEntity) {
        viewModelScope.launch {
            repository.removeFavorite(coin)

            // Also delete alert for this coin
            repository.deleteAlertByCoinId(coin.id)

            _favoriteList.value = _favoriteList.value.filterNot { it.id == coin.id }
            _favorites.value = _favorites.value - coin.id
            println("❌ Removed ${coin.name} and its alert (if existed)")
        }
    }





    // NEW: one source-of-truth refresh from DB → Set<String>
    fun syncFavorites() {
        viewModelScope.launch {
            val ids = repository.getFavorites().map { it.id }.toSet()
            _favorites.value = ids
        }
    }


    // Alert functions

    fun loadAlerts() {
        viewModelScope.launch {
            val all = repository.getAllAlerts()
            _alerts.value = repository.getAllAlerts().associateBy { it.coinId }
            println("🔔 Loaded ${all.size} alerts from DB")
        }
    }


    fun saveOrUpdateAlert(coin: CoinEntity, target: Double) {
        viewModelScope.launch {
            repository.upsertAlert(
                AlertEntity(
                    coinId = coin.id,
                    coinName = coin.name,
                    targetPrice = target
                )
            )
            loadAlerts() // refresh map for UI
        }
    }


    fun removeAlert(coinId: String) {
        viewModelScope.launch {
            repository.deleteAlertFor(coinId)
            _alerts.value = _alerts.value - coinId
        }
    }



    fun startAlertChecker(context: Context) {
        viewModelScope.launch {
            while (true) {
                println("🔄 Checking alerts...")
                try {
                    val alerts = repository.getAllAlerts()
                    println("📊 Found ${alerts.size} alerts in database")
                    val coins = _coins.value   // use existing prices
                    println("💰 Currently tracking ${coins.size} coins")

                    for (alert in alerts) {
                        val coin = coins.find { it.id == alert.coinId }
                        println("🔍 Comparing ${coin?.name}: current=${coin?.current_price}, target=${alert.targetPrice}")
                        if (coin != null && !alert.triggered) {

                            if (coin.current_price >= alert.targetPrice) {
                                println("🚨 Triggered ${coin.name} alert!")
                                showNotification(context, "${coin.name} reached ${coin.current_price}")
                                vibratePhone(context)
                                // Delete the alert after triggering
                                repository.deleteAlertFor(alert.coinId)
                                // Refresh alerts map for UI update
                                loadAlerts()
                                println("🧹 Removed alert for ${coin.name} after triggering")
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                delay(30_000)
            }
        }
    }




    @SuppressLint("MissingPermission")
    private fun showNotification(context: Context, message: String) {
        val channelId = "price_alerts_channel"
        val manager = context.getSystemService(NotificationManager::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Price Alerts",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Crypto Alert 🚨")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }



    @SuppressLint("MissingPermission")
    private fun vibratePhone(context: Context) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    500, // milliseconds
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(500)
        }
    }



}

