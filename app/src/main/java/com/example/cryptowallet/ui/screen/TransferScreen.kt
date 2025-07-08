package com.example.cryptowallet.ui.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.cryptowallet.ui.theme.CryptoWalletTheme
import com.example.cryptowallet.ui.viewmodel.AccountAsset
import com.example.cryptowallet.ui.viewmodel.TransferViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TransferScreen(
    navController: NavController,
    viewModel: TransferViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )

    // Đồng bộ trạng thái hiển thị của bottom sheet với ViewModel
    LaunchedEffect(uiState.isAssetSheetVisible) {
        if (uiState.isAssetSheetVisible) {
            scope.launch { sheetState.show() }
        } else {
            scope.launch { sheetState.hide() }
        }
    }

    // Lắng nghe khi người dùng kéo đóng sheet
    LaunchedEffect(sheetState.isVisible) {
        if (!sheetState.isVisible) {
            viewModel.hideAssetSheet()
        }
    }

    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        sheetContent = {
            AssetSelectionSheet(
                assets = uiState.availableAssets,
                onAssetSelected = { viewModel.onSelectAsset(it) }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Chuyển tiền") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                        }
                    }
                )
            },
            bottomBar = {
                Button(
                    onClick = {
                        viewModel.confirmTransfer(context) {
                            // Khi chuyển tiền thành công, quay lại màn hình trước
                            navController.popBackStack()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(50.dp),
                    enabled = uiState.amount.isNotBlank() && uiState.selectedAsset != null
                ) {
                    Text("Xác nhận")
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Column {
                        AccountCard(label = "Từ", account = uiState.fromAccount)
                        Spacer(modifier = Modifier.height(8.dp))
                        AccountCard(label = "Đến", account = uiState.toAccount)
                    }
                    IconButton(
                        onClick = { viewModel.onSwapAccounts() },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(Icons.Default.SwapVert, contentDescription = "Hoán đổi")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("Tài sản", modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.showAssetSheet() }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Hiển thị icon và tên của tài sản đã chọn
                        uiState.selectedAsset?.let {
                            AsyncImage(
                                model = it.asset.iconUrl,
                                contentDescription = it.asset.name,
                                modifier = Modifier.size(24.dp).clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(it.asset.symbol, modifier = Modifier.weight(1f))
                        } ?: Text("Chọn tài sản", modifier = Modifier.weight(1f))

                        Icon(Icons.Default.UnfoldMore, contentDescription = "Chọn tài sản")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Số tiền", modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = uiState.amount,
                    onValueChange = { viewModel.onAmountChange(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Nhập số lượng") },
                    trailingIcon = {
                        TextButton(onClick = { viewModel.setMaxAmount() }) {
                            Text("Tối đa")
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Hiển thị số dư khả dụng
                val availableBalance = if (uiState.fromAccount == "Tài khoản Funding") {
                    uiState.selectedAsset?.fundingBalance
                } else {
                    uiState.selectedAsset?.tradingBalance
                }
                Text(
                    "Số dư khả dụng: ${availableBalance?.toPlainString() ?: "0"} ${uiState.selectedAsset?.asset?.symbol ?: ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun AssetSelectionSheet(
    assets: List<AccountAsset>,
    onAssetSelected: (AccountAsset) -> Unit
) {
    LazyColumn(contentPadding = PaddingValues(16.dp)) {
        item {
            Text("Chọn tài sản", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 8.dp))
        }
        items(assets) { accountAsset ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAssetSelected(accountAsset) }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = accountAsset.asset.iconUrl,
                    contentDescription = accountAsset.asset.name,
                    modifier = Modifier.size(40.dp).clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(accountAsset.asset.symbol, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AccountCard(label: String, account: String) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Text(account, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun TransferScreenPreview() {
    CryptoWalletTheme {
        TransferScreen(navController = NavController(LocalContext.current))
    }
}
