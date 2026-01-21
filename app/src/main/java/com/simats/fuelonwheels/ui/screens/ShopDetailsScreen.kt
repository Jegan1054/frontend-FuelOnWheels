package com.simats.fuelonwheels.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.simats.fuelonwheels.models.CreateServiceRequest
import com.simats.fuelonwheels.models.NearbyShop
import com.simats.fuelonwheels.network.RetrofitClient
import com.simats.fuelonwheels.repository.ApiRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopDetailsScreen(
    token: String,
    shop: NearbyShop,
    onNavigateBack: () -> Unit,
    onRequestCreated: () -> Unit
) {
    val repository = remember { ApiRepository(RetrofitClient.apiService) }
    val scope = rememberCoroutineScope()

    // ✅ NULL-SAFE SERVICES
    val validServices = remember(shop.services) {
        shop.services
            ?.filter { it.isNotBlank() }
            ?: emptyList()
    }

    var selectedServiceIndex by remember { mutableStateOf<Int?>(null) }
    var description by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shop Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
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

            // ================= SHOP INFO =================
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(Modifier.padding(16.dp)) {

                    Text(
                        text = shop.name?.takeIf { it.isNotBlank() } ?: "Unknown Shop",
                        style = MaterialTheme.typography.headlineSmall
                    )

                    Text(
                        text = shop.serviceType
                            ?.replaceFirstChar { it.uppercase() }
                            ?: "Service",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(Modifier.height(8.dp))

                    // ✅ SAFE DISTANCE
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Place, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = shop.distance?.let {
                                String.format("%.1f km away", it)
                            } ?: "Distance unavailable"
                        )
                    }

                    // ✅ SAFE RATING
                    shop.avgRating?.let {
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Star,
                                null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(String.format("%.1f", it))
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ================= SERVICES =================
            Text("Services", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))

            if (validServices.isEmpty()) {
                Text(
                    text = "No services available",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                validServices.forEachIndexed { index, service ->
                    ServiceItemCard(
                        service = service,
                        isSelected = selectedServiceIndex == index,
                        onClick = { selectedServiceIndex = index }
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }

            Spacer(Modifier.height(24.dp))

            // ================= NOTES =================
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Additional notes (optional)") },
                minLines = 3
            )

            Spacer(Modifier.height(24.dp))

            // ================= REQUEST BUTTON =================
            Button(
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedServiceIndex != null && !loading && validServices.isNotEmpty(),
                onClick = { showDialog = true }
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Request Service")
                }
            }
        }

        // ================= CONFIRM DIALOG =================
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Confirm Request") },
                text = { Text("Are you sure you want to request this service?") },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                val shopId = shop.id ?: return@launch
                                val serviceIndex = selectedServiceIndex ?: return@launch

                                loading = true
                                try {
                                    repository.createServiceRequest(
                                        token,
                                        CreateServiceRequest(
                                            shopId = shopId,
                                            serviceId = serviceIndex + 1,
                                            description = description.takeIf { it.isNotBlank() }
                                        )
                                    )
                                    onRequestCreated()
                                } finally {
                                    loading = false
                                    showDialog = false
                                }
                            }
                        }
                    ) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun ServiceItemCard(
    service: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor =
                if (isSelected)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surface
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = service,
                style = MaterialTheme.typography.bodyLarge
            )

            Icon(
                imageVector =
                    if (isSelected)
                        Icons.Default.CheckCircle
                    else
                        Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint =
                    if (isSelected)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
