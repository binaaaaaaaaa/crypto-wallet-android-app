package com.example.cryptowallet.ui.screen.auth

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.cryptowallet.R
import com.example.cryptowallet.navigation.AppRoutes
import com.example.cryptowallet.ui.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider

@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val uiState by authViewModel.authState.collectAsState()
    val context = LocalContext.current

    val webClientId = "YOUR_DEFAULT_WEB_CLIENT_ID"

    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    val googleAuthLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                val credential = GoogleAuthProvider.getCredential(account.idToken!!, null)
                authViewModel.signInWithCredential(credential)
            } catch (e: ApiException) {
                Log.w("LoginScreen", "Google sign in failed", e)
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(64.dp))
        Box(modifier = Modifier.size(80.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape))

        Spacer(modifier = Modifier.height(16.dp))
        Text("CryptoWallet", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Ví crypto an toàn và hiệu quả", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(48.dp))

        Text("Đăng nhập", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.email,
            onValueChange = { authViewModel.onEmailChange(it) },
            label = { Text("Email của bạn") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            isError = uiState.emailError != null,
            supportingText = {
                if (uiState.emailError != null) {
                    Text(uiState.emailError!!, color = MaterialTheme.colorScheme.error)
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.password,
            onValueChange = { authViewModel.onPasswordChange(it) },
            label = { Text("Mật khẩu") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (uiState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            trailingIcon = {
                val image = if (uiState.isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { authViewModel.togglePasswordVisibility() }) {
                    Icon(imageVector = image, contentDescription = "Toggle password visibility")
                }
            }
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = { /* TODO: Forgot Password */ }) {
                Text("Quên mật khẩu?")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.authError != null) {
            Text(
                text = uiState.authError!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(
            onClick = { authViewModel.login() },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Đăng nhập")
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("HOẶC", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SocialLoginButton(
                painter = painterResource(id = R.drawable.ic_google),
                onClick = {
                    val signInIntent = googleSignInClient.signInIntent
                    googleAuthLauncher.launch(signInIntent)
                }
            )
            Spacer(modifier = Modifier.width(24.dp))
            SocialLoginButton(
                painter = painterResource(id = R.drawable.ic_facebook),
                onClick = { /* TODO: Facebook Login */ }
            )
            Spacer(modifier = Modifier.width(24.dp))
            SocialLoginButton(
                painter = painterResource(id = R.drawable.ic_twitter),
                onClick = { /* TODO: Twitter/X Login */ }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Chưa có tài khoản?")
            TextButton(onClick = { navController.navigate(AppRoutes.REGISTER_SCREEN) }) {
                Text("Đăng ký")
            }
        }
    }
}

@Composable
fun SocialLoginButton(painter: Painter, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(50.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painter,
            contentDescription = "Social Login",
            modifier = Modifier.size(24.dp)
        )
    }
}