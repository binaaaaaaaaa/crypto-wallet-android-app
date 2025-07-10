package com.example.cryptowallet.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
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
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.cryptowallet.navigation.AppRoutes
import com.example.cryptowallet.ui.theme.CryptoWalletTheme
import com.example.cryptowallet.ui.viewmodel.ProfileViewModel
import com.example.cryptowallet.ui.viewmodel.UserProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Hồ sơ", "Bảo mật")

    if (uiState.isCountrySelectionDialogVisible) {
        CountrySelectionDialog(
            onCountrySelected = { viewModel.onCountrySelected(it) },
            onDismiss = { viewModel.hideCountryDialog() }
        )
    }

    if (uiState.isPasswordWarningDialogVisible) {
        SecurityWarningDialog(
            title = "Thông báo bảo mật",
            text = "Để bảo vệ tài sản của bạn, việc thay đổi mật khẩu có nghĩa là trong vòng 24 giờ, bạn sẽ không thể rút hoặc bán tiền mã hóa trên thị trường P2P.",
            onDismiss = { viewModel.hidePasswordWarningDialog() },
            onConfirm = {
                viewModel.hidePasswordWarningDialog()
            }
        )
    }

    if (uiState.isSeedPhraseWarningDialogVisible) {
        SecurityWarningDialog(
            title = "Cảnh báo bảo mật",
            text = "Bạn sắp xem cụm từ khôi phục. Hãy đảm bảo không có ai đang nhìn vào màn hình của bạn.",
            onDismiss = { viewModel.hideSeedPhraseWarningDialog() },
            onConfirm = {
                viewModel.hideSeedPhraseWarningDialog()
                navController.navigate(AppRoutes.SEED_PHRASE_SCREEN)
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trung tâm người dùng") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                uiState.userProfile?.let { profile ->
                    ProfileHeader(
                        name = profile.name,
                        email = profile.email,
                        avatarUrl = profile.avatarUrl
                    )
                }
            }

            item {
                TabRow(selectedTabIndex = selectedTabIndex) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title) }
                        )
                    }
                }
            }

            if (selectedTabIndex == 0) {
                item {
                    uiState.userProfile?.let { profile ->
                        ProfileInfoSection(
                            profile = profile,
                            onCountryClick = { viewModel.showCountryDialog() },
                            onLogoutClick = onLogout
                        )
                    }
                }
            } else {
                item {
                    SecuritySection(
                        onPasswordClick = { viewModel.showPasswordWarningDialog() },
                        onEmailClick = {
                            val email = uiState.userProfile?.email ?: "no-email"
                            navController.navigate("${AppRoutes.VERIFY_EMAIL_SCREEN}/$email")
                        },
                        onSeedPhraseClick = { viewModel.showSeedPhraseWarningDialog() }
                    )
                }
            }
        }
    }
}

@Composable
fun CountrySelectionDialog(
    onCountrySelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val countries = listOf("Việt Nam", "United States", "Singapore", "Japan", "Korea")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 400.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            LazyColumn(modifier = Modifier.padding(16.dp)) {
                item {
                    Text("Chọn quốc gia/khu vực", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 8.dp))
                }
                items(countries) { country ->
                    Text(
                        text = country,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCountrySelected(country) }
                            .padding(vertical = 12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SecurityWarningDialog(
    title: String,
    text: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Warning, contentDescription = "Cảnh báo") },
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Tiếp tục")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}

@Composable
fun ProfileHeader(name: String, email: String, avatarUrl: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = avatarUrl,
            contentDescription = "Avatar",
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(email, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        }
        OutlinedButton(onClick = { /*TODO*/ }) {
            Text("Điều chỉnh")
        }
    }
}

@Composable
fun ProfileInfoSection(
    profile: UserProfile,
    onCountryClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        InfoRow(title = "Xác minh danh tính", value = profile.verificationStatus)
        InfoRow(title = "Quốc gia/Khu vực", value = profile.country, onClick = onCountryClick)
        InfoRow(title = "Bậc phí giao dịch", value = profile.feeLevel)
        InfoRow(title = "UID", value = profile.uid, showCopyIcon = true)
        InfoRow(title = "Tài khoản đã kết nối", value = "")
        InfoRow(title = "Kết nối ví", value = "")
        InfoRow(title = "Chuyển tài khoản", value = "")
        Divider(modifier = Modifier.padding(vertical = 8.dp))
        InfoRow(title = "Đăng xuất", value = "", onClick = onLogoutClick)
    }
}

@Composable
fun SecuritySection(
    onPasswordClick: () -> Unit,
    onEmailClick: () -> Unit,
    onSeedPhraseClick: () -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        InfoRow(title = "Cụm từ khôi phục (Seed Phrase)", value = "", onClick = onSeedPhraseClick)
        InfoRow(title = "Mật khẩu đăng nhập", value = "********", onClick = onPasswordClick)
        InfoRow(title = "Email", value = "Đã liên kết", onClick = onEmailClick)
        InfoRow(title = "Xác thực hai yếu tố (2FA)", value = "Đang bật")
    }
}

@Composable
fun InfoRow(
    title: String,
    value: String,
    showCopyIcon: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null, onClick = { onClick?.invoke() })
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
        if (showCopyIcon) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.ContentCopy, contentDescription = "Sao chép", tint = Color.Gray, modifier = Modifier.size(18.dp))
        } else {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
        }
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ProfileScreenPreview() {
    CryptoWalletTheme {
        ProfileScreen(navController = rememberNavController(), onLogout = {})
    }
}
