package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        CachedMarketQuoteEntity::class,
        WishlistItemEntity::class,
        TradeRecordEntity::class,
        ClaimedRewardEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class VinTradeDatabase : RoomDatabase() {
    abstract fun vinTradeDao(): VinTradeDao

    companion object {
        @Volatile
        private var INSTANCE: VinTradeDatabase? = null

        fun getInstance(context: Context): VinTradeDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    VinTradeDatabase::class.java,
                    "vintrade_vault.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
