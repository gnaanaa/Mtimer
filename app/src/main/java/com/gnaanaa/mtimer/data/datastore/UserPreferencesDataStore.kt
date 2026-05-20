package com.gnaanaa.mtimer.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.gnaanaa.mtimer.ui.theme.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesDataStore @Inject constructor(
    @ApplicationContext val context: Context
) {
    private val useLightThemeKey = booleanPreferencesKey("use_light_theme")
    private val themeModeKey = intPreferencesKey("theme_mode")
    private val isOnboardingCompletedKey = booleanPreferencesKey("onboarding_completed")
    private val isGoogleFitEnabledKey = booleanPreferencesKey("google_fit_enabled")
    private val isHealthConnectEnabledKey = booleanPreferencesKey("health_connect_enabled")

    private val homeHintShownKey = booleanPreferencesKey("home_hint_shown")
    private val historyHintShownKey = booleanPreferencesKey("history_hint_shown")
    private val presetsHintShownKey = booleanPreferencesKey("presets_hint_shown")
    private val guideHintShownKey = booleanPreferencesKey("guide_hint_shown")

    val useLightTheme: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[useLightThemeKey] ?: false
        }

    val themeMode: Flow<ThemeMode> = context.dataStore.data
        .map { preferences ->
            val modeIndex = preferences[themeModeKey]
            if (modeIndex != null) {
                ThemeMode.entries.getOrElse(modeIndex) { ThemeMode.FOLLOW_SYSTEM }
            } else {
                // Migration: If we have the old boolean, use it to set initial mode
                val oldUseLight = preferences[useLightThemeKey]
                if (oldUseLight != null) {
                    if (oldUseLight) ThemeMode.LIGHT else ThemeMode.DARK
                } else {
                    ThemeMode.FOLLOW_SYSTEM
                }
            }
        }

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[isOnboardingCompletedKey] ?: false
        }

    val isGoogleFitEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[isGoogleFitEnabledKey] ?: false
        }

    val isHealthConnectEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[isHealthConnectEnabledKey] ?: false
        }

    val homeHintShown: Flow<Boolean> = context.dataStore.data.map { it[homeHintShownKey] ?: false }
    val historyHintShown: Flow<Boolean> = context.dataStore.data.map { it[historyHintShownKey] ?: false }
    val presetsHintShown: Flow<Boolean> = context.dataStore.data.map { it[presetsHintShownKey] ?: false }
    val guideHintShown: Flow<Boolean> = context.dataStore.data.map { it[guideHintShownKey] ?: false }

    suspend fun setUseLightTheme(useLight: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[useLightThemeKey] = useLight
            preferences[themeModeKey] = if (useLight) ThemeMode.LIGHT.ordinal else ThemeMode.DARK.ordinal
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[themeModeKey] = mode.ordinal
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        android.util.Log.d("HealthConnect", "DataStore: Setting onboarding completed = $completed")
        context.dataStore.edit { preferences ->
            preferences[isOnboardingCompletedKey] = completed
        }
    }

    suspend fun setGoogleFitEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[isGoogleFitEnabledKey] = enabled
        }
    }

    suspend fun setHealthConnectEnabled(enabled: Boolean) {
        android.util.Log.d("HealthConnect", "DataStore: Saving HC enabled = $enabled")
        context.dataStore.edit { preferences ->
            preferences[isHealthConnectEnabledKey] = enabled
        }
    }

    suspend fun setHomeHintShown(shown: Boolean = true) {
        context.dataStore.edit { it[homeHintShownKey] = shown }
    }
    suspend fun setHistoryHintShown(shown: Boolean = true) {
        context.dataStore.edit { it[historyHintShownKey] = shown }
    }
    suspend fun setPresetsHintShown(shown: Boolean = true) {
        context.dataStore.edit { it[presetsHintShownKey] = shown }
    }
    suspend fun setGuideHintShown(shown: Boolean = true) {
        context.dataStore.edit { it[guideHintShownKey] = shown }
    }
}
