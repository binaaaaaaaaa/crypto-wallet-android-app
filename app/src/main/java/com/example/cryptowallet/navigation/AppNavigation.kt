package com.example.cryptowallet.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.cryptowallet.ui.screen.*
import com.example.cryptowallet.ui.screen.auth.LoginScreen
import com.example.cryptowallet.ui.screen.auth.RegisterScreen
import com.example.cryptowallet.ui.screen.main.MainScreen
import com.example.cryptowallet.ui.viewmodel.AuthViewModel

@Composable
fun AppNavigation(
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val navController = rememberNavController()
    val authState by authViewModel.authState.collectAsState()

    // Luôn bắt đầu từ màn hình chờ để xác định trạng thái đăng nhập
    NavHost(navController = navController, startDestination = AppRoutes.SPLASH_SCREEN) {

        composable(AppRoutes.SPLASH_SCREEN) {
            // Màn hình chờ đơn giản
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        authGraph(
            navController = navController,
            authViewModel = authViewModel
        )

        mainGraph(
            navController = navController,
            isDarkTheme = isDarkTheme,
            onThemeToggle = onThemeToggle,
            onLogout = { authViewModel.logout() }
        )
    }

    // Lắng nghe sự thay đổi trạng thái để điều hướng sau khi tải xong
    LaunchedEffect(authState.isLoading) {
        if (!authState.isLoading) {
            val destination = if (authState.isAuthenticated) AppRoutes.MAIN_GRAPH else AppRoutes.AUTH_GRAPH
            navController.navigate(destination) {
                // Xóa màn hình chờ khỏi back stack để người dùng không thể quay lại
                popUpTo(AppRoutes.SPLASH_SCREEN) { inclusive = true }
            }
        }
    }
}

// Hàm mở rộng để định nghĩa đồ thị xác thực
fun NavGraphBuilder.authGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    navigation(
        startDestination = AppRoutes.LOGIN_SCREEN,
        route = AppRoutes.AUTH_GRAPH
    ) {
        composable(AppRoutes.LOGIN_SCREEN) {
            LoginScreen(navController = navController, authViewModel = authViewModel)
        }
        composable(AppRoutes.REGISTER_SCREEN) {
            RegisterScreen(navController = navController, authViewModel = authViewModel)
        }
    }
}

// Hàm mở rộng để định nghĩa đồ thị nội dung chính
fun NavGraphBuilder.mainGraph(
    navController: NavHostController,
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    onLogout: () -> Unit
) {
    navigation(
        startDestination = AppRoutes.MAIN_SCREEN,
        route = AppRoutes.MAIN_GRAPH
    ) {
        composable(AppRoutes.MAIN_SCREEN) {
            MainScreen(
                mainNavController = navController,
                isDarkTheme = isDarkTheme,
                onThemeToggle = onThemeToggle,
                onLogout = onLogout
            )
        }
        // Định nghĩa tất cả các màn hình có thể truy cập sau khi đăng nhập
        composable(AppRoutes.TRANSACTIONS_SCREEN) { TransactionHistoryScreen(navController) }
        composable(AppRoutes.SEND_SCREEN) { SendScreen(navController) }
        composable(AppRoutes.P2P_SCREEN) { P2PScreen(navController) }
        composable(AppRoutes.TRANSFER_SCREEN) { TransferScreen(navController) }
        composable(AppRoutes.FUNDING_SCREEN) { FundingScreen(navController) }
        composable(AppRoutes.TRADING_SCREEN) { TradingScreen(navController) }
        composable(AppRoutes.SELECT_TOKEN_SCREEN) { SelectTokenScreen(navController) }
        composable(AppRoutes.SELECT_WITHDRAW_TOKEN_SCREEN) { SelectWithdrawTokenScreen(navController) }
        composable(AppRoutes.CONVERT_SCREEN) { ConvertScreen(navController) }
        composable(AppRoutes.CHANGE_EMAIL_SCREEN) { ChangeEmailScreen(navController) }
        composable(AppRoutes.SEED_PHRASE_SCREEN) { SeedPhraseScreen(navController) }

        composable(
            route = "${AppRoutes.SELECT_NETWORK_SCREEN}/{assetSymbol}",
            arguments = listOf(navArgument("assetSymbol") { type = NavType.StringType })
        ) { backStackEntry ->
            val assetSymbol = backStackEntry.arguments?.getString("assetSymbol")
            SelectNetworkScreen(navController, assetSymbol)
        }

        composable(
            route = "${AppRoutes.RECEIVE_SCREEN}/{assetSymbol}/{networkName}",
            arguments = listOf(
                navArgument("assetSymbol") { type = NavType.StringType },
                navArgument("networkName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val assetSymbol = backStackEntry.arguments?.getString("assetSymbol")
            val networkName = backStackEntry.arguments?.getString("networkName")
            ReceiveScreen(navController, assetSymbol, networkName)
        }

        composable(
            route = "${AppRoutes.ASSET_DETAIL_SCREEN}/{assetId}",
            arguments = listOf(navArgument("assetId") { type = NavType.StringType })
        ) { backStackEntry ->
            val assetId = backStackEntry.arguments?.getString("assetId")
            if (assetId != null) {
                AssetDetailScreen(navController, assetId)
            }
        }

        composable(
            route = "${AppRoutes.SELECT_DESTINATION_SCREEN}/{assetSymbol}",
            arguments = listOf(navArgument("assetSymbol") { type = NavType.StringType })
        ) { backStackEntry ->
            val assetSymbol = backStackEntry.arguments?.getString("assetSymbol")
            SelectDestinationScreen(navController, assetSymbol)
        }

        composable(
            route = "${AppRoutes.VERIFY_EMAIL_SCREEN}/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email")
            VerifyEmailScreen(navController, email)
        }
    }
}
