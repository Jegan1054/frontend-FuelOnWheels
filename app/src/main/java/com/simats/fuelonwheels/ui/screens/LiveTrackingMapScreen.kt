package com.simats.fuelonwheels.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveTrackingMapScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val fusedClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            hasPermission = it
        }

    var currentLatLng by remember { mutableStateOf<LatLng?>(null) }
    var isTracking by remember { mutableStateOf(true) }

    val cameraState = rememberCameraPositionState()

    /* 🔁 LOCATION UPDATE EVERY 3 SECONDS */
    LaunchedEffect(isTracking, hasPermission) {
        if (hasPermission && isTracking) {
            while (true) {
                val location = fusedClient.lastLocation.await()
                location?.let {
                    val newLatLng = LatLng(it.latitude, it.longitude)
                    currentLatLng = newLatLng

                    cameraState.position = CameraPosition.fromLatLngZoom(
                        newLatLng,
                        16f
                    )
                }
                delay(3000)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Live Location Tracking") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
                actions = {
                    IconButton(onClick = { isTracking = !isTracking }) {
                        Icon(
                            if (isTracking) Icons.Default.Stop else Icons.Default.PlayArrow,
                            null
                        )
                    }
                }
            )
        }
    ) { padding ->

        if (!hasPermission) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Location permission required")
                Spacer(Modifier.height(12.dp))
                Button(onClick = {
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }) {
                    Text("Grant Permission")
                }
            }
        } else {
            GoogleMap(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                cameraPositionState = cameraState,
                properties = MapProperties(isMyLocationEnabled = true),
                uiSettings = MapUiSettings(
                    myLocationButtonEnabled = true,
                    zoomControlsEnabled = true
                )
            ) {
                currentLatLng?.let {
                    Marker(
                        state = MarkerState(position = it),
                        title = "Live Location",
                        snippet = "Updating every 3 sec"
                    )
                }
            }
        }
    }
}
