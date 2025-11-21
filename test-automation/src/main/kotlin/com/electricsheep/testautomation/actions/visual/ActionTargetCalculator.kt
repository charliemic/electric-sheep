package com.electricsheep.testautomation.actions.visual

import org.slf4j.LoggerFactory

/**
 * Calculates safe tap targets for actions (like human precision targeting).
 * 
 * **Human Process**: "How do I make sure I tap in the button?"
 * - Center Calculation: "The center is at (850, 1220)"
 * - Safety Margin: "I'll tap slightly inside to avoid edges"
 * - Final Target: "I'll tap at (850, 1220)"
 * 
 * **System Process**: Calculates safe tap locations with margins
 * 
 * **Complexity**: ⭐ (Low - simple calculations)
 * **Effectiveness**: High (reduces tap failures)
 */
class ActionTargetCalculator {
    private val logger = LoggerFactory.getLogger(javaClass)
    
    /**
     * Default safety margin (pixels from edge).
     * Humans naturally avoid edges when tapping.
     */
    private val defaultSafetyMargin = 10
    
    /**
     * Tap target with coordinates and confidence.
     */
    data class TapTarget(
        val x: Int,
        val y: Int,
        val confidence: Double,
        val safetyMargin: Int = 10
    )
    
    /**
     * Swipe path between two elements.
     */
    data class SwipePath(
        val startX: Int,
        val startY: Int,
        val endX: Int,
        val endY: Int,
        val duration: Int  // milliseconds
    )
    
    /**
     * Calculate safe tap target for element (human: targets center, avoids edges).
     */
    fun calculateTapTarget(
        elementLocation: VisualElementFinder.ElementLocation,
        spatialInfo: SpatialAnalyzer.SpatialInfo,
        safetyMargin: Int = defaultSafetyMargin
    ): TapTarget {
        val boundingBox = elementLocation.boundingBox
        
        // Calculate center point
        val centerX = boundingBox.x + boundingBox.width / 2
        val centerY = boundingBox.y + boundingBox.height / 2
        
        // Apply safety margin (avoid edges, like human precision)
        val safeX = centerX.coerceIn(
            boundingBox.x + safetyMargin,
            boundingBox.x + boundingBox.width - safetyMargin
        )
        val safeY = centerY.coerceIn(
            boundingBox.y + safetyMargin,
            boundingBox.y + boundingBox.height - safetyMargin
        )
        
        logger.debug("Tap target: ($safeX, $safeY) for element at (${boundingBox.x}, ${boundingBox.y}, ${boundingBox.width}x${boundingBox.height})")
        
        return TapTarget(
            x = safeX,
            y = safeY,
            confidence = elementLocation.confidence,
            safetyMargin = safetyMargin
        )
    }
    
    /**
     * Calculate swipe path between two elements (human: "I swipe from here to there").
     */
    fun calculateSwipePath(
        fromElement: VisualElementFinder.ElementLocation,
        toElement: VisualElementFinder.ElementLocation,
        spatialAnalyzer: SpatialAnalyzer
    ): SwipePath {
        val fromTarget = calculateTapTarget(fromElement, spatialAnalyzer.analyzeLocation(fromElement))
        val toTarget = calculateTapTarget(toElement, spatialAnalyzer.analyzeLocation(toElement))
        
        // Calculate swipe duration based on distance (human: faster for shorter distances)
        val distance = spatialAnalyzer.calculateDistance(fromElement, toElement)
        val duration = calculateSwipeDuration(distance)
        
        logger.debug("Swipe path: (${fromTarget.x}, ${fromTarget.y}) → (${toTarget.x}, ${toTarget.y}), distance: ${distance.toInt()}px, duration: ${duration}ms")
        
        return SwipePath(
            startX = fromTarget.x,
            startY = fromTarget.y,
            endX = toTarget.x,
            endY = toTarget.y,
            duration = duration
        )
    }
    
    /**
     * Calculate swipe duration based on distance (human-like: faster for shorter swipes).
     */
    private fun calculateSwipeDuration(distance: Double): Int {
        // Base duration: 300ms
        // Additional time: 1ms per 10 pixels
        // Min: 200ms, Max: 1000ms
        val baseDuration = 300
        val additionalDuration = (distance / 10).toInt()
        return (baseDuration + additionalDuration).coerceIn(200, 1000)
    }
    
    /**
     * Calculate tap target for text input (human: taps in center of field).
     */
    fun calculateInputFieldTarget(
        fieldLocation: VisualElementFinder.ElementLocation,
        spatialInfo: SpatialAnalyzer.SpatialInfo
    ): TapTarget {
        // For input fields, target slightly left of center (where text typically starts)
        val boundingBox = fieldLocation.boundingBox
        val centerX = boundingBox.x + boundingBox.width / 3 // Left third (text start)
        val centerY = boundingBox.y + boundingBox.height / 2
        
        val safeX = centerX.coerceIn(
            boundingBox.x + defaultSafetyMargin,
            boundingBox.x + boundingBox.width - defaultSafetyMargin
        )
        val safeY = centerY.coerceIn(
            boundingBox.y + defaultSafetyMargin,
            boundingBox.y + boundingBox.height - defaultSafetyMargin
        )
        
        return TapTarget(
            x = safeX,
            y = safeY,
            confidence = fieldLocation.confidence,
            safetyMargin = defaultSafetyMargin
        )
    }
}

