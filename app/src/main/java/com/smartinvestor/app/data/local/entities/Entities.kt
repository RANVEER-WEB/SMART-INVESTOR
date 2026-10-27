package com.smartinvestor.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

// ══════════════════════════════════════════════════════════
// Room Entities — Offline Cache per gemini.md §2
// ══════════════════════════════════════════════════════════

@Entity(tableName = "cached_stocks")
data class CachedStockEntity(
    @PrimaryKey val symbol: String,
    val companyName: String,
    val currentPrice: Double,
    val change: Double,
    val changePercent: Double,
    val highPrice: Double,
    val lowPrice: Double,
    val openPrice: Double,
    val previousClose: Double,
    val logoUrl: String = "",
    val lastUpdated: Long = System.currentTimeMillis(),
)

@Entity(tableName = "watchlist")
data class WatchlistEntity(
    @PrimaryKey val symbol: String,
    val companyName: String,
    val addedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val message: String,
    val type: String, // "ai", "profit", "news"
    val isRead: Boolean = false,
    val deepLink: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
)

@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey val query: String,
    val timestamp: Long = System.currentTimeMillis(),
)
