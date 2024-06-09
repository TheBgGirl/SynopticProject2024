package com.example.farmsimulator

import android.content.Context
import com.example.farmsimulator.stores.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow


class FakeSettingsStore(context: Context) : SettingsRepository(context) {
    override val lowDataModeFlow = MutableStateFlow(false)

    override suspend fun setLowDataMode(lowDataMode: Boolean) {
        lowDataModeFlow.value = lowDataMode
    }
}