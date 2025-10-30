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
    val tax: Double = 0.0, // Tax in base currency
    val stockImpactJson: String = "" // JSON serialized StockImpact (empty string for backwards compatibility)
) {
    fun getLineItems(): List<LineItem> {
        return if (itemsJson.isBlank()) emptyList()
        else Json.decodeFromString(itemsJson)
    }
    
    fun getStockImpact(): StockImpact {
        return if (stockImpactJson.isBlank()) {
            // For transactions before stock system, mark as NOT_APPLICABLE
            StockImpact(StockImpactStatus.NOT_APPLICABLE)
        } else {
            Json.decodeFromString(stockImpactJson)
        }
    }
    
    companion object {
        fun serializeLineItems(items: List<LineItem>): String {
            return Json.encodeToString(items)
        }
        
        fun serializeStockImpact(stockImpact: StockImpact): String {
            return Json.encodeToString(stockImpact)
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

/**
 * Stock impact status for a transaction
 */
enum class StockImpactStatus {
    STOCK_DEDUCTED,    // Stock successfully deducted
    STOCK_FAILED,      // Stock deduction failed
    NOT_APPLICABLE     // Transaction before stock system
}

/**
 * Stock impact information for a transaction
 */
data class StockImpact(
    val status: StockImpactStatus,
    val itemsDeducted: Map<Long, Int> = emptyMap(), // productId -> quantity
    val itemsFailed: Map<Long, Int> = emptyMap(),   // productId -> quantity that failed
    val totalItemsAffected: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun getTotalDeducted(): Int = itemsDeducted.values.sum()
    fun getTotalFailed(): Int = itemsFailed.values.sum()
    fun getSuccessRate(): Float {
        val total = getTotalDeducted() + getTotalFailed()
        return if (total > 0) getTotalDeducted().toFloat() / total.toFloat() else 0f
    }
}
