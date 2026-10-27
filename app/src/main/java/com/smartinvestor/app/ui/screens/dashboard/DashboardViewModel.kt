package com.smartinvestor.app.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartinvestor.app.data.repository.StockRepository
import com.smartinvestor.app.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val stockRepository: StockRepository
) : ViewModel() {

    private val _stocksState = MutableStateFlow<UiState<List<WatchlistItem>>>(UiState.Idle)
    val stocksState: StateFlow<UiState<List<WatchlistItem>>> = _stocksState.asStateFlow()

    private val _newsState = MutableStateFlow<UiState<List<MarketNews>>>(UiState.Idle)
    val newsState: StateFlow<UiState<List<MarketNews>>> = _newsState.asStateFlow()

    private val _chartState = MutableStateFlow<UiState<StockCandle>>(UiState.Idle)
    val chartState: StateFlow<UiState<StockCandle>> = _chartState.asStateFlow()

    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    var selectedSymbol = MutableStateFlow("AAPL")
        private set

    private var chartJob: Job? = null

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        loadMarketMovers()
        loadNews()
        loadChart(selectedSymbol.value)
    }

    private fun loadMarketMovers() {
        viewModelScope.launch {
            _stocksState.value = UiState.Loading
            try {
                // ── Concurrency Guard ─────────────────────────────────
                // Limit to 3 parallel requests at a time to prevent
                // CPU/IO starvation (was causing ANR in com.android.phone).
                // Previously: 8 symbols × 2 API calls = 16 simultaneous requests.
                // Now: max 3 at once, each making only 1 merged call.
                val semaphore = Semaphore(3)
                val items = stockRepository.defaultSymbols.map { symbol ->
                    async {
                        semaphore.withPermit {
                            // Single call: fetches quote + profile + caches in one shot
                            stockRepository.getQuoteWithCache(symbol).getOrNull()
                                ?: WatchlistItem(
                                    symbol = symbol,
                                    companyName = symbol,
                                    currentPrice = 0.0,
                                    change = 0.0,
                                    changePercent = 0.0,
                                    logoUrl = "",
                                )
                        }
                    }
                }.awaitAll()
                _stocksState.value = UiState.Success(items)
                _isOffline.value = false
            } catch (e: Exception) {
                _stocksState.value = UiState.Error(e.message ?: "Failed to load stocks")
                _isOffline.value = true
            }
        }
    }

    private fun loadNews() {
        viewModelScope.launch {
            _newsState.value = UiState.Loading
            val result = stockRepository.getMarketNews()
            _newsState.value = result.fold(
                onSuccess = { UiState.Success(it.take(10)) },
                onFailure = { UiState.Error(it.message ?: "Failed to load news") }
            )
        }
    }

    fun loadChart(symbol: String) {
        selectedSymbol.value = symbol
        viewModelScope.launch {
            _chartState.value = UiState.Loading
            val now = System.currentTimeMillis() / 1000
            val thirtyDaysAgo = now - (30 * 24 * 60 * 60)
            val result = stockRepository.getStockCandles(symbol, "D", thirtyDaysAgo, now)
            _chartState.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Failed to load chart") }
            )
        }
    }
}
