package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.UserPreferencesStore
import com.example.data.local.VinTradeDatabase
import com.example.data.model.AssetCategory
import com.example.data.model.ClaimedReward
import com.example.data.model.CollectorTier
import com.example.data.model.DataSourceMode
import com.example.data.model.GiftCategory
import com.example.data.model.GiftItem
import com.example.data.model.MarketAsset
import com.example.data.model.PrizeItem
import com.example.data.model.Timeframe
import com.example.data.model.TradeRecord
import com.example.data.model.WineCategory
import com.example.data.model.WineItem
import com.example.data.repository.CatalogSeeds
import com.example.data.repository.VinTradeRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

sealed interface RedeemableSelection {
    data class Wine(val item: WineItem) : RedeemableSelection
    data class Gift(val item: GiftItem) : RedeemableSelection
    data class Prize(val item: PrizeItem) : RedeemableSelection
}

data class VinTradeUiState(
    val isRefreshing: Boolean = true,
    val isSimulationRunning: Boolean = true,
    val simulationTickCount: Int = 0,
    val dataSourceMode: DataSourceMode = DataSourceMode.BASELINE,
    val lastSyncedMillis: Long = System.currentTimeMillis(),
    val marketAssets: List<MarketAsset> = emptyList(),
    val featuredAssetId: String = "nvda",
    val selectedAssetCategory: AssetCategory = AssetCategory.ALL,
    val selectedTimeframe: Timeframe = Timeframe.D1,
    val selectedAssetDetail: MarketAsset? = null,
    val sentimentScore: Int = 68,
    val wines: List<WineItem> = emptyList(),
    val selectedWineCategory: WineCategory = WineCategory.ALL,
    val wineSearchQuery: String = "",
    val gifts: List<GiftItem> = emptyList(),
    val selectedGiftCategory: GiftCategory = GiftCategory.ALL,
    val prizes: List<PrizeItem> = emptyList(),
    val wishlistIds: Set<String> = emptySet(),
    val recentTrades: List<TradeRecord> = emptyList(),
    val claimedRewards: List<ClaimedReward> = emptyList(),
    val vaultPoints: Int = 1650,
    val lifetimePoints: Int = 1650,
    val currentTier: CollectorTier = CollectorTier.GOLD,
    val nextTier: CollectorTier? = CollectorTier.PLATINUM,
    val redeemableSelection: RedeemableSelection? = null
)

class VinTradeViewModel(application: Application) : AndroidViewModel(application) {
    private val database = VinTradeDatabase.getInstance(application)
    private val prefs = UserPreferencesStore(application)
    private val repository = VinTradeRepository(database.vinTradeDao(), prefs)

    private val _uiState = MutableStateFlow(
        VinTradeUiState(
            marketAssets = CatalogSeeds.baselineMarketSnapshot,
            wines = repository.getWines(),
            gifts = repository.getGifts(),
            prizes = repository.getPrizes()
        )
    )
    val uiState: StateFlow<VinTradeUiState> = _uiState.asStateFlow()

    private val _snackbarEvents = MutableSharedFlow<String>()
    val snackbarEvents: SharedFlow<String> = _snackbarEvents.asSharedFlow()

    init {
        observeLocalStreams()
        refreshMarketData()
        startSimulatedTickerLoop()
    }

    private fun observeLocalStreams() {
        viewModelScope.launch {
            combine(
                repository.vaultPointsFlow,
                repository.lifetimePointsFlow,
                repository.wishlistIdsFlow,
                repository.recentTradesFlow,
                repository.claimedRewardsFlow
            ) { vaultPoints, lifetimePoints, wishlist, trades, rewards ->
                val tier = CollectorTier.forPoints(lifetimePoints)
                val next = CollectorTier.nextTier(tier)
                Triple(
                    Triple(vaultPoints, lifetimePoints, Pair(tier, next)),
                    wishlist,
                    Pair(trades, rewards)
                )
            }.collect { (pointsBundle, wishlist, logs) ->
                val (vaultPts, lifetimePts, tierPair) = pointsBundle
                _uiState.update { state ->
                    state.copy(
                        vaultPoints = vaultPts,
                        lifetimePoints = lifetimePts,
                        currentTier = tierPair.first,
                        nextTier = tierPair.second,
                        wishlistIds = wishlist,
                        recentTrades = logs.first,
                        claimedRewards = logs.second
                    )
                }
            }
        }
    }

