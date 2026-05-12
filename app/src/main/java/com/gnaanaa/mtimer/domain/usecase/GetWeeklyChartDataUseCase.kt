package com.gnaanaa.mtimer.domain.usecase

import com.gnaanaa.mtimer.data.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

data class WeeklyChartData(
    val weeklyMinutes: List<Int>,
    val labels: List<String>,
    val thisWeekMinutes: Int,
    val bestWeekMinutes: Int,
    val averageMinutes: Int
)

class GetWeeklyChartDataUseCase @Inject constructor(
    private val sessionRepository: SessionRepository
) {
    operator fun invoke(): Flow<WeeklyChartData> {
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val labelFormatter = DateTimeFormatter.ofPattern("MMM dd")

        return sessionRepository.getWeeklyStats().map { stats ->
            val today = LocalDate.now()
            val currentMonday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            
            val resultMinutes = mutableListOf<Int>()
            val resultLabels = mutableListOf<String>()
            
            val statsMap = stats.associate { it.weekStart to it.totalSeconds }

            // Fill 12 weeks back from current week
            for (i in 11 downTo 0) {
                val weekMonday = currentMonday.minusWeeks(i.toLong())
                val weekKey = weekMonday.format(dateFormatter)
                
                val totalSeconds = statsMap[weekKey] ?: 0L
                val totalMinutes = (totalSeconds / 60).toInt()
                
                resultMinutes.add(totalMinutes)
                resultLabels.add(weekMonday.format(labelFormatter))
            }

            val thisWeek = resultMinutes.last()
            val bestWeek = resultMinutes.maxOrNull() ?: 0
            val average = if (resultMinutes.isNotEmpty()) resultMinutes.average().toInt() else 0

            WeeklyChartData(
                weeklyMinutes = resultMinutes,
                labels = resultLabels,
                thisWeekMinutes = thisWeek,
                bestWeekMinutes = bestWeek,
                averageMinutes = average
            )
        }
    }
}
