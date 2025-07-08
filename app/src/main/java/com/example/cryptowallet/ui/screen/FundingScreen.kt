package com.example.cryptowallet.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.example.cryptowallet.ui.viewmodel.FundingAsset
import com.example.cryptowallet.ui.viewmodel.FundingViewModel
import com.example.cryptowallet.utils.formatCurrency
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FundingScreen(
    navController: NavController,
    viewModel: FundingViewModel = viewModel(factory = FundingViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isVisible by remember { derivedStateOf { BalanceManager.isBalanceVisible } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Funding") },
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
                FundingHeader(
                    totalValue = uiState.totalValue,
                    isVisible = isVisible,
                    onToggleVisibility = { BalanceManager.toggleVisibility() }
                )
            }
            item {
                // Gọi hàm ActionButtons đã có sẵn, không định nghĩa lại
                ActionButtons(
                    onDepositClick = { navController.navigate(AppRoutes.SELECT_TOKEN_SCREEN) },
                    onWithdrawClick = { navController.navigate(AppRoutes.SELECT_WITHDRAW_TOKEN_SCREEN) },
                    onTransferClick = { navController.navigate(AppRoutes.TRANSFER_SCREEN) },
                    onHistoryClick = { navController.navigate(AppRoutes.TRANSACTIONS_SCREEN) }
                )
            }
            item {
                FundingAssetListHeader()
            }
            items(uiState.assets) { asset ->
                FundingAssetRow(asset = asset, isVisible = isVisible)
            }
        }
    }
}

// SỬA LỖI: Đã xóa các định nghĩa trùng lặp của ActionButtons và ActionButton ở đây.

@Composable
fun FundingHeader(totalValue: BigDecimal, isVisible: Boolean, onToggleVisibility: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Giá trị vốn chủ sở hữu",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            IconButton(onClick = onToggleVisibility, modifier = Modifier.size(24.dp)) {
                Icon(
                    imageVector = if (isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = "Ẩn/hiện giá trị",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
        Text(
            text = if (isVisible) formatCurrency(totalValue.toDouble()) else "*****",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun FundingAssetListHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Tên", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        Text("Số lượng", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
    }
}

@Composable
fun FundingAssetRow(asset: FundingAsset, isVisible: Boolean) {
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
fun FundingScreenPreview() {
    CryptoWalletTheme {
        FundingScreen(navController = rememberNavController())
    }
}
