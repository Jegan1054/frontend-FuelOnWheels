package com.simats.fuelonwheels.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.simats.fuelonwheels.network.RetrofitClient
import com.simats.fuelonwheels.utils.PreferencesManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuelDashboardScreen(
    token: String,
    navController: NavController,
    preferencesManager: PreferencesManager
) {
    /* ---------------- COLORS ---------------- */
    val bgColor = Color(0xFF0B1C2D)
    val cardColor = Color(0xFF102A43)

    /* ---------------- STATE ---------------- */
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    var totalOrders by remember { mutableStateOf(0) }
    var pendingOrders by remember { mutableStateOf(0) }
    var totalLiters by remember { mutableStateOf(0.0) }
    var rating by remember { mutableStateOf(0.0) }
    var earnings by remember { mutableStateOf(0.0) }

    val scope = rememberCoroutineScope()

    /* ---------------- API CALL ---------------- */
    LaunchedEffect(Unit) {
        try {
            val response =
                RetrofitClient.apiService.getOwnerDashboard("Bearer $token")

            if (response.isSuccessful && response.body() != null) {
                response.body()!!.dashboard?.let { d ->
                    totalOrders = d.totalOrders
                    pendingOrders = d.pendingOrders
                    totalLiters = d.totalLitersDelivered
                    rating = d.rating
                    earnings = d.totalEarnings
                }
            } else {
                error = "Failed to load dashboard"
            }
        } catch (e: Exception) {
            error = e.message ?: "Network error"
        } finally {
            isLoading = false
        }
    }

    /* ---------------- UI ---------------- */
    Scaffold(
        containerColor = bgColor,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Fuel Partner", color = Color(0xFFFF9800))
                        Text(
                            "Fuel Dashboard",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                preferencesManager.clearAll()
                                navController.navigate("login") {
                                    popUpTo(0)
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Default.Logout, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = bgColor
                )

            )
        },
        /* ---------------- BOTTOM NAV ---------------- */
        bottomBar = {
            NavigationBar(containerColor = cardColor) {
                NavigationBarItem(
                    selected = true,
                    onClick = {},
                    icon = { Icon(Icons.Default.Dashboard, null) },
                    label = { Text("Dashboard") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("manage_fuel_prices",) },
                    icon = { Icon(Icons.Default.MiscellaneousServices, null) },
                    label = { Text("Service") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("manage_requests",) },
                    icon = { Icon(Icons.Default.List, null) },
                    label = { Text("Orders") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate("profile") },
                    icon = { Icon(Icons.Default.Settings, null) },
                    label = { Text("Settings") }
                )
            }
        }

    ) { padding ->

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        } else if (error != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(error!!, color = MaterialTheme.colorScheme.error)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                /* -------- STAT CARDS -------- */
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        DashboardStatCard(
                            title = "Orders",
                            value = totalOrders.toString(),
                            icon = Icons.Default.Inventory,
                            modifier = Modifier.weight(1f)
                        )
                        DashboardStatCard(
                            title = "Rating",
                            value = rating.toString(),
                            icon = Icons.Default.Star,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        DashboardStatCard(
                            title = "Fuel Delivered",
                            value = "${totalLiters}L",
                            icon = Icons.Default.LocalGasStation,
                            modifier = Modifier.weight(1f)
                        )
                        DashboardStatCard(
                            title = "Pending",
                            value = pendingOrders.toString(),
                            icon = Icons.Default.Schedule,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                /* -------- TOTAL EARNINGS -------- */
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = cardColor),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.AccountBalanceWallet,
                                null,
                                tint = Color(0xFFFF9800),
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Total Earnings", color = Color.LightGray)
                                Text(
                                    "₹${"%.2f".format(earnings)}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.headlineSmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/* ---------------- COMPONENT ---------------- */

@Composable
private fun DashboardStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF102A43)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, null, tint = Color(0xFF1E88E5))
            Text(title, color = Color.LightGray)
            Text(
                value,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}


@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF102A43)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, null, tint = Color(0xFF1E88E5))
            Text(title, color = Color.LightGray)
            Text(
                value,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}

@Composable
private fun AmountCard(amount: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF102A43)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.AccountBalanceWallet,
                null,
                tint = Color(0xFFFF9800),
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Total Amount Received", color = Color.LightGray)
                Text(
                    amount,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun OwnerDashboardScreenPreview() {
    FuelDashboardScreen(
        token = "",
        navController = NavController(LocalContext.current),
        preferencesManager = PreferencesManager(LocalContext.current)
    )
}