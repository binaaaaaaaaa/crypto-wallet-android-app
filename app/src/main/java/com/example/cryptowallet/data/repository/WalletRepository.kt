package com.example.cryptowallet.data.repository

import com.example.cryptowallet.data.remote.CoinMarketData
import com.example.cryptowallet.data.remote.CryptoApiService
import com.example.cryptowallet.model.CryptoAsset
import com.example.cryptowallet.model.Transaction
import com.example.cryptowallet.model.TransactionType
import com.example.cryptowallet.ui.viewmodel.AssetDetail
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.math.BigDecimal
import java.util.*

/**
 * Interface định nghĩa các nguồn dữ liệu cho ứng dụng.
 */
interface WalletRepository {
    fun getAssets(): Flow<List<CryptoAsset>>
    fun getTransactions(): Flow<List<Transaction>>
    fun getAssetDetails(assetId: String): Flow<AssetDetail>
    fun getAssetChartData(assetId: String, days: String): Flow<List<Pair<Long, Float>>>
    suspend fun getMarketData(): List<CoinMarketData>
}

/**
 * Lớp triển khai của WalletRepository, chịu trách nhiệm lấy dữ liệu thực tế từ API.
 */
class WalletRepositoryImpl(
    private val apiService: CryptoApiService
) : WalletRepository {

    // Dữ liệu giả lập cho tài khoản demo
    private val demoUserBalances = mapOf(
        "bitcoin" to BigDecimal("0.5"),      // ~$35,000
        "ethereum" to BigDecimal("10"),       // ~$35,000
        "tether" to BigDecimal("10000"),    // $10,000
        "solana" to BigDecimal("100"),      // ~$15,000
        "xrp" to BigDecimal("10000")        // ~$5,000
    )

    private var assetsCache: List<CryptoAsset>? = null

    override fun getAssets(): Flow<List<CryptoAsset>> = flow {
        // Gửi dữ liệu từ cache đi ngay lập tức (nếu có)
        assetsCache?.let { emit(it) }

        val currentUser = Firebase.auth.currentUser

        // Xác định xem nên sử dụng số dư nào
        val balancesToShow = if (currentUser?.email == "khangtran@gmail.com") {
            demoUserBalances
        } else {
            emptyMap() // Tài khoản mới hoặc khác sẽ không có tài sản
        }

        if (balancesToShow.isEmpty()) {
            emit(emptyList())
            return@flow
        }

        val marketData = apiService.getCoinMarkets(vsCurrency = "usd", sparkline = false)

        val freshAssets = marketData
            .filter { marketInfo -> balancesToShow.containsKey(marketInfo.id) }
            .map { marketInfo ->
                CryptoAsset(
                    id = marketInfo.id,
                    name = marketInfo.name,
                    symbol = marketInfo.symbol.uppercase(),
                    iconUrl = marketInfo.imageUrl,
                    priceInUsd = marketInfo.currentPrice,
                    balance = balancesToShow[marketInfo.id] ?: BigDecimal.ZERO
                )
            }

        assetsCache = freshAssets
        emit(freshAssets)
    }
        .flowOn(Dispatchers.IO)


    override fun getTransactions(): Flow<List<Transaction>> = flow {
        delay(500)
        val mockTransactions = listOf(
            Transaction("1", TransactionType.RECEIVED, "BTC", BigDecimal("0.05"), Date(System.currentTimeMillis() - 86400000 * 1), "1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa"),
            Transaction("2", TransactionType.SENT, "ETH", BigDecimal("2.5"), Date(System.currentTimeMillis() - 86400000 * 3), "0xde0b295669a9fd93d5f28d9ec85e40f4cb697bae")
        )
        emit(mockTransactions)
    }.flowOn(Dispatchers.IO)

    override fun getAssetDetails(assetId: String): Flow<AssetDetail> = flow {
        val detailData = apiService.getCoinDetails(assetId)
        val assetDetail = AssetDetail(
            id = detailData.id,
            name = detailData.name,
            symbol = detailData.symbol.uppercase(),
            iconUrl = detailData.image.large,
            currentPrice = detailData.marketData.currentPrice["usd"] ?: BigDecimal.ZERO,
            priceChange24h = detailData.marketData.priceChange24h["usd"] ?: 0.0,
            balance = demoUserBalances[assetId] ?: BigDecimal.ZERO // Tạm thời dùng demo balance
        )
        emit(assetDetail)
    }.flowOn(Dispatchers.IO)

    override fun getAssetChartData(assetId: String, days: String): Flow<List<Pair<Long, Float>>> = flow {
        val chartData = apiService.getMarketChart(assetId, days = days)
        val pricePoints = chartData.prices.map { priceEntry ->
            Pair(priceEntry[0].toLong(), priceEntry[1].toFloat())
        }
        emit(pricePoints)
    }.flowOn(Dispatchers.IO)

    override suspend fun getMarketData(): List<CoinMarketData> {
        return apiService.getCoinMarkets(perPage = 100, sparkline = true)
    }
}
