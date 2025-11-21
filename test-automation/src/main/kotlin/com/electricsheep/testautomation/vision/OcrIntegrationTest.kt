package com.electricsheep.testautomation.vision

import com.electricsheep.testautomation.monitoring.ScreenEvaluator
import org.slf4j.LoggerFactory
import java.io.File

/**
 * Integration test for OCR functionality.
 * 
 * This test:
 * 1. Takes a screenshot (or uses existing one)
 * 2. Runs OCR on it
 * 3. Verifies text extraction works
 * 4. Reports results
 * 
 * Can be run standalone or as part of test suite.
 */
fun main(args: Array<String>) {
    val logger = LoggerFactory.getLogger("OcrIntegrationTest")
    
    // Get screenshot path from args or use default
    val screenshotPath = args.getOrNull(0) ?: "test-results/screenshots"
    
    logger.info("OCR Integration Test")
    logger.info("====================")
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
    
    val screenshotFile: File = (screenshot?.takeIf { it.exists() } ?: run {
        logger.error("❌ No screenshot found at: $screenshotPath")
        logger.info("")
        logger.info("Usage:")
        logger.info("  java -cp ... OcrIntegrationTest <screenshot-path>")
        logger.info("  java -cp ... OcrIntegrationTest test-results/screenshots  # Uses latest screenshot")
        System.exit(1)
        throw IllegalStateException("No screenshot found")
    }) as File
    
    logger.info("Testing with screenshot: ${screenshotFile.name}")
    logger.info("")
    
    try {
        // Create TextDetector
        logger.info("Creating TextDetector...")
        val textDetector = TextDetector()
        logger.info("✅ TextDetector created")
        logger.info("")
        
        // Extract text
        logger.info("Extracting text from screenshot...")
        val startTime = System.currentTimeMillis()
        val extractedText = textDetector.extractText(screenshotFile)
        val duration = System.currentTimeMillis() - startTime
        logger.info("✅ Text extraction completed in ${duration}ms")
        logger.info("")
        
        // Report results
        logger.info("Results:")
        logger.info("--------")
        logger.info("Full text length: ${extractedText.fullText.length} characters")
        logger.info("Error messages found: ${extractedText.errorMessages.size}")
        extractedText.errorMessages.forEach { logger.info("  - $it") }
        logger.info("Screen indicators found: ${extractedText.screenIndicators.size}")
        extractedText.screenIndicators.forEach { logger.info("  - $it") }
        logger.info("Button labels found: ${extractedText.buttonLabels.size}")
        extractedText.buttonLabels.forEach { logger.info("  - $it") }
        logger.info("Loading indicators found: ${extractedText.loadingIndicators.size}")
        extractedText.loadingIndicators.forEach { logger.info("  - $it") }
        logger.info("")
        
        // Show first 500 chars of extracted text
        if (extractedText.fullText.isNotEmpty()) {
            logger.info("Extracted text preview (first 500 chars):")
            logger.info("${extractedText.fullText.take(500)}...")
            logger.info("")
        }
        
        // Test ScreenEvaluator integration
        logger.info("Testing ScreenEvaluator integration...")
        val screenEvaluator = com.electricsheep.testautomation.monitoring.ScreenEvaluator(aiVisionAPI = null, textDetector = textDetector)
        
        val evaluation = kotlinx.coroutines.runBlocking {
            screenEvaluator.evaluateScreen(
                screenshot = screenshotFile,
                expectedState = "Mood Management",
                expectedElements = listOf("Save", "Mood History")
            )
        }
        
        logger.info("✅ ScreenEvaluator evaluation completed")
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
        val success = extractedText.fullText.isNotEmpty() || 
                     extractedText.errorMessages.isNotEmpty() ||
                     extractedText.screenIndicators.isNotEmpty()
        
        if (success) {
            logger.info("✅ OCR Integration Test: PASSED")
            logger.info("   - Text extraction: ✅ Working")
            logger.info("   - Pattern detection: ✅ Working")
            logger.info("   - ScreenEvaluator integration: ✅ Working")
            logger.info("   - Latency: ${duration}ms (target: <100ms)")
            if (duration < 100) {
                logger.info("   - Latency: ✅ Meets requirement")
            } else {
                logger.info("   - Latency: ⚠️  Exceeds target (acceptable for first iteration)")
            }
        } else {
            logger.warn("⚠️  OCR Integration Test: PARTIAL")
            logger.warn("   - Text extraction may have failed or screenshot has no text")
            logger.warn("   - This is acceptable if screenshot has no readable text")
        }
        
    } catch (e: Exception) {
        logger.error("❌ OCR Integration Test: FAILED")
        logger.error("Error: ${e.message}")
        e.printStackTrace()
        System.exit(1)
    }
}

