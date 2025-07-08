package com.example.cryptowallet.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.cryptowallet.ui.theme.CryptoWalletTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeEmailScreen(navController: NavController) {
    var newEmail by remember { mutableStateOf("") }
    var newEmailCode by remember { mutableStateOf("") }
    var currentEmailCode by remember { mutableStateOf("") }
    var phoneCode by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thay đổi địa chỉ email") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = { /* TODO: Xử lý xác nhận */ },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = newEmail.isNotBlank() && newEmailCode.isNotBlank() && currentEmailCode.isNotBlank() && phoneCode.isNotBlank()
                ) {
                    Text("Xác nhận")
                }
                TextButton(onClick = { /*TODO*/ }) {
                    Text("Không thể xác minh?")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hộp cảnh báo
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Để bảo vệ tài khoản, bạn sẽ không thể rút tiền hoặc sử dụng giao dịch P2P để bán tiền mã hóa trong 24 giờ sau khi bạn đặt lại hoặc thay đổi email tài khoản của mình.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Các ô nhập liệu
            OutlinedTextField(value = newEmail, onValueChange = { newEmail = it }, label = { Text("Địa chỉ email mới") }, modifier = Modifier.fillMaxWidth())
            VerificationCodeInput(value = newEmailCode, onValueChange = { newEmailCode = it }, label = "Xác minh địa chỉ email mới")
            VerificationCodeInput(value = currentEmailCode, onValueChange = { currentEmailCode = it }, label = "Xác minh địa chỉ email hiện tại")
            VerificationCodeInput(value = phoneCode, onValueChange = { phoneCode = it }, label = "Xác thực qua điện thoại")
        }
    }
}

@Composable
fun VerificationCodeInput(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        trailingIcon = {
            TextButton(onClick = { /* TODO: Xử lý gửi mã */ }) {
                Text("Gửi mã")
            }
        }
    )
}
