package com.example.farmsimulator.ui.farm

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.farmsimulator.R
import com.example.farmsimulator.models.FarmData
import com.example.farmsimulator.stores.SettingsRepository
import com.example.farmsimulator.ui.utils.InputField
import com.example.farmsimulator.ui.utils.createDialog
import com.example.farmsimulator.utils.DEFAULT_LAT_LONG
import com.example.farmsimulator.utils.NotificationHandler
import com.example.farmsimulator.utils.RequestLocationPermissionBinary
import com.example.farmsimulator.utils.getLocation
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState

// Page to locate the farm
@OptIn(ExperimentalPermissionsApi::class)
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun LocatorPage(
    onCropPlannerClick: (height: Int, width: Int, latLng: LatLng, List<CropInfo>) -> Unit,
    previousFarms: List<FarmData>,
    settingsRepository: SettingsRepository,
    isLoading: Boolean
) {
    val context = LocalContext.current
    val locationClient = LocationServices.getFusedLocationProviderClient(context)
    val keyboardController = LocalSoftwareKeyboardController.current

    var height by remember { mutableIntStateOf(0) }
    var width by remember { mutableIntStateOf(0) }

    var showMap by remember { mutableStateOf(false) }
    var latLng by remember { mutableStateOf(DEFAULT_LAT_LONG) }
    val scrollState = rememberScrollState()

    var useLocation by remember { mutableStateOf(false) }
    var locationAccessible by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var isPositioned by remember { mutableStateOf(false) }

    val postNotificationPermission =
        rememberPermissionState(permission = android.Manifest.permission.POST_NOTIFICATIONS)
    val notificationHandler = NotificationHandler(context)
    val lowDataMode = settingsRepository.lowDataModeFlow.collectAsState(initial = false).value

    LaunchedEffect(Unit) {
        if (!postNotificationPermission.status.isGranted) {
            postNotificationPermission.launchPermissionRequest()
        }
    }

    if (!lowDataMode) {
        RequestLocationPermissionBinary {
            locationAccessible = it
        }
    }

    val locationAcquiredString = stringResource(id = R.string.location_acquired)
    val locationNotAcquiredString = stringResource(id = R.string.location_not_acquired)
    val latLngString = stringResource(id = R.string.latlng)
    val defaultLocationString = stringResource(id = R.string.default_location)

    LaunchedEffect(useLocation) {
        if (useLocation && locationAccessible && !lowDataMode) {
            loading = true
            getLocation(
                context = context,
                fusedLocationClient = locationClient,
                onGetLocationSuccess = {
                    latLng = it
                    loading = false
                    notificationHandler.showNotification(
                        locationAcquiredString,
                        latLngString.format(it.latitude, it.longitude)
                    )
                },
                onGetLocationFailed = {
                    latLng = DEFAULT_LAT_LONG
                    loading = false
                    notificationHandler.showNotification(
                        locationNotAcquiredString,
                        defaultLocationString
                    )
                }
            )
        } else {
            latLng = DEFAULT_LAT_LONG
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(scrollState)
            .testTag("locatorPage"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {


        FarmDimensionsForm(
            useLocation = useLocation && !lowDataMode,
            locationAccessible = locationAccessible && !lowDataMode,
            context = context,
            loading = loading,
            onUseLocationChange = { useLocation = it },
            lowDataMode = lowDataMode,
            onSubmit = { w, h ->
                showMap = true
                keyboardController?.hide()
                height = h
                width = w
                isPositioned = true
            }
        )

        if (showMap) {
            WorldMap(
                latLng = latLng,
                setLatLng = {
                    latLng = it
                }, lowDataMode = lowDataMode
            )
        }

        if (isPositioned) {
            NextPageButton {
                onCropPlannerClick(height, width, latLng, emptyList())
            }
        }
        Spacer(modifier = Modifier.size(16.dp))

        PreviousFarms(previousFarms = previousFarms, onFarmSelected = {
            onCropPlannerClick(it.height, it.width, it.latLong, it.crops)
        })
    }
}

// Button to proceed to the crop planner
@Composable
fun NextPageButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 2.dp)
    ) {
        Text(
            text = stringResource(id = R.string.proceed_to_planner),
            style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold)
        )
    }
}

