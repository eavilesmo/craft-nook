package com.example.modernandroidapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.modernandroidapp.ui.navigation.AppNavigation
import com.example.modernandroidapp.ui.theme.CraftNookTheme
import com.example.modernandroidapp.ui.viewmodel.InventoryViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main activity for the Craft Nook application.
 *
 * Serves as the entry point for the app and hosts the navigation graph.
 * The activity uses Jetpack Compose for UI and is configured for Hilt dependency injection.
 *
 * ViewModels are automatically injected via Hilt and the AppNavigation composable
 * manages navigation between different screens.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CraftNookTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    // Create ViewModel with Hilt injection
                    val viewModel: InventoryViewModel = hiltViewModel()

                    // Navigation graph manages screen transitions
                    AppNavigation(viewModel = viewModel)
                }
            }
        }
    }
}
