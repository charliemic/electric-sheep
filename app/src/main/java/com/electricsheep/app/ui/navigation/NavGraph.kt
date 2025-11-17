package com.electricsheep.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.electricsheep.app.ui.screens.LandingScreen
import com.electricsheep.app.ui.screens.mood.MoodManagementScreen
import com.electricsheep.app.util.Logger

sealed class Screen(val route: String) {
    object Landing : Screen("landing")
    object MoodManagement : Screen("mood_management")
}

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Landing.route
    ) {
        composable(Screen.Landing.route) {
            LandingScreen(
                onNavigateToMoodManagement = {
                    Logger.info("NavGraph", "Navigating to Mood Management screen")
                    navController.navigate(Screen.MoodManagement.route)
                }
            )
        }
        composable(Screen.MoodManagement.route) {
            MoodManagementScreen(
                onNavigateBack = {
                    Logger.debug("NavGraph", "Navigating back from Mood Management screen")
                    navController.popBackStack()
                }
            )
        }
    }
}

