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
        val sdkStatus = HealthConnectClient.getSdkStatus(context)
        android.util.Log.d("HealthConnect", "SDK Status: $sdkStatus")
        
        // On Android 14+, Health Connect is built-in, so status might be different
        // or we can just try to get the client.
        return try {
            HealthConnectClient.getOrCreate(context)
        } catch (e: Exception) {
            android.util.Log.e("HealthConnect", "Error getting client", e)
            null
        }
    }
}
