package com.gnaanaa.mtimer.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gnaanaa.mtimer.ui.about.AboutScreen
import com.gnaanaa.mtimer.ui.history.SessionHistoryScreen
import com.gnaanaa.mtimer.ui.home.DotMatrix
import com.gnaanaa.mtimer.ui.home.HomeScreen
import com.gnaanaa.mtimer.ui.preset.PresetListScreen
import com.gnaanaa.mtimer.ui.settings.SettingsScreen
import kotlinx.coroutines.launch
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Alignment

private data class DrawerMenuItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun MainContainer(
    rootNavController: NavHostController,
    onStartTimer: () -> Unit
) {
    val drawerNavController = rememberNavController()
    val navBackStackEntry by drawerNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route
    
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
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
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    ) {
        NavHost(
            navController = drawerNavController,
            startDestination = Screen.Home.route
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
                    onOpenDrawer = { scope.launch { drawerState.open() } }
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onBack = { drawerNavController.popBackStack() },
                    onOpenDrawer = { scope.launch { drawerState.open() } }
                )
            }
            composable(Screen.About.route) {
                AboutScreen(
                    onBack = { drawerNavController.popBackStack() },
                    onOpenDrawer = { scope.launch { drawerState.open() } }
                )
            }
        }
    }
}
