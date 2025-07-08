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
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.cryptowallet.data.manager.BalanceManager
import com.example.cryptowallet.model.CryptoAsset
import com.example.cryptowallet.navigation.AppRoutes
import com.example.cryptowallet.ui.theme.CryptoWalletTheme
import com.example.cryptowallet.ui.viewmodel.WalletUiState
import com.example.cryptowallet.ui.viewmodel.WalletViewModel
import com.example.cryptowallet.utils.formatCurrency
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.Locale

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun WalletRoute(
    navController: NavHostController,
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    viewModel: WalletViewModel = viewModel(factory = WalletViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    WalletScreen(
        uiState = uiState,
        onNavigateToTransactions = { navController.navigate(AppRoutes.TRANSACTIONS_SCREEN) },
        onNavigateToSend = { navController.navigate(AppRoutes.SELECT_WITHDRAW_TOKEN_SCREEN) },
        onNavigateToReceive = { navController.navigate(AppRoutes.SELECT_TOKEN_SCREEN) },
        onNavigateToAssetDetail = { assetId -> navController.navigate("${AppRoutes.ASSET_DETAIL_SCREEN}/$assetId") },
        onNavigateToP2P = { navController.navigate(AppRoutes.P2P_SCREEN) },
        onNavigateToTransfer = { navController.navigate(AppRoutes.TRANSFER_SCREEN) },
        onNavigateToFunding = { navController.navigate(AppRoutes.FUNDING_SCREEN) },
        onNavigateToTrading = { navController.navigate(AppRoutes.TRADING_SCREEN) },
        onRefresh = { viewModel.fetchWalletData() },
        isDarkTheme = isDarkTheme,
        onThemeToggle = onThemeToggle,
        onSearchQueryChanged = { query -> viewModel.onSearchQueryChanged(query) },
        onShowDepositSheet = { viewModel.showDepositSheet() },
        onHideDepositSheet = { viewModel.hideDepositSheet() },
        onShowWithdrawSheet = { viewModel.showWithdrawSheet() },
        onHideWithdrawSheet = { viewModel.hideWithdrawSheet() }
    )
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    uiState: WalletUiState,
    onNavigateToTransactions: () -> Unit,
    onNavigateToSend: () -> Unit,
    onNavigateToReceive: () -> Unit,
    onNavigateToAssetDetail: (String) -> Unit,
    onNavigateToP2P: () -> Unit,
    onNavigateToTransfer: () -> Unit,
    onNavigateToFunding: () -> Unit,
    onNavigateToTrading: () -> Unit,
    onRefresh: () -> Unit,
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onShowDepositSheet: () -> Unit,
    onHideDepositSheet: () -> Unit,
    onShowWithdrawSheet: () -> Unit,
    onHideWithdrawSheet: () -> Unit
) {
    val pullRefreshState = rememberPullRefreshState(refreshing = uiState.isLoading, onRefresh = onRefresh)
    val scope = rememberCoroutineScope()

    val depositSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden, skipHalfExpanded = true)
    val withdrawSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden, skipHalfExpanded = true)

    val isBalanceVisible by remember { derivedStateOf { BalanceManager.isBalanceVisible } }

    LaunchedEffect(uiState.isDepositSheetVisible) {
        if (uiState.isDepositSheetVisible) scope.launch { depositSheetState.show() }
        else scope.launch { depositSheetState.hide() }
    }
    LaunchedEffect(depositSheetState.isVisible) {
        if (!depositSheetState.isVisible) onHideDepositSheet()
    }

    LaunchedEffect(uiState.isWithdrawSheetVisible) {
        if (uiState.isWithdrawSheetVisible) scope.launch { withdrawSheetState.show() }
        else scope.launch { withdrawSheetState.hide() }
    }
    LaunchedEffect(withdrawSheetState.isVisible) {
        if (!withdrawSheetState.isVisible) onHideWithdrawSheet()
    }

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

    ModalBottomSheetLayout(
        sheetState = withdrawSheetState,
        sheetShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        sheetContent = {
            WithdrawOptionsSheetContent(
                onWithdrawCryptoClick = {
                    scope.launch { withdrawSheetState.hide() }.invokeOnCompletion { onNavigateToSend() }
                },
                onP2PClick = {
                    scope.launch { withdrawSheetState.hide() }.invokeOnCompletion { onNavigateToP2P() }
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
                        scope.launch { depositSheetState.hide() }.invokeOnCompletion { onNavigateToReceive() }
                    },
                    onP2PClick = {
                        scope.launch { depositSheetState.hide() }.invokeOnCompletion { onNavigateToP2P() }
                    }
                )
            }
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Tài sản") },
                        actions = {
                            IconButton(onClick = onThemeToggle) {
                                Icon(
                                    imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = "Chuyển đổi giao diện"
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                        .pullRefresh(pullRefreshState)
                ) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item { SearchBar(
                            query = uiState.searchQuery,
                            onQueryChange = onSearchQueryChanged,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )}
                        item { TotalBalanceCard(
                            balance = uiState.totalBalance.toDouble(),
                            pnlValue = uiState.pnlValue,
                            pnlPercentage = uiState.pnlPercentage,
                            isPnlPositive = uiState.isPnlPositive,
                            isBalanceVisible = isBalanceVisible,
                            onToggleVisibility = { BalanceManager.toggleVisibility() }
                        ) }
                        item { ActionButtons(
                            onDepositClick = onShowDepositSheet,
                            onWithdrawClick = onShowWithdrawSheet,
                            onTransferClick = onNavigateToTransfer,
                            onHistoryClick = onNavigateToTransactions
                        ) }
                        item {
                            AssetAllocationCard(
                                fundingBalance = uiState.fundingBalance.toDouble(),
                                tradingBalance = uiState.tradingBalance.toDouble(),
                                totalBalance = uiState.totalBalance.toDouble(),
                                isBalanceVisible = isBalanceVisible,
                                onFundingClick = onNavigateToFunding,
                                onTradingClick = onNavigateToTrading
                            )
                        }
                        item { AssetListHeader() }

                        if (filteredAssets.isEmpty() && !uiState.isLoading) {
                            item {
                                Text(
                                    text = "Không tìm thấy tài sản.",
                                    modifier = Modifier.padding(16.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            items(
                                items = filteredAssets,
                                key = { asset -> asset.id }
                            ) { asset ->
                                AssetRow(
                                    asset = asset,
                                    onClick = { onNavigateToAssetDetail(asset.id) },
                                    isBalanceVisible = isBalanceVisible
                                )
                            }
                        }
                    }
                    PullRefreshIndicator(
                        refreshing = uiState.isLoading,
                        state = pullRefreshState,
                        modifier = Modifier.align(Alignment.TopCenter),
                        backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun WithdrawOptionsSheetContent(
    onWithdrawCryptoClick: () -> Unit,
    onP2PClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Text(
            "Chọn phương thức rút tiền",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp, start = 8.dp)
        )
        DepositOptionRow(
            icon = Icons.Default.Upload,
            title = "Rút",
            description = "Chuyển tiền mã hóa đến ví, sàn giao dịch",
            onClick = onWithdrawCryptoClick
        )
        Divider(modifier = Modifier.padding(vertical = 8.dp))
        DepositOptionRow(
            icon = Icons.Default.People,
            title = "Giao dịch P2P",
            description = "Bán tiền mã hóa với phí 0 đồng",
            onClick = onP2PClick
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}


@Composable
fun DepositOptionsSheetContent(
    onDepositCryptoClick: () -> Unit,
    onP2PClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Text(
            "Chọn phương thức nạp tiền",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp, start = 8.dp)
        )
        DepositOptionRow(
            icon = Icons.Default.Download,
            title = "Nạp crypto",
            description = "Chuyển tiền mã hóa từ ví on-chain hoặc sàn giao dịch",
            onClick = onDepositCryptoClick
        )
        Divider(modifier = Modifier.padding(vertical = 8.dp))
        DepositOptionRow(
            icon = Icons.Default.People,
            title = "Giao dịch P2P",
            description = "Mua/bán với phí 0 đồng qua hơn 100 phương thức thanh toán",
            onClick = onP2PClick
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun DepositOptionRow(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            Text(text = description, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.Gray
        )
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text("Tìm kiếm tài sản...") },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = "Search Icon")
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear Search")
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(50)
    )
}

@Composable
fun TotalBalanceCard(
    balance: Double,
    pnlValue: Double,
    pnlPercentage: Double,
    isPnlPositive: Boolean,
    isBalanceVisible: Boolean,
    onToggleVisibility: () -> Unit
) {
    val chartModelProducer = remember {
        ChartEntryModelProducer(
            listOf(
                entryOf(0, 4), entryOf(1, 8), entryOf(2, 6), entryOf(3, 9),
                entryOf(4, 7), entryOf(5, 11), entryOf(6, 10)
            )
        )
    }

    val pnlColor = if (isPnlPositive) Color(0xFF4CAF50) else Color.Red
    val pnlSign = if (isPnlPositive) "+" else ""

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Tổng giá trị ước tính",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                IconButton(onClick = onToggleVisibility, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = if (isBalanceVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Ẩn/hiện tài sản",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isBalanceVisible) formatCurrency(balance) else "*****",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "PNL hôm nay",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (isBalanceVisible) String.format(Locale.US, "%s$%.2f (%.2f%%)", pnlSign, pnlValue, pnlPercentage) else "********",
                    color = pnlColor,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "View PNL details",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
        Chart(
            chart = lineChart(
                lines = listOf(
                    com.patrykandpatrick.vico.compose.chart.line.lineSpec(
                        lineColor = pnlColor,
                        lineThickness = 2.dp
                    )
                )
            ),
            chartModelProducer = chartModelProducer,
            modifier = Modifier
                .width(100.dp)
                .height(50.dp),
            startAxis = null,
            bottomAxis = null,
        )
    }
}

@Composable
fun ActionButtons(
    onDepositClick: () -> Unit,
    onWithdrawClick: () -> Unit,
    onTransferClick: () -> Unit,
    onHistoryClick: () -> Unit
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
        ActionButton(text = "Lịch sử", icon = Icons.Default.History, onClick = onHistoryClick)
    }
}

@Composable
fun ActionButton(text: String, icon: ImageVector, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(8.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = text, tint = MaterialTheme.colorScheme.onSecondaryContainer)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = text, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun AssetAllocationCard(
    fundingBalance: Double,
    tradingBalance: Double,
    totalBalance: Double,
    isBalanceVisible: Boolean,
    onFundingClick: () -> Unit,
    onTradingClick: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text("Phân bổ", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        if (totalBalance > 0) {
            val fundingRatio = (fundingBalance / totalBalance).toFloat()
            val tradingRatio = (tradingBalance / totalBalance).toFloat()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
            ) {
                Box(modifier = Modifier
                    .fillMaxHeight()
                    .weight(fundingRatio)
                    .background(Color.Blue))
                Box(modifier = Modifier
                    .fillMaxHeight()
                    .weight(tradingRatio)
                    .background(Color(0xFFFFA500)))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        AllocationRow(
            label = "Funding",
            amount = fundingBalance,
            color = Color.Blue,
            isBalanceVisible = isBalanceVisible,
            onClick = onFundingClick
        )
        AllocationRow(
            label = "Giao dịch",
            amount = tradingBalance,
            color = Color(0xFFFFA500),
            isBalanceVisible = isBalanceVisible,
            onClick = onTradingClick
        )
    }
}

@Composable
fun AllocationRow(
    label: String,
    amount: Double,
    color: Color,
    isBalanceVisible: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(color))
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Text(
            text = if (isBalanceVisible) formatCurrency(amount) else "***",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.Gray
        )
    }
}


@Composable
fun AssetListHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Tài sản",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Giá trị",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun AssetRow(
    asset: CryptoAsset,
    onClick: () -> Unit,
    isBalanceVisible: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = asset.iconUrl,
            contentDescription = "${asset.name} logo",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = asset.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
            Text(text = asset.symbol, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = if (isBalanceVisible) formatCurrency(asset.getValueInUsd().toDouble()) else "*****",
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = if (isBalanceVisible) "${asset.balance}" else "***",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WalletScreenPreview() {
    // Preview
}
