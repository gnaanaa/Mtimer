package com.gnaanaa.mtimer.ui.settings

import android.content.Context
import android.net.Uri
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.feature.ExperimentalMindfulnessSessionApi
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.MindfulnessSessionRecord
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gnaanaa.mtimer.data.datastore.UserPreferencesDataStore
import com.gnaanaa.mtimer.data.repository.PresetRepository
import com.gnaanaa.mtimer.data.repository.SessionRepository
import com.gnaanaa.mtimer.data.db.toEntity
import com.gnaanaa.mtimer.data.db.toDomain
import com.gnaanaa.mtimer.data.sync.BackupData
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
    private val presetRepository: PresetRepository,
    private val sessionRepository: SessionRepository
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
        HealthPermission.getWritePermission(HeartRateRecord::class)
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

    val isGoogleFitEnabled: StateFlow<Boolean> = userPreferencesDataStore.isGoogleFitEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isHealthConnectEnabled: StateFlow<Boolean> = userPreferencesDataStore.isHealthConnectEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun toggleTheme(useLight: Boolean) {
        viewModelScope.launch {
            userPreferencesDataStore.setUseLightTheme(useLight)
        }
    }

    fun toggleGoogleFit(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesDataStore.setGoogleFitEnabled(enabled)
        }
    }

    fun toggleHealthConnect(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesDataStore.setHealthConnectEnabled(enabled)
        }
    }

    fun exportPresets(context: Context, uri: Uri) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val presets = presetRepository.getAllPresetsList().map { it.toEntity() }
                    val sessions = sessionRepository.getAllSessionsList().map { it.toEntity() }
                    val backupData = BackupData(presets, sessions)
                    val json = gson.toJson(backupData)
                    context.contentResolver.openOutputStream(uri)?.use { output ->
                        output.write(json.toByteArray())
                    }
                } catch (e: Exception) {
                    android.util.Log.e("SettingsViewModel", "Failed to export backup", e)
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
                        val type = object : TypeToken<BackupData>() {}.type
                        val backupData: BackupData = gson.fromJson(json, type)
                        
                        // Import presets
                        backupData.presets.forEach { presetRepository.savePreset(it.toDomain()) }
                        
                        // Import sessions
                        val localSessions = sessionRepository.getAllSessionsList()
                        val localSessionTimes = localSessions.map { it.startTime }.toSet()
                        backupData.sessions.forEach { remoteEntity ->
                            if (remoteEntity.startTime !in localSessionTimes) {
                                sessionRepository.saveSession(remoteEntity.toDomain())
                            }
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("SettingsViewModel", "Failed to import backup", e)
                }
            }
        }
    }
}
