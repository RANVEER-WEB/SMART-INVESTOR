package com.smartinvestor.app.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartinvestor.app.data.local.dao.SearchHistoryDao
import com.smartinvestor.app.data.local.entities.SearchHistoryEntity
import com.smartinvestor.app.data.repository.PortfolioRepository
import com.smartinvestor.app.data.repository.StockRepository
import com.smartinvestor.app.domain.model.*
import com.smartinvestor.app.ui.components.ChartTimeframe
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import javax.inject.Inject

data class MarketIndex(
    val name: String,
    val value: Double,
    val change: Double,
    val changePercent: Double
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val stockRepository: StockRepository,
    private val portfolioRepository: PortfolioRepository,
    private val searchHistoryDao: SearchHistoryDao,
) : ViewModel() {

    private val _stocksState = MutableStateFlow<UiState<List<WatchlistItem>>>(UiState.Idle)
    val stocksState: StateFlow<UiState<List<WatchlistItem>>> = _stocksState.asStateFlow()

    private val _newsState = MutableStateFlow<UiState<List<MarketNews>>>(UiState.Idle)
    val newsState: StateFlow<UiState<List<MarketNews>>> = _newsState.asStateFlow()

    private val _chartState = MutableStateFlow<UiState<StockCandle>>(UiState.Idle)
    val chartState: StateFlow<UiState<StockCandle>> = _chartState.asStateFlow()

    private val _chartTimeframe = MutableStateFlow(ChartTimeframe.ONE_MONTH)
    val chartTimeframe: StateFlow<ChartTimeframe> = _chartTimeframe.asStateFlow()

    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    var selectedSymbol = MutableStateFlow("AAPL")
        private set

    // Live Portfolio Net Worth from PortfolioRepository
    val portfolioNetWorth: StateFlow<Double> = combine(
        portfolioRepository.virtualCash,
        portfolioRepository.holdings
    ) { cash, holdings ->
        cash + holdings.sumOf { it.currentValue }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1000000.0)

    val portfolioProfitLoss: StateFlow<Pair<Double, Double>> = portfolioRepository.holdings.map { holdings ->
        val invested = holdings.sumOf { it.investedValue }
        val current = holdings.sumOf { it.currentValue }
        val pl = current - invested
        val plPercent = if (invested > 0) (pl / invested) * 100 else 0.0
        Pair(pl, plPercent)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Pair(0.0, 0.0))

    // Market indices (NIFTY 50, SENSEX, BANK NIFTY)
    val marketIndices = listOf(
        MarketIndex("NIFTY 50", 24325.80, 142.30, 0.59),
        MarketIndex("SENSEX", 79942.20, 480.15, 0.60),
        MarketIndex("BANK NIFTY", 51870.45, -85.20, -0.16)
    )

    // Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val recentSearches: StateFlow<List<String>> = searchHistoryDao.getRecentSearches()
        .map { list -> list.map { it.query } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        loadMarketMovers()
        loadNews()
        loadChart(selectedSymbol.value)
    }

    fun setChartTimeframe(tf: ChartTimeframe) {
        _chartTimeframe.value = tf
        loadChart(selectedSymbol.value)
    }

    private fun loadMarketMovers() {
        viewModelScope.launch {
            _stocksState.value = UiState.Loading
            try {
                val semaphore = Semaphore(3)
                val items = stockRepository.defaultSymbols.map { symbol ->
                    async {
                        semaphore.withPermit {
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
            val secondsPerDay = 24 * 60 * 60L
            val from = now - (_chartTimeframe.value.days * secondsPerDay)
            val result = stockRepository.getStockCandles(symbol, "D", from, now)
            _chartState.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Failed to load chart") }
            )
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun recordSearch(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            searchHistoryDao.insertSearch(SearchHistoryEntity(query = query.trim().uppercase()))
        }
    }
}
