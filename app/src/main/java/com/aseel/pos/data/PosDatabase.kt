package com.aseel.pos.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.aseel.pos.core.PaymentMethod

@Database(
    entities = [Product::class, Category::class, Transaction::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class PosDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
}

class Converters {
    @TypeConverter
    fun fromPaymentMethod(value: PaymentMethod): String = value.name
    
    @TypeConverter
    fun toPaymentMethod(value: String): PaymentMethod = PaymentMethod.valueOf(value)
}
