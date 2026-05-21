package com.example.waqt.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.filled.ViewTimeline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.material.icons.outlined.ViewTimeline
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.waqt.ui.components.WaqtFloatingNavBar
import com.example.waqt.ui.components.WaqtGradientBackground
import com.example.waqt.ui.screens.HomeScreen
import com.example.waqt.ui.screens.PlannerScreen
import com.example.waqt.ui.screens.QiblaScreen
import com.example.waqt.ui.screens.SettingsScreen

sealed class WaqtDestination(
    val route: String,
    val label: String,
    val outlinedIcon: ImageVector,
    val filledIcon: ImageVector
) {
    data object Home : WaqtDestination("home", "Home", Icons.Outlined.Home, Icons.Filled.Home)
    data object Planner : WaqtDestination("planner", "Planner", Icons.Outlined.ViewTimeline, Icons.Filled.ViewTimeline)
    data object Qibla : WaqtDestination("qibla", "Qibla", Icons.Outlined.TravelExplore, Icons.Filled.TravelExplore)
    data object Settings : WaqtDestination("settings", "Settings", Icons.Outlined.Settings, Icons.Filled.Settings)
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

    val navItems = bottomDestinations.map { dest ->
        Triple(
            dest.route,
            dest.label,
            if (currentRoute == dest.route) dest.filledIcon else dest.outlinedIcon
        )
    }

    WaqtGradientBackground(modifier = modifier) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            bottomBar = {
                WaqtFloatingNavBar(
                    destinations = navItems,
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        if (currentRoute != route) {
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = WaqtDestination.Home.route,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                composable(WaqtDestination.Home.route) {
                    HomeScreen(
                        onViewPlanner = {
                            navController.navigate(WaqtDestination.Planner.route) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable(WaqtDestination.Planner.route) { PlannerScreen() }
                composable(WaqtDestination.Qibla.route) { QiblaScreen() }
                composable(WaqtDestination.Settings.route) { SettingsScreen() }
            }
        }
    }
}
