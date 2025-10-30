package com.aseel.pos.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.aseel.pos.core.PaymentMethod
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Long = Instant.now().toEpochMilli(),
    val totalBase: Double, // Total in YER
    val currencyCode: String,
    val rateToBase: Float, // Exchange rate at transaction time
    val itemsJson: String, // JSON serialized LineItems
    val paymentMethod: PaymentMethod,
    val cashierName: String? = null,
    val discount: Double = 0.0, // Discount in base currency
    val tax: Double = 0.0 // Tax in base currency
) {
    fun getLineItems(): List<LineItem> {
        return if (itemsJson.isBlank()) emptyList()
        else Json.decodeFromString(itemsJson)
    }
    
    companion object {
        fun serializeLineItems(items: List<LineItem>): String {
            return Json.encodeToString(items)
        }
    }
}

@Serializable
data class LineItem(
    val productId: Long,
    val productName: String,
    val sku: String,
    val qty: Int,
    val unitPriceBase: Double,
    val subtotalBase: Double
)
