package com.aseel.pos.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(
    private val productDao: ProductDao,
    private val categoryDao: CategoryDao
) {
    fun getAllProducts(): Flow<List<Product>> = productDao.getAllProducts()
    
    fun getProductsByCategory(categoryId: Long): Flow<List<Product>> = 
        productDao.getProductsByCategory(categoryId)
    
    suspend fun getProductById(id: Long): Product? = productDao.getProductById(id)
    
    suspend fun getProductBySku(sku: String): Product? = productDao.getProductBySku(sku)
    
    fun searchProducts(query: String): Flow<List<Product>> = productDao.searchProducts(query)
    
    suspend fun insertProduct(product: Product): Long = productDao.insertProduct(product)
    
    suspend fun decrementStock(productId: Long, qty: Int): Boolean {
        return productDao.decrementStock(productId, qty) > 0
    }
    
    suspend fun incrementStock(productId: Long, qty: Int): Boolean {
        return productDao.incrementStock(productId, qty) > 0
    }
    
    suspend fun setStockQuantity(productId: Long, quantity: Int): Boolean {
        return productDao.setStockQuantity(productId, quantity) > 0
    }
    
    fun getAllCategories(): Flow<List<Category>> = categoryDao.getAllCategories()
    
    suspend fun getCategoryById(id: Long): Category? = categoryDao.getCategoryById(id)
}
