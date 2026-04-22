package com.gnaanaa.mtimer.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [SessionEntity::class, PresetEntity::class], version = 1, exportSchema = false)
abstract class MTimerDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
    abstract fun presetDao(): PresetDao
}
