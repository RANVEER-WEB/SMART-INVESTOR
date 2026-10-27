package com.smartinvestor.app.domain.model

// ══════════════════════════════════════════════════════════
// Smart Investor — UiState
// Per gemini.md §4: Every API call MUST use Loading/Success/Error states
// ══════════════════════════════════════════════════════════

/**
 * Sealed class representing the three mandatory states for every API call.
 * Used across all ViewModels for consistent state management.
 */
sealed class UiState<out T> {

    /** Initial/idle state before any action is taken. */
    data object Idle : UiState<Nothing>()

    /** Loading state — triggers shimmer/skeleton UI per brandguidelines.md §2. */
    data object Loading : UiState<Nothing>()

    /**
     * Success state — triggers "Celebration Glow" for mutations,
     * or simply displays data for queries.
     */
    data class Success<T>(val data: T) : UiState<T>()

    /**
     * Error state — triggers "Professional Alert Card" with soft-red glow
     * and "Smart Retry" button per brandguidelines.md §2.
     */
    data class Error(
        val message: String,
        val throwable: Throwable? = null
    ) : UiState<Nothing>()
}
