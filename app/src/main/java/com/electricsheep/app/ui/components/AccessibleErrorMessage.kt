package com.electricsheep.app.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import com.electricsheep.app.ui.theme.Spacing

/**
 * Accessible error message component that announces errors to screen readers.
 * 
 * Features:
 * - Live region for dynamic error announcements
 * - Error semantics for screen readers
 * - Consistent styling
 * - Proper color contrast
 * 
 * @param message The error message to display
 * @param modifier Additional modifier to apply
 * @param textAlign Text alignment (default: Start)
 */
@Composable
fun AccessibleErrorMessage(
    message: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start
) {
    Text(
        text = message,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodySmall,
        textAlign = textAlign,
        modifier = modifier
            .semantics {
                error(message)
                liveRegion = androidx.compose.ui.semantics.LiveRegionMode.Polite
                contentDescription = "Error: $message"
            }
    )
}

