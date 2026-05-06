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
    object Main : Screen("main") {
        fun createRoute(initialRoute: String? = null): String {
            return if (initialRoute == null) "main" else "main?initialRoute=$initialRoute"
        }
    }
    object PresetEdit : Screen("preset_edit/{presetId}?name={name}&duration={duration}") {
        fun createRoute(
            presetId: String,
            name: String? = null,
            duration: Int? = null
        ): String {
            val base = "preset_edit/$presetId"
            val params = mutableListOf<String>()
            if (name != null) params.add("name=$name")
            if (duration != null) params.add("duration=$duration")
            
            return if (params.isEmpty()) base else "$base?${params.joinToString("&")}"
        }
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
        
        composable(
            route = Screen.Main.route + "?initialRoute={initialRoute}",
            arguments = listOf(
                navArgument("initialRoute") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val initialRoute = backStackEntry.arguments?.getString("initialRoute")
            MainContainer(
                rootNavController = navController,
                onStartTimer = { navController.navigate(Screen.Timer.route) },
                initialDrawerRoute = initialRoute ?: Screen.Home.route
            )
        }

        composable(Screen.Timer.route) {
            TimerScreen(
                onClose = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.PresetEdit.route,
            arguments = listOf(
                navArgument("presetId") { type = NavType.StringType },
                navArgument("name") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("duration") { 
                    type = NavType.StringType // Pass as string to keep nullable/defaultValue simple
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            PresetEditScreen(
                backStackEntry = backStackEntry,
                onBack = { navController.popBackStack() },
                soundPlayer = soundPlayer
            )
        }
    }
}
