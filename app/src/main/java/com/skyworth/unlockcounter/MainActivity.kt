package com.skyworth.unlockcounter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.skyworth.unlockcounter.ui.home.HomeScreen
import com.skyworth.unlockcounter.ui.home.HomeViewModel
import com.skyworth.unlockcounter.ui.navigation.Routes
import com.skyworth.unlockcounter.ui.onboarding.OnboardingScreen
import com.skyworth.unlockcounter.ui.permission.PermissionScreen
import com.skyworth.unlockcounter.ui.splash.SplashScreen
import com.skyworth.unlockcounter.ui.theme.UnlockCounterTheme
import com.skyworth.unlockcounter.ui.users.CloudUserDetailScreen
import com.skyworth.unlockcounter.ui.users.CloudUserDetailViewModel
import com.skyworth.unlockcounter.ui.users.UserDetailScreen
import com.skyworth.unlockcounter.ui.users.UserDetailViewModel
import com.skyworth.unlockcounter.ui.users.UsersScreen
import com.skyworth.unlockcounter.ui.users.UsersViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as UnlockApp

        setContent {
            UnlockCounterTheme {
                UnlockCounterNav(app = app)
            }
        }
    }
}

@Composable
private fun UnlockCounterNav(app: UnlockApp) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(
                onNavigateToPermission = {
                    navController.navigate(Routes.PERMISSION) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToOnboarding = {
                    navController.navigate(Routes.ONBOARDING) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.PERMISSION) {
            PermissionScreen(
                onPermissionGranted = {
                    navController.navigate(Routes.ONBOARDING) {
                        popUpTo(Routes.PERMISSION) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onComplete = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            val viewModel: HomeViewModel = viewModel(
                factory = HomeViewModel.Factory(app.repository, app.userRepository)
            )
            HomeScreen(
                viewModel = viewModel,
                onOpenUsers = { navController.navigate(Routes.USERS) }
            )
        }

        composable(Routes.USERS) {
            val viewModel: UsersViewModel = viewModel(
                factory = UsersViewModel.Factory(app.userRepository, app.repository)
            )
            UsersScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onAddUser = { navController.navigate(Routes.ONBOARDING) },
                onCloudUserClick = { cloudUserId ->
                    navController.navigate(Routes.cloudUserDetail(cloudUserId))
                },
                onLocalUserClick = { userId ->
                    navController.navigate(Routes.userDetail(userId))
                }
            )
        }

        composable(
            route = Routes.USER_DETAIL,
            arguments = listOf(navArgument("userId") { type = NavType.LongType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getLong("userId") ?: return@composable
            val viewModel: UserDetailViewModel = viewModel(
                factory = UserDetailViewModel.Factory(userId, app.userRepository, app.repository)
            )
            UserDetailScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }

        composable(
            route = Routes.CLOUD_USER_DETAIL,
            arguments = listOf(navArgument("cloudUserId") { type = NavType.StringType })
        ) { backStackEntry ->
            val cloudUserId = backStackEntry.arguments?.getString("cloudUserId") ?: return@composable
            val viewModel: CloudUserDetailViewModel = viewModel(
                factory = CloudUserDetailViewModel.Factory(cloudUserId, app.cloudSync)
            )
            CloudUserDetailScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
    }
}
