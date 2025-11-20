package com.electricsheep.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * Focus management utilities for improved keyboard navigation and accessibility.
 */

/**
 * Request focus on a composable when it becomes visible.
 * Useful for focusing input fields when screens load or when errors occur.
 * 
 * @param shouldFocus Whether focus should be requested
 * @param modifier Modifier to apply focus requester to
 * @return Modified modifier with focus requester
 */
@Composable
fun rememberFocusRequester(): FocusRequester {
    return remember { FocusRequester() }
}

/**
 * Request focus when condition is true.
 */
@Composable
fun RequestFocusWhen(
    focusRequester: FocusRequester,
    shouldFocus: Boolean
) {
    LaunchedEffect(shouldFocus) {
        if (shouldFocus) {
            focusRequester.requestFocus()
        }
    }
}

/**
 * Clear focus from current focused element.
 * Useful for dismissing keyboard after actions.
 */
@Composable
fun ClearFocus() {
    val focusManager = LocalFocusManager.current
    LaunchedEffect(Unit) {
        focusManager.clearFocus()
    }
}

/**
 * Screen reader announcement for dynamic content changes.
 * Use for announcing state changes that aren't visually obvious.
 * 
 * Note: This should be placed in a small, out-of-view location.
 * The announcement will be made when shouldAnnounce becomes true.
 * 
 * @param announcement The text to announce
 * @param shouldAnnounce Whether to make the announcement
 */
@Composable
fun ScreenReaderAnnouncement(
    announcement: String,
    shouldAnnounce: Boolean
) {
    if (shouldAnnounce && announcement.isNotBlank()) {
        // Use a small, invisible box for the announcement
        Box(
            modifier = Modifier
                .size(1.dp)
                .semantics {
                    contentDescription = announcement
                    liveRegion = androidx.compose.ui.semantics.LiveRegionMode.Polite
                }
        ) {
            // Empty - semantics handle the announcement
        }
    }
}

