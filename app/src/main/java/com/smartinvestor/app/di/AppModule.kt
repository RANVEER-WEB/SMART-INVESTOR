package com.smartinvestor.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.smartinvestor.app.data.local.AppDatabase
import com.smartinvestor.app.data.local.dao.NotificationDao
import com.smartinvestor.app.data.local.dao.SearchHistoryDao
import com.smartinvestor.app.data.local.dao.StockDao
import com.smartinvestor.app.data.local.dao.WatchlistDao
import com.smartinvestor.app.data.remote.FinnhubApi
import com.smartinvestor.app.data.remote.GeminiApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// ══════════════════════════════════════════════════════════
// Smart Investor — Hilt Dependency Injection Module
// ══════════════════════════════════════════════════════════

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "smart_investor_prefs")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // ── Room Database ──

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "smart_investor_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideStockDao(db: AppDatabase): StockDao = db.stockDao()

    @Provides
    fun provideWatchlistDao(db: AppDatabase): WatchlistDao = db.watchlistDao()

    @Provides
    fun provideNotificationDao(db: AppDatabase): NotificationDao = db.notificationDao()

    @Provides
    fun provideSearchHistoryDao(db: AppDatabase): SearchHistoryDao = db.searchHistoryDao()

    // ── DataStore Preferences ──

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }

    // ── API Clients ──

    @Provides
    @Singleton
    fun provideFinnhubApi(): FinnhubApi = FinnhubApi()

    @Provides
    @Singleton
    fun provideGeminiApi(): GeminiApi = GeminiApi()
}
