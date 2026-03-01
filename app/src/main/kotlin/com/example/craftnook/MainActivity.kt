package com.example.craftnook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.craftnook.ui.navigation.AppNavigation
import com.example.craftnook.ui.theme.BackgroundLight
import com.example.craftnook.ui.theme.CraftNookTheme
import com.example.craftnook.ui.theme.PrimaryLight
import com.example.craftnook.ui.viewmodel.InventoryViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

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
                Surface(modifier = Modifier.fillMaxSize()) {
                    val viewModel: InventoryViewModel = hiltViewModel()
                    AppEntryAnimation(viewModel = viewModel)
                }
            }
        }
    }
}

/**
 * Manages the app entry experience:
 *  1. A splash screen (Leaf logo + app name) fades in over 600 ms, holds for 800 ms,
 *     then fades out over 400 ms.
 *  2. The main navigation content fades in over 500 ms as the splash exits.
 */
@Composable
private fun AppEntryAnimation(viewModel: InventoryViewModel) {
    // Track whether the splash is still showing
    var splashVisible by remember { mutableStateOf(true) }

    val splashAlpha = remember { Animatable(0f) }
    val contentAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Fade splash in
        splashAlpha.animateTo(1f, animationSpec = tween(durationMillis = 600))
        // Hold
        delay(800)
        // Fade splash out while fading content in simultaneously
        splashAlpha.animateTo(0f, animationSpec = tween(durationMillis = 400))
        splashVisible = false
        contentAlpha.animateTo(1f, animationSpec = tween(durationMillis = 500))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Main content — always composed, alpha controls visibility
        Box(modifier = Modifier.alpha(contentAlpha.value)) {
            AppNavigation(viewModel = viewModel)
        }

        // Splash overlay — sits on top until dismissed
        if (splashVisible) {
            SplashOverlay(alpha = splashAlpha.value)
        }
    }
}

@Composable
private fun SplashOverlay(alpha: Float) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(alpha)
            .background(BackgroundLight),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector        = Icons.Filled.Eco,
                contentDescription = "Craft Nook logo",
                tint               = PrimaryLight,
                modifier           = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text       = "Craft Nook",
                fontSize   = 28.sp,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.onBackground,
                letterSpacing = 1.sp
            )
        }
    }
}
