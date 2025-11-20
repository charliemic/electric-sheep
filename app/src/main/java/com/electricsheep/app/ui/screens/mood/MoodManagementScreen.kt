package com.electricsheep.app.ui.screens.mood

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.error
import android.content.Intent
import com.electricsheep.app.config.MoodConfig
import com.electricsheep.app.data.model.Mood
import com.electricsheep.app.ui.theme.Spacing
import com.electricsheep.app.util.DateFormatter
import com.electricsheep.app.util.Logger
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun MoodManagementScreen(
    viewModel: MoodManagementViewModel,
    onNavigateBack: () -> Unit
) {
    Logger.info("MoodManagementScreen", "Mood Management screen displayed")
    
    // Observe ViewModel state
    val currentUser by viewModel.currentUser.collectAsState()
    val emailText by viewModel.emailText.collectAsState()
    val passwordText by viewModel.passwordText.collectAsState()
    // Removed isSignUpMode - Google OAuth handles both sign-in and account creation automatically
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val moodScoreText by viewModel.moodScoreText.collectAsState()
    val isSavingMood by viewModel.isSavingMood.collectAsState()
    val moodErrorMessage by viewModel.moodErrorMessage.collectAsState()
    val moods by viewModel.moods.collectAsState()
    val googleOAuthUrl by viewModel.googleOAuthUrl.collectAsState()
    
    val keyboardController = LocalSoftwareKeyboardController.current
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // Handle Google OAuth URL - open in Chrome Custom Tabs (Android best practice)
    LaunchedEffect(googleOAuthUrl) {
        googleOAuthUrl?.let { url ->
            try {
                Logger.info("MoodManagementScreen", "Attempting to open Google OAuth URL in Custom Tab: $url")
                
                // Use Chrome Custom Tabs for OAuth (recommended Android pattern)
                // Custom Tabs provide better UX: look like part of the app, share cookies, faster
                val uri = android.net.Uri.parse(url)
                
                // Check if Chrome Custom Tabs is available
                val packageName = androidx.browser.customtabs.CustomTabsClient.getPackageName(
                    context,
                    null // Use default browser
                )
                
                Logger.debug("MoodManagementScreen", "CustomTabsClient.getPackageName returned: $packageName")
                
                if (packageName != null) {
                    val builder = androidx.browser.customtabs.CustomTabsIntent.Builder()
                    builder.setShowTitle(true)
                    // Use a default toolbar color (can be customized based on app theme)
                    // Note: MaterialTheme.colorScheme is not accessible in LaunchedEffect
                    builder.setToolbarColor(0xFF4A7C7E.toInt()) // Primary teal-blue
                    
                    val customTabsIntent = builder.build()
                    customTabsIntent.intent.setPackage(packageName)
                    customTabsIntent.launchUrl(context, uri)
                    Logger.info("MoodManagementScreen", "Opened Google OAuth URL in Custom Tab (package: $packageName)")
                } else {
                    // Fallback to regular browser if Custom Tabs not available
                    Logger.warn("MoodManagementScreen", "Custom Tabs not available, falling back to regular browser")
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    
                    // Check what activities can handle this intent
                    val resolveInfo = context.packageManager.queryIntentActivities(intent, 0)
                    Logger.debug("MoodManagementScreen", "Found ${resolveInfo.size} activities that can handle ACTION_VIEW for URL")
                    resolveInfo.forEach { info ->
                        Logger.debug("MoodManagementScreen", "  - ${info.activityInfo.packageName}/${info.activityInfo.name}")
                    }
                    
                    if (intent.resolveActivity(context.packageManager) != null) {
                        try {
                            context.startActivity(intent)
                            Logger.info("MoodManagementScreen", "Opened Google OAuth URL in browser")
                        } catch (e: Exception) {
                            Logger.error("MoodManagementScreen", "Failed to start browser activity", e)
                            viewModel.setError("Failed to open browser: ${e.message}")
                        }
                    } else {
                        Logger.error("MoodManagementScreen", "No browser app found to open OAuth URL")
                        viewModel.setError("No web browser found. Please install Chrome from the Play Store to sign in with Google.")
                    }
                }
                
                // Clear the URL after attempting to open to prevent re-opening
                viewModel.clearGoogleOAuthUrl()
            } catch (e: android.content.ActivityNotFoundException) {
                Logger.error("MoodManagementScreen", "No browser app found to open OAuth URL", e)
                viewModel.setError("No web browser found. Please install Chrome from the Play Store to sign in with Google.")
                viewModel.clearGoogleOAuthUrl()
            } catch (e: Exception) {
                Logger.error("MoodManagementScreen", "Failed to open OAuth URL", e)
                viewModel.setError("Failed to open sign-in page. Please try again.")
                viewModel.clearGoogleOAuthUrl()
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mood Management") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            Logger.debug("MoodManagementScreen", "User tapped back button")
                            onNavigateBack()
                        },
                        modifier = Modifier.semantics {
                            contentDescription = "Navigate back to main screen"
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null // Handled by parent IconButton
                        )
                    }
                },
                actions = {
                    // Show user info and logout if authenticated
                    if (currentUser != null) {
                        // Compact user info - email with Person icon
                        // Placed in top bar for minimal intrusion
                        Row(
                            modifier = Modifier
                                .padding(horizontal = Spacing.sm)
                                .semantics {
                                    contentDescription = "Signed in as ${currentUser!!.email}"
                                },
                            horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = currentUser!!.email,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        // Logout button - compact icon button
                        IconButton(
                            onClick = {
                                Logger.info("MoodManagementScreen", "User tapped logout")
                                viewModel.signOut()
                            },
                            modifier = Modifier.semantics {
                                contentDescription = "Sign out from current account"
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = null // Handled by parent IconButton
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        // Use LazyColumn instead of Column with verticalScroll to avoid nested scrolling issues
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding() // Add padding for IME (keyboard) and text selection toolbar
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Show login UI if not authenticated
            if (currentUser == null) {
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Sign In",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Text(
                            text = "Sign in with Google to get started. If you don't have an account, one will be created automatically.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Primary: Sign in with Google button
                        // Google OAuth automatically handles both sign-in and account creation
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    viewModel.signInWithGoogle()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics {
                                    contentDescription = if (isLoading) {
                                        "Starting Google sign-in, please wait"
                                    } else {
                                        "Sign in with Google"
                                    }
                                    if (isLoading) {
                                        stateDescription = "Loading"
                                    }
                                },
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .semantics { contentDescription = "Loading Google sign-in" },
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text("Sign in with Google")
                            }
                        }
                        
                        // Error message
                        if (errorMessage != null) {
                            Text(
                                text = errorMessage!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .semantics {
                                        error(errorMessage!!)
                                    }
                            )
                        }
                        
                        // Secondary: Email/password option (collapsible)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        var showEmailPassword by remember { mutableStateOf(false) }
                        
                        TextButton(
                            onClick = { showEmailPassword = !showEmailPassword },
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics {
                                    contentDescription = if (showEmailPassword) {
                                        "Hide email and password sign in"
                                    } else {
                                        "Show email and password sign in"
                                    }
                                }
                        ) {
                            Text(
                                text = if (showEmailPassword) {
                                    "Hide email/password sign in"
                                } else {
                                    "Sign in with email and password"
                                },
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        
                        if (showEmailPassword) {
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Email input
                        OutlinedTextField(
                            value = emailText,
                            onValueChange = { viewModel.updateEmailText(it) },
                            label = { Text("Email") },
                            placeholder = { Text("user@example.com") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics {
                                    contentDescription = "Email address input field"
                                },
                            enabled = !isLoading,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            )
                        )
                        
                        // Password input
                        OutlinedTextField(
                            value = passwordText,
                            onValueChange = { viewModel.updatePasswordText(it) },
                            label = { Text("Password") },
                            placeholder = { Text("Enter password") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics {
                                    contentDescription = "Password input field"
                                },
                            enabled = !isLoading,
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            )
                        )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Create Account button (primary action for new users)
                            Button(
                                onClick = {
                                    viewModel.signUp()
                                    keyboardController?.hide()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .semantics {
                                        contentDescription = if (isLoading) {
                                            "Creating account, please wait"
                                        } else {
                                            "Create account with email and password"
                                        }
                                        if (isLoading) {
                                            stateDescription = "Loading"
                                        }
                                    },
                                enabled = !isLoading && emailText.isNotBlank() && passwordText.isNotBlank()
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .semantics { 
                                                contentDescription = "Creating account progress"
                                            },
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                } else {
                                    Text("Create Account")
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Sign in button (for existing users)
                            OutlinedButton(
                                onClick = {
                                    viewModel.signIn()
                                    keyboardController?.hide()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .semantics {
                                        contentDescription = if (isLoading) {
                                            "Signing in, please wait"
                                        } else {
                                            "Sign in with email and password"
                                        }
                                        if (isLoading) {
                                            stateDescription = "Loading"
                                        }
                                    },
                                enabled = !isLoading && emailText.isNotBlank() && passwordText.isNotBlank()
                            ) {
                                Text("Sign In")
                            }
                        }
                    }
                }
            } else {
                // Show app content when authenticated
                item {
                    Text(
                        text = "Mood Management",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // Mood input section - moved to top for better UX and to avoid toolbar overlap
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "How are you feeling?",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Text(
                                text = "Rate your mood from ${MoodConfig.MIN_SCORE} to ${MoodConfig.MAX_SCORE}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            OutlinedTextField(
                                value = moodScoreText,
                                onValueChange = { viewModel.updateMoodScoreText(it) },
                                label = { Text("Mood Score") },
                                placeholder = { Text("${MoodConfig.MIN_SCORE}-${MoodConfig.MAX_SCORE}") },
                                modifier = Modifier
                                    .fillMaxWidth(0.8f) // Make it narrower and centered
                                    .semantics {
                                        contentDescription = "Mood score input field. Enter a number between ${MoodConfig.MIN_SCORE} and ${MoodConfig.MAX_SCORE}"
                                        if (moodErrorMessage != null) {
                                            error(moodErrorMessage!!)
                                        }
                                    },
                                enabled = !isSavingMood,
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Done
                                ),
                                supportingText = {
                                    if (moodErrorMessage == null) {
                                        Text("Enter a number between ${MoodConfig.MIN_SCORE} and ${MoodConfig.MAX_SCORE}")
                                    } else {
                                        Text(moodErrorMessage!!)
                                    }
                                },
                                isError = moodErrorMessage != null
                            )
                            
                            Button(
                                onClick = {
                                    viewModel.saveMood()
                                    // Hide keyboard after save is triggered
                                    keyboardController?.hide()
                                },
                                modifier = Modifier
                                    .fillMaxWidth(0.8f) // Match text field width
                                    .semantics {
                                        contentDescription = if (isSavingMood) {
                                            "Saving mood entry, please wait"
                                        } else {
                                            "Save mood entry"
                                        }
                                        if (isSavingMood) {
                                            stateDescription = "Saving"
                                        }
                                    },
                                enabled = !isSavingMood && moodScoreText.isNotBlank()
                            ) {
                                if (isSavingMood) {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .semantics { contentDescription = "Saving progress" },
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                } else {
                                    Text("Save Mood")
                                }
                            }
                        }
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
                
                    
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
                
                // Mood history section
                item {
                    Text(
                        text = "Mood History",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                if (moods.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "No mood entries yet. Add your first mood above!",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    // Use items() for the mood list directly in LazyColumn
                    items(moods) { mood ->
                        MoodEntryRow(mood = mood)
                    }
                }
                
                // Add extra spacing at bottom to account for keyboard and toolbar
                item {
                    Spacer(modifier = Modifier.height(120.dp))
                }
            }
        }
    }
}
