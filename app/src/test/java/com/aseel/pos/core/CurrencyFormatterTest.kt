package com.aseel.pos.core

import org.junit.Test
import org.junit.Assert.*
import java.math.BigDecimal

/**
 * Unit tests for CurrencyFormatter rounding rules and currency conversion
 */
class CurrencyFormatterTest {

    @Test
    fun `format YER currency correctly`() {
        val amount = BigDecimal("1234.567")
        val formatted = CurrencyFormatter.format(amount, Currency.YER)
        assertTrue("YER should not have decimal places", formatted.contains("1,235"))
        assertFalse("YER should not contain decimal point", formatted.contains("."))
    }

    @Test
    fun `format USD currency correctly with 2 decimal places`() {
        val amount = BigDecimal("1234.567")
        val formatted = CurrencyFormatter.format(amount, Currency.USD)
        assertTrue("USD should have 2 decimal places", formatted.contains("1,234.57"))
        assertFalse("USD should not have more than 2 decimals", formatted.contains("1234.567"))
    }

    @Test
    fun `format SAR currency correctly with 2 decimal places`() {
        val amount = BigDecimal("1234.567")
        val formatted = CurrencyFormatter.format(amount, Currency.SAR)
        assertTrue("SAR should have 2 decimal places", formatted.contains("1,234.57"))
        assertFalse("SAR should not have more than 2 decimals", formatted.contains("1234.567"))
    }

    @Test
    fun `handle zero amount gracefully`() {
        val zero = BigDecimal("0")
        val yerFormatted = CurrencyFormatter.format(zero, Currency.YER)
        val usdFormatted = CurrencyFormatter.format(zero, Currency.USD)
        val sarFormatted = CurrencyFormatter.format(zero, Currency.SAR)
        
        assertTrue("Zero YER should be formatted", yerFormatted.isNotEmpty())
        assertTrue("Zero USD should be formatted", usdFormatted.isNotEmpty())
        assertTrue("Zero SAR should be formatted", sarFormatted.isNotEmpty())
    }

    @Test
    fun `convert currency with correct rate`() {
        val baseAmount = BigDecimal("100")
        val yerToUsd = ExchangeRates(yerToUsd = BigDecimal("0.0039"), yerToSar = BigDecimal("0.0146"))
        
        val usdAmount = yerToUsd.getRate(Currency.USD) ?: BigDecimal.ONE
        val sarAmount = yerToUsd.getRate(Currency.SAR) ?: BigDecimal.ONE
        
        assertTrue("USD rate should be 0.0039", usdAmount == BigDecimal("0.0039"))
        assertTrue("SAR rate should be 0.0146", sarAmount == BigDecimal("0.0146"))
    }

    @Test
    fun `format negative amounts correctly`() {
        val negative = BigDecimal("-1234.567")
        val yerFormatted = CurrencyFormatter.format(negative, Currency.YER)
        val usdFormatted = CurrencyFormatter.format(negative, Currency.USD)
        
        assertTrue("Negative YER should be formatted", yerFormatted.contains("-"))
        assertTrue("Negative USD should be formatted", usdFormatted.contains("-"))
    }

    @Test
    fun `handle large numbers with proper thousand separators`() {
        val largeAmount = BigDecimal("1234567.89")
        val formatted = CurrencyFormatter.format(largeAmount, Currency.USD)
        
        assertTrue("Should contain thousand separators", formatted.contains(","))
        assertTrue("Should show 1,234,567.89", formatted.contains("1,234,567.89"))
    }

    @Test
    fun `round half up correctly for USD`() {
        val amount = BigDecimal("1234.555") // Should round to 1234.56
        val formatted = CurrencyFormatter.format(amount, Currency.USD)
        assertTrue("Should round up to 1234.56", formatted.contains("1,234.56"))
    }

    @Test
    fun `round half up correctly for SAR`() {
        val amount = BigDecimal("1234.555") // Should round to 1234.56
        val formatted = CurrencyFormatter.format(amount, Currency.SAR)
        assertTrue("Should round up to 1234.56", formatted.contains("1,234.56"))
    }

    @Test
    fun `YER formatting should be whole numbers only`() {
        val amount = BigDecimal("1234.9") // Should round to 1235
        val formatted = CurrencyFormatter.format(amount, Currency.YER)
        assertTrue("YER should be whole number", !formatted.contains("."))
        assertTrue("YER should be 1,235", formatted.contains("1,235"))
    }
}