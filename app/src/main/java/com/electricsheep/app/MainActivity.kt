package com.electricsheep.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.electricsheep.app.ui.navigation.AppNavGraph
import com.electricsheep.app.ui.theme.ElectricSheepTheme
import com.electricsheep.app.util.Logger
import io.github.jan.supabase.gotrue.handleDeeplinks
import io.github.jan.supabase.gotrue.user.UserSession
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.info("MainActivity", "Application starting")
        
        // Handle OAuth callback from deep link
        handleOAuthCallback(intent)
        
        setContent {
            ElectricSheepTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    AppNavGraph(navController = navController)
                }
            }
        }
        
        Logger.debug("MainActivity", "UI content set successfully")
    }
    
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        // Handle OAuth callback when activity is already running
        intent?.let { handleOAuthCallback(it) }
    }
    
    /**
     * Handle OAuth callback from deep link using Supabase SDK's native method.
     * The SDK handles PKCE verification, state validation, and session creation automatically.
     */
    private fun handleOAuthCallback(intent: Intent?) {
        intent?.data?.let { uri ->
            if (uri.scheme == "com.electricsheep.app" && uri.host == "auth-callback") {
                Logger.info("MainActivity", "OAuth callback received: $uri")
                
                val application = applicationContext as ElectricSheepApplication
                val supabaseClient = application.getSupabaseClient()
                
                if (supabaseClient != null) {
                    // Use Supabase SDK's native handleDeeplinks extension function
                    // This processes the deep link and completes the OAuth flow automatically
                    // It handles PKCE verification, state validation, and session creation
                    // The callback receives the UserSession when authentication completes
                    Logger.debug("MainActivity", "Processing OAuth callback with SDK's handleDeeplinks()")
                    // Call handleDeeplinks - it's a top-level function that takes SupabaseClient as first parameter
                    // In Kotlin, this appears as an extension function: supabaseClient.handleDeeplinks(intent) { session -> }
                    supabaseClient.handleDeeplinks(intent) { session: UserSession ->
                        Logger.info("MainActivity", "OAuth callback processed successfully by SDK - Session created")
                        // The SDK has created the session, now retrieve the user
                        lifecycleScope.launch {
                            val userManager = application.getUserManager()
                            userManager.handleOAuthCallback(uri)
                                .onSuccess { user ->
                                    Logger.info("MainActivity", "OAuth callback successful: ${user.id}")
                                    // UI will automatically update via StateFlow in ViewModel
                                }
                                .onFailure { error ->
                                    Logger.error("MainActivity", "OAuth callback failed", error)
                                    // Error is already logged, UI can show error via ViewModel state if needed
                                }
                        }
                    }
                } else {
                    Logger.error("MainActivity", "Supabase client is null - cannot process OAuth callback")
                }
            }
        }
    }
}

