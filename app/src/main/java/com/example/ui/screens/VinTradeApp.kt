package com.example.ui.screens

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.WineBar
import androidx.compose.material.icons.outlined.AutoGraph
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.WineBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.components.AssetDetailSheet
import com.example.ui.components.ItemRedeemSheet
import com.example.ui.viewmodel.RedeemableSelection
import com.example.ui.viewmodel.VinTradeViewModel

object VinTradeRoutes {
    const val ROUTE_TRADING = "trading"
    const val ROUTE_WINES = "wines"
    const val ROUTE_GIFTS = "gifts"
    const val ROUTE_PRIZES = "prizes"
}

private data class NavDestination(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

private val destinations = listOf(
    NavDestination(
        route = VinTradeRoutes.ROUTE_TRADING,
        label = "Trading",
        selectedIcon = Icons.Filled.AutoGraph,
        unselectedIcon = Icons.Outlined.AutoGraph,
        testTag = "nav_trading"
    ),
    NavDestination(
        route = VinTradeRoutes.ROUTE_WINES,
        label = "Wines",
        selectedIcon = Icons.Filled.WineBar,
        unselectedIcon = Icons.Outlined.WineBar,
        testTag = "nav_wines"
    ),
    NavDestination(
        route = VinTradeRoutes.ROUTE_GIFTS,
        label = "Gifts",
        selectedIcon = Icons.Filled.CardGiftcard,
        unselectedIcon = Icons.Outlined.CardGiftcard,
        testTag = "nav_gifts"
    ),
    NavDestination(
        route = VinTradeRoutes.ROUTE_PRIZES,
        label = "Prizes",
        selectedIcon = Icons.Filled.EmojiEvents,
        unselectedIcon = Icons.Outlined.EmojiEvents,
        testTag = "nav_prizes"
    )
)

@Composable
fun VinTradeApp(viewModel: VinTradeViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: VinTradeRoutes.ROUTE_TRADING
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.snackbarEvents.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    fun navigateTopLevel(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 600.dp

        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                if (!useRail) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        destinations.forEach { dest ->
                            val selected = currentRoute == dest.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateTopLevel(dest.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = dest.label
                                    )
                                },
                                label = { Text(dest.label) },
                                modifier = Modifier.testTag(dest.testTag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useRail) {
                    NavigationRail(containerColor = MaterialTheme.colorScheme.surface) {
                        destinations.forEach { dest ->
                            val selected = currentRoute == dest.route
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateTopLevel(dest.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = dest.label
                                    )
                                },
                                label = { Text(dest.label) },
                                modifier = Modifier.testTag(dest.testTag)
                            )
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = VinTradeRoutes.ROUTE_TRADING,
                    modifier = Modifier.weight(1f)
                ) {
                    composable(VinTradeRoutes.ROUTE_TRADING) {
                        TradingPulseScreen(
                            state = state,
                            onRefresh = viewModel::refreshMarketData,
                            onToggleSimulation = viewModel::toggleSimulation,
                            onSelectFeaturedAsset = viewModel::selectFeaturedAsset,
                            onSelectCategory = viewModel::selectAssetCategory,
                            onOpenAsset = viewModel::openAssetDetail,
                            onOpenRedeemable = viewModel::openRedeemable,
                            onNavigateToWines = { navigateTopLevel(VinTradeRoutes.ROUTE_WINES) },
                            onNavigateToPrizes = { navigateTopLevel(VinTradeRoutes.ROUTE_PRIZES) }
                        )
                    }
                    composable(VinTradeRoutes.ROUTE_WINES) {
                        WinesCatalogScreen(
                            state = state,
                            onSelectCategory = viewModel::selectWineCategory,
                            onSearchChange = viewModel::updateWineSearch,
                            onToggleWishlist = viewModel::toggleWishlist,
                            onOpenWine = { wine -> viewModel.openRedeemable(RedeemableSelection.Wine(wine)) }
                        )
                    }
                    composable(VinTradeRoutes.ROUTE_GIFTS) {
                        GiftsCatalogScreen(
                            state = state,
                            onSelectCategory = viewModel::selectGiftCategory,
                            onToggleWishlist = viewModel::toggleWishlist,
                            onOpenGift = { gift -> viewModel.openRedeemable(RedeemableSelection.Gift(gift)) }
                        )
                    }
                    composable(VinTradeRoutes.ROUTE_PRIZES) {
                        PrizesVaultScreen(
                            state = state,
                            onOpenPrize = { prize -> viewModel.openRedeemable(RedeemableSelection.Prize(prize)) }
                        )
                    }
                }
            }
        }
    }

    state.selectedAssetDetail?.let { asset ->
        AssetDetailSheet(
            asset = asset,
            selectedTimeframe = state.selectedTimeframe,
            onSelectTimeframe = viewModel::selectTimeframe,
            onExecuteTrade = { side, notional ->
                viewModel.executePracticeTrade(asset, side, notional)
            },
            onDismiss = viewModel::closeAssetDetail
        )
    }

    state.redeemableSelection?.let { selection ->
        ItemRedeemSheet(
            selection = selection,
            vaultPoints = state.vaultPoints,
            onClaim = viewModel::claimSelection,
            onDismiss = viewModel::closeRedeemable
        )
    }
}
