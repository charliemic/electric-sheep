package com.electricsheep.testautomation.vision

import nu.pattern.OpenCV
import org.opencv.core.*
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import org.slf4j.LoggerFactory
import java.io.File

/**
 * Detects known UI patterns using template matching (like human pattern recognition).
 * 
 * This mimics human instant icon recognition - when you see a familiar icon,
 * you recognize it immediately without "reading" it. This component does the same.
 * 
 * Uses OpenCV template matching for fast, exact pattern detection.
 * 
 * **Human Parallel**: "I recognize that icon" - instant pattern recognition
 * **Speed**: 5-20ms per template (very fast)
 * **Accuracy**: 95-99% for exact matches
 * 
 * **Limitations**:
 * - Only works for exact matches (same scale, rotation, appearance)
 * - Requires reference template images
 * - Brittle to UI changes
 * 
 * **When to Use**: Known, stable UI elements (error icons, loading spinners, etc.)
 */
class PatternRecognizer(
    private val templateDir: File? = null
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    
    // Check if OpenCV is available
    private val opencvAvailable: Boolean = run {
        try {
            // Try to load OpenCV native library
            nu.pattern.OpenCV.loadLocally()
            true
        } catch (e: Exception) {
            logger.warn("OpenCV not available - pattern recognition disabled. Install OpenCV or use alternative.")
            false
        }
    }
    
    /**
     * Detected pattern with location and confidence.
     */
    data class DetectedPattern(
        val name: String,
        val location: Point,
        val confidence: Double,
        val size: Size
    )
    
    /**
     * Pattern detection result.
     */
    data class PatternDetectionResult(
        val detectedPatterns: List<DetectedPattern>,
        val hasErrorIcon: Boolean,
        val hasLoadingSpinner: Boolean,
        val hasSuccessCheckmark: Boolean,
        val hasBlockingDialog: Boolean
    )
    
    /**
     * Detect known patterns in screenshot.
     * 
     * @param screenshot The screenshot to analyze
     * @return PatternDetectionResult with detected patterns
     */
    fun detectPatterns(screenshot: File): PatternDetectionResult {
        if (!opencvAvailable) {
            logger.debug("OpenCV not available, returning empty pattern detection")
            return PatternDetectionResult(
                detectedPatterns = emptyList(),
                hasErrorIcon = false,
                hasLoadingSpinner = false,
                hasSuccessCheckmark = false,
                hasBlockingDialog = false
            )
        }
        
        return try {
            logger.debug("Detecting patterns in screenshot: ${screenshot.name}")
            
            // Load screenshot
            val screenshotMat = Imgcodecs.imread(screenshot.absolutePath)
            if (screenshotMat.empty()) {
                logger.warn("Failed to load screenshot: ${screenshot.absolutePath}")
                return PatternDetectionResult(emptyList(), false, false, false, false)
            }
            
            val detectedPatterns = mutableListOf<DetectedPattern>()
            
            // Load and match templates
            val templates = loadTemplates()
            for ((name, templateMat) in templates) {
                val matches = matchTemplate(screenshotMat, templateMat, name)
                detectedPatterns.addAll(matches)
            }
            
            // Determine high-level patterns from detected matches
            val hasErrorIcon = detectedPatterns.any { it.name.contains("error", ignoreCase = true) }
            val hasLoadingSpinner = detectedPatterns.any { it.name.contains("loading", ignoreCase = true) || it.name.contains("spinner", ignoreCase = true) }
            val hasSuccessCheckmark = detectedPatterns.any { it.name.contains("success", ignoreCase = true) || it.name.contains("checkmark", ignoreCase = true) }
            val hasBlockingDialog = detectedPatterns.any { it.name.contains("dialog", ignoreCase = true) || it.name.contains("popup", ignoreCase = true) }
            
            logger.debug("Detected ${detectedPatterns.size} patterns: ${detectedPatterns.map { it.name }}")
            
            PatternDetectionResult(
                detectedPatterns = detectedPatterns,
                hasErrorIcon = hasErrorIcon,
                hasLoadingSpinner = hasLoadingSpinner,
                hasSuccessCheckmark = hasSuccessCheckmark,
                hasBlockingDialog = hasBlockingDialog
            )
        } catch (e: Exception) {
            logger.warn("Pattern detection failed: ${e.message}")
            PatternDetectionResult(emptyList(), false, false, false, false)
        }
    }
    
    /**
     * Load template images from template directory.
     */
    private fun loadTemplates(): Map<String, Mat> {
        val templates = mutableMapOf<String, Mat>()
        
        if (templateDir == null || !templateDir.exists()) {
            logger.debug("No template directory provided, using empty template set")
            return templates
        }
        
        try {
            templateDir.listFiles { _, name -> 
                name.endsWith(".png", ignoreCase = true) || 
                name.endsWith(".jpg", ignoreCase = true)
            }?.forEach { templateFile ->
                val templateMat = Imgcodecs.imread(templateFile.absolutePath)
                if (!templateMat.empty()) {
                    val name = templateFile.nameWithoutExtension
                    templates[name] = templateMat
                    logger.debug("Loaded template: $name")
                }
            }
        } catch (e: Exception) {
            logger.warn("Failed to load templates: ${e.message}")
        }
        
        return templates
    }
    
    /**
     * Match template against screenshot.
     */
    private fun matchTemplate(screenshot: Mat, template: Mat, templateName: String): List<DetectedPattern> {
        val matches = mutableListOf<DetectedPattern>()
        
        try {
            // Create result matrix
            val result = Mat()
            
            // Perform template matching
            Imgproc.matchTemplate(screenshot, template, result, Imgproc.TM_CCOEFF_NORMED)
            
            // Find matches above threshold (80% similarity)
            val threshold = 0.8
            val minMaxLoc = Core.minMaxLoc(result)
            
            if (minMaxLoc.maxVal >= threshold) {
                // Found a match
                val location = minMaxLoc.maxLoc
                matches.add(
                    DetectedPattern(
                        name = templateName,
                        location = location,
                        confidence = minMaxLoc.maxVal,
                        size = template.size()
                    )
                )
                
                logger.debug("Pattern '$templateName' detected at (${location.x}, ${location.y}) with confidence ${minMaxLoc.maxVal}")
            }
        } catch (e: Exception) {
            logger.warn("Template matching failed for '$templateName': ${e.message}")
        }
        
        return matches
    }
}

