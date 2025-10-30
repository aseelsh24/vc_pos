package com.aseel.pos.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aseel.pos.R
import com.aseel.pos.data.Product
import com.aseel.pos.ui.StockInfo
import com.aseel.pos.ui.StockViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductManagementScreen(
    onNavigateBack: () -> Unit,
    viewModel: StockViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    var selectedTabIndex by remember { mutableStateOf(0) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var confirmDialogContent by remember { mutableStateOf<ConfirmDialogContent?>(null) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf("") }
    var showErrorMessage by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // State for manual stock adjustment
    var stockAdjustmentValue by remember { mutableStateOf("") }
    var selectedProductForAdjustment by remember { mutableStateOf<Product?>(null) }

    val allStockInfo by viewModel.getAllStockInfo().collectAsState(initial = emptyList())
    val lowStockProducts by viewModel.getLowStockProducts().collectAsState(initial = emptyList())

    // Show success message temporarily
    LaunchedEffect(showSuccessMessage) {
        if (showSuccessMessage) {
            kotlinx.coroutines.delay(2000)
            showSuccessMessage = false
        }
    }

    // Show error message temporarily
    LaunchedEffect(showErrorMessage) {
        if (showErrorMessage) {
            kotlinx.coroutines.delay(3000)
            showErrorMessage = false
        }
    }

    fun showSuccess(msg: String) {
        successMessage = msg
        showSuccessMessage = true
    }

    fun showError(msg: String) {
        errorMessage = msg
        showErrorMessage = true
    }

    fun showConfirmDialog(
        product: Product,
        adjustment: Int,
        onConfirm: () -> Unit
    ) {
        val adjustmentText = if (adjustment > 0) "+$adjustment" else "$adjustment"
        confirmDialogContent = ConfirmDialogContent(
            product = product,
            adjustment = adjustment,
            title = "تأكيد تغيير المخزون",
            message = "هل تريد ${if (adjustment > 0) "إضافة" else "خصم"} $adjustment وحدة من ${product.nameAr}؟\nالمخزون الحالي: ${product.quantity_in_stock}\nالمخزون الجديد: ${product.quantity_in_stock + adjustment}",
            onConfirm = onConfirm
        )
        showConfirmDialog = true
    }

    fun handleStockAdjustment(product: Product, adjustment: Int) {
        if (product.quantity_in_stock + adjustment < 0) {
            showError("لا يمكن أن يكون المخزون أقل من صفر")
            return
        }
        
        showConfirmDialog(product, adjustment) {
            scope.launch {
                val result = if (adjustment > 0) {
                    viewModel.addStock(product.id.toInt(), adjustment)
                } else {
                    viewModel.deductStock(product.id.toInt(), -adjustment)
                }
                
                result.fold(
                    onSuccess = {
                        val action = if (adjustment > 0) "تم إضافة" else "تم خصم"
                        showSuccess("$action $adjustment وحدة من ${product.nameAr}")
                    },
                    onFailure = { exception ->
                        showError("خطأ في تحديث المخزون: ${exception.message}")
                    }
                )
            }
        }
    }

    fun handleManualStockAdjustment(product: Product) {
        val adjustment = stockAdjustmentValue.toIntOrNull()
        if (adjustment == null || adjustment == 0) {
            showError("يرجى إدخال رقم صحيح")
            return
        }
        
        // Calculate the actual adjustment from current stock
        val actualAdjustment = adjustment - product.quantity_in_stock
        handleStockAdjustment(product, actualAdjustment)
        
        // Clear the input
        stockAdjustmentValue = ""
        selectedProductForAdjustment = null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("إدارة المنتجات") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Success/Error Messages
            if (showSuccessMessage) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(successMessage, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (showErrorMessage) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF44336))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Error, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(errorMessage, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Tab Row
            TabRow(selectedTabIndex = selectedTabIndex) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("جميع المنتجات") }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("تنبيه المخزون المنخفض") }
                )
                Tab(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    text = { Text("إدارة المخزون") }
                )
            }

            when (selectedTabIndex) {
                0 -> AllProductsTab(
                    stockInfo = allStockInfo,
                    onIncrementStock = { product, amount ->
                        handleStockAdjustment(product, amount)
                    },
                    onDecrementStock = { product, amount ->
                        handleStockAdjustment(product, -amount)
                    },
                    onManualAdjust = { product ->
                        selectedProductForAdjustment = product
                    }
                )
                1 -> LowStockTab(
                    products = lowStockProducts,
                    onIncrementStock = { product, amount ->
                        handleStockAdjustment(product, amount)
                    },
                    onManualAdjust = { product ->
                        selectedProductForAdjustment = product
                    }
                )
                2 -> StockManagementTab(
                    stockInfo = allStockInfo,
                    onAdjustStock = { product, adjustment ->
                        handleStockAdjustment(product, adjustment)
                    }
                )
            }

            // Manual Stock Adjustment Dialog
            if (selectedProductForAdjustment != null) {
                ManualStockAdjustmentDialog(
                    product = selectedProductForAdjustment!!,
                    currentStock = selectedProductForAdjustment!!.quantity_in_stock,
                    stockAdjustmentValue = stockAdjustmentValue,
                    onValueChange = { stockAdjustmentValue = it },
                    onConfirm = { handleManualStockAdjustment(selectedProductForAdjustment!!) },
                    onDismiss = {
                        selectedProductForAdjustment = null
                        stockAdjustmentValue = ""
                    }
                )
            }

            // Confirmation Dialog
            if (showConfirmDialog && confirmDialogContent != null) {
                ConfirmationDialog(
                    content = confirmDialogContent!!,
                    onDismiss = {
                        showConfirmDialog = false
                        confirmDialogContent = null
                    }
                )
            }
        }
    }
}

