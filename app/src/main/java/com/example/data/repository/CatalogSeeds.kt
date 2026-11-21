package com.example.data.repository

import com.example.R
import com.example.data.model.AssetCategory
import com.example.data.model.CandlePoint
import com.example.data.model.CollectorTier
import com.example.data.model.GiftCategory
import com.example.data.model.GiftItem
import com.example.data.model.MarketAsset
import com.example.data.model.PrizeItem
import com.example.data.model.SignalType
import com.example.data.model.WineCategory
import com.example.data.model.WineItem
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

object CatalogSeeds {

    fun buildCandlesFromSeries(series: List<Double>): List<CandlePoint> {
        if (series.size < 4) return emptyList()
        val chunkSize = max(1, series.size / 14)
        val chunks = series.chunked(chunkSize).takeLast(14)
        return chunks.mapIndexed { index, slice ->
            val open = slice.first()
            val close = slice.last()
            val maxVal = slice.maxOrNull() ?: close
            val minVal = slice.minOrNull() ?: close
            val spread = max(abs(close - open) * 0.35, close * 0.0025)
            CandlePoint(
                label = "T-${chunks.size - index}",
                open = open,
                high = max(maxVal, max(open, close) + spread),
                low = min(minVal, min(open, close) - spread),
                close = close
            )
        }
    }

    fun computeRsi14(prices: List<Double>): Double {
        if (prices.size < 15) return 54.0
        val recent = prices.takeLast(20)
        var gains = 0.0
        var losses = 0.0
        for (i in 1 until recent.size) {
            val diff = recent[i] - recent[i - 1]
            if (diff >= 0) gains += diff else losses += abs(diff)
        }
        val period = (recent.size - 1).coerceAtLeast(1)
        val avgGain = gains / period
        val avgLoss = losses / period
        if (avgLoss == 0.0) return 76.0
        val rs = avgGain / avgLoss
        return (100.0 - (100.0 / (1.0 + rs))).coerceIn(18.0, 88.0)
    }

    fun computeEma20DeltaPercent(prices: List<Double>): Double {
        if (prices.isEmpty()) return 0.0
        val k = 2.0 / (20.0 + 1.0)
        var ema = prices.first()
        for (price in prices) {
            ema = price * k + ema * (1.0 - k)
        }
        val latest = prices.last()
        return if (ema == 0.0) 0.0 else ((latest - ema) / ema) * 100.0
    }

    fun classifySignal(rsi: Double, change24h: Double, emaDelta: Double): SignalType {
        return when {
            rsi <= 44.0 || (emaDelta > 1.2 && change24h > 0.8) -> SignalType.STRONG_BUY
            emaDelta > 0.0 || change24h > 0.0 -> SignalType.ACCUMULATE
            rsi >= 72.0 -> SignalType.TAKE_PROFIT
            else -> SignalType.NEUTRAL
        }
    }

