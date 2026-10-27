package com.smartinvestor.app.data.repository

import com.smartinvestor.app.data.local.dao.HoldingDao
import com.smartinvestor.app.data.local.dao.OrderDao
import com.smartinvestor.app.data.local.dao.StockDao
import com.smartinvestor.app.data.local.entities.HoldingEntity
import com.smartinvestor.app.data.local.entities.OrderEntity
import com.smartinvestor.app.domain.model.PortfolioHolding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

data class PortfolioSummary(
    val virtualCash: Double,
    val investedValue: Double,
    val currentValue: Double,
    val totalProfitLoss: Double,
    val profitLossPercent: Double,
    val holdings: List<PortfolioHolding>
)

@Singleton
class PortfolioRepository @Inject constructor(
    private val holdingDao: HoldingDao,
    private val orderDao: OrderDao,
    private val stockDao: StockDao,
    private val preferencesRepository: UserPreferencesRepository,
) {
    val virtualCash: Flow<Double> = preferencesRepository.virtualCashBalance

    val holdings: Flow<List<PortfolioHolding>> = holdingDao.getAllHoldings().map { entities ->
        entities.map { entity ->
            val cached = stockDao.getCachedStock(entity.symbol)
            val currentPrice = cached?.currentPrice?.takeIf { it > 0 } ?: entity.avgBuyPrice
            PortfolioHolding(
                symbol = entity.symbol,
                companyName = entity.companyName,
                quantity = entity.quantity,
                avgBuyPrice = entity.avgBuyPrice,
                currentPrice = currentPrice,
                logoUrl = cached?.logoUrl ?: ""
            )
        }
    }

    val orders: Flow<List<OrderEntity>> = orderDao.getAllOrders()

    /**
     * Executes a simulated BUY order.
     */
    suspend fun executeBuy(
        symbol: String,
        companyName: String,
        quantity: Int,
        price: Double
    ): Result<OrderEntity> {
        if (quantity <= 0) return Result.failure(IllegalArgumentException("Quantity must be greater than 0"))
        if (price <= 0) return Result.failure(IllegalArgumentException("Invalid execution price"))

        val totalCost = quantity * price
        val currentCash = preferencesRepository.virtualCashBalance.first()

        if (currentCash < totalCost) {
            return Result.failure(IllegalStateException("Insufficient virtual balance: required ₹$totalCost, available ₹$currentCash"))
        }

        // Deduct virtual balance
        preferencesRepository.setVirtualCashBalance(currentCash - totalCost)

        // Update or insert holding
        val existing = holdingDao.getHolding(symbol)
        if (existing == null) {
            holdingDao.upsertHolding(
                HoldingEntity(
                    symbol = symbol,
                    companyName = companyName,
                    quantity = quantity,
                    avgBuyPrice = price
                )
            )
        } else {
            val newQuantity = existing.quantity + quantity
            val newAvgPrice = ((existing.quantity * existing.avgBuyPrice) + totalCost) / newQuantity
            holdingDao.upsertHolding(
                existing.copy(
                    quantity = newQuantity,
                    avgBuyPrice = newAvgPrice,
                    lastUpdated = System.currentTimeMillis()
                )
            )
        }

        val order = OrderEntity(
            symbol = symbol,
            companyName = companyName,
            type = "BUY",
            quantity = quantity,
            executionPrice = price
        )
        val orderId = orderDao.insertOrder(order)
        return Result.success(order.copy(id = orderId))
    }

    /**
     * Executes a simulated SELL order.
     */
    suspend fun executeSell(
        symbol: String,
        quantity: Int,
        price: Double
    ): Result<OrderEntity> {
        if (quantity <= 0) return Result.failure(IllegalArgumentException("Quantity must be greater than 0"))
        if (price <= 0) return Result.failure(IllegalArgumentException("Invalid execution price"))

        val existing = holdingDao.getHolding(symbol)
            ?: return Result.failure(IllegalStateException("You do not hold shares of $symbol to sell"))

        if (existing.quantity < quantity) {
            return Result.failure(IllegalStateException("Cannot sell $quantity shares: you only own ${existing.quantity} shares"))
        }

        val totalRevenue = quantity * price
        val currentCash = preferencesRepository.virtualCashBalance.first()

        // Credit cash
        preferencesRepository.setVirtualCashBalance(currentCash + totalRevenue)

        // Update or delete holding
        val remainingQty = existing.quantity - quantity
        if (remainingQty <= 0) {
            holdingDao.deleteHolding(symbol)
        } else {
            holdingDao.upsertHolding(
                existing.copy(
                    quantity = remainingQty,
                    lastUpdated = System.currentTimeMillis()
                )
            )
        }

        val order = OrderEntity(
            symbol = symbol,
            companyName = existing.companyName,
            type = "SELL",
            quantity = quantity,
            executionPrice = price
        )
        val orderId = orderDao.insertOrder(order)
        return Result.success(order.copy(id = orderId))
    }

    /**
     * Seeds initial demo portfolio if completely empty.
     */
    suspend fun seedDemoHoldingsIfEmpty() {
        val current = holdingDao.getAllHoldings().first()
        if (current.isEmpty()) {
            holdingDao.upsertHolding(HoldingEntity("AAPL", "Apple Inc.", 15, 160.0))
            holdingDao.upsertHolding(HoldingEntity("MSFT", "Microsoft Corp.", 8, 380.0))
            holdingDao.upsertHolding(HoldingEntity("NVDA", "NVIDIA Corp.", 10, 820.0))
            orderDao.insertOrder(OrderEntity(symbol = "AAPL", companyName = "Apple Inc.", type = "BUY", quantity = 15, executionPrice = 160.0))
            orderDao.insertOrder(OrderEntity(symbol = "MSFT", companyName = "Microsoft Corp.", type = "BUY", quantity = 8, executionPrice = 380.0))
            orderDao.insertOrder(OrderEntity(symbol = "NVDA", companyName = "NVIDIA Corp.", type = "BUY", quantity = 10, executionPrice = 820.0))
        }
    }
}
