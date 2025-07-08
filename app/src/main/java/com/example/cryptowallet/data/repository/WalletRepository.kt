package com.example.cryptowallet.data.repository

import com.example.cryptowallet.data.remote.CoinMarketData
import com.example.cryptowallet.data.remote.CryptoApiService
import com.example.cryptowallet.model.CryptoAsset
import com.example.cryptowallet.model.Transaction
import com.example.cryptowallet.model.TransactionType
import com.example.cryptowallet.ui.viewmodel.AssetDetail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.math.BigDecimal
import java.util.*

interface WalletRepository {
    fun getAssets(): Flow<List<CryptoAsset>>
    fun getTransactions(): Flow<List<Transaction>>
    fun getAssetDetails(assetId: String): Flow<AssetDetail>
    fun getAssetChartData(assetId: String, days: String): Flow<List<Pair<Long, Float>>>
    // CẬP NHẬT: Thêm hàm mới
    suspend fun getMarketData(): List<CoinMarketData>
}

class WalletRepositoryImpl(
    private val apiService: CryptoApiService
) : WalletRepository {

    private val userBalances = mapOf(
        "bitcoin" to BigDecimal("0.512"),
        "ethereum" to BigDecimal("10.2"),
        "solana" to BigDecimal("125.7"),
        "dogecoin" to BigDecimal("2500.0"),
        "tether" to BigDecimal("1500.48"),
        "xrp" to BigDecimal("1050.0")
    )

    override fun getAssets(): Flow<List<CryptoAsset>> = flow {
        val marketData = apiService.getCoinMarkets(vsCurrency = "usd", sparkline = false) // Không cần sparkline ở đây
        val assets = marketData
            .filter { marketInfo: CoinMarketData -> userBalances.containsKey(marketInfo.id) }
            .map { marketInfo: CoinMarketData ->
                CryptoAsset(
                    id = marketInfo.id,
                    name = marketInfo.name,
                    symbol = marketInfo.symbol.uppercase(),
                    iconUrl = marketInfo.imageUrl,
                    priceInUsd = marketInfo.currentPrice,
                    balance = userBalances[marketInfo.id] ?: BigDecimal.ZERO
                )
            }
        emit(assets)
    }.flowOn(Dispatchers.IO)

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
            balance = userBalances[assetId] ?: BigDecimal.ZERO
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

    // CẬP NHẬT: Implement hàm mới
    override suspend fun getMarketData(): List<CoinMarketData> {
        return apiService.getCoinMarkets(perPage = 100, sparkline = true)
    }
}
