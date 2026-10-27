package com.smartinvestor.app.data.remote

import com.smartinvestor.app.BuildConfig
import com.smartinvestor.app.domain.model.CompanyProfile
import com.smartinvestor.app.domain.model.MarketNews
import com.smartinvestor.app.domain.model.StockCandle
import com.smartinvestor.app.domain.model.StockQuote
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// ══════════════════════════════════════════════════════════
// Smart Investor — Finnhub API Client
// Provides real-time stock data for Dashboard & Charts
// API key from BuildConfig (local.properties)
// ══════════════════════════════════════════════════════════

class FinnhubApi {

    private val baseUrl = "https://finnhub.io/api/v1"
    private val apiKey = BuildConfig.FINNHUB_API_KEY

    private val client by lazy {
        HttpClient(Android) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    prettyPrint = false
                })
            }
            install(Logging) {
                level = LogLevel.NONE
            }
        }
    }

    // No init block — the HTTP client is lazy and only created on first use.
    // This prevents any work from happening during Hilt's DI graph construction.

    /**
     * Get real-time quote for a stock symbol.
     * Example: getQuote("AAPL")
     */
    suspend fun getQuote(symbol: String): StockQuote = withContext(Dispatchers.IO) {
        return@withContext client.get("$baseUrl/quote") {
            parameter("symbol", symbol)
            parameter("token", apiKey)
        }.body()
    }

    /**
     * Get company profile information.
     */
    suspend fun getCompanyProfile(symbol: String): CompanyProfile = withContext(Dispatchers.IO) {
        return@withContext client.get("$baseUrl/stock/profile2") {
            parameter("symbol", symbol)
            parameter("token", apiKey)
        }.body()
    }

    /**
     * Get latest market news.
     * @param category One of: general, forex, crypto, merger
     */
    suspend fun getMarketNews(category: String = "general"): List<MarketNews> = withContext(Dispatchers.IO) {
        return@withContext client.get("$baseUrl/news") {
            parameter("category", category)
            parameter("token", apiKey)
        }.body()
    }

    /**
     * Get stock candle data for charting.
     * @param resolution Supported: 1, 5, 15, 30, 60, D, W, M
     * @param from Unix timestamp (seconds)
     * @param to Unix timestamp (seconds)
     */
    suspend fun getStockCandles(
        symbol: String,
        resolution: String = "D",
        from: Long,
        to: Long
    ): StockCandle = withContext(Dispatchers.IO) {
        return@withContext client.get("$baseUrl/stock/candle") {
            parameter("symbol", symbol)
            parameter("resolution", resolution)
            parameter("from", from)
            parameter("to", to)
            parameter("token", apiKey)
        }.body()
    }

    /**
     * Search for stock symbols.
     */
    suspend fun searchSymbol(query: String): SymbolSearchResult = withContext(Dispatchers.IO) {
        return@withContext client.get("$baseUrl/search") {
            parameter("q", query)
            parameter("token", apiKey)
        }.body()
    }
}

@kotlinx.serialization.Serializable
data class SymbolSearchResult(
    val count: Int = 0,
    val result: List<SymbolSearchItem> = emptyList()
)

@kotlinx.serialization.Serializable
data class SymbolSearchItem(
    val description: String = "",
    val displaySymbol: String = "",
    val symbol: String = "",
    val type: String = "",
)
