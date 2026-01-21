package com.simats.fuelonwheels

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.simats.fuelonwheels.models.NearbyShop
import com.simats.fuelonwheels.ui.screens.*
import com.simats.fuelonwheels.ui.theme.FuelOnWheelsTheme
import com.simats.fuelonwheels.utils.PreferencesManager
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val preferencesManager = PreferencesManager(this)

        setContent {
FuelOnWheelsTheme {                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(preferencesManager)
                }
            }
        }
    }
}

@Composable
fun AppNavigation(preferencesManager: PreferencesManager) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    val token by preferencesManager.tokenFlow.collectAsState(initial = null)
    val userRole by preferencesManager.userRoleFlow.collectAsState(initial = null)

    val startDestination = "splash"

    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable("splash") {
            SplashScreen(
                onNavigateToNext = {
                    if (token != null) {
                        navController.navigate("home") {
                            popUpTo("splash") { inclusive = true }
                        }
                    } else {
                        navController.navigate("login") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                }
            )
        }


        // Authentication
        composable("login") {
            LoginScreen(
                onLoginSuccess = { authToken, role ->
                    scope.launch {
                        preferencesManager.saveToken(authToken)
                        preferencesManager.saveUserRole(role)
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate("register")
                },
                onNavigateToForgotPassword = {
                    navController.navigate("forgot_password")
                }
            )
        }
        composable("register") {
            RegisterScreen(
                onNavigateToVerify = { email -> navController.navigate("verify_otp/$email") },
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "verify_otp/{email}",
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            VerifyOtpScreen(
                email = email,
                onVerifySuccess = { authToken, role ->
                    scope.launch {
                        preferencesManager.saveUserSession(authToken, role, email)
                        when (role) {
                            "user" -> navController.navigate("user_location")
                            "mechanic" -> navController.navigate("register_shop")
                            "owner" -> navController.navigate("register_bunk")
                        }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("forgot_password") {
            ForgotPasswordScreen(
                onNavigateBack = { navController.popBackStack() },
                onPasswordReset = {
                    navController.navigate("login") {
                        popUpTo("forgot_password") { inclusive = true }
                    }
                }
            )
        }
        composable("user_location") {
            UserLocationScreen(
                token = token!!,
                onContinue = {
                    navController.navigate("user_image_upload")
                }
            )
        }

        composable("user_image_upload") {
            UserImageUploadScreen(
                token = token!!,
                onUploadSuccess = {
                    navController.navigate("home") {
                        popUpTo("user_image_upload") { inclusive = true }
                    }
                }
            )
        }


        composable("register_shop") {
            RegisterShopScreen(
                token = token!!,
                onSuccess = {
                    navController.navigate("home") {
                        popUpTo("register_shop") { inclusive = true }
                    }
                }
            )
        }

        composable("register_bunk") {
            RegisterBunkScreen(
                token = token!!,
                onSuccess = {
                    navController.navigate("home") {
                        popUpTo("register_bunk") { inclusive = true }
                    }
                }
            )
        }

        // Home
        composable("home") {
            token?.let { authToken ->
                when (userRole) {
                    "user" -> UserHomeScreen(
                        token = authToken,
                        preferencesManager = preferencesManager,
                        navController = navController
                    )
                    "mechanic" -> navController.navigate("mechanic_dashboard")
                    "owner" -> navController.navigate("owner_dashboard")
                }
            }
        }

        // Mechanic Routes


        // Mechanic Routes
        composable("mechanic_dashboard") {
            token?.let {
                MechanicDashboardScreen(
                    token = it,
                    navController = navController,
                    preferencesManager = preferencesManager
                )
            }
        }

// Owner Routes
        composable("owner_dashboard") {
            token?.let {
                FuelDashboardScreen(  // ✅ This matches your function name
                    token = it,
                    navController = navController,
                    preferencesManager = preferencesManager
                )
            }
        }



        // User Routes
        composable("nearby_shops") {
            token?.let {
                NearbyShopsScreen(
                    token = it,
                    onNavigateBack = { navController.popBackStack() },
                    onShopClick = { shop ->

                        navController.currentBackStackEntry
                            ?.savedStateHandle
                            ?.set("shop", shop)

                        navController.navigate("shop_details")
                    }
                )
            }
        }


        composable("shop_details") {

            val shop =
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.get<NearbyShop>("shop")


            if (shop != null && token != null) {
                ShopDetailsScreen(
                    token = token!!,
                    shop = shop,
                    onNavigateBack = { navController.popBackStack() },
                    onRequestCreated = {
                        navController.navigate("order_history") {
                            popUpTo("shop_details") { inclusive = true }
                        }
                    }
                )
            }
        }



        composable("order_history") {
            token?.let {
                OrderHistoryScreen(
                    token = it,
                    navController = navController
                )
            }
        }

        // In MainActivity.kt inside NavHost
        composable("order_details/{orderId}") { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId")?.toIntOrNull() ?: 0
            if (orderId > 0 && token != null) {
                OrderDetailScreen(
                    orderId = orderId,
                    token = token!!,
                    onNavigateBack = { navController.popBackStack() }
                )
            } else {
                navController.popBackStack()
            }
        }

        composable("profile") {
            token?.let {
                ProfileScreen(
                    token = it,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }


        composable("manage_services") {
            token?.let {
                MechanicServiceManagementScreen(
                    token = it,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        composable("manage_requests") {
            token?.let {
                RequestManagementScreen(
                    token = it,
                    userRole = userRole,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        composable("chat"){
            AiAssistantScreen()
        }

        composable("manage_fuel_prices") {
            token?.let {
                FuelPriceManagementScreen(
                    token = it,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
