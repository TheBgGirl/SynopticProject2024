package com.example.farmsimulator.ui.farm

import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.example.farmsimulator.R
import com.example.farmsimulator.opengl.OpenGLComposeView
import com.example.farmsimulator.stores.SettingsRepository
import com.google.android.gms.maps.model.LatLng
import com.wales.Crop
import com.wales.FarmElement

enum class Month(@StringRes val title: Int) {
    JANUARY(R.string.january),
    FEBRUARY(R.string.february),
    MARCH(R.string.march),
    APRIL(R.string.april),
    MAY(R.string.may),
    JUNE(R.string.june),
    JULY(R.string.july),
    AUGUST(R.string.august),
    SEPTEMBER(R.string.september),
    OCTOBER(R.string.october),
    NOVEMBER(R.string.november),
    DECEMBER(R.string.december)
}

fun convertToPredictorForm(width: Int, height: Int, crops: List<CropInfo>): List<List<Crop>> {
    val cropLayout = MutableList(height) { MutableList(width) { Crop.NA } }


    for (i in 0 until height) {
        for (j in 0 until width) {
            val crop = crops.firstOrNull() { it.x == j && it.y == i }
            val cropEnumType = when (crop?.cropType) {
                CropTypes.Rice -> Crop.RICE
                CropTypes.Pumpkins -> Crop.PUMPKIN
                CropTypes.LeafyGreens -> Crop.LEAFY
                else -> Crop.NA
            }
            cropLayout[i][j] = cropEnumType
        }
    }

    return cropLayout
}

@Composable
fun FarmView(latLng: LatLng, width: Int, height: Int, crops: List<CropInfo>, toResults: () -> Unit, settingsRepository: SettingsRepository, ecoMode: Boolean, getYield: (Double, Double, Int, Int, List<List<Crop>>, Int) -> List<List<FarmElement>>, saveYield: (List<List<FarmElement>>) -> Unit) {
    var selected by remember {
        mutableStateOf(Pair(0,0))
    }
    var selectedCrop by remember {
        mutableStateOf<CropInfo?>(null)
    }

    var selectedMonth by remember {
        mutableStateOf(Month.JANUARY)
    }

    var showPopup by remember {
        mutableStateOf(false)
    }

    var yield by remember {
        mutableStateOf<List<List<FarmElement>>>(emptyList())
    }

    var selectedCropYield by remember {
        mutableStateOf<FarmElement?>(null)
    }

    val fieldInPredictorForm = convertToPredictorForm(width, height, crops)

    // on month change, get new yield
    fun getYieldForMonth() {
        val month = if (selectedMonth.ordinal == 0 ) 1 else selectedMonth.ordinal
        val lat = latLng.latitude
        val lon = latLng.longitude
        val width = width
        val height = height
        Log.w("FarmView", "Getting yield for month $month")

        yield = getYield(lat, lon, width, height, fieldInPredictorForm, month)
        Log.w("FarmView", "yield: $yield")
        saveYield(yield)
    }

    // get yield for the first time
    LaunchedEffect (selectedMonth) {
        getYieldForMonth()
        println(yield)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        OpenGLComposeView(modifier = Modifier.fillMaxSize(), width = width, height = height, crops = crops, ecoMode = ecoMode, onClick = {
            // it is upside down
            selected = Pair(it.first, height - it.second - 1)
            selectedCrop = crops.find { crop -> crop.x == selected.first && crop.y == selected.second }
            selectedCropYield = yield[selected.second][selected.first]
            showPopup = true
        }, yield = yield)
        Text("Selected: $selected")
    }

    MonthPopup(
        selectedMonth = selectedMonth,
        onMonthSelected = { selectedMonth = it },
    )

        InfoPopup(
            crop = selectedCrop,
            onDismiss = { selectedCrop = null; showPopup = false },
            yield = selectedCropYield,
            toResults = toResults
        )
}

/*
data class FarmElement(val weather: Weather, val yield: Double, val cropType: Crop)
data class Weather(var temp: Double, val sunshine: Double, val precipitation: Double)
 */
@Composable
fun InfoPopup(
    modifier: Modifier = Modifier,
    crop: CropInfo?,
    yield: FarmElement?,
    onDismiss: () -> Unit = {},
    toResults: () -> Unit = {}
) {
    val targetHeight = if (crop != null) 125.dp else 100.dp
    val height by animateDpAsState(targetValue = targetHeight, label = "popup height")

    val titleText = if (crop != null) stringResource(id = R.string.crop_info) else stringResource(id = R.string.no_crop_selected)

    Popup(
        alignment = Alignment.BottomStart,
        onDismissRequest = onDismiss
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .background(Color.White)
                .wrapContentHeight()
                .padding(16.dp)
                .height(height)
        ) {
            Column {
                Text(
                    text = titleText,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.Black
                )
                if (crop != null) {
                    Row (
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = "${stringResource(id = R.string.type)} ${crop.cropType}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Black
                            )
                            Text(
                                // truncate yield to 2 decimal places
                                text = "${stringResource(id = R.string.yield)} ${yield?.yield?.let { String.format("%.2f", it) }}%",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Black
                            )
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Row {
                        Column {
                            Text(
                                text = "${stringResource(id = R.string.sunshine)} ${yield?.weather?.sunshine?.let { String.format("%.2f", it) }} hours",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Black
                            )
                        }
                        Column {
                            Text(
                                text = "${stringResource(id = R.string.rainfall)} ${yield?.weather?.precipitation?.let { String.format("%.2f", it) }} mm",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Black
                            )
                        }
                        Column {
                            Text(
                                text = "${stringResource(id = R.string.temperature)} ${yield?.weather?.temp?.let { String.format("%.2f", it) }}°C",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Black
                            )
                        }
                    }
                } else {
                    Button(onClick = toResults) {
                        Text(stringResource(id = R.string.see_results))
                    }
                }
            }
        }
    }
}


@Composable
fun MonthPopup(
    modifier: Modifier = Modifier,
    selectedMonth: Month,
    onMonthSelected: (Month) -> Unit,
) {
    Popup(
        alignment = Alignment.TopStart,
        onDismissRequest = {}
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(Color.White)
                .padding(16.dp)
        ) {
            var currentMonth by remember { mutableStateOf(selectedMonth) }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = {
                        currentMonth = currentMonth.minus(1)
                        onMonthSelected(currentMonth)
                    }
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.previous_month))
                }

                Text(
                    text = stringResource(id = currentMonth.title),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall,
                )

                IconButton(
                    onClick = {
                        currentMonth = currentMonth.plus(1)
                        onMonthSelected(currentMonth)
                    }
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = stringResource(id = R.string.next_month))
                }
            }
        }
    }
}

fun Month.minus(months: Int): Month {
    val newOrdinal = (this.ordinal - months + 12) % 12
    return Month.entries[newOrdinal]
}

fun Month.plus(months: Int): Month {
    val newOrdinal = (this.ordinal + months) % 12
    return Month.entries[newOrdinal]
}