// Form to enter the dimensions of the farm
@Composable
fun FarmDimensionsForm(
    useLocation: Boolean,
    context: Context,
    locationAccessible: Boolean,
    loading: Boolean,
    onUseLocationChange: (Boolean) -> Unit,
    lowDataMode: Boolean,
    onSubmit: (width: Int, height: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var height by remember { mutableStateOf("") }
    var width by remember { mutableStateOf("") }
    var heightError by remember { mutableStateOf("") }
    var widthError by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(!isOnline(context)) }

    val heightString = stringResource(id = R.string.height)
    val widthString = stringResource(id = R.string.width)
    if (showDialog) {
        createDialog(
            onDismissRequest = { showDialog = false },
            onConfirmation = { showDialog = false },
            dialogTitle = stringResource(id = R.string.offline_title),
            dialogText = stringResource(id = R.string.offline_message),
            icon = Icons.Default.Warning
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {

        Text(
            text = stringResource(id = R.string.enter_dimensions),
            style = MaterialTheme.typography.headlineMedium
        )

        InputField(
            value = height,
            onValueChange = {
                heightError = ""
                if (it.toIntOrNull() != null || it.isEmpty()) {
                    height = it
                }
            },
            label = heightString,
            error = heightError,
            onValueError = { heightError = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 2.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            trailingIcon = {
                Text(
                    text = stringResource(id = R.string.meters),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        )

        InputField(
            value = width,
            onValueChange = {
                widthError = ""
                if (it.toIntOrNull() != null || it.isEmpty()) {
                    width = it
                }
            },
            label = widthString,
            error = widthError,
            onValueError = { widthError = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 2.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            trailingIcon = {
                Text(
                    text = stringResource(id = R.string.meters),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        )

        if(!lowDataMode){
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = useLocation,
                    onCheckedChange = onUseLocationChange,
                    enabled = locationAccessible
                )
                Text(
                    text = stringResource(id = R.string.use_location),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        val emptyErrorString = stringResource(id = R.string.empty_error)
        val rangeErrorString = stringResource(id = R.string.invalid_range)
        val numberErrorString = stringResource(id = R.string.number_error)
        Button(
            onClick = {
                val heightValidation = validateInput(
                    height,
                    parse = { it.toDoubleOrNull() },
                    predicates = listOf { it in 1.0..15.0 })
                val widthValidation = validateInput(
                    width,
                    parse = { it.toDoubleOrNull() },
                    predicates = listOf { it in 1.0..15.0 })

                if (heightValidation == DimensionInputError.NONE && widthValidation == DimensionInputError.NONE) {
                    onSubmit(width.toInt(), height.toInt())
                } else {
                    heightError = when (heightValidation) {
                        DimensionInputError.EMPTY -> emptyErrorString.format(heightString)
                        DimensionInputError.TYPE_MISMATCH -> numberErrorString.format(heightString)
                        DimensionInputError.OUT_OF_RANGE -> rangeErrorString.format(
                            heightString,
                            1,
                            25
                        )

                        DimensionInputError.NONE -> ""
                    }
                    widthError = when (widthValidation) {
                        DimensionInputError.EMPTY -> emptyErrorString.format(widthString)
                        DimensionInputError.TYPE_MISMATCH -> numberErrorString.format(widthString)
                        DimensionInputError.OUT_OF_RANGE -> rangeErrorString.format(
                            widthString,
                            1,
                            25
                        )

                        DimensionInputError.NONE -> ""
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 60.dp),
            enabled = !loading
        ) {
            Text(
                text = if (loading) stringResource(id = R.string.loading) else stringResource(id = R.string.get_location),
                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)
            )
        }
    }
}

// Enum class to represent the different types of errors
enum class DimensionInputError {
    EMPTY,
    TYPE_MISMATCH,
    OUT_OF_RANGE,
    NONE
}

// Helper function to validate input
inline fun <reified T> validateInput(
    input: String,
    parse: (String) -> T? = { if (it is T) it else null },
    predicates: List<(T) -> Boolean> = emptyList()
): DimensionInputError {

    if (input.isEmpty()) {
        return DimensionInputError.EMPTY
    }

    val parsed = try {
        parse(input)
    } catch (e: Exception) {
        null
    } ?: return DimensionInputError.TYPE_MISMATCH

    if (predicates.any { !it(parsed) }) {
        return DimensionInputError.OUT_OF_RANGE
    }

    return DimensionInputError.NONE
}

// Show the world map using Google Maps
@Composable
fun WorldMap(
    latLng: LatLng, setLatLng: (LatLng) -> Unit,
    lowDataMode: Boolean,
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

    val longLatString = stringResource(id = R.string.latlng)

    var longError by remember { mutableStateOf("") }
    var latError by remember { mutableStateOf("") }

    Text(
        text = longLatString.format(latLng.latitude, latLng.longitude),
        style = MaterialTheme.typography.bodyMedium
    )
    Spacer(modifier = Modifier.size(8.dp))

    Column {
        // Input fields for latitude and longitude
        InputField(
            value = latLng.latitude.toString(),
            onValueChange = {
                val newLat = it.toDoubleOrNull()
                if (newLat != null) {
                    val newLatLng = LatLng(newLat, latLng.longitude)
                    setLatLng(newLatLng)
                }
            },
            label = stringResource(id = R.string.latitude),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            error = latError,
            onValueError = { latError = it }
        )

        InputField(
            value = latLng.longitude.toString(),
            onValueChange = {
                val newLong = it.toDoubleOrNull()
                if (newLong != null) {
                    val newLatLng = LatLng(latLng.latitude, newLong)
                    setLatLng(newLatLng)
                }
            },
            label = stringResource(id = R.string.longitiude),
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            error = longError,
            onValueError = { longError = it }
        )
    }

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

    if (!lowDataMode) {
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
}

fun isOnline(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val capabilities =
        connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)

    if (capabilities != null) {
        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
            Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
            return true
        } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
            return true
        } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
            Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
            return true
        }
    }
    return false
}


@Composable
fun PreviousFarms(previousFarms: List<FarmData>, onFarmSelected: (FarmData) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val selectedIndex = remember { mutableIntStateOf(-1) }

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable(onClick = { expanded = true })
                .padding(vertical = 12.dp, horizontal = 2.dp)
        ) {
            Text(
                text = "Previous Farms",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = "Dropdown Indicator",
                tint = Color.Black
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            previousFarms.forEachIndexed { index, farm ->
                DropdownMenuItem(onClick = {
                    selectedIndex.intValue = index
                    expanded = false
                    onFarmSelected(farm)
                }, text = {
                    Text(
                        text = "Farm ${farm.width}x${farm.height}",
                        style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    )
                })
            }
            if (previousFarms.isEmpty()) {
                DropdownMenuItem(onClick = {}, text = {
                    Text(
                        text = "No previous farms",
                        style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    )
                })
            }
        }

        if (selectedIndex.intValue != -1) {
            Text(
                text = "Selected: Farm ${previousFarms[selectedIndex.intValue].width}x${previousFarms[selectedIndex.intValue].height}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}
