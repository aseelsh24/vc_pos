package com.aseel.pos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aseel.pos.core.Currency
import com.aseel.pos.core.CurrencyFormatter
import com.aseel.pos.core.PaymentMethod
import com.aseel.pos.data.Product
import com.aseel.pos.ui.CartItem
import com.aseel.pos.ui.PosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosScreen(
    onNavigateToTransactions: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: PosViewModel = hiltViewModel()
) {
    val cart by viewModel.cart.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val products by viewModel.products.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val settings by viewModel.settings.collectAsState()
    
    var showPaymentDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("نقطة البيع") },
                actions = {
                    IconButton(
                        onClick = onNavigateToTransactions,
                        modifier = Modifier.size(56.dp) // Ensure 48dp minimum touch target
                    ) {
                        Icon(Icons.Default.Receipt, "المعاملات")
                    }
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.size(56.dp) // Ensure 48dp minimum touch target
                    ) {
                        Icon(Icons.Default.Settings, "الإعدادات")
                    }
                }
            )
        }
    ) { padding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Cart Section (Left - 30%)
            Card(
                modifier = Modifier
                    .weight(0.3f)
                    .fillMaxHeight()
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "السلة",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(cart) { item ->
                            CartItemCard(
                                item = item,
                                currency = Currency.fromCode(settings.selectedCurrency),
                                rates = settings.exchangeRates,
                                onIncrement = { viewModel.updateQuantity(item.product.id, item.quantity + 1) },
                                onDecrement = { viewModel.updateQuantity(item.product.id, item.quantity - 1) },
                                onRemove = { viewModel.removeFromCart(item.product.id) }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    HorizontalDivider()
                    
                    Text(
                        text = "الإجمالي: ${viewModel.getTotalFormatted()}",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.clearCart() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إلغاء")
                        }
                        
                        Button(
                            onClick = { showPaymentDialog = true },
                            modifier = Modifier.weight(1f),
                            enabled = cart.isNotEmpty()
                        ) {
                            Text("دفع")
                        }
                    }
                }
            }
            
            // Products Section (Right - 70%)
            Column(
                modifier = Modifier
                    .weight(0.7f)
                    .fillMaxHeight()
                    .padding(8.dp)
            ) {
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    placeholder = { Text("بحث عن منتج...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    singleLine = true
                )
                
                // Category Tabs
                ScrollableTabRow(
                    selectedTabIndex = categories.indexOfFirst { it.id == selectedCategory }.takeIf { it >= 0 } ?: 0,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Tab(
                        selected = selectedCategory == null,
                        onClick = { viewModel.selectCategory(null) },
                        text = { Text("الكل") }
                    )
                    categories.forEach { category ->
                        Tab(
                            selected = selectedCategory == category.id,
                            onClick = { viewModel.selectCategory(category.id) },
                            text = { Text(category.nameAr) }
                        )
                    }
                }
                
                // Product Grid
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 150.dp),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(products) { product ->
                        ProductCard(
                            product = product,
                            currency = Currency.fromCode(settings.selectedCurrency),
                            rates = settings.exchangeRates,
                            onClick = { viewModel.addToCart(product) }
                        )
                    }
                }
            }
        }
    }
    
    if (showPaymentDialog) {
        PaymentDialog(
            total = viewModel.getTotalFormatted(),
            onDismiss = { showPaymentDialog = false },
            onConfirm = { paymentMethod ->
                viewModel.checkout(paymentMethod)
                showPaymentDialog = false
            }
        )
    }
}

@Composable
fun CartItemCard(
    item: CartItem,
    currency: Currency,
    rates: com.aseel.pos.core.ExchangeRates,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.product.nameAr,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = CurrencyFormatter.format(item.product.priceBase, currency, rates),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(56.dp) // Ensure 48dp minimum touch target
                ) {
                    Icon(Icons.Default.Delete, "حذف")
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onDecrement,
                        modifier = Modifier.size(56.dp) // Ensure 48dp minimum touch target
                    ) {
                        Icon(Icons.Default.Remove, "تقليل")
                    }
                    Text(
                        text = "${item.quantity}",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    IconButton(
                        onClick = onIncrement,
                        modifier = Modifier.size(56.dp) // Ensure 48dp minimum touch target
                    ) {
                        Icon(Icons.Default.Add, "زيادة")
                    }
                }
                Text(
                    text = CurrencyFormatter.format(item.subtotal, currency, rates),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductCard(
    product: Product,
    currency: Currency,
    rates: com.aseel.pos.core.ExchangeRates,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.8f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = product.nameAr,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Column {
                Text(
                    text = product.sku,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = CurrencyFormatter.format(product.priceBase, currency, rates),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                if (product.stockQty > 0) {
                    Text(
                        text = "المخزون: ${product.stockQty}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "غير متوفر",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun PaymentDialog(
    total: String,
    onDismiss: () -> Unit,
    onConfirm: (PaymentMethod) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("اختر طريقة الدفع") },
        text = {
            Column {
                Text("الإجمالي: $total", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))
                PaymentMethod.entries.forEach { method ->
                    Button(
                        onClick = { onConfirm(method) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            when (method) {
                                PaymentMethod.CASH -> "نقدي"
                                PaymentMethod.CARD -> "بطاقة"
                                PaymentMethod.TRANSFER -> "تحويل"
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
