package com.aseel.pos

import android.app.Application
import com.aseel.pos.data.SeedDataManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class PosApplication : Application() {
    @Inject
    lateinit var seedDataManager: SeedDataManager
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    override fun onCreate() {
        super.onCreate()
        // Seed database on first run
        applicationScope.launch {
            seedDataManager.seedIfNeeded()
        }
    }
}
