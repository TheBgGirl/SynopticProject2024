package com.example.farmsimulator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import com.example.farmsimulator.ui.theme.FarmSimulator
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

@Composable
fun RequestLocationPermissionBinary(callback: (Boolean) -> Unit) {
    RequestLocationPermission(
        onPermissionGranted = { callback(true) },
        onPermissionDenied = { callback(false) },
        onPermissionsRevoked = { callback(false) }
    )
}

@Composable
fun DetailsPage() {
    val context = LocalContext.current
    val locationClient = LocationServices.getFusedLocationProviderClient(context)
    val keyboardController = LocalSoftwareKeyboardController.current
    val defaultLatLong = LatLng(20.1, 77.5)

    // use double to conform with the Google Maps API (lat lng)
    var height by remember { mutableDoubleStateOf(0.0) }
    var width by remember { mutableDoubleStateOf(0.0) }

    var showMap by remember { mutableStateOf(false) }
    var latLng by remember { mutableStateOf(defaultLatLong) }
    val scrollState = rememberScrollState()

    var useLocation by remember { mutableStateOf(false) }
    var locationAccessible by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }

    RequestLocationPermissionBinary {
        locationAccessible = it
    }

    LaunchedEffect(useLocation) {
        if (useLocation && locationAccessible) {
            loading = true
            getCurrentLocation(
                context = context,
                fusedLocationClient = locationClient,
                onGetCurrentLocationSuccess = { newLocation ->
                    latLng = newLocation
                    loading = false
                },
                onGetCurrentLocationFailed = {
                    latLng = defaultLatLong
                    loading = false
                }
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FarmDimensionsForm(
            useLocation = useLocation,
            locationAccessible = locationAccessible,
            loading = loading,
            onUseLocationChange = { useLocation = it },
            onSubmit = { w, h ->
                showMap = true
                keyboardController?.hide()
                height = h
                width = w
            }
        )

        if (showMap) {
            MyMap(
                latLng = latLng,
                setLatLng = {
                    latLng = it
                })
        }
    }
}

@Composable
fun FarmDimensionsForm(
    useLocation: Boolean,
    locationAccessible: Boolean,
    loading: Boolean,
    onUseLocationChange: (Boolean) -> Unit,
    onSubmit: (width: Double, height: Double) -> Unit
) {
    var height by remember { mutableStateOf("") }
    var width by remember { mutableStateOf("") }
    var heightError by remember { mutableStateOf("") }
    var widthError by remember { mutableStateOf("") }

    Column(
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

        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = useLocation,
                onCheckedChange = onUseLocationChange,
                enabled = locationAccessible
            )
            Text(text = "Use current location", style = MaterialTheme.typography.bodyMedium)
        }

        Button(
            onClick = {
                if (validateInput(height, { heightError = it }, width, { widthError = it })) {
                    onSubmit(width.toDouble(), height.toDouble())
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 2.dp),
            enabled = !loading
        ) {
            Text(
                text = if (loading) "Loading..." else "Get Location",
                style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold)
            )
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
    val heightValue = height.toDoubleOrNull()
    val widthValue = width.toDoubleOrNull()

    val heightError = when (heightValue) {
        null -> "Height must be a number"
        !in 1.0..1000.0 -> "Enter a valid height between 1 and 1000"
        else -> null
    }

    val widthError = when (widthValue) {
        null -> "Width must be a number"
        !in 1.0..1000.0 -> "Enter a valid width between 1 and 1000"
        else -> null
    }

    setHeightError(heightError.orEmpty())
    setWidthError(widthError.orEmpty())

    return heightError == null && widthError == null
}

@Composable
fun MyMap(
    latLng: LatLng, setLatLng: (LatLng) -> Unit,
    modifier: Modifier = Modifier
) {
    var isMapLoaded by remember { mutableStateOf(false) }
    var zoom by remember { mutableFloatStateOf(15f) }
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

    Text(text = "Latitude: %.2f Longitude: %.2f".format(latLng.latitude, latLng.longitude))

    LaunchedEffect(key1 = cameraPositionState.isMoving) {
        snapshotFlow { cameraPositionState.position }
            .collect { position ->
                if (!cameraPositionState.isMoving) {
                    setLatLng(position.target)
                }
                if (position.zoom != zoom) {
                    zoom = position.zoom
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