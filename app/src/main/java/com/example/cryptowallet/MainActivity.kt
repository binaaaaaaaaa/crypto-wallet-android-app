package com.example.cryptowallet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import com.example.cryptowallet.navigation.AppNavigation
import com.example.cryptowallet.ui.theme.CryptoWalletTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CryptoWalletApp()
        }
    }
}

@Composable
fun CryptoWalletApp() {
    var isDarkTheme by remember { mutableStateOf(false) }
    val systemTheme = isSystemInDarkTheme()

    LaunchedEffect(systemTheme) {
        isDarkTheme = systemTheme
    }


    CryptoWalletTheme(darkTheme = isDarkTheme) {
        AppNavigation(
            isDarkTheme = isDarkTheme,
            onThemeToggle = { isDarkTheme = !isDarkTheme }
        )
    }
}
