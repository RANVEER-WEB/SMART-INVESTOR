package com.smartinvestor.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

// ══════════════════════════════════════════════════════════
// Smart Investor — Application Class
// Hilt entry point for dependency injection
// ══════════════════════════════════════════════════════════

@HiltAndroidApp
class SmartInvestorApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // All API client initialization is deferred via lazy delegates
        // and accessed only from background coroutines (Dispatchers.IO).
        // No pre-warming here — prevents any main-thread blocking.
    }
}
