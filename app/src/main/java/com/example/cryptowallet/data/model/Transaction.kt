package com.example.cryptowallet.model

import java.math.BigDecimal
import java.util.Date

/**
 * Đại diện cho một giao dịch (gửi hoặc nhận).
 *
 * @param id Mã định danh duy nhất của giao dịch.
 * @param type Loại giao dịch, là SENT (đã gửi) hoặc RECEIVED (đã nhận).
 * @param assetSymbol Ký hiệu của tài sản được giao dịch (ví dụ: "BTC").
 * @param amount Số lượng tài sản trong giao dịch.
 * @param date Thời gian giao dịch diễn ra.
 * @param address Địa chỉ ví của người gửi/nhận.
 */
data class Transaction(
    val id: String,
    val type: TransactionType,
    val assetSymbol: String,
    val amount: BigDecimal,
    val date: Date,
    val address: String
)

/**
 * Enum định nghĩa các loại giao dịch có thể có.
 */
enum class TransactionType {
    SENT,
    RECEIVED
}
