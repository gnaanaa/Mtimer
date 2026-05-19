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

import com.gnaanaa.mtimer.data.sync.fetchHeartRateRange
import androidx.health.connect.client.records.HeartRateRecord
import java.time.Instant
import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HistoryUiState(
    val isLoading: Boolean = true,
    val sessionCount: Int = 0,
    val totalDuration: Long = 0L,
    val chartData: WeeklyChartData? = null,
    val groupedSessions: Map<String, List<Session>> = emptyMap()
)

@HiltViewModel
class SessionHistoryViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sessionRepository: SessionRepository,
    private val getWeeklyChartDataUseCase: GetWeeklyChartDataUseCase
) : ViewModel() {

    private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    private val _heartRateSamples = MutableStateFlow<List<HeartRateRecord.Sample>>(emptyList())
    val heartRateSamples = _heartRateSamples.asStateFlow()

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

    fun fetchHeartRate(session: Session) {
        viewModelScope.launch {
            val start = Instant.ofEpochMilli(session.startTime)
            val end = if (session.endTime > session.startTime) {
                Instant.ofEpochMilli(session.endTime)
            } else {
                start.plusSeconds(session.durationSeconds.toLong())
            }
            _heartRateSamples.value = fetchHeartRateRange(context, start, end)
        }
    }

    fun clearHeartRate() {
        _heartRateSamples.value = emptyList()
    }
}
