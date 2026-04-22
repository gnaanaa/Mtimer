package com.gnaanaa.mtimer.domain.usecase

import android.content.Context
import com.gnaanaa.mtimer.data.sync.getWeeklyMindfulnessMinutes
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class GetWeeklyStatsUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke(): Int {
        return try {
            getWeeklyMindfulnessMinutes(context)
        } catch (e: Exception) {
            0
        }
    }
}
