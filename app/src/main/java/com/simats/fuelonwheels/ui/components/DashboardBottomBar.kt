package com.simats.fuelonwheels.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun DashboardBottomBar(
    navController: NavController,
    role: String
) {
    NavigationBar {
        NavigationBarItem(
            selected = false,
            onClick = {
                navController.navigate(
                    if (role == "mechanic") "mechanic_dashboard" else "owner_dashboard"
                )
            },
            icon = { Icon(Icons.Default.Dashboard, null) },
            label = { Text("Dashboard") }
        )

        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("manage_requests") },
            icon = { Icon(Icons.Default.List, null) },
            label = { Text("Orders") }
        )

        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("profile") },
            icon = { Icon(Icons.Default.Person, null) },
            label = { Text("Profile") }
        )
    }
}
