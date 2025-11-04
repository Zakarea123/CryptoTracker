package com.example.cryptotracker.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cryptotracker.data.db.CoinEntity
import com.example.cryptotracker.data.model.Coin
import com.example.cryptotracker.data.repository.CoinRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class CoinViewModel(private val repository: CoinRepository) : ViewModel() {
    private val _coins = MutableStateFlow<List<Coin>>(emptyList())
    val coins = _coins.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    private val _favorites = MutableStateFlow<Set<String>>(emptySet())
    val favorites = _favorites.asStateFlow()
    private val _favoriteList = MutableStateFlow<List<CoinEntity>>(emptyList())
    val favoriteList = _favoriteList.asStateFlow()

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
            _favoriteList.value = _favoriteList.value.filterNot { it.id == coin.id }

            // also update the star state directly
            _favorites.value = _favorites.value - coin.id

            println("❌ Removed ${coin.name} from favorites screen")
        }
    }

    // NEW: one source-of-truth refresh from DB → Set<String>
    fun syncFavorites() {
        viewModelScope.launch {
            val ids = repository.getFavorites().map { it.id }.toSet()
            _favorites.value = ids
        }
    }


}

