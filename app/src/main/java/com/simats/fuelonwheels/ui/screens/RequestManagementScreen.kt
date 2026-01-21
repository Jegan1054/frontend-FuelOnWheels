package com.simats.fuelonwheels.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.simats.fuelonwheels.models.*
import com.simats.fuelonwheels.network.RetrofitClient
import com.simats.fuelonwheels.repository.ApiRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLngBounds

enum class RequestFilter { ALL, PENDING, ACCEPTED, REJECTED, COMPLETED }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestManagementScreen(
    token: String,
    userRole: String?,
    onNavigateBack: () -> Unit
) {
    val repository = remember { ApiRepository(RetrofitClient.apiService) }
    val scope = rememberCoroutineScope()

    var requests by remember { mutableStateOf<List<ServiceRequestItem>>(emptyList()) }
    var selectedFilter by remember { mutableStateOf(RequestFilter.ALL) }
    var selectedRequest by remember { mutableStateOf<ServiceRequestItem?>(null) }
    var loading by remember { mutableStateOf(true) }

    fun loadRequests() {
        scope.launch {
            loading = true
            try {
                val response =
                    if (userRole == "mechanic")
                        repository.viewMechanicRequests(token)
                    else repository.viewOwnerRequests(token)

                if (response.isSuccessful)
                    requests = response.body()?.data?.requests ?: emptyList()
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) { loadRequests() }

    selectedRequest?.let {
        RequestDetailScreen(
            token = token,
            userRole = userRole,
            request = it,
            repository = repository,
            onBack = { selectedRequest = null },
            onRefresh = { loadRequests() }
        )
        return
    }

    val filtered = when (selectedFilter) {
        RequestFilter.ALL -> requests
        else -> requests.filter { it.status.equals(selectedFilter.name, true) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Service Requests", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->

        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            RequestFilterTabs(selectedFilter) { selectedFilter = it }

            when {
                loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator()
                }

                filtered.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Text(
                        "No service requests available",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filtered) { request ->
                        RequestCard(request) {
                            selectedRequest = request
                        }
                    }
                }
            }
        }
    }
}

/* ================= REQUEST CARD ================= */

