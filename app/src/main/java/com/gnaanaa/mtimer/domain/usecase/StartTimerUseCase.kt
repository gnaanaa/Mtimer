package com.gnaanaa.mtimer.domain.usecase

import android.content.Context
import android.content.Intent
import com.gnaanaa.mtimer.domain.model.Preset
import com.gnaanaa.mtimer.service.MeditationForegroundService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class StartTimerUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    operator fun invoke(preset: Preset) {
        val intent = Intent(context, MeditationForegroundService::class.java).apply {
            action = MeditationForegroundService.ACTION_START
            putExtra(MeditationForegroundService.EXTRA_PRESET_ID, preset.id)
            putExtra(MeditationForegroundService.EXTRA_PRESET_NAME, preset.name)
            putExtra(MeditationForegroundService.EXTRA_DURATION_SECONDS, preset.durationSeconds)
            putExtra(MeditationForegroundService.EXTRA_PREPARE_SECONDS, preset.prepareSeconds)
            putExtra(MeditationForegroundService.EXTRA_START_SOUND_ID, preset.startSoundId)
            putExtra(MeditationForegroundService.EXTRA_END_SOUND_ID, preset.endSoundId)
        }
        context.startForegroundService(intent)
    }
}
