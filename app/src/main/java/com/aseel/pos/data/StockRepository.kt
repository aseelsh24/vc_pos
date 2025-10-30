package com.aseel.pos.data

import kotlinx.coroutines.flow.Flow
import java.io.File

/**
 * Repository interface for managing stock operations
 */
interface StockRepository {
    
    /**
     * Check current stock level for a product
     */
    suspend fun checkStock(productId: Long): Int
    
    /**
     * Check stock by SKU
     */
    suspend fun checkStockBySku(sku: String): Int
    
    /**
     * Deduct stock quantity from a product (atomic operation)
     * @return true if successful, false if insufficient stock
     */
    suspend fun deductStock(productId: Long, quantity: Int): Boolean
    
    /**
     * Deduct stock for multiple items in a transaction
     * @return Map of productId to quantity deducted (for rollback purposes)
     */
    suspend fun deductStockBatch(items: Map<Long, Int>): Map<Long, Int>
    
    /**
     * Add stock to a product
     */
    suspend fun addStock(productId: Long, quantity: Int)
    
    /**
     * Add stock for multiple products
     */
    suspend fun addStockBatch(items: Map<Long, Int>)
    
    /**
     * Rollback stock deduction (used for transaction failure)
     */
    suspend fun rollbackStockDeduction(items: Map<Long, Int>)
    
    /**
     * Get all products with their stock levels
     */
    fun getAllProductsWithStock(): Flow<List<ProductWithStock>>
    
    /**
     * Get products with low stock (below reorder level)
     */
    fun getLowStockProducts(): Flow<List<ProductWithStock>>
    
    /**
     * Get products by category with stock information
     */
    fun getProductsWithStockByCategory(categoryId: Long): Flow<List<ProductWithStock>>
    
    /**
     * Calculate total stock value across all products
     */
    suspend fun calculateTotalStockValue(): Double
    
    /**
     * Get reorder level for a product
     */
    suspend fun getReorderLevel(productId: Long): Int
    
    /**
     * Set reorder level for a product
     */
    suspend fun setReorderLevel(productId: Long, level: Int)
    
    /**
     * Generate comprehensive stock report
     */
    suspend fun generateStockReport(): StockReport
    
    /**
     * Generate stock analytics
     */
    suspend fun generateStockAnalytics(): StockAnalytics
    
    /**
     * Export inventory to CSV file
     */
    suspend fun exportInventoryToCsv(file: File): Boolean
    
    /**
     * Get stock movements/history for a product
     */
    suspend fun getStockMovements(productId: Long): List<StockMovement>
    
    /**
     * Get stock value by category
     */
    suspend fun getStockValueByCategory(): Map<String, Double>
}

/**
 * Data class for products with stock information
 */
data class ProductWithStock(
    val product: Product,
    val currentStock: Int,
    val reorderLevel: Int,
    val stockValue: Double,
    val isLowStock: Boolean
)

/**
 * Stock report with comprehensive information
 */
data class StockReport(
    val totalProducts: Int,
    val totalStockValue: Double,
    val lowStockProducts: Int,
    val outOfStockProducts: Int,
    val categories: List<CategoryStockInfo>,
    val generatedAt: Long
)

/**
 * Category-wise stock information
 */
data class CategoryStockInfo(
    val categoryName: String,
    val totalProducts: Int,
    val totalStockValue: Double,
    val lowStockCount: Int
)

/**
 * Stock analytics and insights
 */
data class StockAnalytics(
    val averageStockValue: Double,
    val highestValueProduct: ProductWithStock?,
    val lowestStockProduct: ProductWithStock?,
    val stockDistribution: Map<String, Int>, // stock range -> count
    val reorderAlerts: List<ProductWithStock>,
    val generatedAt: Long
)

/**
 * Stock movement record
 */
data class StockMovement(
    val productId: Long,
    val productName: String,
    val quantityChange: Int,
    val movementType: MovementType,
    val timestamp: Long,
    val referenceId: String? = null
)

/**
 * Types of stock movements
 */
enum class MovementType {
    IN,        // Stock added
    OUT,       // Stock deducted
    ADJUSTMENT, // Manual adjustment
    ROLLBACK   // Transaction rollback
}
