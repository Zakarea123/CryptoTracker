package com.example.cryptotracker.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.cryptotracker.data.model.Coin
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    coins: List<Coin>,
    isLoading: Boolean,
    favorites: Set<String>,
    onRefresh: () -> Unit,
    onFavoriteClick: (Coin) -> Unit
)
{
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("CryptoTracker") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF121212),
                    titleContentColor = Color.White
                )
            )
        }
    ) {
        padding -> Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFF1E1E1E))
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White
                )
            } else {
                if (coins.isEmpty()) {
                    Text(
                        text = "No coins available.",
                        color = Color.White,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    ) {
                        items(coins) { coin ->
                            val favorite = favorites.contains(coin.id)
                            CoinItem(
                                coin = coin,
                                isFavorite = favorite,
                                onFavoriteClick = {onFavoriteClick(it)})
                            }
                        }
                    }
                }
            }
        }
    }

@Composable
fun CoinItem(coin: Coin, isFavorite: Boolean, onFavoriteClick: (Coin) -> Unit)
{
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically)
    {
        Image(
            painter = rememberAsyncImagePainter(coin.image),
            contentDescription = coin.name,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column{
            Text(text = coin.name, color = Color.White, fontWeight = FontWeight.Bold)
            Text(text = "${coin.symbol.uppercase()} — $${coin.current_price}", color = Color.LightGray)
        }
        Spacer(modifier = Modifier.weight(1f))

        val change = coin.price_change_percentage_24h
        val color = if (change >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
        Text(
            text = String.format("%.2f%%", change),
            color = color,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(end = 8.dp)
        )

        // Favorite toggle button
        IconButton(onClick = { onFavoriteClick(coin) }) {
            Icon(
                imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = if (isFavorite) "Unfavorite" else "Favorite",
                tint = if (isFavorite) Color.Yellow else Color.Gray
            )
        }
    }
}