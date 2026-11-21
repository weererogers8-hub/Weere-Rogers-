package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_market_quotes")
data class CachedMarketQuoteEntity(
    @PrimaryKey val id: String,
    val symbol: String,
    val name: String,
    val category: String,
    val currentPrice: Double,
    val change24hPercent: Double,
    val volumeLabel: String,
    val rsi14: Double,
    val ema20DeltaPercent: Double,
    val signal: String,
    val sparklineCsv: String,
    val unitPrefix: String,
    val decimals: Int,
    val updatedAtMillis: Long
)

@Entity(tableName = "wishlist_items")
data class WishlistItemEntity(
    @PrimaryKey val itemId: String,
    val addedAtMillis: Long
)

@Entity(tableName = "trade_records")
data class TradeRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val symbol: String,
    val side: String,
    val entryPrice: Double,
    val notionalUsd: Double,
    val pointsEarned: Int,
    val timestampMillis: Long
)

@Entity(tableName = "claimed_rewards")
data class ClaimedRewardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val itemId: String,
    val itemTitle: String,
    val itemType: String,
    val pointsSpent: Int,
    val recipientNote: String,
    val claimedAtMillis: Long
)
