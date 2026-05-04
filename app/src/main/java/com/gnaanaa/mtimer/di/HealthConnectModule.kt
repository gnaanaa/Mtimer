package com.gnaanaa.mtimer.di

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HealthConnectModule {

    @Provides
    @Singleton
    fun provideHealthConnectClient(@ApplicationContext context: Context): HealthConnectClient? {
        val status = HealthConnectClient.getSdkStatus(context)
        android.util.Log.d("HealthConnect", "Module: SDK Status detected as $status")
        
        // On Android 14+ (API 34+), Health Connect is part of the system.
        // The SDK status might return SDK_UNAVAILABLE if the app-based provider is used,
        // but the system-based one is active.
        return try {
            // Attempt to create even if status says unavailable, as API 34+ handles this differently
            HealthConnectClient.getOrCreate(context)
        } catch (e: Exception) {
            android.util.Log.e("HealthConnect", "Module: Failed to provide HealthConnectClient", e)
            null
        }
    }
}
