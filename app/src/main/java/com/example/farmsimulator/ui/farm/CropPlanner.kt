package com.example.farmsimulator.ui.farm

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.farmsimulator.R
import com.example.farmsimulator.stores.SettingsRepository
import com.example.farmsimulator.ui.utils.InputField
import com.example.farmsimulator.ui.utils.SelectTextField
import com.google.android.gms.maps.model.LatLng
import kotlin.math.roundToInt


// Define crop types with string resources for their names
sealed class CropTypes(@StringRes val name: Int) {
    data object None : CropTypes(R.string.none)
    data object Pumpkins : CropTypes(R.string.pumpkin)
    data object LeafyGreens : CropTypes(R.string.leafy_greens)
    data object Rice : CropTypes(R.string.rice)

    private object Initializer {
        val items = listOf(None, Pumpkins, LeafyGreens, Rice)
    }

    companion object {
        val items: List<CropTypes> by lazy { Initializer.items }
    }
}

// Data class to hold crop information
data class CropInfo(val cropType: CropTypes, val x: Int, val y: Int)

// Main planner page composable function
@Composable
fun PlannerPage(latLng: LatLng, height: Int, width: Int, cropInfo: List<CropInfo>, toFarmView: (List<CropInfo>) -> Unit, settingsRepository: SettingsRepository) {
    var addedCrops by remember {
        mutableStateOf(cropInfo)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("plannerPage")
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Farm grid composable
        FarmGrid(height = height, width = width, crops = addedCrops, onCropAdd = {
            addedCrops = addedCrops + it
        })
        Spacer(modifier = Modifier.height(16.dp))
        if (addedCrops.isNotEmpty()) {
            Button(onClick = {
                toFarmView(addedCrops)
            }) {
                Text(text = stringResource(id = R.string.view_farm))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(id = R.string.enter_crops),
            style = MaterialTheme.typography.headlineMedium
        )

        // Enter crops form composable
        EnterCropsForm(
            addedCrops = addedCrops,
            onCropsChange = { addedCrops = it },
            width = width,
            height = height
        )
    }
}

// Farm grid composable function
@Composable
fun FarmGrid(
    modifier: Modifier = Modifier,
    height: Int,
    width: Int,
    crops: List<CropInfo>,
    onCropAdd: (CropInfo) -> Unit,
) {
    var selectedCells by remember {
        mutableStateOf(listOf<Pair<Int, Int>>())
    }
    var isDragging by remember {
        mutableStateOf(false)
    }

    var gridOffset by remember { mutableStateOf(Offset.Zero) }
    var showChooseCrop by remember { mutableStateOf(false) }

    // Choose crop dialog composable, shown when user drags to select cells
    if (showChooseCrop) {
        ChooseCropDialog(
            onCropAdd = onCropAdd,
            onDismiss = {
                showChooseCrop = false; selectedCells = listOf()
            },
            selectedCells = selectedCells,
        )
    }

    // Box with constraints to handle layout
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
    ) {
        val maxWidth = maxWidth
        val gridCellSize = maxWidth / width

        LazyVerticalGrid(columns = GridCells.Fixed(width), modifier = modifier
            .height(gridCellSize * height)
            .onGloballyPositioned { coordinates ->
                val position = coordinates.positionInRoot()
                gridOffset = Offset(position.x, position.y)
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        isDragging = true
                        selectedCells = listOf()
                    },
                    onDrag = { change, _ ->
                        val x: Int =
                            ((change.position.x - gridOffset.x) / gridCellSize.toPx()).roundToInt()
                        val y: Int =
                            ((change.position.y - gridOffset.y + 275) / gridCellSize.toPx()).roundToInt()
                        if (x in 0 until width && y in 0 until height) {
                            val cell = x to y
                            if (!selectedCells.contains(cell)) {
                                selectedCells = selectedCells + cell
                            }
                        }
                    },
                    onDragEnd = {
                        isDragging = false
                        showChooseCrop = true
                    }
                )
            }
        ) {
            items(count = height * width, key = {
                it
            }) { index ->
                val x = index % width
                val y = index / width

                val crop = crops.firstOrNull { it.x == x && it.y == y }
                val isSelected = selectedCells.contains(x to y)
                CropSquare(
                    crop = crop,
                    onCropAdd = onCropAdd,
                    y = y,
                    x = x,
                    isSelected = isSelected,
                    modifier = Modifier
                        .width(gridCellSize)
                        .height(gridCellSize)
                )
            }
        }
    }
}

