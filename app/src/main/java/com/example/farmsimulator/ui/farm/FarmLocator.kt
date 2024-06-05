package com.example.farmsimulator.ui.farm

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.farmsimulator.ui.utils.InputField
import com.example.farmsimulator.utils.createDialog

@OptIn(ExperimentalPermissionsApi::class)
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun LocatorPage(onCropPlannerClick: (height: Int, width: Int, latLng: LatLng) -> Unit) {
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
    val hasConnection by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!postNotificationPermission.status.isGranted) {
            postNotificationPermission.launchPermissionRequest()
        }
    }

    RequestLocationPermissionBinary {
        /*
        if (it) {
            notificationHandler.showNotification("Location permission granted", "You can now use location")
        } else {
            notificationHandler.showNotification("Location permission denied", "Using default location")
        }
         */

        locationAccessible = it
    }

    val locationAcquiredString = stringResource(id = R.string.location_acquired)
    val locationNotAcquiredString = stringResource(id = R.string.location_not_acquired)
    val latLngString = stringResource(id = R.string.latlng)
    val defaultLocationString = stringResource(id = R.string.default_location)

    LaunchedEffect(useLocation) {
        if (useLocation && locationAccessible) {
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
            useLocation = useLocation,
            locationAccessible = locationAccessible,
            context = context,
            loading = loading,
            onUseLocationChange = { useLocation = it },
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
                })
        }

        if (isPositioned) {
            NextPageButton {
                onCropPlannerClick(height, width, latLng)
            }
        }
    }
}

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

@Composable
fun FarmDimensionsForm(
    useLocation: Boolean,
    context: Context,
    locationAccessible: Boolean,
    loading: Boolean,
    onUseLocationChange: (Boolean) -> Unit,
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

        val emptyErrorString = stringResource(id = R.string.empty_error)
        val rangeErrorString = stringResource(id = R.string.invalid_range)
        val numberErrorString = stringResource(id = R.string.number_error)
        Button(
            onClick = {
                val heightValidation = validateInput(height, parse = { it.toDoubleOrNull() }, predicates = listOf { it in 1.0..1000.0 })
                val widthValidation = validateInput(width, parse = { it.toDoubleOrNull() }, predicates = listOf { it in 1.0..1000.0 })

                if (heightValidation == DimensionInputError.NONE && widthValidation == DimensionInputError.NONE) {
                    onSubmit(width.toInt(), height.toInt())
                } else {
                    heightError = when (heightValidation) {
                        DimensionInputError.EMPTY -> emptyErrorString.format(heightString)
                        DimensionInputError.TYPE_MISMATCH -> numberErrorString.format(heightString)
                        DimensionInputError.OUT_OF_RANGE -> rangeErrorString.format(heightString, 1, 1000)
                        DimensionInputError.NONE -> ""
                    }
                    widthError = when (widthValidation) {
                        DimensionInputError.EMPTY -> emptyErrorString.format(widthString)
                        DimensionInputError.TYPE_MISMATCH -> numberErrorString.format(widthString)
                        DimensionInputError.OUT_OF_RANGE -> rangeErrorString.format(widthString, 1, 1000)
                        DimensionInputError.NONE -> ""
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 2.dp),
            enabled = !loading
        ) {
            Text(
                text = if (loading) stringResource(id = R.string.loading) else stringResource(id = R.string.get_location),
                style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold)
            )
        }
    }
}

// fix this logic
enum class DimensionInputError {
    EMPTY,
    TYPE_MISMATCH,
    OUT_OF_RANGE,
    NONE
}

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

@Composable
fun WorldMap(
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

    val longLatString = stringResource(id = R.string.latlng)

    Text(
        text = longLatString.format(latLng.latitude, latLng.longitude),
        style = MaterialTheme.typography.bodyMedium
    )

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