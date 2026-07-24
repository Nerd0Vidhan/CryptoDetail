package `in`.mato.cryptodetail.data.remote

import kotlinx.coroutines.test.runTest
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CryptoApiServiceTest {
    private lateinit var server: MockWebServer
    private lateinit var service: CryptoApiService

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        service = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CryptoApiService::class.java)
    }

    @After
    fun tearDown() {
        server.close()
    }

    @Test
    fun getMarketsRequestsRanked() = runTest {
        server.enqueue(
            MockResponse.Builder()
                .addHeader("Content-Type", "application/json")
                .body("""[{"id":"bitcoin","market_cap_rank":1,"symbol":"btc","name":"Bitcoin","image":"https://image","current_price":94250.45,"price_change_percentage_24h":2.45,"market_cap":1850000000000,"high_24h":95000,"low_24h":92000}]""")
                .build(),
        )

        val result = service.getCoinMarkets(limit = 20, page = 1)

        assertEquals("bitcoin", result.single().id)
        assertEquals(2.45, result.single().change24h ?: 0.0, 0.0)
        assertEquals(
            "/coins/markets?vs_currency=usd&order=market_cap_desc&per_page=20&page=1&sparkline=false&price_change_percentage=24h",
            server.takeRequest().target,
        )
    }

    @Test
    fun getCoinDetail() = runTest {
        server.enqueue(
            MockResponse.Builder()
                .addHeader("Content-Type", "application/json")
                .body("""{"id":"bitcoin","symbol":"btc","name":"Bitcoin","image":{"large":"https://large"},"market_data":{"current_price":{"usd":94250.45},"market_cap":{"usd":1850000000000},"high_24h":{"usd":95000},"low_24h":{"usd":92000},"price_change_percentage_24h":2.45},"last_updated":"2026-07-24T10:00:00Z"}""")
                .build(),
        )

        val result = service.getCoinDetail("bitcoin")

        assertEquals(94250.45, result.marketData?.currentPrice?.get("usd") ?: 0.0, 0.0)
        assertEquals("/coins/bitcoin?localization=false&tickers=false&market_data=true&community_data=false&developer_data=false&sparkline=false", server.takeRequest().target)
    }

    @Test
    fun getMarketChart() = runTest {
        server.enqueue(
            MockResponse.Builder()
                .addHeader("Content-Type", "application/json")
                .body("""{"prices":[[1721779200000,118000.0],[1721865600000,119000.0]]}""")
                .build(),
        )

        val result = service.getMarketChart(id = "bitcoin", days = 7)

        assertEquals(119000.0, result.prices.last()[1], 0.0)
        assertEquals("/coins/bitcoin/market_chart?vs_currency=usd&days=7&interval=daily", server.takeRequest().target)
    }
}
