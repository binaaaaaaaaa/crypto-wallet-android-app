package com.example.cryptowallet.utils

import java.text.NumberFormat
import java.util.Locale

/**
 * Định dạng một số Double thành chuỗi tiền tệ (ví dụ: $1,234.56).
 * @param amount Số tiền cần định dạng.
 * @return Chuỗi đã được định dạng.
 */
fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale.US)
    return format.format(amount)
}
