package com.aseel.pos.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aseel.pos.core.Currency
import com.aseel.pos.ui.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToProductManagement: () -> Unit = {},
    onNavigateToInventoryReports: () -> Unit = {},
    onNavigateToTransactions: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()
    var showRateDialog by remember { mutableStateOf(false) }
    var editingCurrency by remember { mutableStateOf<Currency?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("الإعدادات") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "رجوع")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "العملة",
                style = MaterialTheme.typography.headlineSmall
            )
            
            Currency.entries.forEach { currency ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = if (settings.selectedCurrency == currency.code) {
                        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    } else {
                        CardDefaults.cardColors()
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "${currency.code} (${currency.symbol})",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "السعر: ${settings.exchangeRates.getRate(currency)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row {
                            TextButton(
                                onClick = {
                                    editingCurrency = currency
                                    showRateDialog = true
                                }
                            ) {
                                Text("تعديل السعر")
                            }
                            if (settings.selectedCurrency != currency.code) {
                                Button(
                                    onClick = { viewModel.updateCurrency(currency) }
                                ) {
                                    Text("اختيار")
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "الميزات",
                style = MaterialTheme.typography.headlineSmall
            )
            
            // Barcode Scanner Toggle
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "ماسح الباركود",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "تفعيل مسح الباركود لإضافة المنتجات تلقائياً",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = settings.enableBarcodeScanner,
                            onCheckedChange = { viewModel.updateBarcodeScannerEnabled(it) },
                            modifier = Modifier.semantics {
                                contentDescription = if (settings.enableBarcodeScanner) {
                                    "إيقاف ماسح الباركود"
                                } else {
                                    "تفعيل ماسح الباركود"
                                }
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Product Management Navigation
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "إدارة المنتجات",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "إدارة المنتجات والمخزون",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onNavigateToProductManagement,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("فتح إدارة المنتجات")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Inventory Reports Navigation
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "تقارير المخزون",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "عرض تقارير مفصلة عن المخزون والمبيعات",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onNavigateToInventoryReports,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("عرض التقارير")
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "معلومات المتجر",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("الاسم: ${settings.storeName}")
                    Text("الإصدار: 0.1.0-alpha")
                }
            }
        }
    }
    
    if (showRateDialog && editingCurrency != null) {
        ExchangeRateDialog(
            currency = editingCurrency!!,
            currentRate = settings.exchangeRates.getRate(editingCurrency!!),
            onDismiss = {
                showRateDialog = false
                editingCurrency = null
            },
            onConfirm = { newRate ->
                viewModel.updateExchangeRate(editingCurrency!!.code, newRate)
                showRateDialog = false
                editingCurrency = null
            }
        )
    }
}

@Composable
fun ExchangeRateDialog(
    currency: Currency,
    currentRate: Float,
    onDismiss: () -> Unit,
    onConfirm: (Float) -> Unit
) {
    var rateText by remember { mutableStateOf(currentRate.toString()) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تعديل سعر ${currency.code}") },
        text = {
            Column {
                Text("1 ${currency.code} = X ريال يمني")
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = rateText,
                    onValueChange = { rateText = it },
                    label = { Text("السعر") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    rateText.toFloatOrNull()?.let { onConfirm(it) }
                },
                enabled = rateText.toFloatOrNull() != null && rateText.toFloat() > 0
            ) {
                Text("حفظ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
