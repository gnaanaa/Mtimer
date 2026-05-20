package com.gnaanaa.mtimer.ui.about

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.android.billingclient.api.ProductDetails
import com.gnaanaa.mtimer.ui.home.DotMatrix
import com.gnaanaa.mtimer.ui.home.InterFont
import com.gnaanaa.mtimer.ui.home.styleDottedDigits
import com.gnaanaa.mtimer.ui.theme.Radius
import com.gnaanaa.mtimer.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBack: () -> Unit,
    onOpenDrawer: () -> Unit,
    scrollToSupport: Boolean = false,
    viewModel: AboutViewModel = hiltViewModel()
) {
    val uriHandler = LocalUriHandler.current
    val productDetails by viewModel.productDetails.collectAsState()
    val purchaseSuccess by viewModel.purchaseSuccess.collectAsState()
    val context = LocalContext.current
    val lazyListState = rememberLazyListState()
    var expandedIndex by remember { mutableIntStateOf(-1) }

    LaunchedEffect(scrollToSupport) {
        if (scrollToSupport) {
            kotlinx.coroutines.delay(300)
            lazyListState.animateScrollToItem(lazyListState.layoutInfo.totalItemsCount - 1)
        }
    }

    LaunchedEffect(purchaseSuccess) {
        if (purchaseSuccess) {
            android.widget.Toast.makeText(context, "Thank you. It genuinely helps.", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    // Adjust scroll when an accordion expands
    LaunchedEffect(expandedIndex) {
        if (expandedIndex != -1) {
            // Header is item 0, then accordions start at index 1
            lazyListState.animateScrollToItem(index = expandedIndex + 1)
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
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(Spacing.tiny)
        ) {
            // Header Section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Spacing.medium),
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
                Spacer(Modifier.height(Spacing.medium))
            }

            // 1. WHAT THIS IS
            item {
                AboutAccordion(
                    title = "WHAT THIS IS",
                    isExpanded = expandedIndex == 0,
                    onToggle = { expandedIndex = if (expandedIndex == 0) -1 else 0 }
                ) {
                    Text(
                        text = "MTimer is a minimal meditation timer built for people who sit seriously. Drawing inspiration from Sri M’s teachings and the ancient Nath tradition, it provides a focused environment for your daily practice.\n\nNo guided voices, no streaks, no social feeds. Just a timer, a bell, and an honest record of your journey.",
                        fontFamily = InterFont,
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                    )
                }
            }

            // 2. CORE FEATURES
            item {
                AboutAccordion(
                    title = "CORE FEATURES",
                    isExpanded = expandedIndex == 1,
                    onToggle = { expandedIndex = if (expandedIndex == 1) -1 else 1 }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                        FeaturePoint("SELECT", "Choose a preset from the home screen or the 'HOW TO MEDITATE' guide. Each preset defines your sitting time, preparation delay, and interval chimes.")
                        FeaturePoint("SIT", "Press 'START SESSION'. MTimer counts down your preparation time before the starting bell rings, letting you settle in properly.")
                        FeaturePoint("FINISH", "When the timer ends, the final bell rings and your session is saved. If you stop early, MTimer records exactly how long you actually sat.")
                        FeaturePoint("CAPTURE", "Each session records date, actual duration, completion status, and heart rate (if supported). Your data always stays on your device.")
                    }
                }
            }

            // 3. DATA & PRIVACY
            item {
                AboutAccordion(
                    title = "DATA & PRIVACY",
                    isExpanded = expandedIndex == 2,
                    onToggle = { expandedIndex = if (expandedIndex == 2) -1 else 2 }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.medium)) {
                        Text(
                            text = "MTimer prioritizes your privacy. We do not operate servers, collect personal data, or use trackers. All your meditation data stays on your device or in accounts you explicitly control (Google Drive, Health Connect).",
                            fontFamily = InterFont,
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                        )
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
                    }
                }
            }

            // 4. INTEGRATIONS
            item {
                AboutAccordion(
                    title = "INTEGRATIONS",
                    isExpanded = expandedIndex == 3,
                    onToggle = { expandedIndex = if (expandedIndex == 3) -1 else 3 }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.medium)) {
                        FeaturePoint("HEALTH CONNECT", "Writes sessions as standardised Mindfulness records. Share data with Samsung Health, Google Fit, and others. Supports heart rate sync.")
                        FeaturePoint("GOOGLE FIT", "Optional sync for compatibility with third-party reward programs that haven't moved to Health Connect yet.")
                        FeaturePoint("CLOUD BACKUP", "Uses your personal Google Drive to sync presets and history across devices. No MTimer server involved.")
                        FeaturePoint("IMPORT & EXPORT", "Your data is yours. Export full history as a standard JSON file at any time for manual backup or migration.")
                    }
                }
            }

            // 5. FAQ
            item {
                AboutAccordion(
                    title = "FAQ",
                    isExpanded = expandedIndex == 4,
                    onToggle = { expandedIndex = if (expandedIndex == 4) -1 else 4 }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.medium)) {
                        FaqItem("Does MTimer work offline?", "Yes. All your meditation data is stored locally. Syncing to Google Drive or Health Connect is entirely optional.")
                        FaqItem("Why use Health Connect?", "It allows MTimer to share your mindfulness minutes with other apps and read heart rate data from your wearable during sessions.")
                        FaqItem("How do I restore data?", "You can either use the Google Drive backup feature in Settings or manually import a JSON backup file exported from your old device.")
                        FaqItem("Is my data private?", "Absolutely. MTimer has no servers and collects no personal data. Your records stay on your device and in accounts you control.")
                    }
                }
            }

            // 6. OPEN SOURCE
            item {
                AboutAccordion(
                    title = "OPEN SOURCE & CONTACT",
                    isExpanded = expandedIndex == 5,
                    onToggle = { expandedIndex = if (expandedIndex == 5) -1 else 5 }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.medium)) {
                        Text(
                            text = "MTimer is released under the MIT License. The code and meditation guides are open and free to share.",
                            fontFamily = InterFont,
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                        )
                        FeaturePoint("CONTACT & CODE", "Found a bug or have an idea? Reach out or contribute on GitHub:\nhttps://github.com/gnaanaa/Mtimer")
                        Text(
                            text = "Copyright © 2025 MTimer contributors.",
                            fontFamily = InterFont,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Support Section (Static)
            item {
                Spacer(Modifier.height(Spacing.large))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                Spacer(Modifier.height(Spacing.large))
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
}

@Composable
private fun AboutAccordion(
    title: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.run { (red + green + blue) < 0.5 }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.nano),
        shape = RoundedCornerShape(Radius.medium),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) 
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isExpanded) 0.4f else 0.2f)
        )
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() }
                    .padding(Spacing.medium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontFamily = InterFont,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                )
            }
            
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(start = Spacing.medium, end = Spacing.medium, bottom = Spacing.medium)) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun FeaturePoint(label: String, content: String) {
    Column {
        Text(
            text = label,
            fontFamily = InterFont,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.5.sp,
            color = MaterialTheme.colorScheme.secondary
        )
        Text(
            text = content,
            fontFamily = InterFont,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
        )
    }
}

