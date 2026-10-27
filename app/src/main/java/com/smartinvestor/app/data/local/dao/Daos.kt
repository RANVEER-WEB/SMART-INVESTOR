package com.smartinvestor.app.data.local.dao

import androidx.room.*
import com.smartinvestor.app.data.local.entities.CachedStockEntity
import com.smartinvestor.app.data.local.entities.NotificationEntity
import com.smartinvestor.app.data.local.entities.SearchHistoryEntity
import com.smartinvestor.app.data.local.entities.WatchlistEntity
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
