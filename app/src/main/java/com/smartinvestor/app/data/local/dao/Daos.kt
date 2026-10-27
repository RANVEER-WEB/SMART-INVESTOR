package com.smartinvestor.app.data.local.dao

import androidx.room.*
import com.smartinvestor.app.data.local.entities.*
import kotlinx.coroutines.flow.Flow

// ══════════════════════════════════════════════════════════
// Room DAOs — Data Access Objects
// ══════════════════════════════════════════════════════════

@Dao
interface StockDao {
    @Query("SELECT * FROM cached_stocks ORDER BY symbol ASC")
    fun getAllCachedStocks(): Flow<List<CachedStockEntity>>

    @Query("SELECT * FROM cached_stocks WHERE symbol = :symbol")
    suspend fun getCachedStock(symbol: String): CachedStockEntity?

    @Upsert
    suspend fun upsertStock(stock: CachedStockEntity)

    @Upsert
    suspend fun upsertStocks(stocks: List<CachedStockEntity>)

    @Query("DELETE FROM cached_stocks WHERE symbol = :symbol")
    suspend fun deleteStock(symbol: String)

    @Query("DELETE FROM cached_stocks")
    suspend fun clearAll()
}

@Dao
interface WatchlistDao {
    @Query("SELECT * FROM watchlist ORDER BY addedAt DESC")
    fun getWatchlist(): Flow<List<WatchlistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToWatchlist(item: WatchlistEntity)

    @Query("DELETE FROM watchlist WHERE symbol = :symbol")
    suspend fun removeFromWatchlist(symbol: String)

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist WHERE symbol = :symbol)")
    suspend fun isInWatchlist(symbol: String): Boolean
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE isRead = 0 ORDER BY timestamp DESC")
    fun getUnreadNotifications(): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>

    @Insert
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Int)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllAsRead()

    @Query("DELETE FROM notifications")
    suspend fun clearAll()
}

@Dao
interface SearchHistoryDao {
    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT 10")
    fun getRecentSearches(): Flow<List<SearchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearch(search: SearchHistoryEntity)

    @Query("DELETE FROM search_history WHERE query = :query")
    suspend fun deleteSearch(query: String)

    @Query("DELETE FROM search_history")
    suspend fun clearAll()
}

@Dao
interface HoldingDao {
    @Query("SELECT * FROM portfolio_holdings ORDER BY symbol ASC")
    fun getAllHoldings(): Flow<List<HoldingEntity>>

    @Query("SELECT * FROM portfolio_holdings WHERE symbol = :symbol")
    suspend fun getHolding(symbol: String): HoldingEntity?

    @Upsert
    suspend fun upsertHolding(holding: HoldingEntity)

    @Query("DELETE FROM portfolio_holdings WHERE symbol = :symbol")
    suspend fun deleteHolding(symbol: String)

    @Query("DELETE FROM portfolio_holdings")
    suspend fun clearAll()
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM paper_orders ORDER BY timestamp DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Insert
    suspend fun insertOrder(order: OrderEntity): Long

    @Query("DELETE FROM paper_orders")
    suspend fun clearAll()
}

@Dao
interface LessonProgressDao {
    @Query("SELECT * FROM lesson_progress")
    fun getAllProgress(): Flow<List<LessonProgressEntity>>

    @Query("SELECT * FROM lesson_progress WHERE lessonId = :lessonId")
    suspend fun getProgress(lessonId: String): LessonProgressEntity?

    @Upsert
    suspend fun saveProgress(progress: LessonProgressEntity)
}
