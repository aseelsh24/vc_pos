package com.aseel.pos.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY nameAr ASC")
    fun getAllProducts(): Flow<List<Product>>
    
    @Query("SELECT * FROM products WHERE categoryId = :categoryId ORDER BY nameAr ASC")
    fun getProductsByCategory(categoryId: Long): Flow<List<Product>>
    
    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Long): Product?
    
    @Query("SELECT * FROM products WHERE sku = :sku LIMIT 1")
    suspend fun getProductBySku(sku: String): Product?
    
    @Query("SELECT * FROM products WHERE nameAr LIKE '%' || :query || '%' OR nameEn LIKE '%' || :query || '%' OR sku LIKE '%' || :query || '%'")
    fun searchProducts(query: String): Flow<List<Product>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<Product>)
    
    @Update
    suspend fun updateProduct(product: Product)
    
    @Query("UPDATE products SET quantity_in_stock = quantity_in_stock - :qty WHERE id = :productId AND quantity_in_stock >= :qty")
    suspend fun decrementStock(productId: Long, qty: Int): Int
    
    @Query("UPDATE products SET quantity_in_stock = quantity_in_stock + :qty WHERE id = :productId")
    suspend fun incrementStock(productId: Long, qty: Int): Int
    
    @Query("UPDATE products SET quantity_in_stock = :quantity WHERE id = :productId")
    suspend fun setStockQuantity(productId: Long, quantity: Int): Int
}
