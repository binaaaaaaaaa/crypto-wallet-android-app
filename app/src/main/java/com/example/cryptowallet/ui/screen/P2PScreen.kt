package com.example.cryptowallet.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.cryptowallet.ui.theme.CryptoWalletTheme
import java.text.DecimalFormat
import androidx.compose.ui.platform.LocalContext

// Data class để biểu diễn một lời chào bán/mua P2P
data class P2POffer(
    val merchantName: String,
    val totalTrades: Int,
    val completionRate: Double,
    val price: Double,
    val availableAmount: Double,
    val minLimit: Double,
    val maxLimit: Double,
    val paymentMethods: List<String>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun P2PScreen(navController: NavController) {
    val buyOffers = listOf(
        P2POffer("NhàĐạiThắng", 2468, 99.95, 26302.0, 1731.13, 5000000.0, 15000000.0, listOf("Chuyển khoản ngân hàng")),
        P2POffer("tăngVNDP2Pgiá", 5227, 99.52, 26303.0, 2582.54, 4000000.0, 15000000.0, listOf("Chuyển khoản ngân hàng")),
        P2POffer("ThươngMạiUyTín", 8405, 99.86, 26304.0, 3688.64, 5000000.0, 10000000.0, listOf("Techcombank")),
        P2POffer("Audition GD247", 2105, 99.81, 26305.0, 1200.0, 1000000.0, 5000000.0, listOf("Momo", "Viettel Pay"))
    )

    val sellOffers = listOf(
        P2POffer("HảiPhòngP2PPro", 732, 97.47, 26368.0, 98.0, 200000.0, 2584064.0, listOf("Chuyển khoản ngân hàng")),
        P2POffer("trade247", 116665, 99.09, 26365.0, 19600.0, 10000000.0, 516754000.0, listOf("Chuyển khoản ngân hàng")),
        P2POffer("PhucSang_OTC247", 11443, 99.44, 26365.0, 5937.29, 50000000.0, 156536650.0, listOf("Chuyển khoản ngân hàng")),
        P2POffer("XácNhậnNhanh", 2073, 99.74, 26364.0, 4500.0, 20000000.0, 118638000.0, listOf("Momo"))
    )

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Mua", "Bán")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Giao dịch P2P") }
                // SỬA LỖI: Đã xóa navigationIcon (nút quay lại) ở đây
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            val currentOffers = if (selectedTabIndex == 0) buyOffers else sellOffers

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(currentOffers) { offer ->
                    OfferRow(offer = offer, type = tabs[selectedTabIndex])
                }
            }
        }
    }
}

@Composable
fun OfferRow(offer: P2POffer, type: String) {
    val priceFormatter = DecimalFormat("#,###")
    val amountFormatter = DecimalFormat("#,##0.00")

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        offer.merchantName.first().toString(),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(offer.merchantName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${offer.totalTrades} giao dịch",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Divider(
                    modifier = Modifier
                        .height(12.dp)
                        .width(1.dp)
                        .padding(horizontal = 8.dp)
                )
                Text(
                    "Hoàn tất ${offer.completionRate}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "đ ${priceFormatter.format(offer.price)}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (type == "Mua") Color(0xFF4CAF50) else Color.Red
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoRow(label = "Khả dụng", value = "${amountFormatter.format(offer.availableAmount)} USDT")
                    InfoRow(label = "Giới hạn lệnh", value = "đ ${priceFormatter.format(offer.minLimit)} - đ ${priceFormatter.format(offer.maxLimit)}")
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        offer.paymentMethods.forEach {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(it, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    }
                }
                Button(
                    onClick = { /* TODO */ },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (type == "Mua") Color(0xFF4CAF50) else Color.Red
                    )
                ) {
                    Text(type)
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row {
        Text(
            "$label: ",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            modifier = Modifier.width(80.dp)
        )
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
    }
}

@Preview(showBackground = true)
@Composable
fun P2PScreenPreview() {
    CryptoWalletTheme(darkTheme = true) {
        P2PScreen(navController = NavController(LocalContext.current))
    }
}
