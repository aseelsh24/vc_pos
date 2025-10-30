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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass.*
import com.aseel.pos.core.Currency
import com.aseel.pos.core.CurrencyFormatter
import com.aseel.pos.core.PaymentMethod
import com.aseel.pos.data.Product
import com.aseel.pos.ui.CartItem
import com.aseel.pos.ui.PosViewModel

/**
 * Get current window size class for responsive design
 */
@Composable
fun getWindowSizeClass(): WindowSizeClass {
    val windowInfo = LocalWindowInfo.current
    return WindowSizeClass.compute(
        widthDp = windowInfo.widthDp,
        heightDp = windowInfo.heightDp
    )
}

/**
 * Main POS screen with adaptive layout
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdaptivePosScreen(
    onNavigateToTransactions: () -> Unit,
    onNavigateToInventoryReport: () -> Unit,
    onNavigateToProductManagement: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: PosViewModel = hiltViewModel()
) {
    val windowSizeClass = getWindowSizeClass()
    
    when (windowSizeClass.widthSizeClass) {
        Compact -> {
            // Phone layout - single screen navigation
            CompactPosLayout(
                onNavigateToTransactions = onNavigateToTransactions,
                onNavigateToInventoryReport = onNavigateToInventoryReport,
                onNavigateToProductManagement = onNavigateToProductManagement,
                onNavigateToSettings = onNavigateToSettings,
                viewModel = viewModel
            )
        }
        Medium -> {
            // Small tablet layout - adapted single screen
            TabletPosLayout(
                onNavigateToTransactions = onNavigateToTransactions,
                onNavigateToInventoryReport = onNavigateToInventoryReport,
                onNavigateToProductManagement = onNavigateToProductManagement,
                onNavigateToSettings = onNavigateToSettings,
                viewModel = viewModel,
                isLargeTablet = false
            )
        }
        Expanded -> {
            // Large tablet layout - two-pane master-detail
            LargeTabletPosLayout(
                onNavigateToTransactions = onNavigateToTransactions,
                onNavigateToInventoryReport = onNavigateToInventoryReport,
                onNavigateToProductManagement = onNavigateToProductManagement,
                onNavigateToSettings = onNavigateToSettings,
                viewModel = viewModel
            )
        }
    }
}

/**
 * Compact layout for phones
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompactPosLayout(
    onNavigateToTransactions: () -> Unit,
    onNavigateToInventoryReport: () -> Unit,
    onNavigateToProductManagement: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: PosViewModel
) {
    var currentScreen by remember { mutableStateOf("pos") }
    
    when (currentScreen) {
        "pos" -> {
            CompactPosMainScreen(
                onNavigateToTransactions = { currentScreen = "transactions" },
                onNavigateToInventoryReport = { currentScreen = "inventory_report" },
                onNavigateToProductManagement = { currentScreen = "product_management" },
                onNavigateToSettings = { currentScreen = "settings" },
                viewModel = viewModel
            )
        }
        "transactions" -> {
            TransactionsScreen(
                onNavigateBack = { currentScreen = "pos" },
                onNavigateToSettings = { currentScreen = "settings" }
            )
        }
        "inventory_report" -> {
            InventoryReportScreen(
                onNavigateBack = { currentScreen = "pos" },
                onNavigateToProductManagement = { currentScreen = "product_management" }
            )
        }
        "product_management" -> {
            ProductManagementScreen(
                onNavigateBack = { currentScreen = "pos" }
            )
        }
        "settings" -> {
            SettingsScreen(
                onNavigateBack = { currentScreen = "pos" },
                onNavigateToTransactions = { currentScreen = "transactions" },
                onNavigateToProductManagement = { currentScreen = "product_management" }
            )
        }
    }
}

/**
 * Tablet layout for small-medium tablets
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabletPosLayout(
    onNavigateToTransactions: () -> Unit,
    onNavigateToInventoryReport: () -> Unit,
    onNavigateToProductManagement: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: PosViewModel,
    isLargeTablet: Boolean = false
) {
    val cart by viewModel.cart.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val products by viewModel.products.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val stockViewModel: com.aseel.pos.ui.StockViewModel = hiltViewModel()
    val lowStockProducts by stockViewModel.getLowStockProducts().collectAsState(initial = emptyList())
    val lowStockCount = lowStockProducts.size
    
    var showPaymentDialog by remember { mutableStateOf(false) }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "نقطة البيع",
                        modifier = Modifier.semantics {
                            contentDescription = "نقطة البيع - القائمة الرئيسية"
                        }
                    )
                },
                actions = {
                    // Show barcode scanner button only if enabled in settings
                    if (settings.enableBarcodeScanner) {
                        IconButton(
                            onClick = { /* TODO: Navigate to barcode scanner */ },
                            modifier = Modifier.size(56.dp).semantics {
                                contentDescription = "مسح الباركود"
                            }
                        ) {
                            Icon(Icons.Default.QrCode, "مسح الباركود", modifier = Modifier.semantics {
                                contentDescription = "أيقونة مسح الباركود"
                            })
                        }
                    }
                    IconButton(
                        onClick = onNavigateToTransactions,
                        modifier = Modifier.size(56.dp).semantics {
                            contentDescription = "المعاملات السابقة"
                        }
                    ) {
                        Icon(Icons.Default.Receipt, "المعاملات", modifier = Modifier.semantics {
                            contentDescription = "عرض سجل المعاملات السابقة"
                        })
                    }
                    IconButton(
                        onClick = onNavigateToInventoryReport,
                        modifier = Modifier.size(56.dp).semantics {
                            contentDescription = "تقرير المخزون"
                        }
                    ) {
                        Icon(Icons.Default.Inventory, "تقرير المخزون", modifier = Modifier.semantics {
                            contentDescription = "عرض تقرير المخزون والمبيعات"
                        })
                    }
                    // Low Stock Notification Badge
                    BadgedBox(
                        badge = {
                            if (lowStockCount > 0) {
                                Badge(
                                    modifier = Modifier.semantics {
                                        contentDescription = "$lowStockCount منتج بمخزون منخفض"
                                    }
                                ) {
                                    Text(lowStockCount.toString())
                                }
                            }
                        },
                        modifier = Modifier.semantics {
                            contentDescription = if (lowStockCount > 0) {
                                "تنبيه: $lowStockCount منتج بمخزون منخفض"
                            } else {
                                "جميع المنتجات لديها مخزون كافي"
                            }
                        }
                    ) {
                        IconButton(
                            onClick = onNavigateToInventoryReport,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(
                                Icons.Default.Inventory, 
                                "تقرير المخزون",
                                modifier = Modifier.semantics {
                                    contentDescription = if (lowStockCount > 0) {
                                        "تقرير المخزون - يوجد $lowStockCount منتج بمخزون منخفض"
                                    } else {
                                        "تقرير المخزون"
                                    }
                                }
                            )
                        }
                    }
                    IconButton(
                        onClick = onNavigateToProductManagement,
                        modifier = Modifier.size(56.dp).semantics {
                            contentDescription = "إدارة المنتجات"
                        }
                    ) {
                        Icon(Icons.Default.ManageSearch, "إدارة المنتجات", modifier = Modifier.semantics {
                            contentDescription = "إدارة المنتجات والمخزون"
                        })
                    }
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.size(56.dp).semantics {
                            contentDescription = "الإعدادات"
                        }
                    ) {
                        Icon(Icons.Default.Settings, "الإعدادات", modifier = Modifier.semantics {
                            contentDescription = "إعدادات التطبيق والعملة"
                        })
                    }
                }
            )
        }
    ) { padding ->
        if (isLargeTablet) {
            // Large tablet - two panels
            TwoPanelLayout(
                padding = padding,
                cart = cart,
                products = products,
                categories = categories,
                selectedCategory = selectedCategory,
                searchQuery = searchQuery,
                settings = settings,
                selectedProduct = selectedProduct,
                onProductSelected = { selectedProduct = it },
                onAddToCart = { viewModel.addToCart(it) },
                onUpdateQuantity = { id, qty -> viewModel.updateQuantity(id, qty) },
                onRemoveFromCart = { id -> viewModel.removeFromCart(id) },
                onClearCart = { viewModel.clearCart() },
                onCheckout = { showPaymentDialog = true },
                onSearchQueryChange = { viewModel.setSearchQuery(it) },
                onCategorySelected = { viewModel.selectCategory(it) },
                getTotalFormatted = { viewModel.getTotalFormatted() }
            )
        } else {
            // Small tablet - adapted single screen
            SinglePanelTabletLayout(
                padding = padding,
                cart = cart,
                products = products,
                categories = categories,
                selectedCategory = selectedCategory,
                searchQuery = searchQuery,
                settings = settings,
                selectedProduct = selectedProduct,
                onProductSelected = { selectedProduct = it },
                onAddToCart = { viewModel.addToCart(it) },
                onUpdateQuantity = { id, qty -> viewModel.updateQuantity(id, qty) },
                onRemoveFromCart = { id -> viewModel.removeFromCart(id) },
                onClearCart = { viewModel.clearCart() },
                onCheckout = { showPaymentDialog = true },
                onSearchQueryChange = { viewModel.setSearchQuery(it) },
                onCategorySelected = { viewModel.selectCategory(it) },
                getTotalFormatted = { viewModel.getTotalFormatted() }
            )
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

/**
 * Large tablet layout with two panels
 */
@Composable
fun TwoPanelLayout(
    padding: PaddingValues,
    cart: List<CartItem>,
    products: List<Product>,
    categories: List<com.aseel.pos.data.Category>,
    selectedCategory: Long?,
    searchQuery: String,
    settings: com.aseel.pos.data.PosSettings,
    selectedProduct: Product?,
    onProductSelected: (Product) -> Unit,
    onAddToCart: (Product) -> Unit,
    onUpdateQuantity: (Long, Int) -> Unit,
    onRemoveFromCart: (Long) -> Unit,
    onClearCart: () -> Unit,
    onCheckout: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onCategorySelected: (Long?) -> Unit,
    getTotalFormatted: () -> String
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        // Left panel - Product list and search (60%)
        Column(
            modifier = Modifier
                .weight(0.6f)
                .fillMaxHeight()
                .padding(8.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .semantics {
                        contentDescription = "بحث عن المنتجات بالاسم أو الرمز"
                    },
                placeholder = { 
                    Text(
                        "بحث عن منتج...",
                        modifier = Modifier.semantics {
                            contentDescription = "اكتب اسم أو رمز المنتج للبحث"
                        }
                    ) 
                },
                leadingIcon = { 
                    Icon(
                        Icons.Default.Search, 
                        null,
                        modifier = Modifier.semantics {
                            contentDescription = "أيقونة البحث"
                        }
                    ) 
                },
                singleLine = true
            )
            
            // Category Tabs
            ScrollableTabRow(
                selectedTabIndex = categories.indexOfFirst { it.id == selectedCategory }.takeIf { it >= 0 } ?: 0,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Tab(
                    selected = selectedCategory == null,
                    onClick = { onCategorySelected(null) },
                    modifier = Modifier.semantics {
                        contentDescription = "جميع الفئات"
                    }
                ) {
                    Text("الكل")
                }
                categories.forEach { category ->
                    Tab(
                        selected = selectedCategory == category.id,
                        onClick = { onCategorySelected(category.id) },
                        modifier = Modifier.semantics {
                            contentDescription = "فئة ${category.nameAr}"
                        }
                    ) {
                        Text(category.nameAr)
                    }
                }
            }
            
            // Product Grid
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 180.dp),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(products) { product ->
                    AccessibleProductCard(
                        product = product,
                        currency = Currency.fromCode(settings.selectedCurrency),
                        rates = settings.exchangeRates,
                        onClick = { 
                            onProductSelected(product)
                            onAddToCart(product)
                        },
                        isSelected = selectedProduct?.id == product.id
                    )
                }
            }
        }
        
        // Right panel - Cart (40%)
        AccessibleCartPanel(
            modifier = Modifier.weight(0.4f),
            cart = cart,
            settings = settings,
            onUpdateQuantity = onUpdateQuantity,
            onRemoveFromCart = onRemoveFromCart,
            onClearCart = onClearCart,
            onCheckout = onCheckout,
            getTotalFormatted = getTotalFormatted
        )
    }
}/**
 * Accessible product card with proper semantics
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessibleProductCard(
    product: Product,
    currency: Currency,
    rates: com.aseel.pos.core.ExchangeRates,
    onClick: () -> Unit,
    isSelected: Boolean = false
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.8f)
            .semantics {
                contentDescription = "منتج: ${product.nameAr}. السعر: ${CurrencyFormatter.format(product.priceBase, currency, rates)}. المخزون: ${product.stockQty} قطعة${if (product.stockQty > 0) "" else " - غير متوفر"}"
                testTag = "product_card_${product.id}"
            },
        colors = if (isSelected) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        } else {
            CardDefaults.cardColors()
        }
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
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.semantics {
                    contentDescription = "اسم المنتج: ${product.nameAr}"
                }
            )
            Column {
                Text(
                    text = "الرمز: ${product.sku}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.semantics {
                        contentDescription = "رمز المنتج: ${product.sku}"
                    }
                )
                Text(
                    text = CurrencyFormatter.format(product.priceBase, currency, rates),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.semantics {
                        contentDescription = "السعر: ${CurrencyFormatter.format(product.priceBase, currency, rates)}"
                    }
                )
                if (product.stockQty > 0) {
                    Text(
                        text = "المخزون: ${product.stockQty} قطعة",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.semantics {
                            contentDescription = "الكمية المتوفرة: ${product.stockQty} قطعة"
                        }
                    )
                } else {
                    Text(
                        text = "غير متوفر",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.semantics {
                            contentDescription = "المنتج غير متوفر حالياً"
                        }
                    )
                }
            }
        }
    }
}

/**
 * Accessible cart panel with proper semantics
 */
