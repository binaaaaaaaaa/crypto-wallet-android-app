package com.example.cryptowallet.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.cryptowallet.navigation.AppRoutes
import com.example.cryptowallet.ui.theme.CryptoWalletTheme
import androidx.compose.ui.platform.LocalContext

// Data class để biểu diễn thông tin một mạng lưới
data class NetworkInfo(
    val name: String,
    val fullName: String,
    val iconUrl: String,
    val fee: String,
    val time: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectNetworkScreen(
    navController: NavController,
    assetSymbol: String?
) {
    // Dữ liệu giả lập các mạng phổ biến
    val networks = listOf(
        NetworkInfo("TRC20", "Tron (TRC20)", "https://s2.coinmarketcap.com/static/img/coins/64x64/1958.png", "0.01 USDT", "~1 phút"),
        NetworkInfo("ERC20", "Ethereum (ERC20)", "https://s2.coinmarketcap.com/static/img/coins/64x64/1027.png", "0.01 USDT", "~7 phút"),
        NetworkInfo("BEP20", "BNB Smart Chain (BEP20)", "https://s2.coinmarketcap.com/static/img/coins/64x64/1839.png", "0.01 USDT", "~1 phút"),
        NetworkInfo("Polygon", "Polygon", "https://s2.coinmarketcap.com/static/img/coins/64x64/3890.png", "0.01 USDT", "~3 phút")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chọn mạng") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(networks) { network ->
                NetworkRow(network = network) {
                    // Khi chọn một mạng, điều hướng đến màn hình Nhận
                    // và truyền cả ký hiệu token và tên mạng
                    navController.navigate("${AppRoutes.RECEIVE_SCREEN}/$assetSymbol/${network.name}")
                }
            }
        }
    }
}

@Composable
fun NetworkRow(network: NetworkInfo, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = network.iconUrl,
            contentDescription = network.name,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(network.fullName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            Text(
                "Số tiền nạp tối thiểu: ${network.fee}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Text(
                "Ước tính sẽ đến sau: ${network.time}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
fun SelectNetworkScreenPreview() {
    CryptoWalletTheme(darkTheme = true) {
        SelectNetworkScreen(navController = NavController(LocalContext.current), assetSymbol = "USDT")
    }
}
