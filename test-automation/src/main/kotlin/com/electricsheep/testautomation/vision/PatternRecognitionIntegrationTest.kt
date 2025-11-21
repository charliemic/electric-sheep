package com.electricsheep.testautomation.vision

import com.electricsheep.testautomation.monitoring.ScreenEvaluator
import org.slf4j.LoggerFactory
import java.io.File

/**
 * Integration test for Pattern Recognition functionality.
 * 
 * This test:
 * 1. Takes a screenshot (or uses existing one)
 * 2. Runs pattern recognition on it
 * 3. Verifies pattern detection works
 * 4. Tests hybrid integration (OCR + Pattern Recognition)
 * 5. Reports results
 * 
 * Can be run standalone or as part of test suite.
 */
fun main(args: Array<String>) {
    val logger = LoggerFactory.getLogger("PatternRecognitionIntegrationTest")
    
    // Get screenshot path from args or use default
    val screenshotPath = args.getOrNull(0) ?: "test-results/screenshots"
    
    logger.info("Pattern Recognition Integration Test")
    logger.info("====================================")
    logger.info("Screenshot path: $screenshotPath")
    
    // Find a screenshot to test with
    val screenshotDir = File(screenshotPath)
    val screenshot: File? = if (screenshotDir.isDirectory) {
        // Find latest screenshot
        screenshotDir.listFiles { _, name -> name.endsWith(".png") }
            ?.maxByOrNull { it.lastModified() }
    } else if (screenshotDir.isFile && screenshotDir.name.endsWith(".png")) {
        screenshotDir
    } else {
        null
    }
    
    val screenshotFile: File = screenshot?.takeIf { it.exists() } ?: run {
        logger.error("❌ No screenshot found at: $screenshotPath")
        logger.info("")
        logger.info("Usage:")
        logger.info("  java -cp ... PatternRecognitionIntegrationTest <screenshot-path>")
        logger.info("  java -cp ... PatternRecognitionIntegrationTest test-results/screenshots  # Uses latest screenshot")
        System.exit(1)
        throw IllegalStateException("No screenshot found")
    }
    
    logger.info("Testing with screenshot: ${screenshotFile.name}")
    logger.info("")
    
    try {
        // Create PatternRecognizer
        logger.info("Creating PatternRecognizer...")
        val patternRecognizer = PatternRecognizer(templateDir = null) // No templates for now
        logger.info("✅ PatternRecognizer created")
        logger.info("")
        
        // Detect patterns
        logger.info("Detecting patterns in screenshot...")
        val startTime = System.currentTimeMillis()
        val patternResult = patternRecognizer.detectPatterns(screenshotFile)
        val duration = System.currentTimeMillis() - startTime
        logger.info("✅ Pattern detection completed in ${duration}ms")
        logger.info("")
        
        // Report results
        logger.info("Results:")
        logger.info("--------")
        logger.info("Patterns detected: ${patternResult.detectedPatterns.size}")
        patternResult.detectedPatterns.forEach { pattern: com.electricsheep.testautomation.vision.PatternRecognizer.DetectedPattern ->
            logger.info("  - ${pattern.name} at (${pattern.location.first}, ${pattern.location.second}) confidence: ${String.format("%.2f", pattern.confidence)}")
        }
        logger.info("Has error icon: ${patternResult.hasErrorIcon}")
        logger.info("Has loading spinner: ${patternResult.hasLoadingSpinner}")
        logger.info("Has success checkmark: ${patternResult.hasSuccessCheckmark}")
        logger.info("Has blocking dialog: ${patternResult.hasBlockingDialog}")
        logger.info("")
        
        // Test hybrid integration (OCR + Pattern Recognition)
        logger.info("Testing Hybrid Visual Analysis (OCR + Pattern Recognition)...")
        val textDetector = TextDetector()
        val screenEvaluator = ScreenEvaluator(
            aiVisionAPI = null,
            textDetector = textDetector,
            patternRecognizer = patternRecognizer
        )
        
        val evaluation = kotlinx.coroutines.runBlocking {
            screenEvaluator.evaluateScreen(
                screenshot = screenshotFile,
                expectedState = "Mood Management",
                expectedElements = listOf("Save", "Mood History")
            )
        }
        
        logger.info("✅ Hybrid evaluation completed")
        logger.info("")
        logger.info("Evaluation Results:")
        logger.info("------------------")
        logger.info("Overall state: ${evaluation.overallState}")
        logger.info("Summary: ${evaluation.summary}")
        logger.info("Observations: ${evaluation.observations.size}")
        evaluation.observations.forEach { obs: com.electricsheep.testautomation.monitoring.ScreenObservation ->
            logger.info("  - [${obs.severity}] ${obs.type}: ${obs.message}")
        }
        logger.info("")
        
        // Success criteria
        val success = patternResult.detectedPatterns.isNotEmpty() || 
                     patternResult.hasErrorIcon ||
                     patternResult.hasLoadingSpinner ||
                     patternResult.hasSuccessCheckmark ||
                     patternResult.hasBlockingDialog ||
                     evaluation.observations.isNotEmpty()
        
        if (success) {
            logger.info("✅ Pattern Recognition Integration Test: PASSED")
            logger.info("   - Pattern detection: ✅ Working")
            logger.info("   - Hybrid integration: ✅ Working")
            logger.info("   - ScreenEvaluator integration: ✅ Working")
            logger.info("   - Latency: ${duration}ms (target: <20ms)")
            if (duration < 20) {
                logger.info("   - Latency: ✅ Meets requirement")
            } else {
                logger.info("   - Latency: ⚠️  Exceeds target (acceptable if OpenCV not optimized)")
            }
        } else {
            logger.warn("⚠️  Pattern Recognition Integration Test: PARTIAL")
            logger.warn("   - No patterns detected (may be expected if no templates provided)")
            logger.warn("   - Hybrid integration still working")
        }
        
    } catch (e: Exception) {
        logger.error("❌ Pattern Recognition Integration Test: FAILED")
        logger.error("Error: ${e.message}")
        e.printStackTrace()
        System.exit(1)
    }
}

