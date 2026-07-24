package `in`.mato.cryptodetail.data.repository

import `in`.mato.cryptodetail.data.remote.CoinDetailDto
import `in`.mato.cryptodetail.data.remote.CoinMarketDataDto
import `in`.mato.cryptodetail.data.remote.CoinMarketDto
import `in`.mato.cryptodetail.data.remote.CryptoApiService
import `in`.mato.cryptodetail.data.remote.MarketChartDto
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class CryptoRepositoryImplTest {
    private val api = mockk<CryptoApiService>()
    private val repository = CryptoRepositoryImpl(api)

    @Test
    fun marketDTOMapToDomain() = runTest {
        coEvery { api.getCoinMarkets(limit = 10, page = 1) } returns listOf(
            CoinMarketDto(id = "bitcoin", rank = 1, symbol = "btc", name = "Bitcoin", price = 100.0, change24h = 1.2),
        )

        val result = repository.getTopCryptocurrencies(10)

        assertEquals("bitcoin", result.getOrThrow().single().id)
        assertEquals("Bitcoin", result.getOrThrow().single().name)
    }

    @Test
    fun coinDetailMapUSDData() = runTest {
        coEvery { api.getCoinDetail("bitcoin") } returns CoinDetailDto(
            id = "bitcoin",
            symbol = "btc",
            name = "Bitcoin",
            marketData = CoinMarketDataDto(
                currentPrice = mapOf("usd" to 100.0),
                marketCap = mapOf("usd" to 500.0),
                dailyHigh = mapOf("usd" to 110.0),
                dailyLow = mapOf("usd" to 90.0),
                change24h = 1.5,
            ),
        )

        val result = repository.getCryptoDetail("bitcoin")

        assertEquals(500.0, result.getOrThrow().marketCap ?: 0.0, 0.0)
        assertEquals(90.0, result.getOrThrow().dailyLow ?: 0.0, 0.0)
    }

    @Test
    fun chartPairsHistory() = runTest {
        coEvery { api.getMarketChart(id = "bitcoin", days = 7) } returns MarketChartDto(
            prices = listOf(listOf(1721779200000.0, 118000.0)),
        )

        val result = repository.getPriceHistory("bitcoin", 7)

        assertEquals("1721779200000", result.getOrThrow().single().timestamp)
        assertEquals(118000.0, result.getOrThrow().single().price, 0.0)
    }

    @Test
    fun networkFailures() = runTest {
        coEvery { api.getCoinMarkets(limit = 10, page = 1) } throws IOException("No connection")

        val result = repository.getTopCryptocurrencies(10)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IOException)
    }
}
