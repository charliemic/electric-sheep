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
     * Handle OAuth callback from deep link.
     * Called when user returns from OAuth provider (e.g., Google sign-in).
     */
    private fun handleOAuthCallback(intent: Intent?) {
        intent?.data?.let { uri ->
            if (uri.scheme == "com.electricsheep.app" && uri.host == "auth-callback") {
                Logger.info("MainActivity", "OAuth callback received: $uri")
                
                val application = applicationContext as ElectricSheepApplication
                val userManager = application.getUserManager()
                
                lifecycleScope.launch {
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
        }
    }
}

