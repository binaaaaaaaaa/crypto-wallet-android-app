package com.example.cryptowallet.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Business
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.cryptowallet.navigation.AppRoutes
import com.example.cryptowallet.ui.theme.CryptoWalletTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectDestinationScreen(
    navController: NavController,
    assetSymbol: String?
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chọn đích đến") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Đích đến đã lưu", style = MaterialTheme.typography.titleMedium)
            DestinationOptionCard(
                icon = Icons.Default.Book,
                title = "Sổ địa chỉ",
                description = "Địa chỉ on-chain và người nhận mà bạn đã lưu",
                onClick = {
                    // Khi chọn, điều hướng đến màn hình Rút tiền cuối cùng
                    navController.navigate(AppRoutes.SEND_SCREEN)
                }
            )

            Text("Đích đến mới", style = MaterialTheme.typography.titleMedium)
            DestinationOptionCard(
                icon = Icons.Default.Business,
                title = "Sàn giao dịch hoặc ví",
                description = "Rút tiền on-chain sang các sàn giao dịch hoặc ví on-chain khác",
                onClick = {
                    navController.navigate(AppRoutes.SEND_SCREEN)
                }
            )
        }
    }
}

@Composable
fun DestinationOptionCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SelectDestinationScreenPreview() {
    CryptoWalletTheme {
        SelectDestinationScreen(navController = rememberNavController(), assetSymbol = "USDT")
    }
}
