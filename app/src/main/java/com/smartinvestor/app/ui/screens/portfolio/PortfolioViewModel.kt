package com.smartinvestor.app.ui.screens.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartinvestor.app.data.local.entities.OrderEntity
import com.smartinvestor.app.data.repository.PortfolioRepository
import com.smartinvestor.app.data.repository.StockRepository
import com.smartinvestor.app.domain.model.PortfolioHolding
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PortfolioUiData(
    val virtualCash: Double = 1000000.0,
    val investedValue: Double = 0.0,
    val currentValue: Double = 0.0,
    val totalProfitLoss: Double = 0.0,
    val profitLossPercent: Double = 0.0,
    val totalPortfolioNetWorth: Double = 1000000.0,
    val holdings: List<PortfolioHolding> = emptyList(),
    val recentOrders: List<OrderEntity> = emptyList()
)

@HiltViewModel
class PortfolioViewModel @Inject constructor(
    private val portfolioRepository: PortfolioRepository,
    private val stockRepository: StockRepository
) : ViewModel() {

    private val _uiData = MutableStateFlow(PortfolioUiData())
    val uiData: StateFlow<PortfolioUiData> = _uiData.asStateFlow()

    init {
        viewModelScope.launch {
            portfolioRepository.seedDemoHoldingsIfEmpty()
        }
        observePortfolio()
    }

    private fun observePortfolio() {
        viewModelScope.launch {
            combine(
                portfolioRepository.virtualCash,
                portfolioRepository.holdings,
                portfolioRepository.orders
            ) { cash, holdingsList, ordersList ->
                val invested = holdingsList.sumOf { it.investedValue }
                val current = holdingsList.sumOf { it.currentValue }
                val pl = current - invested
                val plPercent = if (invested > 0) (pl / invested) * 100 else 0.0
                val netWorth = cash + current

                PortfolioUiData(
                    virtualCash = cash,
                    investedValue = invested,
                    currentValue = current,
                    totalProfitLoss = pl,
                    profitLossPercent = plPercent,
                    totalPortfolioNetWorth = netWorth,
                    holdings = holdingsList,
                    recentOrders = ordersList.take(5)
                )
            }.collect { data ->
                _uiData.value = data
            }
        }
    }
}
