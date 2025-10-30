package com.aseel.pos.ui.screens

import android.content.Context
import android.print.PrintManager
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.window.layout.FoldingFeature
import com.aseel.pos.core.Currency
import com.aseel.pos.core.ExchangeRates
import com.aseel.pos.data.Transaction
import com.aseel.pos.ui.PosViewModel
import com.aseel.pos.util.ReceiptFormatter
import com.aseel.pos.util.ReceiptPrintDocumentAdapter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Receipt preview screen that displays a formatted receipt
 * and provides options to print to PDF or thermal printer
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptPreviewScreen(
    transaction: Transaction,
    storeName: String,
    cashierName: String? = null,
    onNavigateBack: () -> Unit,
    viewModel: PosViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()
    val context = LocalContext.current
    
    var isGeneratingPdf by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("معاينة الإيصال") },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.semantics {
                            contentDescription = "العودة للخلف"
                        }
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "العودة للخلف")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Receipt content
            ReceiptPreviewContent(
                transaction = transaction,
                storeName = storeName,
                cashierName = cashierName,
                exchangeRates = settings.exchangeRates,
                currency = Currency.fromCode(settings.selectedCurrency),
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = 16.dp)
            )
            
            // Print buttons
            PrintButtons(
                onPrintToPdf = {
                    printToPdf(
                        context = context,
                        transaction = transaction,
                        storeName = storeName,
                        cashierName = cashierName,
                        exchangeRates = settings.exchangeRates,
                        currency = Currency.fromCode(settings.selectedCurrency)
                    ) { isGeneratingPdf = it }
                },
                onPrintToThermal = {
                    printToThermal(context)
                },
                isGeneratingPdf = isGeneratingPdf,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ReceiptPreviewContent(
    transaction: Transaction,
    storeName: String,
    cashierName: String?,
    exchangeRates: ExchangeRates,
    currency: Currency,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                spotColor = MaterialTheme.colorScheme.shadow,
                ambientColor = MaterialTheme.colorScheme.surfaceVariant
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Format and display receipt
            val receiptText = ReceiptFormatter.formatReceipt(
                transaction = transaction,
                storeName = storeName,
                cashierName = cashierName,
                exchangeRates = exchangeRates,
                currency = currency
            )
            
            // Split into lines and display with appropriate styling
            val lines = receiptText.split("\n")
            
            lines.forEach { line ->
                ReceiptLine(
                    text = line,
                    isSeparator = line.startsWith("=") || line.startsWith("-"),
                    isHeader = line.contains(storeName),
                    isTotal = line.startsWith("TOTAL:")
                )
                
                // Add extra spacing after certain elements
                when {
                    line.isBlank() -> Spacer(modifier = Modifier.height(4.dp))
                    line.startsWith("=") -> Spacer(modifier = Modifier.height(8.dp))
                    line.startsWith("TOTAL:") -> Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun ReceiptLine(
    text: String,
    isSeparator: Boolean = false,
    isHeader: Boolean = false,
    isTotal: Boolean = false
) {
    val textStyle = when {
        isHeader -> MaterialTheme.typography.headlineSmall.copy(
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
        )
        isTotal -> MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        isSeparator -> MaterialTheme.typography.bodyMedium.copy(
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        text.startsWith("Date:") || text.startsWith("Time:") || text.startsWith("Cashier:") || 
        text.startsWith("Transaction ID:") || text.startsWith("Payment:") -> 
            MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        text.startsWith("  x") -> MaterialTheme.typography.bodySmall.copy(
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        text.startsWith("Subtotal:") || text.startsWith("Discount:") || 
        text.startsWith("Tax:") -> MaterialTheme.typography.bodyMedium.copy(
            fontFamily = FontFamily.Monospace
        )
        text.startsWith("ITEMS") -> MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold
        )
        else -> MaterialTheme.typography.bodyMedium.copy(
            fontFamily = FontFamily.Monospace
        )
    }
    
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isHeader || isSeparator || isTotal) Alignment.Center else Alignment.Start
    ) {
        Text(
            text = text,
            style = textStyle,
            modifier = Modifier.padding(vertical = if (isSeparator) 4.dp else 0.dp)
        )
    }
}

@Composable
private fun PrintButtons(
    onPrintToPdf: (Boolean) -> Unit,
    onPrintToThermal: () -> Unit,
    isGeneratingPdf: Boolean,
    modifier: Modifier = Modifier
) {
    // Use responsive layout based on screen width
    val isTablet = isTabletLayout()
    
    if (isTablet) {
        // Tablet: Side-by-side buttons
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PrintButton(
                text = "طباعة PDF",
                icon = Icons.Outlined.Print,
                onClick = { onPrintToPdf(true) },
                isLoading = isGeneratingPdf,
                modifier = Modifier.weight(1f)
            )
            
            PrintButton(
                text = "طباعة حرارية",
                icon = Icons.Default.Print,
                onClick = onPrintToThermal,
                modifier = Modifier.weight(1f),
                enabled = !isGeneratingPdf
            )
        }
    } else {
        // Phone: Stacked buttons
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PrintButton(
                text = "طباعة PDF",
                icon = Icons.Outlined.Print,
                onClick = { onPrintToPdf(true) },
                isLoading = isGeneratingPdf,
                modifier = Modifier.fillMaxWidth()
            )
            
            PrintButton(
                text = "طباعة حرارية",
                icon = Icons.Default.Print,
                onClick = onPrintToThermal,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isGeneratingPdf
            )
        }
    }
}

@Composable
private fun PrintButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier.semantics {
            contentDescription = if (isLoading) "جاري إنشاء $text" else text
        },
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.width(8.dp))
        } else {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        Text(
            text = if (isLoading) "جاري التحضير..." else text,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

/**
 * Print receipt to PDF using the system's print service
 */
private fun printToPdf(
    context: Context,
    transaction: Transaction,
    storeName: String,
    cashierName: String?,
    exchangeRates: ExchangeRates,
    currency: Currency,
    onStatusChange: (Boolean) -> Unit
) {
    try {
        onStatusChange(true)
        
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        
        val printAdapter = ReceiptPrintDocumentAdapter(
            context = context,
            transaction = transaction,
            storeName = storeName,
            cashierName = cashierName,
            exchangeRates = exchangeRates,
            currency = currency
        )
        
        val jobName = "إيصال رقم ${transaction.id} - ${getStoreNameForJob(storeName)}"
        
        printManager.print(jobName, printAdapter, null)
        
        Toast.makeText(
            context,
            "تم بدء عملية الطباعة إلى PDF",
            Toast.LENGTH_SHORT
        ).show()
        
    } catch (e: Exception) {
        Toast.makeText(
            context,
            "فشل في بدء طباعة PDF: ${e.message}",
            Toast.LENGTH_LONG
        ).show()
    } finally {
        onStatusChange(false)
    }
}

/**
 * Attempt to print to thermal printer
 */
private fun printToThermal(context: Context) {
    // TODO: Implement thermal printer connection and printing
    // For now, show a "Not Connected" message
    Toast.makeText(
        context,
        "الطابعة الحرارية غير متصلة",
        Toast.LENGTH_LONG
    ).show()
}

/**
 * Truncate store name for print job name
 */
private fun getStoreNameForJob(storeName: String): String {
    return if (storeName.length > 30) {
        storeName.take(30) + "..."
    } else {
        storeName
    }
}

/**
 * Check if the current layout is tablet-sized
 */
@Composable
private fun isTabletLayout(): Boolean {
    // Use WindowSizeClass if available, fallback to screen width check
    val windowSize = androidx.compose.material3.windowsizeclass.WindowSizeClass.compute(
        density = LocalContext.current.resources.displayMetrics.density,
        windowBounds = LocalContext.current.resources.displayMetrics
    )
    
    return windowSize.widthSizeClass >= androidx.compose.material3.windowsizeclass.WindowSizeClass.COMPACT
}