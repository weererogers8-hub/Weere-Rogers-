package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CandlestickChart
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WineBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.AssetCategory
import com.example.data.model.MarketAsset
import com.example.ui.components.Formatters
import com.example.ui.components.InteractiveTrendLineChart
import com.example.ui.components.MarketSentimentGauge
import com.example.ui.components.MarketTickerCard
import com.example.ui.components.TradingHeroBanner
import com.example.ui.theme.LocalVinTradeColors
import com.example.ui.viewmodel.RedeemableSelection
import com.example.ui.viewmodel.VinTradeUiState

@Composable
fun TradingPulseScreen(
    state: VinTradeUiState,
    onRefresh: () -> Unit,
    onToggleSimulation: () -> Unit,
    onSelectFeaturedAsset: (String) -> Unit,
    onSelectCategory: (AssetCategory) -> Unit,
    onOpenAsset: (MarketAsset) -> Unit,
    onOpenRedeemable: (RedeemableSelection) -> Unit,
    onNavigateToWines: () -> Unit,
    onNavigateToPrizes: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ext = LocalVinTradeColors.current
    val filteredAssets = if (state.selectedAssetCategory == AssetCategory.ALL) {
        state.marketAssets
    } else {
        state.marketAssets.filter { it.category == state.selectedAssetCategory }
    }
    val featuredAsset = state.marketAssets.find { it.id == state.featuredAssetId }
        ?: state.marketAssets.firstOrNull()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Column(modifier = Modifier.widthIn(max = 640.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "VinTrade Dashboard",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = if (state.isSimulationRunning) {
                                "Simulated Stream Active • Tick #${state.simulationTickCount}"
                            } else {
                                "Stream Paused • ${Formatters.formatTime(state.lastSyncedMillis)}"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (state.isSimulationRunning) ext.bull else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onToggleSimulation,
                            modifier = Modifier.testTag("toggle_simulation_button")
                        ) {
                            Icon(
                                imageVector = if (state.isSimulationRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = if (state.isSimulationRunning) {
                                    "Pause simulated price stream"
                                } else {
                                    "Resume simulated price stream"
                                },
                                tint = ext.gold
                            )
                        }
                        IconButton(
                            onClick = onRefresh,
                            modifier = Modifier.testTag("refresh_market_button")
                        ) {
                            if (state.isRefreshing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = ext.gold
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = "Refresh live market rates",
                                    tint = ext.gold
                                )
                            }
                        }
                    }
                }
            }
        }

        if (featuredAsset != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 640.dp)
                        .testTag("main_trend_line_card"),
                    shape = MaterialTheme.shapes.large,
                    border = BorderStroke(1.dp, ext.gold.copy(alpha = 0.35f)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = ext.goldSubtle,
                                shape = CircleShape
                            ) {
                                Text(
                                    text = if (state.isSimulationRunning) "SIMULATED LIVE TREND" else "PAUSED SNAPSHOT",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ext.gold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            TextButton(onClick = { onOpenAsset(featuredAsset) }) {
                                Icon(
                                    imageVector = Icons.Filled.CandlestickChart,
                                    contentDescription = null,
                                    tint = ext.gold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Candles & Trade", color = ext.gold)
                            }
                        }

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(vertical = 6.dp)
                        ) {
                            items(state.marketAssets, key = { "pill_${it.id}" }) { asset ->
                                FilterChip(
                                    selected = asset.id == featuredAsset.id,
                                    onClick = { onSelectFeaturedAsset(asset.id) },
                                    label = {
                                        Text(
                                            text = asset.symbol,
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    },
                                    modifier = Modifier.testTag("chart_selector_${asset.id}")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        InteractiveTrendLineChart(
                            asset = featuredAsset,
                            isLiveTicking = state.isSimulationRunning,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DashboardMetricChip(
                                label = "RSI (14)",
                                value = String.format("%.1f", featuredAsset.rsi14),
                                modifier = Modifier.weight(1f)
                            )
                            DashboardMetricChip(
                                label = "20-EMA Δ",
                                value = Formatters.formatPercent(featuredAsset.ema20DeltaPercent),
                                modifier = Modifier.weight(1f)
                            )
                            DashboardMetricChip(
                                label = "SIGNAL",
                                value = featuredAsset.signal.label,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        item {
            TradingHeroBanner(
                dataSourceMode = state.dataSourceMode,
                vaultPoints = state.vaultPoints,
                lifetimePoints = state.lifetimePoints,
                currentTier = state.currentTier,
                nextTier = state.nextTier,
                modifier = Modifier.widthIn(max = 640.dp)
            )
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp),
                shape = MaterialTheme.shapes.large,
                border = BorderStroke(1.dp, ext.borderSubtle),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Trading Signal Indicator",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Composite RSI & EMA-20 market breadth",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            color = ext.goldSubtle,
                            shape = CircleShape
                        ) {
                            Text(
                                text = "${state.marketAssets.count { it.change24hPercent > 0 }}/${state.marketAssets.size} Bullish",
                                style = MaterialTheme.typography.labelSmall,
                                color = ext.gold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    MarketSentimentGauge(
                        score = state.sentimentScore,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(AssetCategory.entries) { cat ->
                    FilterChip(
                        selected = cat == state.selectedAssetCategory,
                        onClick = { onSelectCategory(cat) },
                        label = { Text(cat.label) },
                        modifier = Modifier.testTag("asset_filter_${cat.name}")
                    )
                }
            }
        }

        items(filteredAssets, key = { it.id }) { asset ->
            MarketTickerCard(
                asset = asset,
                onClick = { onOpenAsset(asset) },
                modifier = Modifier.widthIn(max = 640.dp)
            )
        }

        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cellar & Prize Highlights",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    TextButton(onClick = onNavigateToWines) {
                        Text("Explore Cellar", color = ext.gold)
                    }
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(state.wines.take(2), key = { it.id }) { wine ->
                        QuickShowcaseChipCard(
                            icon = Icons.Filled.WineBar,
                            eyebrow = "${wine.criticScore} PTS • ${wine.vintage}",
                            title = wine.name,
                            priceLabel = Formatters.formatPrice(wine.marketPriceUsd, "$", 0),
                            pointsLabel = Formatters.formatPoints(wine.pointsCost),
                            onClick = { onOpenRedeemable(RedeemableSelection.Wine(wine)) }
                        )
                    }
                    items(state.prizes.take(2), key = { it.id }) { prize ->
                        QuickShowcaseChipCard(
                            icon = Icons.Filled.CardGiftcard,
                            eyebrow = prize.category,
                            title = prize.title,
                            priceLabel = "Val ${Formatters.formatPrice(prize.estimatedValueUsd, "$", 0)}",
                            pointsLabel = Formatters.formatPoints(prize.pointsRequired),
                            onClick = { onNavigateToPrizes() }
                        )
                    }
                }
            }
        }

        if (state.recentTrades.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 640.dp),
                    shape = MaterialTheme.shapes.medium,
                    border = BorderStroke(1.dp, ext.borderSubtle),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Recent Signal Entries",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        state.recentTrades.take(4).forEach { trade ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${trade.side} ${trade.symbol}",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = if (trade.side == "BUY") ext.bull else ext.bear
                                    )
                                    Text(
                                        text = "${Formatters.formatPrice(trade.notionalUsd, "$", 0)} • ${Formatters.formatDateTime(trade.timestampMillis)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "+${trade.pointsEarned} pts",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = ext.gold
                                )
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }
    }
}

@Composable
private fun DashboardMetricChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
        shape = MaterialTheme.shapes.small
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
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

@Composable
private fun QuickShowcaseChipCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    eyebrow: String,
    title: String,
    priceLabel: String,
    pointsLabel: String,
    onClick: () -> Unit
) {
    val ext = LocalVinTradeColors.current
    Card(
        onClick = onClick,
        modifier = Modifier.width(210.dp),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, ext.borderSubtle),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = ext.gold,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = eyebrow,
                    style = MaterialTheme.typography.labelSmall,
                    color = ext.gold
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = priceLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = pointsLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = ext.bull
                )
            }
        }
    }
}
