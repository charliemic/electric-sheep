package com.electricsheep.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

/**
 * Accessible button component with built-in loading state and accessibility features.
 * 
 * Features:
 * - Minimum 48dp touch target (Material Design standard)
 * - Screen reader support with descriptive content and state
 * - Consistent loading state handling
 * - Proper semantic roles
 * 
 * @param text The button text
 * @param onClick The action to perform when clicked
 * @param modifier Additional modifier to apply
 * @param enabled Whether the button is enabled
 * @param isLoading Whether the button is in loading state
 * @param contentDescription Detailed description for screen readers (defaults to button text)
 * @param loadingDescription Description for loading state (e.g., "Saving", "Loading")
 */
@Composable
fun AccessibleButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    contentDescriptionParam: String? = null,
    loadingDescription: String? = null
) {
    val buttonDesc = contentDescriptionParam ?: text
    val effectiveLoadingDescription = loadingDescription ?: "Loading"
    val descText = if (isLoading) {
        "$buttonDesc, $effectiveLoadingDescription"
    } else {
        buttonDesc
    }
    
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier.semantics {
            role = Role.Button
            contentDescription = descText
        }.then(
            if (isLoading) {
                Modifier.semantics {
                    stateDescription = effectiveLoadingDescription
                }
            } else {
                Modifier
            }
        )
    ) {
        if (isLoading) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(16.dp)
                        .semantics { 
                            contentDescription = "$effectiveLoadingDescription progress indicator"
                        },
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            }
        } else {
            Text(text)
        }
    }
}

