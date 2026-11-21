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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.model.CollectorTier
import com.example.data.model.PrizeItem
import com.example.ui.components.Formatters
import com.example.ui.components.PrizeVaultCard
import com.example.ui.theme.LocalVinTradeColors
import com.example.ui.viewmodel.VinTradeUiState

@Composable
fun PrizesVaultScreen(
    state: VinTradeUiState,
    onOpenPrize: (PrizeItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val ext = LocalVinTradeColors.current

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
            ) {
                Text(
                    text = "Prizes & Collector Vault",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Unlock milestone trophies, reserve allocations & estate getaways",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(CollectorTier.entries) { tier ->
                        val isCurrent = tier == state.currentTier
                        val isReached = state.lifetimePoints >= tier.minPoints
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = if (isCurrent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(
                                width = if (isCurrent) 1.5.dp else 1.dp,
                                color = if (isCurrent) ext.gold else ext.borderSubtle
                            )
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isReached) Icons.Filled.CheckCircle else Icons.Filled.EmojiEvents,
                                        contentDescription = null,
                                        tint = if (isReached) ext.bull else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = " ${tier.title}",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${Formatters.formatPoints(tier.minPoints)} • ${tier.bonusMultiplier}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isCurrent) ext.gold else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        items(state.prizes, key = { it.id }) { prize ->
            PrizeVaultCard(
                prize = prize,
                vaultPoints = state.vaultPoints,
                onClick = { onOpenPrize(prize) },
                modifier = Modifier.widthIn(max = 640.dp)
            )
        }

        if (state.claimedRewards.isNotEmpty()) {
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
                            text = "Claimed Vault Ledger",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        state.claimedRewards.forEach { claimed ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = claimed.itemTitle,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${claimed.recipientNote} • ${Formatters.formatDateTime(claimed.claimedAtMillis)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Surface(
                                    color = ext.goldSubtle,
                                    shape = CircleShape
                                ) {
                                    Text(
                                        text = "-${claimed.pointsSpent} pts",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ext.gold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }
    }
}
