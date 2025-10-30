package com.aseel.pos.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of StockRepository with comprehensive stock management
 */
@Singleton
class StockRepositoryImpl @Inject constructor(
    private val productDao: ProductDao,
    private val categoryDao: CategoryDao
) : StockRepository {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    
    override suspend fun checkStock(productId: Long): Int {
        val product = productDao.getProductById(productId)
        return product?.quantity_in_stock ?: 0
    }
    
    override suspend fun checkStockBySku(sku: String): Int {
        val product = productDao.getProductBySku(sku)
        return product?.quantity_in_stock ?: 0
    }
    
    override suspend fun deductStock(productId: Long, quantity: Int): Boolean {
        require(quantity > 0) { "Quantity must be positive" }
        
        val product = productDao.getProductById(productId) 
            ?: throw IllegalArgumentException("Product not found: $productId")
        
        return if (product.quantity_in_stock >= quantity) {
            val updatedProduct = product.copy(
                quantity_in_stock = product.quantity_in_stock - quantity
            )
            productDao.updateProduct(updatedProduct)
            true
        } else {
            false
        }
    }
    
    override suspend fun deductStockBatch(items: Map<Long, Int>): Map<Long, Int> {
        require(items.isNotEmpty()) { "Items map cannot be empty" }
        
        // Check stock availability for all items first
        val stockMap = mutableMapOf<Long, Int>()
        for ((productId, quantity) in items) {
            val currentStock = checkStock(productId)
            stockMap[productId] = currentStock
            
            if (currentStock < quantity) {
                // Insufficient stock, rollback any previous deductions and return
                rollbackStockDeduction(stockMap.filter { it.value < it.key })
                return emptyMap()
            }
        }
        
        // All items have sufficient stock, proceed with deduction
        val deductedItems = mutableMapOf<Long, Int>()
        for ((productId, quantity) in items) {
            val product = productDao.getProductById(productId) 
                ?: throw IllegalArgumentException("Product not found: $productId")
            
            val updatedProduct = product.copy(
                quantity_in_stock = product.quantity_in_stock - quantity
            )
            productDao.updateProduct(updatedProduct)
            deductedItems[productId] = quantity
        }
        
        return deductedItems
    }
    
    override suspend fun addStock(productId: Long, quantity: Int) {
        require(quantity > 0) { "Quantity must be positive" }
        
        val product = productDao.getProductById(productId)
            ?: throw IllegalArgumentException("Product not found: $productId")
        
        val updatedProduct = product.copy(
            quantity_in_stock = product.quantity_in_stock + quantity
        )
        productDao.updateProduct(updatedProduct)
    }
    
    override suspend fun addStockBatch(items: Map<Long, Int>) {
        require(items.isNotEmpty()) { "Items map cannot be empty" }
        
        for ((productId, quantity) in items) {
            addStock(productId, quantity)
        }
    }
    
    override suspend fun rollbackStockDeduction(items: Map<Long, Int>) {
        for ((productId, quantity) in items) {
            addStock(productId, quantity)
        }
    }
    
    override fun getAllProductsWithStock(): Flow<List<ProductWithStock>> {
        return productDao.getAllProducts().map { products ->
            products.map { product ->
                createProductWithStock(product)
            }
        }
    }
    
    override fun getLowStockProducts(): Flow<List<ProductWithStock>> {
        return getAllProductsWithStock().map { products ->
            products.filter { it.isLowStock }
        }
    }
    
    override fun getProductsWithStockByCategory(categoryId: Long): Flow<List<ProductWithStock>> {
        return productDao.getProductsByCategory(categoryId).map { products ->
            products.map { product ->
                createProductWithStock(product)
            }
        }
    }
    
    override suspend fun calculateTotalStockValue(): Double {
        val products = getAllProductsList()
        return products.sumOf { product ->
            product.quantity_in_stock * product.priceBase
        }
    }
    
    override suspend fun getReorderLevel(productId: Long): Int {
        // For now, using a default reorder level of 10
        // In a real implementation, you might want to store this in the database
        return 10
    }
    
    override suspend fun setReorderLevel(productId: Long, level: Int) {
        require(level >= 0) { "Reorder level cannot be negative" }
        // Implementation would store reorder level in database
        // For now, this is a placeholder
    }
    
    override suspend fun generateStockReport(): StockReport {
        val products = getAllProductsList()
        val categories = getAllCategoriesList()
        
        val productsWithStock = products.map { createProductWithStock(it) }
        
        val totalProducts = products.size
        val totalStockValue = products.sumOf { it.quantity_in_stock * it.priceBase }
        val lowStockProducts = productsWithStock.count { it.isLowStock }
        val outOfStockProducts = products.count { it.quantity_in_stock == 0 }
        
        val categoryInfoMap = mutableMapOf<String, CategoryStockInfo>()
        
        for (category in categories) {
            val categoryProducts = products.filter { it.categoryId == category.id }
            val categoryProductsWithStock = categoryProducts.map { createProductWithStock(it) }
            
            categoryInfoMap[category.nameAr] = CategoryStockInfo(
                categoryName = category.nameAr,
                totalProducts = categoryProducts.size,
                totalStockValue = categoryProducts.sumOf { it.quantity_in_stock * it.priceBase },
                lowStockCount = categoryProductsWithStock.count { it.isLowStock }
            )
        }
        
        return StockReport(
            totalProducts = totalProducts,
            totalStockValue = totalStockValue,
            lowStockProducts = lowStockProducts,
            outOfStockProducts = outOfStockProducts,
            categories = categoryInfoMap.values.toList(),
            generatedAt = System.currentTimeMillis()
        )
    }
    
    override suspend fun generateStockAnalytics(): StockAnalytics {
        val products = getAllProductsList()
        
        if (products.isEmpty()) {
            return StockAnalytics(
                averageStockValue = 0.0,
                highestValueProduct = null,
                lowestStockProduct = null,
                stockDistribution = emptyMap(),
                reorderAlerts = emptyList(),
                generatedAt = System.currentTimeMillis()
            )
        }
        
        val productsWithStock = products.map { createProductWithStock(it) }
        
        val totalValue = products.sumOf { it.quantity_in_stock * it.priceBase }
        val averageStockValue = totalValue / products.size
        
        val highestValueProduct = productsWithStock.maxByOrNull { it.stockValue }
        val lowestStockProduct = productsWithStock.minByOrNull { it.currentStock }
        
        // Stock distribution by ranges
        val distribution = mutableMapOf<String, Int>()
        distribution["0"] = products.count { it.quantity_in_stock == 0 }
        distribution["1-10"] = products.count { it.quantity_in_stock in 1..10 }
        distribution["11-50"] = products.count { it.quantity_in_stock in 11..50 }
        distribution["51-100"] = products.count { it.quantity_in_stock in 51..100 }
        distribution["100+"] = products.count { it.quantity_in_stock > 100 }
        
        val reorderAlerts = productsWithStock.filter { it.isLowStock }
        
        return StockAnalytics(
            averageStockValue = averageStockValue,
            highestValueProduct = highestValueProduct,
            lowestStockProduct = lowestStockProduct,
            stockDistribution = distribution,
            reorderAlerts = reorderAlerts,
            generatedAt = System.currentTimeMillis()
        )
    }
    
    override suspend fun exportInventoryToCsv(file: File): Boolean {
        return try {
            FileWriter(file).use { writer ->
                // Write CSV header
                writer.append("SKU,Name (AR),Name (EN),Category,Current Stock,Reorder Level,Unit Price,Stock Value,Low Stock\n")
                
                val products = getAllProductsList()
                val categories = getAllCategoriesList()
                val categoryMap = categories.associateBy { it.id }
                
                for (product in products) {
                    val categoryName = categoryMap[product.categoryId]?.nameAr ?: "Uncategorized"
                    val stockValue = product.quantity_in_stock * product.priceBase
                    val reorderLevel = getReorderLevel(product.id)
                    val isLowStock = product.quantity_in_stock <= reorderLevel
                    
                    writer.append("${escapeCsv(product.sku)},")
                    writer.append("${escapeCsv(product.nameAr)},")
                    writer.append("${escapeCsv(product.nameEn ?: "")},")
                    writer.append("${escapeCsv(categoryName)},")
                    writer.append("${product.quantity_in_stock},")
                    writer.append("$reorderLevel,")
                    writer.append("${"%.2f".format(product.priceBase)},")
                    writer.append("${"%.2f".format(stockValue)},")
                    writer.append("${if (isLowStock) "Yes" else "No"}\n")
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    override suspend fun getStockMovements(productId: Long): List<StockMovement> {
        // In a real implementation, you would query a stock movements table
        // For now, this is a placeholder that returns an empty list
        return emptyList()
    }
    
    override suspend fun getStockValueByCategory(): Map<String, Double> {
        val products = getAllProductsList()
        val categories = getAllCategoriesList()
        
        val categoryMap = categories.associateBy { it.id }
        val result = mutableMapOf<String, Double>()
        
        for (category in categories) {
            val categoryProducts = products.filter { it.categoryId == category.id }
            val totalValue = categoryProducts.sumOf { it.quantity_in_stock * it.priceBase }
            result[category.nameAr] = totalValue
        }
        
        // Add Uncategorized category
        val uncategorizedProducts = products.filter { it.categoryId == null }
        if (uncategorizedProducts.isNotEmpty()) {
            val totalValue = uncategorizedProducts.sumOf { it.quantity_in_stock * it.priceBase }
            result["Uncategorized"] = totalValue
        }
        
        return result
    }
    
    /**
     * Helper function to create ProductWithStock from Product
     */
    private suspend fun createProductWithStock(product: Product): ProductWithStock {
        val reorderLevel = getReorderLevel(product.id)
        val stockValue = product.quantity_in_stock * product.priceBase
        val isLowStock = product.quantity_in_stock <= reorderLevel
        
        return ProductWithStock(
            product = product,
            currentStock = product.quantity_in_stock,
            reorderLevel = reorderLevel,
            stockValue = stockValue,
            isLowStock = isLowStock
        )
    }
    
    /**
     * Escape CSV field values
     */
    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }
    
    /**
     * Helper to get all elements from Flow
     */
    private suspend fun getAllProductsList(): List<Product> {
        return try {
            var result: List<Product> = emptyList()
            productDao.getAllProducts().collect { products ->
                result = products
            }
            result
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Helper to get all categories from Flow
     */
    private suspend fun getAllCategoriesList(): List<Category> {
        return try {
            var result: List<Category> = emptyList()
            categoryDao.getAllCategories().collect { categories ->
                result = categories
            }
            result
        } catch (e: Exception) {
            emptyList()
        }
    }
}
