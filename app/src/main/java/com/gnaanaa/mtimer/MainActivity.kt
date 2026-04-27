package com.gnaanaa.mtimer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.gnaanaa.mtimer.data.datastore.UserPreferencesDataStore
import com.gnaanaa.mtimer.ui.navigation.Screen
import com.gnaanaa.mtimer.ui.navigation.MTimerNavGraph
import com.gnaanaa.mtimer.ui.theme.MTimerTheme
import com.gnaanaa.mtimer.widget.worker.WidgetUpdateWorker
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userPreferencesDataStore: UserPreferencesDataStore

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
                        if (isOnboardingCompleted == true) Screen.Home.route else Screen.Onboarding.route
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
