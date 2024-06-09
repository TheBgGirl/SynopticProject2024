package com.example.farmsimulator.ui.farm

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.example.farmsimulator.R
import com.example.farmsimulator.opengl.OpenGLComposeView
import com.google.android.gms.maps.model.LatLng

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

@Composable
fun FarmView(latLng: LatLng, width: Int, height: Int, crops: List<CropInfo>, toResults: () -> Unit) {
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        OpenGLComposeView(modifier = Modifier.fillMaxSize(), width = width, height = height, crops = crops, onClick = {
            // it is upside down
            selected = Pair(it.first, height - it.second - 1)
            selectedCrop = crops.find { crop -> crop.x == selected.first && crop.y == selected.second }
            showPopup = true
        })
        Text("Selected: $selected")
    }

    MonthPopup(
        selectedMonth = selectedMonth,
        onMonthSelected = { selectedMonth = it },
    )

    if (selectedCrop != null) {
        InfoPopup(
            crop = selectedCrop,
            onDismiss = { selectedCrop = null; showPopup = false }
        )
    } else {
        NoCropPopup()
    }
}

@Composable
fun NoCropPopup() {
    Popup(
        alignment = Alignment.BottomStart,
        onDismissRequest = {}
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp)
                .height(30.dp)
        ) {
            Column {
                Text(
                    text = stringResource(id = R.string.no_crop_selected),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
fun InfoPopup(
    modifier: Modifier = Modifier,
    crop: CropInfo?,
    onDismiss: () -> Unit = {}
) {
    Popup(
        alignment = Alignment.BottomStart,
        onDismissRequest = onDismiss
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp)
                .height(175.dp)
        ) {
            Column {

                Text(
                    text = stringResource(id = R.string.crop_info),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.Black
                )
                Text(
                    text = "${stringResource(id = R.string.type)} ${crop?.cropType ?: stringResource(id = R.string.no_crop)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Black
                )
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