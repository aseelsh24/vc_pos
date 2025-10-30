package com.aseel.pos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.RemoveCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aseel.pos.core.Currency
import com.aseel.pos.core.CurrencyFormatter
import com.aseel.pos.core.ExchangeRates
import com.aseel.pos.data.StockImpactStatus
import com.aseel.pos.data.Transaction
import com.aseel.pos.ui.SortOption
import com.aseel.pos.ui.StockImpactSummary
import com.aseel.pos.ui.TransactionsViewModel
import com.aseel.pos.util.ReceiptFormatter
import com.aseel.pos.util.ReceiptPrintDocumentAdapter
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToReceipt: (Transaction) -> Unit,
    viewModel: TransactionsViewModel = hiltViewModel()
) {
    val transactions by viewModel.filteredTransactions.collectAsState()
    val stockSummary by viewModel.stockImpactSummary.collectAsState()
    var selectedTransaction by remember { mutableStateOf<Transaction?>(null) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("المعاملات") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, "رجوع")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showSortDialog = true }) {
                            Icon(Icons.Default.Sort, "ترتيب")
                        }
                        IconButton(onClick = { showFilterDialog = true }) {
                            Icon(Icons.Default.FilterList, "تصفية")
                        }
                    }
                )
                
                // Stock Impact Summary Bar
                StockImpactSummaryBar(summary = stockSummary)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Bar
            SearchBar(
                onSearch = { viewModel.updateSearchQuery(it) },
                onClearFilters = { viewModel.clearFilters() }
            )
            
            // Transactions List
            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "لا توجد معاملات",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(transactions) { transaction ->
                        TransactionCard(
                            transaction = transaction,
                            onClick = { selectedTransaction = transaction },
                            onViewReceipt = {
                                selectedTransaction = null
                                onNavigateToReceipt(transaction)
                            }
                        )
                    }
                }
            }
        }
    }
    
    // Filter Dialog
    if (showFilterDialog) {
        FilterDialog(
            onDismiss = { showFilterDialog = false },
            onFilterSelected = { filter ->
                viewModel.updateStockImpactFilter(filter)
                showFilterDialog = false
            }
        )
    }
    
    // Sort Dialog
    if (showSortDialog) {
        SortDialog(
            onDismiss = { showSortDialog = false },
            onSortSelected = { sort ->
                viewModel.updateSortOption(sort)
                showSortDialog = false
            }
        )
    }
    
    selectedTransaction?.let { transaction ->
        TransactionDetailDialog(
            transaction = transaction,
            onDismiss = { selectedTransaction = null },
            onViewReceipt = { 
                selectedTransaction = null
                onNavigateToReceipt(transaction)
            }
        )
    }
}

@Composable
fun SearchBar(
    onSearch: (String) -> Unit,
    onClearFilters: () -> Unit
) {
    var searchText by remember { mutableStateOf("") }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = searchText,
            onValueChange = {
                searchText = it
                onSearch(it)
            },
            modifier = Modifier
                .weight(1f)
                .semantics {
                    contentDescription = "Search transactions by ID, cashier name, payment method, or product name"
                },
            placeholder = { Text("البحث في المعاملات...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null)
            },
            singleLine = true,
            shape = MaterialTheme.shapes.medium
        )
        
        if (searchText.isNotEmpty()) {
            Spacer(modifier = Modifier.width(8.dp))
            TextButton(
                onClick = {
                    searchText = ""
                    onSearch("")
                    onClearFilters()
                }
            ) {
                Text("مسح")
            }
        }
    }
}

