package com.example.farmsimulator.ui.farm

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.farmsimulator.opengl.OpenGLComposeView
import com.google.android.gms.maps.model.LatLng

@Composable
fun FarmView(latLng: LatLng, width: Int, height: Int, crops: List<CropInfo>) {
    var selected by remember {
        mutableStateOf<Pair<Int,Int>>(Pair(0,0))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        OpenGLComposeView(modifier = Modifier.height(250.dp), width = width, height = height, crops = crops, onClick = {
            selected = it
        })
        Text("Selected: $selected")
    }
}