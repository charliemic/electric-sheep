package com.electricsheep.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.Settings
import com.electricsheep.app.R
import com.electricsheep.app.BuildConfig
import com.electricsheep.app.ElectricSheepApplication
import com.electricsheep.app.config.Environment
import com.electricsheep.app.config.FeatureFlag
import com.electricsheep.app.ui.components.LoadingOverlay
import com.electricsheep.app.util.Logger
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch

@Composable
fun LandingScreen(
    onNavigateToMoodManagement: () -> Unit,
    onNavigateToTrivia: () -> Unit,
    application: ElectricSheepApplication
) {
    Logger.debug("LandingScreen", "Landing screen displayed")

    // Check feature flag for indicator
    val showIndicator = application.getFeatureFlagManager().isEnabled(
        FeatureFlag.SHOW_FEATURE_FLAG_INDICATOR,
        defaultValue = false
    )
    
    // Check feature flag for trivia app
    val isTriviaEnabled = application.getFeatureFlagManager().isEnabled(
        FeatureFlag.ENABLE_TRIVIA_APP,
        defaultValue = false
    )

    // Debug-only: Show staging environment indicator
    val isDebug = BuildConfig.DEBUG
    val environmentManager = application.getEnvironmentManager()
    val isEnvironmentSwitchingAvailable = environmentManager.isEnvironmentSwitchingAvailable()
    val currentEnvironment = environmentManager.getSelectedEnvironment()
    val isUsingStaging = currentEnvironment == Environment.STAGING

    // Loading state for environment switching
    var isSwitchingEnvironment by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .semantics {
                contentDescription = "Electric Sheep app home screen"
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
        ) {
            // Debug indicators - grouped in top-right corner
            if (isDebug || showIndicator) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (isDebug && isEnvironmentSwitchingAvailable) {
                        EnvironmentSwitcher(
                            application = application,
                            currentEnvironment = currentEnvironment,
                            isUsingStaging = isUsingStaging,
                            onSwitchingChange = { isSwitching -> isSwitchingEnvironment = isSwitching },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    } else if (isDebug) {
                        DebugEnvironmentIndicator(
                            isUsingStaging = isUsingStaging,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }

                    // Feature flag indicator (if enabled)
                    if (showIndicator) {
                        FeatureFlagIndicator(
                            modifier = Modifier
                        )
                    }
                }
            }
        // Electric Sheep Logo
        Image(
            painter = painterResource(id = R.drawable.ic_electric_sheep_logo),
            contentDescription = "Electric Sheep Logo",
            modifier = Modifier
                .size(120.dp)
                .padding(bottom = 16.dp)
                .semantics {
                    contentDescription = "Electric Sheep Logo - A stylized sheep with electric lightning elements"
                    role = Role.Image
                }
        )

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

        // Trivia Quiz app (behind feature flag)
        if (isTriviaEnabled) {
            UtilityCard(
                title = "Trivia Quiz",
                description = "Test your knowledge with pub quiz style questions",
                onClick = {
                    Logger.info("LandingScreen", "User tapped Trivia Quiz utility")
                    onNavigateToTrivia()
                },
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .semantics {
                        contentDescription = "Trivia Quiz utility. Test your knowledge with pub quiz style questions"
                        role = Role.Button
                    }
            )
        }

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

        // Show loading overlay during environment switch
        if (isSwitchingEnvironment) {
            LoadingOverlay(
                message = "Switching environment..."
            )
        }
    }
}

/**
 * Environment switcher with dropdown menu.
 * Allows switching between staging and production in debug builds.
 * Shows a warning dialog if user is logged in when switching.
 */
@Composable
fun EnvironmentSwitcher(
    application: ElectricSheepApplication,
    currentEnvironment: Environment,
    isUsingStaging: Boolean,
    onSwitchingChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDropdown by rememberSaveable { mutableStateOf(false) }
    var showWarningDialog by rememberSaveable { mutableStateOf(false) }
    var pendingEnvironment by rememberSaveable { mutableStateOf<Environment?>(null) }

    val userManager = application.getUserManager()
    val currentUser by userManager.currentUser.collectAsStateWithLifecycle()
    val isLoggedIn = currentUser != null

    val coroutineScope = rememberCoroutineScope()
    val environmentManager = application.getEnvironmentManager()

    // Warning dialog when switching while logged in
    if (showWarningDialog && pendingEnvironment != null) {
        AlertDialog(
            onDismissRequest = {
                showWarningDialog = false
                pendingEnvironment = null
            },
            title = {
                Text(
                    text = "Switch Environment?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "You are currently logged in. Switching environments will log you out " +
                            "because authentication tokens are environment-specific.\n\n" +
                            "Do you want to continue?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showWarningDialog = false
                        val env = pendingEnvironment!!
                        pendingEnvironment = null
                        onSwitchingChange(true)

                        coroutineScope.launch {
                            try {
                                environmentManager.setSelectedEnvironment(env)
                                application.reinitializeSupabaseClient()
                            } finally {
                                onSwitchingChange(false)
                            }
                        }
                    }
                ) {
                    Text("Switch & Logout")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showWarningDialog = false
                        pendingEnvironment = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Box(modifier = modifier) {
        Card(
            modifier = Modifier.clickable {
                showDropdown = true
            },
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
                            "Debug mode: Using staging Supabase environment. Tap to switch environment"
                        } else {
                            "Debug mode: Using production Supabase environment. Tap to switch environment"
                        }
                        role = Role.Button
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
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Dropdown menu",
                    tint = if (isUsingStaging) {
                        MaterialTheme.colorScheme.onErrorContainer
                    } else {
                        MaterialTheme.colorScheme.onTertiaryContainer
                    },
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        DropdownMenu(
            expanded = showDropdown,
            onDismissRequest = { showDropdown = false }
        ) {
            DropdownMenuItem(
                text = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("PRODUCTION")
                        if (currentEnvironment == Environment.PRODUCTION) {
                            Text("✓", fontWeight = FontWeight.Bold)
                        }
                    }
                },
                onClick = {
                    showDropdown = false
                    if (currentEnvironment != Environment.PRODUCTION) {
                        if (isLoggedIn) {
                            pendingEnvironment = Environment.PRODUCTION
                            showWarningDialog = true
                        } else {
                            onSwitchingChange(true)
                            coroutineScope.launch {
                                try {
                                    environmentManager.setSelectedEnvironment(Environment.PRODUCTION)
                                    application.reinitializeSupabaseClient()
                                } finally {
                                    onSwitchingChange(false)
                                }
                            }
                        }
                    }
                }
            )
            DropdownMenuItem(
                text = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("STAGING")
                        if (currentEnvironment == Environment.STAGING) {
                            Text("✓", fontWeight = FontWeight.Bold)
                        }
                    }
                },
                onClick = {
                    showDropdown = false
                    if (currentEnvironment != Environment.STAGING) {
                        if (isLoggedIn) {
                            pendingEnvironment = Environment.STAGING
                            showWarningDialog = true
                        } else {
                            onSwitchingChange(true)
                            coroutineScope.launch {
                                try {
                                    environmentManager.setSelectedEnvironment(Environment.STAGING)
                                    application.reinitializeSupabaseClient()
                                } finally {
                                    onSwitchingChange(false)
                                }
                            }
                        }
                    }
                }
            )
        }
    }
}

/**
 * Debug environment indicator (non-interactive).
 * Shows which Supabase environment is being used (staging or production).
 * Only visible in debug builds when environment switching is not available.
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

