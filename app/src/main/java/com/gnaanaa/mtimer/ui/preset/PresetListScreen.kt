package com.gnaanaa.mtimer.ui.preset

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
import com.gnaanaa.mtimer.domain.model.Preset
import com.gnaanaa.mtimer.ui.home.DotMatrix
import com.gnaanaa.mtimer.ui.home.InterFont
import com.gnaanaa.mtimer.ui.home.styleDottedDigits

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
                            letterSpacing = 4.sp
                        )
                        Text(
                            text = "${presets.size} CONFIGURED".styleDottedDigits(),
                            fontFamily = InterFont,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(0.9f)
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
            FloatingActionButton(
                onClick = onCreatePreset,
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                contentColor = MaterialTheme.colorScheme.primary
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
                    color = MaterialTheme.colorScheme.onBackground.copy(0.8f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(presets, key = { it.id }) { preset ->
                    PresetItem(
                        preset = preset,
                        onClick = { onEditPreset(preset.id) },
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
            title = { Text("DELETE PRESET", fontFamily = InterFont, fontWeight = FontWeight.Bold, letterSpacing = 1.sp) },
            text = { 
                Text(
                    "Are you sure you want to delete \"${preset.name}\"?",
                    fontFamily = InterFont,
                    fontSize = 14.sp
                ) 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deletePreset(preset)
                        presetToDelete = null
                    }
                ) {
                    Text("DELETE", color = MaterialTheme.colorScheme.error, fontFamily = InterFont, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { presetToDelete = null }) {
                    Text("CANCEL", fontFamily = InterFont, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
            }
        )
    }
}

@Composable
fun PresetItem(
    preset: Preset,
    onClick: () -> Unit,
    onStart: () -> Unit,
    onDelete: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(
                primaryColor.copy(alpha = 0.07f),
                shape = MaterialTheme.shapes.large
            )
            .clip(MaterialTheme.shapes.large),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clickable(onClick = onClick)
                .padding(start = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Lotus icon
            Icon(
                imageVector = Icons.Default.Spa,
                contentDescription = null,
                modifier = Modifier.size(26.dp),
                tint = primaryColor
            )

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    preset.name.uppercase(),
                    fontFamily = InterFont,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val mins = preset.durationSeconds / 60
                val secs = preset.durationSeconds % 60
                val durationLabel = if (secs == 0) "${mins}M" else "${mins}M ${secs}S"
                Text(
                    text = durationLabel.styleDottedDigits(),
                    fontFamily = InterFont, // Base font, digits dotted
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(0.95f)
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onBackground.copy(0.6f)
                )
            }
        }

        // Start Session Button (Fixed Width, Full Height)
        val startGreen = Color(0xFF4CAF50)
        Box(
            modifier = Modifier
                .width(64.dp)
                .fillMaxHeight()
                .background(startGreen.copy(alpha = 0.15f))
                .clickable(onClick = onStart),
            contentAlignment = Alignment.Center
        ) {
            // Left Border
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp)
                    .align(Alignment.CenterStart)
                    .background(startGreen.copy(alpha = 0.2f))
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Spa,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = startGreen
                )
                val mins = preset.durationSeconds / 60
                Text(
                    text = "${mins}M".styleDottedDigits(),
                    fontFamily = InterFont,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = startGreen
                )
            }
        }
    }
}
