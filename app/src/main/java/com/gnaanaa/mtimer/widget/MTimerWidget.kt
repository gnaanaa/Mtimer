package com.gnaanaa.mtimer.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.FontFamily
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.gnaanaa.mtimer.data.db.MTimerDatabase
import com.gnaanaa.mtimer.data.db.toDomain
import com.gnaanaa.mtimer.domain.model.Preset
import com.gnaanaa.mtimer.service.MeditationForegroundService
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.layout.size
import androidx.glance.layout.Box
import androidx.glance.layout.ContentScale
import androidx.glance.text.TextAlign
import com.gnaanaa.mtimer.R
import com.gnaanaa.mtimer.data.sync.DriveSyncWorker
import com.gnaanaa.mtimer.widget.worker.WidgetUpdateWorker

class MTimerWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = MTimerWidgetStateDefinition

    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(DpSize(110.dp, 40.dp), DpSize(220.dp, 100.dp))
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val minutes = prefs[intPreferencesKey("weekly_minutes")] ?: 0
            val presetsJson = prefs[stringPreferencesKey("presets_json")] ?: "[]"
            val presets = try {
                Json.decodeFromString<List<com.gnaanaa.mtimer.data.db.PresetEntity>>(presetsJson)
            } catch (e: Exception) {
                emptyList()
            }

            MTimerWidgetContent(minutes, presets)
        }
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun database(): MTimerDatabase
}

class StartAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val presetId = parameters[PresetIdKey] ?: return
        
        val entryPoint = EntryPointAccessors.fromApplication(context, WidgetEntryPoint::class.java)
        val db = entryPoint.database()
        val presetEntity = db.presetDao().getPresetById(presetId) ?: return
        val preset = presetEntity.toDomain()

        MeditationForegroundService.startTimer(context, preset)
    }

    companion object {
        val PresetIdKey = ActionParameters.Key<String>("preset_id")
    }
}

class RefreshAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        // Enqueue cloud sync and widget update
        DriveSyncWorker.enqueue(context)
        WidgetUpdateWorker.updateNow(context)
    }
}

@Composable
fun MTimerWidgetContent(minutes: Int, presets: List<com.gnaanaa.mtimer.data.db.PresetEntity>) {
    val size = LocalSize.current
    val isExpanded = size.width >= 200.dp
    
    val dotMatrixStyle = TextStyle(
        color = ColorProvider(Color.White),
        fontFamily = FontFamily.Monospace, // Using Monospace as fallback for Glance
        fontWeight = FontWeight.Medium,
        textAlign = TextAlign.Center
    )

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(8.dp)
    ) {
        // Sync Icon at Top Right
        Box(
            modifier = GlanceModifier.fillMaxWidth(),
            contentAlignment = Alignment.TopEnd
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_sync),
                contentDescription = "Sync",
                modifier = GlanceModifier
                    .size(20.dp)
                    .clickable(actionRunCallback<RefreshAction>())
            )
        }

        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!isExpanded) {
                // Compact Layout
                Text(
                    text = "${minutes}M",
                    style = dotMatrixStyle.copy(
                        fontSize = 18.sp, 
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "THIS WEEK",
                    style = dotMatrixStyle.copy(
                        color = ColorProvider(Color.White.copy(alpha = 0.6f)),
                        fontSize = 8.sp
                    )
                )
                
                if (presets.isNotEmpty()) {
                    Spacer(modifier = GlanceModifier.height(8.dp))
                    val firstPreset = presets.first()
                    val displayName = if (firstPreset.name.length > 18) firstPreset.name.take(15) + "..." else firstPreset.name
                    
                    Box(
                        modifier = GlanceModifier
                            .fillMaxWidth() // Uses weight-based sizing indirectly by filling parent width
                            .padding(horizontal = 24.dp) // Creates a centered look by adding margins
                            .background(ColorProvider(Color.DarkGray.copy(alpha = 0.5f)))
                            .padding(8.dp)
                            .clickable(actionRunCallback<StartAction>(
                                actionParametersOf(StartAction.PresetIdKey to firstPreset.id)
                            )),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = displayName.uppercase(),
                                style = dotMatrixStyle.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                                maxLines = 1
                            )
                            Text(
                                text = "${firstPreset.durationSeconds / 60}M",
                                style = dotMatrixStyle.copy(fontSize = 10.sp, color = ColorProvider(Color.White.copy(alpha = 0.8f)))
                            )
                        }
                    }
                }
            } else {
                // Expanded Layout
                Text(
                    text = "WEEKLY PROGRESS",
                    style = dotMatrixStyle.copy(
                        color = ColorProvider(Color.White.copy(alpha = 0.6f)),
                        fontSize = 9.sp
                    )
                )
                
                Text(
                    text = "${minutes} MINUTES",
                    style = dotMatrixStyle.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                
                Spacer(modifier = GlanceModifier.height(12.dp))
                
                Text(
                    text = "QUICK START",
                    style = dotMatrixStyle.copy(
                        color = ColorProvider(Color.White.copy(alpha = 0.5f)),
                        fontSize = 8.sp
                    )
                )
                Spacer(modifier = GlanceModifier.height(4.dp))
                
                Row(
                    modifier = GlanceModifier.fillMaxWidth().padding(horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (presets.isEmpty()) {
                        Text(
                            text = "NO PRESETS",
                            style = dotMatrixStyle.copy(fontSize = 11.sp, color = ColorProvider(Color.Gray))
                        )
                    } else {
                        presets.take(3).forEachIndexed { index, preset ->
                            val displayName = if (preset.name.length > 12) preset.name.take(10) + "..." else preset.name
                            Box(
                                modifier = GlanceModifier
                                    .defaultWeight() // Distribute space evenly
                                    .background(ColorProvider(Color.DarkGray.copy(alpha = 0.5f)))
                                    .padding(8.dp)
                                    .clickable(actionRunCallback<StartAction>(
                                        actionParametersOf(StartAction.PresetIdKey to preset.id)
                                    )),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = displayName.uppercase(),
                                        style = dotMatrixStyle.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "${preset.durationSeconds / 60}M",
                                        style = dotMatrixStyle.copy(fontSize = 9.sp, color = ColorProvider(Color.White.copy(alpha = 0.8f)))
                                    )
                                }
                            }
                            if (index < presets.take(3).size - 1) {
                                Spacer(modifier = GlanceModifier.width(8.dp)) // Spacing between buttons
                            }
                        }
                    }
                }
            }
        }
    }
}
