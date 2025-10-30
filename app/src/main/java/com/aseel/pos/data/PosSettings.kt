package com.aseel.pos.data

import com.aseel.pos.core.Currency
import com.aseel.pos.core.ExchangeRates
import kotlinx.serialization.Serializable

@Serializable
data class PosSettings(
    val selectedCurrency: String = Currency.YER.code,
    val exchangeRates: ExchangeRates = ExchangeRates(),
    val taxIncluded: Boolean = false,
    val taxRate: Double = 0.0,
    val storeName: String = "متجر الأصيل",
    val storeNameEn: String = "Aseel Store",
    val enableBarcodeScanner: Boolean = false
)
