package com.gnaanaa.mtimer.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gnaanaa.mtimer.ui.about.AboutScreen
import com.gnaanaa.mtimer.ui.history.SessionHistoryScreen
import com.gnaanaa.mtimer.ui.home.DotMatrix
import com.gnaanaa.mtimer.ui.home.HomeScreen
import com.gnaanaa.mtimer.ui.howtomeditate.HowToMeditateScreen
import com.gnaanaa.mtimer.ui.preset.PresetListScreen
import com.gnaanaa.mtimer.ui.settings.SettingsScreen
import kotlinx.coroutines.launch
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Alignment
import com.google.android.play.core.review.ReviewManagerFactory
import android.app.Activity

private data class DrawerMenuItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun MainContainer(
    rootNavController: NavHostController,
    onStartTimer: () -> Unit,
    initialDrawerRoute: String = Screen.Home.route
) {
    val drawerNavController = rememberNavController()
    val navBackStackEntry by drawerNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route
    
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val reviewManager = remember { ReviewManagerFactory.create(context) }

    LaunchedEffect(initialDrawerRoute) {
        if (drawerNavController.currentDestination?.route != initialDrawerRoute) {
            drawerNavController.navigate(initialDrawerRoute) {
                popUpTo(Screen.Home.route) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            // ... (rest of the drawer content)
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.background,
                drawerShape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
            ) {
                Spacer(Modifier.height(16.dp))
                Text(
                    "MTIMER",
                    modifier = Modifier.padding(24.dp),
                    fontFamily = DotMatrix,
                    fontSize = 24.sp,
                    letterSpacing = 6.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )
                
                Spacer(Modifier.height(16.dp))
                
                val menuItems = listOf(
                    DrawerMenuItem("HOME", Icons.Default.Home, Screen.Home.route),
                    DrawerMenuItem("HISTORY", Icons.Default.History, Screen.History.route),
                    DrawerMenuItem("PRESETS", Icons.AutoMirrored.Filled.List, Screen.PresetList.route),
                    DrawerMenuItem("HOW TO MEDITATE", Icons.Default.SelfImprovement, Screen.HowToMeditate.route),
                    DrawerMenuItem("SETTINGS", Icons.Default.Settings, Screen.Settings.route),
                    DrawerMenuItem("ABOUT", Icons.Default.Info, Screen.About.route)
                )

                menuItems.forEach { item ->
                    val isSelected = currentRoute == item.route
                    
                    Surface(
                        onClick = {
                            scope.launch { drawerState.close() }
                            if (currentRoute != item.route) {
                                if (item.route == Screen.Home.route) {
                                    drawerNavController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Home.route) { inclusive = true }
                                    }
                                } else {
                                    drawerNavController.navigate(item.route)
                                }
                            }
                        },
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = MaterialTheme.shapes.large,
                        color = if (isSelected) 
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        else 
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        contentColor = if (isSelected)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = if (isSelected)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Spacer(Modifier.width(16.dp))
                            Text(
                                text = item.label,
                                fontFamily = DotMatrix,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                letterSpacing = 2.sp,
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                Spacer(Modifier.weight(1f))

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )

                // Rate this app
                DrawerActionItem(
                    label = "RATE THIS APP",
                    subtext = "HELPS US GROW",
                    icon = Icons.Default.Star,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            val request = reviewManager.requestReviewFlow()
                            request.addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val reviewInfo = task.result
                                    (context as? Activity)?.let { activity ->
                                        reviewManager.launchReviewFlow(activity, reviewInfo)
                                    }
                                }
                            }
                        }
                    }
                )

                // Support this app
                DrawerActionItem(
                    label = "SUPPORT THIS APP",
                    subtext = "NO ADS, NO SUBSCRIPTION",
                    icon = Icons.Default.Favorite,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            drawerNavController.navigate(Screen.About.route + "?scrollToSupport=true") {
                                launchSingleTop = true
                            }
                        }
                    }
                )
                Spacer(Modifier.height(16.dp))
            }
        }
    ) {
        NavHost(
            navController = drawerNavController,
            startDestination = initialDrawerRoute
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onStartTimer = onStartTimer,
                    onNavigateToPresets = { drawerNavController.navigate(Screen.PresetList.route) },
                    onNavigateToSettings = { drawerNavController.navigate(Screen.Settings.route) },
                    onNavigateToHistory = { drawerNavController.navigate(Screen.History.route) },
                    onNavigateToAbout = { drawerNavController.navigate(Screen.About.route) },
                    onOpenDrawer = { scope.launch { drawerState.open() } }
                )
            }
            composable(Screen.HowToMeditate.route) {
                HowToMeditateScreen(
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    onCreatePreset = { name, duration ->
                        rootNavController.navigate(Screen.PresetEdit.createRoute("new", name, duration))
                    }
                )
            }
            composable(Screen.History.route) {
                SessionHistoryScreen(
                    onBack = { drawerNavController.popBackStack() },
                    onOpenDrawer = { scope.launch { drawerState.open() } }
                )
            }
            composable(Screen.PresetList.route) {
                PresetListScreen(
                    onBack = { drawerNavController.popBackStack() },
                    onEditPreset = { id -> rootNavController.navigate(Screen.PresetEdit.createRoute(id)) },
                    onCreatePreset = { rootNavController.navigate(Screen.PresetEdit.createRoute("new")) },
                    onStartTimer = onStartTimer,
                    onOpenDrawer = { scope.launch { drawerState.open() } }
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onBack = { drawerNavController.popBackStack() },
                    onOpenDrawer = { scope.launch { drawerState.open() } }
                )
            }
            composable(
                route = Screen.About.route + "?scrollToSupport={scrollToSupport}",
                arguments = listOf(
                    androidx.navigation.navArgument("scrollToSupport") {
                        type = androidx.navigation.NavType.BoolType
                        defaultValue = false
                    }
                )
            ) { backStackEntry ->
                val scrollToSupport = backStackEntry.arguments?.getBoolean("scrollToSupport") ?: false
                AboutScreen(
                    onBack = { drawerNavController.popBackStack() },
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    scrollToSupport = scrollToSupport
                )
            }
            composable(Screen.About.route) {
                // Fallback for direct navigation without params
                AboutScreen(
                    onBack = { drawerNavController.popBackStack() },
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    scrollToSupport = false
                )
            }
        }
    }
}

@Composable
private fun DrawerActionItem(
    label: String,
    subtext: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 2.dp)
            .fillMaxWidth()
            .height(52.dp),
        shape = MaterialTheme.shapes.large,
        color = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = label,
                    fontFamily = DotMatrix,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 1.sp,
                    fontSize = 13.sp
                )
                Text(
                    text = subtext,
                    fontFamily = com.gnaanaa.mtimer.ui.home.InterFont,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}
