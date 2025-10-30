package com.aseel.pos.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

object PosSettingsSerializer : Serializer<PosSettings> {
    override val defaultValue: PosSettings = PosSettings()
    
    override suspend fun readFrom(input: InputStream): PosSettings {
        return try {
            Json.decodeFromString(
                deserializer = PosSettings.serializer(),
                string = input.readBytes().decodeToString()
            )
        } catch (e: SerializationException) {
            e.printStackTrace()
            defaultValue
        }
    }
    
    override suspend fun writeTo(t: PosSettings, output: OutputStream) {
        withContext(Dispatchers.IO) {
            output.write(
                Json.encodeToString(
                    serializer = PosSettings.serializer(),
                    value = t
                ).encodeToByteArray()
            )
        }
    }
}

val Context.settingsDataStore: DataStore<PosSettings> by dataStore(
    fileName = "pos_settings.json",
    serializer = PosSettingsSerializer
)

class SettingsRepository(private val dataStore: DataStore<PosSettings>) {
    val settings: Flow<PosSettings> = dataStore.data
    
    suspend fun updateSettings(transform: (PosSettings) -> PosSettings) {
        dataStore.updateData(transform)
    }
    
    suspend fun updateCurrency(currency: String) {
        dataStore.updateData { it.copy(selectedCurrency = currency) }
    }
    
    suspend fun updateExchangeRate(currencyCode: String, rate: Float) {
        dataStore.updateData { 
            val newRates = it.exchangeRates.rates.toMutableMap().apply {
                put(currencyCode, rate)
            }
            it.copy(exchangeRates = it.exchangeRates.copy(rates = newRates))
        }
    }
    
    suspend fun updateBarcodeScannerEnabled(enabled: Boolean) {
        dataStore.updateData { it.copy(enableBarcodeScanner = enabled) }
    }
}
