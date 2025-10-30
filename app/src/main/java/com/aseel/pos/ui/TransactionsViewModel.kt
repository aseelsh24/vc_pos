package com.aseel.pos.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aseel.pos.data.StockImpact
import com.aseel.pos.data.StockImpactStatus
import com.aseel.pos.data.Transaction
import com.aseel.pos.data.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {
    
    private val _searchQuery = MutableStateFlow("")
    private val _stockImpactFilter = MutableStateFlow<StockImpactStatus?>(null)
    private val _sortBy = MutableStateFlow<SortOption>(SortOption.DATE_DESC)
    
    val transactions: StateFlow<List<Transaction>> = transactionRepository.getAllTransactions()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    // Filtered and sorted transactions
    val filteredTransactions: StateFlow<List<Transaction>> = combine(
        transactions,
        _searchQuery,
        _stockImpactFilter,
        _sortBy
    ) { transactions, searchQuery, stockFilter, sortOption ->
        var filtered = transactions
        
        // Filter by search query
        if (searchQuery.isNotBlank()) {
            val query = searchQuery.lowercase()
            filtered = filtered.filter { transaction ->
                transaction.id.toString().contains(query) ||
                transaction.cashierName?.lowercase()?.contains(query) == true ||
                transaction.paymentMethod.name.lowercase().contains(query) ||
                transaction.getLineItems().any { it.productName.lowercase().contains(query) }
            }
        }
        
        // Filter by stock impact status
        stockFilter?.let { filter ->
            filtered = filtered.filter { it.getStockImpact().status == filter }
        }
        
        // Sort transactions
        when (sortOption) {
            SortOption.DATE_ASC -> filtered.sortedBy { it.date }
            SortOption.DATE_DESC -> filtered.sortedByDescending { it.date }
            SortOption.TOTAL_ASC -> filtered.sortedBy { it.totalBase }
            SortOption.TOTAL_DESC -> filtered.sortedByDescending { it.totalBase }
            SortOption.STOCK_STATUS -> filtered.sortedBy { it.getStockImpact().status }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    // Stock impact summary
    val stockImpactSummary: StateFlow<StockImpactSummary> = combine(
        transactions
    ) { transactions ->
        val summary = StockImpactSummary()
        transactions.forEach { transaction ->
            when (transaction.getStockImpact().status) {
                StockImpactStatus.STOCK_DEDUCTED -> summary.successful++
                StockImpactStatus.STOCK_FAILED -> summary.failed++
                StockImpactStatus.NOT_APPLICABLE -> summary.notApplicable++
            }
            summary.total++
        }
        summary
    }.stateIn(viewModelScope, SharingStarted.Lazily, StockImpactSummary())
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun updateStockImpactFilter(filter: StockImpactStatus?) {
        _stockImpactFilter.value = filter
    }
    
    fun updateSortOption(sortOption: SortOption) {
        _sortBy.value = sortOption
    }
    
    fun getTransactionById(id: Long, onResult: (Transaction?) -> Unit) {
        viewModelScope.launch {
            val transaction = transactionRepository.getTransactionById(id)
            onResult(transaction)
        }
    }
    
    fun clearFilters() {
        _searchQuery.value = ""
        _stockImpactFilter.value = null
    }
}

enum class SortOption {
    DATE_ASC,
    DATE_DESC,
    TOTAL_ASC,
    TOTAL_DESC,
    STOCK_STATUS
}

data class StockImpactSummary(
    var total: Int = 0,
    var successful: Int = 0,
    var failed: Int = 0,
    var notApplicable: Int = 0
) {
    fun getSuccessRate(): Float = if (total > 0) successful.toFloat() / total.toFloat() else 0f
}
