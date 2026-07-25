package `in`.mato.cryptodetail.data.repository

import `in`.mato.cryptodetail.data.remote.CoinDetailDto
import `in`.mato.cryptodetail.data.remote.CoinMarketDto
import `in`.mato.cryptodetail.data.remote.CryptoApiService
import `in`.mato.cryptodetail.data.remote.MarketChartDto
import `in`.mato.cryptodetail.domain.model.Crypto
import `in`.mato.cryptodetail.domain.model.CryptoDetail
import `in`.mato.cryptodetail.domain.model.HistoryPoint
import `in`.mato.cryptodetail.domain.repository.CryptoRepository
import javax.inject.Inject

class CryptoRepositoryImpl @Inject constructor(
    private val api: CryptoApiService,
) : CryptoRepository {
    override suspend fun getTopCryptocurrencies(limit: Int, page: Int): Result<List<Crypto>> = runCatching {
        api.getCoinMarkets(limit = limit, page = page).map(CoinMarketDto::toDomain)
    }

    override suspend fun getCryptoDetail(id: String): Result<CryptoDetail> = runCatching {
        api.getCoinDetail(id).toDomain()
    }

    override suspend fun getPriceHistory(id: String, days: Int): Result<List<HistoryPoint>> = runCatching {
        api.getMarketChart(id = id, days = days).toDomain()
    }
}

private fun CoinMarketDto.toDomain() = Crypto(
    id = id,
    rank = rank,
    symbol = symbol,
    name = name,
    price = price,
    change24h = change24h ?: 0.0,
    marketCap = marketCap,
    dailyHigh = dailyHigh,
    dailyLow = dailyLow,
    lastUpdated = lastUpdated,
    imageUrl = imageUrl,
)

private fun CoinDetailDto.toDomain() = CryptoDetail(
    id = id,
    symbol = symbol,
    name = name,
    imageUrl = image?.large,
    currentPrice = marketData?.currentPrice?.get("inr"),
    marketCap = marketData?.marketCap?.get("inr"),
    dailyHigh = marketData?.dailyHigh?.get("inr"),
    dailyLow = marketData?.dailyLow?.get("inr"),
    change24h = marketData?.change24h,
    lastUpdated = lastUpdated,
)

private fun MarketChartDto.toDomain(): List<HistoryPoint> = prices.mapNotNull { point ->
    val timestamp = point.getOrNull(0)?.toLong() ?: return@mapNotNull null
    val price = point.getOrNull(1) ?: return@mapNotNull null
    HistoryPoint(timestamp = timestamp.toString(), price = price)
}
