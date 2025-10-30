package com.aseel.pos.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aseel.pos.data.Product
import com.aseel.pos.data.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Data class to hold stock information for inventory reports
 */
data class StockInfo(
    val productId: Long,
    val sku: String,
    val productName: String,
    val currentStock: Int,
    val isLowStock: Boolean,
    val stockValue: Double
)

@HiltViewModel
class StockViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {
    
    /**
     * Check if a product has stock available
     * @param productId The ID of the product to check
     * @return true if product has stock (quantity > 0), false otherwise
     */
    suspend fun checkStock(productId: Int): Boolean {
        return try {
            val product = productRepository.getProductById(productId.toLong())
            product?.quantity_in_stock ?: 0 > 0
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Deduct stock for a product (e.g., after a sale)
     * @param productId The ID of the product
     * @param quantity The quantity to deduct
     * @return Result.success(Unit) if successful, Result.failure(Exception) if failed
     */
    suspend fun deductStock(productId: Int, quantity: Int): Result<Unit> {
        return try {
            if (quantity <= 0) {
                return Result.failure(IllegalArgumentException("Quantity must be greater than 0"))
            }
            
            val success = productRepository.decrementStock(productId.toLong(), quantity)
            if (!success) {
                return Result.failure(IllegalStateException("Insufficient stock or product not found"))
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Add stock to a product (e.g., when receiving new inventory)
     * @param productId The ID of the product
     * @param quantity The quantity to add
     * @return Result.success(Unit) if successful, Result.failure(Exception) if failed
     */
    suspend fun addStock(productId: Int, quantity: Int): Result<Unit> {
        return try {
            if (quantity <= 0) {
                return Result.failure(IllegalArgumentException("Quantity must be greater than 0"))
            }
            
            val success = productRepository.incrementStock(productId.toLong(), quantity)
            if (!success) {
                return Result.failure(IllegalStateException("Product not found"))
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get all products with low stock (below 10 units)
     * @return Flow of products with low stock for alerts
     */
    fun getLowStockProducts(): Flow<List<Product>> {
        return productRepository.getAllProducts()
            .map { products ->
                products.filter { it.quantity_in_stock < 10 }
            }
    }
    
    /**
     * Get all stock information for inventory reports
     * @return Flow of StockInfo objects with comprehensive stock data
     */
    fun getAllStockInfo(): Flow<List<StockInfo>> {
        return productRepository.getAllProducts()
            .map { products ->
                products.map { product ->
                    StockInfo(
                        productId = product.id,
                        sku = product.sku,
                        productName = product.nameAr,
                        currentStock = product.quantity_in_stock,
                        isLowStock = product.quantity_in_stock < 10,
                        stockValue = product.priceBase * product.quantity_in_stock
                    )
                }.sortedBy { it.productName }
            }
    }
    
    /**
     * Get stock info for a specific product
     * @param productId The ID of the product
     * @return Flow of single StockInfo object
     */
    fun getStockInfoForProduct(productId: Int): Flow<StockInfo?> {
        return productRepository.getAllProducts()
            .map { products ->
                products.find { it.id == productId.toLong() }?.let { product ->
                    StockInfo(
                        productId = product.id,
                        sku = product.sku,
                        productName = product.nameAr,
                        currentStock = product.quantity_in_stock,
                        isLowStock = product.quantity_in_stock < 10,
                        stockValue = product.priceBase * product.quantity_in_stock
                    )
                }
            }
    }
    
    /**
     * Check if a product has sufficient stock for a requested quantity
     * @param productId The ID of the product
     * @param requestedQuantity The quantity being requested
     * @return true if sufficient stock, false otherwise
     */
    suspend fun hasSufficientStock(productId: Int, requestedQuantity: Int): Boolean {
        return try {
            val product = productRepository.getProductById(productId.toLong())
            product?.quantity_in_stock ?: 0 >= requestedQuantity
        } catch (e: Exception) {
            false
        }
    }
}
