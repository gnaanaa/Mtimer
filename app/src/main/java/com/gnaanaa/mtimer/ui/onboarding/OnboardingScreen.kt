package com.gnaanaa.mtimer.ui.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.MindfulnessSessionRecord
import androidx.health.connect.client.feature.ExperimentalMindfulnessSessionApi
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import com.gnaanaa.mtimer.data.datastore.UserPreferencesDataStore
import com.gnaanaa.mtimer.ui.home.DotMatrix
import kotlinx.coroutines.launch
import javax.inject.Inject

@dagger.hilt.android.lifecycle.HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userPreferencesDataStore: UserPreferencesDataStore,
    val healthConnectClient: HealthConnectClient?
) : androidx.lifecycle.ViewModel() {
    fun completeOnboarding() {
        viewModelScope.launch {
            userPreferencesDataStore.setOnboardingCompleted(true)
        }
    }

    fun setGoogleFitEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesDataStore.setGoogleFitEnabled(enabled)
        }
    }

    fun setHealthConnectEnabled(context: android.content.Context, enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesDataStore.setHealthConnectEnabled(enabled)
            if (enabled) {
                com.gnaanaa.mtimer.data.sync.HealthConnectSyncWorker.enqueue(context)
            }
        }
    }
}

@OptIn(ExperimentalMindfulnessSessionApi::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val availability = viewModel.healthConnectClient != null

    val permissions = remember {
        setOf(
            HealthPermission.getWritePermission(MindfulnessSessionRecord::class),
            HealthPermission.getReadPermission(MindfulnessSessionRecord::class),
            HealthPermission.getReadPermission(HeartRateRecord::class),
            HealthPermission.getWritePermission(HeartRateRecord::class)
        )
    }

    LaunchedEffect(Unit) {
        android.util.Log.d("HealthConnect", "Onboarding initialized. Permissions: $permissions")
    }

    val healthLauncher = rememberLauncherForActivityResult(
        PermissionController.createRequestPermissionResultContract()
    ) { granted ->
        android.util.Log.d("HealthConnect", "Onboarding permissions result: $granted")
        viewModel.setHealthConnectEnabled(context, true)
        viewModel.completeOnboarding()
        onComplete()
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        if (availability) {
            healthLauncher.launch(permissions)
        } else {
            viewModel.completeOnboarding()
            onComplete()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "MTIMER",
                fontFamily = DotMatrix,
                fontSize = 32.sp,
                letterSpacing = 8.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Track your mindfulness journey across apps via Health Connect and Google Fit.",
                fontFamily = DotMatrix,
                fontSize = 14.sp,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f)
            )
            Spacer(modifier = Modifier.height(48.dp))
            Button(
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else if (availability) {
                        healthLauncher.launch(permissions)
                    } else {
                        viewModel.completeOnboarding()
                        onComplete()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = (if (availability) "Enable Health Connect" else "Health Connect Not Available").uppercase(),
                    fontFamily = DotMatrix,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    val fitnessOptions = com.gnaanaa.mtimer.data.sync.getGoogleFitOptions()
                    val account = com.google.android.gms.auth.api.signin.GoogleSignIn.getAccountForExtension(context, fitnessOptions)
                    if (!com.google.android.gms.auth.api.signin.GoogleSignIn.hasPermissions(account, fitnessOptions)) {
                        com.google.android.gms.auth.api.signin.GoogleSignIn.requestPermissions(
                            context as android.app.Activity,
                            1002,
                            account,
                            fitnessOptions
                        )
                    }
                    viewModel.setGoogleFitEnabled(true)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text(
                    text = "ENABLE GOOGLE FIT",
                    fontFamily = DotMatrix,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = {
                    viewModel.completeOnboarding()
                    onComplete()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "SKIP FOR NOW",
                    fontFamily = DotMatrix,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
