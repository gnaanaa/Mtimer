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
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.gnaanaa.mtimer.ui.components.ContextualHint
import com.gnaanaa.mtimer.ui.home.DotMatrix
import com.gnaanaa.mtimer.ui.home.InterFont
import com.gnaanaa.mtimer.ui.home.styleDottedDigits
import com.gnaanaa.mtimer.ui.theme.ThemeMode
import com.gnaanaa.mtimer.ui.theme.Spacing
import com.gnaanaa.mtimer.ui.theme.Radius
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
    val themeMode by viewModel.themeMode.collectAsState()
    val healthConnectGranted by viewModel.healthConnectPermissionsGranted.collectAsState()
    val healthConnectEnabled by viewModel.isHealthConnectEnabled.collectAsState()
    
    // Debug logging
    android.util.Log.d("HealthConnectUI", "healthConnectEnabled: $healthConnectEnabled, healthConnectGranted: $healthConnectGranted")
    
    val sdkStatus by viewModel.sdkStatus.collectAsState()
    val googleAccount by viewModel.googleAccount.collectAsState()
    val googleFitEnabled by viewModel.isGoogleFitEnabled.collectAsState()
    val showSettingsHint by viewModel.showSettingsHint.collectAsState()
    val importStatus by viewModel.importStatus.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

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
    ) { granted ->
        viewModel.checkPermissions(context)
        // If user just came back from granting permissions, the feature should be enabled
        if (granted.isNotEmpty()) {
            viewModel.toggleHealthConnect(context, true)
        }
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
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "SETTINGS",
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
        val isDark = MaterialTheme.colorScheme.background.run { (red + green + blue) < 0.5 }
        
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = Spacing.medium)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.medium)
        ) {
            ContextualHint(
                text = "Manage your theme, health integrations, and data backups here.",
                isVisible = showSettingsHint,
                onDismiss = { viewModel.dismissSettingsHint() }
            )

            Spacer(Modifier.height(Spacing.tiny))

            SettingsSectionLabel("APPEARANCE", Icons.Default.Palette)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Radius.large),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) 
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(modifier = Modifier.padding(Spacing.medium)) {
                    Text(
                        text = "THEME MODE",
                        fontFamily = InterFont,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Choose how MTimer appears on your device.",
                        fontFamily = InterFont,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(Spacing.small))
                    
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ThemeMode.entries.forEachIndexed { index, mode ->
                            SegmentedButton(
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = ThemeMode.entries.size),
                                onClick = { viewModel.setThemeMode(mode) },
                                selected = themeMode == mode,
                                label = {
                                    Text(
                                        text = when(mode) {
                                            ThemeMode.FOLLOW_SYSTEM -> "SYSTEM"
                                            ThemeMode.LIGHT -> "LIGHT"
                                            ThemeMode.DARK -> "DARK"
                                        },
                                        fontFamily = InterFont,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            )
                        }
                    }
                }
            }

            SettingsSectionLabel("SYNC & INTEGRATIONS", Icons.Default.Sync)

            SettingsToggleCard(
                title = "HEALTH CONNECT",
                subtitle = "Share mindfulness data with other health apps.",
                checked = healthConnectEnabled,
                onCheckedChange = { enabled ->
                    if (enabled) {
                        // When turning ON, always check/request permissions first.
                        // The launcher callback or checkPermissions will handle the DataStore sync.
                        if (healthConnectGranted) {
                            viewModel.toggleHealthConnect(context, true)
                        } else {
                            permissionsLauncher.launch(viewModel.permissions)
                        }
                    } else {
                        // When turning OFF, just update DataStore.
                        viewModel.toggleHealthConnect(context, false)
                    }
                },
                enabled = viewModel.isHealthConnectAvailable,
                onContent = {
                    if (viewModel.isHealthConnectAvailable) {
                        Spacer(modifier = Modifier.height(Spacing.micro))
                        Text(
                            text = "To fully revoke access, use the button below to open system settings and remove MTimer's permissions.",
                            fontFamily = InterFont,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            modifier = Modifier.padding(bottom = Spacing.micro)
                        )
                        OutlinedButton(
                            onClick = { viewModel.openHC(context) },
                            shape = RoundedCornerShape(Radius.small),
                            modifier = Modifier.height(32.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = Spacing.small, vertical = 0.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF007BFF).copy(alpha = 0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF007BFF))
                        ) {
                            Text("MANAGE PERMISSIONS", fontFamily = InterFont, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            )

            SettingsToggleCard(
                title = "GOOGLE FIT",
                subtitle = "Log meditation as activity for third-party programs.",
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

            SettingsSectionLabel("GOOGLE ACCOUNT", Icons.Default.AccountCircle)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Radius.large),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) 
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(Spacing.medium)
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
                            Spacer(modifier = Modifier.width(Spacing.medium))
                            Column {
                                Text(
                                    text = (googleAccount?.displayName ?: "USER").uppercase(),
                                    fontFamily = InterFont,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = (googleAccount?.email ?: "").styleDottedDigits(),
                                    fontFamily = InterFont,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(Spacing.medium))
                        Text(
                            text = "Connected for cloud backup of presets and history.",
                            fontFamily = InterFont,
                            fontSize = 13.sp,
                            letterSpacing = 0.5.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                        )
                        Spacer(modifier = Modifier.height(Spacing.medium))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val electricBlue = Color(0xFF007BFF)
                            val softRed = Color(0xFFEF5350)
                            
                            OutlinedButton(
                                onClick = { viewModel.syncDrive(context) },
                                shape = RoundedCornerShape(Radius.small),
                                modifier = Modifier.height(32.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = Spacing.small, vertical = 0.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, electricBlue.copy(alpha = 0.5f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = electricBlue)
                            ) {
                                Text("FORCE CLOUD SYNC", fontFamily = InterFont, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = {
                                    googleSignInClient.signOut().addOnCompleteListener {
                                        viewModel.updateGoogleAccount(context, null)
                                    }
                                },
                                shape = RoundedCornerShape(Radius.small),
                                modifier = Modifier.height(32.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = Spacing.small, vertical = 0.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, softRed.copy(alpha = 0.5f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = softRed)
                            ) {
                                Text("SIGN OUT", fontFamily = InterFont, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Text(
                            text = "Sign in to sync your presets and history across devices via Google Drive.",
                            fontFamily = InterFont,
                            fontSize = 13.sp,
                            letterSpacing = 0.5.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                        )
                        Spacer(modifier = Modifier.height(Spacing.medium))
                        val meditationGreen = Color(0xFF4CAF50)
                        Button(
                            onClick = {
                                googleSignInLauncher.launch(googleSignInClient.signInIntent)
                            },
                            modifier = Modifier.align(Alignment.End).height(32.dp),
                            shape = RoundedCornerShape(Radius.small),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = Spacing.small, vertical = 0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = meditationGreen)
                        ) {
                            Text("SIGN IN", fontFamily = InterFont, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            SettingsSectionLabel("BACKUP & RESTORE", Icons.Default.Storage)

            Text(
                text = "Export your full history and presets as a standard JSON file. This file is unencrypted for interoperability — please store it securely.",
                fontFamily = InterFont,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                modifier = Modifier.padding(bottom = Spacing.micro)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.tiny)
            ) {
                OutlinedButton(
                    onClick = { exportLauncher.launch("mtimer_backup.json") },
                    modifier = Modifier.weight(1f).height(32.dp),
                    shape = RoundedCornerShape(Radius.small),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = Spacing.small, vertical = 0.dp)
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(Spacing.tiny))
                    Text("EXPORT", fontFamily = InterFont, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = { importLauncher.launch(arrayOf("application/json", "application/octet-stream")) },
                    modifier = Modifier.weight(1f).height(32.dp),
                    shape = RoundedCornerShape(Radius.small),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = Spacing.small, vertical = 0.dp)
                ) {
                    Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(Spacing.tiny))
                    Text("IMPORT", fontFamily = InterFont, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(Spacing.large))
        }
    }
}

@Composable
private fun SettingsSectionLabel(text: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = Spacing.tiny, bottom = Spacing.micro)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(Spacing.small))
        Text(
            text = text,
            fontFamily = InterFont,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
        )
    }
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
    val isDark = MaterialTheme.colorScheme.background.run { (red + green + blue) < 0.5 }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Radius.large),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) 
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(Spacing.medium)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontFamily = InterFont,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                    Text(
                        text = subtitle,
                        fontFamily = InterFont,
                        fontSize = 13.sp,
                        letterSpacing = 0.5.sp,
                        color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
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
