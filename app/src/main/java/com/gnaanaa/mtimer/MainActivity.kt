package com.gnaanaa.mtimer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.gnaanaa.mtimer.data.datastore.UserPreferencesDataStore
import com.gnaanaa.mtimer.data.repository.PresetRepository
import com.gnaanaa.mtimer.service.MeditationForegroundService
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Ensure widget is updated
        WidgetUpdateWorker.enqueue(this)

        setContent {
            val useLightTheme by userPreferencesDataStore.useLightTheme.collectAsState(initial = false)
            val isOnboardingCompleted by userPreferencesDataStore.isOnboardingCompleted.collectAsState(initial = null)

            if (isOnboardingCompleted != null) {
                MTimerTheme(darkTheme = !useLightTheme) {
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
                                        navController.navigate(Screen.History.route)
                                    }
                                }
                            }
                        }
                    }

                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        MTimerNavGraph(
                            navController = navController,
                            startDestination = startDestination
                        )
                    }
                }
            }
        }
    }
}
