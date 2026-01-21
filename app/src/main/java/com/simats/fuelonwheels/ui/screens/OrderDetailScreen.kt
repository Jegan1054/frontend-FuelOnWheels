package com.simats.fuelonwheels.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.simats.fuelonwheels.models.*
import com.simats.fuelonwheels.network.RetrofitClient
import com.simats.fuelonwheels.repository.ApiRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    token: String,
    orderId: Int,
    onNavigateBack: () -> Unit
) {
    val repository = remember { ApiRepository(RetrofitClient.apiService) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    /* ---------------- LOCATION ---------------- */
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

    var isTracking by remember { mutableStateOf(true) }
    var currentLatLng by remember { mutableStateOf<LatLng?>(null) }

    val cameraState = rememberCameraPositionState()

    /* 🔁 UPDATE LOCATION EVERY 3 SECONDS */
    LaunchedEffect(isTracking, hasPermission) {
        if (hasPermission && isTracking) {
            while (true) {
                try {
                    val location = fusedClient.lastLocation.await()
                    location?.let {
                        val newLatLng = LatLng(it.latitude, it.longitude)
                        currentLatLng = newLatLng
                        cameraState.position =
                            CameraPosition.fromLatLngZoom(newLatLng, 16f)
                    }
                } catch (_: Exception) {}
                delay(3000)
            }
        }
    }

    /* ---------------- ORDER ---------------- */
    var orderDetail by remember { mutableStateOf<OrderDetail?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isPaying by remember { mutableStateOf(false) }
    var isRating by remember { mutableStateOf(false) }
    var paymentMethod by remember { mutableStateOf("cod") } // default
 // default
    var ratingStars by remember { mutableStateOf(5) }
    var ratingReview by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }

    fun loadOrder() {
        scope.launch {
            isLoading = true
            val response = repository.getOrderStatus(token, orderId)
            if (response.isSuccessful) {
                orderDetail = response.body()?.order
            } else {
                message = "Failed to load order"
            }
            isLoading = false
        }
    }

    fun makePayment() {
        orderDetail?.id?.let { id ->
            scope.launch {
                isPaying = true
                val response = repository.makePayment(
                    token,
                    PaymentRequest(orderId = id, paymentMethod = paymentMethod)
                )
                if (response.isSuccessful) {
                    message = response.body()?.message ?: "Payment successful"
                    loadOrder() // refresh order
                } else {
                    message = response.body()?.error ?: "Payment failed"
                }
                isPaying = false
            }
        }
    }

    fun giveRating() {
        orderDetail?.id?.let { id ->
            scope.launch {
                isRating = true
                val response = repository.giveRating(
                    token,
                    RatingRequest(orderId = id, rating = ratingStars, review = ratingReview)
                )
                if (response.isSuccessful) {
                    message = response.body()?.message ?: "Rating submitted"
                    loadOrder()
                } else {
                    message = response.body()?.error ?: "Rating failed"
                }
                isRating = false
            }
        }
    }

    LaunchedEffect(Unit) { loadOrder() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order Tracking") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {

            /* ---------------- MAP ---------------- */
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .padding(16.dp)
            ) {
                if (!hasPermission) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Location permission required")
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = {
                            permissionLauncher.launch(
                                Manifest.permission.ACCESS_FINE_LOCATION
                            )
                        }) {
                            Text("Grant Permission")
                        }
                    }
                } else {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraState,
                        properties = MapProperties(
                            isMyLocationEnabled = true
                        ),
                        uiSettings = MapUiSettings(
                            zoomControlsEnabled = true,
                            myLocationButtonEnabled = true
                        )
                    ) {
                        currentLatLng?.let {
                            Marker(
                                state = MarkerState(it),
                                title = "Live Location",
                                snippet = "Updating every 3 seconds"
                            )
                        }
                    }
                }
            }

            /* ---------------- ORDER INFO ---------------- */
            orderDetail?.let { order ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "Order Status",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            order.status.uppercase(),
                            style = MaterialTheme.typography.titleLarge
                        )

                        Spacer(Modifier.height(12.dp))
                        Text("Shop: ${order.shop.name}")
                        Text("Service: ${order.service.name} - \$${order.service.price}")
                        order.description?.let { Text("Description: $it") }
                        order.finalAmount?.let { Text("Final Amount: $it") }
                        order.liters?.let { Text("Liters: $it") }
                        order.payment?.let { Text("Payment Status: ${it.status}") }
                        order.rating?.let { Text("Rating: ${it.stars} - ${it.review ?: ""}") }

                        Spacer(Modifier.height(12.dp))

                        // 🔹 PAYMENT BUTTON
                        if (order.status.lowercase() == "completed" && order.payment == null) {
                            Text(
                                "Payment Method",
                                style = MaterialTheme.typography.titleSmall
                            )

                            Spacer(Modifier.height(8.dp))

                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = paymentMethod == "cod",
                                        onClick = { paymentMethod = "cod" }
                                    )
                                    Text("Cash on Delivery (COD)")
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = paymentMethod == "online",
                                        onClick = { paymentMethod = "online" }
                                    )
                                    Text("Online Payment")
                                }
                            }

                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = { makePayment() },
                                enabled = !isPaying,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (isPaying) CircularProgressIndicator(modifier = Modifier.size(20.dp))
                                else Text("Pay Now")
                            }
                        }

                        // 🔹 RATING
                        if ( order.rating == null) {
                            Spacer(Modifier.height(12.dp))
                            Text("Give Rating:")
                            Row {
                                repeat(5) { i ->
                                    IconButton(onClick = { ratingStars = i + 1 }) {
                                        Icon(
                                            imageVector = if (i < ratingStars) Icons.Default.Star else Icons.Default.StarBorder,
                                            contentDescription = null
                                        )
                                    }
                                }
                            }
                            OutlinedTextField(
                                value = ratingReview,
                                onValueChange = { ratingReview = it },
                                label = { Text("Review (optional)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = { giveRating() },
                                enabled = !isRating,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (isRating) CircularProgressIndicator(modifier = Modifier.size(20.dp))
                                else Text("Submit Rating")
                            }
                        }
                    }
                }
            }

            if (isLoading) {
                Spacer(Modifier.height(24.dp))
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            message?.let {
                Spacer(Modifier.height(16.dp))
                Text(
                    it,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}
