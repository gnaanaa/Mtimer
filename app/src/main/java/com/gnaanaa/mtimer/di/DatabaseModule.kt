package com.gnaanaa.mtimer.di

import android.content.Context
import androidx.room.Room
import com.gnaanaa.mtimer.data.db.PresetDao
import com.gnaanaa.mtimer.data.db.SessionDao
import com.gnaanaa.mtimer.data.db.MTimerDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MTimerDatabase {
        return Room.databaseBuilder(
            context,
            MTimerDatabase::class.java,
            "mtimer_database"
        ).build()
    }

    @Provides
    fun provideSessionDao(database: MTimerDatabase): SessionDao = database.sessionDao()

    @Provides
    fun providePresetDao(database: MTimerDatabase): PresetDao = database.presetDao()
}
