package com.aseel.pos.di

import android.content.Context
import androidx.datastore.core.DataStore
import com.aseel.pos.data.PosSettings
import com.aseel.pos.data.SettingsRepository
import com.aseel.pos.data.settingsDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {
    
    @Provides
    @Singleton
    fun provideSettingsDataStore(@ApplicationContext context: Context): DataStore<PosSettings> {
        return context.settingsDataStore
    }
    
    @Provides
    @Singleton
    fun provideSettingsRepository(dataStore: DataStore<PosSettings>): SettingsRepository {
        return SettingsRepository(dataStore)
    }
}