    val baselineMarketSnapshot: List<MarketAsset> by lazy {
        val nvdaSeries = listOf(138.4, 139.2, 138.9, 140.1, 141.5, 140.8, 142.3, 143.0, 142.6, 144.2, 145.1, 144.8, 146.4, 147.2, 146.9, 148.35)
        val aaplSeries = listOf(224.5, 225.1, 224.8, 225.9, 226.4, 226.1, 227.0, 227.8, 227.3, 228.1, 228.9, 228.5, 229.4, 230.1, 229.8, 230.65)
        val btcSeries = listOf(88400.0, 88950.0, 88620.0, 89300.0, 89750.0, 90120.0, 89910.0, 90650.0, 91200.0, 90840.0, 91520.0, 91880.0, 92150.0, 92480.0, 92310.0, 92890.0)
        val ethSeries = listOf(3120.0, 3145.0, 3110.0, 3168.0, 3190.0, 3210.0, 3195.0, 3240.0, 3272.0, 3255.0, 3288.0, 3310.0, 3294.0, 3325.0, 3348.0, 3362.0)
        val goldSeries = listOf(2610.0, 2618.0, 2614.0, 2625.0, 2631.0, 2629.0, 2638.0, 2644.0, 2640.0, 2649.0, 2656.0, 2652.0, 2661.0, 2668.0, 2672.0, 2679.0)
        val eurUsdSeries = listOf(1.062, 1.064, 1.061, 1.065, 1.067, 1.066, 1.069, 1.071, 1.070, 1.072, 1.074, 1.073, 1.075, 1.076, 1.075, 1.078)
        val bdxSeries = listOf(368.4, 369.1, 370.0, 369.6, 371.2, 372.8, 372.1, 374.0, 375.3, 376.2, 375.9, 377.4, 378.6, 379.2, 380.5, 381.8)
        val champSeries = listOf(512.0, 514.2, 513.8, 516.4, 518.0, 519.5, 518.9, 521.4, 523.1, 522.7, 524.8, 526.0, 527.5, 528.2, 529.8, 531.4)

        listOf(
            MarketAsset(
                id = "nvda",
                symbol = "NVDA",
                name = "NVIDIA Corp.",
                category = AssetCategory.STOCKS,
                currentPrice = nvdaSeries.last(),
                change24hPercent = 2.84,
                volumeLabel = "Vol 42.1M",
                rsi14 = computeRsi14(nvdaSeries),
                ema20DeltaPercent = computeEma20DeltaPercent(nvdaSeries),
                signal = SignalType.STRONG_BUY,
                sparkline = nvdaSeries,
                candles = buildCandlesFromSeries(nvdaSeries),
                decimals = 2
            ),
            MarketAsset(
                id = "aapl",
                symbol = "AAPL",
                name = "Apple Inc.",
                category = AssetCategory.STOCKS,
                currentPrice = aaplSeries.last(),
                change24hPercent = 1.36,
                volumeLabel = "Vol 31.8M",
                rsi14 = computeRsi14(aaplSeries),
                ema20DeltaPercent = computeEma20DeltaPercent(aaplSeries),
                signal = SignalType.ACCUMULATE,
                sparkline = aaplSeries,
                candles = buildCandlesFromSeries(aaplSeries),
                decimals = 2
            ),
            MarketAsset(
                id = "bitcoin",
                symbol = "BTC / USD",
                name = "Bitcoin Spot",
                category = AssetCategory.CRYPTO,
                currentPrice = btcSeries.last(),
                change24hPercent = 3.42,
                volumeLabel = "Vol $38.4B",
                rsi14 = computeRsi14(btcSeries),
                ema20DeltaPercent = computeEma20DeltaPercent(btcSeries),
                signal = SignalType.STRONG_BUY,
                sparkline = btcSeries,
                candles = buildCandlesFromSeries(btcSeries),
                decimals = 0
            ),
            MarketAsset(
                id = "pax-gold",
                symbol = "XAU / USD",
                name = "Gold Bullion Index",
                category = AssetCategory.COMMODITY,
                currentPrice = goldSeries.last(),
                change24hPercent = 1.18,
                volumeLabel = "Vol $194M",
                rsi14 = computeRsi14(goldSeries),
                ema20DeltaPercent = computeEma20DeltaPercent(goldSeries),
                signal = SignalType.ACCUMULATE,
                sparkline = goldSeries,
                candles = buildCandlesFromSeries(goldSeries),
                decimals = 2
            ),
            MarketAsset(
                id = "bordeaux-500",
                symbol = "BDX-500",
                name = "Bordeaux Grand Cru Index",
                category = AssetCategory.WINE_INDEX,
                currentPrice = bdxSeries.last(),
                change24hPercent = 2.15,
                volumeLabel = "840 Cases",
                rsi14 = computeRsi14(bdxSeries),
                ema20DeltaPercent = computeEma20DeltaPercent(bdxSeries),
                signal = SignalType.STRONG_BUY,
                sparkline = bdxSeries,
                candles = buildCandlesFromSeries(bdxSeries),
                unitPrefix = "",
                decimals = 1
            ),
            MarketAsset(
                id = "ethereum",
                symbol = "ETH / USD",
                name = "Ethereum Spot",
                category = AssetCategory.CRYPTO,
                currentPrice = ethSeries.last(),
                change24hPercent = 2.64,
                volumeLabel = "Vol $16.2B",
                rsi14 = computeRsi14(ethSeries),
                ema20DeltaPercent = computeEma20DeltaPercent(ethSeries),
                signal = SignalType.ACCUMULATE,
                sparkline = ethSeries,
                candles = buildCandlesFromSeries(ethSeries),
                decimals = 2
            ),
            MarketAsset(
                id = "eur-usd",
                symbol = "EUR / USD",
                name = "Euro Spot FX",
                category = AssetCategory.FOREX,
                currentPrice = eurUsdSeries.last(),
                change24hPercent = 0.48,
                volumeLabel = "ECB Ref",
                rsi14 = computeRsi14(eurUsdSeries),
                ema20DeltaPercent = computeEma20DeltaPercent(eurUsdSeries),
                signal = SignalType.NEUTRAL,
                sparkline = eurUsdSeries,
                candles = buildCandlesFromSeries(eurUsdSeries),
                unitPrefix = "",
                decimals = 4
            ),
            MarketAsset(
                id = "champagne-50",
                symbol = "CHMP-50",
                name = "Prestige Champagne Index",
                category = AssetCategory.WINE_INDEX,
                currentPrice = champSeries.last(),
                change24hPercent = 1.92,
                volumeLabel = "312 Lots",
                rsi14 = computeRsi14(champSeries),
                ema20DeltaPercent = computeEma20DeltaPercent(champSeries),
                signal = SignalType.ACCUMULATE,
                sparkline = champSeries,
                candles = buildCandlesFromSeries(champSeries),
                unitPrefix = "",
                decimals = 1
            )
        )
    }

