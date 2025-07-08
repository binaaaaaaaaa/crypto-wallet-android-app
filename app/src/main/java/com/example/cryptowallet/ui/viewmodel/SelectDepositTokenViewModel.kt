package com.example.cryptowallet.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.cryptowallet.data.remote.CryptoApiService
import com.example.cryptowallet.data.repository.WalletRepository
import com.example.cryptowallet.data.repository.WalletRepositoryImpl
import com.example.cryptowallet.model.CryptoAsset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal

/**
 * Trạng thái giao diện cho màn hình Chọn Token để Nạp.
 */
data class SelectDepositTokenUiState(
    val isLoading: Boolean = false,
    val allTokens: List<CryptoAsset> = emptyList(),
    val error: String? = null,
    val searchQuery: String = ""
)

class SelectDepositTokenViewModel(
    private val walletRepository: WalletRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SelectDepositTokenUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadAllTokens()
    }

    private fun loadAllTokens() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                // Lấy dữ liệu của 100 token hàng đầu từ thị trường
                val marketData = (walletRepository as WalletRepositoryImpl).getMarketData()
                val tokens = marketData.map { data ->
                    CryptoAsset(
                        id = data.id,
                        name = data.name,
                        symbol = data.symbol.uppercase(),
                        iconUrl = data.imageUrl,
                        // Số dư không cần thiết ở màn hình này
                        balance = BigDecimal.ZERO,
                        priceInUsd = data.currentPrice
                    )
                }
                _uiState.update { it.copy(isLoading = false, allTokens = tokens) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Không thể tải danh sách token.") }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val apiService = CryptoApiService.create()
                val walletRepository = WalletRepositoryImpl(apiService)
                SelectDepositTokenViewModel(walletRepository)
            }
        }
    }
}
