package com.gnaanaa.mtimer.domain.usecase

import android.content.Context
import android.content.Intent
import com.gnaanaa.mtimer.service.MeditationForegroundService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class StopTimerUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    operator fun invoke() {
        val intent = Intent(context, MeditationForegroundService::class.java).apply {
            action = MeditationForegroundService.ACTION_STOP
        }
        context.startService(intent)
    }
}
