package com.gnaanaa.mtimer.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gnaanaa.mtimer.data.repository.SessionRepository
import com.gnaanaa.mtimer.domain.model.Session
import com.gnaanaa.mtimer.domain.usecase.GetWeeklyChartDataUseCase
import com.gnaanaa.mtimer.domain.usecase.WeeklyChartData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class HistoryUiState(
    val isLoading: Boolean = true,
    val sessionCount: Int = 0,
    val totalDuration: Long = 0L,
    val chartData: WeeklyChartData? = null,
    val groupedSessions: Map<String, List<Session>> = emptyMap()
)

@HiltViewModel
class SessionHistoryViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val getWeeklyChartDataUseCase: GetWeeklyChartDataUseCase
) : ViewModel() {

    private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    val uiState: StateFlow<HistoryUiState> = combine(
        sessionRepository.getSessionCount(),
        sessionRepository.getTotalDuration(),
        getWeeklyChartDataUseCase(),
        sessionRepository.getAllSessions()
    ) { count, duration, chart, sessions ->
        val grouped = sessions.groupByTo(LinkedHashMap()) { session ->
            monthYearFormat.format(Date(session.startTime)).uppercase()
        }
        HistoryUiState(
            isLoading = false,
            sessionCount = count,
            totalDuration = duration,
            chartData = chart,
            groupedSessions = grouped
        )
    }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryUiState(isLoading = false) // Start without loader
    )
}
