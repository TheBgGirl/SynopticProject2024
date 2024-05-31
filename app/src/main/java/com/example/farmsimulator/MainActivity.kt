package com.example.farmsimulator

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScopeInstance.align
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScopeInstance.align
import androidx.compose.foundation.layout.FlowColumnScopeInstance.align
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.navigation.compose.rememberNavController
import com.example.farmsimulator.ui.theme.FarmSimulator
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setContent {
            FarmSimulator {
                val navController = rememberNavController()

                Scaffold(modifier = Modifier.fillMaxSize(), bottomBar = {
                    BottomNav(navController = navController)
                }) { paddingValues ->
                    Box(modifier = Modifier.padding(paddingValues)) {
                        NavGraph(navController = navController)
                    }
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
fun getLastUserLocation(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient,
    onGetLastLocationSuccess: (Pair<Double, Double>) -> Unit,
    onGetLastLocationFailed: (Exception) -> Unit
) {
    if (areLocationPermissionsGranted(context)) {
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
fun getCurrentLocation(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient,
    onGetCurrentLocationSuccess: (LatLng) -> Unit,
    onGetCurrentLocationFailed: (Exception) -> Unit,
    priority: Boolean = true
) {
    val accuracy = if (priority) Priority.PRIORITY_HIGH_ACCURACY
    else Priority.PRIORITY_BALANCED_POWER_ACCURACY

    if (areLocationPermissionsGranted(context)) {
        fusedLocationClient.getCurrentLocation(
            accuracy, CancellationTokenSource().token,
        ).addOnSuccessListener { location ->
            location?.let {
                onGetCurrentLocationSuccess(LatLng(it.latitude, it.longitude))
            }
        }.addOnFailureListener { exception ->
            onGetCurrentLocationFailed(exception)
        }
    }
}

fun areLocationPermissionsGranted(
    context: Context
): Boolean {
    return (ActivityCompat.checkSelfPermission(
        context, android.Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
        context, android.Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED)
}


@Composable
fun LandingPage() {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Welcome to Farm Simulator",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 2.dp)
                        .testTag("welcomeText")
                )
                Image(
                    painter = painterResource(id = R.drawable.hero),
                    contentDescription = "Landing Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 2.dp)
                        .testTag("landingImage")
                )
                Text(
                    text = "Welcome to Farm Simulator, your essential tool for strategic farming and flood management. Through our intuitive grid-based layout, you can easily identify which areas of your farm are most susceptible to flooding and how often they are affected.",
                    style = TextStyle(fontSize = 20.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 2.dp)
                )
                Text(
                    text = "Plan your crop placements with precision, as our simulator calculates potential yields based on your management decisions. Gain insights into water risks and optimize your agricultural output by understanding the dynamics of your land. Begin your journey with Farm Simulator today and turn your farming challenges into opportunities for innovation!",
                    style = TextStyle(fontSize = 20.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 2.dp)
                )
            }
        }
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

        if (permissionsToRequest.isNotEmpty()) {
            permissionState.launchMultiplePermissionRequest()
        }

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
fun DetailsPage() {
    val context = LocalContext.current
    val locationClient = LocationServices.getFusedLocationProviderClient(context)

    var height by remember { mutableStateOf("") }
    var width by remember { mutableStateOf("") }
    var heightError by remember { mutableStateOf("") }
    var widthError by remember { mutableStateOf("") }
    var showMap by remember {
        mutableStateOf(false)
    }
    var latLng by remember {
        mutableStateOf<LatLng?>(null)
    }
    val scrollState = rememberScrollState()
    var locationText by remember {
        mutableStateOf("No location found")
    }

    RequestLocationPermission(
        onPermissionGranted = {
            getCurrentLocation(
                context = context, fusedLocationClient = locationClient,
                onGetCurrentLocationSuccess = {
                    latLng = it
                    locationText =
                        "Latitude: %.2f, Longitude: %.2f".format(it.latitude, it.longitude)
                },
                onGetCurrentLocationFailed = {
                    latLng = null
                })
        },
        onPermissionDenied = {
            latLng = null
        },
        onPermissionsRevoked = {
            latLng = null
        }
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(scrollState),

        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Enter Farm Dimensions", style = MaterialTheme.typography.headlineMedium)

        InputField(
            value = height,
            onValueChange = {
                height = it
                heightError = ""
            },
            label = "Height",
            error = heightError,
            onValueError = { heightError = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 2.dp)
        )

        InputField(
            value = width,
            onValueChange = {
                width = it
                widthError = ""
            },
            label = "Width",
            error = widthError,
            onValueError = { widthError = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 2.dp)
        )

        Button(
            onClick = {
                showMap = validateInput(
                    height,
                    setHeightError = { heightError = it },
                    width,
                    setWidthError = { widthError = it })
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 2.dp),
        ) {
            Text(
                text = "Get Location",
                style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold)
            )
        }

        if (showMap) {
            Text(text = locationText, style = MaterialTheme.typography.bodyMedium)

            latLng?.let {
                MyMap(
                    latLng = it,
                    setLatLng = { new: LatLng ->
                        latLng = new
                        locationText =
                            "Latitude: %.2f, Longitude: %.2f".format(new.latitude, new.longitude)
                    })
            }
        }
    }
}

@Composable
fun FarmSizeForm(
    onSubmit: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var height by remember { mutableStateOf("") }
    var width by remember { mutableStateOf("") }
    var heightError by remember { mutableStateOf("") }
    var widthError by remember { mutableStateOf("") }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        InputField(
            value = height,
            onValueChange = {
                height = it
                heightError = ""
            },
            label = "Height",
            error = heightError,
            onValueError = { heightError = it }
        )

        InputField(
            value = width,
            onValueChange = {
                width = it
                widthError = ""
            },
            label = "Width",
            error = widthError,
            onValueError = { widthError = it }
        )

        Button(
            onClick = {
                val isValid = validateInput(
                    height,
                    setHeightError = { heightError = it },
                    width,
                    setWidthError = { widthError = it }
                )
                if (isValid) {
                    onSubmit(height.toInt(), width.toInt())
                }
            }
        ) {
            Text("Submit")
        }
    }
}

@Composable
fun InputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String,
    onValueError: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = {
            onValueChange(it)
            onValueError("")
        },
        label = {
            val text = if (error.isNotEmpty()) "$label - Error: $error" else label
            Text(text = text)
        },
        modifier = modifier,
        isError = error.isNotEmpty(),
    )
}

