package com.smartinvestor.app.ui.screens.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartinvestor.app.data.repository.StockRepository
import com.smartinvestor.app.domain.model.WatchlistItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WatchlistViewModel @Inject constructor(
    private val stockRepository: StockRepository
) : ViewModel() {

    val watchlist: StateFlow<List<WatchlistItem>> = stockRepository.getWatchlist()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        seedInitialWatchlistIfEmpty()
    }

    private fun seedInitialWatchlistIfEmpty() {
        viewModelScope.launch {
            if (watchlist.value.isEmpty()) {
                val defaults = listOf(
                    "AAPL" to "Apple Inc.",
                    "GOOGL" to "Alphabet Inc.",
                    "MSFT" to "Microsoft Corp.",
                    "TSLA" to "Tesla Inc.",
                    "NVDA" to "NVIDIA Corp.",
                    "AMZN" to "Amazon.com Inc."
                )
                defaults.forEach { (sym, name) ->
                    if (!stockRepository.isInWatchlist(sym)) {
                        stockRepository.addToWatchlist(sym, name)
                        // Trigger cache update
                        stockRepository.getQuoteWithCache(sym)
                    }
                }
            }
        }
    }

    fun removeFromWatchlist(symbol: String) {
        viewModelScope.launch {
            stockRepository.removeFromWatchlist(symbol)
        }
    }

    fun addToWatchlist(symbol: String, name: String) {
        viewModelScope.launch {
            stockRepository.addToWatchlist(symbol, name)
            stockRepository.getQuoteWithCache(symbol)
        }
    }
}
