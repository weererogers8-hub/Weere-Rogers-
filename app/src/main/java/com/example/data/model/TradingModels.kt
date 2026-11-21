package com.example.data.model

import androidx.annotation.DrawableRes
import com.example.R

enum class AssetCategory(val label: String) {
    ALL("All"),
    STOCKS("Stocks"),
    CRYPTO("Crypto"),
    FOREX("Forex"),
    COMMODITY("Bullion"),
    WINE_INDEX("Wine Index")
}

enum class SignalType(val label: String) {
    STRONG_BUY("Strong Buy"),
    ACCUMULATE("Accumulate"),
    NEUTRAL("Hold"),
    TAKE_PROFIT("Take Profit")
}

enum class Timeframe(val label: String) {
    H1("1H"),
    D1("1D"),
    W1("1W"),
    M1("1M")
}

enum class DataSourceMode(val label: String) {
    LIVE("LIVE MARKET PULSE"),
    CACHED("CACHED SNAPSHOT"),
    BASELINE("MARKET SNAPSHOT")
}

data class CandlePoint(
    val label: String,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double
)

data class MarketAsset(
    val id: String,
    val symbol: String,
    val name: String,
    val category: AssetCategory,
    val currentPrice: Double,
    val change24hPercent: Double,
    val volumeLabel: String,
    val rsi14: Double,
    val ema20DeltaPercent: Double,
    val signal: SignalType,
    val sparkline: List<Double>,
    val candles: List<CandlePoint>,
    val unitPrefix: String = "$",
    val decimals: Int = 2,
    val isLiveFromApi: Boolean = false
)

enum class WineCategory(val label: String) {
    ALL("All"),
    RED("Grand Red"),
    CHAMPAGNE("Champagne"),
    WHITE("White"),
    RESERVE("Reserve")
}

data class WineItem(
    val id: String,
    val name: String,
    val estate: String,
    val vintage: Int,
    val region: String,
    val varietal: String,
    val criticScore: Int,
    val marketPriceUsd: Double,
    val changePercent: Double,
    val pointsCost: Int,
    val category: WineCategory,
    val investmentGrade: String,
    val tastingNotes: String,
    @DrawableRes val imageRes: Int = R.drawable.img_wine_reserve
)

enum class GiftCategory(val label: String) {
    ALL("All"),
    DECANTER("Crystal & Bar"),
    HAMPER("Hampers"),
    ACCESSORY("Leather & Travel"),
    EXPERIENCE("Experiences")
}

data class GiftItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: GiftCategory,
    val priceUsd: Double,
    val pointsCost: Int,
    val badge: String,
    val description: String,
    @DrawableRes val imageRes: Int = R.drawable.img_luxury_gifts
)

enum class CollectorTier(
    val title: String,
    val minPoints: Int,
    val bonusMultiplier: String
) {
    SILVER("Silver Collector", 0, "1.0x Signal Pts"),
    GOLD("Gold Sommelier", 1500, "1.25x Signal Pts"),
    PLATINUM("Platinum Vault", 3500, "1.5x Signal Pts"),
    DIAMOND("Diamond Patron", 7000, "2.0x Signal Pts");

    companion object {
        fun forPoints(points: Int): CollectorTier {
            return entries.lastOrNull { points >= it.minPoints } ?: SILVER
        }

        fun nextTier(current: CollectorTier): CollectorTier? {
            val idx = entries.indexOf(current)
            return if (idx in 0 until entries.lastIndex) entries[idx + 1] else null
        }
    }
}

data class PrizeItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val tierRequired: CollectorTier,
    val pointsRequired: Int,
    val estimatedValueUsd: Double,
    val category: String,
    val description: String,
    val isGrandPrize: Boolean,
    @DrawableRes val imageRes: Int = R.drawable.img_grand_prize_vault
)

data class TradeRecord(
    val id: Long,
    val symbol: String,
    val side: String,
    val entryPrice: Double,
    val notionalUsd: Double,
    val pointsEarned: Int,
    val timestampMillis: Long
)

data class ClaimedReward(
    val id: Long,
    val itemId: String,
    val itemTitle: String,
    val itemType: String,
    val pointsSpent: Int,
    val recipientNote: String,
    val claimedAtMillis: Long
)
