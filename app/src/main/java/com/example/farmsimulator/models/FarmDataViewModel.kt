package com.example.farmsimulator.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.farmsimulator.ui.farm.CropInfo
import com.google.android.gms.maps.model.LatLng
import com.wales.FarmElement

data class FarmData(
    val width: Int,
    val height: Int,
    val crops: List<CropInfo>,
    val latLong: LatLng
)

class FarmDataViewModel : ViewModel() {
    private val _currentFarmData = MutableLiveData<FarmData>()
    val currentFarmData: LiveData<FarmData> = _currentFarmData

    private val _savedFarms = MutableLiveData<List<FarmData>>()
    val savedFarms: LiveData<List<FarmData>> = _savedFarms

    private val _yield = MutableLiveData<List<List<List<FarmElement>>>>()
    val yield: LiveData<List<List<List<FarmElement>>>> = _yield

    fun updateFarmData(farmData: FarmData) {
        _currentFarmData.value = farmData
    }

    fun updateYield(yield: List<List<List<FarmElement>>>) {
        _yield.value = yield
    }

    fun saveFarmData(farmData: FarmData) {
        val farms = _savedFarms.value.orEmpty().toMutableList()
        if (!farms.contains(farmData)) {
            farms.add(farmData)
        }
        _savedFarms.value = farms
    }

    fun removeFarmData(farmData: FarmData) {
        val farms = _savedFarms.value.orEmpty().toMutableList()
        farms.remove(farmData)
        _savedFarms.value = farms
    }

    fun clearSavedFarms() {
        _savedFarms.value = emptyList()
    }
}