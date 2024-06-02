package com.example.farmsimulator.ui.farm

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.LatLng

@Composable
fun PlannerPage(latLng: LatLng, height: Double, width: Double, onBackNavigation: () -> Unit) {
    val scroll = rememberScrollState()

    Column (modifier = Modifier.verticalScroll(scroll)) {
        Text("Crop Planner")
        Text("Height: $height")
        Text("Width: $width")
        Text("Latitude: ${latLng.latitude}")
        Text("Longitude: ${latLng.longitude}")

        Button(onClick = onBackNavigation) {
            Text("Back")
        }
    }
}
