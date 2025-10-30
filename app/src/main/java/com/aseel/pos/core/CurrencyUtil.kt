package com.aseel.pos.core

import kotlinx.serialization.Serializable
import kotlin.math.pow
import kotlin.math.roundToInt

@Serializable
data class ExchangeRates(
    val rates: Map<String, Float> = mapOf(
        "YER" to 1.0f,
        "USD" to 250.0f,  // 250 YER = 1 USD
        "SAR" to 66.67f   // 66.67 YER = 1 SAR
    )
) {
    fun getRate(currency: Currency): Float = rates[currency.code] ?: 1.0f
    
    fun convertFromBase(amountInYER: Double, toCurrency: Currency): Double {
        val rate = getRate(toCurrency)
        return amountInYER / rate
    }
    
    fun convertToBase(amount: Double, fromCurrency: Currency): Double {
        val rate = getRate(fromCurrency)
        return amount * rate
    }
}

object CurrencyFormatter {
    fun format(amount: Double, currency: Currency, rates: ExchangeRates): String {
        val converted = rates.convertFromBase(amount, currency)
        val rounded = roundToDecimals(converted, currency.decimals)
        return when (currency) {
            Currency.YER -> "${rounded.toInt()} ${currency.symbol}"
            else -> "${currency.symbol} ${"%.${currency.decimals}f".format(rounded)}"
        }
    }
    
    fun formatBase(amount: Double): String {
        return "${amount.toInt()} ر.ي"
    }
    
    private fun roundToDecimals(value: Double, decimals: Int): Double {
        val multiplier = 10.0.pow(decimals)
        return (value * multiplier).roundToInt() / multiplier
    }
}
