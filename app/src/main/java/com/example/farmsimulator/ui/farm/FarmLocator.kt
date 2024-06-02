package com.example.farmsimulator.ui.farm

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
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

@OptIn(ExperimentalPermissionsApi::class)
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun LocatorPage(onCropPlannerClick: (height: Double, width: Double, latLng: LatLng) -> Unit) {
    val context = LocalContext.current
    val locationClient = LocationServices.getFusedLocationProviderClient(context)
    val keyboardController = LocalSoftwareKeyboardController.current

    var height by remember { mutableDoubleStateOf(0.0) }
    var width by remember { mutableDoubleStateOf(0.0) }

    var showMap by remember { mutableStateOf(false) }
    var latLng by remember { mutableStateOf(DEFAULT_LAT_LONG) }
    val scrollState = rememberScrollState()

    var useLocation by remember { mutableStateOf(false) }
    var locationAccessible by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var isPositioned by remember { mutableStateOf(false) }

    val postNotificationPermission = rememberPermissionState(permission = android.Manifest.permission.POST_NOTIFICATIONS)
    val notificationHandler = NotificationHandler(context)

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
                    notificationHandler.showNotification(locationAcquiredString, latLngString.format(it.latitude, it.longitude))
                },
                onGetLocationFailed = {
                    latLng = DEFAULT_LAT_LONG
                    loading = false
                    notificationHandler.showNotification(locationNotAcquiredString, defaultLocationString)
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

        Text(text = stringResource(id = R.string.enter_dimensions), style = MaterialTheme.typography.headlineMedium)

        InputField(
            value = height,
            onValueChange = {
                height = it
                heightError = ""
            },
            label = stringResource(id = R.string.height),
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
            label = stringResource(id = R.string.width),
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
            Text(text = stringResource(id = R.string.use_location), style = MaterialTheme.typography.bodyMedium)
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
                text = if (loading) stringResource(id = R.string.loading) else stringResource(id = R.string.get_location),
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
            val text = if (error.isNotEmpty()) stringResource(id = R.string.input_error, label, error) else label
            Text(text = text)
        },
        modifier = modifier,
        isError = error.isNotEmpty(),
    )
}

@Composable
fun validateInput(
    height: String,
    setHeightError: (String) -> Unit,
    width: String,
    setWidthError: (String) -> Unit
): Boolean {
    val heightValue = height.toDoubleOrNull()
    val widthValue = width.toDoubleOrNull()

    val heightError = when (heightValue) {
        null -> stringResource(id = R.string.number_error, stringResource(id = R.string.height))
        !in 1.0..1000.0 -> stringResource(id = R.string.input_error, stringResource(id = R.string.height), 1, 1000)
        else -> null
    }

    val widthError = when (widthValue) {
        null -> stringResource(id = R.string.number_error, stringResource(id = R.string.width))
        !in 1.0..1000.0 -> stringResource(id = R.string.input_error, stringResource(id = R.string.width), 1, 1000)
        else -> null
    }

    setHeightError(heightError.orEmpty())
    setWidthError(widthError.orEmpty())

    return heightError == null && widthError == null
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
