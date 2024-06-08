package com.example.farmsimulator.ui.farm

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.example.farmsimulator.opengl.OpenGLComposeView
import com.google.android.gms.maps.model.LatLng

@Composable
fun FarmView(latLng: LatLng, width: Int, height: Int, crops: List<CropInfo>) {
    var selected by remember {
        mutableStateOf<Pair<Int,Int>>(Pair(0,0))
    }
    var showPopup by remember {
        mutableStateOf(false)
    }

    var popupPosition by remember {
        mutableStateOf(Offset(0f, 0f))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        OpenGLComposeView(modifier = Modifier.fillMaxSize(), width = width, height = height, crops = crops, onClick = {
            // it is upside down
            selected = Pair(it.first, height - it.second - 1)
            showPopup = true
            popupPosition = Offset(it.first.toFloat(), it.second.toFloat())
        })
        Text("Selected: $selected")
    }

    if (showPopup) {
        InfoPopup(
            position = popupPosition,
            crop = crops.find { it.x == selected.first && it.y == selected.second },
            onDismiss = { showPopup = false }
        )
    }
}

@Composable
fun InfoPopup(modifier: Modifier = Modifier, position: Offset, crop: CropInfo?, onDismiss: () -> Unit = {}) {
    Popup(
        alignment = Alignment.TopStart,
        offset = IntOffset(position.x.toInt(), position.y.toInt()),
        onDismissRequest = onDismiss
    ) {
        Box(
            modifier = modifier
                .size(100.dp)
                .background(Color.White)
        ) {
            Text("Crop: ${crop?.cropType}")
        }
    }
}
