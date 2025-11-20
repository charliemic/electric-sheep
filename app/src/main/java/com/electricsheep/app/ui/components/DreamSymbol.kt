package com.electricsheep.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.unit.dp

/**
 * Abstract symbol inspired by "Do Androids Dream of Electric Sheep?"
 * 
 * Themes: Dreams, pastoral calm, flowing consciousness, layers of reality
 * Design: Overlapping circles - like ripples or dream layers
 * 
 * Three concentric circles representing:
 * - Outer layer: Pastoral calm, outer consciousness (subtle)
 * - Middle layer: Dreams, middle consciousness (medium)
 * - Inner layer: Core consciousness, present moment (solid)
 * 
 * @param modifier Additional modifier to apply
 * @param color Color of the symbol
 * @param size Size of the symbol (defaults to 40dp)
 */
@Composable
fun DreamSymbol(
    modifier: Modifier = Modifier,
    color: Color,
    size: androidx.compose.ui.unit.Dp = 40.dp
) {
    Canvas(
        modifier = modifier.size(size)
    ) {
        val centerX = this.size.width / 2
        val centerY = this.size.height / 2
        
        // Scale factor to fit in circle (original design is 72dp viewport, circles at 36,36)
        val scale = this.size.width / 72f
        
        // Outer circle - larger, subtle (pastoral calm, outer consciousness)
        drawCircle(
            color = color.copy(alpha = 0.4f),
            radius = 18f * scale,
            center = androidx.compose.ui.geometry.Offset(centerX, centerY)
        )
        
        // Middle circle - medium, more visible (dreams, middle layer)
        drawCircle(
            color = color.copy(alpha = 0.6f),
            radius = 12f * scale,
            center = androidx.compose.ui.geometry.Offset(centerX, centerY)
        )
        
        // Inner circle - solid, clear (core consciousness, present moment)
        drawCircle(
            color = color,
            radius = 6f * scale,
            center = androidx.compose.ui.geometry.Offset(centerX, centerY)
        )
    }
}

