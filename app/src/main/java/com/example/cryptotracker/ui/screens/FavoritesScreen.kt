package com.example.cryptotracker.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.cryptotracker.data.db.AlertEntity
import com.example.cryptotracker.data.db.CoinEntity
import androidx.compose.runtime.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    favorites: List<CoinEntity>,
    alerts: Map<String, AlertEntity>,
    onBack: () -> Unit,
    onSaveAlert: (CoinEntity, Double) -> Unit,
    onRemoveAlert: (String) -> Unit,
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
                        val alert = alerts[coin.id]   // get the alert for this coin (if any)

                        FavoriteItem(
                            coin = coin,
                            alert = alert,
                            onSaveAlert = onSaveAlert,
                            onRemoveAlert = onRemoveAlert,
                            onRemove = onRemove
                        )

                        Divider(color = Color.Gray.copy(alpha = 0.3f))
                    }
                }
            }
        }
    }
}

@Composable
fun FavoriteItem(
    coin: CoinEntity,
    alert: AlertEntity?,
    onSaveAlert: (CoinEntity, Double) -> Unit, //for saving alerts
    onRemoveAlert: (String) -> Unit,           // for deleting alerts
    onRemove: (CoinEntity) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

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

            // Show price + alert threshold
            val alertText = alert?.let { "Alert: $${"%.2f".format(it.targetPrice)}" } ?: "No alert"
            Text(
                text = "${coin.symbol.uppercase()} — $${coin.price}  •  $alertText",
                color = Color.LightGray
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // 👇 New bell icon for alerts
        IconButton(onClick = { showDialog = true }) {
            Icon(
                imageVector = if (alert == null) Icons.Outlined.Notifications else Icons.Filled.Notifications,
                contentDescription = "Set alert",
                tint = if (alert == null) Color.Gray else Color(0xFFFFC107)
            )
        }

        // Existing delete icon
        IconButton(onClick = { onRemove(coin) }) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Remove",
                tint = Color.Red
            )
        }
    }

    // 👇 Show dialog when bell is pressed
    if (showDialog) {
        AlertInputDialog(
            coinName = coin.name,
            existing = alert,
            onConfirm = { target -> onSaveAlert(coin, target) },
            onRemove = if (alert != null) ({ onRemoveAlert(coin.id) }) else null,
            onDismiss = { showDialog = false }
        )
    }
}




@Composable
fun AlertInputDialog(
    coinName: String,
    existing: AlertEntity?,
    onConfirm: (Double) -> Unit,
    onRemove: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    var input by remember { mutableStateOf(existing?.targetPrice?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Set alert for $coinName" else "Edit alert for $coinName") },
        text = {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                label = { Text("Target price (e.g. 45000.0)") },
                singleLine = true
            )
        },
        confirmButton = {
            Button(onClick = {
                input.toDoubleOrNull()?.let(onConfirm)
                onDismiss()
            }) { Text("Save") }
        },
        dismissButton = {
            Row {
                if (existing != null && onRemove != null) {
                    TextButton(onClick = { onRemove(); onDismiss() }) { Text("Remove") }
                }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )
}