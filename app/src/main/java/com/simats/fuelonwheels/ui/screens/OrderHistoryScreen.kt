package com.simats.fuelonwheels.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.simats.fuelonwheels.models.Order
import com.simats.fuelonwheels.network.RetrofitClient
import com.simats.fuelonwheels.ApiRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryScreen(
    token: String,
    navController: NavController
) {
    val repository = remember { ApiRepository(RetrofitClient.apiService) }
    val scope = rememberCoroutineScope()

    var orders by remember { mutableStateOf<List<Order>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var currentPage by remember { mutableStateOf(1) }
    var totalPages by remember { mutableStateOf(1) }
    var activeTab by remember { mutableStateOf("all") }

    fun loadOrders(page: Int, serviceType: String? = null) {
        if (isLoading) return
        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = repository.getOrderHistory(token, page, 10)
                if (response.isSuccessful) {
                    val body = response.body()
                    val allOrders = body?.orders ?: emptyList()

                    orders = when (serviceType) {
                        "fuel" -> allOrders.filter { it.shopType == "fuel" }
                        "mechanic" -> allOrders.filter { it.shopType == "mechanic" }
                        else -> allOrders
                    }

                    totalPages = body?.pagination?.pages ?: 1
                    currentPage = page
                } else {
                    errorMessage = "Server error (${response.code()})"
                }
            } catch (e: Exception) {
                errorMessage = "Network error: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(activeTab) {
        loadOrders(1, if (activeTab == "all") null else activeTab)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order History") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, "Home") },
                    label = { Text("Home") },
                    selected = false,
                    onClick = { navController.navigate("home") }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.History, "Orders") },
                    label = { Text("Orders") },
                    selected = true,
                    onClick = {}
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, "Profile") },
                    label = { Text("Me") },
                    selected = false,
                    onClick = { navController.navigate("profile") }
                )
            }
        }
    ) { padding ->

        when {
            isLoading && orders.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            errorMessage != null && orders.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            orders.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No order history yet")
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    // ---------- TAB HEADER ----------
                    item {
                        TabRow(
                            selectedTabIndex = when (activeTab) {
                                "fuel" -> 1
                                "mechanic" -> 2
                                else -> 0
                            }
                        ) {
                            Tab(
                                selected = activeTab == "all",
                                onClick = { activeTab = "all" },
                                text = { Text("All") }
                            )
                            Tab(
                                selected = activeTab == "fuel",
                                onClick = { activeTab = "fuel" },
                                text = { Text("Fuel") }
                            )
                            Tab(
                                selected = activeTab == "mechanic",
                                onClick = { activeTab = "mechanic" },
                                text = { Text("Mechanic") }
                            )
                        }
                    }

                    // ---------- ORDER LIST ----------
                    items(orders) { order ->
                        OrderCard(order = order) {
                            navController.navigate("order_details/${order.id}")
                        }
                    }

                    // ---------- PAGINATION ----------
                    if (totalPages > 1) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = {
                                        if (currentPage > 1) {
                                            loadOrders(
                                                currentPage - 1,
                                                if (activeTab == "all") null else activeTab
                                            )
                                        }
                                    },
                                    enabled = currentPage > 1 && !isLoading
                                ) {
                                    Text("Previous")
                                }

                                Text("Page $currentPage / $totalPages")

                                TextButton(
                                    onClick = {
                                        if (currentPage < totalPages) {
                                            loadOrders(
                                                currentPage + 1,
                                                if (activeTab == "all") null else activeTab
                                            )
                                        }
                                    },
                                    enabled = currentPage < totalPages && !isLoading
                                ) {
                                    Text("Next")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------- ORDER CARD ----------------

@Composable
fun OrderCard(order: Order, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val icon = when (order.shopType) {
                "fuel" -> Icons.Default.LocalGasStation
                "mechanic" -> Icons.Default.Build
                else -> Icons.Default.ShoppingCart
            }

            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(order.serviceName, fontWeight = FontWeight.SemiBold)
                Text(order.shopName, style = MaterialTheme.typography.bodySmall)
                Text(order.requestedAt, style = MaterialTheme.typography.bodySmall)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "₹${order.finalAmount ?: order.servicePrice}",
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                StatusChip(order.status)
            }
        }
    }
}

@Composable
fun StatusChip(status: String) {
    val color = when (status.lowercase()) {
        "pending" -> MaterialTheme.colorScheme.tertiary
        "accepted", "paid" -> MaterialTheme.colorScheme.primary
        "completed" -> MaterialTheme.colorScheme.secondary
        "rejected", "cancelled" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.outline
    }

    Surface(
        color = color.copy(alpha = 0.2f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            status.uppercase(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun OrderHistoryPreview() {
    OrderHistoryScreen("", rememberNavController())
}
