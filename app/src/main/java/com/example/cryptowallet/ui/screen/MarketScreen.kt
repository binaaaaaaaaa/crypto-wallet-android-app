package com.example.cryptowallet.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.cryptowallet.navigation.AppRoutes
import com.example.cryptowallet.ui.viewmodel.MarketCategory
import com.example.cryptowallet.ui.viewmodel.MarketCoin
import com.example.cryptowallet.ui.viewmodel.MarketViewModel
import com.example.cryptowallet.utils.formatCurrency
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketScreen(
    navController: NavController,
    viewModel: MarketViewModel = viewModel(factory = MarketViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Thị trường") })
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Thanh tìm kiếm
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                label = { Text("Tìm kiếm") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Xóa")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(50)
            )

            // Các tab phân loại
            TabRow(selectedTabIndex = uiState.selectedCategory.ordinal) {
                MarketCategory.values().forEach { category ->
                    Tab(
                        selected = uiState.selectedCategory == category,
                        onClick = { viewModel.onCategorySelected(category) },
                        text = { Text(when(category) {
                            MarketCategory.ALL -> "Tất cả"
                            MarketCategory.GAINERS -> "Tăng giá"
                            MarketCategory.LOSERS -> "Giảm giá"
                        }) }
                    )
                }
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
                    items(uiState.displayedCoins, key = { it.id }) { coin ->
                        MarketCoinRow(
                            coin = coin,
                            onClick = {
                                // Điều hướng đến màn hình chi tiết
                                navController.navigate("${AppRoutes.ASSET_DETAIL_SCREEN}/${coin.id}")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MarketCoinRow(coin: MarketCoin, onClick: () -> Unit) {
    val priceChangeColor = if (coin.priceChangePercentage24h >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
    val chartModelProducer = remember(coin.id) { ChartEntryModelProducer(coin.chartEntries) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = coin.iconUrl,
            contentDescription = coin.name,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(coin.symbol, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            Text(coin.name, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }

        // Biểu đồ Sparkline
        if (coin.chartEntries.isNotEmpty()) {
            Chart(
                chart = lineChart(lines = listOf(com.patrykandpatrick.vico.compose.chart.line.lineSpec(lineColor = priceChangeColor))),
                chartModelProducer = chartModelProducer,
                modifier = Modifier.width(80.dp).height(40.dp),
                startAxis = null, bottomAxis = null, endAxis = null
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = formatCurrency(coin.price.toDouble()),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = String.format(Locale.US, "%.2f%%", coin.priceChangePercentage24h),
                color = priceChangeColor,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
