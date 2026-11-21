package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.WineBar
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.CollectorTier
import com.example.data.model.DataSourceMode
import com.example.data.model.GiftItem
import com.example.data.model.MarketAsset
import com.example.data.model.PrizeItem
import com.example.data.model.SignalType
import com.example.data.model.WineItem
import com.example.ui.theme.LocalVinTradeColors

@Composable
fun TradingHeroBanner(
    dataSourceMode: DataSourceMode,
    vaultPoints: Int,
    lifetimePoints: Int,
    currentTier: CollectorTier,
    nextTier: CollectorTier?,
    modifier: Modifier = Modifier
) {
    val ext = LocalVinTradeColors.current
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, ext.gold.copy(alpha = 0.35f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(188.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_trading_cellar_hero),
                contentDescription = "VinTrade cellar trading exchange banner",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.35f),
                                Color(0xFF14090D).copy(alpha = 0.92f)
                            )
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = if (dataSourceMode == DataSourceMode.LIVE) {
                            ext.bull.copy(alpha = 0.22f)
                        } else {
                            ext.gold.copy(alpha = 0.22f)
                        },
                        shape = CircleShape,
                        border = BorderStroke(
                            1.dp,
                            if (dataSourceMode == DataSourceMode.LIVE) ext.bull else ext.gold
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (dataSourceMode == DataSourceMode.LIVE) ext.bull else ext.gold
                                    )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = dataSourceMode.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }
                    }

                    Surface(
                        color = Color.Black.copy(alpha = 0.48f),
                        shape = CircleShape,
                        border = BorderStroke(1.dp, ext.gold.copy(alpha = 0.45f))
                    ) {
                        Text(
                            text = currentTier.title,
                            style = MaterialTheme.typography.labelLarge,
                            color = ext.gold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = "CELLAR & EXCHANGE",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.72f)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = Formatters.formatPoints(vaultPoints),
                                style = MaterialTheme.typography.displaySmall,
                                color = Color.White
                            )
                            Text(
                                text = "Redeemable for fine wines, gifts & vault prizes",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.80f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    if (nextTier != null) {
                        val span = (nextTier.minPoints - currentTier.minPoints).coerceAtLeast(1)
                        val progress = ((lifetimePoints - currentTier.minPoints).toFloat() / span).coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape),
                            color = ext.gold,
                            trackColor = Color.White.copy(alpha = 0.18f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${(nextTier.minPoints - lifetimePoints).coerceAtLeast(0)} pts to ${nextTier.title}",
                            style = MaterialTheme.typography.labelSmall,
                            color = ext.gold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MarketTickerCard(
    asset: MarketAsset,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ext = LocalVinTradeColors.current
    val isPositive = asset.change24hPercent >= 0
    val trendColor = if (isPositive) ext.bull else ext.bear
    val signalColor = when (asset.signal) {
        SignalType.STRONG_BUY -> ext.bull
        SignalType.ACCUMULATE -> ext.gold
        SignalType.NEUTRAL -> MaterialTheme.colorScheme.onSurfaceVariant
        SignalType.TAKE_PROFIT -> ext.bear
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("market_card_${asset.id}")
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, ext.borderSubtle),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1.15f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = asset.symbol,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        color = signalColor.copy(alpha = 0.16f),
                        shape = CircleShape
                    ) {
                        Text(
                            text = asset.signal.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = signalColor,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = asset.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "RSI ${asset.rsi14.toInt()} • ${asset.volumeLabel}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            SparklineChart(
                points = asset.sparkline,
                isPositive = isPositive,
                modifier = Modifier
                    .width(68.dp)
                    .height(36.dp)
            )

            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.weight(0.75f)
            ) {
                Text(
                    text = Formatters.formatPrice(asset.currentPrice, asset.unitPrefix, asset.decimals),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isPositive) {
                            Icons.AutoMirrored.Filled.TrendingUp
                        } else {
                            Icons.AutoMirrored.Filled.TrendingDown
                        },
                        contentDescription = null,
                        tint = trendColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = Formatters.formatPercent(asset.change24hPercent),
                        style = MaterialTheme.typography.labelMedium,
                        color = trendColor
                    )
                }
            }
        }
    }
}

