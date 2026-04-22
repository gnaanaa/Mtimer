package com.gnaanaa.mtimer.ui.settings

import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.feature.ExperimentalMindfulnessSessionApi
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.MindfulnessSessionRecord
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gnaanaa.mtimer.data.datastore.UserPreferencesDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalMindfulnessSessionApi::class)
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesDataStore: UserPreferencesDataStore,
    private val healthConnectClient: HealthConnectClient?
) : ViewModel() {

    private val _healthConnectPermissionsGranted = MutableStateFlow(false)
    val healthConnectPermissionsGranted = _healthConnectPermissionsGranted.asStateFlow()

    val permissions = setOf(
        HealthPermission.getWritePermission(MindfulnessSessionRecord::class),
        HealthPermission.getReadPermission(MindfulnessSessionRecord::class)
    )

    init {
        checkPermissions()
    }

    fun checkPermissions() {
        viewModelScope.launch {
            try {
                healthConnectClient?.let { client ->
                    val granted = client.permissionController.getGrantedPermissions()
                    android.util.Log.d("HealthConnect", "Granted permissions: $granted")
                    _healthConnectPermissionsGranted.value = granted.containsAll(permissions)
                } ?: run {
                    android.util.Log.w("HealthConnect", "Client is null in checkPermissions")
                    _healthConnectPermissionsGranted.value = false
                }
            } catch (e: Exception) {
                android.util.Log.e("HealthConnect", "Error checking permissions", e)
            }
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
}
