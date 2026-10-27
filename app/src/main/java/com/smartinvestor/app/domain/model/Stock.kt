package com.smartinvestor.app.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ══════════════════════════════════════════════════════════
// Smart Investor — Stock Domain Models
// Used by Finnhub API and Dashboard
// ══════════════════════════════════════════════════════════

@Serializable
data class StockQuote(
    @SerialName("c") val currentPrice: Double = 0.0,
    @SerialName("d") val change: Double = 0.0,
    @SerialName("dp") val changePercent: Double = 0.0,
    @SerialName("h") val highPrice: Double = 0.0,
    @SerialName("l") val lowPrice: Double = 0.0,
    @SerialName("o") val openPrice: Double = 0.0,
    @SerialName("pc") val previousClose: Double = 0.0,
    @SerialName("t") val timestamp: Long = 0L,
)

@Serializable
data class CompanyProfile(
    val country: String = "",
    val currency: String = "",
    val exchange: String = "",
    @SerialName("finnhubIndustry") val industry: String = "",
    val ipo: String = "",
    val logo: String = "",
    @SerialName("marketCapitalization") val marketCap: Double = 0.0,
    val name: String = "",
    val phone: String = "",
    @SerialName("shareOutstanding") val sharesOutstanding: Double = 0.0,
    val ticker: String = "",
    val weburl: String = "",
)

@Serializable
data class MarketNews(
    val category: String = "",
    val datetime: Long = 0L,
    val headline: String = "",
    val id: Long = 0L,
    val image: String = "",
    val related: String = "",
    val source: String = "",
    val summary: String = "",
    val url: String = "",
)

@Serializable
data class StockCandle(
    @SerialName("c") val closePrices: List<Double> = emptyList(),
    @SerialName("h") val highPrices: List<Double> = emptyList(),
    @SerialName("l") val lowPrices: List<Double> = emptyList(),
    @SerialName("o") val openPrices: List<Double> = emptyList(),
    @SerialName("t") val timestamps: List<Long> = emptyList(),
    @SerialName("v") val volumes: List<Long> = emptyList(),
    @SerialName("s") val status: String = "",
)

/**
 * Local watchlist item combining stock info with user preferences.
 */
data class WatchlistItem(
    val symbol: String,
    val companyName: String,
    val currentPrice: Double = 0.0,
    val change: Double = 0.0,
    val changePercent: Double = 0.0,
    val logoUrl: String = "",
)

/**
 * Portfolio holding model.
 */
data class PortfolioHolding(
    val symbol: String,
    val companyName: String,
    val quantity: Int,
    val avgBuyPrice: Double,
    val currentPrice: Double = 0.0,
    val logoUrl: String = "",
) {
    val investedValue: Double get() = quantity * avgBuyPrice
    val currentValue: Double get() = quantity * currentPrice
    val profitLoss: Double get() = currentValue - investedValue
    val profitLossPercent: Double get() = if (investedValue > 0) (profitLoss / investedValue) * 100 else 0.0
}
