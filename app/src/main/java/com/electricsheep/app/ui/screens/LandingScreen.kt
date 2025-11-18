package com.electricsheep.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.Settings
import com.electricsheep.app.BuildConfig
import com.electricsheep.app.ElectricSheepApplication
import com.electricsheep.app.config.FeatureFlag
import com.electricsheep.app.util.Logger

@Composable
fun LandingScreen(
    onNavigateToMoodManagement: () -> Unit,
    application: ElectricSheepApplication
) {
    Logger.debug("LandingScreen", "Landing screen displayed")
    
    // Check feature flag for indicator
    val showIndicator = application.getFeatureFlagManager().isEnabled(
        FeatureFlag.SHOW_FEATURE_FLAG_INDICATOR,
        defaultValue = false
    )
    
    // Debug-only: Show staging environment indicator
    val isDebug = BuildConfig.DEBUG
    val isUsingStaging = BuildConfig.USE_STAGING_SUPABASE && isDebug
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        // Debug environment indicator (only in debug builds)
        if (isDebug) {
            DebugEnvironmentIndicator(
                isUsingStaging = isUsingStaging,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(bottom = 8.dp)
            )
        }
        
        // Feature flag indicator (if enabled)
        if (showIndicator) {
            FeatureFlagIndicator(
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(bottom = 8.dp)
            )
        }
        
        Text(
            text = "Electric Sheep",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "Personal Utilities",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        UtilityCard(
            title = "Mood Management",
            description = "Track your mood throughout the day and analyse trends",
            onClick = {
                Logger.info("LandingScreen", "User tapped Mood Management utility")
                onNavigateToMoodManagement()
            },
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .semantics {
                    contentDescription = "Mood Management utility. Track your mood throughout the day and analyse trends"
                    role = Role.Button
                }
        )
        
        // Placeholder for future utilities
        UtilityCard(
            title = "Coming Soon",
            description = "More utilities will be available soon",
            onClick = { },
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .semantics {
                    contentDescription = "Coming Soon utility. More utilities will be available soon. Currently unavailable"
                    role = Role.Button
                },
            enabled = false
        )
    }
}

/**
 * Debug environment indicator.
 * Shows which Supabase environment is being used (staging or production).
 * Only visible in debug builds.
 */
@Composable
fun DebugEnvironmentIndicator(
    isUsingStaging: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUsingStaging) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.tertiaryContainer
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .semantics {
                    contentDescription = if (isUsingStaging) {
                        "Debug mode: Using staging Supabase environment"
                    } else {
                        "Debug mode: Using production Supabase environment"
                    }
                    role = Role.Image
                },
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                tint = if (isUsingStaging) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.onTertiaryContainer
                },
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = if (isUsingStaging) "STAGING" else "PROD",
                style = MaterialTheme.typography.labelSmall,
                color = if (isUsingStaging) {
                    MaterialTheme.colorScheme.onErrorContainer
                } else {
                    MaterialTheme.colorScheme.onTertiaryContainer
                },
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Feature flag indicator icon.
 * Shows a jazzy flag icon to demonstrate feature flag functionality.
 * This icon appears conditionally based on the show_feature_flag_indicator flag.
 */
@Composable
fun FeatureFlagIndicator(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .semantics {
                    contentDescription = "Feature flag indicator. This icon appears conditionally based on feature flag settings"
                    role = Role.Image
                },
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Flag,
                contentDescription = "Feature flag",
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Feature Flag",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun UtilityCard(
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Card(
        modifier = modifier
            .height(120.dp)
            .then(
                if (enabled) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (enabled) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = if (enabled) {
                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                },
                textAlign = TextAlign.Start
            )
        }
    }
}

