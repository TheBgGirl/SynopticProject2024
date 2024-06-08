package com.example.farmsimulator.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.farmsimulator.ui.farm.CropInfo
import com.google.android.gms.maps.model.LatLng

data class FarmData(
    val width: Int,
    val height: Int,
    val crops: List<CropInfo>,
    val latLong: LatLng
)

class FarmDataViewModel : ViewModel() {
    private val _crops = MutableLiveData<List<CropInfo>>()
    val crops: LiveData<List<CropInfo>> get() = _crops

    private val _height = MutableLiveData<Int>()
    val height: LiveData<Int> get() = _height

    private val _width = MutableLiveData<Int>()
    val width: LiveData<Int> get() = _width

    private val _latLong = MutableLiveData<LatLng>()
    val latLong: LiveData<LatLng> get() = _latLong

    fun setFarmData(width: Int, height: Int, crops: List<CropInfo>) {
        _width.value = width
        _height.value = height
        _crops.value = crops
    }

    fun setFarmSize(height: Int, width: Int) {
           _height.value = height
              _width.value = width
    }

    fun setLatLong(latLong: LatLng) {
        _latLong.value = latLong
    }

    fun addCrop(crop: CropInfo) {
        val currentCrops = _crops.value?.toMutableList() ?: mutableListOf()
        currentCrops.add(crop)
        _crops.value = currentCrops
    }

    fun removeCrop(crop: CropInfo) {
        val currentCrops = _crops.value?.toMutableList() ?: mutableListOf()
        currentCrops.remove(crop)
        _crops.value = currentCrops
    }

    fun clearCrops() {
        _crops.value = emptyList()
    }
}