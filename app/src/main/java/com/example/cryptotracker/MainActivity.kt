package com.example.cryptotracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.LaunchedEffect
import com.example.cryptotracker.data.repository.CoinRepository
import com.example.cryptotracker.ui.CoinViewModel
import com.example.cryptotracker.ui.screens.MainScreen
import com.example.cryptotracker.ui.theme.CryptoTrackerTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.cryptotracker.ui.screens.FavoritesScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        setContent {
            CryptoTrackerTheme {
                val vm = remember { CoinViewModel(CoinRepository(this)) }
                val coins by vm.coins.collectAsState()
                val isLoading by vm.isLoading.collectAsState()
                val favorites by vm.favorites.collectAsState()
                val favoriteList by vm.favoriteList.collectAsState()
                val alerts by vm.alerts.collectAsState()
                var showFavorites by remember { mutableStateOf(false) }

                // load once when the app starts
                LaunchedEffect(Unit) {
                    vm.loadCoins()
                    vm.syncFavorites()
                    vm.loadAlerts()
                    vm.startAlertChecker(this@MainActivity) // start periodic check
                }


                if (showFavorites) {
                    FavoritesScreen(
                        favorites = favoriteList,
                        alerts = alerts,
                        onBack = { showFavorites = false },
                        onSaveAlert = { coin, target -> vm.saveOrUpdateAlert(coin, target) },
                        onRemoveAlert = { coinId -> vm.removeAlert(coinId) },
                        onRemove = { vm.removeFavorite(it) }
                    )
                } else {
                    MainScreen(
                        coins = coins,
                        isLoading = isLoading,
                        favorites = favorites,
                        onRefresh = { vm.loadCoins() },
                        onFavoriteClick = { vm.toggleFavorite(it) }
                    )

                    // Floating Action Button to open favorites
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        FloatingActionButton(
                            onClick = {
                                vm.loadFavorites()
                                vm.loadAlerts()
                                showFavorites = true
                            },
                            containerColor = Color(0xFF2196F3),
                            contentColor = Color.White,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "View Favorites"
                            )
                        }
                    }
                }
            }
        }

    }
}