package com.aseel.pos.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aseel.pos.data.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject

/**
 * Data class for product sales analytics
 */
data class ProductSalesAnalytics(
    val productId: Long,
    val productName: String,
    val sku: String,
    val totalQuantitySold: Int,
    val totalRevenue: Double,
    val numberOfTransactions: Int,
    val averageOrderSize: Double
)

/**
 * Comprehensive inventory report data
 */
data class InventoryReport(
    val stockReport: StockReport,
    val stockAnalytics: StockAnalytics,
    val salesAnalytics: List<ProductSalesAnalytics>,
    val totalStockValue: Double,
    val lowStockProducts: List<ProductWithStock>,
    val mostSoldProducts: List<ProductSalesAnalytics>,
    val leastSoldProducts: List<ProductSalesAnalytics>
)

@HiltViewModel
class InventoryReportViewModel @Inject constructor(
    private val stockRepository: StockRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {
    
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()
    
    private val _inventoryReport = MutableStateFlow<InventoryReport?>(null)
    val inventoryReport: StateFlow<InventoryReport?> = _inventoryReport.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    /**
     * Load inventory report data
     */
    fun loadInventoryReport() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _error.value = null
            
            try {
                // Calculate date range (last 30 days for sales analytics)
                val endDate = Instant.now()
                val startDate = endDate.minus(30, ChronoUnit.DAYS)
                
                // Get stock data
                val stockReport = stockRepository.generateStockReport()
                val stockAnalytics = stockRepository.generateStockAnalytics()
                val totalStockValue = stockRepository.calculateTotalStockValue()
                
                // Get low stock products
                val lowStockProducts = mutableListOf<ProductWithStock>()
                stockRepository.getLowStockProducts().collect { products ->
                    lowStockProducts.clear()
                    lowStockProducts.addAll(products)
                }
                
                // Get sales analytics
                val salesAnalytics = calculateSalesAnalytics(startDate.toEpochMilli(), endDate.toEpochMilli())
                val mostSoldProducts = salesAnalytics.sortedByDescending { it.totalQuantitySold }.take(5)
                val leastSoldProducts = salesAnalytics.sortedBy { it.totalQuantitySold }.take(5)
                
                val report = InventoryReport(
                    stockReport = stockReport,
                    stockAnalytics = stockAnalytics,
                    salesAnalytics = salesAnalytics,
                    totalStockValue = totalStockValue,
                    lowStockProducts = lowStockProducts,
                    mostSoldProducts = mostSoldProducts,
                    leastSoldProducts = leastSoldProducts
                )
                
                _inventoryReport.value = report
            } catch (e: Exception) {
                _error.value = "Failed to load inventory report: ${e.message}"
                e.printStackTrace()
            } finally {
                _isRefreshing.value = false
            }
        }
    }
    
    /**
     * Refresh inventory report
     */
    fun refresh() {
        loadInventoryReport()
    }
    
    /**
     * Export inventory to CSV
     */
    fun exportToCsv(
        context: android.content.Context,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val dir = context.getExternalFilesDir(null)
                val timestamp = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date())
                val file = File(dir, "inventory_report_$timestamp.csv")
                
                val success = stockRepository.exportInventoryToCsv(file)
                if (success) {
                    onResult(true, "Inventory exported to: ${file.absolutePath}")
                } else {
                    onResult(false, "Failed to export inventory to CSV")
                }
            } catch (e: Exception) {
                onResult(false, "Error: ${e.message}")
            }
        }
    }
    
    /**
     * Calculate sales analytics from transactions
     */
    private suspend fun calculateSalesAnalytics(startDate: Long, endDate: Long): List<ProductSalesAnalytics> {
        return try {
            val transactions = mutableListOf<Transaction>()
            transactionRepository.getTransactionsByDateRange(startDate, endDate).collect { txs ->
                transactions.clear()
                transactions.addAll(txs)
            }
            
            val productSalesMap = mutableMapOf<Long, ProductSalesData>()
            
            for (transaction in transactions) {
                val lineItems = transaction.getLineItems()
                for (item in lineItems) {
                    val existing = productSalesMap[item.productId]
                    if (existing == null) {
                        productSalesMap[item.productId] = ProductSalesData(
                            productId = item.productId,
                            productName = item.productName,
                            sku = item.sku,
                            totalQuantitySold = item.qty,
                            totalRevenue = item.subtotalBase,
                            numberOfTransactions = 1
                        )
                    } else {
                        productSalesMap[item.productId] = existing.copy(
                            totalQuantitySold = existing.totalQuantitySold + item.qty,
                            totalRevenue = existing.totalRevenue + item.subtotalBase,
                            numberOfTransactions = existing.numberOfTransactions + 1
                        )
                    }
                }
            }
            
            productSalesMap.values.map { data ->
                ProductSalesAnalytics(
                    productId = data.productId,
                    productName = data.productName,
                    sku = data.sku,
                    totalQuantitySold = data.totalQuantitySold,
                    totalRevenue = data.totalRevenue,
                    numberOfTransactions = data.numberOfTransactions,
                    averageOrderSize = data.totalQuantitySold.toDouble() / data.numberOfTransactions
                )
            }.sortedBy { it.productName }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    /**
     * Get stock alerts (products below reorder level)
     */
    fun getStockAlerts(): Flow<List<ProductWithStock>> {
        return stockRepository.getLowStockProducts()
    }
    
    /**
     * Get all products with stock information
     */
    fun getAllProductsWithStock(): Flow<List<ProductWithStock>> {
        return stockRepository.getAllProductsWithStock()
    }
    
    /**
     * Get stock value by category
     */
    fun getStockValueByCategory(): Flow<Map<String, Double>> {
        return flow {
            try {
                val valueByCategory = stockRepository.getStockValueByCategory()
                emit(valueByCategory)
            } catch (e: Exception) {
                emit(emptyMap())
            }
        }
    }
}

/**
 * Helper data class for sales calculations
 */
private data class ProductSalesData(
    val productId: Long,
    val productName: String,
    val sku: String,
    var totalQuantitySold: Int,
    var totalRevenue: Double,
    var numberOfTransactions: Int
)