// Composable function for the crop selection dialog
@Composable
fun ChooseCropDialog(
    onCropAdd: (CropInfo) -> Unit,
    onDismiss: () -> Unit,
    selectedCells: List<Pair<Int, Int>>,
) {
    var chosenType by remember {
        mutableStateOf<CropTypes>(CropTypes.None)
    }

    val cropNames = CropTypes.items.map { stringResource(id = it.name) }

    AlertDialog(onDismissRequest = onDismiss, confirmButton = {
        Button(onClick = {
            for (cell in selectedCells) {
                onCropAdd(CropInfo(chosenType, cell.first, cell.second))
            }
            onDismiss()
        }) {
            Text(text = stringResource(id = R.string.add_crop))
        }
    }, dismissButton = {
        Button(onClick = onDismiss) {
            Text(text = stringResource(id = R.string.cancel))
        }
    }, title = {
        Text(text = stringResource(id = R.string.choose_crop))
    }, text = {
        Column {
            SelectTextField(
                selectedValue = stringResource(id = chosenType.name),
                onValueChange = {
                    chosenType = CropTypes.items[cropNames.indexOf(it)]
                },
                selectOptions = CropTypes.items.filter { it != CropTypes.None }.map { stringResource(id = it.name) },
                label = stringResource(id = R.string.crop_type)
            )
        }
    })
}

// Composable function for a single crop square in the grid
@Composable
fun CropSquare(
    crop: CropInfo?,
    modifier: Modifier = Modifier,
    isSelected: Boolean,
    onCropAdd: (CropInfo) -> Unit,
    x: Int,
    y: Int,
) {
    var showChooseCrop by remember { mutableStateOf(false) }
    val cropType = crop?.cropType
    val color = when {
        isSelected -> Color.Red
        cropType == CropTypes.Pumpkins -> Color(0xFFFFA500)
        cropType == CropTypes.LeafyGreens -> Color(0xFF32CD32)
        cropType == CropTypes.Rice -> Color.Gray
        cropType == CropTypes.None -> Color.White
        else -> Color.Green
    }

    if (showChooseCrop) {
        ChooseCropDialog(
            onCropAdd = onCropAdd,
            onDismiss = {
                showChooseCrop = false;
            },
            selectedCells = listOf(x to y),
        )
    }

    // Box composable for the crop square
    Box(modifier = modifier
        .height(50.dp)
        .width(50.dp)
        .background(color)
        .border(1.dp, Color.Black)
        .testTag("cropSquare")
        .clickable(enabled = crop == null) {
            showChooseCrop = true
        })
}

// Composable function for the form to enter crop information for the farm
@Composable
fun EnterCropsForm(
    modifier: Modifier = Modifier,
    addedCrops: List<CropInfo>,
    onCropsChange: (List<CropInfo>) -> Unit = {},
    width: Int,
    height: Int
) {
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

// Represents a single crop field in the form
@Composable
fun CropField(
    cropInfo: CropInfo,
    onCropInfoChange: (CropInfo) -> Unit,
    onDelete: (CropInfo) -> Unit,
    width: Int,
    height: Int
) {
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
            val cropOptions =
                CropTypes.items.filter { it != CropTypes.None }.map { stringResource(id = it.name) }

            Column(modifier = Modifier.width(150.dp)) {
                SelectTextField(
                    selectedValue = cropType,
                    onValueChange = {
                        onCropInfoChange(
                            cropInfo.copy(cropType = CropTypes.items[cropOptions.indexOf(it) + 1])
                        )
                    },
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
