package com.electricsheep.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.electricsheep.app.ui.theme.Spacing

/**
 * Accessible screen wrapper that provides consistent structure and accessibility.
 * 
 * Features:
 * - Consistent top bar with accessible title
 * - Proper screen semantics
 * - Standard padding
 * 
 * @param title The screen title (announced to screen readers)
 * @param modifier Additional modifier to apply
 * @param navigationIcon Optional navigation icon (back button, etc.)
 * @param actions Optional actions in the top bar
 * @param content The screen content
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessibleScreen(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier.semantics {
            contentDescription = "Screen: $title"
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.semantics {
                            contentDescription = "Screen title: $title"
                        }
                    )
                },
                navigationIcon = navigationIcon ?: {},
                actions = actions
            )
        }
    ) { paddingValues ->
        content(paddingValues)
    }
}

