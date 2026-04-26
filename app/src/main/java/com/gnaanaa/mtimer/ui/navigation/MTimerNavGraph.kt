package com.gnaanaa.mtimer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.gnaanaa.mtimer.ui.home.HomeScreen
import com.gnaanaa.mtimer.ui.history.SessionHistoryScreen
import com.gnaanaa.mtimer.ui.onboarding.OnboardingScreen
import com.gnaanaa.mtimer.ui.preset.PresetEditScreen
import com.gnaanaa.mtimer.ui.preset.PresetListScreen
import com.gnaanaa.mtimer.ui.settings.SettingsScreen
import com.gnaanaa.mtimer.ui.timer.TimerScreen

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object Timer : Screen("timer")
    object PresetList : Screen("preset_list")
    object Settings : Screen("settings")
    object History : Screen("history")
    object PresetEdit : Screen("preset_edit/{presetId}") {
        fun createRoute(presetId: String) = "preset_edit/$presetId"
    }
}

@Composable
fun MTimerNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onComplete = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Home.route) {
            HomeScreen(
                onStartTimer = { navController.navigate(Screen.Timer.route) },
                onNavigateToPresets = { navController.navigate(Screen.PresetList.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToHistory = { navController.navigate(Screen.History.route) }
            )
        }
        composable(Screen.History.route) {
            SessionHistoryScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Timer.route) {
            TimerScreen(
                onClose = { navController.popBackStack() }
            )
        }
        composable(Screen.PresetList.route) {
            PresetListScreen(
                onBack = { navController.popBackStack() },
                onEditPreset = { id -> navController.navigate(Screen.PresetEdit.createRoute(id)) },
                onCreatePreset = { navController.navigate(Screen.PresetEdit.createRoute("new")) }
            )
        }
        composable(
            route = Screen.PresetEdit.route,
            arguments = listOf(navArgument("presetId") { type = NavType.StringType })
        ) { backStackEntry ->
            // Pass backStackEntry so PresetEditScreen can read the presetId argument
            PresetEditScreen(
                backStackEntry = backStackEntry,
                onBack = { navController.popBackStack() }
            )
        }
    }
}