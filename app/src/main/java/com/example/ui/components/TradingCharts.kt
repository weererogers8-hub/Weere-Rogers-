package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.CandlePoint
import com.example.data.model.MarketAsset
import com.example.ui.theme.LocalVinTradeColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

@Composable
fun InteractiveTrendLineChart(
    asset: MarketAsset,
    isLiveTicking: Boolean,
    modifier: Modifier = Modifier
) {
    val ext = LocalVinTradeColors.current
    val points = asset.sparkline
    val isPositive = asset.change24hPercent >= 0
    val trendColor = if (isPositive) ext.bull else ext.bear
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)

    var scrubbedIndex by remember(asset.id) { mutableStateOf<Int?>(null) }
    val pulseTransition = rememberInfiniteTransition(label = "live_dot")
    val pulseRadius by pulseTransition.animateFloat(
        initialValue = 5f,
        targetValue = 13f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_radius"
    )

    val minVal = points.minOrNull() ?: asset.currentPrice
    val maxVal = points.maxOrNull() ?: asset.currentPrice
    val activeIdx = scrubbedIndex?.coerceIn(0, (points.size - 1).coerceAtLeast(0))
    val inspectedPrice = activeIdx?.let { points.getOrNull(it) } ?: asset.currentPrice

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (activeIdx != null) "INSPECTED TICK #${activeIdx + 1}" else "REAL-TIME TREND LINE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = Formatters.formatPrice(inspectedPrice, asset.unitPrefix, asset.decimals),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Surface(
                color = trendColor.copy(alpha = 0.16f),
                shape = CircleShape,
                border = BorderStroke(1.dp, trendColor.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (isLiveTicking) trendColor else ext.gold)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = Formatters.formatPercent(asset.change24hPercent),
                        style = MaterialTheme.typography.labelMedium,
                        color = trendColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .pointerInput(points) {
                    detectTapGestures(
                        onPress = { offset ->
                            if (points.size > 1) {
                                val idx = ((offset.x / size.width) * (points.size - 1))
                                    .toInt()
                                    .coerceIn(0, points.lastIndex)
                                scrubbedIndex = idx
                                tryAwaitRelease()
                                scrubbedIndex = null
                            }
                        }
                    )
                }
                .pointerInput(points) {
                    detectHorizontalDragGestures(
                        onDragEnd = { scrubbedIndex = null },
                        onDragCancel = { scrubbedIndex = null },
                        onHorizontalDrag = { change, _ ->
                            if (points.size > 1) {
                                val idx = ((change.position.x / size.width) * (points.size - 1))
                                    .toInt()
                                    .coerceIn(0, points.lastIndex)
                                scrubbedIndex = idx
                            }
                        }
                    )
                }
        ) {
            if (points.size < 2) return@Canvas
            val range = (maxVal - minVal).takeIf { it > 0.0 } ?: 1.0
            val padTop = size.height * 0.10f
            val padBottom = size.height * 0.12f
            val drawHeight = (size.height - padTop - padBottom).coerceAtLeast(1f)
            val stepX = size.width / (points.size - 1).coerceAtLeast(1)

            fun yFor(value: Double): Float {
                val norm = ((value - minVal) / range).toFloat()
                return padTop + drawHeight * (1f - norm)
            }

            // Horizontal dashed price guides
            repeat(3) { step ->
                val y = padTop + drawHeight * (step / 2f)
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                )
            }

            val linePath = Path()
            val fillPath = Path()
            val emaPath = Path()
            var ema = points.first()
            val k = 2.0 / 7.0
            var lastPointOffset = Offset.Zero

            points.forEachIndexed { idx, value ->
                val x = idx * stepX
                val y = yFor(value)
                ema = value * k + ema * (1.0 - k)
                val emaY = yFor(ema)

                if (idx == 0) {
                    linePath.moveTo(x, y)
                    emaPath.moveTo(x, emaY)
                    fillPath.moveTo(x, size.height)
                    fillPath.lineTo(x, y)
                } else {
                    linePath.lineTo(x, y)
                    emaPath.lineTo(x, emaY)
                    fillPath.lineTo(x, y)
                }
                if (idx == points.lastIndex) {
                    lastPointOffset = Offset(x, y)
                }
            }
            fillPath.lineTo(size.width, size.height)
            fillPath.close()

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        trendColor.copy(alpha = 0.30f),
                        Color.Transparent
                    )
                )
            )

            // 20-EMA gold guide line
            drawPath(
                path = emaPath,
                color = ext.gold.copy(alpha = 0.78f),
                style = Stroke(
                    width = 1.6.dp.toPx(),
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
                )
            )

            // Primary price trend line
            drawPath(
                path = linePath,
                color = trendColor,
                style = Stroke(
                    width = 2.8.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // Scrubbed crosshair or live terminal pulse
            if (activeIdx != null) {
                val x = activeIdx * stepX
                val y = yFor(points[activeIdx])
                drawLine(
                    color = ext.gold.copy(alpha = 0.65f),
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 1.2.dp.toPx()
                )
                drawCircle(color = ext.gold, radius = 6.dp.toPx(), center = Offset(x, y))
            } else {
                if (isLiveTicking) {
                    drawCircle(
                        color = trendColor.copy(alpha = 0.25f),
                        radius = pulseRadius.dp.toPx(),
                        center = lastPointOffset
                    )
                }
                drawCircle(
                    color = trendColor,
                    radius = 4.5.dp.toPx(),
                    center = lastPointOffset
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Low ${Formatters.formatPrice(minVal, asset.unitPrefix, asset.decimals)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "--- EMA Trend",
                style = MaterialTheme.typography.labelSmall,
                color = ext.gold
            )
            Text(
                text = "High ${Formatters.formatPrice(maxVal, asset.unitPrefix, asset.decimals)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MarketSentimentGauge(
    score: Int,
    modifier: Modifier = Modifier
) {
    val ext = LocalVinTradeColors.current
    val animatedScore by animateFloatAsState(
        targetValue = score.coerceIn(0, 100).toFloat(),
        animationSpec = tween(durationMillis = 650),
        label = "sentiment_gauge"
    )
    val trackBg = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
    val label = when {
        score >= 70 -> "Strong Bullish"
        score >= 55 -> "Accumulation"
        score >= 42 -> "Balanced Range"
        else -> "Defensive"
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(112.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 14.dp.toPx()
                val diameter = min(size.width * 0.85f, size.height * 1.65f)
                val arcSize = Size(diameter, diameter)
                val topLeft = Offset(
                    x = (size.width - diameter) / 2f,
                    y = size.height - diameter / 2f - 8.dp.toPx()
                )

                drawArc(
                    color = trackBg,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                val sweep = (animatedScore / 100f) * 180f
                drawArc(
                    brush = Brush.horizontalGradient(
                        colors = listOf(ext.bear, ext.gold, ext.bull)
                    ),
                    startAngle = 180f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                val angleRad = ((180f + sweep) * PI / 180.0)
                val radius = diameter / 2f
                val center = Offset(topLeft.x + radius, topLeft.y + radius)
                val tip = Offset(
                    x = center.x + (radius * 0.78f * cos(angleRad)).toFloat(),
                    y = center.y + (radius * 0.78f * sin(angleRad)).toFloat()
                )
                drawLine(
                    color = ext.gold,
                    start = center,
                    end = tip,
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawCircle(
                    color = ext.gold,
                    radius = 6.dp.toPx(),
                    center = center
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$score",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = ext.gold
                )
            }
        }
    }
}

@Composable
fun SparklineChart(
    points: List<Double>,
    isPositive: Boolean,
    modifier: Modifier = Modifier
) {
    val ext = LocalVinTradeColors.current
    val strokeColor = if (isPositive) ext.bull else ext.bear

    Canvas(modifier = modifier) {
        if (points.size < 2) return@Canvas
        val minVal = points.minOrNull() ?: return@Canvas
        val maxVal = points.maxOrNull() ?: return@Canvas
        val range = (maxVal - minVal).takeIf { it > 0.0 } ?: 1.0

        val linePath = Path()
        val fillPath = Path()
        val stepX = size.width / (points.size - 1).coerceAtLeast(1)

        points.forEachIndexed { idx, value ->
            val x = idx * stepX
            val normalizedY = ((value - minVal) / range).toFloat()
            val y = size.height - (normalizedY * size.height * 0.82f + size.height * 0.09f)
            if (idx == 0) {
                linePath.moveTo(x, y)
                fillPath.moveTo(x, size.height)
                fillPath.lineTo(x, y)
            } else {
                linePath.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }
        fillPath.lineTo(size.width, size.height)
        fillPath.close()

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    strokeColor.copy(alpha = 0.28f),
                    Color.Transparent
                )
            )
        )
        drawPath(
            path = linePath,
            color = strokeColor,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
fun InteractiveCandlestickChart(
    candles: List<CandlePoint>,
    unitPrefix: String,
    decimals: Int,
    modifier: Modifier = Modifier
) {
    val ext = LocalVinTradeColors.current
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    var selectedIdx by remember(candles) { mutableIntStateOf((candles.size - 1).coerceAtLeast(0)) }

    Column(modifier = modifier) {
        val activeCandle = candles.getOrNull(selectedIdx)
        if (activeCandle != null) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OhlcStat("OPEN", Formatters.formatPrice(activeCandle.open, unitPrefix, decimals))
                    OhlcStat("HIGH", Formatters.formatPrice(activeCandle.high, unitPrefix, decimals))
                    OhlcStat("LOW", Formatters.formatPrice(activeCandle.low, unitPrefix, decimals))
                    OhlcStat(
                        "CLOSE",
                        Formatters.formatPrice(activeCandle.close, unitPrefix, decimals),
                        highlight = if (activeCandle.close >= activeCandle.open) ext.bull else ext.bear
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .pointerInput(candles) {
                    detectTapGestures { offset ->
                        if (candles.isNotEmpty()) {
                            val rawIdx = ((offset.x / size.width) * candles.size).toInt()
                            selectedIdx = rawIdx.coerceIn(0, candles.lastIndex)
                        }
                    }
                }
        ) {
            if (candles.isEmpty()) return@Canvas
            val minPrice = candles.minOf { it.low }
            val maxPrice = candles.maxOf { it.high }
            val range = (maxPrice - minPrice).takeIf { it > 0.0 } ?: 1.0

            fun yFor(price: Double): Float {
                val norm = ((price - minPrice) / range).toFloat()
                return size.height - (norm * size.height * 0.84f + size.height * 0.08f)
            }

            repeat(3) { step ->
                val y = size.height * (0.2f + step * 0.3f)
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                )
            }

            val slotWidth = size.width / candles.size.coerceAtLeast(1)
            val bodyWidth = max(6.dp.toPx(), slotWidth * 0.52f)
            val emaPath = Path()
            var ema = candles.first().close
            val k = 2.0 / 6.0

            candles.forEachIndexed { idx, c ->
                val centerX = slotWidth * idx + slotWidth / 2f
                val isBull = c.close >= c.open
                val candleColor = if (isBull) ext.bull else ext.bear

                if (idx == selectedIdx) {
                    drawRoundRect(
                        color = ext.gold.copy(alpha = 0.12f),
                        topLeft = Offset(slotWidth * idx, 0f),
                        size = Size(slotWidth, size.height),
                        cornerRadius = CornerRadius(6.dp.toPx())
                    )
                }

                drawLine(
                    color = candleColor,
                    start = Offset(centerX, yFor(c.high)),
                    end = Offset(centerX, yFor(c.low)),
                    strokeWidth = 1.8.dp.toPx(),
                    cap = StrokeCap.Round
                )

                val topY = yFor(max(c.open, c.close))
                val bottomY = yFor(min(c.open, c.close))
                val bodyHeight = max(3.dp.toPx(), bottomY - topY)
                drawRoundRect(
                    color = candleColor,
                    topLeft = Offset(centerX - bodyWidth / 2f, topY),
                    size = Size(bodyWidth, bodyHeight),
                    cornerRadius = CornerRadius(2.dp.toPx())
                )

                ema = c.close * k + ema * (1.0 - k)
                val emaY = yFor(ema)
                if (idx == 0) emaPath.moveTo(centerX, emaY) else emaPath.lineTo(centerX, emaY)
            }

            drawPath(
                path = emaPath,
                color = ext.gold,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
private fun OhlcStat(
    label: String,
    value: String,
    highlight: Color = MaterialTheme.colorScheme.onSurface
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = highlight
        )
    }
}
