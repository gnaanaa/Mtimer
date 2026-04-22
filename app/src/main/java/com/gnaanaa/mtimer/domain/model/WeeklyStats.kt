package com.gnaanaa.mtimer.domain.model

enum class StatsSource { HEALTH_CONNECT, LOCAL }

data class WeeklyStats(
    val weekStart: Long,
    val totalMinutes: Int,
    val sessionCount: Int,
    val source: StatsSource
)
