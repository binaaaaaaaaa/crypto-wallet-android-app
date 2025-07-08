package com.example.cryptowallet.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.cryptowallet.data.manager.AccountType
import com.example.cryptowallet.data.manager.BalanceManager
import com.example.cryptowallet.data.remote.CryptoApiService
import com.example.cryptowallet.data.repository.WalletRepository
import com.example.cryptowallet.data.repository.WalletRepositoryImpl
import com.example.cryptowallet.model.Transaction
import com.patrykandpatrick.vico.core.entry.ChartEntry
import com.patrykandpatrick.vico.core.entry.entryOf
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal

data class AssetDetail(
    val id: String,
    val name: String,
    val symbol: String,
    val iconUrl: String,
    val currentPrice: BigDecimal,
    val priceChange24h: Double,
    val balance: BigDecimal
) {
    fun getValueInUsd(): BigDecimal = balance * currentPrice
}

enum class ChartRange(val days: String) {
    D1("1"),
    W1("7"),
    M1("30"),
    M6("180")
}

data class AssetDetailUiState(
    val isLoading: Boolean = false,
    val assetDetail: AssetDetail? = null,
    val error: String? = null,
    val chartEntries: List<ChartEntry> = emptyList(),
    val selectedChartRange: ChartRange = ChartRange.W1,
    val accountType: AccountType? = null,
    val isDepositSheetVisible: Boolean = false,
    val isWithdrawSheetVisible: Boolean = false,
    val recentTransactions: List<Transaction> = emptyList(),
    val fundingBalance: BigDecimal = BigDecimal.ZERO,
    val tradingBalance: BigDecimal = BigDecimal.ZERO
)

class AssetDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val walletRepository: WalletRepository
) : ViewModel() {

    private val assetId: String = checkNotNull(savedStateHandle["assetId"])

    private val _uiState = MutableStateFlow(AssetDetailUiState())
    val uiState: StateFlow<AssetDetailUiState> = _uiState.asStateFlow()

    init {
        fetchAssetDetails()
        fetchChartData(ChartRange.W1)
    }

    private fun fetchAssetDetails() {
        viewModelScope.launch {
            walletRepository.getAssetDetails(assetId)
                .onStart { _uiState.update { it.copy(isLoading = true, error = null) } }
                .catch { e: Throwable ->
                    _uiState.update { it.copy(isLoading = false, error = "Không thể tải dữ liệu chi tiết.") }
                }
                .collect { detail: AssetDetail ->
                    val fundingBalance = BalanceManager.fundingBalances[detail.symbol] ?: BigDecimal.ZERO
                    val tradingBalance = BalanceManager.tradingBalances[detail.symbol] ?: BigDecimal.ZERO

                    val account = if (fundingBalance > BigDecimal.ZERO) {
                        AccountType.FUNDING
                    } else if (tradingBalance > BigDecimal.ZERO) {
                        AccountType.TRADING
                    } else {
                        null
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            assetDetail = detail,
                            fundingBalance = fundingBalance,
                            tradingBalance = tradingBalance,
                            accountType = account
                        )
                    }
                    fetchRecentTransactions(detail.symbol)
                }
        }
    }

    private fun fetchChartData(range: ChartRange) {
        viewModelScope.launch {
            _uiState.update { it.copy(chartEntries = emptyList()) }
            walletRepository.getAssetChartData(assetId, days = range.days)
                .catch { /* Bỏ qua lỗi biểu đồ */ }
                .collect { pricePoints: List<Pair<Long, Float>> ->
                    val entries = pricePoints.mapIndexed { index, pair ->
                        entryOf(index.toFloat(), pair.second)
                    }
                    _uiState.update { it.copy(chartEntries = entries) }
                }
        }
    }

    private fun fetchRecentTransactions(assetSymbol: String) {
        viewModelScope.launch {
            walletRepository.getTransactions()
                .catch { /* Bỏ qua lỗi */ }
                .collect { allTransactions ->
                    val filtered = allTransactions.filter { it.assetSymbol.equals(assetSymbol, ignoreCase = true) }
                    _uiState.update { it.copy(recentTransactions = filtered) }
                }
        }
    }

    fun onChartRangeSelected(range: ChartRange) {
        _uiState.update { it.copy(selectedChartRange = range) }
        fetchChartData(range)
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
        fun Factory(assetId: String): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val savedStateHandle = createSavedStateHandle()
                savedStateHandle["assetId"] = assetId

                val apiService = CryptoApiService.create()
                val walletRepository = WalletRepositoryImpl(apiService)
                AssetDetailViewModel(savedStateHandle, walletRepository)
            }
        }
    }
}