@Composable
private fun AllProductsTab(
    stockInfo: List<StockInfo>,
    onIncrementStock: (Product, Int) -> Unit,
    onDecrementStock: (Product, Int) -> Unit,
    onManualAdjust: (Product) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(stockInfo) { info ->
            ProductStockCard(
                product = Product(
                    id = info.productId,
                    sku = info.sku,
                    nameAr = info.productName,
                    priceBase = info.stockValue / info.currentStock.coerceAtLeast(1),
                    quantity_in_stock = info.currentStock
                ),
                isLowStock = info.isLowStock,
                onIncrementStock = onIncrementStock,
                onDecrementStock = onDecrementStock,
                onManualAdjust = onManualAdjust
            )
        }
    }
}

@Composable
private fun LowStockTab(
    products: List<Product>,
    onIncrementStock: (Product, Int) -> Unit,
    onManualAdjust: (Product) -> Unit
) {
    if (products.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("جميع المنتجات لديها مخزون كافي", style = MaterialTheme.typography.headlineSmall)
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(products) { product ->
                LowStockCard(
                    product = product,
                    onIncrementStock = onIncrementStock,
                    onManualAdjust = onManualAdjust
                )
            }
        }
    }
}

@Composable
private fun StockManagementTab(
    stockInfo: List<StockInfo>,
    onAdjustStock: (Product, Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F6))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "إجمالي المنتجات",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${stockInfo.size} منتج",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val lowStockCount = stockInfo.count { it.isLowStock }
                    Text(
                        text = "منتجات بمخزون منخفض",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (lowStockCount > 0) Color(0xFFFF5722) else Color(0xFF4CAF50)
                    )
                    Text(
                        text = "$lowStockCount منتج",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (lowStockCount > 0) Color(0xFFFF5722) else Color(0xFF4CAF50)
                    )
                }
            }
        }
        items(stockInfo) { info ->
            StockManagementCard(
                product = Product(
                    id = info.productId,
                    sku = info.sku,
                    nameAr = info.productName,
                    priceBase = info.stockValue / info.currentStock.coerceAtLeast(1),
                    quantity_in_stock = info.currentStock
                ),
                stockInfo = info,
                onAdjustStock = onAdjustStock
            )
        }
    }
}

