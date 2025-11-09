package com.example.cryptotracker.ui

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

// ViewModel that holds app data and calls the CoinRepository to fetch and store information
class CoinViewModel(private val repository: CoinRepository) : ViewModel() {

    // Expose a readonly StateFlow to the UI so only the ViewModel can modify this data
    private val _coins = MutableStateFlow<List<Coin>>(emptyList())
    val coins = _coins.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _favorites = MutableStateFlow<Set<String>>(emptySet())
    val favorites = _favorites.asStateFlow()

    private val _favoriteList = MutableStateFlow<List<CoinEntity>>(emptyList())
    val favoriteList = _favoriteList.asStateFlow()

    private val _alerts = MutableStateFlow<Map<String, AlertEntity>>(emptyMap())
    val alerts = _alerts.asStateFlow()


    fun loadCoins() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val result = repository.fetchCoins()
                _coins.value = result
            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }
            finally
            {
                _isLoading.value = false
            }
        }
    }

    fun startAutoRefresh() {
        viewModelScope.launch {
            while (true) {
                try {
                    loadCoins()
                    loadFavorites()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(30_000)
                println("Refreshing........")
            }
        }
    }

    // Adds and removes coins from favorites
    fun toggleFavorite(coin: Coin) = viewModelScope.launch {
        val current = _favorites.value.toMutableSet()
        val entity = CoinEntity(coin.id, coin.name, coin.symbol, coin.current_price, coin.image)

        if (coin.id in current) {
            repository.removeFavorite(entity)
            repository.deleteAlertFor(coin.id)
            current.remove(coin.id)
        } else {
            repository.addFavorite(entity)
            current.add(coin.id)
        }

        _favorites.value = current
    }


    fun loadFavorites() {
        viewModelScope.launch {
            val favoFromDb = repository.getFavorites()
            val liveCoins = _coins.value

            // Update favorite prices based on latest data
            val updatedFavo = favoFromDb.map { fav ->
                val liveCoin = liveCoins.find {it.id == fav.id}
                if (liveCoin != null)
                {
                    fav.copy(price = liveCoin.current_price)
                }
                else
                    fav
            }
            _favoriteList.value = updatedFavo
        }
    }


    fun removeFavorite(coin: CoinEntity) {
        viewModelScope.launch{
            repository.removeFavorite(coin)

            // Also delete alert for this coin
            repository.deleteAlertFor(coin.id)

            _favoriteList.value = _favoriteList.value.filterNot { it.id == coin.id }
            _favorites.value = _favorites.value - coin.id
        }
    }


    // Sync favorites with main page
    fun syncFavorites() {
        viewModelScope.launch {
            val ids = repository.getFavorites().map { it.id }.toSet()
            _favorites.value = ids
        }
    }


    // Alert functions
    fun loadAlerts() {
        viewModelScope.launch {
            _alerts.value = repository.getAllAlerts().associateBy { it.coinId }
        }
    }


    fun saveOrUpdateAlert(coin: CoinEntity, target: Double, type: String) {
        viewModelScope.launch {
            repository.upsertAlert(
                AlertEntity(
                    coinId = coin.id,
                    coinName = coin.name,
                    targetPrice = target,
                    alertType = type
                )
            )
            loadAlerts()
        }
    }


    fun removeAlert(coinId: String) {
        viewModelScope.launch {
            repository.deleteAlertFor(coinId)
            _alerts.value = _alerts.value - coinId
        }
    }


    // Checks all favorited coins and triggers notifications when their prices reach the user’s alert targets
    fun startAlertChecker(context: Context) {
        viewModelScope.launch {
            while (true) {
                try {
                    val alerts = repository.getAllAlerts()
                    val coins = _coins.value   // use existing prices, because of api limitations
                    for (alert in alerts) {
                        val coin = coins.find { it.id == alert.coinId }
                        if (coin != null)
                        {
                            val shouldTrigger =
                                (alert.alertType == "ABOVE" && coin.current_price >= alert.targetPrice) ||
                                        (alert.alertType == "BELOW" && coin.current_price <= alert.targetPrice)
                            if (shouldTrigger)
                            {
                                showNotification(context, "${coin.name} ${alert.alertType.lowercase()} $${"%.2f".format(alert.targetPrice)}")
                                vibratePhone(context)

                                repository.deleteAlertFor(alert.coinId)
                                loadAlerts()
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                delay(10000)
                println("Checking alerts")
            }
        }
    }

    // Done with OpenAI help
    private fun showNotification(context: Context, message: String) {
        val channelId = "price_alerts_channel"
        val manager = context.getSystemService(NotificationManager::class.java)

            val channel = NotificationChannel(
                channelId,
                "Price Alerts",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)


        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle("Crypto Alert 🚨")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }


    private fun vibratePhone(context: Context) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        // Checks the OS version to pick the right API for full compatibility
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            vibrator.vibrate(VibrationEffect.createOneShot(500,VibrationEffect.DEFAULT_AMPLITUDE))
        }
        else
        {
            // Fallback for older devices
            @Suppress("DEPRECATION")
            vibrator.vibrate(500)
        }
    }



}

