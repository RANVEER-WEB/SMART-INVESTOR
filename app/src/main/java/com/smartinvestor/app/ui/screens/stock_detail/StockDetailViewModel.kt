package com.smartinvestor.app.ui.screens.stock_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartinvestor.app.data.repository.PortfolioRepository
import com.smartinvestor.app.data.repository.StockRepository
import com.smartinvestor.app.domain.model.*
import com.smartinvestor.app.ui.components.ChartTimeframe
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StockDetailData(
    val symbol: String,
    val quote: StockQuote,
    val profile: CompanyProfile?,
    val candles: StockCandle?,
    val isInWatchlist: Boolean,
    val userHoldingQuantity: Int = 0
)

@HiltViewModel
class StockDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val stockRepository: StockRepository,
    private val portfolioRepository: PortfolioRepository,
) : ViewModel() {

    val symbol: String = savedStateHandle.get<String>("symbol") ?: "AAPL"

    private val _uiState = MutableStateFlow<UiState<StockDetailData>>(UiState.Loading)
    val uiState: StateFlow<UiState<StockDetailData>> = _uiState.asStateFlow()

    private val _timeframe = MutableStateFlow(ChartTimeframe.ONE_MONTH)
    val timeframe: StateFlow<ChartTimeframe> = _timeframe.asStateFlow()

    private val _orderState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val orderState: StateFlow<UiState<String>> = _orderState.asStateFlow()

    init {
        loadStockDetails()
    }

    fun setTimeframe(tf: ChartTimeframe) {
        _timeframe.value = tf
        loadStockDetails()
    }

    fun loadStockDetails() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val quote = stockRepository.getQuote(symbol).getOrNull()
                    ?: StockQuote(currentPrice = 150.0, change = 1.5, changePercent = 1.01)
                val profile = stockRepository.getCompanyProfile(symbol).getOrNull()
                val isWatchlist = stockRepository.isInWatchlist(symbol)

                val now = System.currentTimeMillis() / 1000
                val secondsPerDay = 24 * 60 * 60L
                val from = now - (_timeframe.value.days * secondsPerDay)
                val candles = stockRepository.getStockCandles(symbol, "D", from, now).getOrNull()

                // Query user's current holdings for this symbol
                val currentHoldings = portfolioRepository.holdings.first()
                val userHolding = currentHoldings.find { it.symbol.equals(symbol, ignoreCase = true) }
                val userShares = userHolding?.quantity ?: 0

                _uiState.value = UiState.Success(
                    StockDetailData(
                        symbol = symbol,
                        quote = quote,
                        profile = profile,
                        candles = candles,
                        isInWatchlist = isWatchlist,
                        userHoldingQuantity = userShares
                    )
                )
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to load stock data")
            }
        }
    }

    fun toggleWatchlist() {
        viewModelScope.launch {
            val current = _uiState.value
            if (current is UiState.Success) {
                val data = current.data
                if (data.isInWatchlist) {
                    stockRepository.removeFromWatchlist(symbol)
                } else {
                    stockRepository.addToWatchlist(symbol, data.profile?.name ?: symbol)
                }
                _uiState.value = UiState.Success(data.copy(isInWatchlist = !data.isInWatchlist))
            }
        }
    }

    fun buyStock(quantity: Int) {
        viewModelScope.launch {
            _orderState.value = UiState.Loading
            val current = _uiState.value
            if (current is UiState.Success) {
                val price = current.data.quote.currentPrice
                val res = portfolioRepository.executeBuy(
                    symbol = symbol,
                    companyName = current.data.profile?.name ?: symbol,
                    quantity = quantity,
                    price = price
                )
                _orderState.value = res.fold(
                    onSuccess = {
                        loadStockDetails()
                        UiState.Success("Successfully bought $quantity shares of $symbol at ₹${String.format("%.2f", price)}!")
                    },
                    onFailure = { UiState.Error(it.message ?: "Buy order failed") }
                )
            }
        }
    }

    fun sellStock(quantity: Int) {
        viewModelScope.launch {
            _orderState.value = UiState.Loading
            val current = _uiState.value
            if (current is UiState.Success) {
                val price = current.data.quote.currentPrice
                val res = portfolioRepository.executeSell(
                    symbol = symbol,
                    quantity = quantity,
                    price = price
                )
                _orderState.value = res.fold(
                    onSuccess = {
                        loadStockDetails()
                        UiState.Success("Successfully sold $quantity shares of $symbol at ₹${String.format("%.2f", price)}!")
                    },
                    onFailure = { UiState.Error(it.message ?: "Sell order failed") }
                )
            }
        }
    }

    fun clearOrderState() {
        _orderState.value = UiState.Idle
    }
}
