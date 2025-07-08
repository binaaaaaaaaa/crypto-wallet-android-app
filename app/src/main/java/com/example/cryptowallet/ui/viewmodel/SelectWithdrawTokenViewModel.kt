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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Trạng thái cho giao diện Chọn Token
data class SelectTokenUiState(
    val assets: List<CryptoAsset> = emptyList(),
    val searchQuery: String = ""
)

class SelectWithdrawTokenViewModel(
    private val walletRepository: WalletRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SelectTokenUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadAssets()
    }

    // Tải danh sách tài sản từ repository
    private fun loadAssets() {
        viewModelScope.launch {
            walletRepository.getAssets().collect { assets ->
                _uiState.update { it.copy(assets = assets) }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val apiService = CryptoApiService.create()
                val walletRepository = WalletRepositoryImpl(apiService)
                SelectWithdrawTokenViewModel(walletRepository)
            }
        }
    }
}