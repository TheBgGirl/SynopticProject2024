package com.example.farmsimulator

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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
    private var locationPermissionGranted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setContent {
            FarmSimulator {
                var isLocationPermissionGranted by remember { mutableStateOf(false) }
                var isLocationPermissionDenied by remember { mutableStateOf(false) }
                var isLocationPermissionRevoked by remember { mutableStateOf(false) }

                var location by remember {
                    mutableStateOf(
                        LatLng(11.5564, 104.9282)
                    )
                }

                var currentScreen by remember {
                    mutableStateOf(Screen.HOME)
                }

                Scaffold(modifier = Modifier.fillMaxSize(), bottomBar = {
                    BottomNav(selectedScreen = currentScreen) {
                        currentScreen = it
                    }
                }) { paddingValues ->
                    Box(modifier = Modifier.padding(paddingValues)) {
                        when (currentScreen) {
                            Screen.HOME -> LandingPage()
                            Screen.MAP -> DetailsScreen()
                        }
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
    if (areLocationPermissionsGranted(fusedLocationClient, context)) {
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

    if (areLocationPermissionsGranted(fusedLocationClient, context)) {
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

fun areLocationPermissionsGranted(fusedLocationClient: FusedLocationProviderClient, context: Context): Boolean {
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

@SuppressLint("MissingPermission")
@Composable
fun DetailsScreen() {
    val context = LocalContext.current
    val locationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val (height, setHeight) = remember { mutableStateOf("") }
    val (width, setWidth) = remember { mutableStateOf("") }
    val (heightError, setHeightError) = remember { mutableStateOf<String?>(null) }
    val (widthError, setWidthError) = remember { mutableStateOf<String?>(null) }
    val (location, setLocation) = remember { mutableStateOf("") }
    var latLng = LatLng(0.0, 0.0)

    // use other func
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                locationClient.lastLocation.addOnSuccessListener { loc ->
                    loc?.let {
                        setLocation("Latitude: ${loc.latitude}, Longitude: ${loc.longitude}")
                        latLng = LatLng(loc.latitude, loc.longitude)
                    } ?: setLocation("Location not available")
                }.addOnFailureListener {
                    setLocation("Failed to get location")
                }
            } else {
                setLocation("Permission Denied")
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Enter Farm Dimensions", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = height,
            onValueChange = {
                setHeight(it)
                setHeightError(null)  // Clear error when user changes text
            },
            label = {
                if (heightError != null) Text("Height (Error: $heightError)")
                else Text("Height")
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 2.dp),
            isError = heightError != null
        )
        OutlinedTextField(
            value = width,
            onValueChange = {
                setWidth(it)
                setWidthError(null)
            },
            label = { Text(if (heightError != null) "Width - Error: $widthError" else "width") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 2.dp),
            isError = widthError != null,
            singleLine = true
        )

        Button(
            onClick = {
                if (!validateInput(height, setHeightError, width, setWidthError)) {
                    locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 2.dp),
        ) {
            Text(
                "Get Location",
                style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold)
            )
        }

        if (location.isNotEmpty()) {
            Text(location, style = MaterialTheme.typography.bodyMedium)

            MyMap(latLng = latLng)
        }
    }
}

fun validateInput(
    height: String,
    setHeightError: (String) -> Unit,
    width: String,
    setWidthError: (String) -> Unit
): Boolean {
    val heightValue = height.toIntOrNull()
    val widthValue = width.toIntOrNull()
    var error = false;
    if (heightValue == null || heightValue !in 1..1000) {
        setHeightError("Enter a valid height between 1 and 1000")
        error = true;
    }

    if (widthValue == null || widthValue !in 1..1000) {
        setWidthError("Enter a valid width between 1 and 1000")
        error = true;
    }
    return error;
}

@Composable
fun MyMap(latLng: LatLng, modifier: Modifier = Modifier) {
    var isMapLoaded by remember { mutableStateOf(false) }
    var zoom by remember { mutableFloatStateOf(15f) }
    var uiSettings by remember { mutableStateOf(MapUiSettings()) }
    var properties by remember {
        mutableStateOf(
            MapProperties(
                mapType = MapType.SATELLITE
            )
        )
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
                cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(latLng, zoom)
                },
                uiSettings = uiSettings,
                properties = properties
            )
    }
}