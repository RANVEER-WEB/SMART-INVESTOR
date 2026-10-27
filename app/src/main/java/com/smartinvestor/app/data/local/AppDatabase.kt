package com.smartinvestor.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.smartinvestor.app.data.local.dao.*
import com.smartinvestor.app.data.local.entities.*

// ══════════════════════════════════════════════════════════
// Smart Investor — Room Database
// Offline cache & Paper Trading per gemini.md §2
// ══════════════════════════════════════════════════════════

@Database(
    entities = [
        CachedStockEntity::class,
        WatchlistEntity::class,
        NotificationEntity::class,
        SearchHistoryEntity::class,
        HoldingEntity::class,
        OrderEntity::class,
        LessonProgressEntity::class,
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun stockDao(): StockDao
    abstract fun watchlistDao(): WatchlistDao
    abstract fun notificationDao(): NotificationDao
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun holdingDao(): HoldingDao
    abstract fun orderDao(): OrderDao
    abstract fun lessonProgressDao(): LessonProgressDao
}
