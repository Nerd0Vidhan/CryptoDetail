package `in`.mato.cryptodetail.presentation.cryptodetail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import `in`.mato.cryptodetail.domain.model.CryptoDetail
import `in`.mato.cryptodetail.domain.model.HistoryPoint
import `in`.mato.cryptodetail.ui.theme.lightGreen
import `in`.mato.cryptodetail.ui.theme.red
import java.text.DateFormat
import java.text.NumberFormat
import java.util.Date
import java.util.Locale
import kotlin.math.pow
import kotlin.math.sqrt

@Composable
fun CryptoDetailRoute(
    onBack: () -> Unit,
    viewModel: CryptoDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    CryptoDetailScreen(
        state = state,
        onBack = onBack,
        onRetry = viewModel::refresh,
        onRangeSelected = viewModel::selectRange,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CryptoDetailScreen(
    state: CryptoDetailUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onRangeSelected: (ChartRange) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.detail?.name ?: "Coin detail") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onRetry, enabled = !state.isLoading) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
    ) { padding ->
        when {
            state.isLoading -> DetailLoading(Modifier.padding(padding))
            state.detail == null -> DetailError(
                modifier = Modifier.padding(padding),
                message = state.errorMessage ?: "Unable to load this cryptocurrency.",
                onRetry = onRetry,
            )
            else -> DetailContent(
                modifier = Modifier.padding(padding),
                detail = state.detail,
                history = state.history,
                chartError = state.errorMessage,
                selectedRange = state.selectedRange,
                isHistoryLoading = state.isHistoryLoading,
                onRangeSelected = onRangeSelected,
            )
        }
    }
}

@Composable
private fun DetailContent(
    modifier: Modifier,
    detail: CryptoDetail,
    history: List<HistoryPoint>,
    chartError: String?,
    selectedRange: ChartRange,
    isHistoryLoading: Boolean,
    onRangeSelected: (ChartRange) -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { CoinSummary(detail) }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (selectedRange == ChartRange.SevenDays) "7-day price movement" else "30-day price movement",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                RangeSelector(
                    selectedRange = selectedRange,
                    isLoading = isHistoryLoading,
                    availablePoints = history.size,
                    onRangeSelected = onRangeSelected,
                )
            }
            Spacer(Modifier.height(8.dp))
            when {
                isHistoryLoading -> ChartLoading()
                history.size >= 2 -> PriceChart(history)
                else -> ChartUnavailable(chartError)
            }
        }
        item {
            Text("Market overview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            MarketMetrics(detail)
        }
    }
}

@Composable
private fun CoinSummary(detail: CryptoDetail) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (detail.imageUrl != null) {
                AsyncImage(
                    model = detail.imageUrl,
                    contentDescription = "${detail.name} logo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape),
                )
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(detail.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(detail.symbol.uppercase(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                Text(detail.currentPrice?.toInr() ?: "—", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun MarketMetrics(detail: CryptoDetail) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        MetricRow("Market cap", detail.marketCap?.toInr())
        MetricRow("24h high", detail.dailyHigh?.toInr())
        MetricRow("24h low", detail.dailyLow?.toInr())
        MetricRow("24h change", detail.change24h?.let { String.format(Locale.US, "%+.2f%%", it) }, highlight = detail.change24h)
        MetricRow("Last updated", detail.lastUpdated ?: "—")
    }
}

@Composable
private fun MetricRow(label: String, value: String?, highlight: Double? = null) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 13.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = value ?: "—",
                fontWeight = FontWeight.SemiBold,
                color = when {
                    highlight == null -> MaterialTheme.colorScheme.onSurface
                    highlight >= 0 -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.error
                },
            )
        }
    }
}

