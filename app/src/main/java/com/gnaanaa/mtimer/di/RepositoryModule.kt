package com.gnaanaa.mtimer.di

import com.gnaanaa.mtimer.data.repository.PresetRepository
import com.gnaanaa.mtimer.data.repository.PresetRepositoryImpl
import com.gnaanaa.mtimer.data.repository.SessionRepository
import com.gnaanaa.mtimer.data.repository.SessionRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSessionRepository(
        sessionRepositoryImpl: SessionRepositoryImpl
    ): SessionRepository

    @Binds
    @Singleton
    abstract fun bindPresetRepository(
        presetRepositoryImpl: PresetRepositoryImpl
    ): PresetRepository
}
