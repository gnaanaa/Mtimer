package com.gnaanaa.mtimer.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
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
    private val isOnboardingCompletedKey = booleanPreferencesKey("onboarding_completed")

    val useLightTheme: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[useLightThemeKey] ?: false
        }

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[isOnboardingCompletedKey] ?: false
        }

    suspend fun setUseLightTheme(useLight: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[useLightThemeKey] = useLight
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[isOnboardingCompletedKey] = completed
        }
    }
}
