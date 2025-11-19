package com.electricsheep.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Color-blind friendly, comforting color palette.
 * Designed with high contrast ratios for accessibility while maintaining
 * a warm, sophisticated aesthetic.
 */

// Light theme colors (40 suffix = darker, more saturated)
// Primary: Soft blue-teal - calming and accessible
val PrimaryLight = Color(0xFF4A7C7E) // Deep teal-blue
val PrimaryContainerLight = Color(0xFFB8E0E2) // Light teal
val OnPrimaryLight = Color(0xFFFFFFFF) // White text on primary
val OnPrimaryContainerLight = Color(0xFF1A3F41) // Dark teal text

// Secondary: Warm gray-beige - comforting neutral
val SecondaryLight = Color(0xFF6B6B6B) // Medium gray
val SecondaryContainerLight = Color(0xFFE5E5E5) // Light warm gray
val OnSecondaryLight = Color(0xFFFFFFFF) // White text
val OnSecondaryContainerLight = Color(0xFF2A2A2A) // Dark gray text

// Tertiary: Soft sage green - gentle accent
val TertiaryLight = Color(0xFF6B8E6B) // Muted sage green
val TertiaryContainerLight = Color(0xFFD4E6D4) // Light sage
val OnTertiaryLight = Color(0xFFFFFFFF) // White text
val OnTertiaryContainerLight = Color(0xFF2A3F2A) // Dark green text

// Error: Warm coral (distinguishable from greens/blues for color blindness)
val ErrorLight = Color(0xFFC85A4A) // Warm coral-red
val ErrorContainerLight = Color(0xFFFFE5E2) // Light coral
val OnErrorLight = Color(0xFFFFFFFF) // White text
val OnErrorContainerLight = Color(0xFF5A1F1A) // Dark red text

// Background and surface colors
val BackgroundLight = Color(0xFFF8F8F6) // Soft off-white
val SurfaceLight = Color(0xFFFFFFFF) // Pure white
val SurfaceVariantLight = Color(0xFFF0F0ED) // Very light warm gray
val OnBackgroundLight = Color(0xFF1A1A1A) // Near black
val OnSurfaceLight = Color(0xFF1A1A1A) // Near black
val OnSurfaceVariantLight = Color(0xFF4A4A4A) // Medium gray

// Dark theme colors (80 suffix = lighter, less saturated)
// Primary: Lighter teal-blue for dark mode
val PrimaryDark = Color(0xFF8BCBCE) // Light teal-blue
val PrimaryContainerDark = Color(0xFF2A5A5C) // Dark teal
val OnPrimaryDark = Color(0xFF0A2A2C) // Very dark teal text
val OnPrimaryContainerDark = Color(0xFFB8E0E2) // Light teal text

// Secondary: Light warm gray
val SecondaryDark = Color(0xFFB8B8B8) // Light gray
val SecondaryContainerDark = Color(0xFF4A4A4A) // Medium gray
val OnSecondaryDark = Color(0xFF1A1A1A) // Dark text
val OnSecondaryContainerDark = Color(0xFFE5E5E5) // Light gray text

// Tertiary: Light sage
val TertiaryDark = Color(0xFFA8C8A8) // Light sage
val TertiaryContainerDark = Color(0xFF4A6A4A) // Dark sage
val OnTertiaryDark = Color(0xFF1A2A1A) // Dark text
val OnTertiaryContainerDark = Color(0xFFD4E6D4) // Light sage text

// Error: Light coral
val ErrorDark = Color(0xFFFF8A7A) // Light coral
val ErrorContainerDark = Color(0xFF8A2A1A) // Dark coral
val OnErrorDark = Color(0xFF1A0A0A) // Dark text
val OnErrorContainerDark = Color(0xFFFFE5E2) // Light coral text

// Background and surface colors for dark mode
val BackgroundDark = Color(0xFF1A1C1A) // Very dark blue-gray
val SurfaceDark = Color(0xFF252725) // Dark gray-green
val SurfaceVariantDark = Color(0xFF2A2C2A) // Slightly lighter dark
val OnBackgroundDark = Color(0xFFE5E5E3) // Light warm gray
val OnSurfaceDark = Color(0xFFE5E5E3) // Light warm gray
val OnSurfaceVariantDark = Color(0xFFB8B8B6) // Medium light gray

// Legacy color names for backward compatibility (mapped to new colors)
val Purple80 = PrimaryDark
val PurpleGrey80 = SecondaryDark
val Pink80 = TertiaryDark

val Purple40 = PrimaryLight
val PurpleGrey40 = SecondaryLight
val Pink40 = TertiaryLight

