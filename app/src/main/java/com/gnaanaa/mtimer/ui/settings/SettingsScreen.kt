package com.gnaanaa.mtimer.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.AsyncImage
import com.gnaanaa.mtimer.ui.home.DotMatrix
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenDrawer: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val useLightTheme by viewModel.useLightTheme.collectAsState()
    val healthConnectGranted by viewModel.healthConnectPermissionsGranted.collectAsState()
    val healthConnectEnabled by viewModel.isHealthConnectEnabled.collectAsState()
    
    // Debug logging
    android.util.Log.d("HealthConnectUI", "healthConnectEnabled: $healthConnectEnabled, healthConnectGranted: $healthConnectGranted")
    
    val sdkStatus by viewModel.sdkStatus.collectAsState()
    val googleAccount by viewModel.googleAccount.collectAsState()
    val googleFitEnabled by viewModel.isGoogleFitEnabled.collectAsState()
    val importStatus by viewModel.importStatus.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Sync DataStore state with actual System Permission state
    LaunchedEffect(healthConnectGranted) {
        if (!healthConnectGranted && healthConnectEnabled) {
            android.util.Log.d("HealthConnectUI", "Permissions revoked via system, disabling feature in app.")
            viewModel.toggleHealthConnect(context, false)
        }
    }

    LaunchedEffect(importStatus) {
        importStatus?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_LONG).show()
            viewModel.clearImportStatus()
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { viewModel.exportPresets(context, it) }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importPresets(context, it) }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkPermissions(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val permissionsLauncher = rememberLauncherForActivityResult(
        PermissionController.createRequestPermissionResultContract()
    ) {
        viewModel.checkPermissions(context)
    }

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
        .build()
    val googleSignInClient = GoogleSignIn.getClient(context, gso)

    val googleSignInLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            android.util.Log.d("GoogleSignIn", "Sign in success: ${account.email}")
            viewModel.updateGoogleAccount(context, account)
        } catch (e: ApiException) {
            android.util.Log.e("GoogleSignIn", "Sign in failed: status=${e.statusCode}, message=${e.message}")
            android.widget.Toast.makeText(
                context,
                "Sign-in failed (Error ${e.statusCode}). Check console configuration.",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "SETTINGS",
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
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            SettingsSectionLabel("APPEARANCE")

            SettingsToggleCard(
                title = "LIGHT THEME",
                subtitle = "Use light colors instead of true black",
                checked = useLightTheme,
                onCheckedChange = { viewModel.toggleTheme(it) }
            )

            SettingsSectionLabel("SYNC & INTEGRATIONS")

            SettingsToggleCard(
                title = "HEALTH CONNECT",
                subtitle = "Share mindfulness data with other health apps.",
                checked = healthConnectEnabled,
                onCheckedChange = { enabled ->
                    android.util.Log.d("HealthConnectUI", "Toggle changed to: $enabled")
                    viewModel.toggleHealthConnect(context, enabled)
                    if (enabled && !healthConnectGranted) {
                        android.util.Log.d("HealthConnectUI", "Requesting permissions...")
                        permissionsLauncher.launch(viewModel.permissions)
                    }
                },
                enabled = viewModel.isHealthConnectAvailable,
                onContent = {
                    if (viewModel.isHealthConnectAvailable) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "To fully revoke access, use the button below to open system settings and remove MTimer's permissions.",
                            fontFamily = DotMatrix,
                            fontSize = 10.sp,
                            lineHeight = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        OutlinedButton(
                            onClick = { viewModel.openHC(context) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(32.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                        ) {
                            Text("MANAGE PERMISSIONS", fontFamily = DotMatrix, fontSize = 10.sp)
                        }
                    }
                }
            )

            SettingsToggleCard(
                title = "GOOGLE FIT",
                subtitle = "Log meditation as activity for AIA Vitality and others.",
                checked = googleFitEnabled,
                onCheckedChange = { enabled ->
                    viewModel.toggleGoogleFit(enabled)
                    if (enabled) {
                        val fitnessOptions = com.gnaanaa.mtimer.data.sync.getGoogleFitOptions()
                        val account = GoogleSignIn.getAccountForExtension(context, fitnessOptions)
                        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
                            GoogleSignIn.requestPermissions(
                                context as android.app.Activity,
                                1001,
                                account,
                                fitnessOptions
                            )
                        }
                    }
                }
            )

            SettingsSectionLabel("GOOGLE ACCOUNT")

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    if (googleAccount != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = googleAccount?.photoUrl,
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = (googleAccount?.displayName ?: "USER").uppercase(),
                                    fontFamily = DotMatrix,
                                    fontSize = 14.sp,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = googleAccount?.email ?: "",
                                    fontFamily = DotMatrix,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.95f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Connected for cloud backup of presets and history.",
                            fontFamily = DotMatrix,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 1.0f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.syncDrive(context) },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("FORCE CLOUD SYNC", fontFamily = DotMatrix, fontSize = 12.sp)
                            }
                            OutlinedButton(
                                onClick = {
                                    googleSignInClient.signOut().addOnCompleteListener {
                                        viewModel.updateGoogleAccount(context, null)
                                    }
                                },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("SIGN OUT", fontFamily = DotMatrix, fontSize = 12.sp)
                            }
                        }
                    } else {
                        Text(
                            text = "Sign in to sync your presets and history across devices via Google Drive.",
                            fontFamily = DotMatrix,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 1.0f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                googleSignInLauncher.launch(googleSignInClient.signInIntent)
                            },
                            modifier = Modifier.align(Alignment.End),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("SIGN IN", fontFamily = DotMatrix, fontSize = 12.sp)
                        }
                    }
                }
            }

            SettingsSectionLabel("BACKUP & RESTORE")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { exportLauncher.launch("mtimer_backup.json") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("EXPORT", fontFamily = DotMatrix, fontSize = 12.sp)
                }
                OutlinedButton(
                    onClick = { importLauncher.launch(arrayOf("application/json", "application/octet-stream")) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("IMPORT", fontFamily = DotMatrix, fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SettingsSectionLabel(text: String) {
    Text(
        text = text,
        fontFamily = DotMatrix,
        fontSize = 12.sp,
        letterSpacing = 3.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary, // Brighter and bolder
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
private fun SettingsToggleCard(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    onContent: @Composable (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontFamily = DotMatrix,
                        fontSize = 14.sp,
                        letterSpacing = 1.sp,
                        color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                    Text(
                        text = subtitle,
                        fontFamily = DotMatrix,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp,
                        color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 1.0f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                    )
                }
                Switch(
                    checked = checked,
                    onCheckedChange = onCheckedChange,
                    enabled = enabled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
            onContent?.invoke()
        }
    }
}
