package com.electricsheep.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.electricsheep.app.ui.theme.Spacing

/**
 * Accessible card component with consistent styling and accessibility features.
 * 
 * Features:
 * - Standard elevation and padding
 * - Optional clickable with proper semantics
 * - Screen reader support
 * - Consistent styling
 * 
 * @param modifier Additional modifier to apply
 * @param onClick Optional click handler (makes card clickable)
 * @param contentDescription Description for screen readers (required if clickable)
 * @param elevation Card elevation (1dp subtle, 2dp standard, 4dp interactive)
 * @param padding Card padding (default 16dp, can be 20dp for content-heavy)
 * @param content The card content
 */
@Composable
fun AccessibleCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentDescriptionParam: String? = null,
    elevation: Int = 2,
    padding: androidx.compose.ui.unit.Dp = Spacing.md,
    containerColor: androidx.compose.ui.graphics.Color? = null,
    content: @Composable () -> Unit
) {
    val desc = contentDescriptionParam // Avoid shadowing
    val cardModifier = if (onClick != null) {
        modifier
            .clickable(onClick = onClick)
            .semantics {
                role = Role.Button
                if (desc != null) {
                    contentDescription = desc
                }
            }
    } else {
        modifier.semantics {
            if (desc != null) {
                contentDescription = desc
            }
        }
    }
    
    Card(
        modifier = cardModifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor ?: MaterialTheme.colorScheme.surface
        )
    ) {
        Box(modifier = Modifier.padding(padding)) {
            content()
        }
    }
}