@Composable
fun AccessibleCartPanel(
    modifier: Modifier = Modifier,
    cart: List<CartItem>,
    settings: com.aseel.pos.data.PosSettings,
    onUpdateQuantity: (Long, Int) -> Unit,
    onRemoveFromCart: (Long) -> Unit,
    onClearCart: () -> Unit,
    onCheckout: () -> Unit,
    getTotalFormatted: () -> String
) {
    val currency = Currency.fromCode(settings.selectedCurrency)
    
    Card(
        modifier = modifier
            .fillMaxHeight()
            .padding(8.dp)
            .semantics {
                contentDescription = "سلة التسوق. تحتوي على ${cart.size} منتج${if (cart.size != 1) "ات" else ""}"
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "سلة التسوق",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
                    .semantics {
                        contentDescription = "سلة التسوق"
                    }
            )
            
            if (cart.isEmpty()) {
                Text(
                    text = "السلة فارغة",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(32.dp)
                        .semantics {
                            contentDescription = "السلة فارغة - أضف منتجات من القائمة اليسرى"
                        }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(cart) { item ->
                        AccessibleCartItemCard(
                            item = item,
                            currency = currency,
                            rates = settings.exchangeRates,
                            onIncrement = { onUpdateQuantity(item.product.id, item.quantity + 1) },
                            onDecrement = { onUpdateQuantity(item.product.id, item.quantity - 1) },
                            onRemove = { onRemoveFromCart(item.product.id) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                HorizontalDivider()
                
                Text(
                    text = "الإجمالي: ${getTotalFormatted()}",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .semantics {
                            contentDescription = "الإجمالي: ${getTotalFormatted()}"
                        }
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onClearCart,
                        modifier = Modifier.weight(1f).semantics {
                            contentDescription = "إفراغ السلة"
                        }
                    ) {
                        Text("إلغاء")
                    }
                    
                    Button(
                        onClick = onCheckout,
                        modifier = Modifier.weight(1f).semantics {
                            contentDescription = "إتمام الدفع"
                        },
                        enabled = cart.isNotEmpty()
                    ) {
                        Text("دفع")
                    }
                }
            }
        }
    }
}

/**
 * Accessible cart item with proper semantics
 */
@Composable
fun AccessibleCartItemCard(
    item: CartItem,
    currency: Currency,
    rates: com.aseel.pos.core.ExchangeRates,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().semantics {
            contentDescription = "${item.product.nameAr}. الكمية: ${item.quantity}. السعر الفرعي: ${CurrencyFormatter.format(item.subtotal, currency, rates)}"
            testTag = "cart_item_${item.product.id}"
        }
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
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.semantics {
                            contentDescription = "المنتج: ${item.product.nameAr}"
                        }
                    )
                    Text(
                        text = CurrencyFormatter.format(item.product.priceBase, currency, rates),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.semantics {
                            contentDescription = "سعر الوحدة: ${CurrencyFormatter.format(item.product.priceBase, currency, rates)}"
                        }
                    )
                }
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(56.dp).semantics {
                        contentDescription = "حذف ${item.product.nameAr} من السلة"
                    }
                ) {
                    Icon(Icons.Default.Delete, "حذف", modifier = Modifier.semantics {
                        contentDescription = "حذف المنتج من السلة"
                    })
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
                        modifier = Modifier.size(56.dp).semantics {
                            contentDescription = "تقليل كمية ${item.product.nameAr}"
                        }
                    ) {
                        Icon(Icons.Default.Remove, "تقليل", modifier = Modifier.semantics {
                            contentDescription = "تقليل الكمية"
                        })
                    }
                    Text(
                        text = "${item.quantity}",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp)
                            .semantics {
                                contentDescription = "الكمية الحالية: ${item.quantity}"
                            }
                    )
                    IconButton(
                        onClick = onIncrement,
                        modifier = Modifier.size(56.dp).semantics {
                            contentDescription = "زيادة كمية ${item.product.nameAr}"
                        }
                    ) {
                        Icon(Icons.Default.Add, "زيادة", modifier = Modifier.semantics {
                            contentDescription = "زيادة الكمية"
                        })
                    }
                }
                Text(
                    text = CurrencyFormatter.format(item.subtotal, currency, rates),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.semantics {
                        contentDescription = "المجموع الفرعي: ${CurrencyFormatter.format(item.subtotal, currency, rates)}"
                    }
                )
            }
        }
    }
}