@Composable
fun StockImpactSummaryBar(summary: StockImpactSummary) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Total Transactions
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = summary.total.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "الإجمالي",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Divider(modifier = Modifier.height(30.dp))
            
            // Successful Stock Deductions
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = summary.successful.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                    Text(
                        text = "تم الخصم",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            Divider(modifier = Modifier.height(30.dp))
            
            // Failed Stock Deductions
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Error,
                    contentDescription = null,
                    tint = Color(0xFFF44336),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = summary.failed.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF44336)
                    )
                    Text(
                        text = "فشل الخصم",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            Divider(modifier = Modifier.height(30.dp))
            
            // Not Applicable
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.RemoveCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = summary.notApplicable.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "غير قابل",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
fun FilterDialog(
    onDismiss: () -> Unit,
    onFilterSelected: (StockImpactStatus?) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تصفية حسب حالة المخزون") },
        text = {
            Column {
                TextButton(
                    onClick = { onFilterSelected(null) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("جميع المعاملات")
                }
                
                FilterOption(
                    icon = Icons.Outlined.CheckCircle,
                    text = "تم خصم المخزون",
                    color = Color(0xFF4CAF50),
                    count = null
                ) { onFilterSelected(StockImpactStatus.STOCK_DEDUCTED) }
                
                FilterOption(
                    icon = Icons.Outlined.Error,
                    text = "فشل في خصم المخزون",
                    color = Color(0xFFF44336),
                    count = null
                ) { onFilterSelected(StockImpactStatus.STOCK_FAILED) }
                
                FilterOption(
                    icon = Icons.Outlined.RemoveCircle,
                    text = "غير قابل للتطبيق",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    count = null
                ) { onFilterSelected(StockImpactStatus.NOT_APPLICABLE) }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق")
            }
        }
    )
}

@Composable
fun FilterOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    color: Color,
    count: Int?,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium
            )
            count?.let {
                Text(
                    text = it.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SortDialog(
    onDismiss: () -> Unit,
    onSortSelected: (SortOption) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ترتيب المعاملات") },
        text = {
            Column {
                SortOptionItem(
                    text = "التاريخ (الأحدث أولاً)",
                    isSelected = false
                ) { onSortSelected(SortOption.DATE_DESC) }
                
                SortOptionItem(
                    text = "التاريخ (الأقدم أولاً)",
                    isSelected = false
                ) { onSortSelected(SortOption.DATE_ASC) }
                
                SortOptionItem(
                    text = "المبلغ (الأعلى أولاً)",
                    isSelected = false
                ) { onSortSelected(SortOption.TOTAL_DESC) }
                
                SortOptionItem(
                    text = "المبلغ (الأقل أولاً)",
                    isSelected = false
                ) { onSortSelected(SortOption.TOTAL_ASC) }
                
                SortOptionItem(
                    text = "حالة المخزون",
                    isSelected = false
                ) { onSortSelected(SortOption.STOCK_STATUS) }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق")
            }
        }
    )
}

@Composable
fun SortOptionItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionCard(
    transaction: Transaction,
    onClick: () -> Unit,
    onViewReceipt: () -> Unit
) {
    val stockImpact = transaction.getStockImpact()
    
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "معاملة #${transaction.id}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = formatDate(transaction.date),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "الدفع: ${transaction.paymentMethod.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = CurrencyFormatter.formatBase(transaction.totalBase),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = transaction.currencyCode,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Stock Impact Row
            StockImpactRow(stockImpact = stockImpact)
            
            // View Receipt Button
            Divider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onViewReceipt,
                    modifier = Modifier.semantics {
                        contentDescription = "View receipt for transaction ${transaction.id}"
                    }
                ) {
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(
                            id = android.R.drawable.ic_menu_save
                        ),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("عرض الإيصال")
                }
            }
        }
    }
}

