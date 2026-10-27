package com.smartinvestor.app

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.pow
import kotlin.math.roundToLong

class FinancialCalculationsTest {

    /**
     * Standard SIP Compound Growth Formula:
     * FV = P * [ ( (1 + i)^n - 1 ) / i ] * (1 + i)
     * where:
     * P = monthly contribution
     * i = monthly return rate (annual_rate / 12)
     * n = total months (years * 12)
     */
    private fun calculateSipFutureValue(monthly: Double, annualRatePercent: Double, years: Int): Double {
        val i = (annualRatePercent / 100.0) / 12.0
        val n = (years * 12).toDouble()
        return if (i > 0) {
            monthly * (((1.0 + i).pow(n) - 1.0) / i) * (1.0 + i)
        } else {
            monthly * n
        }
    }

    /**
     * Target Goal Required SIP Formula:
     * P = FV / [ [ ( (1 + i)^n - 1 ) / i ] * (1 + i) ]
     */
    private fun calculateRequiredMonthlySip(targetCorpus: Double, annualRatePercent: Double, years: Int): Double {
        val i = (annualRatePercent / 100.0) / 12.0
        val n = (years * 12).toDouble()
        return if (i > 0) {
            targetCorpus / ((((1.0 + i).pow(n) - 1.0) / i) * (1.0 + i))
        } else {
            targetCorpus / n
        }
    }

    /**
     * Indian Capital Gains Tax (Union Budget 2024-25):
     * STCG = 20% flat
     * LTCG = 12.5% on gains exceeding ₹1,25,000 exemption limit
     */
    private fun calculateEquityCapitalGainsTax(stcgGain: Double, ltcgGain: Double): Pair<Double, Double> {
        val stcgTax = (stcgGain.coerceAtLeast(0.0)) * 0.20
        val taxableLtcg = (ltcgGain - 125000.0).coerceAtLeast(0.0)
        val ltcgTax = taxableLtcg * 0.125
        return Pair(stcgTax, ltcgTax)
    }

    @Test
    fun testSipCalculation_TenYears_TwelvePercent() {
        // Monthly: ₹10,000, 12% p.a., 10 years (120 months)
        val fv = calculateSipFutureValue(10000.0, 12.0, 10)
        // Independent mathematical benchmark: ₹23,23,391 approx
        assertEquals(2323391L, fv.roundToLong())
    }

    @Test
    fun testSipCalculation_ZeroRate_ReturnsPrincipal() {
        val fv = calculateSipFutureValue(5000.0, 0.0, 5)
        assertEquals(300000L, fv.roundToLong()) // 5000 * 60 = 3,00,000
    }

    @Test
    fun testTargetGoalPlanner_ReciprocalConsistency() {
        val target = 5000000.0 // ₹50 Lakhs
        val rate = 12.0
        val years = 15

        val requiredSip = calculateRequiredMonthlySip(target, rate, years)
        val resultingCorpus = calculateSipFutureValue(requiredSip, rate, years)

        // Verifies the reciprocal relationship: feeding required SIP back returns target corpus
        assertEquals(target.roundToLong(), resultingCorpus.roundToLong())
    }

    @Test
    fun testIndianTaxCalculation_BelowExemptionLimit() {
        // LTCG is ₹1,00,000 (below ₹1,25,000 exemption)
        val (stcgTax, ltcgTax) = calculateEquityCapitalGainsTax(stcgGain = 40000.0, ltcgGain = 100000.0)
        assertEquals(8000.0, stcgTax, 0.01) // 20% of 40,000
        assertEquals(0.0, ltcgTax, 0.01) // 0% on LTCG under ₹1.25L
    }

    @Test
    fun testIndianTaxCalculation_AboveExemptionLimit() {
        // LTCG is ₹2,25,000 (taxable: 2,25,000 - 1,25,000 = 1,00,000)
        val (stcgTax, ltcgTax) = calculateEquityCapitalGainsTax(stcgGain = 50000.0, ltcgGain = 225000.0)
        assertEquals(10000.0, stcgTax, 0.01) // 20% of 50,000
        assertEquals(12500.0, ltcgTax, 0.01) // 12.5% of 1,00,000
    }
}
