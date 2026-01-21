package com.simats.fuelonwheels.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Color Palette
val OrangeMain = Color(0xFFFF6B00)
val OrangeDark = Color(0xFFE55A00)
val OrangeLight = Color(0xFFFF8533)
val DarkBlue = Color(0xFF1A2332)
val DarkBlueDark = Color(0xFF0F1621)
val LightGray = Color(0xFFF5F5F5)
val Gray = Color(0xFF9E9E9E)
val GreenSuccess = Color(0xFF4CAF50)
val RedError = Color(0xFFDC3545)
val YellowWarning = Color(0xFFFFC107)

private val LightColorScheme = lightColorScheme(
    primary = OrangeMain,
    onPrimary = Color.White,
    primaryContainer = OrangeLight,
    secondary = DarkBlue,
    onSecondary = Color.White,
    background = LightGray,
    surface = Color.White,
    onSurface = Color.Black,
    error = RedError,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = OrangeMain,
    onPrimary = Color.White,
    primaryContainer = OrangeDark,
    secondary = DarkBlueDark,
    onSecondary = Color.White,
    background = DarkBlue,
    surface = DarkBlueDark,
    onSurface = Color.White,
    error = RedError,
    onError = Color.White
)

val AppTypography  = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

@Composable
fun FuelOnWheelsTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography ,
        content = content
    )
}