package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VinTradeDao {
    @Query("SELECT * FROM cached_market_quotes ORDER BY id ASC")
    suspend fun getCachedQuotes(): List<CachedMarketQuoteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertQuotes(quotes: List<CachedMarketQuoteEntity>)

    @Query("SELECT itemId FROM wishlist_items")
    fun observeWishlistIds(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addWishlistItem(item: WishlistItemEntity)

    @Query("DELETE FROM wishlist_items WHERE itemId = :itemId")
    suspend fun removeWishlistItem(itemId: String)

    @Query("SELECT * FROM trade_records ORDER BY timestampMillis DESC LIMIT 25")
    fun observeRecentTrades(): Flow<List<TradeRecordEntity>>

    @Insert
    suspend fun insertTradeRecord(record: TradeRecordEntity)

    @Query("SELECT * FROM claimed_rewards ORDER BY claimedAtMillis DESC")
    fun observeClaimedRewards(): Flow<List<ClaimedRewardEntity>>

    @Insert
    suspend fun insertClaimedReward(reward: ClaimedRewardEntity)
}
