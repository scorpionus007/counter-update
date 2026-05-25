package com.skyworth.unlockcounter.ui.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.skyworth.unlockcounter.UnlockApp
import com.skyworth.unlockcounter.util.UsageAccessChecker
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToPermission: () -> Unit,
    onNavigateToOnboarding: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as UnlockApp

    LaunchedEffect(Unit) {
        delay(400)
        if (!UsageAccessChecker.isGranted(context)) {
            onNavigateToPermission()
            return@LaunchedEffect
        }
        if (!app.userRepository.hasUsers() || app.userRepository.getActiveUserId() == null) {
            onNavigateToOnboarding()
        } else {
            onNavigateToHome()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}
