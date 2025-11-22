package com.electricsheep.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.electricsheep.app.ElectricSheepApplication
import com.electricsheep.app.auth.MfaManager
import com.electricsheep.app.auth.SupabaseAuthProvider
import com.electricsheep.app.auth.User
import com.electricsheep.app.ui.screens.LandingScreen
import com.electricsheep.app.ui.screens.mood.MoodManagementScreen
import com.electricsheep.app.ui.screens.mood.MoodManagementViewModel
import com.electricsheep.app.ui.screens.mood.MoodManagementViewModelFactory
import com.electricsheep.app.ui.screens.mfa.MfaSetupScreen
import com.electricsheep.app.ui.screens.mfa.MfaSetupViewModelFactory
import com.electricsheep.app.ui.screens.mfa.MfaVerifyScreen
import com.electricsheep.app.ui.screens.mfa.MfaVerifyViewModelFactory
import com.electricsheep.app.ui.screens.trivia.TriviaScreen
import com.electricsheep.app.util.Logger

sealed class Screen(val route: String) {
    object Landing : Screen("landing")
    object MoodManagement : Screen("mood_management")
    object Trivia : Screen("trivia")
    object MfaSetup : Screen("mfa_setup")
    
    companion object {
        // MFA verify route with parameters
        const val MfaVerifyRoute = "mfa_verify/{challengeId}/{userId}"
        fun createMfaVerifyRoute(challengeId: String, userId: String) = 
            "mfa_verify/$challengeId/$userId"
    }
}

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Landing.route
    ) {
        composable(Screen.Landing.route) {
            val context = LocalContext.current
            val application = context.applicationContext as ElectricSheepApplication
            LandingScreen(
                onNavigateToMoodManagement = {
                    Logger.info("NavGraph", "Navigating to Mood Management screen")
                    navController.navigate(Screen.MoodManagement.route)
                },
                onNavigateToTrivia = {
                    Logger.info("NavGraph", "Navigating to Trivia screen")
                    navController.navigate(Screen.Trivia.route)
                },
                application = application
            )
        }
        composable(Screen.MoodManagement.route) {
            val context = LocalContext.current
            val application = context.applicationContext as ElectricSheepApplication
            val userManager = application.getUserManager()
            val moodRepository = application.getMoodRepository()
            
            val viewModel: MoodManagementViewModel = viewModel(
                factory = MoodManagementViewModelFactory(userManager, moodRepository)
            )
            
            MoodManagementScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    Logger.debug("NavGraph", "Navigating back from Mood Management screen")
                    navController.popBackStack()
                },
                onNavigateToMfaVerify = { challengeId, userId ->
                    Logger.info("NavGraph", "Navigating to MFA verify screen: challengeId=$challengeId, userId=$userId")
                    navController.navigate(Screen.createMfaVerifyRoute(challengeId, userId))
                }
            )
        }
        composable(Screen.Trivia.route) {
            val context = LocalContext.current
            val application = context.applicationContext as ElectricSheepApplication
            
            TriviaScreen(
                application = application,
                onNavigateBack = {
                    Logger.debug("NavGraph", "Navigating back from Trivia screen")
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.MfaSetup.route) {
            val context = LocalContext.current
            val application = context.applicationContext as ElectricSheepApplication
            val mfaManager = application.getMfaManager()
            
            if (mfaManager != null) {
                MfaSetupScreen(
                    mfaManager = mfaManager,
                    onEnrollmentComplete = {
                        Logger.info("NavGraph", "MFA enrollment complete, navigating back")
                        navController.popBackStack()
                    },
                    onNavigateBack = {
                        Logger.debug("NavGraph", "Navigating back from MFA setup")
                        navController.popBackStack()
                    }
                )
            } else {
                // MFA not available (offline mode or Supabase not configured)
                // Show error or navigate back
                Logger.warn("NavGraph", "MFA not available - MfaManager is null")
                navController.popBackStack()
            }
        }
        
        composable(
            route = Screen.MfaVerifyRoute,
            arguments = listOf(
                navArgument("challengeId") {
                    type = NavType.StringType
                },
                navArgument("userId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val context = LocalContext.current
            val application = context.applicationContext as ElectricSheepApplication
            val authProvider = application.getAuthProvider()
            val challengeId = backStackEntry.arguments?.getString("challengeId") ?: ""
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            
            if (authProvider is SupabaseAuthProvider && challengeId.isNotEmpty() && userId.isNotEmpty()) {
                MfaVerifyScreen(
                    authProvider = authProvider,
                    challengeId = challengeId,
                    userId = userId,
                    onVerificationComplete = { user ->
                        Logger.info("NavGraph", "MFA verification complete, user signed in: ${user.id}")
                        // Navigate to main screen (mood management or landing)
                        navController.navigate(Screen.MoodManagement.route) {
                            // Clear back stack to prevent going back to login
                            popUpTo(Screen.Landing.route) { inclusive = true }
                        }
                    },
                    onNavigateBack = {
                        Logger.debug("NavGraph", "Navigating back from MFA verify")
                        navController.popBackStack()
                    }
                )
            } else {
                Logger.error("NavGraph", "MFA verify screen: Invalid parameters or auth provider")
                navController.popBackStack()
            }
        }
    }
}

