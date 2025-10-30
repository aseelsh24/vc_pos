package com.aseel.pos.util

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.CancellationSignal
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.util.Log
import com.aseel.pos.core.Currency
import com.aseel.pos.core.ExchangeRates
import com.aseel.pos.data.Transaction
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

/**
 * PrintDocumentAdapter for generating and printing receipt PDFs
 * Handles multi-page receipts and proper text rendering
 */
class ReceiptPrintDocumentAdapter(
    private val context: Context,
    private val transaction: Transaction,
    private val storeName: String,
    private val cashierName: String? = null,
    private val exchangeRates: ExchangeRates,
    private val currency: Currency
) : PrintDocumentAdapter() {
    
    companion object {
        private const val TAG = "ReceiptPrintAdapter"
        private const val DEFAULT_MARGIN = 24 // pixels
        private const val LINE_HEIGHT = 32 // pixels
        private const val FONT_SIZE = 12 // points
        private const val HEADER_SIZE = 16 // points
        private const val MAX_LINE_WIDTH = 460 // pixels for A4 width
    }
    
    private var totalPages = 0
    private var paint: Paint = Paint()
    private var paintHeader: Paint = Paint()
    private var paintBold: Paint = Paint()
    
    init {
        setupPaint()
    }
    
    /**
     * Setup Paint objects for text rendering
     */
    private fun setupPaint() {
        paint.color = Color.BLACK
        paint.textSize = FONT_SIZE * context.resources.displayMetrics.scaledDensity
        paint.typeface = Typeface.MONOSPACE
        paint.isAntiAlias = true
        
        paintHeader.color = Color.BLACK
        paintHeader.textSize = HEADER_SIZE * context.resources.displayMetrics.scaledDensity
        paintHeader.typeface = Typeface.MONOSPACE
        paintHeader.isFakeBoldText = true
        paintHeader.isAntiAlias = true
        
        paintBold.color = Color.BLACK
        paintBold.textSize = FONT_SIZE * context.resources.displayMetrics.scaledDensity
        paintBold.typeface = Typeface.MONOSPACE
        paintBold.isFakeBoldText = true
        paintBold.isAntiAlias = true
    }
    
    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes?,
        cancellationSignal: CancellationSignal?,
        callback: LayoutResultCallback?,
        extras: Bundle?
    ) {
        try {
            Log.d(TAG, "onLayout called")
            
            // Check for cancellation
            if (cancellationSignal?.isCanceled == true) {
                callback?.onLayoutCancelled()
                return
            }
            
            // Generate receipt content
            val receiptText = ReceiptFormatter.formatReceipt(
                transaction = transaction,
                storeName = storeName,
                cashierName = cashierName,
                exchangeRates = exchangeRates,
                currency = currency
            )
            
            // Calculate total pages needed
            val lines = receiptText.split("\n")
            val pageHeight = newAttributes?.mediaSize?.imageableHeight?.toInt() 
                ?: PrintAttributes.MediaSize.UNKNOWN.getHeightDots(PrintAttributes.Resolution.DPI_300)
            val margins = DEFAULT_MARGIN * 2
            val availableHeight = pageHeight - margins
            
            val linesPerPage = availableHeight / LINE_HEIGHT
            totalPages = (lines.size + linesPerPage - 1) / linesPerPage
            
            Log.d(TAG, "Calculated total pages: $totalPages")
            
            // Check for cancellation again
            if (cancellationSignal?.isCanceled == true) {
                callback?.onLayoutCancelled()
                return
            }
            
            // Return layout info
            val builder = PrintDocumentInfo.Builder("receipt_${transaction.id}.pdf")
                .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .setPageCount(totalPages)
            
            callback?.onLayoutFinished(builder.build(), true)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error in onLayout", e)
            callback?.onLayoutFailed(e.message ?: "Unknown error")
        }
    }
    
    override fun onWrite(
        pageRanges: Array<out PageRange>,
        destination: ParcelFileDescriptor,
        cancellationSignal: CancellationSignal?,
        callback: WriteResultCallback?
    ) {
        try {
            Log.d(TAG, "onWrite called for pages: ${pageRanges.contentToString()}")
            
            // Check for cancellation
            if (cancellationSignal?.isCanceled == true) {
                callback?.onWriteCancelled()
                return
            }
            
            // Create PDF document
            val pdfDocument = PdfDocument()
            
            // Generate receipt content
            val receiptText = ReceiptFormatter.formatReceipt(
                transaction = transaction,
                storeName = storeName,
                cashierName = cashierName,
                exchangeRates = exchangeRates,
                currency = currency
            )
            
            val lines = receiptText.split("\n")
            
            // Process each requested page
            pageRanges.forEach { pageRange ->
                val startPage = pageRange.start
                val endPage = pageRange.end
                
                // Calculate page info
                val pageHeight = PrintAttributes.MediaSize.UNKNOWN.getHeightDots(PrintAttributes.Resolution.DPI_300)
                val pageWidth = PrintAttributes.MediaSize.UNKNOWN.getWidthDots(PrintAttributes.Resolution.DPI_300)
                
                val margins = DEFAULT_MARGIN * 2
                val availableHeight = pageHeight - margins
                val linesPerPage = availableHeight / LINE_HEIGHT
                
                // Create page
                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, startPage).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas
                
                // Calculate lines for this page
                val pageIndex = startPage
                val startLine = pageIndex * linesPerPage
                val endLine = minOf(startLine + linesPerPage, lines.size)
                
                // Draw header for non-first pages
                if (startPage > 0) {
                    canvas.drawText(
                        "--- Page ${startPage + 1} ---",
                        DEFAULT_MARGIN.toFloat(),
                        (DEFAULT_MARGIN + LINE_HEIGHT).toFloat(),
                        paintBold
                    )
                }
                
                // Draw content for this page
                var yPosition = (DEFAULT_MARGIN + LINE_HEIGHT * 2).toFloat()
                
                for (i in startLine until endLine) {
                    if (i >= lines.size) break
                    
                    // Check for cancellation
                    if (cancellationSignal?.isCanceled == true) {
                        pdfDocument.close()
                        callback?.onWriteCancelled()
                        return
                    }
                    
                    val line = lines[i]
                    val paint = when {
                        line.startsWith("=") -> paintBold
                        line.startsWith("-") -> paint
                        line.contains(storeName) -> paintHeader
                        else -> paint
                    }
                    
                    // Draw line with proper wrapping if needed
                    drawLine(canvas, line, DEFAULT_MARGIN.toFloat(), yPosition, paint)
                    yPosition += LINE_HEIGHT
                }
                
                pdfDocument.finishPage(page)
                Log.d(TAG, "Finished page $startPage")
            }
            
            // Write to file
            try {
                FileOutputStream(destination.fileDescriptor).use { outputStream ->
                    pdfDocument.writeTo(outputStream)
                }
            } finally {
                pdfDocument.close()
            }
            
            // Check for cancellation one more time
            if (cancellationSignal?.isCanceled == true) {
                callback?.onWriteCancelled()
                return
            }
            
            Log.d(TAG, "Successfully wrote PDF")
            callback?.onWriteFinished(pageRanges)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error in onWrite", e)
            callback?.onWriteFailed(e.message ?: "Unknown error")
        }
    }
    
    override fun onDestroy() {
        Log.d(TAG, "onDestroy called")
        // Cleanup resources if needed
        paint.reset()
        paintHeader.reset()
        paintBold.reset()
    }
    
    /**
     * Draw a line of text with word wrapping if it exceeds page width
     */
    private fun drawLine(canvas: Canvas, text: String, x: Float, y: Float, paint: Paint) {
        val maxWidth = (canvas.width - DEFAULT_MARGIN * 2).toFloat()
        
        if (paint.measureText(text) <= maxWidth) {
            // Text fits on one line
            canvas.drawText(text, x, y, paint)
        } else {
            // Text needs to be wrapped
            val words = text.split(" ")
            var currentLine = StringBuilder()
            var currentY = y
            
            for (word in words) {
                val testLine = if (currentLine.isEmpty()) word 
                    else "${currentLine} $word"
                
                if (paint.measureText(testLine) <= maxWidth) {
                    currentLine.append(if (currentLine.isEmpty()) "" else " ").append(word)
                } else {
                    // Draw current line if it's not empty
                    if (currentLine.isNotEmpty()) {
                        canvas.drawText(currentLine.toString(), x, currentY, paint)
                        currentY += LINE_HEIGHT
                    }
                    currentLine = StringBuilder(word)
                }
            }
            
            // Draw remaining text
            if (currentLine.isNotEmpty()) {
                canvas.drawText(currentLine.toString(), x, currentY, paint)
            }
        }
    }
    
    /**
     * Save receipt as PDF file for debugging or sharing
     */
    fun saveToFile(outputFile: File): Result<Unit> {
        return try {
            val pdfDocument = PdfDocument()
            
            val receiptText = ReceiptFormatter.formatReceipt(
                transaction = transaction,
                storeName = storeName,
                cashierName = cashierName,
                exchangeRates = exchangeRates,
                currency = currency
            )
            
            val pageInfo = PdfDocument.PageInfo.Builder(
                595, 842, 0 // A4 size at 72 DPI
            ).create()
            
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            
            val lines = receiptText.split("\n")
            var yPosition = DEFAULT_MARGIN.toFloat()
            
            lines.forEach { line ->
                val paint = when {
                    line.startsWith("=") -> paintBold
                    line.startsWith("-") -> paint
                    line.contains(storeName) -> paintHeader
                    else -> paint
                }
                
                drawLine(canvas, line, DEFAULT_MARGIN.toFloat(), yPosition, paint)
                yPosition += LINE_HEIGHT
                
                if (yPosition > canvas.height - DEFAULT_MARGIN) {
                    pdfDocument.finishPage(page)
                    // Start new page if needed
                    val newPageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
                    val newPage = pdfDocument.startPage(newPageInfo)
                    yPosition = DEFAULT_MARGIN.toFloat()
                }
            }
            
            pdfDocument.finishPage(page)
            
            FileOutputStream(outputFile).use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }
            pdfDocument.close()
            
            Log.d(TAG, "Receipt saved to: ${outputFile.absolutePath}")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error saving receipt to file", e)
            Result.failure(e)
        }
    }
}