@Composable
fun WineShowcaseCard(
    wine: WineItem,
    isWishlisted: Boolean,
    onToggleWishlist: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ext = LocalVinTradeColors.current
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("wine_card_${wine.id}")
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, ext.borderSubtle),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(172.dp)
            ) {
                Image(
                    painter = painterResource(id = wine.imageRes),
                    contentDescription = "${wine.name} ${wine.vintage}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.72f))
                            )
                        )
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.65f),
                        shape = CircleShape,
                        border = BorderStroke(1.dp, ext.gold.copy(alpha = 0.6f))
                    ) {
                        Text(
                            text = "${wine.criticScore} PTS • ${wine.vintage}",
                            style = MaterialTheme.typography.labelSmall,
                            color = ext.gold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                    IconButton(
                        onClick = onToggleWishlist,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.45f))
                            .testTag("wishlist_wine_${wine.id}")
                    ) {
                        Icon(
                            imageVector = if (isWishlisted) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Save wine to wishlist",
                            tint = if (isWishlisted) ext.bear else Color.White
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.WineBar,
                        contentDescription = null,
                        tint = ext.gold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${wine.region} • ${wine.investmentGrade}",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White
                    )
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = wine.name,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${wine.estate} • ${wine.varietal}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = Formatters.formatPrice(wine.marketPriceUsd, "$", 0),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Appreciation ${Formatters.formatPercent(wine.changePercent)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = ext.bull
                        )
                    }
                    Button(
                        onClick = onClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ext.gold,
                            contentColor = Color(0xFF1A0D10)
                        ),
                        modifier = Modifier.testTag("redeem_wine_${wine.id}")
                    ) {
                        Text(
                            text = Formatters.formatPoints(wine.pointsCost),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GiftShowcaseCard(
    gift: GiftItem,
    isWishlisted: Boolean,
    onToggleWishlist: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ext = LocalVinTradeColors.current
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("gift_card_${gift.id}")
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, ext.borderSubtle),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(164.dp)
            ) {
                Image(
                    painter = painterResource(id = gift.imageRes),
                    contentDescription = gift.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f))
                            )
                        )
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        color = ext.wineAccent,
                        shape = CircleShape
                    ) {
                        Text(
                            text = gift.badge,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                    IconButton(
                        onClick = onToggleWishlist,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.45f))
                    ) {
                        Icon(
                            imageVector = if (isWishlisted) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Save gift to wishlist",
                            tint = if (isWishlisted) ext.bear else Color.White
                        )
                    }
                }
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = gift.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = gift.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Retail ${Formatters.formatPrice(gift.priceUsd, "$", 0)}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Gift box & ribbon included",
                            style = MaterialTheme.typography.labelSmall,
                            color = ext.gold
                        )
                    }
                    Button(
                        onClick = onClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CardGiftcard,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = Formatters.formatPoints(gift.pointsCost),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PrizeVaultCard(
    prize: PrizeItem,
    vaultPoints: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ext = LocalVinTradeColors.current
    val unlocked = vaultPoints >= prize.pointsRequired
    val progress = (vaultPoints.toFloat() / prize.pointsRequired.coerceAtLeast(1)).coerceIn(0f, 1f)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("prize_card_${prize.id}")
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(
            width = if (prize.isGrandPrize) 1.5.dp else 1.dp,
            color = if (prize.isGrandPrize) ext.gold else ext.borderSubtle
        ),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(156.dp)
            ) {
                Image(
                    painter = painterResource(id = prize.imageRes),
                    contentDescription = prize.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.78f))
                            )
                        )
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        color = if (prize.isGrandPrize) ext.gold else Color.Black.copy(alpha = 0.6f),
                        shape = CircleShape
                    ) {
                        Text(
                            text = prize.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (prize.isGrandPrize) Color(0xFF190C10) else Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                    Surface(
                        color = Color.Black.copy(alpha = 0.65f),
                        shape = CircleShape
                    ) {
                        Text(
                            text = "Value ${Formatters.formatPrice(prize.estimatedValueUsd, "$", 0)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = ext.gold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (unlocked) Icons.Filled.EmojiEvents else Icons.Filled.Lock,
                        contentDescription = null,
                        tint = if (unlocked) ext.bull else ext.gold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (unlocked) "Ready to Claim" else "${prize.tierRequired.title} Milestone",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White
                    )
                }
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = prize.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = prize.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = if (unlocked) ext.bull else ext.gold,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${Formatters.formatPoints(vaultPoints.coerceAtMost(prize.pointsRequired))} / ${Formatters.formatPoints(prize.pointsRequired)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = onClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (unlocked) ext.bull else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (unlocked) Color(0xFF0A1914) else MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text(
                            text = if (unlocked) "Claim Prize" else "Inspect",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}
