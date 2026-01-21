package com.simats.fuelonwheels.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.simats.fuelonwheels.R
import com.simats.fuelonwheels.ui.theme.FuelOnWheelsTheme
import kotlinx.coroutines.delay
import kotlin.math.min

@Composable
fun SplashScreen(
    onNavigateToNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }

    // Animation + delay + navigation
    LaunchedEffect(Unit) {
        isVisible = true
        delay(3000L)
        onNavigateToNext()
    }

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "SplashAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFF6F00))
    ) {

        // Decorative background circles
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val maxRadius = min(size.width, size.height) * 0.7f
            repeat(3) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.05f),
                    center = center,
                    radius = maxRadius * (0.9f - it * 0.1f)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .alpha(alpha),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .shadow(8.dp, CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_truck),
                    contentDescription = "FuelOnWheels Logo",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(70.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "FUELONWHEELS",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "EMERGENCY ASSISTANCE",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.9f)
            )
        }

        // ✅ ONLY ADDITION — FOOTER TEXT
        Text(
            text = "Powered by SIMATS Engineering",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.85f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SplashScreenPreview() {
    FuelOnWheelsTheme {
        SplashScreen(onNavigateToNext = {})
    }
}
