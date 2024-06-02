package com.example.farmsimulator.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.core.app.ActivityCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource

@OptIn(ExperimentalPermissionsApi::class, ExperimentalPermissionsApi::class)
@Composable
fun RequestLocationPermission(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit,
    onPermissionsRevoked: () -> Unit
) {
    val permissionState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
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


@SuppressLint("MissingPermission")
fun getLastUserLocation(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient,
    onGetLastLocationSuccess: (LatLng) -> Unit,
    onGetLastLocationFailed: (Exception) -> Unit
) {
    if (areLocationPermissionsGranted(context)) {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                onGetLastLocationSuccess(LatLng(it.latitude, it.longitude))
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
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED)
}

@Composable
fun RequestLocationPermissionBinary(callback: (Boolean) -> Unit) {
    RequestLocationPermission(
        onPermissionGranted = { callback(true) },
        onPermissionDenied = { callback(false) },
        onPermissionsRevoked = { callback(false) }
    )
}

fun getLocation(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient,
    onGetLocationSuccess: (LatLng) -> Unit,
    onGetLocationFailed: (Exception) -> Unit,
) {
    getCurrentLocation(
        context = context,
        fusedLocationClient = fusedLocationClient,
        onGetCurrentLocationSuccess = onGetLocationSuccess,
        onGetCurrentLocationFailed = {
            getLastUserLocation(
                context = context,
                fusedLocationClient = fusedLocationClient,
                onGetLastLocationSuccess = onGetLocationSuccess,
                onGetLastLocationFailed = onGetLocationFailed
            )
        }
    )
}