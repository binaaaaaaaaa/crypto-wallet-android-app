package com.example.cryptowallet.data.manager

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.cryptowallet.model.CryptoAsset
import java.math.BigDecimal

enum class AccountType { FUNDING, TRADING }

object BalanceManager {
    val fundingBalances = mutableStateMapOf<String, BigDecimal>()
    val tradingBalances = mutableStateMapOf<String, BigDecimal>()

    var isBalanceVisible by mutableStateOf(true)
        private set

    private var isInitialized = false

    // SỬA LỖI: Thay đổi tham số từ WalletRepository sang List<CryptoAsset>
    fun initialize(assets: List<CryptoAsset>) {
        if (isInitialized) return

        assets.forEach { asset ->
            fundingBalances[asset.symbol] = asset.balance.multiply(BigDecimal("0.3"))
            tradingBalances[asset.symbol] = asset.balance.multiply(BigDecimal("0.7"))
        }
        isInitialized = true
    }

    fun reset() {
        fundingBalances.clear()
        tradingBalances.clear()
        isInitialized = false
    }

    fun performTransfer(
        assetSymbol: String,
        amount: BigDecimal,
        from: AccountType,
        to: AccountType
    ): Boolean {
        val fromMap = if (from == AccountType.FUNDING) fundingBalances else tradingBalances
        val toMap = if (to == AccountType.FUNDING) fundingBalances else tradingBalances
        val sourceBalance = fromMap[assetSymbol] ?: BigDecimal.ZERO
        if (sourceBalance < amount) return false
        fromMap[assetSymbol] = sourceBalance - amount
        toMap[assetSymbol] = (toMap[assetSymbol] ?: BigDecimal.ZERO) + amount
        return true
    }

    fun toggleVisibility() {
        isBalanceVisible = !isBalanceVisible
    }
}
