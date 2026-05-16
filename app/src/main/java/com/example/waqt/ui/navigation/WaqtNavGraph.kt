package com.example.waqt.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.material.icons.outlined.ViewTimeline
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.waqt.ui.screens.HomeScreen
import com.example.waqt.ui.screens.PlannerScreen
import com.example.waqt.ui.screens.QiblaScreen
import com.example.waqt.ui.screens.SettingsScreen

sealed class WaqtDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    data object Home : WaqtDestination("home", "Home", Icons.Outlined.Home)
    data object Planner : WaqtDestination("planner", "Planner", Icons.Outlined.ViewTimeline)
    data object Qibla : WaqtDestination("qibla", "Qibla", Icons.Outlined.TravelExplore)
    data object Settings : WaqtDestination("settings", "Settings", Icons.Outlined.Settings)
}

private val bottomDestinations = listOf(
    WaqtDestination.Home,
    WaqtDestination.Planner,
    WaqtDestination.Qibla,
    WaqtDestination.Settings
)

@Composable
fun WaqtNavGraph(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                bottomDestinations.forEach { destination ->
                    NavigationBarItem(
                        selected = currentRoute == destination.route,
                        onClick = {
                            if (currentRoute != destination.route) {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(imageVector = destination.icon, contentDescription = destination.label) },
                        label = { Text(text = destination.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = WaqtDestination.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(WaqtDestination.Home.route) { HomeScreen() }
            composable(WaqtDestination.Planner.route) { PlannerScreen() }
            composable(WaqtDestination.Qibla.route) { QiblaScreen() }
            composable(WaqtDestination.Settings.route) { SettingsScreen() }
        }
    }
}