@Composable
fun StockImpactRow(stockImpact: com.aseel.pos.data.StockImpact) {
    val (icon, color, text) = when (stockImpact.status) {
        StockImpactStatus.STOCK_DEDUCTED -> Triple(
            Icons.Outlined.CheckCircle,
            Color(0xFF4CAF50),
            "تم خصم المخزون"
        )
        StockImpactStatus.STOCK_FAILED -> Triple(
            Icons.Outlined.Error,
            Color(0xFFF44336),
            "فشل في خصم المخزون"
        )
        StockImpactStatus.NOT_APPLICABLE -> Triple(
            Icons.Outlined.RemoveCircle,
            MaterialTheme.colorScheme.onSurfaceVariant,
            "غير قابل للتطبيق"
        )
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = color,
            fontWeight = FontWeight.Medium
        )
        
        // Show detailed information for non-N/A status
        if (stockImpact.status != StockImpactStatus.NOT_APPLICABLE) {
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "(${stockImpact.getTotalDeducted()} نجح، ${stockImpact.getTotalFailed()} فشل)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun TransactionDetailDialog(
    transaction: Transaction,
    onDismiss: () -> Unit,
    onViewReceipt: () -> Unit
) {
    val stockImpact = transaction.getStockImpact()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تفاصيل المعاملة #${transaction.id}") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Basic transaction info
                Text("التاريخ: ${formatDate(transaction.date)}")
                Text("الدفع: ${transaction.paymentMethod.name}")
                Text("العملة: ${transaction.currencyCode}")
                
                if (!transaction.cashierName.isNullOrBlank()) {
                    Text("الكاشير: ${transaction.cashierName}")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                
                // Line items
                transaction.getLineItems().forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${item.productName} x${item.qty}")
                        Text(CurrencyFormatter.formatBase(item.subtotalBase))
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                
                // Totals
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("الإجمالي:", style = MaterialTheme.typography.titleMedium)
                    Text(
                        CurrencyFormatter.formatBase(transaction.totalBase),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                
                // Stock Impact Summary
                Spacer(modifier = Modifier.height(16.dp))
                StockImpactSummaryCard(stockImpact = stockImpact)
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(
                    onClick = onViewReceipt,
                    modifier = Modifier
                        .weight(1f)
                        .semantics { 
                            contentDescription = "View receipt for transaction ${transaction.id}"
                        }
                ) {
                    Text("عرض الإيصال")
                }
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .semantics { 
                            contentDescription = "Close transaction details dialog"
                        }
                ) {
                    Text("إغلاق")
                }
            }
        }
    )
}

