package com.gnaanaa.mtimer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.gnaanaa.mtimer.data.datastore.UserPreferencesDataStore
import com.gnaanaa.mtimer.data.repository.PresetRepository
import com.gnaanaa.mtimer.service.SoundPlayer
import com.gnaanaa.mtimer.service.MeditationForegroundService
import com.gnaanaa.mtimer.ui.theme.ThemeMode
import com.gnaanaa.mtimer.ui.navigation.Screen
import com.gnaanaa.mtimer.ui.navigation.MTimerNavGraph
import com.gnaanaa.mtimer.ui.theme.MTimerTheme
import com.gnaanaa.mtimer.widget.worker.WidgetUpdateWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userPreferencesDataStore: UserPreferencesDataStore

    @Inject
    lateinit var presetRepository: PresetRepository

    @Inject
    lateinit var soundPlayer: SoundPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Ensure widget is updated
        WidgetUpdateWorker.enqueue(this)

        setContent {
            val themeMode by userPreferencesDataStore.themeMode.collectAsState(initial = ThemeMode.FOLLOW_SYSTEM)
            val isOnboardingCompleted by userPreferencesDataStore.isOnboardingCompleted.collectAsState(initial = null)

            if (isOnboardingCompleted != null) {
                val isSystemInDark = isSystemInDarkTheme()
                val darkTheme = when (themeMode) {
                    ThemeMode.FOLLOW_SYSTEM -> isSystemInDark
                    ThemeMode.LIGHT -> false
                    ThemeMode.DARK -> true
                }

                // Standard Android 15 edge-to-edge configuration
                // This replaces the deprecated Accompanist SystemUiController
                LaunchedEffect(darkTheme) {
                    enableEdgeToEdge(
                        statusBarStyle = SystemBarStyle.auto(
                            android.graphics.Color.TRANSPARENT,
                            android.graphics.Color.TRANSPARENT,
                        ) { darkTheme },
                        navigationBarStyle = SystemBarStyle.auto(
                            android.graphics.Color.TRANSPARENT,
                            android.graphics.Color.TRANSPARENT,
                        ) { darkTheme }
                    )
                }

                MTimerTheme(darkTheme = darkTheme) {
                    val navController = rememberNavController()
                    val startDestination = remember(isOnboardingCompleted) {
                        if (isOnboardingCompleted == true) Screen.Main.route else Screen.Onboarding.route
                    }

                    // Handle intent for shortcuts
                    androidx.compose.runtime.LaunchedEffect(intent) {
                        intent?.data?.let { uri ->
                            if (isOnboardingCompleted == true) {
                                when (uri.host) {
                                    "start_default" -> {
                                        CoroutineScope(Dispatchers.IO).launch {
                                            val presets = presetRepository.getAllPresetsList()
                                            val default = presets.firstOrNull()
                                            if (default != null) {
                                                MeditationForegroundService.startTimer(this@MainActivity, default)
                                                launch(Dispatchers.Main) {
                                                    navController.navigate(Screen.Timer.route)
                                                }
                                            }
                                        }
                                    }
                                    "history" -> {
                                        navController.navigate(Screen.Main.createRoute(Screen.History.route)) {
                                            launchSingleTop = true
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        MTimerNavGraph(
                            navController = navController,
                            soundPlayer = soundPlayer,
                            startDestination = startDestination
                        )
                    }
                }
            }
        }
    }
}
