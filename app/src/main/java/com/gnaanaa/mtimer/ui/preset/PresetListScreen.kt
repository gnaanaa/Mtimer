package com.gnaanaa.mtimer.ui.preset

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gnaanaa.mtimer.R
import com.gnaanaa.mtimer.domain.model.Preset
import com.gnaanaa.mtimer.ui.home.DotMatrix
import com.gnaanaa.mtimer.ui.home.InterFont
import com.gnaanaa.mtimer.ui.home.styleDottedDigits
import com.gnaanaa.mtimer.ui.components.ContextualHint
import com.gnaanaa.mtimer.ui.theme.Spacing
import com.gnaanaa.mtimer.ui.theme.Radius
import androidx.compose.ui.zIndex

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresetListScreen(
    onBack: () -> Unit,
    onEditPreset: (String) -> Unit,
    onCreatePreset: () -> Unit,
    onStartTimer: () -> Unit,
    onOpenDrawer: () -> Unit,
    viewModel: PresetViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val presets = uiState.presets
    val showPresetsHint by viewModel.showPresetsHint.collectAsState()
    var presetToDelete by remember { mutableStateOf<Preset?>(null) }

        Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "PRESETS",
                            fontFamily = DotMatrix,
                            letterSpacing = 4.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${presets.size} CONFIGURED".styleDottedDigits(),
                            fontFamily = InterFont,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(0.7f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                }
            )
        },
        floatingActionButton = {
            val isDark = MaterialTheme.colorScheme.background.run { (red + green + blue) < 0.5 }
            FloatingActionButton(
                onClick = onCreatePreset,
                containerColor = if (isDark) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primary,
                contentColor = if (isDark) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Preset")
            }
        }
    ) { padding ->
        if (presets.isEmpty() && !uiState.isLoading) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "NO PRESETS YET",
                    fontFamily = InterFont,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(0.5f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(horizontal = Spacing.medium),
                verticalArrangement = Arrangement.spacedBy(Spacing.tiny),
                contentPadding = PaddingValues(vertical = Spacing.medium)
            ) {
                item {
                    ContextualHint(
                        text = "Create custom timers for different meditation techniques here.",
                        isVisible = showPresetsHint,
                        onDismiss = { viewModel.dismissPresetsHint() }
                    )
                }

                items(presets, key = { it.id }) { preset ->
                    PresetItem(
                        preset = preset,
                        onEdit = { onEditPreset(preset.id) },
                        onStart = {
                            viewModel.startTimer(preset)
                            onStartTimer()
                        },
                        onDelete = { presetToDelete = preset }
                    )
                }
            }
        }
    }

    presetToDelete?.let { preset ->
        AlertDialog(
            onDismissRequest = { presetToDelete = null },
            shape = RoundedCornerShape(Radius.large),
            title = { 
                Text(
                    "DELETE PRESET", 
                    fontFamily = InterFont, 
                    fontWeight = FontWeight.Bold, 
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.primary
                ) 
            },
            text = { 
                Text(
                    "Are you sure you want to delete \"${preset.name}\"?",
                    fontFamily = InterFont,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                ) 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deletePreset(preset)
                        presetToDelete = null
                    }
                ) {
                    val deleteRed = Color(0xFFF44336)
                    Text(
                        "DELETE", 
                        color = deleteRed, 
                        fontFamily = InterFont, 
                        fontWeight = FontWeight.Bold, 
                        letterSpacing = 1.sp
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { presetToDelete = null }) {
                    Text(
                        "CANCEL", 
                        fontFamily = InterFont, 
                        fontWeight = FontWeight.Bold, 
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                }
            }
        )
    }
}

@Composable
fun PresetItem(
    preset: Preset,
    onEdit: () -> Unit,
    onStart: () -> Unit,
    onDelete: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.run { (red + green + blue) < 0.5 }
    val meditationGreen = Color(0xFF4CAF50)
    val electricBlue = Color(0xFF2196F3)
    val deleteRed = Color(0xFFF44336)
    val primaryColor = MaterialTheme.colorScheme.primary
    
    val accentColor = if (isDark) meditationGreen else primaryColor

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp) // Consistent with HistoryRow
            .background(
                if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) 
                else primaryColor.copy(alpha = 0.04f),
                shape = RoundedCornerShape(Radius.medium)
            )
            .border(
                width = 1.dp,
                color = primaryColor.copy(alpha = if (isDark) 0.1f else 0.08f),
                shape = RoundedCornerShape(Radius.medium)
            )
            .clip(RoundedCornerShape(Radius.medium))
            .clickable(onClick = onStart),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(start = Spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Lotus icon
            Icon(
                imageVector = Icons.Default.Spa,
                contentDescription = null,
                modifier = Modifier.size(26.dp),
                tint = accentColor
            )

            Spacer(Modifier.width(Spacing.medium))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    preset.name.uppercase(),
                    fontFamily = InterFont,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
                )
                val mins = preset.durationSeconds / 60
                val secs = preset.durationSeconds % 60
                val durationLabel = if (secs == 0) "${mins}M" else "${mins}M ${secs}S"
                Text(
                    text = durationLabel.styleDottedDigits(),
                    fontFamily = InterFont,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                    color = accentColor.copy(alpha = 0.9f)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.tiny),
                modifier = Modifier.padding(end = Spacing.tiny)
            ) {
                // Edit Button
                Surface(
                    onClick = onEdit,
                    modifier = Modifier.size(36.dp),
                    shape = CircleShape,
                    color = electricBlue.copy(alpha = if (isDark) 0.15f else 0.1f),
                    contentColor = electricBlue.copy(alpha = if (isDark) 0.9f else 0.8f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Delete Button
                Surface(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp),
                    shape = CircleShape,
                    color = deleteRed.copy(alpha = if (isDark) 0.15f else 0.1f),
                    contentColor = deleteRed.copy(alpha = if (isDark) 0.9f else 0.8f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