    private fun startSimulatedTickerLoop() {
        viewModelScope.launch {
            while (isActive) {
                delay(1800L)
                if (!_uiState.value.isSimulationRunning) continue
                _uiState.update { state ->
                    if (state.marketAssets.isEmpty()) return@update state
                    val updatedAssets = state.marketAssets.map { asset ->
                        val volatility = when (asset.category) {
                            AssetCategory.CRYPTO -> 0.0042
                            AssetCategory.STOCKS -> 0.0030
                            AssetCategory.COMMODITY -> 0.0018
                            AssetCategory.FOREX -> 0.0008
                            AssetCategory.WINE_INDEX -> 0.0012
                            AssetCategory.ALL -> 0.0020
                        }
                        val stepPct = (Random.nextDouble(-1.0, 1.06)) * volatility
                        val nextPrice = (asset.currentPrice * (1.0 + stepPct)).coerceAtLeast(0.0001)
                        val updatedSpark = (asset.sparkline.drop(1) + nextPrice).ifEmpty { listOf(nextPrice) }
                        val rsi = CatalogSeeds.computeRsi14(updatedSpark)
                        val emaDelta = CatalogSeeds.computeEma20DeltaPercent(updatedSpark)
                        val nextChange = (asset.change24hPercent + stepPct * 100.0).coerceIn(-18.0, 24.0)
                        asset.copy(
                            currentPrice = nextPrice,
                            change24hPercent = nextChange,
                            rsi14 = rsi,
                            ema20DeltaPercent = emaDelta,
                            signal = CatalogSeeds.classifySignal(rsi, nextChange, emaDelta),
                            sparkline = updatedSpark,
                            candles = CatalogSeeds.buildCandlesFromSeries(updatedSpark)
                        )
                    }
                    val updatedDetail = state.selectedAssetDetail?.let { sel ->
                        updatedAssets.find { it.id == sel.id } ?: sel
                    }
                    state.copy(
                        marketAssets = updatedAssets,
                        selectedAssetDetail = updatedDetail,
                        sentimentScore = computeOverallSentiment(updatedAssets),
                        simulationTickCount = state.simulationTickCount + 1,
                        lastSyncedMillis = System.currentTimeMillis()
                    )
                }
            }
        }
    }

    fun toggleSimulation() {
        _uiState.update { it.copy(isSimulationRunning = !it.isSimulationRunning) }
    }

    fun selectFeaturedAsset(assetId: String) {
        _uiState.update { it.copy(featuredAssetId = assetId) }
    }

    fun refreshMarketData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            val result = repository.fetchLiveMarketFeed()
            val sentiment = computeOverallSentiment(result.assets)
            _uiState.update { state ->
                val updatedSelected = state.selectedAssetDetail?.let { current ->
                    result.assets.find { it.id == current.id } ?: current
                }
                val validFeatured = if (result.assets.any { it.id == state.featuredAssetId }) {
                    state.featuredAssetId
                } else {
                    result.assets.firstOrNull()?.id ?: state.featuredAssetId
                }
                state.copy(
                    isRefreshing = false,
                    marketAssets = result.assets,
                    featuredAssetId = validFeatured,
                    dataSourceMode = result.sourceMode,
                    lastSyncedMillis = result.lastSyncedMillis,
                    sentimentScore = sentiment,
                    selectedAssetDetail = updatedSelected
                )
            }
        }
    }

    private fun computeOverallSentiment(assets: List<MarketAsset>): Int {
        if (assets.isEmpty()) return 64
        val avgRsi = assets.map { it.rsi14 }.average()
        val positiveCount = assets.count { it.change24hPercent > 0 }
        val breadthBonus = (positiveCount.toDouble() / assets.size) * 20.0
        return (avgRsi * 0.8 + breadthBonus).toInt().coerceIn(18, 94)
    }

    fun selectAssetCategory(category: AssetCategory) {
        _uiState.update { it.copy(selectedAssetCategory = category) }
    }

    fun selectTimeframe(timeframe: Timeframe) {
        _uiState.update { it.copy(selectedTimeframe = timeframe) }
    }

    fun openAssetDetail(asset: MarketAsset) {
        _uiState.update { it.copy(selectedAssetDetail = asset, featuredAssetId = asset.id) }
    }

    fun closeAssetDetail() {
        _uiState.update { it.copy(selectedAssetDetail = null) }
    }

    fun selectWineCategory(category: WineCategory) {
        _uiState.update { it.copy(selectedWineCategory = category) }
    }

    fun updateWineSearch(query: String) {
        _uiState.update { it.copy(wineSearchQuery = query) }
    }

    fun selectGiftCategory(category: GiftCategory) {
        _uiState.update { it.copy(selectedGiftCategory = category) }
    }

    fun openRedeemable(selection: RedeemableSelection) {
        _uiState.update { it.copy(redeemableSelection = selection) }
    }

    fun closeRedeemable() {
        _uiState.update { it.copy(redeemableSelection = null) }
    }

    fun toggleWishlist(itemId: String) {
        viewModelScope.launch {
            val isWishlisted = _uiState.value.wishlistIds.contains(itemId)
            repository.toggleWishlist(itemId, isWishlisted)
            _snackbarEvents.emit(
                if (isWishlisted) "Removed from Vault Wishlist" else "Saved to Vault Wishlist"
            )
        }
    }

    fun executePracticeTrade(asset: MarketAsset, side: String, notionalUsd: Double) {
        viewModelScope.launch {
            val pts = repository.recordPracticeTrade(asset, side, notionalUsd)
            _snackbarEvents.emit("$side signal logged on ${asset.symbol} • +$pts Vault pts earned")
        }
    }

    fun claimSelection(
        itemId: String,
        title: String,
        itemType: String,
        pointsCost: Int,
        recipientNote: String
    ) {
        viewModelScope.launch {
            val success = repository.claimReward(itemId, title, itemType, pointsCost, recipientNote)
            if (success) {
                _uiState.update { it.copy(redeemableSelection = null) }
                _snackbarEvents.emit("Claimed $title (-$pointsCost pts)")
            } else {
                _snackbarEvents.emit("Need $pointsCost Vault pts — log trading signals to earn more.")
            }
        }
    }
}
