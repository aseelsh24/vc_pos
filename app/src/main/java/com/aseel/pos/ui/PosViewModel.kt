package com.aseel.pos.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aseel.pos.core.Currency
import com.aseel.pos.core.CurrencyFormatter
import com.aseel.pos.core.PaymentMethod
import com.aseel.pos.data.LineItem
import com.aseel.pos.data.PosSettings
import com.aseel.pos.data.Product
import com.aseel.pos.data.ProductRepository
import com.aseel.pos.data.SettingsRepository
import com.aseel.pos.data.Transaction
import com.aseel.pos.data.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CartItem(
    val product: Product,
    var quantity: Int = 1
) {
    val subtotal: Double get() = product.priceBase * quantity
}

@HiltViewModel
class PosViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val transactionRepository: TransactionRepository,
    private val settingsRepository: SettingsRepository,
    private val stockViewModel: StockViewModel
) : ViewModel() {
    
    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart.asStateFlow()
    
    private val _selectedCategory = MutableStateFlow<Long?>(null)
    val selectedCategory: StateFlow<Long?> = _selectedCategory.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _lowStockProductIds = MutableStateFlow<Set<Long>>(emptySet())
    val lowStockProductIds: StateFlow<Set<Long>> = _lowStockProductIds.asStateFlow()
    
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()
    
    val settings: StateFlow<PosSettings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, PosSettings())
    
    val categories = productRepository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    // Debounced search query for better performance
    private val _debouncedSearchQuery = MutableStateFlow("")
    val debouncedSearchQuery: StateFlow<String> = _debouncedSearchQuery.asStateFlow()
    
    val products = combine(
        _selectedCategory,
        _debouncedSearchQuery
    ) { category, query ->
        when {
            query.isNotBlank() -> productRepository.searchProducts(query)
            category != null -> productRepository.getProductsByCategory(category)
            else -> productRepository.getAllProducts()
        }
    }.flatMapLatest { it }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    // Monitor low stock products for alerts
    private val _lowStockProducts = MutableStateFlow<List<Product>>(emptyList())
    val lowStockProducts: StateFlow<List<Product>> = _lowStockProducts.asStateFlow()
    
    init {
        // Monitor low stock products
        viewModelScope.launch {
            stockViewModel.getLowStockProducts().collect { products ->
                _lowStockProducts.value = products
                _lowStockProductIds.value = products.map { it.id }.toSet()
            }
        }
    }
    
    fun addToCart(product: Product) {
        viewModelScope.launch {
            // Check if product has sufficient stock
            val hasStock = stockViewModel.hasSufficientStock(product.id.toInt(), 1)
            
            if (!hasStock) {
                _snackbarMessage.value = "Out of stock: ${product.nameAr}"
                return@launch
            }
            
            // Check if adding this item would make stock low
            val currentStock = product.quantity_in_stock
            if (currentStock < 10) {
                _snackbarMessage.value = "Low stock alert: ${product.nameAr} (${currentStock} left)"
            }
            
            val currentCart = _cart.value.toMutableList()
            val existing = currentCart.find { it.product.id == product.id }
            if (existing != null) {
                // Check if we can add more of this item
                if (stockViewModel.hasSufficientStock(product.id.toInt(), existing.quantity + 1)) {
                    existing.quantity++
                } else {
                    _snackbarMessage.value = "Insufficient stock for ${product.nameAr}"
                    return@launch
                }
            } else {
                currentCart.add(CartItem(product, 1))
            }
            _cart.value = currentCart
        }
    }
    
    fun updateQuantity(productId: Long, newQuantity: Int) {
        val currentCart = _cart.value.toMutableList()
        val item = currentCart.find { it.product.id == productId }
        if (item != null) {
            if (newQuantity <= 0) {
                currentCart.remove(item)
            } else {
                item.quantity = newQuantity
            }
            _cart.value = currentCart
        }
    }
    
    fun removeFromCart(productId: Long) {
        _cart.value = _cart.value.filter { it.product.id != productId }
    }
    
    fun clearCart() {
        _cart.value = emptyList()
    }
    
    fun selectCategory(categoryId: Long?) {
        _selectedCategory.value = categoryId
    }
    
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        // Debounce search to avoid too many database calls
        viewModelScope.launch {
            _debouncedSearchQuery.value = ""
            delay(300) // Wait 300ms before applying search
            _debouncedSearchQuery.value = query
        }
    }
    
    fun checkout(paymentMethod: PaymentMethod) {
        viewModelScope.launch {
            try {
                // Check if all items have sufficient stock
                for (cartItem in _cart.value) {
                    if (!stockViewModel.hasSufficientStock(
                            cartItem.product.id.toInt(),
                            cartItem.quantity
                        )) {
                        _snackbarMessage.value = "Insufficient stock for ${cartItem.product.nameAr}"
                        return@launch
                    }
                }
                
                // Deduct stock for all items
                for (cartItem in _cart.value) {
                    val stockResult = stockViewModel.deductStock(
                        cartItem.product.id.toInt(),
                        cartItem.quantity
                    )
                    if (stockResult.isFailure) {
                        _snackbarMessage.value = "Failed to deduct stock for ${cartItem.product.nameAr}"
                        return@launch
                    }
                }
                
                val currentSettings = settings.value
                val currency = Currency.fromCode(currentSettings.selectedCurrency)
                val rate = currentSettings.exchangeRates.getRate(currency)
                
                val lineItems = _cart.value.map {
                    LineItem(
                        productId = it.product.id,
                        productName = it.product.nameAr,
                        sku = it.product.sku,
                        qty = it.quantity,
                        unitPriceBase = it.product.priceBase,
                        subtotalBase = it.subtotal
                    )
                }
                
                val transaction = Transaction(
                    totalBase = _cart.value.sumOf { it.subtotal },
                    currencyCode = currency.code,
                    rateToBase = rate,
                    itemsJson = Transaction.serializeLineItems(lineItems),
                    paymentMethod = paymentMethod
                )
                
                transactionRepository.insertTransaction(transaction)
                clearCart()
                _snackbarMessage.value = "Transaction completed successfully!"
            } catch (e: Exception) {
                _snackbarMessage.value = "Transaction failed: ${e.message}"
            }
        }
    }
    
    fun getTotalFormatted(): String {
        val total = _cart.value.sumOf { it.subtotal }
        val currentSettings = settings.value
        val currency = Currency.fromCode(currentSettings.selectedCurrency)
        return CurrencyFormatter.format(total, currency, currentSettings.exchangeRates)
    }
    
    /**
     * Check if a product has low stock
     * @param productId The ID of the product to check
     * @return true if product has low stock (< 10 units), false otherwise
     */
    fun hasLowStock(productId: Long): Boolean {
        return _lowStockProductIds.value.contains(productId)
    }
    
    /**
     * Dismiss the current snackbar message
     */
    fun dismissSnackbar() {
        _snackbarMessage.value = null
    }
}
