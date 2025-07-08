package com.example.cryptowallet.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.cryptowallet.data.manager.BalanceManager
import com.example.cryptowallet.navigation.AppRoutes
import com.example.cryptowallet.ui.theme.CryptoWalletTheme
import com.example.cryptowallet.ui.viewmodel.TradingAsset
import com.example.cryptowallet.ui.viewmodel.TradingViewModel
import com.example.cryptowallet.utils.formatCurrency
import java.math.BigDecimal
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TradingScreen(
    navController: NavController,
    viewModel: TradingViewModel = viewModel(factory = TradingViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isVisible by remember { derivedStateOf { BalanceManager.isBalanceVisible } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Giao dịch") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                // Tái sử dụng Header từ FundingScreen
                FundingHeader(
                    totalValue = uiState.totalValue,
                    isVisible = isVisible,
                    onToggleVisibility = { BalanceManager.toggleVisibility() }
                )
            }
            item {
                // CẬP NHẬT: Kết nối các hành động điều hướng
                TradingActionButtons(
                    onTransferClick = { navController.navigate(AppRoutes.TRANSFER_SCREEN) },
                    onConvertClick = { navController.navigate(AppRoutes.CONVERT_SCREEN) },
                    onHistoryClick = { navController.navigate(AppRoutes.TRANSACTIONS_SCREEN) }
                )
            }
            item {
                // Tái sử dụng Header của danh sách tài sản
                FundingAssetListHeader()
            }
            items(uiState.assets) { asset ->
                TradingAssetRow(asset = asset, isVisible = isVisible)
            }
        }
    }
}

@Composable
fun TradingActionButtons(
    onTransferClick: () -> Unit,
    onConvertClick: () -> Unit,
    onHistoryClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        // Tái sử dụng ActionButton từ WalletScreen
        ActionButton(text = "Chuyển tiền", icon = Icons.Default.SwapHoriz, onClick = onTransferClick)
        ActionButton(text = "Chuyển đổi", icon = Icons.Default.CurrencyExchange, onClick = onConvertClick)
        ActionButton(text = "Lịch sử", icon = Icons.Default.History, onClick = onHistoryClick)
    }
}

@Composable
fun TradingAssetRow(asset: TradingAsset, isVisible: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = asset.asset.iconUrl,
            contentDescription = asset.asset.name,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(asset.asset.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text(
                text = if (isVisible) formatCurrency(asset.asset.priceInUsd.toDouble()) else "*****",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = if (isVisible) formatCurrency(asset.getValueInUsd().toDouble()) else "*****",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = if (isVisible) asset.balance.toPlainString() else "***",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun TradingScreenPreview() {
    CryptoWalletTheme {
        TradingScreen(navController = rememberNavController())
    }
}
