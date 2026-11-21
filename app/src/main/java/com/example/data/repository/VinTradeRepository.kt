package com.example.data.repository

import com.example.data.local.CachedMarketQuoteEntity
import com.example.data.local.ClaimedRewardEntity
import com.example.data.local.TradeRecordEntity
import com.example.data.local.UserPreferencesStore
import com.example.data.local.VinTradeDao
import com.example.data.local.WishlistItemEntity
import com.example.data.model.AssetCategory
import com.example.data.model.ClaimedReward
import com.example.data.model.DataSourceMode
import com.example.data.model.GiftItem
import com.example.data.model.MarketAsset
import com.example.data.model.PrizeItem
import com.example.data.model.SignalType
import com.example.data.model.TradeRecord
import com.example.data.model.WineItem
import com.example.data.network.NetworkModule
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.math.sin

data class MarketFeedResult(
    val assets: List<MarketAsset>,
    val sourceMode: DataSourceMode,
    val lastSyncedMillis: Long
)

class VinTradeRepository(
    private val dao: VinTradeDao,
    private val prefs: UserPreferencesStore
) {
    val vaultPointsFlow: Flow<Int> = prefs.vaultPointsFlow
    val lifetimePointsFlow: Flow<Int> = prefs.lifetimePointsFlow
    val displayCurrencyFlow: Flow<String> = prefs.displayCurrencyFlow

    val wishlistIdsFlow: Flow<Set<String>> = dao.observeWishlistIds().map { it.toSet() }

    val recentTradesFlow: Flow<List<TradeRecord>> = dao.observeRecentTrades().map { entities ->
        entities.map {
            TradeRecord(
                id = it.id,
                symbol = it.symbol,
                side = it.side,
                entryPrice = it.entryPrice,
                notionalUsd = it.notionalUsd,
                pointsEarned = it.pointsEarned,
                timestampMillis = it.timestampMillis
            )
        }
    }

    val claimedRewardsFlow: Flow<List<ClaimedReward>> = dao.observeClaimedRewards().map { entities ->
        entities.map {
            ClaimedReward(
                id = it.id,
                itemId = it.itemId,
                itemTitle = it.itemTitle,
                itemType = it.itemType,
                pointsSpent = it.pointsSpent,
                recipientNote = it.recipientNote,
                claimedAtMillis = it.claimedAtMillis
            )
        }
    }

    fun getWines(): List<WineItem> = CatalogSeeds.wineCatalog
    fun getGifts(): List<GiftItem> = CatalogSeeds.giftCatalog
    fun getPrizes(): List<PrizeItem> = CatalogSeeds.prizeCatalog

    suspend fun fetchLiveMarketFeed(): MarketFeedResult = coroutineScope {
        val cryptoDeferred = async {
            runCatching { NetworkModule.coinGeckoService.getMarkets() }.getOrNull()
        }
        val fxDeferred = async {
            runCatching { NetworkModule.frankfurterService.getLatestRates() }.getOrNull()
        }

        val cryptoList = cryptoDeferred.await()
        val fxRates = fxDeferred.await()

        val liveAssets = mutableListOf<MarketAsset>()

        if (!cryptoList.isNullOrEmpty()) {
            cryptoList.forEach { coin ->
                val rawSpark = coin.sparklineIn7d?.price?.takeLast(24).orEmpty()
                val series = if (rawSpark.size >= 6) rawSpark else listOf(
                    coin.currentPrice * 0.985,
                    coin.currentPrice * 0.992,
                    coin.currentPrice * 0.989,
                    coin.currentPrice * 0.996,
                    coin.currentPrice * 0.998,
                    coin.currentPrice
                )
                val change = coin.priceChangePercentage24h ?: 0.0
                val rsi = CatalogSeeds.computeRsi14(series)
                val emaDelta = CatalogSeeds.computeEma20DeltaPercent(series)
                val category = if (coin.id == "pax-gold") AssetCategory.COMMODITY else AssetCategory.CRYPTO
                val displaySymbol = when (coin.id) {
                    "pax-gold" -> "XAU / USD"
                    else -> "${coin.symbol.uppercase()} / USD"
                }
                val displayName = when (coin.id) {
                    "pax-gold" -> "Gold Bullion (PAXG)"
                    else -> "${coin.name} Spot"
                }
                val volBillions = (coin.totalVolume ?: 0.0) / 1_000_000_000.0
                val volText = if (volBillions >= 1.0) {
                    String.format("Vol $%.1fB", volBillions)
                } else {
                    String.format("Vol $%.0fM", (coin.totalVolume ?: 0.0) / 1_000_000.0)
                }

                liveAssets += MarketAsset(
                    id = coin.id,
                    symbol = displaySymbol,
                    name = displayName,
                    category = category,
                    currentPrice = coin.currentPrice,
                    change24hPercent = change,
                    volumeLabel = volText,
                    rsi14 = rsi,
                    ema20DeltaPercent = emaDelta,
                    signal = CatalogSeeds.classifySignal(rsi, change, emaDelta),
                    sparkline = series,
                    candles = CatalogSeeds.buildCandlesFromSeries(series),
                    decimals = if (coin.currentPrice >= 1000) 0 else 2,
                    isLiveFromApi = true
                )
            }
        }

        if (fxRates != null) {
            fxRates.rates["USD"]?.let { eurUsd ->
                val series = buildFxSeries(eurUsd)
                val rsi = CatalogSeeds.computeRsi14(series)
                val emaDelta = CatalogSeeds.computeEma20DeltaPercent(series)
                liveAssets += MarketAsset(
                    id = "eur-usd",
                    symbol = "EUR / USD",
                    name = "Euro Spot FX (ECB)",
                    category = AssetCategory.FOREX,
                    currentPrice = eurUsd,
                    change24hPercent = ((series.last() - series.first()) / series.first()) * 100.0,
                    volumeLabel = "ECB ${fxRates.date}",
                    rsi14 = rsi,
                    ema20DeltaPercent = emaDelta,
                    signal = CatalogSeeds.classifySignal(rsi, 0.3, emaDelta),
                    sparkline = series,
                    candles = CatalogSeeds.buildCandlesFromSeries(series),
                    unitPrefix = "",
                    decimals = 4,
                    isLiveFromApi = true
                )
            }
            val gbpPerEur = fxRates.rates["GBP"]
            val usdPerEur = fxRates.rates["USD"]
            if (gbpPerEur != null && usdPerEur != null && gbpPerEur > 0.0) {
                val gbpUsd = usdPerEur / gbpPerEur
                val series = buildFxSeries(gbpUsd)
                val rsi = CatalogSeeds.computeRsi14(series)
                val emaDelta = CatalogSeeds.computeEma20DeltaPercent(series)
                liveAssets += MarketAsset(
                    id = "gbp-usd",
                    symbol = "GBP / USD",
                    name = "Sterling Spot FX (ECB)",
                    category = AssetCategory.FOREX,
                    currentPrice = gbpUsd,
                    change24hPercent = ((series.last() - series.first()) / series.first()) * 100.0,
                    volumeLabel = "ECB ${fxRates.date}",
                    rsi14 = rsi,
                    ema20DeltaPercent = emaDelta,
                    signal = CatalogSeeds.classifySignal(rsi, 0.25, emaDelta),
                    sparkline = series,
                    candles = CatalogSeeds.buildCandlesFromSeries(series),
                    unitPrefix = "",
                    decimals = 4,
                    isLiveFromApi = true
                )
            }
        }

        if (liveAssets.isNotEmpty()) {
            // Merge Fine Wine Index & simulated equity benchmarks alongside live financial feeds
            val supplementalAssets = CatalogSeeds.baselineMarketSnapshot.filter {
                it.category == AssetCategory.WINE_INDEX || it.category == AssetCategory.STOCKS
            }
            val combined = liveAssets + supplementalAssets
            val now = System.currentTimeMillis()
            dao.upsertQuotes(combined.map { it.toEntity(now) })
            return@coroutineScope MarketFeedResult(
                assets = combined,
                sourceMode = DataSourceMode.LIVE,
                lastSyncedMillis = now
            )
        }

        // Fall back to Room cached quotes if network unavailable
        val cached = dao.getCachedQuotes()
        if (cached.isNotEmpty()) {
            return@coroutineScope MarketFeedResult(
                assets = cached.map { it.toDomain() },
                sourceMode = DataSourceMode.CACHED,
                lastSyncedMillis = cached.maxOf { it.updatedAtMillis }
            )
        }

        return@coroutineScope MarketFeedResult(
            assets = CatalogSeeds.baselineMarketSnapshot,
            sourceMode = DataSourceMode.BASELINE,
            lastSyncedMillis = System.currentTimeMillis()
        )
    }

    suspend fun toggleWishlist(itemId: String, currentlyWishlisted: Boolean) {
        if (currentlyWishlisted) {
            dao.removeWishlistItem(itemId)
        } else {
            dao.addWishlistItem(WishlistItemEntity(itemId = itemId, addedAtMillis = System.currentTimeMillis()))
        }
    }

    suspend fun recordPracticeTrade(asset: MarketAsset, side: String, notionalUsd: Double): Int {
        val pointsEarned = ((notionalUsd / 100.0).toInt().coerceIn(25, 350))
        dao.insertTradeRecord(
            TradeRecordEntity(
                symbol = asset.symbol,
                side = side,
                entryPrice = asset.currentPrice,
                notionalUsd = notionalUsd,
                pointsEarned = pointsEarned,
                timestampMillis = System.currentTimeMillis()
            )
        )
        prefs.addVaultPoints(pointsEarned)
        return pointsEarned
    }

    suspend fun claimReward(
        itemId: String,
        itemTitle: String,
        itemType: String,
        pointsCost: Int,
        recipientNote: String
    ): Boolean {
        val spent = prefs.spendVaultPoints(pointsCost)
        if (!spent) return false
        dao.insertClaimedReward(
            ClaimedRewardEntity(
                itemId = itemId,
                itemTitle = itemTitle,
                itemType = itemType,
                pointsSpent = pointsCost,
                recipientNote = recipientNote.ifBlank { "Personal Vault Dispatch" },
                claimedAtMillis = System.currentTimeMillis()
            )
        )
        return true
    }

    suspend fun setDisplayCurrency(currency: String) {
        prefs.setDisplayCurrency(currency)
    }

    private fun buildFxSeries(latest: Double): List<Double> {
        return (0..15).map { idx ->
            val wave = sin(idx * 0.55) * 0.0028
            val drift = (idx - 15) * 0.00025
            latest * (1.0 + wave + drift)
        }.dropLast(1) + latest
    }

    private fun MarketAsset.toEntity(updatedAt: Long): CachedMarketQuoteEntity {
        return CachedMarketQuoteEntity(
            id = id,
            symbol = symbol,
            name = name,
            category = category.name,
            currentPrice = currentPrice,
            change24hPercent = change24hPercent,
            volumeLabel = volumeLabel,
            rsi14 = rsi14,
            ema20DeltaPercent = ema20DeltaPercent,
            signal = signal.name,
            sparklineCsv = sparkline.joinToString(",") { it.toString() },
            unitPrefix = unitPrefix,
            decimals = decimals,
            updatedAtMillis = updatedAt
        )
    }

    private fun CachedMarketQuoteEntity.toDomain(): MarketAsset {
        val spark = sparklineCsv.split(",").mapNotNull { it.toDoubleOrNull() }
        return MarketAsset(
            id = id,
            symbol = symbol,
            name = name,
            category = AssetCategory.entries.find { it.name == category } ?: AssetCategory.CRYPTO,
            currentPrice = currentPrice,
            change24hPercent = change24hPercent,
            volumeLabel = volumeLabel,
            rsi14 = rsi14,
            ema20DeltaPercent = ema20DeltaPercent,
            signal = SignalType.entries.find { it.name == signal } ?: SignalType.NEUTRAL,
            sparkline = spark,
            candles = CatalogSeeds.buildCandlesFromSeries(spark),
            unitPrefix = unitPrefix,
            decimals = decimals,
            isLiveFromApi = false
        )
    }
}
