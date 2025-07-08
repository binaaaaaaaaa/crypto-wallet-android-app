package com.example.cryptowallet.model

import java.math.BigDecimal

data class CryptoAsset(
    // SỬA LỖI: Thêm trường `id` vào đây
    val id: String,
    val name: String,
    val symbol: String,
    val iconUrl: String,
    val balance: BigDecimal,
    val priceInUsd: BigDecimal
) {
    fun getValueInUsd(): BigDecimal = balance.multiply(priceInUsd)
}
