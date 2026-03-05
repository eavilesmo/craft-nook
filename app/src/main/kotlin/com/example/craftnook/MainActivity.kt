package com.example.craftnook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.craftnook.ui.navigation.AppNavigation
import com.example.craftnook.ui.theme.BackgroundLight
import com.example.craftnook.ui.theme.CraftNookTheme
import com.example.craftnook.ui.viewmodel.InventoryViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

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

@Composable
private fun AppEntryAnimation(viewModel: InventoryViewModel) {
    var splashVisible by remember { mutableStateOf(true) }

    val splashAlpha = remember { Animatable(0f) }
    val contentAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        splashAlpha.animateTo(1f, animationSpec = tween(durationMillis = 600))
        delay(800)
        splashAlpha.animateTo(0f, animationSpec = tween(durationMillis = 400))
        splashVisible = false
        contentAlpha.animateTo(1f, animationSpec = tween(durationMillis = 500))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.alpha(contentAlpha.value)) {
            AppNavigation(viewModel = viewModel)
        }

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
            Image(
                painter            = painterResource(R.drawable.app_logo),
                contentDescription = "Craft Nook logo",
                modifier           = Modifier.size(160.dp)
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
