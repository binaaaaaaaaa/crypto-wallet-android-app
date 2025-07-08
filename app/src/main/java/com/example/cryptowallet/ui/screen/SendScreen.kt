package com.example.cryptowallet.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.cryptowallet.ui.theme.CryptoWalletTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendScreen(navController: NavController) {
    var address by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gửi Crypto") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại"
                        )
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Tạo giao dịch mới",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Địa chỉ người nhận") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Số lượng") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            // TODO: Thêm dropdown để chọn loại coin

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    // Trong thực tế, bạn sẽ xử lý logic gửi ở đây.
                    // Hiện tại, chúng ta chỉ hiển thị một thông báo.
                    Toast.makeText(
                        context,
                        "Đang xử lý gửi $amount đến $address",
                        Toast.LENGTH_LONG
                    ).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = address.isNotBlank() && amount.isNotBlank()
            ) {
                Text("Gửi")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SendScreenPreview() {
    CryptoWalletTheme {
        // Sử dụng NavController từ LocalContext cho Preview
        SendScreen(navController = NavController(LocalContext.current))
    }
}