    val wineCatalog: List<WineItem> = listOf(
        WineItem(
            id = "wine_pauillac_2018",
            name = "Château Pauillac Grand Reserve",
            estate = "Domaine de la Côte d'Or",
            vintage = 2018,
            region = "Pauillac, Bordeaux",
            varietal = "Cabernet Sauvignon Blend",
            criticScore = 98,
            marketPriceUsd = 685.0,
            changePercent = 4.6,
            pointsCost = 1400,
            category = WineCategory.RED,
            investmentGrade = "AAA Cellar Grade",
            tastingNotes = "Crushed blackcurrant, cedarwood smoke, graphite minerality, and velvety cocoa tannins with a 30-year aging horizon.",
            imageRes = R.drawable.img_wine_reserve
        ),
        WineItem(
            id = "wine_champagne_2013",
            name = "Cuvée Or Blanc Millésime",
            estate = "Maison Valmont-Reims",
            vintage = 2013,
            region = "Côte des Blancs, Champagne",
            varietal = "100% Grand Cru Chardonnay",
            criticScore = 97,
            marketPriceUsd = 420.0,
            changePercent = 3.1,
            pointsCost = 950,
            category = WineCategory.CHAMPAGNE,
            investmentGrade = "Prestige Brut",
            tastingNotes = "Toasted brioche, Meyer lemon zest, white hazelnut, and fine crystalline effervescence.",
            imageRes = R.drawable.img_trading_cellar_hero
        ),
        WineItem(
            id = "wine_brunello_2017",
            name = "Tenuta Montalcino Riserva",
            estate = "Podere San Lorenzo",
            vintage = 2017,
            region = "Tuscany, Italy",
            varietal = "Sangiovese Grosso",
            criticScore = 96,
            marketPriceUsd = 295.0,
            changePercent = 2.4,
            pointsCost = 720,
            category = WineCategory.RED,
            investmentGrade = "AA+ Estate",
            tastingNotes = "Morello cherry, dried wild rose, blood orange peel, and warm Tuscan leather spice.",
            imageRes = R.drawable.img_wine_reserve
        ),
        WineItem(
            id = "wine_montrachet_2020",
            name = "Puligny Premier Cru Les Folatières",
            estate = "Domaine Moreau-Blanc",
            vintage = 2020,
            region = "Burgundy, France",
            varietal = "Chardonnay",
            criticScore = 96,
            marketPriceUsd = 390.0,
            changePercent = 5.2,
            pointsCost = 880,
            category = WineCategory.WHITE,
            investmentGrade = "Allocated Lot",
            tastingNotes = "White peach, wet limestone, acacia honey, and vibrant saline tension.",
            imageRes = R.drawable.img_wine_reserve
        ),
        WineItem(
            id = "wine_napa_2019",
            name = "Oakville Sovereign Reserve",
            estate = "Vintner Ridge Estate",
            vintage = 2019,
            region = "Napa Valley, California",
            varietal = "Cabernet Sauvignon",
            criticScore = 99,
            marketPriceUsd = 540.0,
            changePercent = 3.9,
            pointsCost = 1200,
            category = WineCategory.RESERVE,
            investmentGrade = "Single-Vineyard",
            tastingNotes = "Blackberry compote, espresso crema, violet petals, and polished French oak.",
            imageRes = R.drawable.img_trading_cellar_hero
        )
    )

