package `in`.mato.cryptodetail.domain.model

data class Crypto(
    val id: String,
    val rank: Int?,
    val symbol: String,
    val name: String,
    val price: Double,
    val change24h: Double,
    val marketCap: Double?,
    val dailyHigh: Double?,
    val dailyLow: Double?,
    val lastUpdated: String?,
    val imageUrl: String?,
)

data class CryptoDetail(
    val id: String,
    val symbol: String,
    val name: String,
    val imageUrl: String?,
    val currentPrice: Double?,
    val marketCap: Double?,
    val dailyHigh: Double?,
    val dailyLow: Double?,
    val change24h: Double?,
    val lastUpdated: String?,
)

data class HistoryPoint(
    val timestamp: String,
    val price: Double,
)
