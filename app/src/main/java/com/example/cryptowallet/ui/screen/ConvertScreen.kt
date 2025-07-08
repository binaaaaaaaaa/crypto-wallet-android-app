package com.example.cryptowallet.ui.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.cryptowallet.model.CryptoAsset
import com.example.cryptowallet.ui.theme.CryptoWalletTheme
import com.example.cryptowallet.ui.viewmodel.ConvertViewModel
import com.example.cryptowallet.ui.viewmodel.SelectionType
import kotlinx.coroutines.launch
import java.math.BigDecimal

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ConvertScreen(
    navController: NavController,
    viewModel: ConvertViewModel = viewModel(factory = ConvertViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )

    LaunchedEffect(uiState.isAssetSheetVisible) {
        if (uiState.isAssetSheetVisible) scope.launch { sheetState.show() }
        else scope.launch { sheetState.hide() }
    }

    LaunchedEffect(sheetState.isVisible) {
        if (!sheetState.isVisible) viewModel.hideAssetSheet()
    }

    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        sheetContent = {
            ConvertAssetSelectionSheet(
                assets = uiState.availableAssets,
                onAssetSelected = { viewModel.selectAsset(it) }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Chuyển đổi") },
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
                        Toast.makeText(context, "Xem trước báo giá...", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(50.dp),
                    enabled = uiState.fromAmount.isNotBlank()
                ) {
                    Text("Xem trước báo giá")
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
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ConvertCard(
                            label = "Từ",
                            asset = uiState.fromAsset,
                            amount = uiState.fromAmount,
                            onAmountChange = { viewModel.onFromAmountChange(it) },
                            onAssetClick = { viewModel.showAssetSheet(SelectionType.FROM) },
                            onMaxClick = { viewModel.setMaxAmount() },
                            availableBalance = uiState.fromAsset?.balance
                        )
                        ConvertCard(
                            label = "Đến",
                            asset = uiState.toAsset,
                            amount = uiState.toAmount,
                            isInputEnabled = false,
                            onAmountChange = {},
                            onAssetClick = { viewModel.showAssetSheet(SelectionType.TO) },
                            onMaxClick = {}
                        )
                    }
                    IconButton(
                        onClick = { viewModel.swapAssets() },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(Icons.Default.SwapVert, contentDescription = "Hoán đổi")
                    }
                }
            }
        }
    }
}

@Composable
fun ConvertCard(
    label: String,
    asset: CryptoAsset?,
    amount: String,
    onAmountChange: (String) -> Unit,
    onAssetClick: () -> Unit,
    onMaxClick: () -> Unit,
    isInputEnabled: Boolean = true,
    availableBalance: BigDecimal? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                availableBalance?.let {
                    Text(
                        "Khả dụng: ${it.toPlainString()} ${asset?.symbol ?: ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable(onClick = onAssetClick)
                        .padding(vertical = 4.dp, horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = asset?.iconUrl,
                        contentDescription = asset?.name,
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(asset?.symbol ?: "Chọn", fontWeight = FontWeight.Bold)
                    Icon(Icons.Default.UnfoldMore, contentDescription = null)
                }

                BasicTextField(
                    value = amount,
                    onValueChange = onAmountChange,
                    enabled = isInputEnabled,
                    textStyle = LocalTextStyle.current.copy(
                        textAlign = TextAlign.End,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                if (isInputEnabled) {
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = onMaxClick) {
                        Text("Tối đa")
                    }
                }
            }
        }
    }
}

@Composable
fun ConvertAssetSelectionSheet(
    assets: List<CryptoAsset>,
    onAssetSelected: (CryptoAsset) -> Unit
) {
    LazyColumn(contentPadding = PaddingValues(16.dp)) {
        item {
            Text("Chọn tài sản", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 8.dp))
        }
        items(assets, key = { it.id }) { asset ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAssetSelected(asset) }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = asset.iconUrl,
                    contentDescription = asset.name,
                    modifier = Modifier.size(40.dp).clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(asset.symbol, fontWeight = FontWeight.Bold)
            }
        }
    }
}
