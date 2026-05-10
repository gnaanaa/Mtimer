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
import androidx.compose.ui.text.style.TextOverflow
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun SessionHistoryScreen(
    onBack: () -> Unit,
    onOpenDrawer: () -> Unit,
    viewModel: SessionHistoryViewModel = hiltViewModel()
) {
    val groupedSessions by viewModel.groupedSessions.collectAsState()
    val sessionCount by viewModel.sessionCount.collectAsState()
    val totalDuration by viewModel.totalDuration.collectAsState()
    val chartData by viewModel.chartData.collectAsState()
    var selectedSession by remember { mutableStateOf<Session?>(null) }

    // State to keep track of expanded/collapsed months
    val expandedStates = remember { mutableStateMapOf<String, Boolean>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "HISTORY",
                        fontFamily = DotMatrix,
                        letterSpacing = 4.sp
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
        if (groupedSessions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "NO SESSIONS RECORDED YET",
                    fontFamily = DotMatrix,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    PracticeSummaryCard(sessionCount, totalDuration)
                    
                    chartData?.let { data ->
                        Spacer(modifier = Modifier.height(24.dp))
                        WeeklyStatsRow(
                            thisWeek = data.thisWeekMinutes,
                            bestWeek = data.bestWeekMinutes,
                            avg = data.averageMinutes
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        WeeklyMinutesChart(
                            minutes = data.weeklyMinutes,
                            labels = data.labels,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
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
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = monthYear,
                                        fontFamily = DotMatrix,
                                        fontSize = 12.sp,
                                        letterSpacing = 2.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    if (!isExpanded) {
                                        val totalSeconds = sessionsInGroup.sumOf { it.durationSeconds.toLong() }
                                        val hours = totalSeconds / 3600
                                        val minutes = (totalSeconds % 3600) / 60
                                        val summary = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
                                        
                                        Text(
                                            text = "${sessionsInGroup.size} SESSIONS • $summary",
                                            fontFamily = InterFont,
                                            fontSize = 10.sp,
                                            letterSpacing = 0.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    }
                                }

                                Icon(
                                    imageVector = if (isExpanded) 
                                        Icons.Default.KeyboardArrowUp 
                                    else 
                                        Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    if (isExpanded) {
                        items(sessionsInGroup, key = { it.id }) { session ->
                            SessionItem(
                                session = session,
                                onClick = { selectedSession = session }
                            )
                        }
                        
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }

    selectedSession?.let {
        SessionDetailDialog(it) { selectedSession = null }
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
        StatItem("THIS WEEK", "${thisWeek}M")
        StatItem("BEST WEEK", "${bestWeek}M")
        StatItem("12W AVG", "${avg}M")
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontFamily = DotMatrix,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            fontFamily = DotMatrix,
            fontSize = 9.sp,
            letterSpacing = 1.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun PracticeSummaryCard(count: Int, totalSeconds: Long) {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "LIFETIME PRACTICE",
                fontFamily = DotMatrix,
                fontSize = 12.sp,
                letterSpacing = 2.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "${hours}h ${minutes}m",
                        fontFamily = DotMatrix,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "TOTAL TIME",
                        fontFamily = DotMatrix,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = count.toString(),
                        fontFamily = DotMatrix,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "SESSIONS",
                        fontFamily = DotMatrix,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun SessionItem(session: Session, onClick: () -> Unit) {
    val dateFormat = remember { SimpleDateFormat("MMM dd • HH:mm", Locale.getDefault()) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = (session.presetName ?: "MEDITATION").uppercase(),
                fontFamily = DotMatrix,
                fontSize = 12.sp,
                letterSpacing = 1.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = dateFormat.format(Date(session.startTime)).uppercase().alignColons(),
                fontFamily = InterFont,
                fontSize = 12.sp,
                letterSpacing = 0.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.95f)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = formatDurationAligned(session.durationSeconds),
                fontFamily = DotMatrix,
                fontSize = 18.sp,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.width(12.dp))

            Icon(
                imageVector = if (session.completed) Icons.Default.CheckCircle else Icons.Default.Cancel,
                contentDescription = if (session.completed) "COMPLETED" else "STOPPED",
                modifier = Modifier.size(20.dp),
                tint = if (session.completed) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
            )
        }
    }
}
