package com.simats.fuelonwheels.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Tasks
import com.simats.fuelonwheels.network.RetrofitClient
import com.simats.fuelonwheels.ApiRepository
import com.simats.fuelonwheels.utils.PreferencesManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserHomeScreen(
    token: String,
    preferencesManager: PreferencesManager,
    navController: NavController
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val apiRepository = ApiRepository(RetrofitClient.apiService)

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted -> hasLocationPermission = isGranted }

    var currentLocationDisplay by remember { mutableStateOf("Fetching location...") }
    var latitude by remember { mutableStateOf(0.0) }
    var longitude by remember { mutableStateOf(0.0) }

    val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    // Continuously update location every 3 seconds
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            while (true) {
                delay(3000L)
                try {
                    val location = withContext(kotlinx.coroutines.Dispatchers.IO) {
                        Tasks.await(fusedLocationClient.lastLocation)
                    }
                    if (location != null) {
                        latitude = location.latitude
                        longitude = location.longitude
                        currentLocationDisplay = "Lat: ${"%.4f".format(latitude)}, Lng: ${"%.4f".format(longitude)}"

                        // ✅ Send location to server using your API
                        scope.launch {
                            try {
                                val response = apiRepository.updateLocation(token, latitude, longitude)
                                // Optional: log success/failure
                            } catch (e: Exception) {
                                // Handle error silently or show snackbar
                            }
                        }
                    } else {
                        currentLocationDisplay = "Location unavailable"
                    }
                } catch (e: Exception) {
                    currentLocationDisplay = "Error fetching location"
                }
            }
        }
    }

    // Request permission if not granted
    if (!hasLocationPermission) {
        LaunchedEffect(Unit) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FuelOnWheels") },
                actions = {
                    IconButton(onClick = { navController.navigate("profile") }) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                    IconButton(
                        onClick = {
                            scope.launch {
                                preferencesManager.clearAll()
                                navController.navigate("login") {
                                    popUpTo("home") { inclusive = true }
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, "Home") },
                    label = { Text("Home") },
                    selected = true,
                    onClick = { /* Already home */ }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.ChatBubble, "chat") },
                    label = { Text("chat") },
                    selected = false,
                    onClick = { navController.navigate("chat") }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.History, "Orders") },
                    label = { Text("Orders") },
                    selected = false,
                    onClick = { navController.navigate("order_history") }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, "Me") },
                    label = { Text("Me") },
                    selected = false,
                    onClick = { navController.navigate("profile") }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Current Location Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Current Location",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = currentLocationDisplay,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = { /* TODO: Notifications */ }) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Running Low Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFF5E6) // Light orange
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Running Low On Fuel?",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Get petrol or diesel\nGet mechanic Assistance",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = { navController.navigate("nearby_shops") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF7A00), // Orange
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Text("Order Fuel and mechanic")
                        }
                    }
                }
            }

            // Service Cards Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ServiceOptionCard(
                    icon = Icons.Default.LocalGasStation,
                    title = "Fuel Delivery",
                    subtitle = "Petrol & Diesel",
                    onClick = { navController.navigate("nearby_shops?service_type=fuel") }
                )

                ServiceOptionCard(
                    icon = Icons.Default.Build,
                    title = "Mechanic",
                    subtitle = "Breakdown Help",
                    onClick = { navController.navigate("nearby_shops?service_type=mechanic") }
                )
            }

            // Nearby Drivers Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Lens,
                            contentDescription = "Nearby",
                            tint = Color.Green
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "3 Drivers Nearby",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ServiceOptionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(160.dp)
            .height(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = title,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
@Preview(showBackground = true,showSystemUi = true)
@Composable
fun UserHomeScreenPreview() {
    UserHomeScreen(token = "", preferencesManager = PreferencesManager(LocalContext.current), navController = NavController(LocalContext.current))}