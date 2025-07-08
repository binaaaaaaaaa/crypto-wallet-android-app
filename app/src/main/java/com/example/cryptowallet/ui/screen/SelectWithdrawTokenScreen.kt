package com.example.cryptowallet.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.cryptowallet.model.CryptoAsset
import com.example.cryptowallet.navigation.AppRoutes
import com.example.cryptowallet.ui.theme.CryptoWalletTheme
import com.example.cryptowallet.ui.viewmodel.SelectWithdrawTokenViewModel
import com.example.cryptowallet.utils.formatCurrency

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectWithdrawTokenScreen(
    navController: NavController,
    viewModel: SelectWithdrawTokenViewModel = viewModel(factory = SelectWithdrawTokenViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val filteredAssets = remember(uiState.assets, uiState.searchQuery) {
        if (uiState.searchQuery.isBlank()) {
            uiState.assets
        } else {
            uiState.assets.filter { asset ->
                asset.name.contains(uiState.searchQuery, ignoreCase = true) ||
                        asset.symbol.contains(uiState.searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chọn tiền mã hóa") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                label = { Text("Tìm kiếm") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )

            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Tiền mã hóa", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Text("Số dư", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
                items(filteredAssets, key = { it.id }) { asset ->
                    WithdrawTokenRow(asset = asset) {
                        // Khi chọn một token, điều hướng đến màn hình Chọn Đích đến
                        navController.navigate("${AppRoutes.SELECT_DESTINATION_SCREEN}/${asset.symbol}")
                    }
                }
            }
        }
    }
}

@Composable
fun WithdrawTokenRow(asset: CryptoAsset, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = asset.iconUrl,
            contentDescription = asset.name,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(asset.symbol, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            Text(asset.name, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = asset.balance.toPlainString(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "≈ ${formatCurrency(asset.getValueInUsd().toDouble())}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}
