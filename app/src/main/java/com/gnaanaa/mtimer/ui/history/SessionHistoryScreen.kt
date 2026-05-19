package com.gnaanaa.mtimer.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gnaanaa.mtimer.domain.model.Session
import com.gnaanaa.mtimer.ui.home.DotMatrix
import com.gnaanaa.mtimer.ui.home.InterFont
import com.gnaanaa.mtimer.ui.home.SessionDetailDialog
import com.gnaanaa.mtimer.ui.home.formatDurationAligned
import com.gnaanaa.mtimer.ui.home.alignColons
import com.gnaanaa.mtimer.ui.home.styleDottedDigits
import com.gnaanaa.mtimer.ui.theme.Spacing
import com.gnaanaa.mtimer.ui.theme.Radius
import androidx.compose.ui.text.style.TextOverflow
import androidx.health.connect.client.records.HeartRateRecord
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun SessionHistoryScreen(
    onBack: () -> Unit,
    onOpenDrawer: () -> Unit,
    viewModel: SessionHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val heartRateSamples by viewModel.heartRateSamples.collectAsState()
    val groupedSessions = uiState.groupedSessions
    val sessionCount = uiState.sessionCount
    val totalDuration = uiState.totalDuration
    val chartData = uiState.chartData
    var selectedSession by remember { mutableStateOf<Session?>(null) }

    // State to keep track of expanded/collapsed months
    val expandedStates = remember { mutableStateMapOf<String, Boolean>() }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "HISTORY",
                        fontFamily = DotMatrix,
                        letterSpacing = 4.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                }
            )
        }
    ) { padding ->
        if (groupedSessions.isEmpty() && !uiState.isLoading) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "NO SESSIONS RECORDED YET",
                    fontFamily = DotMatrix,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(Spacing.medium),
                verticalArrangement = Arrangement.spacedBy(Spacing.tiny)
            ) {
                item {
                    PracticeSummaryCard(sessionCount, totalDuration)
                    
                    chartData?.let { data ->
                        Spacer(modifier = Modifier.height(Spacing.large))
                        WeeklyStatsRow(
                            thisWeek = data.thisWeekMinutes,
                            bestWeek = data.bestWeekMinutes,
                            avg = data.averageMinutes
                        )
                        Spacer(modifier = Modifier.height(Spacing.medium))
                        WeeklyMinutesChart(
                            minutes = data.weeklyMinutes,
                            labels = data.labels,
                            modifier = Modifier.padding(horizontal = Spacing.tiny)
                        )
                    }

                    Spacer(modifier = Modifier.height(Spacing.large))
                }

                groupedSessions.forEach { (monthYear, sessionsInGroup) ->
                    val isExpanded = expandedStates[monthYear] ?: true

                    stickyHeader {
                        Surface(
                            onClick = { expandedStates[monthYear] = !isExpanded },
                            color = MaterialTheme.colorScheme.background
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = Spacing.tiny),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = monthYear,
                                        fontFamily = InterFont,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                    )
                                    if (!isExpanded) {
                                        val totalSeconds = sessionsInGroup.sumOf { it.durationSeconds.toLong() }
                                        val hours = totalSeconds / 3600
                                        val minutes = (totalSeconds % 3600) / 60
                                        val summary = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
                                        
                                        Text(
                                            text = "${sessionsInGroup.size} SESSIONS • $summary".styleDottedDigits(),
                                            fontFamily = InterFont,
                                            fontSize = 11.sp,
                                            letterSpacing = 0.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        )
                                    }
                                }

                                Icon(
                                    imageVector = if (isExpanded) 
                                        Icons.Default.KeyboardArrowUp 
                                    else 
                                        Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }

                    if (isExpanded) {
                        items(sessionsInGroup, key = { it.id }) { session ->
                            SessionItem(
                                session = session,
                                onClick = { 
                                    selectedSession = session
                                    viewModel.fetchHeartRate(session)
                                }
                            )
                        }
                        
                        item {
                            Spacer(modifier = Modifier.height(Spacing.medium))
                        }
                    }
                }
            }
        }
    }

    selectedSession?.let {
        SessionDetailDialog(
            session = it, 
            heartRateSamples = heartRateSamples,
            onDismiss = { 
                selectedSession = null
                viewModel.clearHeartRate()
            }
        )
    }
}

@Composable
fun WeeklyStatsRow(thisWeek: Int, bestWeek: Int, avg: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        StatItem("THIS WEEK", "${thisWeek}M".styleDottedDigits())
        StatItem("BEST WEEK", "${bestWeek}M".styleDottedDigits())
        StatItem("12W AVG", "${avg}M".styleDottedDigits())
    }
}

@Composable
fun StatItem(label: String, value: Any) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (value is androidx.compose.ui.text.AnnotatedString) {
            Text(
                text = value,
                fontFamily = InterFont,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        } else {
            Text(
                text = value.toString(),
                fontFamily = InterFont,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = label,
            fontFamily = InterFont,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.5.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun PracticeSummaryCard(count: Int, totalSeconds: Long) {
    val isDark = MaterialTheme.colorScheme.background.run { (red + green + blue) < 0.5 }
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Radius.large),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) 
                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier.padding(Spacing.medium),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "LIFETIME PRACTICE",
                fontFamily = InterFont,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
            )
            Spacer(modifier = Modifier.height(Spacing.small))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "${hours}h ${minutes}m".styleDottedDigits(),
                        fontFamily = InterFont,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "TOTAL TIME",
                        fontFamily = InterFont,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = count.toString().styleDottedDigits(),
                        fontFamily = InterFont,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "SESSIONS",
                        fontFamily = InterFont,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun SessionItem(session: Session, onClick: () -> Unit) {
    val isDark = MaterialTheme.colorScheme.background.run { (red + green + blue) < 0.5 }
    val primaryColor = MaterialTheme.colorScheme.primary
    val dateFormat = remember { SimpleDateFormat("MMM dd • HH:mm", Locale.getDefault()) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp) // Consistent with other items
            .clip(RoundedCornerShape(Radius.medium))
            .background(
                if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) 
                else primaryColor.copy(alpha = 0.04f)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.medium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = (session.presetName ?: "MEDITATION").uppercase(),
                fontFamily = InterFont,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onBackground.copy(0.85f)
            )
            Text(
                text = dateFormat.format(Date(session.startTime)).uppercase().alignColons(),
                fontFamily = InterFont,
                fontSize = 11.sp,
                letterSpacing = 0.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
        
        Spacer(modifier = Modifier.width(Spacing.medium))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = formatDurationAligned(session.durationSeconds),
                fontFamily = InterFont,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.width(Spacing.small))

            Icon(
                imageVector = if (session.completed) Icons.Default.CheckCircle else Icons.Default.Cancel,
                contentDescription = if (session.completed) "COMPLETED" else "STOPPED",
                modifier = Modifier.size(20.dp),
                tint = if (session.completed) Color(0xFF4CAF50).copy(0.9f) else MaterialTheme.colorScheme.error.copy(0.9f)
            )
        }
    }
}
