package com.gnaanaa.mtimer.ui.about

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gnaanaa.mtimer.ui.home.DotMatrix
import com.gnaanaa.mtimer.ui.home.InterFont
import com.gnaanaa.mtimer.ui.home.styleDottedDigits
import com.gnaanaa.mtimer.ui.theme.Spacing
import com.gnaanaa.mtimer.ui.theme.Radius
import android.app.Activity
import com.android.billingclient.api.ProductDetails

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBack: () -> Unit,
    onOpenDrawer: () -> Unit,
    scrollToSupport: Boolean = false,
    viewModel: AboutViewModel = hiltViewModel()
) {
    val uriHandler = LocalUriHandler.current
    val scrollState = rememberScrollState()
    val productDetails by viewModel.productDetails.collectAsState()
    val purchaseSuccess by viewModel.purchaseSuccess.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(scrollToSupport) {
        if (scrollToSupport) {
            kotlinx.coroutines.delay(200)
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    LaunchedEffect(purchaseSuccess) {
        if (purchaseSuccess) {
            android.widget.Toast.makeText(context, "Thank you. It genuinely helps.", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ABOUT", fontFamily = DotMatrix, letterSpacing = 3.sp, color = MaterialTheme.colorScheme.primary) },
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
                .verticalScroll(scrollState)
                .padding(Spacing.medium),
            horizontalAlignment = Alignment.Start
        ) {
            // Header Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "MTIMER",
                    fontFamily = DotMatrix,
                    fontSize = 32.sp,
                    letterSpacing = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "RETURN TO YOURSELF, DAILY.",
                    fontFamily = DotMatrix,
                    fontSize = 12.sp,
                    letterSpacing = 2.sp,
                    modifier = Modifier.padding(top = Spacing.micro)
                )
                Text(
                    text = "VERSION 1.2.0".styleDottedDigits(),
                    fontFamily = InterFont,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f),
                    modifier = Modifier.padding(top = Spacing.tiny)
                )
            }

            Spacer(Modifier.height(Spacing.huge))

            AboutSection("WHAT THIS IS", "MTimer is a minimal meditation timer built for people who sit seriously. Drawing inspiration from Sri M’s teachings and the ancient Nath tradition, it provides a focused environment for your daily practice.\n\nNo guided voices, no streaks, no social feeds. Just a timer, a bell, and an honest record of your journey.")

            AboutSection("HOW TO MEDITATE", "MTimer includes a built-in guide to classical meditation methods drawn from the Nath tradition and Sri M's teachings. From foundational breath awareness to advanced inner gazing, you can learn the techniques directly in the app. Each method in the guide includes a 'CREATE PRESET' button that automatically configures a timer with the recommended duration for that specific practice.")

            AboutSection("HOW IT WORKS", "1. SELECT — Choose a preset from the home screen or the 'HOW TO MEDITATE' guide. Each preset defines your sitting time, preparation delay, and interval chimes.\n\n2. SIT — Press 'START SESSION'. MTimer will count down your preparation time (so you can settle in) before the starting bell rings.\n\n3. FINISH — When the timer ends, the final bell rings and your session is automatically saved to your history. If you need to stop early, press 'STOP' — MTimer will still record exactly how many minutes you sat.")

            AboutSection("WHAT IT CAPTURES", "Each completed or stopped session records:\n\n• Date and start time\n• Actual duration — what you sat, not what you planned\n• Completion status\n• Preset used\n• Heart rate, if your device supports it\n\nYour data lives on your device. Always.")

            AboutSection("HEALTH CONNECT", "MTimer writes each session to Android Health Connect as a MindfulnessSessionRecord with type MEDITATION — the correct, standardised record for meditation practice. This makes your data available to any app that reads from Health Connect, including Samsung Health, Wear OS, and future integrations.\n\nPermissions requested:\n• Write Mindfulness Sessions\n• Read & Write Heart Rate\n\nNote on Permissions: Disabling the switch in Settings stops MTimer from syncing data, but Android requires you to manually revoke permissions in the system dashboard if you wish to completely disconnect the link. Use the 'Manage Permissions' button in Settings to do this.\n\nMTimer never reads your data from other apps. It only writes what it records itself.")

            AboutSection("GOOGLE FIT", "For compatibility with health reward apps that read from Google Fit rather than Health Connect, MTimer can optionally sync sessions to Google Fit using the native MEDITATION activity type — not a workaround, not labelled as exercise. A genuine meditation record.\n\nThis is opt-in. Google Fit sync can be enabled or disabled independently from Health Connect in Settings.\n\nNote: Google Fit APIs are deprecated by Google and will be shut down in 2026. Health Connect is the long-term home for your data.")

            AboutSection("GOOGLE DRIVE BACKUP", "Your session history and presets can be backed up to your personal Google Drive. Backups are stored in your own account — MTimer has no server, no cloud of its own. Restore from backup at any time, including when switching devices.")

            AboutSection("IMPORT & EXPORT", "Your data is yours. Export your full session history and presets as a JSON file at any time. This file can be used to migrate your data to a new device or kept as a personal backup. No proprietary format, no lock-in.")

            AboutSection("PERMISSIONS", "MTimer requests only what it needs:\n\n• Health Connect: Write MindfulnessRecord\n• Health Connect: Read/Write Heart Rate\n• Google Fit: Activity (Optional)\n• Google Drive: Backup (Optional)\n• Notifications: Session reminders\n\nNo location. No contacts. No microphone. No camera.")

            AboutSection("OPEN SOURCE", "MTimer is released under the MIT License.\n\nCopyright © 2025\n\nPermission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files, to deal in the software without restriction — including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies — subject to the following condition: the above copyright notice and this permission notice shall be included in all copies or substantial portions of the software.\n\nThe software is provided as is, without warranty of any kind.\n\n---\n\nThe written content in this app — including the meditation methods, practice descriptions, and instructional text — is licensed under Creative Commons Attribution 4.0 International (CC BY 4.0). You are free to share and adapt the written content for any purpose, including commercially, as long as you give appropriate credit.")

            AboutSection("PRIVACY", "MTimer prioritizes your privacy. We do not operate servers, collect personal data, or use trackers. All your meditation data stays on your device or in accounts you explicitly control (Google Drive, Health Connect).")

            Button(
                onClick = { 
                    uriHandler.openUri("https://gnaanaa.github.io/Mtimer/PRIVACY_POLICY.html") 
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Radius.medium),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    "VIEW FULL PRIVACY POLICY",
                    fontFamily = InterFont,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(Modifier.height(Spacing.huge))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            Spacer(Modifier.height(Spacing.extraLarge))

            AboutSection("CONTACT", "Ideas, bugs, or feedback — open an issue or get in touch.\n(https://github.com/gnaanaa/Mtimer)")

            Spacer(Modifier.height(Spacing.medium))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            Spacer(Modifier.height(Spacing.extraLarge))

            SupportSection(
                productDetails = productDetails,
                onSupport = { productId ->
                    (context as? Activity)?.let { viewModel.supportApp(it, productId) }
                }
            )

            Spacer(Modifier.height(Spacing.huge))
        }
    }
}

@Composable
private fun SupportSection(
    productDetails: Map<String, ProductDetails>,
    onSupport: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "SUPPORT THIS APP",
            fontFamily = InterFont,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "This app has no ads and no subscription. If it's been useful to you, a small tip helps keep it going.",
            fontFamily = InterFont,
            fontSize = 14.sp,
            letterSpacing = 0.5.sp,
            lineHeight = 22.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TipButton(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Favorite,
                price = productDetails["tip_1"]?.oneTimePurchaseOfferDetails?.formattedPrice ?: "$1",
                onClick = { onSupport("tip_1") }
            )
            TipButton(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.LocalCafe,
                price = productDetails["tip_3"]?.oneTimePurchaseOfferDetails?.formattedPrice ?: "$3",
                onClick = { onSupport("tip_3") }
            )
            TipButton(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Cake,
                price = productDetails["tip_6"]?.oneTimePurchaseOfferDetails?.formattedPrice ?: "$6",
                onClick = { onSupport("tip_6") }
            )
        }
    }
}

@Composable
private fun TipButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    price: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(68.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            contentColor = MaterialTheme.colorScheme.primary
        ),
        contentPadding = PaddingValues(4.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = price.styleDottedDigits(),
                fontFamily = InterFont,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun AboutSection(title: String, content: String) {
    Column(modifier = Modifier.padding(bottom = 32.dp)) {
        Text(
            text = title,
            fontFamily = InterFont,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = content,
            fontFamily = InterFont,
            fontSize = 14.sp,
            letterSpacing = 0.5.sp,
            lineHeight = 22.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
        )
    }
}
