package com.example.cryptowallet.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.math.BigDecimal

data class CoinMarketData(
    @SerializedName("id") val id: String,
    @SerializedName("symbol") val symbol: String,
    @SerializedName("name") val name: String,
    @SerializedName("image") val imageUrl: String,
    @SerializedName("current_price") val currentPrice: BigDecimal,
    @SerializedName("price_change_percentage_24h") val priceChangePercentage24h: Double?,
    @SerializedName("sparkline_in_7d") val sparklineIn7d: SparklineData?
)

data class SparklineData(
    @SerializedName("price") val price: List<Double>?
)

data class CoinDetailData(
    @SerializedName("id") val id: String,
    @SerializedName("symbol") val symbol: String,
    @SerializedName("name") val name: String,
    @SerializedName("image") val image: ImageUrl,
    @SerializedName("market_data") val marketData: MarketData
)

data class ImageUrl(@SerializedName("large") val large: String)

data class MarketData(
    @SerializedName("current_price") val currentPrice: Map<String, BigDecimal>,
    @SerializedName("price_change_percentage_24h_in_currency") val priceChange24h: Map<String, Double>
)

data class MarketChartData(
    @SerializedName("prices") val prices: List<List<Double>>
)

interface CryptoApiService {

    @GET("api/v3/coins/markets")
    suspend fun getCoinMarkets(
        @Query("vs_currency") vsCurrency: String = "usd",
        @Query("order") order: String = "market_cap_desc",
        @Query("per_page") perPage: Int = 100,
        @Query("page") page: Int = 1,
        @Query("sparkline") sparkline: Boolean = true
    ): List<CoinMarketData>

    @GET("api/v3/coins/{id}")
    suspend fun getCoinDetails(
        @Path("id") assetId: String,
        @Query("localization") localization: Boolean = false,
        @Query("tickers") tickers: Boolean = false,
        @Query("market_data") marketData: Boolean = true,
        @Query("community_data") communityData: Boolean = false,
        @Query("developer_data") developerData: Boolean = false,
        @Query("sparkline") sparkline: Boolean = false
    ): CoinDetailData

    @GET("api/v3/coins/{id}/market_chart")
    suspend fun getMarketChart(
        @Path("id") assetId: String,
        @Query("vs_currency") vsCurrency: String = "usd",
        @Query("days") days: String = "7"
    ): MarketChartData

    companion object {
        private const val BASE_URL = "https://api.coingecko.com/"

        fun create(): CryptoApiService {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            return retrofit.create(CryptoApiService::class.java)
        }
    }
}
