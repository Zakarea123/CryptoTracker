package com.example.cryptotracker.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.cryptotracker.data.db.CoinEntity


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    favorites: List<CoinEntity>,
    onBack: () -> Unit,
    onRemove: (CoinEntity) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favorites") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF121212),
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFF1E1E1E))
        ) {
            if (favorites.isEmpty()) {
                Text(
                    "No favorites yet.",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                    items(favorites) { coin ->
                        FavoriteItem(coin, onRemove)
                        Divider(color = Color.Gray.copy(alpha = 0.3f))
                    }
                }
            }
        }
    }
}

@Composable
fun FavoriteItem(coin: CoinEntity, onRemove: (CoinEntity) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberAsyncImagePainter(coin.image),
            contentDescription = coin.name,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = coin.name, color = Color.White, fontWeight = FontWeight.Bold)
            Text(text = "${coin.symbol.uppercase()} — $${coin.price}", color = Color.LightGray)
        }
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = { onRemove(coin) }) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Remove",
                tint = Color.Red
            )
        }
    }
}