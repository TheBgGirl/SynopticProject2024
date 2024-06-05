package com.example.farmsimulator.ui.farm

import android.provider.CalendarContract.Colors
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.farmsimulator.R
import com.example.farmsimulator.ui.utils.InputField
import com.example.farmsimulator.ui.utils.SelectTextField
import com.google.android.gms.maps.model.LatLng

@Composable
fun PlannerPage(latLng: LatLng, height: Int, width: Int, onBackNavigation: () -> Unit) {
    var addedCrops by remember {
        mutableStateOf(listOf<CropInfo>())
    }


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("plannerPage"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FarmGrid(height = height, width = width, crops = addedCrops)

        Text(
            text = stringResource(id = R.string.enter_crops),
            style = MaterialTheme.typography.headlineMedium
        )
        EnterCropsForm(addedCrops = addedCrops, onCropsChange = { addedCrops = it }, width = width, height = height)

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onBackNavigation, modifier = Modifier.testTag("backButton")) {
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

    private object Initializer {
        val items = listOf(None, Corn, Soy, Wheat, Rice)
    }

    companion object {
        val items: List<CropTypes> by lazy { Initializer.items }
    }
}

data class CropInfo(val cropType: CropTypes, val x: Int, val y: Int)

@Composable
fun EnterCropsForm(modifier: Modifier = Modifier, addedCrops: List<CropInfo>, onCropsChange: (List<CropInfo>) -> Unit = {}, width: Int, height: Int) {

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        for (crop in addedCrops) {
            CropField(cropInfo = crop, onCropInfoChange = {
                val index = addedCrops.indexOf(crop)
                val changedCrops = addedCrops.toMutableList().apply {
                    set(index, it)
                }
                onCropsChange(changedCrops)
            }, width = width, height = height, onDelete = {
                onCropsChange(addedCrops - it)
            })
        }
        Button(modifier = Modifier.testTag("addCropButton"), onClick = {
            val newCrop = CropInfo(CropTypes.None, 0, 0)
            onCropsChange(addedCrops + newCrop)
        }) {
            Text(text = stringResource(id = R.string.add_crop))
        }
    }
}

@Composable
fun FarmGrid(modifier: Modifier = Modifier, height: Int, width: Int, crops: List<CropInfo>) {
    LazyVerticalGrid(columns = GridCells.Fixed(width), modifier = modifier) {
        items(height * width) { index ->
            val crop = crops.firstOrNull { it.x == index % width && it.y == index / width }
            CropSquare(crop = crop)
        }
    }
}


@Composable
fun CropSquare(crop: CropInfo?, modifier: Modifier = Modifier) {
    val cropType = crop?.cropType
    val color = when (cropType) {
        CropTypes.Corn -> Color.Yellow
        CropTypes.Soy -> Color.Black
        CropTypes.Wheat -> Color.Blue
        CropTypes.Rice -> Color.Gray
        else -> Color.White
    }

    Box(modifier = modifier
        .height(50.dp)
        .width(50.dp)
        .background(color)
        .border(1.dp, Color.Black))
}


@Composable
fun CropField(cropInfo: CropInfo, onCropInfoChange: (CropInfo) -> Unit, onDelete: (CropInfo) -> Unit, width: Int, height: Int) {
    var xError by remember { mutableStateOf("") }
    var yError by remember { mutableStateOf("") }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("cropFieldInput"),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            InputField(
                value = cropInfo.x.toString(),
                onValueChange = {
                    val value = it.toIntOrNull()
                    if (value == null || value < 0 || value >= width) {
                        xError = "Invalid number"
                    } else {
                        onCropInfoChange(cropInfo.copy(x = value))
                        xError = ""
                    }
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
                    val value = it.toIntOrNull()
                    if (value == null || value < 0 || value >= height) {
                        yError = "Invalid number"
                    } else {
                        onCropInfoChange(cropInfo.copy(y = value))
                        yError = ""
                    }
                },
                label = "Y",
                error = yError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                onValueError = { yError = it }
            )

            Spacer(modifier = Modifier.width(8.dp))
            val cropType = stringResource(id = cropInfo.cropType.name)
            val cropOptions = CropTypes.items.filter { it != CropTypes.None }.map { stringResource(id = it.name) }

            Column (modifier = Modifier.width(150.dp)) {
                SelectTextField(
                    selectedValue = cropType,
                    onValueChange = { onCropInfoChange(
                        cropInfo.copy(cropType = CropTypes.items[cropOptions.indexOf(it)])
                    ) },
                    selectOptions = cropOptions,
                    label = stringResource(id = R.string.crop_type),
                )
            }

            IconButton(onClick = { onDelete(cropInfo) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
}
