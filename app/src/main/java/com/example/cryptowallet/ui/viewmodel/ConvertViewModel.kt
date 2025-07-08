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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode

enum class SelectionType { FROM, TO }

data class ConvertUiState(
    val fromAsset: CryptoAsset? = null,
    val toAsset: CryptoAsset? = null,
    val fromAmount: String = "",
    val toAmount: String = "",
    val availableAssets: List<CryptoAsset> = emptyList(),
    val isAssetSheetVisible: Boolean = false,
    val currentSelectionType: SelectionType = SelectionType.FROM
)

class ConvertViewModel(
    private val walletRepository: WalletRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConvertUiState())
    val uiState: StateFlow<ConvertUiState> = _uiState.asStateFlow()

    init {
        loadAvailableAssets()
    }

    private fun loadAvailableAssets() {
        viewModelScope.launch {
            walletRepository.getAssets().collect { assets ->
                _uiState.update {
                    it.copy(
                        availableAssets = assets,
                        fromAsset = assets.find { it.symbol == "USDT" } ?: assets.getOrNull(0),
                        toAsset = assets.find { it.symbol == "BTC" } ?: assets.getOrNull(1)
                    )
                }
            }
        }
    }

    fun onFromAmountChange(amount: String) {
        val fromAmountDecimal = amount.toBigDecimalOrNull() ?: BigDecimal.ZERO
        _uiState.update {
            it.copy(
                fromAmount = amount,
                toAmount = calculateToAmount(fromAmountDecimal, it.fromAsset, it.toAsset)
            )
        }
    }

    private fun calculateToAmount(fromAmount: BigDecimal, fromAsset: CryptoAsset?, toAsset: CryptoAsset?): String {
        if (fromAsset == null || toAsset == null || toAsset.priceInUsd == BigDecimal.ZERO) {
            return ""
        }
        val fromValueInUsd = fromAmount * fromAsset.priceInUsd
        val toAmount = fromValueInUsd.divide(toAsset.priceInUsd, 8, RoundingMode.HALF_UP)
        return toAmount.toPlainString()
    }

    fun swapAssets() {
        _uiState.update {
            val newFromAmount = it.toAmount
            val newToAmount = calculateToAmount(newFromAmount.toBigDecimalOrNull() ?: BigDecimal.ZERO, it.toAsset, it.fromAsset)
            it.copy(
                fromAsset = it.toAsset,
                toAsset = it.fromAsset,
                fromAmount = newFromAmount,
                toAmount = newToAmount
            )
        }
    }

    fun selectAsset(asset: CryptoAsset) {
        _uiState.update {
            if (it.currentSelectionType == SelectionType.FROM) {
                if (asset.id == it.toAsset?.id) {
                    it.copy(fromAsset = it.toAsset, toAsset = it.fromAsset, isAssetSheetVisible = false)
                } else {
                    it.copy(fromAsset = asset, isAssetSheetVisible = false)
                }
            } else {
                if (asset.id == it.fromAsset?.id) {
                    it.copy(toAsset = it.fromAsset, fromAsset = it.toAsset, isAssetSheetVisible = false)
                } else {
                    it.copy(toAsset = asset, isAssetSheetVisible = false)
                }
            }
        }
        onFromAmountChange(_uiState.value.fromAmount)
    }

    // CẬP NHẬT: Thêm hàm xử lý nút "Tối đa"
    fun setMaxAmount() {
        val state = _uiState.value
        val maxAmount = state.fromAsset?.balance
        if (maxAmount != null) {
            onFromAmountChange(maxAmount.toPlainString())
        }
    }

    fun showAssetSheet(type: SelectionType) {
        _uiState.update { it.copy(isAssetSheetVisible = true, currentSelectionType = type) }
    }

    fun hideAssetSheet() {
        _uiState.update { it.copy(isAssetSheetVisible = false) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val apiService = CryptoApiService.create()
                val walletRepository = WalletRepositoryImpl(apiService)
                ConvertViewModel(walletRepository)
            }
        }
    }
}
