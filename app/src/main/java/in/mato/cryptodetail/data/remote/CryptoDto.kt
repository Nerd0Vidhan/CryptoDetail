package `in`.mato.cryptodetail.data.remote

import com.google.gson.annotations.SerializedName

data class CoinMarketDto(
    val id: String,
    @SerializedName("market_cap_rank") val rank: Int? = null,
    val symbol: String,
    val name: String,
    @SerializedName("current_price") val price: Double,
    @SerializedName("price_change_percentage_24h") val change24h: Double? = null,
    @SerializedName("market_cap") val marketCap: Double? = null,
    @SerializedName("high_24h") val dailyHigh: Double? = null,
    @SerializedName("low_24h") val dailyLow: Double? = null,
    @SerializedName("last_updated") val lastUpdated: String? = null,
    @SerializedName("image") val imageUrl: String? = null,
)

data class CoinDetailDto(
    val id: String,
    val symbol: String,
    val name: String,
    val image: CoinImageDto? = null,
    @SerializedName("market_data") val marketData: CoinMarketDataDto? = null,
    @SerializedName("last_updated") val lastUpdated: String? = null,
)

data class CoinImageDto(val large: String? = null)

data class CoinMarketDataDto(
    @SerializedName("current_price") val currentPrice: Map<String, Double?> = emptyMap(),
    @SerializedName("market_cap") val marketCap: Map<String, Double?> = emptyMap(),
    @SerializedName("high_24h") val dailyHigh: Map<String, Double?> = emptyMap(),
    @SerializedName("low_24h") val dailyLow: Map<String, Double?> = emptyMap(),
    @SerializedName("price_change_percentage_24h") val change24h: Double? = null,
)

data class MarketChartDto(
    val prices: List<List<Double>> = emptyList(),
)
