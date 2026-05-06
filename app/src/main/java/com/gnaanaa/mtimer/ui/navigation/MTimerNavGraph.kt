package com.gnaanaa.mtimer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.gnaanaa.mtimer.service.SoundPlayer
import com.gnaanaa.mtimer.ui.onboarding.OnboardingScreen
import com.gnaanaa.mtimer.ui.preset.PresetEditScreen
import com.gnaanaa.mtimer.ui.timer.TimerScreen

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object Timer : Screen("timer")
    object PresetList : Screen("preset_list")
    object Settings : Screen("settings")
    object History : Screen("history")
    object About : Screen("about")
    object HowToMeditate : Screen("how_to_meditate")
    object Main : Screen("main")
    object PresetEdit : Screen("preset_edit/{presetId}") {
        fun createRoute(presetId: String) = "preset_edit/$presetId"
    }
}

@Composable
fun MTimerNavGraph(
    navController: NavHostController,
    soundPlayer: SoundPlayer,
    startDestination: String = Screen.Main.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onComplete = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Main.route) {
            MainContainer(
                rootNavController = navController,
                onStartTimer = { navController.navigate(Screen.Timer.route) }
            )
        }

        composable(Screen.Timer.route) {
            TimerScreen(
                onClose = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.PresetEdit.route,
            arguments = listOf(navArgument("presetId") { type = NavType.StringType })
        ) { backStackEntry ->
            PresetEditScreen(
                backStackEntry = backStackEntry,
                onBack = { navController.popBackStack() },
                soundPlayer = soundPlayer
            )
        }
    }
}
