package com.gnaanaa.mtimer.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gnaanaa.mtimer.domain.model.Session
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Custom Lotus Icon using a combination of shapes or a Material Icon approximation
val LotusIcon: ImageVector = Icons.Default.SelfImprovement

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onStartTimer: () -> Unit,
    onNavigateToPresets: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToHistory: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val recentSessions by viewModel.recentSessions.collectAsState()
    val presets by viewModel.presets.collectAsState()
    var selectedSession by remember { mutableStateOf<Session?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MTimer") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Top Section: Circular Presets (First Half)
            Column(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxWidth()
            ) {
                Text(
                    "Presets",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(16.dp),
                    fontWeight = FontWeight.Bold
                )

                if (presets.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No presets yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    // Use LazyRow for horizontal sliding if more than 4 presets
                    LazyRow(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(presets) { preset ->
                            PresetCircleItem(
                                name = preset.name,
                                duration = "${preset.durationSeconds / 60}m",
                                onClick = {
                                    viewModel.startTimer(preset)
                                    onStartTimer()
                                }
                            )
                        }
                    }
                }

                Button(
                    onClick = onNavigateToPresets,
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Configure Presets")
                }
            }

            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)

            // Bottom Section: History (Second Half)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Recent History",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = onNavigateToHistory) {
                        Text("View All")
                        Icon(Icons.Default.ChevronRight, contentDescription = null)
                    }
                }

                if (recentSessions.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No recent sessions", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(recentSessions.take(5)) { session ->
                            SessionItem(session = session, onClick = { selectedSession = session })
                        }
                    }
                }
            }
        }
    }

    // Detail Dialog
    selectedSession?.let { session ->
        SessionDetailDialog(session = session, onDismiss = { selectedSession = null })
    }
}

@Composable
fun PresetCircleItem(
    name: String,
    duration: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(100.dp)
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color.Black)
                .border(BorderStroke(2.dp, Color.DarkGray), CircleShape)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(8.dp)
            ) {
                Icon(
                    imageVector = LotusIcon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = name,
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = duration,
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun SessionItem(session: Session, onClick: () -> Unit) {
    val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(dateFormat.format(Date(session.startTime)), style = MaterialTheme.typography.bodyMedium)
                Text("${session.durationSeconds / 60} min", style = MaterialTheme.typography.bodySmall)
            }
            Icon(
                imageVector = if (session.completed) Icons.Default.CheckCircle else Icons.Default.Cancel,
                contentDescription = null,
                tint = if (session.completed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun SessionDetailDialog(session: Session, onDismiss: () -> Unit) {
    val dateFormat = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
        title = { Text("Session Details") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DetailRow("Date", dateFormat.format(Date(session.startTime)))
                DetailRow("Started", timeFormat.format(Date(session.startTime)))
                DetailRow("Duration", "${session.durationSeconds / 60}m ${session.durationSeconds % 60}s")
                DetailRow("Status", if (session.completed) "Completed" else "Stopped Early")
                if (session.healthConnectSynced) {
                    DetailRow("Sync", "Synced to Health Connect")
                }
            }
        }
    )
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value)
    }
}