@Composable
fun StockImpactSummaryCard(stockImpact: com.aseel.pos.data.StockImpact) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (stockImpact.status) {
                StockImpactStatus.STOCK_DEDUCTED -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                StockImpactStatus.STOCK_FAILED -> Color(0xFFF44336).copy(alpha = 0.1f)
                StockImpactStatus.NOT_APPLICABLE -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                val (icon, color, title) = when (stockImpact.status) {
                    StockImpactStatus.STOCK_DEDUCTED -> Triple(
                        Icons.Outlined.CheckCircle,
                        Color(0xFF4CAF50),
                        "حالة خصم المخزون: نجح"
                    )
                    StockImpactStatus.STOCK_FAILED -> Triple(
                        Icons.Outlined.Error,
                        Color(0xFFF44336),
                        "حالة خصم المخزون: فشل"
                    )
                    StockImpactStatus.NOT_APPLICABLE -> Triple(
                        Icons.Outlined.RemoveCircle,
                        MaterialTheme.colorScheme.onSurfaceVariant,
                        "حالة خصم المخزون: غير قابل للتطبيق"
                    )
                }
                
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (stockImpact.status != StockImpactStatus.NOT_APPLICABLE) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("إجمالي العناصر:", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "${stockImpact.totalItemsAffected}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("تم خصمها بنجاح:", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF4CAF50))
                    Text(
                        "${stockImpact.getTotalDeducted()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("فشل في خصمها:", style = MaterialTheme.typography.bodyMedium, color = Color(0xFFF44336))
                    Text(
                        "${stockImpact.getTotalFailed()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFF44336),
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = stockImpact.getSuccessRate(),
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF4CAF50)
                )
                Text(
                    text = "معدل النجاح: ${"%.1f".format(stockImpact.getSuccessRate() * 100)}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Detailed breakdown
                if (stockImpact.itemsDeducted.isNotEmpty() || stockImpact.itemsFailed.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "تفاصيل العناصر:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    stockImpact.itemsDeducted.forEach { (productId, qty) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("المنتج #$productId", style = MaterialTheme.typography.bodySmall, color = Color(0xFF4CAF50))
                            Text("+${qty}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF4CAF50))
                        }
                    }
                    
                    stockImpact.itemsFailed.forEach { (productId, qty) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("المنتج #$productId", style = MaterialTheme.typography.bodySmall, color = Color(0xFFF44336))
                            Text("-${qty}", style = MaterialTheme.typography.bodySmall, color = Color(0xFFF44336))
                        }
                    }
                }
            } else {
                Text(
                    text = "هذه المعاملة تمت قبل تطبيق نظام إدارة المخزون",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

/**
 * Print helper functions
 */
private fun printToPdf(
    context: android.content.Context,
    transaction: Transaction,
    storeName: String,
    cashierName: String?,
    exchangeRates: ExchangeRates,
    currency: Currency
) {
    val printManager = context.getSystemService(android.content.Context.PRINT_SERVICE) as android.print.PrintManager
    
    val printAdapter = ReceiptPrintDocumentAdapter(
        context = context,
        transaction = transaction,
        storeName = storeName,
        cashierName = cashierName,
        exchangeRates = exchangeRates,
        currency = currency
    )
    
    val printJob = printManager.print(
        "Receipt_${transaction.id}",
        printAdapter,
        android.print.PrintAttributes.Builder().build()
    )
}

private fun printToThermal(
    context: android.content.Context,
    transaction: Transaction,
    storeName: String,
    cashierName: String?
) {
    // TODO: Implement thermal printing
    // For now, show a toast message
    android.widget.Toast.makeText(
        context,
        "Thermal printer not connected. Please connect a Bluetooth thermal printer.",
        android.widget.Toast.LENGTH_LONG
    ).show()
}

/**
 * ReceiptPreviewScreen - Shows receipt preview with printing options
 * 
 * @param transaction The transaction to display
 * @param storeName Name of the store
 * @param cashierName Name of the cashier (optional)
 * @param onNavigateBack Callback to navigate back
 * @param onPrintToPdf Callback to print to PDF
 * @param onPrintToThermal Callback to print to thermal printer
 */
@Composable
fun ReceiptPreviewScreen(
    transaction: Transaction,
    storeName: String,
    cashierName: String? = null,
    onNavigateBack: () -> Unit,
    onPrintToPdf: () -> Unit,
    onPrintToThermal: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val exchangeRates = remember { com.aseel.pos.core.ExchangeRates.getInstance() }
    val currency = remember { com.aseel.pos.core.Currency.SAR }
    
    val receiptText = remember(transaction, storeName, cashierName) {
        ReceiptFormatter.formatReceipt(
            transaction = transaction,
            storeName = storeName,
            cashierName = cashierName,
            exchangeRates = exchangeRates,
            currency = currency
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("معاينة الإيصال") },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.semantics {
                            contentDescription = "Navigate back to transactions"
                        }
                    ) {
                        Icon(Icons.Default.ArrowBack, "رجوع")
                    }
                }
            )
        },
        floatingActionButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                ExtendedFloatingActionButton(
                    onClick = {
                        printToThermal(
                            context = context,
                            transaction = transaction,
                            storeName = storeName,
                            cashierName = cashierName
                        )
                    },
                    icon = {
                        Icon(
                            painter = androidx.compose.ui.res.painterResource(
                                id = android.R.drawable.ic_menu_share
                            ),
                            contentDescription = "Print to thermal printer"
                        )
                    },
                    text = { Text("طباعة حرارية") },
                    modifier = Modifier.semantics {
                        contentDescription = "Print receipt to thermal printer"
                    }
                )
                
                ExtendedFloatingActionButton(
                    onClick = {
                        printToPdf(
                            context = context,
                            transaction = transaction,
                            storeName = storeName,
                            cashierName = cashierName,
                            exchangeRates = exchangeRates,
                            currency = currency
                        )
                    },
                    icon = {
                        Icon(
                            painter = androidx.compose.ui.res.painterResource(
                                id = android.R.drawable.ic_menu_save
                            ),
                            contentDescription = "Print to PDF"
                        )
                    },
                    text = { Text("طباعة PDF") },
                    modifier = Modifier.semantics {
                        contentDescription = "Print receipt to PDF"
                    }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .semantics {
                        contentDescription = "Receipt preview"
                    },
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = receiptText,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Transaction Summary
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "ملخص المعاملة",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.semantics {
                            contentDescription = "Transaction summary"
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "رقم المعاملة:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "#${transaction.id}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "التاريخ:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = formatDate(transaction.date),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "طريقة الدفع:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = transaction.paymentMethod.name,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    HorizontalDivider()
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "الإجمالي:",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = CurrencyFormatter.formatBase(transaction.totalBase),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
