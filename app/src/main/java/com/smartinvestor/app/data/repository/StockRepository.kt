package com.smartinvestor.app.data.repository

import com.smartinvestor.app.data.local.dao.StockDao
import com.smartinvestor.app.data.local.dao.WatchlistDao
import com.smartinvestor.app.data.local.entities.CachedStockEntity
import com.smartinvestor.app.data.local.entities.WatchlistEntity
import com.smartinvestor.app.data.remote.FinnhubApi
import com.smartinvestor.app.domain.model.CompanyProfile
import com.smartinvestor.app.domain.model.MarketNews
import com.smartinvestor.app.domain.model.StockCandle
import com.smartinvestor.app.domain.model.StockQuote
import com.smartinvestor.app.domain.model.WatchlistItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// ══════════════════════════════════════════════════════════
// Smart Investor — Stock Repository
// Fetches from Finnhub API + caches to Room for offline
// ══════════════════════════════════════════════════════════

@Singleton
class StockRepository @Inject constructor(
    private val finnhubApi: FinnhubApi,
    private val stockDao: StockDao,
    private val watchlistDao: WatchlistDao,
) {
    // ── Default symbols for the dashboard ──
    val defaultSymbols = listOf("AAPL", "GOOGL", "MSFT", "AMZN", "TSLA", "META", "NFLX", "NVDA")

    /**
     * Get real-time quote with offline fallback.
     * Returns cached data if network fails (Offline Protocol).
     */
    suspend fun getQuote(symbol: String): Result<StockQuote> {
        return try {
            val quote = finnhubApi.getQuote(symbol)
            Result.success(quote)
        } catch (e: Exception) {
            // Offline fallback — serve from Room cache
            val cached = stockDao.getCachedStock(symbol)
            if (cached != null) {
                Result.success(
                    StockQuote(
                        currentPrice = cached.currentPrice,
                        change = cached.change,
                        changePercent = cached.changePercent,
                        highPrice = cached.highPrice,
                        lowPrice = cached.lowPrice,
                        openPrice = cached.openPrice,
                        previousClose = cached.previousClose,
                    )
                )
            } else {
                Result.failure(e)
            }
        }
    }

    /**
     * Fetch quote + profile together and persist to cache.
     * Use this instead of calling getQuote + getCompanyProfile separately.
     */
    suspend fun getQuoteWithCache(symbol: String): Result<WatchlistItem> {
        return try {
            val quote = finnhubApi.getQuote(symbol)
            val profile = try { finnhubApi.getCompanyProfile(symbol) } catch (_: Exception) { null }
            // Cache both in a single DB write
            stockDao.upsertStock(
                CachedStockEntity(
                    symbol = symbol,
                    companyName = profile?.name ?: symbol,
                    currentPrice = quote.currentPrice,
                    change = quote.change,
                    changePercent = quote.changePercent,
                    highPrice = quote.highPrice,
                    lowPrice = quote.lowPrice,
                    openPrice = quote.openPrice,
                    previousClose = quote.previousClose,
                    logoUrl = profile?.logo ?: "",
                )
            )
            Result.success(
                WatchlistItem(
                    symbol = symbol,
                    companyName = profile?.name ?: symbol,
                    currentPrice = quote.currentPrice,
                    change = quote.change,
                    changePercent = quote.changePercent,
                    logoUrl = profile?.logo ?: "",
                )
            )
        } catch (e: Exception) {
            // Offline fallback
            val cached = stockDao.getCachedStock(symbol)
            if (cached != null) {
                Result.success(
                    WatchlistItem(
                        symbol = symbol,
                        companyName = cached.companyName,
                        currentPrice = cached.currentPrice,
                        change = cached.change,
                        changePercent = cached.changePercent,
                        logoUrl = cached.logoUrl,
                    )
                )
            } else {
                Result.failure(e)
            }
        }
    }

    /**
     * Get company profile.
     */
    suspend fun getCompanyProfile(symbol: String): Result<CompanyProfile> {
        return try {
            Result.success(finnhubApi.getCompanyProfile(symbol))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get market news.
     */
    suspend fun getMarketNews(category: String = "general"): Result<List<MarketNews>> {
        return try {
            Result.success(finnhubApi.getMarketNews(category))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get candle data for the stock chart hero feature.
     */
    suspend fun getStockCandles(
        symbol: String,
        resolution: String = "D",
        from: Long,
        to: Long
    ): Result<StockCandle> {
        return try {
            Result.success(finnhubApi.getStockCandles(symbol, resolution, from, to))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Search symbols with live suggestions.
     */
    suspend fun searchSymbols(query: String) = try {
        Result.success(finnhubApi.searchSymbol(query))
    } catch (e: Exception) {
        Result.failure(e)
    }

    // ── Watchlist Operations ──

    fun getWatchlist(): Flow<List<WatchlistItem>> {
        return watchlistDao.getWatchlist().map { entities ->
            entities.map { entity ->
                val cached = stockDao.getCachedStock(entity.symbol)
                WatchlistItem(
                    symbol = entity.symbol,
                    companyName = entity.companyName,
                    currentPrice = cached?.currentPrice ?: 0.0,
                    change = cached?.change ?: 0.0,
                    changePercent = cached?.changePercent ?: 0.0,
                    logoUrl = cached?.logoUrl ?: "",
                )
            }
        }
    }

    suspend fun addToWatchlist(symbol: String, companyName: String) {
        watchlistDao.addToWatchlist(WatchlistEntity(symbol = symbol, companyName = companyName))
    }

    suspend fun removeFromWatchlist(symbol: String) {
        watchlistDao.removeFromWatchlist(symbol)
    }

    suspend fun isInWatchlist(symbol: String): Boolean {
        return watchlistDao.isInWatchlist(symbol)
    }

    /**
     * Get all cached stocks (for offline indicator).
     */
    fun getCachedStocks(): Flow<List<CachedStockEntity>> {
        return stockDao.getAllCachedStocks()
    }
}
