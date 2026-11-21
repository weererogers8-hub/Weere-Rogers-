package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WineBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.WineCategory
import com.example.data.model.WineItem
import com.example.ui.components.WineShowcaseCard
import com.example.ui.theme.LocalVinTradeColors
import com.example.ui.viewmodel.VinTradeUiState

@Composable
fun WinesCatalogScreen(
    state: VinTradeUiState,
    onSelectCategory: (WineCategory) -> Unit,
    onSearchChange: (String) -> Unit,
    onToggleWishlist: (String) -> Unit,
    onOpenWine: (WineItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val ext = LocalVinTradeColors.current
    val filteredWines = state.wines.filter { wine ->
        val matchesCategory = state.selectedWineCategory == WineCategory.ALL ||
            wine.category == state.selectedWineCategory
        val query = state.wineSearchQuery.trim()
        val matchesQuery = query.isEmpty() ||
            wine.name.contains(query, ignoreCase = true) ||
            wine.region.contains(query, ignoreCase = true) ||
            wine.estate.contains(query, ignoreCase = true) ||
            wine.vintage.toString().contains(query)
        matchesCategory && matchesQuery
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
                Text(
                    text = "Fine Wines & Cellar",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Investment-grade vintages tracked against market value & Vault points",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = state.wineSearchQuery,
                    onValueChange = onSearchChange,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search vintages",
                            tint = ext.gold
                        )
                    },
                    placeholder = { Text("Search Bordeaux, vintage, estate...") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("wine_search_input")
                )
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(WineCategory.entries) { category ->
                        FilterChip(
                            selected = category == state.selectedWineCategory,
                            onClick = { onSelectCategory(category) },
                            label = { Text(category.label) }
                        )
                    }
                }
            }
        }

        if (filteredWines.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 640.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.WineBar,
                            contentDescription = null,
                            tint = ext.gold,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No vintages match your filter",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Try clearing the search query or switching regions.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredWines, key = { it.id }) { wine ->
                WineShowcaseCard(
                    wine = wine,
                    isWishlisted = state.wishlistIds.contains(wine.id),
                    onToggleWishlist = { onToggleWishlist(wine.id) },
                    onClick = { onOpenWine(wine) },
                    modifier = Modifier.widthIn(max = 640.dp)
                )
            }
        }

        item { Spacer(modifier = Modifier.height(12.dp)) }
    }
}
