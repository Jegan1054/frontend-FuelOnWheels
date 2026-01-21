package com.simats.fuelonwheels.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.simats.fuelonwheels.models.*
import com.simats.fuelonwheels.network.RetrofitClient
import com.simats.fuelonwheels.repository.ApiRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MechanicServiceManagementScreen(
    token: String,
    onNavigateBack: () -> Unit
) {
    val repository = remember { ApiRepository(RetrofitClient.apiService) }
    val scope = rememberCoroutineScope()

    var showAddDialog by remember { mutableStateOf(false) }
    var serviceName by remember { mutableStateOf("") }
    var servicePrice by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    var services by remember { mutableStateOf<List<ServiceItem>>(emptyList()) }

    LaunchedEffect(Unit) {
        try {
            val response = repository.getServices(token)
            if (response.isSuccessful) {
                services = response.body()?.services ?: emptyList()
            } else {
                errorMessage = "Failed to load services"
            }
        } catch (e: Exception) {
            errorMessage = "Error: ${e.message}"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Services") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Service")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            if (errorMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = errorMessage!!,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                errorMessage = null
            }

            if (successMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = successMessage!!,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LaunchedEffect(Unit) {
                    try {
                        val response = repository.getServices(token)
                        if (response.isSuccessful) {
                            services = response.body()?.services ?: emptyList()
                        }
                    } catch (e: Exception) {
                        // Ignore reload error
                    }
                    successMessage = null
                }
            }

            Text(
                text = "Add services you offer with their prices",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(services) { service ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(service.name, style = MaterialTheme.typography.titleMedium)
                            // Safely format price string
                            val displayPrice = try {
                                val num = service.price.toDouble()
                                String.format("%.2f", num)
                            } catch (e: Exception) {
                                service.price
                            }
                            Text("₹$displayPrice", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }

        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Add Service") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = serviceName,
                            onValueChange = { serviceName = it },
                            label = { Text("Service Name") },
                            placeholder = { Text("e.g., Oil Change") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = servicePrice,
                            onValueChange = { servicePrice = it },
                            label = { Text("Price (₹)") },
                            placeholder = { Text("e.g., 500") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                errorMessage = null
                                successMessage = null

                                try {
                                    val price = servicePrice.toDoubleOrNull()
                                    if (serviceName.isBlank() || price == null) {
                                        errorMessage = "Please enter valid service name and price"
                                        isLoading = false
                                        return@launch
                                    }

                                    val response = repository.addService(
                                        token,
                                        AddServiceRequest(serviceName, price)
                                    )

                                    if (response.isSuccessful) {
                                        successMessage = "Service added successfully"
                                        serviceName = ""
                                        servicePrice = ""
                                        showAddDialog = false
                                    } else {
                                        errorMessage = "Failed to add service"
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "Network error: ${e.message}"
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        } else {
                            Text("Add")
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuelPriceManagementScreen(
    token: String,
    onNavigateBack: () -> Unit
) {
    val repository = remember { ApiRepository(RetrofitClient.apiService) }
    val scope = rememberCoroutineScope()

    var showAddDialog by remember { mutableStateOf(false) }
    var fuelName by remember { mutableStateOf("") }
    var fuelPrice by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    var fuelPrices by remember { mutableStateOf<List<FuelItem>>(emptyList()) }

    LaunchedEffect(Unit) {
        try {
            val response = repository.getFuelPrices(token)
            if (response.isSuccessful) {
                fuelPrices = response.body()?.services ?: emptyList()
            } else {
                errorMessage = "Failed to load fuel prices"
            }
        } catch (e: Exception) {
            errorMessage = "Error: ${e.message}"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Fuel Prices") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Fuel Type")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            if (errorMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = errorMessage!!,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                errorMessage = null
            }

            if (successMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = successMessage!!,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LaunchedEffect(Unit) {
                    try {
                        val response = repository.getFuelPrices(token)
                        if (response.isSuccessful) {
                            fuelPrices = response.body()?.services ?: emptyList()
                        }
                    } catch (e: Exception) {
                        // Ignore
                    }
                    successMessage = null
                }
            }

            Text(
                text = "Add fuel types and prices per liter",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(fuelPrices) { fuel ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(fuel.name, style = MaterialTheme.typography.titleMedium)
                            val displayPrice = try {
                                val num = fuel.price.toDouble()
                                String.format("%.2f", num)
                            } catch (e: Exception) {
                                fuel.price
                            }
                            Text("₹$displayPrice/L", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }

        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Add Fuel Type") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = fuelName,
                            onValueChange = { fuelName = it },
                            label = { Text("Fuel Type") },
                            placeholder = { Text("e.g., Petrol") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = fuelPrice,
                            onValueChange = { fuelPrice = it },
                            label = { Text("Price per Liter (₹)") },
                            placeholder = { Text("e.g., 100") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                errorMessage = null
                                successMessage = null

                                try {
                                    val price = fuelPrice.toDoubleOrNull()
                                    if (fuelName.isBlank() || price == null) {
                                        errorMessage = "Please enter valid fuel type and price"
                                        isLoading = false
                                        return@launch
                                    }

                                    val response = repository.addFuelPrice(
                                        token,
                                        AddFuelPriceRequest(fuelName, price)
                                    )

                                    if (response.isSuccessful) {
                                        successMessage = "Fuel type added successfully"
                                        fuelName = ""
                                        fuelPrice = ""
                                        showAddDialog = false
                                    } else {
                                        errorMessage = "Failed to add fuel type"
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "Network error: ${e.message}"
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        } else {
                            Text("Add")
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

// --- PREVIEWS ---
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun FuelPriceManagementScreenPreview() {
    FuelPriceManagementScreen(token = "dummy_token", onNavigateBack = {})
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ManageServicesScreenPreview() {
    MechanicServiceManagementScreen(token = "dummy_token", onNavigateBack = {})
}