@Composable
private fun PriceChart(history: List<HistoryPoint>) {
    var selectedPoint by remember(history) { mutableStateOf<HistoryPoint?>(null) }
    val surfaceColor = MaterialTheme.colorScheme.surface
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(270.dp)
                .padding(14.dp),
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(history) {
                        detectTapGestures { tap ->
                            val plotted = plotPoints(
                                history,
                                Size(size.width.toFloat(), size.height.toFloat())
                            )
                            val nearest =
                                plotted.minByOrNull { (_, point) -> tap.distanceTo(point) }
                            selectedPoint =
                                nearest?.takeIf { (_, point) -> tap.distanceTo(point) <= 30.dp.toPx() }?.first
                        }
                    },
            ) {
                val points = plotPoints(history, size)
                drawChartGrid()
                drawChartGlow(points)
                drawChartLine(points)
                points.forEachIndexed { index, (historyPoint, point) ->
                    val pointColor = if (index == 0) lightGreen else movementColor(
                        previous = points[index - 1].first.price,
                        current = historyPoint.price,
                    )
                    drawCircle(color = pointColor, radius = if (historyPoint == selectedPoint) 7.dp.toPx() else 4.dp.toPx(), center = point)
                    drawCircle(color = surfaceColor, radius = 2.dp.toPx(), center = point)
                }
            }
            Column(modifier = Modifier.wrapContentSize()) {
                AnimatedVisibility(
                    visible = selectedPoint != null
                ) {
                    selectedPoint?.let { ChartTooltip(it) }
                }
            }
        }
    }
}

@Composable
private fun ChartTooltip(point: HistoryPoint) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(14.dp),
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(point.price.toInr(), color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
            Text(point.timestamp.toReadableDate(), color = MaterialTheme.colorScheme.onPrimaryContainer, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun ChartUnavailable(message: String?) {
    Box(modifier = Modifier
        .fillMaxWidth()
        .height(180.dp), contentAlignment = Alignment.Center) {
        Text(message ?: "Chart data is not available.", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ChartLoading() {
    val transition = rememberInfiniteTransition(label = "chartShimmer")
    val shimmerProgress by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmerProgress",
    )
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(270.dp)
                .padding(14.dp),
        ) {
            drawChartGrid()
            val shimmer = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.04f),
                    Color.White.copy(alpha = 0.20f),
                    Color.White.copy(alpha = 0.04f),
                ),
                start = Offset(size.width * shimmerProgress - size.width, 0f),
                end = Offset(size.width * shimmerProgress, size.height),
            )
            val placeholderPoints = listOf(
                Offset(size.width * 0.04f, size.height * 0.66f),
                Offset(size.width * 0.20f, size.height * 0.44f),
                Offset(size.width * 0.36f, size.height * 0.58f),
                Offset(size.width * 0.52f, size.height * 0.30f),
                Offset(size.width * 0.70f, size.height * 0.48f),
                Offset(size.width * 0.96f, size.height * 0.22f),
            )
            val path = curvedPath(placeholderPoints)
            val fill = Path().apply {
                addPath(path)
                lineTo(placeholderPoints.last().x, size.height)
                lineTo(placeholderPoints.first().x, size.height)
                close()
            }
            drawPath(path = fill, brush = shimmer)
            drawPath(path = path, brush = shimmer, style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round))
        }
    }
}

@Composable
private fun RangeSelector(
    selectedRange: ChartRange,
    isLoading: Boolean,
    availablePoints: Int,
    onRangeSelected: (ChartRange) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        ChartRange.entries.forEach { range ->
            val label = if (range == ChartRange.ThirtyDays && availablePoints in 1..29) "MAX" else range.label
            if (range == selectedRange) {
                Button(onClick = {}, enabled = false, modifier = Modifier.height(34.dp)) { Text(label) }
            } else {
                OutlinedButton(
                    onClick = { onRangeSelected(range) },
                    enabled = !isLoading,
                    modifier = Modifier.height(34.dp),
                ) { Text(label) }
            }
        }
    }
}

@Composable
private fun DetailLoading(modifier: Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
}

