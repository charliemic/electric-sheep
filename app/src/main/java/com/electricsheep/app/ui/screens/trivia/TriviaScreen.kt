package com.electricsheep.app.ui.screens.trivia

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.electricsheep.app.config.FeatureFlag
import com.electricsheep.app.ElectricSheepApplication
import com.electricsheep.app.ui.components.AccessibleScreen
import com.electricsheep.app.ui.theme.Spacing
import com.electricsheep.app.util.Logger

/**
 * Trivia/Pub Quiz screen.
 * Allows users to answer trivia questions and track their performance.
 * 
 * This is an initial setup - future enhancements will include:
 * - Question display and answer selection
 * - Performance tracking
 * - Difficulty adjustment based on user performance
 * - Category filtering
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TriviaScreen(
    application: ElectricSheepApplication,
    onNavigateBack: () -> Unit
) {
    Logger.info("TriviaScreen", "Trivia screen displayed")
    
    // Check feature flag - if disabled, show message and return
    val featureFlagManager = application.getFeatureFlagManager()
    val isTriviaEnabled = featureFlagManager.isEnabled(
        FeatureFlag.ENABLE_TRIVIA_APP,
        defaultValue = false
    )
    
    if (!isTriviaEnabled) {
        Logger.warn("TriviaScreen", "Trivia app is not enabled via feature flag")
        // In a real scenario, we might navigate back or show an error
        // For now, we'll show a placeholder message
    }
    
    AccessibleScreen(
        title = "Trivia Quiz",
        navigationIcon = {
            IconButton(
                onClick = {
                    Logger.info("TriviaScreen", "User tapped back button")
                    onNavigateBack()
                },
                modifier = Modifier.semantics {
                    contentDescription = "Navigate back"
                    role = Role.Button
                }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            // Welcome message
            Text(
                text = "Welcome to Trivia Quiz!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics {
                    contentDescription = "Welcome to Trivia Quiz"
                }
            )
            
            Text(
                text = "Test your knowledge with pub quiz style questions",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.semantics {
                    contentDescription = "Test your knowledge with pub quiz style questions"
                }
            )
            
            // Placeholder for future features
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = "Coming soon: Question display and answer selection"
                    },
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.md),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    Text(
                        text = "Coming Soon",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "Question display, answer selection, and performance tracking will be available soon.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (!isTriviaEnabled) {
                // Show message if feature flag is disabled
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            contentDescription = "Feature flag disabled notice"
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = "Note: Trivia app is currently disabled via feature flag. Enable it in staging to test.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(Spacing.md)
                    )
                }
            }
        }
    }
}

