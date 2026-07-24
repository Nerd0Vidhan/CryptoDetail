package `in`.mato.cryptodetail

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import `in`.mato.cryptodetail.domain.model.Crypto
import `in`.mato.cryptodetail.domain.model.CryptoDetail
import `in`.mato.cryptodetail.domain.model.HistoryPoint
import `in`.mato.cryptodetail.domain.repository.CryptoRepository
import `in`.mato.cryptodetail.ui.theme.CryptoDetailTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var cryptoRepository: CryptoRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CryptoDetailTheme {
                ApiTestScreen(repository = cryptoRepository)
            }
        }
    }
}

@Composable
private fun ApiTestScreen(repository: CryptoRepository) {
    var listResult by remember { mutableStateOf("") }
    var detailResult by remember { mutableStateOf("") }
    var chartResult by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("API test")

            ScrollableResultField(
                label = "Coin list(/coins/markets)",
                value = listResult,
            )
            ScrollableResultField(
                label = "Coin detail(/coins/{id})",
                value = detailResult,
            )
            ScrollableResultField(
                label = "Price chart(/coins/{id}/market_chart)",
                value = chartResult,
            )

            Button(
                enabled = !isLoading,
                onClick = {
                    scope.launch {
                        isLoading = true
                        listResult = "Loading coin list..."
                        detailResult = "Waiting for list result..."
                        chartResult = "Waiting for list result..."

                        val coinsResult = repository.getTopCryptocurrencies(limit = 10)
                        val coins = coinsResult.getOrElse { error ->
                            val message = error.message ?: "Unknown error"
                            listResult = "List request failed: $message"
                            detailResult = "Not requested because the list failed."
                            chartResult = "Not requested because the list failed."
                            isLoading = false
                            return@launch
                        }

                        listResult = coins.toListDisplayText()
                        val coinId = coins.firstOrNull()?.id
                        if (coinId == null) {
                            detailResult = "No coins returned."
                            chartResult = "No coins returned."
                            isLoading = false
                            return@launch
                        }

                        detailResult = repository.getCryptoDetail(coinId)
                            .fold(
                                onSuccess = CryptoDetail::toDisplayText,
                                onFailure = { "Detail request failed: ${it.message ?: "Unknown error"}" },
                            )
                        chartResult = repository.getPriceHistory(id = coinId, days = 7)
                            .fold(
                                onSuccess = List<HistoryPoint>::toChartDisplayText,
                                onFailure = { "Chart request failed: ${it.message ?: "Unknown error"}" },
                            )
                        isLoading = false
                    }
                },
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.height(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Test APIs")
                }
            }
        }
    }
}

@Composable
private fun ScrollableResultField(label: String, value: String) {
    val scrollState = rememberScrollState()
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .verticalScroll(scrollState),
        maxLines = Int.MAX_VALUE,
    )
}

private fun List<Crypto>.toListDisplayText(): String = joinToString(separator = "\n\n") { coin ->
    "${coin.rank ?: "-"}. ${coin.name} (${coin.symbol.uppercase()})\n" +
        "id=${coin.id}\nprice=$${coin.price}\n24h change=${coin.change24h}%\nmarket cap=${coin.marketCap}\n" +
        "high=${coin.dailyHigh}, low=${coin.dailyLow}"
}

private fun CryptoDetail.toDisplayText(): String = buildString {
    appendLine("$name (${symbol.uppercase()})")
    appendLine("id=$id")
    appendLine("price=$currentPrice")
    appendLine("market cap=$marketCap")
    appendLine("24h high=$dailyHigh")
    appendLine("24h low=$dailyLow")
    appendLine("24h change=$change24h%")
    append("last updated=$lastUpdated")
}

private fun List<HistoryPoint>.toChartDisplayText(): String = joinToString(separator = "\n") { point ->
    "timestamp=${point.timestamp}, price=${point.price}"
}