fun validateInput(
    height: String,
    setHeightError: (String) -> Unit,
    width: String,
    setWidthError: (String) -> Unit
): Boolean {
    val heightValue = height.toIntOrNull()
    val widthValue = width.toIntOrNull()
    var isValid = true
    if (heightValue == null || heightValue !in 1..1000) {
        setHeightError("Enter a valid height between 1 and 1000")
        isValid = false
    }

    if (widthValue == null || widthValue !in 1..1000) {
        setWidthError("Enter a valid width between 1 and 1000")
        isValid = false
    }
    return isValid
}

@Composable
fun MyMap(
    latLng: LatLng, setLatLng: (LatLng) -> Unit,
    modifier: Modifier = Modifier
) {
    var isMapLoaded by remember { mutableStateOf(false) }
    val zoom by remember { mutableFloatStateOf(15f) }
    val uiSettings by remember { mutableStateOf(MapUiSettings()) }
    val properties by remember {
        mutableStateOf(
            MapProperties(
                mapType = MapType.SATELLITE
            )
        )
    }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(latLng, zoom)
    }

    LaunchedEffect(cameraPositionState.isMoving) {
        snapshotFlow { cameraPositionState.position }
            .collect { position ->
                if (!cameraPositionState.isMoving) {
                    setLatLng(position.target)
                }
            }
    }

    Box(modifier = modifier) {
        GoogleMap(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(16.dp)
                .size(300.dp),
            onMapLoaded = {
                isMapLoaded = true
            },
            cameraPositionState = cameraPositionState,
            uiSettings = uiSettings,
            properties = properties
        )
    }
}