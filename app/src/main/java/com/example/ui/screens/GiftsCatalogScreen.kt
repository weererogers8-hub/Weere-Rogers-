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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.model.GiftCategory
import com.example.data.model.GiftItem
import com.example.ui.components.Formatters
import com.example.ui.components.GiftShowcaseCard
import com.example.ui.theme.LocalVinTradeColors
import com.example.ui.viewmodel.VinTradeUiState

@Composable
fun GiftsCatalogScreen(
    state: VinTradeUiState,
    onSelectCategory: (GiftCategory) -> Unit,
    onToggleWishlist: (String) -> Unit,
    onOpenGift: (GiftItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val ext = LocalVinTradeColors.current
    val filteredGifts = if (state.selectedGiftCategory == GiftCategory.ALL) {
        state.gifts
    } else {
        state.gifts.filter { it.category == state.selectedGiftCategory }
    }

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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Luxury Gifts",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Curated hampers, crystal decanters & VIP tasting passes",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(
                        color = ext.goldSubtle,
                        shape = MaterialTheme.shapes.small,
                        border = BorderStroke(1.dp, ext.gold.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = Formatters.formatPoints(state.vaultPoints),
                            style = MaterialTheme.typography.labelMedium,
                            color = ext.gold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(GiftCategory.entries) { category ->
                        FilterChip(
                            selected = category == state.selectedGiftCategory,
                            onClick = { onSelectCategory(category) },
                            label = { Text(category.label) }
                        )
                    }
                }
            }
        }

        items(filteredGifts, key = { it.id }) { gift ->
            GiftShowcaseCard(
                gift = gift,
                isWishlisted = state.wishlistIds.contains(gift.id),
                onToggleWishlist = { onToggleWishlist(gift.id) },
                onClick = { onOpenGift(gift) },
                modifier = Modifier.widthIn(max = 640.dp)
            )
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }
    }
}
