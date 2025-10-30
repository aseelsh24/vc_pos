package com.aseel.pos.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.aseel.pos.core.PaymentMethod
import com.aseel.pos.data.migrations.MIGRATION_2_3

@Database(
    entities = [Product::class, Category::class, Transaction::class],
    version = 3,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class PosDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    
    companion object {
        val MIGRATIONS = arrayOf(
            MIGRATION_2_3
        )
    }
}

class Converters {
    @TypeConverter
    fun fromPaymentMethod(value: PaymentMethod): String = value.name
    
    @TypeConverter
    fun toPaymentMethod(value: String): PaymentMethod = PaymentMethod.valueOf(value)
}
