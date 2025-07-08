package com.example.cryptowallet.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.cryptowallet.data.remote.CryptoApiService
import com.example.cryptowallet.data.repository.WalletRepository
import com.example.cryptowallet.data.repository.WalletRepositoryImpl
import com.patrykandpatrick.vico.core.entry.ChartEntry
import com.patrykandpatrick.vico.core.entry.entryOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Data class để biểu diễn một coin trên màn hình Thị trường
data class MarketCoin(
    val id: String,
    val name: String,
    val symbol: String,
    val iconUrl: String,
    val price: java.math.BigDecimal,
    val priceChangePercentage24h: Double,
    val chartEntries: List<ChartEntry>
)

// Enum để định nghĩa các danh mục trên màn hình Thị trường
enum class MarketCategory {
    ALL, GAINERS, LOSERS
}

// Trạng thái giao diện cho màn hình Thị trường
data class MarketUiState(
    val isLoading: Boolean = false,
    val allCoins: List<MarketCoin> = emptyList(), // Lưu danh sách gốc
    val displayedCoins: List<MarketCoin> = emptyList(), // Danh sách được hiển thị
    val error: String? = null,
    val searchQuery: String = "",
    val selectedCategory: MarketCategory = MarketCategory.ALL
)

class MarketViewModel(
    private val walletRepository: WalletRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(MarketUiState())
    val uiState = _uiState.asStateFlow()

    init {
        fetchMarketData()
    }

    private fun fetchMarketData() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val marketData = walletRepository.getMarketData()
                val marketCoins = marketData.map { data ->
                    val entries = data.sparklineIn7d?.price?.mapIndexedNotNull { index, price ->
                        price?.let { entryOf(index.toFloat(), it.toFloat()) }
                    } ?: emptyList()

                    MarketCoin(
                        id = data.id,
                        name = data.name,
                        symbol = data.symbol.uppercase(),
                        iconUrl = data.imageUrl,
                        price = data.currentPrice,
                        priceChangePercentage24h = data.priceChangePercentage24h ?: 0.0,
                        chartEntries = entries
                    )
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        allCoins = marketCoins
                    )
                }
                // Sau khi có dữ liệu, cập nhật danh sách hiển thị
                updateDisplayedCoins()
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Không thể tải dữ liệu thị trường.") }
            }
        }
    }

    // Hàm được gọi khi người dùng thay đổi nội dung tìm kiếm hoặc chọn tab
    private fun updateDisplayedCoins() {
        _uiState.update { currentState ->
            // Lọc theo từ khóa tìm kiếm trước
            val searchFilteredList = if (currentState.searchQuery.isBlank()) {
                currentState.allCoins
            } else {
                currentState.allCoins.filter { coin ->
                    coin.name.contains(currentState.searchQuery, ignoreCase = true) ||
                            coin.symbol.contains(currentState.searchQuery, ignoreCase = true)
                }
            }

            // SỬA LỖI: Lọc và sắp xếp theo danh mục
            val categorySortedList = when (currentState.selectedCategory) {
                MarketCategory.ALL -> searchFilteredList
                MarketCategory.GAINERS -> searchFilteredList
                    .filter { it.priceChangePercentage24h >= 0 }
                    .sortedByDescending { it.priceChangePercentage24h }
                MarketCategory.LOSERS -> searchFilteredList
                    .filter { it.priceChangePercentage24h < 0 }
                    .sortedBy { it.priceChangePercentage24h }
            }

            currentState.copy(displayedCoins = categorySortedList)
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        updateDisplayedCoins()
    }

    fun onCategorySelected(category: MarketCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
        updateDisplayedCoins()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val apiService = CryptoApiService.create()
                val walletRepository = WalletRepositoryImpl(apiService)
                MarketViewModel(walletRepository)
            }
        }
    }
}
