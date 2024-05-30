package com.example.myapplication

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState

class MainActivity : ComponentActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationPermissionGranted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setContent {
            MyApplicationTheme(dynamicColor = false) {
                var isLocationPermissionGranted by remember { mutableStateOf(false) }
                var isLocationPermissionDenied by remember { mutableStateOf(false) }
                var isLocationPermissionRevoked by remember { mutableStateOf(false) }

                var location by remember { mutableStateOf(
                    LatLng(11.5564, 104.9282)
                )}

                var currentScreen by remember {
                    mutableStateOf(Screen.Home)
                }

                RequestLocationPermission(onPermissionGranted = {
                    isLocationPermissionGranted = true

                    getLastUserLocation(
                        onGetLastLocationSuccess = {
                            location = LatLng(it.first, it.second)
                        },
                        onGetLastLocationFailed = {
                            getCurrentLocation(
                                onGetCurrentLocationSuccess = {
                                    location = LatLng(it.first, it.second)
                                },
                                onGetCurrentLocationFailed = {
                                    location = LatLng(11.5564, 104.9282)
                                }
                            )
                        }
                    )

                }, onPermissionDenied = {
                    isLocationPermissionDenied = true
                }, onPermissionsRevoked = {
                    isLocationPermissionRevoked = true
                })


                Scaffold(modifier = Modifier.fillMaxSize(), bottomBar = {
                    BottomNav(selectedScreen = currentScreen) {
                        currentScreen = it
                    }
                }) { paddingValues ->
                    Box(modifier = Modifier.padding(paddingValues)) {
                        when (currentScreen) {
                            Screen.Home -> MyMap(latLng = location)
                            Screen.Profile -> Text(currentScreen.title)
                            Screen.Settings -> Text(currentScreen.title)
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastUserLocation(
        onGetLastLocationSuccess: (Pair<Double, Double>) -> Unit,
        onGetLastLocationFailed: (Exception) -> Unit
    ) {
        if (areLocationPermissionsGranted()) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        location?.let {
                            onGetLastLocationSuccess(Pair(it.latitude, it.longitude))
                        }
                    }.addOnFailureListener { exception ->
                        onGetLastLocationFailed(exception)
                    }
            }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation(
        onGetCurrentLocationSuccess: (Pair<Double, Double>) -> Unit,
        onGetCurrentLocationFailed: (Exception) -> Unit,
        priority: Boolean = true
    ) {
        val accuracy = if (priority) Priority.PRIORITY_HIGH_ACCURACY
        else Priority.PRIORITY_BALANCED_POWER_ACCURACY

        if (areLocationPermissionsGranted()) {
            fusedLocationClient.getCurrentLocation(
                accuracy, CancellationTokenSource().token,
            ).addOnSuccessListener { location ->
                location?.let {
                    onGetCurrentLocationSuccess(Pair(it.latitude, it.longitude))
                }
            }.addOnFailureListener { exception ->
                onGetCurrentLocationFailed(exception)
            }
        }
    }

    private fun areLocationPermissionsGranted(): Boolean {
        return locationPermissionGranted || (ActivityCompat.checkSelfPermission(
            this, android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            this, android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED)
    }
}


@OptIn(ExperimentalPermissionsApi::class, ExperimentalPermissionsApi::class)
@Composable
fun RequestLocationPermission(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit,
    onPermissionsRevoked: () -> Unit
) {
    val permissionState = rememberMultiplePermissionsState(
        listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    LaunchedEffect(key1 = permissionState) {
        val allPermissionsRevoked =
            permissionState.permissions.size == permissionState.revokedPermissions.size

        val permissionsToRequest = permissionState.permissions.filter {
            !it.status.isGranted
        }

        if (permissionsToRequest.isNotEmpty()) permissionState.launchMultiplePermissionRequest()

        if (allPermissionsRevoked) {
            onPermissionsRevoked()
        } else {
            if (permissionState.allPermissionsGranted) {
                onPermissionGranted()
            } else {
                onPermissionDenied()
            }
        }
    }
}

@Composable
fun MyMap(latLng: LatLng, modifier: Modifier = Modifier) {
    var isMapLoaded by remember { mutableStateOf(false) }
    var zoom by remember { mutableFloatStateOf(15f) }
    var uiSettings by remember { mutableStateOf(MapUiSettings()) }
    var properties by remember { mutableStateOf(MapProperties(
        mapType = MapType.SATELLITE
    )) }

    Box(modifier = modifier) {
        if (isMapLoaded) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                GoogleMap(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                        .size(300.dp),

                    onMapLoaded = {
                        isMapLoaded = true
                    },
                    cameraPositionState = rememberCameraPositionState {
                        position = CameraPosition.fromLatLngZoom(latLng, zoom)
                    },
                    uiSettings = uiSettings,
                    properties = properties
                )

                Button(
                    onClick = { isMapLoaded = false },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Text("Close map")
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                Button(
                    onClick = { isMapLoaded = true },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Text("Open map")
                }
            }
        }
    }
}