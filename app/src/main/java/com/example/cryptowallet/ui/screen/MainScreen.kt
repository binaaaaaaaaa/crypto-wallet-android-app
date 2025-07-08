package com.example.cryptowallet.ui.screen.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.cryptowallet.ui.screen.MarketScreen
import com.example.cryptowallet.ui.screen.P2PScreen
import com.example.cryptowallet.ui.screen.ProfileScreen
import com.example.cryptowallet.ui.screen.WalletRoute

// Data class để định nghĩa một mục trên thanh điều hướng
data class BottomNavItem(
    val title: String,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

// Màn hình chính chứa Scaffold và Bottom Navigation
@Composable
fun MainScreen(
    mainNavController: NavHostController,
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    onLogout: () -> Unit // Thêm tham số onLogout
) {
    val bottomNavController = rememberNavController()
    Scaffold(
        bottomBar = { BottomBar(navController = bottomNavController) }
    ) { innerPadding ->
        BottomNavGraph(
            mainNavController = mainNavController,
            bottomNavController = bottomNavController,
            modifier = Modifier.padding(innerPadding),
            isDarkTheme = isDarkTheme,
            onThemeToggle = onThemeToggle,
            onLogout = onLogout // Truyền xuống
        )
    }
}

@Composable
fun BottomNavGraph(
    mainNavController: NavHostController,
    bottomNavController: NavHostController,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    onLogout: () -> Unit // Thêm tham số onLogout
) {
    NavHost(
        navController = bottomNavController,
        startDestination = "wallet_section",
        modifier = modifier
    ) {
        composable("wallet_section") {
            WalletRoute(
                navController = mainNavController,
                isDarkTheme = isDarkTheme,
                onThemeToggle = onThemeToggle
            )
        }
        composable("market_section") {
            MarketScreen(navController = mainNavController)
        }
        composable("trade_section") {
            P2PScreen(navController = mainNavController)
        }
        composable("profile_section") {
            // Truyền onLogout vào ProfileScreen
            ProfileScreen(navController = mainNavController, onLogout = onLogout)
        }
    }
}

// Composable cho thanh điều hướng dưới cùng
@Composable
fun BottomBar(navController: NavHostController) {
    val items = listOf(
        BottomNavItem("Tài sản", "wallet_section", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet),
        BottomNavItem("Thị trường", "market_section", Icons.Filled.BarChart, Icons.Outlined.BarChart),
        BottomNavItem("Giao dịch", "trade_section", Icons.Filled.SwapHoriz, Icons.Outlined.SwapHoriz),
        BottomNavItem("Hồ sơ", "profile_section", Icons.Filled.Person, Icons.Outlined.Person)
    )

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        items.forEach { item ->
            NavigationBarItem(
                selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (currentDestination?.hierarchy?.any { it.route == item.route } == true) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.title
                    )
                },
                label = { Text(item.title) }
            )
        }
    }
}
