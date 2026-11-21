package com.example.data.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class SparklineIn7dDto(
    @Json(name = "price") val price: List<Double>? = null
)

@JsonClass(generateAdapter = true)
data class CoinMarketDto(
    @Json(name = "id") val id: String,
    @Json(name = "symbol") val symbol: String,
    @Json(name = "name") val name: String,
    @Json(name = "current_price") val currentPrice: Double,
    @Json(name = "total_volume") val totalVolume: Double? = null,
    @Json(name = "price_change_percentage_24h") val priceChangePercentage24h: Double? = null,
    @Json(name = "sparkline_in_7d") val sparklineIn7d: SparklineIn7dDto? = null
)

@JsonClass(generateAdapter = true)
data class FrankfurterLatestDto(
    @Json(name = "base") val base: String,
    @Json(name = "date") val date: String,
    @Json(name = "rates") val rates: Map<String, Double>
)

interface CoinGeckoApiService {
    @GET("api/v3/coins/markets")
    suspend fun getMarkets(
        @Query("vs_currency") vsCurrency: String = "usd",
        @Query("ids") ids: String = "bitcoin,ethereum,solana,pax-gold",
        @Query("sparkline") sparkline: Boolean = true,
        @Query("price_change_percentage") priceChangePercentage: String = "24h"
    ): List<CoinMarketDto>
}

interface FrankfurterApiService {
    @GET("latest")
    suspend fun getLatestRates(
        @Query("from") base: String = "EUR",
        @Query("to") symbols: String = "USD,GBP,CHF,JPY"
    ): FrankfurterLatestDto
}
