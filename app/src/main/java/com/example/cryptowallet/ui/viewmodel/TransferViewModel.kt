package com.example.cryptowallet.ui.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cryptowallet.data.manager.AccountType
import com.example.cryptowallet.data.manager.BalanceManager
import com.example.cryptowallet.model.CryptoAsset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal

// Data class để quản lý số dư của một tài sản trong các tài khoản khác nhau
data class AccountAsset(
    val asset: CryptoAsset,
    val fundingBalance: BigDecimal,
    val tradingBalance: BigDecimal
)

// Trạng thái cho giao diện Chuyển tiền
data class TransferUiState(
    val fromAccount: String = "Tài khoản Funding",
    val toAccount: String = "Tài khoản Giao dịch",
    val amount: String = "",
    val selectedAsset: AccountAsset? = null,
    val availableAssets: List<AccountAsset> = emptyList(),
    val isAssetSheetVisible: Boolean = false
)

class TransferViewModel() : ViewModel() {

    private val _uiState = MutableStateFlow(TransferUiState())
    val uiState: StateFlow<TransferUiState> = _uiState.asStateFlow()

    init {
        loadAvailableAssets()
    }

    // Lấy dữ liệu trực tiếp từ BalanceManager
    private fun loadAvailableAssets() {
        viewModelScope.launch {
            // Lấy danh sách các biểu tượng token từ BalanceManager
            val symbols = BalanceManager.fundingBalances.keys

            val accountAssets = symbols.map { symbol ->
                // Tìm thông tin chi tiết của asset từ danh sách gốc (tạm thời giả lập)
                // Trong ứng dụng thực tế, bạn sẽ có một nguồn dữ liệu chung để lấy thông tin này
                val assetInfo = findAssetInfoBySymbol(symbol)
                AccountAsset(
                    asset = assetInfo,
                    fundingBalance = BalanceManager.fundingBalances[symbol] ?: BigDecimal.ZERO,
                    tradingBalance = BalanceManager.tradingBalances[symbol] ?: BigDecimal.ZERO
                )
            }

            _uiState.update {
                it.copy(
                    availableAssets = accountAssets,
                    selectedAsset = accountAssets.firstOrNull() // Mặc định chọn tài sản đầu tiên
                )
            }
        }
    }

    // Hàm giả lập để lấy thông tin chi tiết của token
    private fun findAssetInfoBySymbol(symbol: String): CryptoAsset {
        // Đây là nơi bạn sẽ tra cứu thông tin từ một nguồn dữ liệu đáng tin cậy
        // Dưới đây là dữ liệu giả lập cho các token chúng ta đang dùng
        return when (symbol.uppercase()) {
            "USDT" -> CryptoAsset("tether", "Tether", "USDT", "https://s2.coinmarketcap.com/static/img/coins/64x64/825.png", BigDecimal.ZERO, BigDecimal.ZERO)
            "BTC" -> CryptoAsset("bitcoin", "Bitcoin", "BTC", "https://s2.coinmarketcap.com/static/img/coins/64x64/1.png", BigDecimal.ZERO, BigDecimal.ZERO)
            "ETH" -> CryptoAsset("ethereum", "Ethereum", "ETH", "https://s2.coinmarketcap.com/static/img/coins/64x64/1027.png", BigDecimal.ZERO, BigDecimal.ZERO)
            "SOL" -> CryptoAsset("solana", "Solana", "SOL", "https://s2.coinmarketcap.com/static/img/coins/64x64/5426.png", BigDecimal.ZERO, BigDecimal.ZERO)
            "XRP" -> CryptoAsset("xrp", "XRP", "XRP", "https://s2.coinmarketcap.com/static/img/coins/64x64/52.png", BigDecimal.ZERO, BigDecimal.ZERO)
            "DOGE" -> CryptoAsset("dogecoin", "Dogecoin", "DOGE", "https://s2.coinmarketcap.com/static/img/coins/64x64/74.png", BigDecimal.ZERO, BigDecimal.ZERO)
            else -> CryptoAsset("unknown", "Unknown", symbol, "", BigDecimal.ZERO, BigDecimal.ZERO)
        }
    }


    fun onAmountChange(newAmount: String) {
        _uiState.update { it.copy(amount = newAmount) }
    }

    fun onSwapAccounts() {
        _uiState.update {
            it.copy(
                fromAccount = it.toAccount,
                toAccount = it.fromAccount
            )
        }
    }

    fun onSelectAsset(asset: AccountAsset) {
        _uiState.update { it.copy(selectedAsset = asset, isAssetSheetVisible = false) }
    }

    fun showAssetSheet() {
        _uiState.update { it.copy(isAssetSheetVisible = true) }
    }

    fun hideAssetSheet() {
        _uiState.update { it.copy(isAssetSheetVisible = false) }
    }

    fun setMaxAmount() {
        val state = _uiState.value
        val fromAccountType = if (state.fromAccount == "Tài khoản Funding") AccountType.FUNDING else AccountType.TRADING
        val maxAmount = if (fromAccountType == AccountType.FUNDING) {
            state.selectedAsset?.fundingBalance
        } else {
            state.selectedAsset?.tradingBalance
        }
        _uiState.update { it.copy(amount = maxAmount?.toPlainString() ?: "") }
    }

    fun confirmTransfer(context: Context, onTransferSuccess: () -> Unit) {
        val state = _uiState.value
        val amountToTransfer = state.amount.toBigDecimalOrNull()
        val asset = state.selectedAsset

        if (amountToTransfer == null || amountToTransfer <= BigDecimal.ZERO || asset == null) {
            Toast.makeText(context, "Vui lòng nhập số tiền hợp lệ", Toast.LENGTH_SHORT).show()
            return
        }

        val fromAccount = if (state.fromAccount == "Tài khoản Funding") AccountType.FUNDING else AccountType.TRADING
        val toAccount = if (state.toAccount == "Tài khoản Funding") AccountType.FUNDING else AccountType.TRADING

        val success = BalanceManager.performTransfer(
            assetSymbol = asset.asset.symbol,
            amount = amountToTransfer,
            from = fromAccount,
            to = toAccount
        )

        if (success) {
            Toast.makeText(context, "Chuyển tiền thành công!", Toast.LENGTH_SHORT).show()
            onTransferSuccess() // Gọi callback để điều hướng về
        } else {
            Toast.makeText(context, "Số dư không đủ", Toast.LENGTH_SHORT).show()
        }
    }
}
