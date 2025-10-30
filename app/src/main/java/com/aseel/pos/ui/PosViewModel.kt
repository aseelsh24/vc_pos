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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
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
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    
    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart.asStateFlow()
    
    private val _selectedCategory = MutableStateFlow<Long?>(null)
    val selectedCategory: StateFlow<Long?> = _selectedCategory.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    val settings: StateFlow<PosSettings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, PosSettings())
    
    val categories = productRepository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    val products = combine(
        _selectedCategory,
        _searchQuery
    ) { category, query ->
        when {
            query.isNotBlank() -> productRepository.searchProducts(query)
            category != null -> productRepository.getProductsByCategory(category)
            else -> productRepository.getAllProducts()
        }
    }.flatMapLatest { it }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    fun addToCart(product: Product) {
        val currentCart = _cart.value.toMutableList()
        val existing = currentCart.find { it.product.id == product.id }
        if (existing != null) {
            existing.quantity++
        } else {
            currentCart.add(CartItem(product, 1))
        }
        _cart.value = currentCart
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
    }
    
    fun checkout(paymentMethod: PaymentMethod) {
        viewModelScope.launch {
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
        }
    }
    
    fun getTotalFormatted(): String {
        val total = _cart.value.sumOf { it.subtotal }
        val currentSettings = settings.value
        val currency = Currency.fromCode(currentSettings.selectedCurrency)
        return CurrencyFormatter.format(total, currency, currentSettings.exchangeRates)
    }
}
