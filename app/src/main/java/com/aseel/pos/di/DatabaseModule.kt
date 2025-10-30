package com.aseel.pos.di

import android.content.Context
import androidx.room.Room
import com.aseel.pos.data.CategoryDao
import com.aseel.pos.data.PosDatabase
import com.aseel.pos.data.ProductDao
import com.aseel.pos.data.StockRepository
import com.aseel.pos.data.StockRepositoryImpl
import com.aseel.pos.data.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PosDatabase {
        return Room.databaseBuilder(
            context,
            PosDatabase::class.java,
            "pos_database"
        )
            .addMigrations(com.aseel.pos.data.migrations.MIGRATION_1_2)
            .addMigrations(com.aseel.pos.data.migrations.MIGRATION_2_3)
            .fallbackToDestructiveMigration()
            .build()
    }
    
    @Provides
    fun provideProductDao(database: PosDatabase): ProductDao = database.productDao()
    
    @Provides
    fun provideCategoryDao(database: PosDatabase): CategoryDao = database.categoryDao()
    
    @Provides
    fun provideTransactionDao(database: PosDatabase): TransactionDao = database.transactionDao()
    
    @Provides
    @Singleton
    fun provideStockRepository(
        productDao: ProductDao,
        categoryDao: CategoryDao
    ): StockRepository {
        return StockRepositoryImpl(productDao, categoryDao)
    }
}
