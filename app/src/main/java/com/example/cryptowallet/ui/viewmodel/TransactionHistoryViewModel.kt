package com.example.cryptowallet.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.cryptowallet.data.remote.CryptoApiService
import com.example.cryptowallet.data.repository.WalletRepository
import com.example.cryptowallet.data.repository.WalletRepositoryImpl
import com.example.cryptowallet.model.Transaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Lớp trạng thái cho giao diện Lịch sử Giao dịch
data class TransactionHistoryUiState(
    val isLoading: Boolean = false,
    val transactions: List<Transaction> = emptyList(),
    val error: String? = null
)

class TransactionHistoryViewModel(
    private val walletRepository: WalletRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionHistoryUiState())
    val uiState: StateFlow<TransactionHistoryUiState> = _uiState.asStateFlow()

    init {
        fetchTransactions()
    }

    private fun fetchTransactions() {
        viewModelScope.launch {
            walletRepository.getTransactions()
                .onStart {
                    _uiState.update { it.copy(isLoading = true) }
                }
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = "Không thể tải lịch sử giao dịch.") }
                }
                .collect { transactionList ->
                    _uiState.update {
                        it.copy(isLoading = false, transactions = transactionList)
                    }
                }
        }
    }

    // Factory để khởi tạo ViewModel với tham số là repository
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                // Trong một dự án lớn, bạn sẽ dùng Dependency Injection (Hilt/Koin) để quản lý việc này.
                val apiService = CryptoApiService.create()
                val walletRepository = WalletRepositoryImpl(apiService)
                TransactionHistoryViewModel(walletRepository)
            }
        }
    }
}
