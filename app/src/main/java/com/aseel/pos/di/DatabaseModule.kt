package com.aseel.pos.di

import android.content.Context
import androidx.room.Room
import com.aseel.pos.data.CategoryDao
import com.aseel.pos.data.PosDatabase
import com.aseel.pos.data.ProductDao
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
            .fallbackToDestructiveMigration()
            .build()
    }
    
    @Provides
    fun provideProductDao(database: PosDatabase): ProductDao = database.productDao()
    
    @Provides
    fun provideCategoryDao(database: PosDatabase): CategoryDao = database.categoryDao()
    
    @Provides
    fun provideTransactionDao(database: PosDatabase): TransactionDao = database.transactionDao()
}
