package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.data.model.MarketAsset
import com.example.data.model.Timeframe
import com.example.ui.theme.LocalVinTradeColors
import com.example.ui.viewmodel.RedeemableSelection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetDetailSheet(
    asset: MarketAsset,
    selectedTimeframe: Timeframe,
    onSelectTimeframe: (Timeframe) -> Unit,
    onExecuteTrade: (side: String, notionalUsd: Double) -> Unit,
    onDismiss: () -> Unit
) {
    val ext = LocalVinTradeColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedNotional by remember { mutableDoubleStateOf(1000.0) }
    val notionals = listOf(500.0, 1000.0, 2500.0)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = asset.symbol,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = asset.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = Formatters.formatPrice(asset.currentPrice, asset.unitPrefix, asset.decimals),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = Formatters.formatPercent(asset.change24hPercent),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (asset.change24hPercent >= 0) ext.bull else ext.bear
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Timeframe.entries.forEach { tf ->
                    FilterChip(
                        selected = tf == selectedTimeframe,
                        onClick = { onSelectTimeframe(tf) },
                        label = { Text(tf.label, style = MaterialTheme.typography.labelMedium) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val displayedCandles = when (selectedTimeframe) {
                Timeframe.H1 -> asset.candles.takeLast(8)
                Timeframe.D1 -> asset.candles
                Timeframe.W1 -> asset.candles.reversed().chunked(1).map { it.first() }.reversed()
                Timeframe.M1 -> asset.candles
            }

            InteractiveCandlestickChart(
                candles = displayedCandles,
                unitPrefix = asset.unitPrefix,
                decimals = asset.decimals,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IndicatorMetricPill(
                    label = "RSI (14)",
                    value = String.format("%.1f", asset.rsi14),
                    modifier = Modifier.weight(1f)
                )
                IndicatorMetricPill(
                    label = "20-EMA Δ",
                    value = Formatters.formatPercent(asset.ema20DeltaPercent),
                    modifier = Modifier.weight(1f)
                )
                IndicatorMetricPill(
                    label = "SIGNAL",
                    value = asset.signal.label,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Practice Signal Entry (Earns Vault Points)",
                style = MaterialTheme.typography.titleSmall,
                color = ext.gold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                notionals.forEach { amount ->
                    FilterChip(
                        selected = selectedNotional == amount,
                        onClick = { selectedNotional = amount },
                        label = { Text(Formatters.formatPrice(amount, "$", 0)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { onExecuteTrade("BUY", selectedNotional) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ext.bull,
                        contentColor = Color(0xFF091B15)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("practice_buy_button")
                ) {
                    Text("Buy Signal (+${(selectedNotional / 100).toInt()} pts)")
                }
                OutlinedButton(
                    onClick = { onExecuteTrade("SELL", selectedNotional) },
                    border = BorderStroke(1.dp, ext.bear),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ext.bear),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("practice_sell_button")
                ) {
                    Text("Short Signal (+${(selectedNotional / 100).toInt()} pts)")
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun IndicatorMetricPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
        shape = MaterialTheme.shapes.small
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemRedeemSheet(
    selection: RedeemableSelection,
    vaultPoints: Int,
    onClaim: (id: String, title: String, type: String, cost: Int, note: String) -> Unit,
    onDismiss: () -> Unit
) {
    val ext = LocalVinTradeColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var recipientNote by remember(selection) { mutableStateOf("") }

    val (id, title, subtitle, type, cost, priceUsd, description, imageRes) = when (selection) {
        is RedeemableSelection.Wine -> Octet(
            selection.item.id,
            "${selection.item.name} (${selection.item.vintage})",
            "${selection.item.estate} • ${selection.item.region}",
            "WINE",
            selection.item.pointsCost,
            selection.item.marketPriceUsd,
            selection.item.tastingNotes,
            selection.item.imageRes
        )
        is RedeemableSelection.Gift -> Octet(
            selection.item.id,
            selection.item.title,
            selection.item.subtitle,
            "GIFT",
            selection.item.pointsCost,
            selection.item.priceUsd,
            selection.item.description,
            selection.item.imageRes
        )
        is RedeemableSelection.Prize -> Octet(
            selection.item.id,
            selection.item.title,
            selection.item.subtitle,
            "PRIZE",
            selection.item.pointsRequired,
            selection.item.estimatedValueUsd,
            selection.item.description,
            selection.item.imageRes
        )
    }

    val canAfford = vaultPoints >= cost

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(176.dp)
                    .clip(MaterialTheme.shapes.medium)
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = ext.gold
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ESTIMATED VALUE",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = Formatters.formatPrice(priceUsd, "$", 0),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Surface(
                        color = ext.goldSubtle,
                        shape = CircleShape
                    ) {
                        Text(
                            text = Formatters.formatPoints(cost),
                            style = MaterialTheme.typography.labelLarge,
                            color = ext.gold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            OutlinedTextField(
                value = recipientNote,
                onValueChange = { recipientNote = it },
                label = { Text("Recipient or Gift Card Note (Optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { onClaim(id, title, type, cost, recipientNote) },
                enabled = canAfford,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ext.gold,
                    contentColor = Color(0xFF190C10)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("confirm_redeem_button")
            ) {
                Text(
                    text = if (canAfford) {
                        "Redeem for ${Formatters.formatPoints(cost)}"
                    } else {
                        "Need ${Formatters.formatPoints(cost - vaultPoints)} More"
                    },
                    style = MaterialTheme.typography.titleSmall
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private data class Octet<A, B, C, D, E, F, G, H>(
    val a: A,
    val b: B,
    val c: C,
    val d: D,
    val e: E,
    val f: F,
    val g: G,
    val h: H
)
