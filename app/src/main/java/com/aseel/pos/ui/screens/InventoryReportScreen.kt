package com.aseel.pos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aseel.pos.core.CurrencyFormatter
import com.aseel.pos.data.ProductWithStock
import com.aseel.pos.ui.InventoryReportViewModel
import com.aseel.pos.ui.ProductSalesAnalytics
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryReportScreen(
    onNavigateBack: () -> Unit,
    onNavigateToProductManagement: () -> Unit,
    viewModel: InventoryReportViewModel = hiltViewModel()
) {
    val inventoryReport by viewModel.inventoryReport.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val error by viewModel.error.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var exportMessage by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    
    // Load data on first composition
    LaunchedEffect(Unit) {
        viewModel.loadInventoryReport()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تقرير المخزون") },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.semantics {
                            contentDescription = "Navigate back"
                        }
                    ) {
                        Icon(Icons.Default.ArrowBack, "رجوع")
                    }
                },
                actions = {
                    // Refresh button
                    IconButton(
                        onClick = { viewModel.refresh() },
                        enabled = !isRefreshing,
                        modifier = Modifier.semantics {
                            contentDescription = "Refresh inventory report"
                        }
                    ) {
                        if (isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Refresh, "تحديث")
                        }
                    }
                    
                    // Export CSV button
                    IconButton(
                        onClick = {
                            viewModel.exportToCsv(context) { success, message ->
                                exportMessage = Pair(success, message)
                            }
                        },
                        modifier = Modifier.semantics {
                            contentDescription = "Export inventory to CSV"
                        }
                    ) {
                        Icon(Icons.Default.FileDownload, "تصدير CSV")
                    }
                    
                    // Product Management button
                    IconButton(
                        onClick = onNavigateToProductManagement,
                        modifier = Modifier.semantics {
                            contentDescription = "Navigate to Product Management"
                        }
                    ) {
                        Icon(Icons.Default.ManageSearch, "إدارة المنتجات")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isRefreshing && inventoryReport == null) {
                // Loading state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.semantics {
                                contentDescription = "Loading inventory report"
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "جاري تحميل تقرير المخزون...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else if (error != null) {
                // Error state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = error!!,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.refresh() }
                        ) {
                            Text("إعادة المحاولة")
                        }
                    }
                }
            } else if (inventoryReport != null) {
                // Success state - show report
                InventoryReportContent(
                    inventoryReport = inventoryReport!!,
                    isRefreshing = isRefreshing
                )
            }
            
            // Export message snackbar
            exportMessage?.let { (success, message) ->
                LaunchedEffect(message) {
                    scope.launch {
                        // Show snackbar and auto-dismiss after 5 seconds
                        val snackbarResult = androidx.compose.material.remember androidx.compose.material.SnackbarHostState
                        androidx.compose.material.SnackbarHostState()
                    }
                }
                
                // Show snackbar
                androidx.compose.material.SnackbarHost(
                    hostState = remember { androidx.compose.material.SnackbarHostState() }
                ) { snackbarData ->
                    androidx.compose.material.Snackbar(
                        snackbarData = snackbarData,
                        modifier = Modifier.semantics {
                            contentDescription = "Export result: $message"
                        }
                    )
                }
                
                DisposableEffect(Unit) {
                    val snackbarHostState = androidx.compose.material.remember { androidx.compose.material.SnackbarHostState() }
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = message,
                            duration = androidx.compose.material.SnackbarDuration.Long
                        )
                    }
                    
                    onDispose {
                        exportMessage = null
                    }
                }
            }
        }
    }
}

@Composable
fun InventoryReportContent(
    inventoryReport: com.aseel.pos.ui.InventoryReport,
    isRefreshing: Boolean
) {
    // Adaptive layout for tablet vs phone
    val isTablet = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp >= 600
    
    if (isTablet) {
        // Tablet layout with side-by-side sections
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Left column - Stock overview and alerts
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StockOverviewCard(
                    inventoryReport = inventoryReport,
                    modifier = Modifier.weight(1f)
                )
                
                StockAlertsCard(
                    lowStockProducts = inventoryReport.lowStockProducts,
                    modifier = Modifier.weight(1.5f)
                )
            }
            
            // Right column - Sales analytics
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SalesAnalyticsCard(
                    salesAnalytics = inventoryReport.salesAnalytics,
                    modifier = Modifier.weight(1f)
                )
                
                TopProductsCard(
                    mostSoldProducts = inventoryReport.mostSoldProducts,
                    leastSoldProducts = inventoryReport.leastSoldProducts,
                    modifier = Modifier.weight(1.5f)
                )
            }
        }
    } else {
        // Phone layout with vertical scrolling
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                StockOverviewCard(inventoryReport = inventoryReport)
            }
            
            item {
                StockAlertsCard(lowStockProducts = inventoryReport.lowStockProducts)
            }
            
            item {
                SalesAnalyticsCard(salesAnalytics = inventoryReport.salesAnalytics)
            }
            
            item {
                TopProductsCard(
                    mostSoldProducts = inventoryReport.mostSoldProducts,
                    leastSoldProducts = inventoryReport.leastSoldProducts
                )
            }
        }
    }
}

