package com.example.craftnook.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.craftnook.ui.screen.InventoryScreen
import com.example.craftnook.ui.screen.JournalScreen
import com.example.craftnook.ui.screen.StatsScreen
import com.example.craftnook.ui.theme.OnBackgroundLight
import com.example.craftnook.ui.theme.PrimaryContainerLight
import com.example.craftnook.ui.theme.PrimaryLight
import com.example.craftnook.ui.viewmodel.InventoryViewModel

/** All navigation routes in the Craft Nook application. */
sealed class CraftNookRoute(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Inventory : CraftNookRoute(
        route          = "inventory",
        label          = "Inventory",
        selectedIcon   = Icons.Filled.Inventory2,
        unselectedIcon = Icons.Outlined.Inventory2
    )
    data object Stats : CraftNookRoute(
        route          = "stats",
        label          = "Stats",
        selectedIcon   = Icons.Filled.BarChart,
        unselectedIcon = Icons.Outlined.BarChart
    )
    data object Journal : CraftNookRoute(
        route          = "journal",
        label          = "Journal",
        selectedIcon   = Icons.Filled.MenuBook,
        unselectedIcon = Icons.Outlined.MenuBook
    )
}

private val bottomNavItems = listOf(
    CraftNookRoute.Inventory,
    CraftNookRoute.Stats,
    CraftNookRoute.Journal
)

/**
 * Main navigation graph with a three-tab bottom navigation bar:
 * Inventory · Stats · Journal (Usage Journal).
 */
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    viewModel: InventoryViewModel
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = PrimaryContainerLight) {
                bottomNavItems.forEach { item ->
                    val selected =
                        currentDestination?.hierarchy?.any { it.route == item.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick  = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState    = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector        = if (selected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label
                            )
                        },
                        label  = { Text(item.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor   = PrimaryLight,
                            selectedTextColor   = PrimaryLight,
                            unselectedIconColor = OnBackgroundLight.copy(alpha = 0.55f),
                            unselectedTextColor = OnBackgroundLight.copy(alpha = 0.55f),
                            indicatorColor      = PrimaryLight.copy(alpha = 0.18f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController    = navController,
            startDestination = CraftNookRoute.Inventory.route,
            modifier         = Modifier.padding(innerPadding)
        ) {
            composable(CraftNookRoute.Inventory.route) {
                InventoryScreen(viewModel = viewModel)
            }
            composable(CraftNookRoute.Stats.route) {
                StatsScreen(viewModel = viewModel)
            }
            composable(CraftNookRoute.Journal.route) {
                JournalScreen(viewModel = viewModel)
            }
        }
    }
}
