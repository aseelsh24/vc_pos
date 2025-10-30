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