@Composable
fun StockOverviewCard(
    inventoryReport: com.aseel.pos.ui.InventoryReport,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Stock overview with total value and product counts"
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "نظرة عامة على المخزون",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Key metrics
            val keyMetrics = listOf(
                "إجمالي قيمة المخزون" to CurrencyFormatter.formatBase(inventoryReport.totalStockValue),
                "عدد المنتجات" to "${inventoryReport.stockReport.totalProducts}",
                "منتجات قليلة المخزون" to "${inventoryReport.stockReport.lowStockProducts}",
                "منتجات نفدت" to "${inventoryReport.stockReport.outOfStockProducts}"
            )
            
            keyMetrics.forEach { (label, value) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = if (label.contains("قليلة") || label.contains("نفدت")) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun StockAlertsCard(
    lowStockProducts: List<ProductWithStock>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Stock alerts for low inventory items"
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (lowStockProducts.isNotEmpty()) {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (lowStockProducts.isNotEmpty()) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "تنبيهات المخزون (أقل من 10 قطع)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (lowStockProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "لا توجد تنبيهات - جميع المنتجات في مستوى جيد",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // Show top 5 low stock products
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(lowStockProducts.take(10)) { product ->
                        LowStockProductCard(product = product)
                    }
                }
            }
        }
    }
}

@Composable
fun LowStockProductCard(
    product: ProductWithStock
) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .semantics {
                contentDescription = "Low stock product: ${product.product.nameAr} with ${product.currentStock} units"
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = product.product.nameAr,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 2
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "الكمية: ${product.currentStock}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
            
            Text(
                text = "القيمة: ${CurrencyFormatter.formatBase(product.stockValue)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SalesAnalyticsCard(
    salesAnalytics: List<ProductSalesAnalytics>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Sales analytics with product performance data"
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "تحليل المبيعات (آخر 30 يوم)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (salesAnalytics.isEmpty()) {
                Text(
                    text = "لا توجد بيانات مبيعات في الفترة المحددة",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                // Summary statistics
                val totalQuantitySold = salesAnalytics.sumOf { it.totalQuantitySold }
                val totalRevenue = salesAnalytics.sumOf { it.totalRevenue }
                val averageOrderSize = salesAnalytics.map { it.averageOrderSize }.average()
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatRow("إجمالي الكمية المباعة", "$totalQuantitySold")
                    StatRow("إجمالي الإيرادات", CurrencyFormatter.formatBase(totalRevenue))
                    StatRow("متوسط حجم الطلب", "%.2f".format(averageOrderSize))
                }
            }
        }
    }
}

@Composable
fun StatRow(
    label: String,
    value: String,
    isHighlight: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Normal,
            color = if (isHighlight) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

@Composable
fun TopProductsCard(
    mostSoldProducts: List<ProductSalesAnalytics>,
    leastSoldProducts: List<ProductSalesAnalytics>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Top selling and least selling products"
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "منتجات مميزة",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Most sold products
            Text(
                text = "الأكثر مبيعاً",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            mostSoldProducts.forEachIndexed { index, product ->
                ProductRankingRow(
                    rank = index + 1,
                    productName = product.productName,
                    quantity = product.totalQuantitySold,
                    revenue = product.totalRevenue,
                    isPositive = true
                )
                
                if (index < mostSoldProducts.size - 1) {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Divider()
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Least sold products
            Text(
                text = "الأقل مبيعاً",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            leastSoldProducts.forEachIndexed { index, product ->
                ProductRankingRow(
                    rank = index + 1,
                    productName = product.productName,
                    quantity = product.totalQuantitySold,
                    revenue = product.totalRevenue,
                    isPositive = false
                )
                
                if (index < leastSoldProducts.size - 1) {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
fun ProductRankingRow(
    rank: Int,
    productName: String,
    quantity: Int,
    revenue: Double,
    isPositive: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isPositive) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                } else {
                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                }
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isPositive) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.secondary
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = rank.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    text = productName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                Text(
                    text = "الكمية: $quantity",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = CurrencyFormatter.formatBase(revenue),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (isPositive) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.secondary
                }
            )
            
            Icon(
                imageVector = if (isPositive) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                contentDescription = null,
                tint = if (isPositive) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.secondary
                },
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
