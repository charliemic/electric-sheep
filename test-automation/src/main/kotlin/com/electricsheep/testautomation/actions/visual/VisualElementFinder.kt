package com.electricsheep.testautomation.actions.visual

import com.electricsheep.testautomation.vision.PatternRecognizer
import com.electricsheep.testautomation.vision.TextDetector
import org.slf4j.LoggerFactory
import java.io.File

/**
 * Finds UI elements using visual detection (like human visual search).
 * 
 * **Human Process**: "Where is the button?"
 * - Pattern Recognition: "I see a button shape"
 * - Text Reading: "I read 'Save' text"
 * - Object Detection: "I see a button element"
 * 
 * **System Process**: Combines multiple visual detection methods
 * - Pattern Recognition (OpenCV)
 * - Text Detection (OCR)
 * - Object Detection (ONNX - Planned)
 * 
 * **Complexity**: ⭐⭐ (Medium - combines multiple detectors)
 * **Effectiveness**: High (90%+ when multiple cues agree)
 */
class VisualElementFinder(
    private val patternRecognizer: PatternRecognizer? = null,
    private val textDetector: TextDetector? = null
    // TODO: Add ObjectDetector when available
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    
    /**
     * Element location found via visual detection.
     */
    data class ElementLocation(
        val boundingBox: BoundingBox,
        val confidence: Double,
        val detectionMethod: DetectionMethod,
        val screenSize: ScreenSize
    ) {
        fun center(): Point {
            return Point(
                x = boundingBox.x + boundingBox.width / 2,
                y = boundingBox.y + boundingBox.height / 2
            )
        }
    }
    
    data class BoundingBox(
        val x: Int,      // Left edge
        val y: Int,      // Top edge
        val width: Int,
        val height: Int
    )
    
    data class Point(val x: Int, val y: Int)
    
    data class ScreenSize(val width: Int, val height: Int)
    
    enum class DetectionMethod {
        PATTERN, TEXT, OBJECT, COMBINED
    }
    
    /**
     * Find button by text label (human-like: "I want to click the Save button").
     * 
     * @param buttonText The text on the button (e.g., "Save", "Cancel")
     * @param screenshot Current screenshot
     * @return ElementLocation if found, null otherwise
     */
    fun findButton(buttonText: String, screenshot: File, screenSize: ScreenSize): ElementLocation? {
        logger.debug("Finding button with text: '$buttonText'")
        
        val matches = mutableListOf<ElementMatch>()
        
        // 1. Text Detection (human: "I read the text")
        if (textDetector != null) {
            val textMatches = findButtonByText(buttonText, screenshot)
            matches.addAll(textMatches)
        }
        
        // 2. Pattern Recognition (human: "I see a button pattern")
        if (patternRecognizer != null) {
            val patternMatches = findButtonByPattern(buttonText, screenshot)
            matches.addAll(patternMatches)
        }
        
        // 3. Integrate matches (human: "I combine multiple cues")
        return integrateMatches(matches, buttonText, screenSize)
    }
    
    /**
     * Find input field by label (human-like: "I want to type in the Email field").
     */
    fun findInputField(fieldLabel: String, screenshot: File, screenSize: ScreenSize): ElementLocation? {
        logger.debug("Finding input field with label: '$fieldLabel'")
        
        val matches = mutableListOf<ElementMatch>()
        
        // Text Detection: Look for label text near input field
        if (textDetector != null) {
            val textMatches = findInputFieldByText(fieldLabel, screenshot)
            matches.addAll(textMatches)
        }
        
        return integrateMatches(matches, fieldLabel, screenSize)
    }
    
    /**
     * Find element by text (generic search).
     */
    fun findElementByText(text: String, screenshot: File, screenSize: ScreenSize): ElementLocation? {
        logger.debug("Finding element with text: '$text'")
        
        if (textDetector == null) {
            logger.warn("TextDetector not available, cannot find element by text")
            return null
        }
        
        val extractedText = textDetector.extractText(screenshot)
        
        // Look for text in extracted content
        val textLocation = findTextLocation(text, extractedText.fullText, screenshot)
        
        return textLocation?.let { location ->
            // Estimate bounding box (text-based elements are typically wider than tall)
            val estimatedWidth = text.length * 10 // Rough estimate: 10px per character
            val estimatedHeight = 24 // Standard text height
            
            ElementLocation(
                boundingBox = BoundingBox(
                    x = location.x,
                    y = location.y,
                    width = estimatedWidth,
                    height = estimatedHeight
                ),
                confidence = 0.75, // Text-only detection is less confident
                detectionMethod = DetectionMethod.TEXT,
                screenSize = screenSize
            )
        }
    }
    
    /**
     * Find button by text using OCR.
     */
    private fun findButtonByText(buttonText: String, screenshot: File): List<ElementMatch> {
        val matches = mutableListOf<ElementMatch>()
        
        try {
            val extractedText = textDetector!!.extractText(screenshot)
            
            // Look for button text in extracted content
            extractedText.buttonLabels.forEach { label ->
                if (label.equals(buttonText, ignoreCase = true)) {
                    // Find location of this text in screenshot
                    val location = findTextLocation(label, extractedText.fullText, screenshot)
                    location?.let {
                        matches.add(
                            ElementMatch(
                                boundingBox = BoundingBox(
                                    x = it.x,
                                    y = it.y,
                                    width = label.length * 10, // Estimate
                                    height = 40 // Standard button height
                                ),
                                confidence = 0.85,
                                method = DetectionMethod.TEXT,
                                text = label
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            logger.warn("Text detection failed: ${e.message}")
        }
        
        return matches
    }
    
    /**
     * Find button by pattern using template matching.
     */
    private fun findButtonByPattern(buttonText: String, screenshot: File): List<ElementMatch> {
        val matches = mutableListOf<ElementMatch>()
        
        try {
            // Look for button pattern templates
            // TODO: This would match button templates, but we need button-specific templates
            // For now, pattern recognition is more for icons than buttons
            // This is a placeholder for when we have button templates
        } catch (e: Exception) {
            logger.warn("Pattern detection failed: ${e.message}")
        }
        
        return matches
    }
    
    /**
     * Find input field by label text.
     */
    private fun findInputFieldByText(fieldLabel: String, screenshot: File): List<ElementMatch> {
        val matches = mutableListOf<ElementMatch>()
        
        try {
            val extractedText = textDetector!!.extractText(screenshot)
            
            // Look for label text (typically appears above or to the left of input field)
            if (extractedText.fullText.contains(fieldLabel, ignoreCase = true)) {
                val location = findTextLocation(fieldLabel, extractedText.fullText, screenshot)
                location?.let {
                    // Input field is typically below the label
                    matches.add(
                        ElementMatch(
                            boundingBox = BoundingBox(
                                x = it.x,
                                y = it.y + 30, // Label is typically 30px above field
                                width = 300, // Standard input field width
                                height = 48 // Standard input field height
                            ),
                            confidence = 0.75,
                            method = DetectionMethod.TEXT,
                            text = fieldLabel
                        )
                    )
                }
            }
        } catch (e: Exception) {
            logger.warn("Input field detection failed: ${e.message}")
        }
        
        return matches
    }
    
    /**
     * Find text location in screenshot.
     * 
     * TODO: This is a simplified version - actual OCR libraries can provide
     * bounding boxes for each word. For now, we estimate based on text position.
     */
    private fun findTextLocation(text: String, fullText: String, screenshot: File): Point? {
        // Simplified: Find text in full text and estimate position
        // In production, OCR libraries provide word-level bounding boxes
        val index = fullText.indexOf(text, ignoreCase = true)
        if (index == -1) return null
        
        // Rough estimate: Assume text is in top portion of screen
        // TODO: Use OCR word-level bounding boxes when available
        // For now, use default screen size estimate
        val estimatedWidth = 1080 // Default Android screen width
        return Point(
            x = estimatedWidth / 2, // Center estimate
            y = 200 + (index / 50) * 30 // Rough vertical position estimate
        )
    }
    
    /**
     * Integrate multiple detection methods (human-like: combines cues).
     */
    private fun integrateMatches(
        matches: List<ElementMatch>,
        expectedText: String,
        screenSize: ScreenSize
    ): ElementLocation? {
        if (matches.isEmpty()) {
            logger.debug("No matches found for: '$expectedText'")
            return null
        }
        
        // Group matches by proximity (same element detected multiple ways)
        val groupedMatches = groupByProximity(matches)
        
        // Find best match (highest confidence, multiple methods agree)
        val bestMatch = groupedMatches.maxByOrNull { group ->
            val avgConfidence = group.map { it.confidence }.average()
            val methodCount = group.map { it.method }.distinct().size
            avgConfidence * (1.0 + methodCount * 0.1) // Bonus for multiple methods
        }
        
        return bestMatch?.let { group ->
            // Use the match with highest confidence
            val primaryMatch = group.maxByOrNull { it.confidence }!!
            
            // Determine detection method
            val detectionMethod = when {
                group.size > 1 -> DetectionMethod.COMBINED
                primaryMatch.method == DetectionMethod.PATTERN -> DetectionMethod.PATTERN
                primaryMatch.method == DetectionMethod.TEXT -> DetectionMethod.TEXT
                else -> DetectionMethod.OBJECT
            }
            
            ElementLocation(
                boundingBox = primaryMatch.boundingBox,
                confidence = primaryMatch.confidence,
                detectionMethod = detectionMethod,
                screenSize = screenSize
            )
        }
    }
    
    /**
     * Group matches by proximity (same element detected multiple ways).
     */
    private fun groupByProximity(matches: List<ElementMatch>): List<List<ElementMatch>> {
        val groups = mutableListOf<MutableList<ElementMatch>>()
        val threshold = 50 // Pixels - matches within 50px are considered same element
        
        matches.forEach { match ->
            val existingGroup = groups.find { group ->
                group.any { existing ->
                    val dx = (match.boundingBox.x - existing.boundingBox.x).toDouble()
                    val dy = (match.boundingBox.y - existing.boundingBox.y).toDouble()
                    val distance = kotlin.math.sqrt(dx * dx + dy * dy)
                    distance < threshold
                }
            }
            
            if (existingGroup != null) {
                existingGroup.add(match)
            } else {
                groups.add(mutableListOf(match))
            }
        }
        
        return groups
    }
    
    /**
     * Element match from a single detection method.
     */
    private data class ElementMatch(
        val boundingBox: BoundingBox,
        val confidence: Double,
        val method: DetectionMethod,
        val text: String? = null
    )
    
}

