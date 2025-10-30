package com.aseel.pos.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aseel.pos.core.Currency
import com.aseel.pos.data.PosSettings
import com.aseel.pos.data.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    
    val settings: StateFlow<PosSettings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, PosSettings())
    
    fun updateCurrency(currency: Currency) {
        viewModelScope.launch {
            settingsRepository.updateCurrency(currency.code)
        }
    }
    
    fun updateExchangeRate(currencyCode: String, rate: Float) {
        viewModelScope.launch {
            settingsRepository.updateExchangeRate(currencyCode, rate)
        }
    }
    
    fun updateBarcodeScannerEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateBarcodeScannerEnabled(enabled)
        }
    }
}