@Composable
fun RequestCard(request: ServiceRequestItem, onClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = MaterialTheme.shapes.large
    ) {
        Column(Modifier.padding(16.dp)) {

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    request.serviceName ?: request.fuelType ?: "Service Request",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                StatusChip(request.status)
            }

            Spacer(Modifier.height(8.dp))

            Text(
                "Requested by ${request.user?.name ?: "--"}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

/* ================= STATUS CHIP ================= */

@Composable
fun StatusChip(status: String?) {
    val (bg, fg) = when (status?.lowercase()) {
        "pending" -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.tertiary
        "accepted" -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.primary
        "completed" -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.secondary
        "rejected" -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.outline
    }

    AssistChip(
        onClick = {},
        label = {
            Text(
                status?.uppercase() ?: "--",
                fontWeight = FontWeight.Medium
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = bg,
            labelColor = fg
        )
    )
}

/* ================= DETAIL SCREEN ================= */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestDetailScreen(
    token: String,
    userRole: String?,
    request: ServiceRequestItem,
    repository: ApiRepository,
    onBack: () -> Unit,
    onRefresh: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var tracking by remember { mutableStateOf<TrackLocationResponse?>(null) }

    // --- States for completion dialog ---
    var showCompletionDialog by remember { mutableStateOf(false) }
    var finalAmount by remember { mutableStateOf("") }
    var finalLiters by remember { mutableStateOf("") }

    // --- Live tracking ---
    LaunchedEffect(request.id) {
        while (true) {
            try {
                val response =
                    if (userRole == "mechanic")
                        repository.trackMechanicUserLocation(token, request.id)
                    else repository.trackOwnerUserLocation(token, request.id)

                if (response.isSuccessful)
                    tracking = response.body()
            } catch (_: Exception) {}
            delay(5000)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Request Details", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->

        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // --- Map ---
            item {
                ElevatedCard {
                    TrackingMap(
                        provider = tracking?.mechanicLocation ?: tracking?.deliveryLocation
                    )
                }
            }

            // --- Live Tracking Info ---
            item {
                InfoCard(
                    "Live Tracking",
                    listOf(
                        "Distance: ${(tracking?.distanceFromShop ?: tracking?.distanceFromBunk) ?: "--"} km",
                        "ETA: ${tracking?.estimatedArrivalTime ?: "--"} mins",
                        "Status: ${request.status}"
                    )
                )
            }

            // --- User Info ---
            item {
                InfoCard(
                    "User Information",
                    listOf(
                        "Name: ${request.user?.name}",
                        "Phone: ${request.user?.phone}"
                    )
                )
            }

            // --- Service Info ---
            item {
                InfoCard(
                    "Service Details",
                    listOf(
                        "Service: ${tracking?.service?.name ?: "--"}",
                        "Type: ${tracking?.service?.type ?: "--"}",
                        "Quantity: ${request.quantity ?: "--"}"
                    )
                )
            }

            // --- Action Buttons ---
            item {
                when (request.status.lowercase()) {
                    "pending" -> Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                scope.launch {
                                    if (userRole == "mechanic") {
                                        repository.acceptRejectRequest(
                                            token,
                                            AcceptRejectRequest(request.id, "reject")
                                        )
                                    } else {
                                        repository.acceptRejectFuelRequest(
                                            token,
                                            AcceptRejectRequest(request.id, "reject")
                                        )
                                    }
                                    onRefresh(); onBack()
                                }
                            }
                        ) { Text("Reject") }

                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                scope.launch {
                                    if (userRole == "mechanic") {
                                        repository.acceptRejectRequest(
                                            token,
                                            AcceptRejectRequest(request.id, "accept")
                                        )
                                    } else {
                                        repository.acceptRejectFuelRequest(
                                            token,
                                            AcceptRejectRequest(request.id, "accept")
                                        )
                                    }
                                    onRefresh(); onBack()
                                }
                            }
                        ) { Text("Accept") }
                    }

                    "accepted" -> Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { showCompletionDialog = true }
                    ) { Text("Mark as Completed") }
                }
            }
        }
    }

    // --- Completion Dialog ---
    if (showCompletionDialog) {
        AlertDialog(
            onDismissRequest = { showCompletionDialog = false },
            title = { Text("Complete Request") },
            text = {
                Column {
                    OutlinedTextField(
                        value = finalAmount,
                        onValueChange = { finalAmount = it },
                        label = { Text("Final Amount") },
                        singleLine = true,
                        placeholder = { Text("Enter final amount") }
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = finalLiters,
                        onValueChange = { finalLiters = it },
                        label = { Text("Liters") },
                        singleLine = true,
                        placeholder = { Text("Enter liters") }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val amount = finalAmount.toDoubleOrNull() ?: 0.0
                    val liters = finalLiters.toDoubleOrNull() ?: 0.0

                    scope.launch {
                        if (userRole == "mechanic") {
                            repository.completeRequest(
                                token,
                                CompleteRequestBody(request.id, amount)
                            )
                        } else {
                            repository.completeFuelRequest(
                                token,
                                CompleteFuelRequest(request.id, amount, liters)
                            )
                        }
                        onRefresh()
                        onBack()
                    }

                    showCompletionDialog = false
                }) {
                    Text("Submit")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCompletionDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/* ================= MAP ================= */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun TrackingMap(
    provider: CurrentLocation? = null
) {
    val context = LocalContext.current

    val fusedClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    var currentLatLng by remember { mutableStateOf<LatLng?>(null) }

    val cameraPositionState = rememberCameraPositionState()

    /* -------- LOCATION PERMISSION -------- */
    val locationPermissionState = rememberPermissionState(
        permission = android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    LaunchedEffect(Unit) {
        if (!locationPermissionState.status.isGranted) {
            locationPermissionState.launchPermissionRequest()
        }
    }

    /* -------- GET GPS LOCATION (SAFE) -------- */
    LaunchedEffect(locationPermissionState.status.isGranted) {
        if (locationPermissionState.status.isGranted) {
            fusedClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    currentLatLng = LatLng(it.latitude, it.longitude)
                }
            }
        }
    }

    /* -------- CAMERA UPDATE -------- */
    LaunchedEffect(currentLatLng, provider) {
        val providerLatLng = provider?.let {
            LatLng(it.latitude, it.longitude)
        }

        when {
            currentLatLng != null && providerLatLng != null -> {
                val bounds = LatLngBounds.builder()
                    .include(currentLatLng!!)
                    .include(providerLatLng)
                    .build()

                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngBounds(bounds, 120),
                    1000
                )
            }

            providerLatLng != null -> {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(providerLatLng, 15f),
                    800
                )
            }

            currentLatLng != null -> {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(currentLatLng!!, 15f),
                    800
                )
            }
        }
    }

    /* -------- MAP UI -------- */
    GoogleMap(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
        cameraPositionState = cameraPositionState,
        uiSettings = MapUiSettings(
            zoomControlsEnabled = false,
            myLocationButtonEnabled = false
        )
    ) {

        /* USER (DEVICE GPS) */
        currentLatLng?.let {
            Marker(
                state = MarkerState(it),
                title = "Your Location"
            )
        }

        /* PROVIDER */
        provider?.let {
            Marker(
                state = MarkerState(LatLng(it.latitude, it.longitude)),
                title = "Service Provider"
            )
        }
    }
}


/* ================= INFO CARD ================= */

@Composable
fun InfoCard(title: String, lines: List<String>) {
    ElevatedCard {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            lines.forEach {
                Text(it, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

/* ================= FILTER TABS ================= */

@Composable
fun RequestFilterTabs(
    selected: RequestFilter,
    onSelected: (RequestFilter) -> Unit
) {
    ScrollableTabRow(selectedTabIndex = selected.ordinal) {
        RequestFilter.values().forEach {
            Tab(
                selected = selected == it,
                onClick = { onSelected(it) },
                text = {
                    Text(
                        it.name.lowercase().replaceFirstChar { c -> c.uppercase() },
                        fontWeight = FontWeight.Medium
                    )
                }
            )
        }
    }
}
