package com.electricsheep.testautomation.vision

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
    private val templateDir: File? = null,
    private val templateManager: com.electricsheep.testautomation.templates.HybridTemplateManager? = null
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    
    // Check if OpenCV is available
    private val opencvAvailable: Boolean = run {
        try {
            // Try to load OpenCV native library
            // Note: org.openpnp:opencv handles native library loading automatically
            // If loading fails, we'll catch it and disable pattern recognition
            try {
                System.loadLibrary(Core.NATIVE_LIBRARY_NAME)
            } catch (e: UnsatisfiedLinkError) {
                // Try alternative loading method
                Core.getVersionString() // This will fail if OpenCV not loaded
            }
            logger.info("OpenCV loaded successfully - pattern recognition enabled")
            true
        } catch (e: UnsatisfiedLinkError) {
            logger.warn("OpenCV not available - pattern recognition disabled. Install OpenCV or use alternative.")
            false
        } catch (e: Exception) {
            logger.warn("OpenCV not available - pattern recognition disabled: ${e.message}")
            false
        }
    }
    
    /**
     * Detected pattern with location and confidence.
     */
    data class DetectedPattern(
        val name: String,
        val location: Pair<Double, Double>, // (x, y) coordinates
        val confidence: Double,
        val size: Pair<Double, Double> // (width, height)
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
            
<<<<<<< HEAD
            // Load templates from directory if provided
            val templates = loadTemplates()
            val detectedPatterns = mutableListOf<DetectedPattern>()
            
            // Match each template
            templates.forEach { (templateName, templateMat) ->
                val matches = matchTemplate(screenshotMat, templateMat, templateName)
                detectedPatterns.addAll(matches)
            }
            
            // Clean up screenshot Mat
            screenshotMat.release()
            
            // Determine pattern types based on detected patterns
            val hasErrorIcon = detectedPatterns.any { it.name.contains("error", ignoreCase = true) }
            val hasLoadingSpinner = detectedPatterns.any { it.name.contains("loading", ignoreCase = true) || it.name.contains("spinner", ignoreCase = true) }
            val hasSuccessCheckmark = detectedPatterns.any { it.name.contains("success", ignoreCase = true) || it.name.contains("checkmark", ignoreCase = true) }
            val hasBlockingDialog = detectedPatterns.any { it.name.contains("dialog", ignoreCase = true) || it.name.contains("modal", ignoreCase = true) }
            
            logger.debug("Pattern detection complete: found ${detectedPatterns.size} patterns")
=======
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
>>>>>>> origin/main
            
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
<<<<<<< HEAD
     * 
     * @return Map of template names to OpenCV Mat objects
=======
>>>>>>> origin/main
     */
    private fun loadTemplates(): Map<String, Mat> {
        val templates = mutableMapOf<String, Mat>()
        
<<<<<<< HEAD
        if (templateDir == null || !templateDir.exists() || !templateDir.isDirectory) {
            logger.debug("No template directory provided or directory doesn't exist")
            return templates
        }
        
        // Load all image files from template directory
        templateDir.listFiles { _, name ->
            name.endsWith(".png", ignoreCase = true) || 
            name.endsWith(".jpg", ignoreCase = true) || 
            name.endsWith(".jpeg", ignoreCase = true)
        }?.forEach { templateFile ->
            try {
                val templateMat = Imgcodecs.imread(templateFile.absolutePath)
                if (!templateMat.empty()) {
                    val templateName = templateFile.nameWithoutExtension
                    templates[templateName] = templateMat
                    logger.debug("Loaded template: $templateName")
                } else {
                    logger.warn("Failed to load template: ${templateFile.absolutePath}")
                }
            } catch (e: Exception) {
                logger.warn("Error loading template ${templateFile.name}: ${e.message}")
            }
=======
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
>>>>>>> origin/main
        }
        
        return templates
    }
    
    /**
<<<<<<< HEAD
     * Match template against screenshot using OpenCV template matching.
     * 
     * @param screenshot OpenCV Mat of the screenshot
     * @param template OpenCV Mat of the template
     * @param templateName Name of the template for identification
     * @return List of detected pattern matches
=======
     * Match template against screenshot.
>>>>>>> origin/main
     */
    private fun matchTemplate(screenshot: Mat, template: Mat, templateName: String): List<DetectedPattern> {
        val matches = mutableListOf<DetectedPattern>()
        
        try {
<<<<<<< HEAD
            // Template matching requires screenshot to be larger than template
            if (screenshot.rows() < template.rows() || screenshot.cols() < template.cols()) {
                logger.debug("Screenshot too small for template $templateName")
                return matches
            }
            
            // Create result matrix for template matching
            val result = Mat()
            val resultRows = screenshot.rows() - template.rows() + 1
            val resultCols = screenshot.cols() - template.cols() + 1
            result.create(resultRows, resultCols, CvType.CV_32FC1)
=======
            // Create result matrix
            val result = Mat()
>>>>>>> origin/main
            
            // Perform template matching
            Imgproc.matchTemplate(screenshot, template, result, Imgproc.TM_CCOEFF_NORMED)
            
<<<<<<< HEAD
            // Find matches above confidence threshold (0.8 = 80% match)
            val confidenceThreshold = 0.8
            val minMaxLoc = Core.minMaxLoc(result)
            val maxVal = minMaxLoc.maxVal
            
            if (maxVal >= confidenceThreshold) {
                // Found a match - get location
                val matchLoc = minMaxLoc.maxLoc
                matches.add(
                    DetectedPattern(
                        name = templateName,
                        location = Pair(matchLoc.x.toDouble(), matchLoc.y.toDouble()),
                        confidence = maxVal,
                        size = Pair(template.cols().toDouble(), template.rows().toDouble())
                    )
                )
                logger.debug("Found match for $templateName at (${matchLoc.x}, ${matchLoc.y}) with confidence $maxVal")
            }
            
            result.release()
        } catch (e: Exception) {
            logger.warn("Template matching failed for $templateName: ${e.message}")
=======
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
>>>>>>> origin/main
        }
        
        return matches
    }
}

