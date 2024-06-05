package com.example.farmsimulator.ui.farm

import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.farmsimulator.R
import com.google.android.gms.maps.model.LatLng
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import com.example.farmsimulator.ui.utils.InputField

@Composable
fun PlannerPage(latLng: LatLng, height: Double, width: Double, onBackNavigation: () -> Unit) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(scrollState)
            .testTag("plannerPage"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        EnterCropsForm()

        Button(onClick = onBackNavigation) {
            Text(text = stringResource(id = R.string.back))
        }
    }
}

sealed class CropTypes(@StringRes val name: Int) {
    data object None : CropTypes(R.string.none)
    data object Corn : CropTypes(R.string.corn)
    data object Soy : CropTypes(R.string.soy)
    data object Wheat : CropTypes(R.string.wheat)
    data object Rice : CropTypes(R.string.rice)

    companion object {
        val items = listOf(None, Corn, Soy, Wheat, Rice)
    }
}

data class CropInfo(val cropType: CropTypes, val x: Double, val y: Double)

@Composable
fun EnterCropsForm(modifier: Modifier = Modifier) {
    var addedCrops by remember {
        mutableStateOf(listOf<CropInfo>())
    }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = stringResource(id = R.string.enter_crops),
            style = MaterialTheme.typography.headlineMedium
        )
        for (crop in addedCrops) {
            CropField(cropInfo = crop) {
                addedCrops = addedCrops.map { cropInfo ->
                    if (cropInfo == crop) {
                        it
                    } else {
                        cropInfo
                    }
                }
            }
        }
        Button(onClick = {
            addedCrops = addedCrops + CropInfo(CropTypes.None, 0.0, 0.0)
        }) {
            Text(text = "Add crop")
        }
    }
}

@Composable
fun CropField(cropInfo: CropInfo, onCropInfoChange: (CropInfo) -> Unit) {
    var xError by remember { mutableStateOf("") }
    var yError by remember { mutableStateOf("") }
    var showDropdown by remember { mutableStateOf(false) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            InputField(
                value = cropInfo.x.toString(),
                onValueChange = {
                    val value = it.toDoubleOrNull()
                    onCropInfoChange(cropInfo.copy(x = value ?: 0.0))
                    xError = if (value == null) "Invalid number" else ""
                },
                onValueError = { xError = it },
                label = "X",
                error = xError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            InputField(
                value = cropInfo.y.toString(),
                onValueChange = {
                    val value = it.toDoubleOrNull()
                    onCropInfoChange(cropInfo.copy(y = value ?: 0.0))
                    yError = if (value == null) "Invalid number" else ""
                },
                label = "Y",
                error = yError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                onValueError = { yError = it }
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column {

            Button(onClick = { showDropdown = !showDropdown }) {
                Text(text = stringResource(id = cropInfo.cropType.name))
            }

            DropdownMenu(expanded = showDropdown, onDismissRequest = { showDropdown = false }) {
                for (cropType in CropTypes.items) {
                    if (cropType == CropTypes.None || cropType == cropInfo.cropType || cropType == null) continue
                    DropdownMenuItem(
                        text = { Text(text = stringResource(id = cropType.name)) },
                        onClick = {
                            onCropInfoChange(cropInfo.copy(cropType = cropType))
                            showDropdown = false
                        }
                    )
                }
            }
            }
        }
    }
}
