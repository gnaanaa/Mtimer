package com.gnaanaa.mtimer.widget

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.glance.state.GlanceStateDefinition
import java.io.File

object MTimerWidgetStateDefinition : GlanceStateDefinition<Preferences> {
    private const val DATASTORE_NAME = "mtimer_widget_prefs"
    private val Context.datastore by preferencesDataStore(name = DATASTORE_NAME)

    override suspend fun getDataStore(context: Context, fileKey: String) = context.datastore

    override fun getLocation(context: Context, fileKey: String): File {
        return File(context.applicationContext.filesDir, "datastore/$DATASTORE_NAME.preferences_pb")
    }
}
