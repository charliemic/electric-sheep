package com.electricsheep.testautomation.actions.visual

import org.slf4j.LoggerFactory

/**
 * Analyzes spatial relationships of UI elements (like human spatial understanding).
 * 
 * **Human Process**: "Where is it relative to the screen?"
 * - Location: "It's in the bottom right"
 * - Size: "It's about 100x40 pixels"
 * - Position: "It's at coordinates (800, 1200)"
 * 
 * **System Process**: Calculates bounding boxes, centers, relative positions
 * 
 * **Complexity**: ⭐ (Low - simple calculations)
 * **Effectiveness**: High (accurate spatial understanding)
 */
class SpatialAnalyzer {
    private val logger = LoggerFactory.getLogger(javaClass)
    
    /**
     * Spatial information about an element.
     */
    data class SpatialInfo(
        val center: VisualElementFinder.Point,
        val relativePosition: RelativePosition,
        val size: Int,  // Area in pixels²
        val isVisible: Boolean,
        val isAccessible: Boolean,
        val boundingBox: VisualElementFinder.BoundingBox
    )
    
    /**
     * Relative position on screen.
     */
    data class RelativePosition(
        val horizontal: HorizontalPosition,
        val vertical: VerticalPosition
    )
    
    enum class HorizontalPosition {
        LEFT, CENTER, RIGHT
    }
    
    enum class VerticalPosition {
        TOP, MIDDLE, BOTTOM
    }
    
    /**
     * Analyze element location and spatial context.
     */
    fun analyzeLocation(element: VisualElementFinder.ElementLocation): SpatialInfo {
        val boundingBox = element.boundingBox
        val screenSize = element.screenSize
        
        // Calculate center point
        val center = VisualElementFinder.Point(
            x = boundingBox.x + boundingBox.width / 2,
            y = boundingBox.y + boundingBox.height / 2
        )
        
        // Calculate relative position
        val relativePosition = calculateRelativePosition(center, screenSize)
        
        // Calculate size (area)
        val size = boundingBox.width * boundingBox.height
        
        // Check visibility (element is fully on screen)
        val isVisible = isFullyVisible(boundingBox, screenSize)
        
        // Check accessibility (element is not blocked by other elements)
        // TODO: Implement actual accessibility check (would need element hierarchy)
        val isAccessible = isVisible // Simplified: if visible, assume accessible
        
        return SpatialInfo(
            center = center,
            relativePosition = relativePosition,
            size = size,
            isVisible = isVisible,
            isAccessible = isAccessible,
            boundingBox = boundingBox
        )
    }
    
    /**
     * Calculate relative position (human: "It's in the bottom right").
     */
    private fun calculateRelativePosition(
        center: VisualElementFinder.Point,
        screenSize: VisualElementFinder.ScreenSize
    ): RelativePosition {
        val horizontal = when {
            center.x < screenSize.width / 3 -> HorizontalPosition.LEFT
            center.x > screenSize.width * 2 / 3 -> HorizontalPosition.RIGHT
            else -> HorizontalPosition.CENTER
        }
        
        val vertical = when {
            center.y < screenSize.height / 3 -> VerticalPosition.TOP
            center.y > screenSize.height * 2 / 3 -> VerticalPosition.BOTTOM
            else -> VerticalPosition.MIDDLE
        }
        
        return RelativePosition(horizontal, vertical)
    }
    
    /**
     * Check if element is fully visible on screen.
     */
    private fun isFullyVisible(
        boundingBox: VisualElementFinder.BoundingBox,
        screenSize: VisualElementFinder.ScreenSize
    ): Boolean {
        return boundingBox.x >= 0 &&
               boundingBox.y >= 0 &&
               (boundingBox.x + boundingBox.width) <= screenSize.width &&
               (boundingBox.y + boundingBox.height) <= screenSize.height
    }
    
    /**
     * Calculate distance between two elements (for swipe calculations).
     */
    fun calculateDistance(
        element1: VisualElementFinder.ElementLocation,
        element2: VisualElementFinder.ElementLocation
    ): Double {
        val center1 = element1.center()
        val center2 = element2.center()
        
        val dx = (center1.x - center2.x).toDouble()
        val dy = (center1.y - center2.y).toDouble()
        return kotlin.math.sqrt(dx * dx + dy * dy)
    }
}

