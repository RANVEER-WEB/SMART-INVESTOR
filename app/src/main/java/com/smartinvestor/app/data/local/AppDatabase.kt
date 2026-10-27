package com.smartinvestor.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.smartinvestor.app.data.local.dao.NotificationDao
import com.smartinvestor.app.data.local.dao.SearchHistoryDao
import com.smartinvestor.app.data.local.dao.StockDao
import com.smartinvestor.app.data.local.dao.WatchlistDao
import com.smartinvestor.app.data.local.entities.CachedStockEntity
import com.smartinvestor.app.data.local.entities.NotificationEntity
import com.smartinvestor.app.data.local.entities.SearchHistoryEntity
import com.smartinvestor.app.data.local.entities.WatchlistEntity

// ══════════════════════════════════════════════════════════
// Smart Investor — Room Database
// Offline cache per gemini.md §2: "Offline Protocol"
// ══════════════════════════════════════════════════════════

@Database(
    entities = [
        CachedStockEntity::class,
        WatchlistEntity::class,
        NotificationEntity::class,
        SearchHistoryEntity::class,
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun stockDao(): StockDao
    abstract fun watchlistDao(): WatchlistDao
    abstract fun notificationDao(): NotificationDao
    abstract fun searchHistoryDao(): SearchHistoryDao
}
