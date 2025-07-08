package com.example.cryptowallet.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.cryptowallet.data.manager.AccountType
import com.example.cryptowallet.data.manager.BalanceManager
import com.example.cryptowallet.model.Transaction
import com.example.cryptowallet.model.TransactionType
import com.example.cryptowallet.navigation.AppRoutes
import com.example.cryptowallet.ui.viewmodel.AssetDetail
import com.example.cryptowallet.ui.viewmodel.AssetDetailViewModel
import com.example.cryptowallet.ui.viewmodel.ChartRange
import com.example.cryptowallet.utils.formatCurrency
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.component.shape.shader.fromBrush
import com.patrykandpatrick.vico.core.component.shape.shader.DynamicShaders
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import kotlinx.coroutines.launch
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun AssetDetailScreen(
    navController: NavController,
    assetId: String
) {
    val viewModel: AssetDetailViewModel = viewModel(factory = AssetDetailViewModel.Factory(assetId))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isVisible by remember { derivedStateOf { BalanceManager.isBalanceVisible } }
    val scope = rememberCoroutineScope()

    val depositSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden, skipHalfExpanded = true)
    val withdrawSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden, skipHalfExpanded = true)

    val chartModelProducer = remember(uiState.chartEntries) {
        ChartEntryModelProducer(uiState.chartEntries)
    }

    LaunchedEffect(uiState.isDepositSheetVisible) {
        if (uiState.isDepositSheetVisible) scope.launch { depositSheetState.show() }
        else scope.launch { depositSheetState.hide() }
    }
    LaunchedEffect(depositSheetState.isVisible) {
        if (!depositSheetState.isVisible) viewModel.hideDepositSheet()
    }

    LaunchedEffect(uiState.isWithdrawSheetVisible) {
        if (uiState.isWithdrawSheetVisible) scope.launch { withdrawSheetState.show() }
        else scope.launch { withdrawSheetState.hide() }
    }
    LaunchedEffect(withdrawSheetState.isVisible) {
        if (!withdrawSheetState.isVisible) viewModel.hideWithdrawSheet()
    }

    ModalBottomSheetLayout(
        sheetState = withdrawSheetState,
        sheetShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        sheetContent = {
            WithdrawOptionsSheetContent(
                onWithdrawCryptoClick = {
                    scope.launch { withdrawSheetState.hide() }.invokeOnCompletion {
                        navController.navigate(AppRoutes.SELECT_WITHDRAW_TOKEN_SCREEN)
                    }
                },
                onP2PClick = {
                    scope.launch { withdrawSheetState.hide() }.invokeOnCompletion {
                        navController.navigate(AppRoutes.P2P_SCREEN)
                    }
                }
            )
        }
    ) {
        ModalBottomSheetLayout(
            sheetState = depositSheetState,
            sheetShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            sheetContent = {
                DepositOptionsSheetContent(
                    onDepositCryptoClick = {
                        scope.launch { depositSheetState.hide() }.invokeOnCompletion {
                            navController.navigate(AppRoutes.SELECT_TOKEN_SCREEN)
                        }
                    },
                    onP2PClick = {
                        scope.launch { depositSheetState.hide() }.invokeOnCompletion {
                            navController.navigate(AppRoutes.P2P_SCREEN)
                        }
                    }
                )
            }
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(uiState.assetDetail?.symbol ?: "Chi tiết") },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Quay lại")
                            }
                        }
                    )
                }
            ) { padding ->
                val asset = uiState.assetDetail

                LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                    if (uiState.isLoading && asset == null) {
                        item {
                            Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                    } else if (asset != null) {
                        item { AssetDetailHeader(asset = asset, isVisible = isVisible) }

                        item {
                            if (uiState.chartEntries.isNotEmpty()) {
                                Chart(
                                    chart = lineChart(
                                        lines = listOf(
                                            com.patrykandpatrick.vico.compose.chart.line.lineSpec(
                                                lineColor = MaterialTheme.colorScheme.primary,
                                                lineBackgroundShader = DynamicShaders.fromBrush(
                                                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                                        listOf(
                                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                                            MaterialTheme.colorScheme.primary.copy(alpha = 0f)
                                                        )
                                                    )
                                                )
                                            )
                                        )
                                    ),
                                    chartModelProducer = chartModelProducer,
                                    startAxis = rememberStartAxis(),
                                    bottomAxis = rememberBottomAxis(),
                                    modifier = Modifier.height(250.dp).padding(horizontal = 16.dp)
                                )
                            } else {
                                Box(
                                    modifier = Modifier.fillMaxWidth().height(250.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }

                        item { ChartRangeSelector(
                            selectedRange = uiState.selectedChartRange,
                            onRangeSelected = { viewModel.onChartRangeSelected(it) }
                        ) }

                        item { AssetDetailActionButtons(
                            onDepositClick = { viewModel.showDepositSheet() },
                            onWithdrawClick = { viewModel.showWithdrawSheet() },
                            onTransferClick = { navController.navigate(AppRoutes.TRANSFER_SCREEN) },
                            onConvertClick = { navController.navigate(AppRoutes.CONVERT_SCREEN) }
                        ) }

                        item { SpotPerformanceCard() }
                        item { PnlInfoSection(asset = asset, isVisible = isVisible) }
                        item { AssetAllocationSection(
                            asset = asset,
                            fundingBalance = uiState.fundingBalance,
                            tradingBalance = uiState.tradingBalance,
                            isVisible = isVisible
                        )}
                        item { RecentTransactionsHeader() }

                        items(uiState.recentTransactions) { transaction ->
                            TransactionRow(transaction = transaction, isVisible = isVisible)
                        }

                    } else if (uiState.error != null) {
                        item {
                            Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Lỗi: ${uiState.error}")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AssetDetailHeader(asset: AssetDetail, isVisible: Boolean) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            "Tổng giá trị ước tính",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (isVisible) formatCurrency(asset.getValueInUsd().toDouble()) else "*****",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (isVisible) "${asset.balance.toPlainString()} ${asset.symbol}" else "*** ${asset.symbol}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun ChartRangeSelector(
    selectedRange: ChartRange,
    onRangeSelected: (ChartRange) -> Unit
) {
    TabRow(
        selectedTabIndex = selectedRange.ordinal,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        ChartRange.values().forEach { range ->
            Tab(
                selected = selectedRange == range,
                onClick = { onRangeSelected(range) },
                text = { Text(when(range) {
                    ChartRange.D1 -> "1 Ngày"
                    ChartRange.W1 -> "1 Tuần"
                    ChartRange.M1 -> "1 Tháng"
                    ChartRange.M6 -> "6 Tháng"
                }) }
            )
        }
    }
}

@Composable
fun AssetDetailActionButtons(
    onDepositClick: () -> Unit,
    onWithdrawClick: () -> Unit,
    onTransferClick: () -> Unit,
    onConvertClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        ActionButton(text = "Nạp", icon = Icons.Default.ArrowDownward, onClick = onDepositClick)
        ActionButton(text = "Rút", icon = Icons.Default.ArrowUpward, onClick = onWithdrawClick)
        ActionButton(text = "Chuyển tiền", icon = Icons.Default.SwapHoriz, onClick = onTransferClick)
        ActionButton(text = "Chuyển đổi", icon = Icons.Default.CurrencyExchange, onClick = onConvertClick)
    }
}

@Composable
fun SpotPerformanceCard() {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Hiệu suất giao dịch spot", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.Info, contentDescription = "Info", modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Dữ liệu hiệu suất của giao dịch spot chỉ tính các tài sản phát sinh từ giao dịch spot, mua/bán, chuyển đổi và các hoạt động liên quan khác.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun PnlInfoSection(asset: AssetDetail, isVisible: Boolean) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("PNL", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Giá chi phí", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Text(if (isVisible) formatCurrency(asset.currentPrice.toDouble() * 0.98) else "--", style = MaterialTheme.typography.bodyLarge)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Giá gần nhất", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Text(if (isVisible) formatCurrency(asset.currentPrice.toDouble()) else "--", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
fun AssetAllocationSection(
    asset: AssetDetail,
    fundingBalance: BigDecimal,
    tradingBalance: BigDecimal,
    isVisible: Boolean
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Phân bổ", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        if (fundingBalance > BigDecimal.ZERO) {
            AllocationRow(
                label = "Funding",
                // SỬA LỖI: Sử dụng `asset.currentPrice` thay vì `asset.priceInUsd`
                amount = (fundingBalance * asset.currentPrice).toDouble(),
                color = Color.Blue,
                isBalanceVisible = isVisible,
                onClick = { /* TODO */ }
            )
        }

        if (tradingBalance > BigDecimal.ZERO) {
            AllocationRow(
                label = "Giao dịch",
                // SỬA LỖI: Sử dụng `asset.currentPrice` thay vì `asset.priceInUsd`
                amount = (tradingBalance * asset.currentPrice).toDouble(),
                color = Color(0xFFFFA500),
                isBalanceVisible = isVisible,
                onClick = { /* TODO */ }
            )
        }
    }
}

@Composable
fun RecentTransactionsHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { /* TODO */ },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Giao dịch gần đây", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Xem thêm")
    }
}

@Composable
fun TransactionRow(transaction: Transaction, isVisible: Boolean) {
    val (icon, color) = when (transaction.type) {
        TransactionType.SENT -> Icons.Default.ArrowUpward to Color(0xFFE57373)
        TransactionType.RECEIVED -> Icons.Default.ArrowDownward to Color(0xFF81C784)
    }
    val sign = if (transaction.type == TransactionType.SENT) "-" else "+"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = transaction.type.name,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (transaction.type == TransactionType.SENT) "Đã gửi" else "Đã nhận",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = formatDate(transaction.date),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
        Text(
            text = if (isVisible) "$sign${transaction.amount} ${transaction.assetSymbol}" else "*****",
            fontWeight = FontWeight.Bold,
            color = color,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

private fun formatDate(date: Date): String {
    val pattern = "dd MMM, yyyy"
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    return sdf.format(date)
}
