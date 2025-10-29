package com.aseel.pos.data

import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao
) {
    fun getAllTransactions(): Flow<List<Transaction>> = transactionDao.getAllTransactions()
    
    suspend fun getTransactionById(id: Long): Transaction? = transactionDao.getTransactionById(id)
    
    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<Transaction>> =
        transactionDao.getTransactionsByDateRange(startDate, endDate)
    
    suspend fun insertTransaction(transaction: Transaction): Long = 
        transactionDao.insertTransaction(transaction)
    
    suspend fun getTodayTotal(): Double? {
        val today = Instant.now().truncatedTo(ChronoUnit.DAYS)
        val tomorrow = today.plus(1, ChronoUnit.DAYS)
        return transactionDao.getTotalSales(today.toEpochMilli(), tomorrow.toEpochMilli())
    }
    
    suspend fun getTodayCount(): Int {
        val today = Instant.now().truncatedTo(ChronoUnit.DAYS)
        val tomorrow = today.plus(1, ChronoUnit.DAYS)
        return transactionDao.getTransactionCount(today.toEpochMilli(), tomorrow.toEpochMilli())
    }
}