@Composable
private fun DetailError(modifier: Modifier, message: String, onRetry: () -> Unit) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(onClick = onRetry) { Text("Retry") }
        }
    }
}

private fun DrawScope.drawChartGrid() {
    repeat(4) { index ->
        val y = size.height * (index + 1) / 5f
        drawLine(
            color = Color.White.copy(alpha = 0.08f),
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 1.dp.toPx(),
        )
    }
}

private fun DrawScope.drawChartGlow(points: List<Pair<HistoryPoint, Offset>>) {
    if (points.size < 2) return
    val baseline = size.height
    for (index in 1 until points.size) {
        val previous = points[index - 1]
        val current = points[index]
        val color = movementColor(previous.first.price, current.first.price)
        val linePath = curvedSegmentPath(previous.second, current.second)
        val fillPath = Path().apply {
            addPath(linePath)
            lineTo(current.second.x, baseline)
            lineTo(previous.second.x, baseline)
            close()
        }
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(color.copy(alpha = 0.28f), color.copy(alpha = 0.01f)),
                startY = 0f,
                endY = baseline,
            ),
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color.copy(alpha = 0.18f), Color.Transparent),
                center = Offset((previous.second.x + current.second.x) / 2f, baseline),
                radius = (current.second.x - previous.second.x) * 1.4f,
            ),
            center = Offset((previous.second.x + current.second.x) / 2f, baseline),
            radius = (current.second.x - previous.second.x) * 1.4f,
        )
    }
}

private fun DrawScope.drawChartLine(points: List<Pair<HistoryPoint, Offset>>) {
    if (points.size < 2) return
    for (index in 1 until points.size) {
        val previous = points[index - 1]
        val current = points[index]
        drawPath(
            path = curvedSegmentPath(previous.second, current.second),
            color = movementColor(previous.first.price, current.first.price),
            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round),
        )
    }
}

private fun curvedSegmentPath(start: Offset, end: Offset): Path = Path().apply {
    moveTo(start.x, start.y)
    val midpointX = (start.x + end.x) / 2f
    cubicTo(midpointX, start.y, midpointX, end.y, end.x, end.y)
}

private fun movementColor(previous: Double, current: Double): Color =
    if (current >= previous) lightGreen else red

private fun curvedPath(points: List<Offset>): Path = Path().apply {
    if (points.isEmpty()) return@apply
    moveTo(points.first().x, points.first().y)
    for (index in 1 until points.size) {
        val previous = points[index - 1]
        val current = points[index]
        val midpointX = (previous.x + current.x) / 2f
        cubicTo(midpointX, previous.y, midpointX, current.y, current.x, current.y)
    }
}

private fun plotPoints(history: List<HistoryPoint>, size: Size): List<Pair<HistoryPoint, Offset>> {
    val leftPadding = size.width * 0.04f
    val rightPadding = size.width * 0.04f
    val topPadding = size.height * 0.10f
    val bottomPadding = size.height * 0.10f
    val minPrice = history.minOf { it.price }
    val maxPrice = history.maxOf { it.price }
    val range = (maxPrice - minPrice).takeIf { it > 0 } ?: 1.0
    return history.mapIndexed { index, point ->
        val x = if (history.size == 1) size.width / 2f else leftPadding +
            (size.width - leftPadding - rightPadding) * index / (history.size - 1)
        val normalizedY = ((point.price - minPrice) / range).toFloat()
        val y = size.height - bottomPadding - normalizedY * (size.height - topPadding - bottomPadding)
        point to Offset(x, y)
    }
}

private fun Offset.distanceTo(other: Offset): Float = sqrt((x - other.x).pow(2) + (y - other.y).pow(2))

private fun Double.toInr(): String = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
    currency = java.util.Currency.getInstance("INR")
    maximumFractionDigits = 2
}.format(this)

private fun String.toReadableDate(): String = runCatching {
    DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.US).format(Date(toLong()))
}.getOrDefault(this)
