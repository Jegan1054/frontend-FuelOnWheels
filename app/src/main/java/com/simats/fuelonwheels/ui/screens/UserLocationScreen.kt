package com.simats.fuelonwheels.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
import com.simats.fuelonwheels.network.RetrofitClient
import com.simats.fuelonwheels.ApiRepository
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
@Composable
fun UserLocationScreen(
    token: String,
    onContinue: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    var hasLocationPermission by remember { mutableStateOf(false) }
    var currentLatLng by remember { mutableStateOf<LatLng?>(null) }

    /* ---------------- Permission Launcher ---------------- */
    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            hasLocationPermission = granted
        }

    /* ---------------- Permission Check ---------------- */
    LaunchedEffect(Unit) {
        hasLocationPermission =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        if (!hasLocationPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    /* ---------------- Live Location Updates ---------------- */
    DisposableEffect(hasLocationPermission) {
        if (!hasLocationPermission) return@DisposableEffect onDispose {}

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            3000L
        ).setMinUpdateDistanceMeters(5f).build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                currentLatLng = LatLng(location.latitude, location.longitude)
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            context.mainLooper
        )

        onDispose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    /* ---------------- UI ---------------- */
    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        Text(
            text = "Select Your Location",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(16.dp)
        )

        if (currentLatLng != null) {

            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(
                    currentLatLng!!,
                    17f
                )
            }

            GoogleMap(
                modifier = Modifier.weight(1f),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    isMyLocationEnabled = true
                ),
                uiSettings = MapUiSettings(
                    myLocationButtonEnabled = true,
                    zoomControlsEnabled = false
                ),
                onMapClick = { latLng ->
                    currentLatLng = latLng
                }
            ) {
                Marker(
                    state = MarkerState(position = currentLatLng!!),
                    title = "Your Location"
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            enabled = currentLatLng != null,
            onClick = {
                currentLatLng?.let {
                    scope.launch {
                        ApiRepository(RetrofitClient.apiService)
                            .updateLocation(
                                token = token,
                                latitude = it.latitude,
                                longitude = it.longitude
                            )
                        onContinue()
                    }
                }
            }
        ) {
            Text("Continue")
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun UserLocationScreenPreview() {
    UserLocationScreen(
        token = "dummy_token",
        onContinue = {}
    )
}
