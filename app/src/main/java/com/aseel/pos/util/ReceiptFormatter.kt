package com.aseel.pos.util

import com.aseel.pos.core.Currency
import com.aseel.pos.core.CurrencyFormatter
import com.aseel.pos.core.ExchangeRates
import com.aseel.pos.data.LineItem
import com.aseel.pos.data.Transaction
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility class for formatting receipt text for thermal printers
 * Optimized for 80mm thermal printers (32-36 characters per line)
 */
object ReceiptFormatter {
    
    private const val RECEIPT_WIDTH = 32 // Characters per line for 80mm thermal printer
    private const val DATE_FORMAT = "yyyy-MM-dd HH:mm"
    private const val TIME_FORMAT = "HH:mm:ss"
    
    /**
     * Formats a complete receipt with all transaction details
     * 
     * @param transaction The transaction to format
     * @param storeName Name of the store
     * @param cashierName Name of the cashier (optional)
     * @param exchangeRates Current exchange rates
     * @param currency Target currency for display
     * @return Formatted receipt string ready for printing
     */
    fun formatReceipt(
        transaction: Transaction,
        storeName: String,
        cashierName: String? = null,
        exchangeRates: ExchangeRates,
        currency: Currency
    ): String {
        val formattedCurrency = Currency.fromCode(transaction.currencyCode)
        
        return buildString {
            // Header
            appendHeader(storeName, transaction.id)
            appendDateTime(transaction.date)
            if (!cashierName.isNullOrBlank()) {
                appendLine("Cashier: $cashierName")
            }
            appendLine("Transaction ID: ${transaction.id}")
            appendLine("Payment: ${transaction.paymentMethod}")
            appendSeparator()
            
            // Items
            appendLine("ITEMS")
            appendLine("=".repeat(RECEIPT_WIDTH))
            transaction.getLineItems().forEach { item ->
                appendItemLine(item, exchangeRates, currency)
            }
            
            appendSeparator()
            
            // Totals
            appendTotals(transaction, exchangeRates, currency, formattedCurrency)
            
            // Footer
            appendSeparator()
            appendFooter()
        }.trimEnd()
    }
    
    /**
     * Formats just the items section of a receipt
     */
    fun formatItems(
        items: List<LineItem>,
        exchangeRates: ExchangeRates,
        currency: Currency
    ): String {
        return buildString {
            appendLine("ITEMS")
            appendLine("=".repeat(RECEIPT_WIDTH))
            items.forEach { item ->
                appendItemLine(item, exchangeRates, currency)
            }
        }.trimEnd()
    }
    
    /**
     * Formats just the totals section of a receipt
     */
    fun formatTotals(
        transaction: Transaction,
        exchangeRates: ExchangeRates,
        currency: Currency
    ): String {
        val formattedCurrency = Currency.fromCode(transaction.currencyCode)
        
        return buildString {
            appendSeparator()
            appendTotals(transaction, exchangeRates, currency, formattedCurrency)
            appendSeparator()
            appendFooter()
        }.trimEnd()
    }
    
    private fun StringBuilder.appendHeader(storeName: String, transactionId: Long) {
        // Center the store name
        val paddedName = storeName.padCenter(RECEIPT_WIDTH)
        appendLine(paddedName)
        appendLine("=".repeat(RECEIPT_WIDTH))
        appendLine()
    }
    
    private fun StringBuilder.appendDateTime(timestamp: Long) {
        val date = Date(timestamp)
        val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
        val timeFormat = SimpleDateFormat(TIME_FORMAT, Locale.getDefault())
        
        appendLine("Date: ${dateFormat.format(date)}")
        appendLine("Time: ${timeFormat.format(date)}")
    }
    
    private fun StringBuilder.appendItemLine(
        item: LineItem,
        exchangeRates: ExchangeRates,
        currency: Currency
    ) {
        val convertedPrice = exchangeRates.convertFromBase(item.unitPriceBase, currency)
        val convertedSubtotal = exchangeRates.convertFromBase(item.subtotalBase, currency)
        
        // Format: Product Name (truncated if needed)
        val maxNameLength = RECEIPT_WIDTH - 10 // Reserve space for price
        val displayName = if (item.productName.length > maxNameLength) {
            item.productName.take(maxNameLength - 3) + "..."
        } else {
            item.productName
        }
        
        appendLine(displayName)
        appendLine("  x${item.qty} @ ${CurrencyFormatter.format(convertedPrice, currency, exchangeRates)}")
        appendLine("  ${CurrencyFormatter.format(convertedSubtotal, currency, exchangeRates)}")
        appendLine()
    }
    
    private fun StringBuilder.appendTotals(
        transaction: Transaction,
        exchangeRates: ExchangeRates,
        currency: Currency,
        originalCurrency: Currency
    ) {
        val subtotal = transaction.totalBase - transaction.tax + transaction.discount
        val convertedSubtotal = exchangeRates.convertFromBase(subtotal, currency)
        val convertedTax = exchangeRates.convertFromBase(transaction.tax, currency)
        val convertedDiscount = exchangeRates.convertFromBase(transaction.discount, currency)
        val convertedTotal = exchangeRates.convertFromBase(transaction.totalBase, currency)
        
        // Original currency total
        appendLine("Subtotal: ${CurrencyFormatter.format(convertedSubtotal, currency, exchangeRates)}")
        
        if (transaction.discount > 0) {
            appendLine("Discount: -${CurrencyFormatter.format(convertedDiscount, currency, exchangeRates)}")
        }
        
        if (transaction.tax > 0) {
            appendLine("Tax: ${CurrencyFormatter.format(convertedTax, currency, exchangeRates)}")
        }
        
        appendLine("=".repeat(RECEIPT_WIDTH))
        appendLine("TOTAL: ${CurrencyFormatter.format(convertedTotal, currency, exchangeRates)}")
        
        // Show original currency equivalent if different
        if (currency != originalCurrency) {
            val originalTotal = CurrencyFormatter.formatBase(transaction.totalBase)
            appendLine("($originalTotal)")
        }
    }
    
    private fun StringBuilder.appendFooter() {
        appendLine()
        appendLine("Thank you for your business!".padCenter(RECEIPT_WIDTH))
        appendLine("Visit us again soon!".padCenter(RECEIPT_WIDTH))
        appendLine()
        appendLine("Served with ❤️  by ASEEL POS".padCenter(RECEIPT_WIDTH))
    }
    
    private fun StringBuilder.appendSeparator() {
        appendLine("-".repeat(RECEIPT_WIDTH))
    }
    
    /**
     * Centers a string within the receipt width
     */
    private fun String.padCenter(width: Int): String {
        return if (this.length >= width) {
            this.take(width)
        } else {
            val padding = width - this.length
            val leftPad = padding / 2
            val rightPad = padding - leftPad
            " ".repeat(leftPad) + this + " ".repeat(rightPad)
        }
    }
    
    /**
     * Formats a simple receipt header with store info only
     */
    fun formatHeader(storeName: String, transactionId: Long): String {
        return buildString {
            appendHeader(storeName, transactionId)
            appendSeparator()
        }.trimEnd()
    }
    
    /**
     * Formats a receipt footer
     */
    fun formatFooter(): String {
        return buildString {
            appendSeparator()
            appendFooter()
        }.trimEnd()
    }
}
