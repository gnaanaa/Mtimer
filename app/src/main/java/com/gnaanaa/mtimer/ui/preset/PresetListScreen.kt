package com.gnaanaa.mtimer.ui.preset

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gnaanaa.mtimer.domain.model.Preset
import com.gnaanaa.mtimer.ui.home.DotMatrix

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresetListScreen(
    onBack: () -> Unit,
    onEditPreset: (String) -> Unit,
    onCreatePreset: () -> Unit,
    onOpenDrawer: () -> Unit,
    viewModel: PresetViewModel = hiltViewModel()
) {
    val presets by viewModel.presets.collectAsState()
    var presetToDelete by remember { mutableStateOf<Preset?>(null) }

    Scaffold(
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
                            "${presets.size} CONFIGURED",
                            fontFamily = DotMatrix,
                            fontSize = 12.sp,
                            letterSpacing = 2.sp,
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
        if (presets.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "NO PRESETS YET",
                    fontFamily = DotMatrix,
                    fontSize = 12.sp,
                    letterSpacing = 3.sp,
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
                        onDelete = { presetToDelete = preset }
                    )
                }
            }
        }
    }

    presetToDelete?.let { preset ->
        AlertDialog(
            onDismissRequest = { presetToDelete = null },
            title = { Text("DELETE PRESET", fontFamily = DotMatrix, letterSpacing = 2.sp) },
            text = { 
                Text(
                    "Are you sure you want to delete \"${preset.name}\"?",
                    fontFamily = DotMatrix,
                    letterSpacing = 1.sp
                ) 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deletePreset(preset)
                        presetToDelete = null
                    }
                ) {
                    Text("DELETE", color = MaterialTheme.colorScheme.error, fontFamily = DotMatrix, letterSpacing = 2.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { presetToDelete = null }) {
                    Text("CANCEL", fontFamily = DotMatrix, letterSpacing = 2.sp)
                }
            }
        )
    }
}

@Composable
fun PresetItem(
    preset: Preset,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.primary.copy(alpha = 0.07f),
                shape = MaterialTheme.shapes.large
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Lotus icon — mirrors the start button
        Icon(
            imageVector = Icons.Default.Spa,
            contentDescription = null,
            modifier = Modifier.size(26.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                preset.name.uppercase(),
                fontFamily = DotMatrix,
                fontSize = 14.sp,
                letterSpacing = 2.sp
            )
            val mins = preset.durationSeconds / 60
            val secs = preset.durationSeconds % 60
            val durationLabel = if (secs == 0) "${mins}m" else "${mins}m ${secs}s"
            Text(
                durationLabel,
                fontFamily = DotMatrix,
                fontSize = 12.sp,
                letterSpacing = 1.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(0.95f)
            )
        }

        IconButton(onClick = onDelete) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Delete",
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onBackground.copy(0.75f)
            )
        }
    }
}