/**
 * Single panel tablet layout
 */
@Composable
fun SinglePanelTabletLayout(
    padding: PaddingValues,
    cart: List<CartItem>,
    products: List<Product>,
    categories: List<com.aseel.pos.data.Category>,
    selectedCategory: Long?,
    searchQuery: String,
    settings: com.aseel.pos.data.PosSettings,
    selectedProduct: Product?,
    onProductSelected: (Product) -> Unit,
    onAddToCart: (Product) -> Unit,
    onUpdateQuantity: (Long, Int) -> Unit,
    onRemoveFromCart: (Long) -> Unit,
    onClearCart: () -> Unit,
    onCheckout: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onCategorySelected: (Long?) -> Unit,
    getTotalFormatted: () -> String
) {
    // Use the existing PosScreen but with enhanced accessibility
    // This is a simplified version - you could also enhance the existing PosScreen
    PosScreen(
        onNavigateToTransactions = { },
        onNavigateToSettings = { },
        viewModel = hiltViewModel()
    )
}

/**
 * Large tablet layout
 */
@Composable
fun LargeTabletPosLayout(
    onNavigateToTransactions: () -> Unit,
    onNavigateToInventoryReport: () -> Unit,
    onNavigateToProductManagement: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: PosViewModel
) {
    val cart by viewModel.cart.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val products by viewModel.products.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val stockViewModel: com.aseel.pos.ui.StockViewModel = hiltViewModel()
    val lowStockProducts by stockViewModel.getLowStockProducts().collectAsState(initial = emptyList())
    val lowStockCount = lowStockProducts.size
    
    var showPaymentDialog by remember { mutableStateOf(false) }
    var selectedProduct by remember { mutableStateOf<com.aseel.pos.data.Product?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "نقطة البيع",
                        modifier = Modifier.semantics {
                            contentDescription = "نقطة البيع - واجهة لوحة التحكم"
                        }
                    )
                },
                actions = {
                    // Show barcode scanner button only if enabled in settings
                    if (settings.enableBarcodeScanner) {
                        IconButton(
                            onClick = { /* TODO: Navigate to barcode scanner */ },
                            modifier = Modifier.size(56.dp).semantics {
                                contentDescription = "مسح الباركود"
                            }
                        ) {
                            Icon(
                                Icons.Default.QrCode, 
                                "مسح الباركود",
                                modifier = Modifier.semantics {
                                    contentDescription = "أيقونة مسح الباركود"
                                }
                            )
                        }
                    }
                    IconButton(
                        onClick = onNavigateToTransactions,
                        modifier = Modifier.size(56.dp).semantics {
                            contentDescription = "عرض سجل المعاملات"
                        }
                    ) {
                        Icon(
                            Icons.Default.Receipt, 
                            "المعاملات",
                            modifier = Modifier.semantics {
                                contentDescription = "أيقونة سجل المعاملات"
                            }
                        )
                    }
                    IconButton(
                        onClick = onNavigateToInventoryReport,
                        modifier = Modifier.size(56.dp).semantics {
                            contentDescription = "تقرير المخزون"
                        }
                    ) {
                        Icon(
                            Icons.Default.Inventory, 
                            "تقرير المخزون",
                            modifier = Modifier.semantics {
                                contentDescription = "أيقونة تقرير المخزون"
                            }
                        )
                    }
                    IconButton(
                        onClick = onNavigateToProductManagement,
                        modifier = Modifier.size(56.dp).semantics {
                            contentDescription = "إدارة المنتجات"
                        }
                    ) {
                        Icon(
                            Icons.Default.ManageSearch, 
                            "إدارة المنتجات",
                            modifier = Modifier.semantics {
                                contentDescription = "أيقونة إدارة المنتجات"
                            }
                        )
                    }
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.size(56.dp).semantics {
                            contentDescription = "فتح إعدادات التطبيق"
                        }
                    ) {
                        Icon(
                            Icons.Default.Settings, 
                            "الإعدادات",
                            modifier = Modifier.semantics {
                                contentDescription = "أيقونة الإعدادات"
                            }
                        )
                    }
                }
            )
        }
    ) { padding ->
        TwoPanelLayout(
            padding = padding,
            cart = cart,
            products = products,
            categories = categories,
            selectedCategory = selectedCategory,
            searchQuery = searchQuery,
            settings = settings,
            selectedProduct = selectedProduct,
            onProductSelected = { selectedProduct = it },
            onAddToCart = { viewModel.addToCart(it) },
            onUpdateQuantity = { id, qty -> viewModel.updateQuantity(id, qty) },
            onRemoveFromCart = { id -> viewModel.removeFromCart(id) },
            onClearCart = { viewModel.clearCart() },
            onCheckout = { showPaymentDialog = true },
            onSearchQueryChange = { viewModel.setSearchQuery(it) },
            onCategorySelected = { viewModel.selectCategory(it) },
            getTotalFormatted = { viewModel.getTotalFormatted() }
        )
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