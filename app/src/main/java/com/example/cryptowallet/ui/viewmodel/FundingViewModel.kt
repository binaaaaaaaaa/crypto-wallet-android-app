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

data class FundingAsset(
    val asset: CryptoAsset,
    val balance: BigDecimal
) {
    fun getValueInUsd(): BigDecimal = balance.multiply(asset.priceInUsd)
}

data class FundingUiState(
    val assets: List<FundingAsset> = emptyList(),
    val totalValue: BigDecimal = BigDecimal.ZERO,
    // Thêm trạng thái để quản lý bảng Rút tiền
    val isWithdrawSheetVisible: Boolean = false
)

class FundingViewModel(
    private val walletRepository: WalletRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(FundingUiState())
    val uiState: StateFlow<FundingUiState> = _uiState.asStateFlow()

    init {
        loadFundingAssets()
    }

    private fun loadFundingAssets() {
        viewModelScope.launch {
            walletRepository.getAssets().collect { allAssets ->
                val fundingAssets = mutableListOf<FundingAsset>()
                var totalFundingValue = BigDecimal.ZERO

                allAssets.forEach { asset ->
                    val fundingBalance = BalanceManager.fundingBalances[asset.symbol]
                    if (fundingBalance != null && fundingBalance > BigDecimal.ZERO) {
                        val fundingAsset = FundingAsset(
                            asset = asset,
                            balance = fundingBalance
                        )
                        fundingAssets.add(fundingAsset)
                        totalFundingValue += fundingAsset.getValueInUsd()
                    }
                }

                _uiState.update { it.copy(assets = fundingAssets, totalValue = totalFundingValue) }
            }
        }
    }

    // Hàm để hiển thị bảng tùy chọn
    fun showWithdrawSheet() {
        _uiState.update { it.copy(isWithdrawSheetVisible = true) }
    }

    // Hàm để ẩn bảng tùy chọn
    fun hideWithdrawSheet() {
        _uiState.update { it.copy(isWithdrawSheetVisible = false) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val apiService = CryptoApiService.create()
                val walletRepository = WalletRepositoryImpl(apiService)
                FundingViewModel(walletRepository)
            }
        }
    }
}