@Composable
private fun FaqItem(question: String, answer: String) {
    Column {
        Text(
            text = "Q: $question",
            fontFamily = InterFont,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(Spacing.nano))
        Text(
            text = answer,
            fontFamily = InterFont,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
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
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = Spacing.tiny)
        )
        Text(
            text = "MTimer has no ads and no subscription. If the app has been useful to your practice, a small tip helps keep the project alive.",
            fontFamily = InterFont,
            fontSize = 14.sp,
            letterSpacing = 0.5.sp,
            lineHeight = 22.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
            modifier = Modifier.padding(bottom = Spacing.large)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            TipButton(
                icon = Icons.Default.Favorite,
                price = productDetails["tip_1"]?.oneTimePurchaseOfferDetails?.formattedPrice ?: "$1",
                onClick = { onSupport("tip_1") }
            )
            TipButton(
                icon = Icons.Default.LocalCafe,
                price = productDetails["tip_3"]?.oneTimePurchaseOfferDetails?.formattedPrice ?: "$3",
                onClick = { onSupport("tip_3") }
            )
            TipButton(
                icon = Icons.Default.Cake,
                price = productDetails["tip_6"]?.oneTimePurchaseOfferDetails?.formattedPrice ?: "$6",
                onClick = { onSupport("tip_6") }
            )
        }
    }
}

@Composable
private fun TipButton(
    icon: ImageVector,
    price: String,
    onClick: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.run { (red + green + blue) < 0.5 }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            onClick = onClick,
            modifier = Modifier.size(72.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Spacer(Modifier.height(Spacing.tiny))
        Text(
            text = price.styleDottedDigits(),
            fontFamily = InterFont,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
        )
    }
}
