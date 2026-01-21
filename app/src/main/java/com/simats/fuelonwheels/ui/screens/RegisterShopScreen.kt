package com.simats.fuelonwheels.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
import com.simats.fuelonwheels.models.RegisterShopRequest
import com.simats.fuelonwheels.network.RetrofitClient
import com.simats.fuelonwheels.ApiRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun RegisterShopScreen(
    token: String,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    var shopName by remember { mutableStateOf("") }
    var radius by remember { mutableStateOf(10f) } // KM
    var latLng by remember { mutableStateOf<LatLng?>(null) }
    var loading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val location = fusedClient.lastLocation.await()
        location?.let {
            latLng = LatLng(it.latitude, it.longitude)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Register Mechanic Shop") },
                navigationIcon = {
                    Icon(
                        Icons.Default.Build,
                        contentDescription = null,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {

            Text(
                text = "Shop Details",
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = shopName,
                onValueChange = { shopName = it },
                label = { Text("Shop Name") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Service Radius: ${radius.toInt()} km",
                style = MaterialTheme.typography.titleMedium
            )

            Slider(
                value = radius,
                onValueChange = { radius = it },
                valueRange = 1f..50f,
                steps = 48
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "Select Shop Location",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.height(8.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {
                latLng?.let { location ->

                    val cameraState = rememberCameraPositionState {
                        position = CameraPosition.fromLatLngZoom(location, 14f)
                    }

                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraState,
                        onMapClick = { latLng = it }
                    ) {

                        Marker(
                            state = MarkerState(position = location),
                            title = "Mechanic Shop"
                        )

                        // 🔵 SERVICE RADIUS CIRCLE
                        Circle(
                            center = location,
                            radius = (radius * 1000).toDouble(), // km → meters
                            strokeColor = MaterialTheme.colorScheme.primary,
                            fillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            strokeWidth = 4f
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    latLng?.let {
                        scope.launch {
                            loading = true
                            ApiRepository(RetrofitClient.apiService)
                                .registerShop(
                                    token,
                                    RegisterShopRequest(
                                        name = shopName,
                                        latitude = it.latitude,
                                        longitude = it.longitude,
                                        radius = radius.toInt()
                                    )
                                )
                            loading = false
                            onSuccess()
                        }
                    }
                },
                enabled = shopName.isNotBlank() && !loading,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Text("Register Shop")
                }
            }
        }
    }
}

@Preview(showBackground = true , showSystemUi = true)
@Composable
fun RegisterShopScreenPreview() {
    RegisterShopScreen(token = "your_token_here", onSuccess = {})
}