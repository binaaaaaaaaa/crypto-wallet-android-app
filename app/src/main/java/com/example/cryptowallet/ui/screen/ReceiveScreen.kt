package com.example.cryptowallet.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.cryptowallet.ui.theme.CryptoWalletTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiveScreen(navController: NavController, assetSymbol: String?, networkName: String?) {
    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val displaySymbol = assetSymbol ?: "USDT"
    val displayNetwork = networkName ?: "Ethereum(ERC20)"
    val walletAddress = "0x5e8c58e1885317a14a425817325f0c6094432c29"
    val qrCodeUrl = "https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=$walletAddress"

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(displaySymbol) },
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
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // SỬA LỖI: Truyền `displaySymbol` vào WarningBox
            WarningBox(assetSymbol = displaySymbol)
            Spacer(modifier = Modifier.height(24.dp))

            // Mã QR
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                AsyncImage(
                    model = qrCodeUrl,
                    contentDescription = "Mã QR địa chỉ ví",
                    modifier = Modifier.size(260.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Mạng: $displayNetwork", style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(24.dp))

            // Địa chỉ và nút sao chép
            AddressCard(
                address = walletAddress,
                onCopyClick = {
                    clipboardManager.setText(AnnotatedString(walletAddress))
                    scope.launch {
                        snackbarHostState.showSnackbar("Đã sao chép địa chỉ!")
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            // Các thông tin chi tiết khác
            DepositDetails()
        }
    }
}

// SỬA LỖI: Thêm tham số `assetSymbol`
@Composable
fun WarningBox(assetSymbol: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFC107).copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.WarningAmber, contentDescription = "Cảnh báo", tint = Color(0xFFFFC107))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Chỉ gửi $assetSymbol đến địa chỉ này. Gửi bất kỳ coin nào khác có thể dẫn đến mất tài sản.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}


@Composable
fun AddressCard(address: String, onCopyClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Địa chỉ nạp", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = address,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    maxLines = 2
                )
                Spacer(modifier = Modifier.width(16.dp))
                Button(onClick = onCopyClick) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Sao chép")
                    Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Sao chép")
                }
            }
        }
    }
}

@Composable
fun DepositDetails() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        DetailRow(label = "Tiền nạp tối thiểu", value = "0.01 USDT")
        DetailRow(label = "Thời gian nạp vào", value = "~7 phút")
        DetailRow(label = "Thời gian cho phép rút", value = "~20 phút")
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Preview(showBackground = true)
@Composable
fun ReceiveScreenRedesignedPreview() {
    CryptoWalletTheme(darkTheme = true) {
        ReceiveScreen(navController = rememberNavController(), assetSymbol = "USDT", networkName = "Ethereum(ERC20)")
    }
}
