package com.smartinvestor.app

import com.smartinvestor.app.domain.model.PortfolioHolding
import org.junit.Assert.assertEquals
import org.junit.Test

class PortfolioHoldingTest {

    @Test
    fun testPortfolioHolding_ProfitCalculations() {
        // Holding 10 shares purchased at ₹100, current price ₹125
        val holding = PortfolioHolding(
            symbol = "RELIANCE",
            companyName = "Reliance Industries Ltd",
            quantity = 10,
            avgBuyPrice = 100.0,
            currentPrice = 125.0
        )

        assertEquals(1000.0, holding.investedValue, 0.001)
        assertEquals(1250.0, holding.currentValue, 0.001)
        assertEquals(250.0, holding.profitLoss, 0.001)
        assertEquals(25.0, holding.profitLossPercent, 0.001)
    }

    @Test
    fun testPortfolioHolding_LossCalculations() {
        // Holding 20 shares purchased at ₹500, current price ₹450
        val holding = PortfolioHolding(
            symbol = "TCS",
            companyName = "Tata Consultancy Services",
            quantity = 20,
            avgBuyPrice = 500.0,
            currentPrice = 450.0
        )

        assertEquals(10000.0, holding.investedValue, 0.001)
        assertEquals(9000.0, holding.currentValue, 0.001)
        assertEquals(-1000.0, holding.profitLoss, 0.001)
        assertEquals(-10.0, holding.profitLossPercent, 0.001)
    }

    @Test
    fun testWeightedAverageBuyPrice() {
        val initialQty = 10
        val initialAvg = 100.0 // total cost = 1000

        val newBuyQty = 10
        val newBuyPrice = 150.0 // total cost = 1500

        val totalQty = initialQty + newBuyQty
        val combinedAvg = ((initialQty * initialAvg) + (newBuyQty * newBuyPrice)) / totalQty

        assertEquals(20, totalQty)
        assertEquals(125.0, combinedAvg, 0.001)
    }
}
