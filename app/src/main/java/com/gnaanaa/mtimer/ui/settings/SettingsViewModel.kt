package com.gnaanaa.mtimer.ui.settings

import android.content.Context
import android.net.Uri
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.feature.ExperimentalMindfulnessSessionApi
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.MindfulnessSessionRecord
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gnaanaa.mtimer.data.datastore.UserPreferencesDataStore
import com.gnaanaa.mtimer.data.repository.PresetRepository
import com.gnaanaa.mtimer.data.sync.hasAllPermissions
import com.gnaanaa.mtimer.data.sync.openHealthConnectSettings
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@OptIn(ExperimentalMindfulnessSessionApi::class)
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesDataStore: UserPreferencesDataStore,
    private val healthConnectClient: HealthConnectClient?,
    private val presetRepository: PresetRepository
) : ViewModel() {

    private val gson = Gson()

    private val _healthConnectPermissionsGranted = MutableStateFlow(false)
    val healthConnectPermissionsGranted = _healthConnectPermissionsGranted.asStateFlow()

    private val _googleAccount = MutableStateFlow<GoogleSignInAccount?>(null)
    val googleAccount = _googleAccount.asStateFlow()

    val permissions = setOf(
        HealthPermission.getWritePermission(MindfulnessSessionRecord::class),
        HealthPermission.getReadPermission(MindfulnessSessionRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getWritePermission(HeartRateRecord::class),
        HealthPermission.getWritePermission(ExerciseSessionRecord::class),
        HealthPermission.getReadPermission(ExerciseSessionRecord::class)
    )

    private val _sdkStatus = MutableStateFlow(HealthConnectClient.SDK_UNAVAILABLE)
    val sdkStatus = _sdkStatus.asStateFlow()

    init {
        // Initial check for Google account
        viewModelScope.launch {
            _googleAccount.value = GoogleSignIn.getLastSignedInAccount(userPreferencesDataStore.context)
        }
    }

    fun checkPermissions(context: Context) {
        viewModelScope.launch {
            try {
                val status = HealthConnectClient.getSdkStatus(context)
                _sdkStatus.value = status
                _healthConnectPermissionsGranted.value = hasAllPermissions(context)
                _googleAccount.value = GoogleSignIn.getLastSignedInAccount(context)
            } catch (e: Exception) {
                android.util.Log.e("HealthConnect", "Error checking permissions/account", e)
            }
        }
    }

    fun openHC(context: Context) {
        openHealthConnectSettings(context)
    }

    fun updateGoogleAccount(context: Context, account: GoogleSignInAccount?) {
        _googleAccount.value = account
        if (account != null) {
            // Trigger sync when signed in
            com.gnaanaa.mtimer.data.sync.DriveSyncWorker.enqueue(context)
        }
    }

    fun isHealthConnectAvailable(): Boolean {
        return healthConnectClient != null
    }

    val useLightTheme: StateFlow<Boolean> = userPreferencesDataStore.useLightTheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun toggleTheme(useLight: Boolean) {
        viewModelScope.launch {
            userPreferencesDataStore.setUseLightTheme(useLight)
        }
    }

    fun exportPresets(context: Context, uri: Uri) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val presets = presetRepository.getAllPresetsList()
                    val json = gson.toJson(presets)
                    context.contentResolver.openOutputStream(uri)?.use { output ->
                        output.write(json.toByteArray())
                    }
                } catch (e: Exception) {
                    android.util.Log.e("SettingsViewModel", "Failed to export presets", e)
                }
            }
        }
    }

    fun importPresets(context: Context, uri: Uri) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val json = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                    if (json != null) {
                        val type = object : TypeToken<List<com.gnaanaa.mtimer.domain.model.Preset>>() {}.type
                        val presets: List<com.gnaanaa.mtimer.domain.model.Preset> = gson.fromJson(json, type)
                        presets.forEach { presetRepository.savePreset(it) }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("SettingsViewModel", "Failed to import presets", e)
                }
            }
        }
    }
}
