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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.Calendar

data class WalletUiState(
    val isLoading: Boolean = false,
    val assets: List<CryptoAsset> = emptyList(),
    val totalBalance: BigDecimal = BigDecimal.ZERO,
    val error: String? = null,
    val searchQuery: String = "",
    val pnlValue: Double = 0.0,
    val pnlPercentage: Double = 0.0,
    val isPnlPositive: Boolean = true,
    val isDepositSheetVisible: Boolean = false,
    val isWithdrawSheetVisible: Boolean = false,
    val fundingBalance: BigDecimal = BigDecimal.ZERO,
    val tradingBalance: BigDecimal = BigDecimal.ZERO
)

class WalletViewModel(
    private val walletRepository: WalletRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

    private var balanceAtStartOfDay: BigDecimal = BigDecimal.ZERO
    private var dayOfLastRecord: Int = -1

    init {
        viewModelScope.launch {
            while (true) {
                fetchWalletData()
                delay(15000)
            }
        }
    }

    fun fetchWalletData() {
        viewModelScope.launch {
            walletRepository.getAssets()
                .onStart {
                    _uiState.update { it.copy(isLoading = true, error = null) }
                }
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = "Không thể tải dữ liệu.") }
                }
                .collect { assetList ->
                    // SỬA LỖI: Đổi tên hàm từ updateBalances thành initialize
                    BalanceManager.initialize(assetList)

                    val newTotalBalance = assetList.fold(BigDecimal.ZERO) { acc, asset ->
                        acc + asset.getValueInUsd()
                    }

                    val calendar = Calendar.getInstance()
                    val currentDay = calendar.get(Calendar.DAY_OF_YEAR)

                    var pnlValue = 0.0
                    var pnlPercentage = 0.0

                    if (dayOfLastRecord != currentDay || balanceAtStartOfDay == BigDecimal.ZERO) {
                        balanceAtStartOfDay = newTotalBalance
                        dayOfLastRecord = currentDay
                    } else {
                        pnlValue = (newTotalBalance - balanceAtStartOfDay).toDouble()
                        if (balanceAtStartOfDay > BigDecimal.ZERO) {
                            pnlPercentage = (pnlValue / balanceAtStartOfDay.toDouble()) * 100
                        }
                    }

                    var fundingValue = BigDecimal.ZERO
                    var tradingValue = BigDecimal.ZERO

                    assetList.forEach { asset ->
                        val fundingQty = BalanceManager.fundingBalances[asset.symbol] ?: BigDecimal.ZERO
                        val tradingQty = BalanceManager.tradingBalances[asset.symbol] ?: BigDecimal.ZERO
                        fundingValue += fundingQty * asset.priceInUsd
                        tradingValue += tradingQty * asset.priceInUsd
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            assets = assetList,
                            totalBalance = newTotalBalance,
                            pnlValue = pnlValue,
                            pnlPercentage = pnlPercentage,
                            isPnlPositive = pnlValue >= 0,
                            fundingBalance = fundingValue,
                            tradingBalance = tradingValue
                        )
                    }
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun showDepositSheet() {
        _uiState.update { it.copy(isDepositSheetVisible = true) }
    }

    fun hideDepositSheet() {
        _uiState.update { it.copy(isDepositSheetVisible = false) }
    }

    fun showWithdrawSheet() {
        _uiState.update { it.copy(isWithdrawSheetVisible = true) }
    }

    fun hideWithdrawSheet() {
        _uiState.update { it.copy(isWithdrawSheetVisible = false) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val apiService = CryptoApiService.create()
                val walletRepository = WalletRepositoryImpl(apiService)
                WalletViewModel(walletRepository)
            }
        }
    }
}