@Composable
private fun ProductStockCard(
    product: Product,
    isLowStock: Boolean,
    onIncrementStock: (Product, Int) -> Unit,
    onDecrementStock: (Product, Int) -> Unit,
    onManualAdjust: (Product) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = if (isLowStock) {
            CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.nameAr,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "الرمز: ${product.sku}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = "المخزون: ${product.quantity_in_stock}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isLowStock) Color(0xFFFF5722) else Color.Unspecified
                        )
                        if (isLowStock) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = "مخزون منخفض",
                                tint = Color(0xFFFF5722),
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stock adjustment buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onDecrementStock(product, 1) },
                    enabled = product.quantity_in_stock > 0,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = null)
                    Text("-1")
                }

                Button(
                    onClick = { onIncrementStock(product, 1) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("+1")
                }

                OutlinedButton(
                    onClick = { onManualAdjust(product) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Text("تحديد")
                }
            }

            if (isLowStock) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFCDD2))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFFF5722)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "تنبيه: المخزون منخفض",
                            color = Color(0xFFFF5722),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LowStockCard(
    product: Product,
    onIncrementStock: (Product, Int) -> Unit,
    onManualAdjust: (Product) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.nameAr,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "الرمز: ${product.sku}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = "المخزون: ${product.quantity_in_stock}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF5722)
                        )
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "مخزون منخفض",
                            tint = Color(0xFFFF5722),
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onIncrementStock(product, 5) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("+5")
                }

                Button(
                    onClick = { onIncrementStock(product, 10) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("+10")
                }

                OutlinedButton(
                    onClick = { onManualAdjust(product) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Text("تحديد")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFCDD2))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFFF5722)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "يجب إعادة تموين هذا المنتج",
                        color = Color(0xFFFF5722),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun StockManagementCard(
    product: Product,
    stockInfo: StockInfo,
    onAdjustStock: (Product, Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = if (stockInfo.isLowStock) {
            CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = product.nameAr,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "الرمز: ${product.sku}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("المخزون الحالي", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text(
                        text = "${product.quantity_in_stock} وحدة",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (stockInfo.isLowStock) Color(0xFFFF5722) else Color.Unspecified
                    )
                }
                Column {
                    Text("قيمة المخزون", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text(
                        text = "%.2f ر.ي".format(stockInfo.stockValue),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (stockInfo.isLowStock) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFCDD2))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFFF5722)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "مخزون منخفض",
                            color = Color(0xFFFF5722),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onAdjustStock(product, -10) },
                    enabled = product.quantity_in_stock >= 10,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("-10")
                }

                OutlinedButton(
                    onClick = { onAdjustStock(product, -5) },
                    enabled = product.quantity_in_stock >= 5,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("-5")
                }

                OutlinedButton(
                    onClick = { onAdjustStock(product, 5) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF4CAF50))
                ) {
                    Text("+5")
                }

                OutlinedButton(
                    onClick = { onAdjustStock(product, 10) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF4CAF50))
                ) {
                    Text("+10")
                }
            }
        }
    }
}

@Composable
private fun ManualStockAdjustmentDialog(
    product: Product,
    currentStock: Int,
    stockAdjustmentValue: String,
    onValueChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تعديل المخزون يدوياً") },
        text = {
            Column {
                Text("المنتج: ${product.nameAr}")
                Text("المخزون الحالي: $currentStock وحدة")
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = stockAdjustmentValue,
                    onValueChange = onValueChange,
                    label = { Text("المخزون الجديد") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                if (stockAdjustmentValue.isNotEmpty() && stockAdjustmentValue.toIntOrNull() != null) {
                    val newStock = stockAdjustmentValue.toInt()
                    val difference = newStock - currentStock
                    if (difference != 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "التغيير: ${if (difference > 0) "+" else ""}$difference وحدة",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (difference > 0) Color(0xFF4CAF50) else Color(0xFFFF5722)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = stockAdjustmentValue.isNotEmpty() && stockAdjustmentValue.toIntOrNull() != null && stockAdjustmentValue.toInt() >= 0
            ) {
                Text("تأكيد")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
private fun ConfirmationDialog(
    content: ConfirmDialogContent,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(content.title) },
        text = { Text(content.message) },
        confirmButton = {
            Button(onClick = {
                content.onConfirm()
                onDismiss()
            }) {
                Text("تأكيد")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

data class ConfirmDialogContent(
    val product: Product,
    val adjustment: Int,
    val title: String,
    val message: String,
    val onConfirm: () -> Unit
)