    val giftCatalog: List<GiftItem> = listOf(
        GiftItem(
            id = "gift_crystal_decanter",
            title = "Hand-Cut Bohemian Decanter Set",
            subtitle = "Lead-Free Crystal & Gold Rim Glasses",
            category = GiftCategory.DECANTER,
            priceUsd = 340.0,
            pointsCost = 800,
            badge = "Bestseller",
            description = "Mouth-blown crystal aeration carafe paired with two slender Bordeaux stems inside a velvet presentation trunk.",
            imageRes = R.drawable.img_luxury_gifts
        ),
        GiftItem(
            id = "gift_sommelier_hamper",
            title = "Grand Vault Sommelier Hamper",
            subtitle = "Reserve Vintage + Artisan Truffle Pairing",
            category = GiftCategory.HAMPER,
            priceUsd = 580.0,
            pointsCost = 1350,
            badge = "Curated Box",
            description = "Signature satin-ribboned presentation chest with numbered vintage selection, dark Belgian cacao, and brass foil cutter.",
            imageRes = R.drawable.img_luxury_gifts
        ),
        GiftItem(
            id = "gift_watch_roll",
            title = "Cognac Bridle Leather Travel Case",
            subtitle = "Suede-Lined 3-Slot Collector Roll",
            category = GiftCategory.ACCESSORY,
            priceUsd = 265.0,
            pointsCost = 620,
            badge = "Artisan Craft",
            description = "Full-grain vegetable-tanned Italian leather watch and cufflink travel case with snap cushions.",
            imageRes = R.drawable.img_luxury_gifts
        ),
        GiftItem(
            id = "gift_private_tasting",
            title = "Private Candlelit Cellar Tasting",
            subtitle = "Guided 5-Vintage Flight for Two",
            category = GiftCategory.EXPERIENCE,
            priceUsd = 490.0,
            pointsCost = 1100,
            badge = "VIP Pass",
            description = "Hosted by a certified Master Sommelier featuring vertical pours from 2010 to 2019 with artisanal charcuterie.",
            imageRes = R.drawable.img_trading_cellar_hero
        )
    )

    val prizeCatalog: List<PrizeItem> = listOf(
        PrizeItem(
            id = "prize_tasting_duo",
            title = "Collector Welcome Tasting Kit",
            subtitle = "Crystal Stem Pair + Sommelier Key",
            tierRequired = CollectorTier.SILVER,
            pointsRequired = 500,
            estimatedValueUsd = 145.0,
            category = "Starter Milestone",
            description = "Instant redemption milestone for active signal traders. Includes twin tasting stems and forged waiter's corkscrew.",
            isGrandPrize = false,
            imageRes = R.drawable.img_luxury_gifts
        ),
        PrizeItem(
            id = "prize_bordeaux_magnum",
            title = "2016 Grand Cru Double Magnum",
            subtitle = "Numbered Wooden Crate Allocation",
            tierRequired = CollectorTier.GOLD,
            pointsRequired = 1500,
            estimatedValueUsd = 920.0,
            category = "Gold Sommelier Prize",
            description = "Signed 3.0L cellar-aged reserve bottle stored at 55°F until your chosen dispatch date.",
            isGrandPrize = false,
            imageRes = R.drawable.img_wine_reserve
        ),
        PrizeItem(
            id = "prize_swiss_chronograph",
            title = "Atelier Skeleton Automatic Timepiece",
            subtitle = "Rose-Gold Case & Exhibition Sapphire",
            tierRequired = CollectorTier.PLATINUM,
            pointsRequired = 3500,
            estimatedValueUsd = 3400.0,
            category = "Grand Vault Prize",
            description = "Flagship mechanical collector prize awarded to Platinum Vault traders with precision Swiss movement.",
            isGrandPrize = true,
            imageRes = R.drawable.img_grand_prize_vault
        ),
        PrizeItem(
            id = "prize_champagne_retreat",
            title = "Reims Private Château Weekend",
            subtitle = "Two-Night Estate Stay & Harvest Tour",
            tierRequired = CollectorTier.DIAMOND,
            pointsRequired = 6500,
            estimatedValueUsd = 5800.0,
            category = "Diamond Patron Prize",
            description = "All-inclusive private estate itinerary in Champagne with cellar blending workshop and Michelin dining.",
            isGrandPrize = true,
            imageRes = R.drawable.img_grand_prize_vault
        )
    )
}
