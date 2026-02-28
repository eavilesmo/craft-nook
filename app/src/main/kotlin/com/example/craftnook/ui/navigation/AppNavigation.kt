package com.example.craftnook.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.craftnook.ui.screen.InventoryScreen
import com.example.craftnook.ui.viewmodel.InventoryViewModel

/**
 * Sealed class defining all navigation routes in the Craft Nook application.
 *
 * Each route represents a distinct screen or destination in the app.
 */
sealed class CraftNookRoute(val route: String) {
    /**
     * Main inventory list screen showing all art materials and their stock levels.
     */
    data object Inventory : CraftNookRoute("inventory")
}

/**
 * Main navigation graph for the Craft Nook application.
 *
 * Manages navigation between different screens using Jetpack Navigation Compose.
 * Currently supports the inventory list screen and stats screen, with extensibility for future screens.
 *
 * @param navController The NavHostController for managing navigation between destinations.
 * @param viewModel The InventoryViewModel shared across screens.
 */
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    viewModel: InventoryViewModel
) {
    NavHost(
        navController = navController,
        startDestination = CraftNookRoute.Inventory.route
    ) {
        composable(CraftNookRoute.Inventory.route) {
            InventoryScreen(
                viewModel = viewModel
            )
        }
    }
}
