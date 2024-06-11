package com.example.farmsimulator.stores

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "settings")

open class SettingsRepository(context: Context) {
    private val dataStore = context.dataStore

    companion object {
        val LOW_DATA_MODE = booleanPreferencesKey("low_data_mode")
        val ECO_MODE = booleanPreferencesKey("eco_mode")
    }

    open val lowDataModeFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[LOW_DATA_MODE] ?: false
    }

    open val ecoModeFlow: Flow<Boolean> = dataStore.data.map { preferences -> // Add this block
        preferences[ECO_MODE] ?: false
    }

    open suspend fun setLowDataMode(lowDataMode: Boolean) {
        dataStore.edit { preferences ->
            preferences[LOW_DATA_MODE] = lowDataMode
        }
    }

    open suspend fun setEcoMode(ecoMode: Boolean) { // Add this function
        dataStore.edit { preferences ->
            preferences[ECO_MODE] = ecoMode
        }
    }
}
