package com.simats.fuelonwheels.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun DashboardCard(
    title: String,
    value: Any,
    icon: ImageVector
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(value.toString(), style = MaterialTheme.typography.headlineSmall)
            }
            Icon(icon, null, modifier = Modifier.size(32.dp))
        }
    }
}
