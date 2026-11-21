package com.example

import com.example.data.model.CollectorTier
import com.example.data.model.SignalType
import com.example.data.repository.CatalogSeeds
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun `rsi and candles compute accurately on ascending price series`() {
        val series = (1..20).map { 100.0 + it * 2.0 }
        val rsi = CatalogSeeds.computeRsi14(series)
        val emaDelta = CatalogSeeds.computeEma20DeltaPercent(series)
        val candles = CatalogSeeds.buildCandlesFromSeries(series)

        assertTrue(rsi in 60.0..88.0)
        assertTrue(emaDelta > 0.0)
        assertTrue(candles.isNotEmpty())
        assertTrue(candles.all { it.high >= it.low })
    }

    @Test
    fun `collector tier resolves correctly by lifetime points`() {
        assertEquals(CollectorTier.SILVER, CollectorTier.forPoints(200))
        assertEquals(CollectorTier.GOLD, CollectorTier.forPoints(1650))
        assertEquals(CollectorTier.PLATINUM, CollectorTier.forPoints(4000))
        assertEquals(CollectorTier.DIAMOND, CollectorTier.forPoints(8200))
    }

    @Test
    fun `signal classification reflects oversold RSI`() {
        val signal = CatalogSeeds.classifySignal(rsi = 32.0, change24h = -1.2, emaDelta = -0.8)
        assertEquals(SignalType.STRONG_BUY, signal)
    }
}
