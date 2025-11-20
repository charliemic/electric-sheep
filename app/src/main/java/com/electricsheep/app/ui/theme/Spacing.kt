package com.electricsheep.app.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Standard spacing scale for consistent layout throughout the app.
 * Based on Material Design 3's 8dp grid system.
 * 
 * Usage:
 * - Card padding: Spacing.md
 * - Section spacing: Spacing.lg
 * - Screen edge padding: Spacing.md
 * - Element gaps: Spacing.sm or Spacing.md
 * - Icon to text: Spacing.xs
 */
object Spacing {
    /** Extra small spacing (4dp) - Tight spacing, icon to text */
    val xs = 4.dp
    
    /** Small spacing (8dp) - Between related elements */
    val sm = 8.dp
    
    /** Medium spacing (16dp) - Standard padding, gaps between elements */
    val md = 16.dp
    
    /** Large spacing (24dp) - Section separation */
    val lg = 24.dp
    
    /** Extra large spacing (32dp) - Major section separation */
    val xl = 32.dp
    
    /** Extra extra large spacing (48dp) - Screen edge padding, large separations */
    val xxl = 48.dp
}

