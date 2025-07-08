package com.example.cryptowallet.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.cryptowallet.data.manager.BalanceManager
import com.example.cryptowallet.data.remote.CryptoApiService
import com.example.cryptowallet.data.repository.WalletRepository
import com.example.cryptowallet.data.repository.WalletRepositoryImpl
import com.example.cryptowallet.model.CryptoAsset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal

// Data class để biểu diễn tài sản trong ví Giao dịch
// Tái sử dụng FundingAsset để tránh lặp code, có thể đổi tên thành AccountAssetItem nếu muốn
data class TradingAsset(
    val asset: CryptoAsset,
    val balance: BigDecimal
) {
    fun getValueInUsd(): BigDecimal = balance.multiply(asset.priceInUsd)
}

// Trạng thái giao diện cho màn hình Giao dịch
data class TradingUiState(
    val assets: List<TradingAsset> = emptyList(),
    val totalValue: BigDecimal = BigDecimal.ZERO
)

class TradingViewModel(
    private val walletRepository: WalletRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(TradingUiState())
    val uiState: StateFlow<TradingUiState> = _uiState.asStateFlow()

    init {
        loadTradingAssets()
    }

    private fun loadTradingAssets() {
        viewModelScope.launch {
            walletRepository.getAssets().collect { allAssets ->
                val tradingAssets = mutableListOf<TradingAsset>()
                var totalTradingValue = BigDecimal.ZERO

                // Lọc và tạo danh sách tài sản cho ví Giao dịch
                allAssets.forEach { asset ->
                    val tradingBalance = BalanceManager.tradingBalances[asset.symbol]
                    if (tradingBalance != null && tradingBalance > BigDecimal.ZERO) {
                        val tradingAsset = TradingAsset(
                            asset = asset,
                            balance = tradingBalance
                        )
                        tradingAssets.add(tradingAsset)
                        totalTradingValue += tradingAsset.getValueInUsd()
                    }
                }

                _uiState.update { it.copy(assets = tradingAssets, totalValue = totalTradingValue) }
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val apiService = CryptoApiService.create()
                val walletRepository = WalletRepositoryImpl(apiService)
                TradingViewModel(walletRepository)
            }
        }
    }
}
