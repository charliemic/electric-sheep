package com.electricsheep.testautomation.monitoring

import com.electricsheep.testautomation.vision.TextDetector
import com.electricsheep.testautomation.vision.PatternRecognizer
import org.slf4j.LoggerFactory
import java.io.File
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * Evaluates screen state by analyzing screenshots visually (like a human would).
 * 
 * CRITICAL: This component uses VISUAL CUES ONLY for all analysis.
 * It does NOT use Appium's internal element queries or DOM structures.
 * All detection is based on what can be "seen" in screenshots.
 * 
 * Uses parallel visual analysis (like human vision):
 * - Pattern Recognition (OpenCV) - Fast icon/pattern detection (5-20ms)
 * - Text Detection (OCR) - Text reading (200-500ms)
 * - Context Integration - Combines all observations
 * 
 * Detects:
 * - Error messages (red text, error icons) - VISUALLY detected
 * - Warning dialogs (yellow/orange indicators) - VISUALLY detected
 * - Blocking elements (overlays, popups, dialogs) - VISUALLY detected
 * - Unexpected states (wrong screen, missing content) - VISUALLY detected
 * - Success indicators (green checkmarks, success messages) - VISUALLY detected
 * - Loading indicators (spinners, progress bars) - VISUALLY detected
 * 
 * This approach is more realistic - it "sees" the screen like a human would,
 * rather than querying internal element structures.
 * 
 * See: .cursor/rules/visual-first-principle.mdc for the architectural principle.
 * See: docs/testing/RUNTIME_VISUAL_EVALUATION_ARCHITECTURE.md for architecture.
 */
class ScreenEvaluator(
    private val aiVisionAPI: AIVisionAPI? = null,
    private val textDetector: TextDetector? = null,
    private val patternRecognizer: PatternRecognizer? = null
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    
    /**
     * AI Vision API interface for visual analysis.
     */
    interface AIVisionAPI {
        suspend fun analyze(screenshot: File, prompt: String): String
    }
    
    /**
     * Evaluate screen by analyzing a screenshot visually.
     * 
     * @param screenshot The screenshot to analyze
     * @param expectedState What state we expect to see (e.g., "Mood Management screen")
     * @param expectedElements Elements we expect to be visible (e.g., "Mood History", "Save Mood button")
     * @return ScreenEvaluation with observations
     */
    suspend fun evaluateScreen(
        screenshot: File,
        expectedState: String? = null,
        expectedElements: List<String> = emptyList()
    ): ScreenEvaluation {
        val observations = mutableListOf<ScreenObservation>()
        
        try {
            if (aiVisionAPI != null) {
                // Use AI vision for sophisticated visual analysis
                observations.addAll(evaluateWithAIVision(screenshot, expectedState, expectedElements))
            } else {
                // Parallel visual analysis (like human vision)
                // Run OCR and Pattern Recognition in parallel for speed
                observations.addAll(
                    evaluateWithHybridVisualAnalysis(screenshot, expectedState, expectedElements)
                )
            }
        } catch (e: Exception) {
            logger.warn("Error during screen evaluation: ${e.message}")
            observations.add(
                ScreenObservation(
                    type = ScreenObservation.ObservationType.UNEXPECTED_STATE,
                    severity = ScreenObservation.Severity.MEDIUM,
                    message = "Could not fully evaluate screen: ${e.message}",
                    screenshot = screenshot
                )
            )
        }
        
        // Determine overall state
        val overallState = determineOverallState(observations)
        val summary = generateSummary(observations, overallState)
        
        return ScreenEvaluation(
            observations = observations,
            overallState = overallState,
            summary = summary
        )
    }
    
    /**
     * Evaluate using AI vision API (sophisticated visual analysis).
     */
    private suspend fun evaluateWithAIVision(
        screenshot: File,
        expectedState: String?,
        expectedElements: List<String>
    ): List<ScreenObservation> {
        val observations = mutableListOf<ScreenObservation>()
        
        val prompt = buildVisualAnalysisPrompt(expectedState, expectedElements)
        val analysis = aiVisionAPI!!.analyze(screenshot, prompt)
        
        // Parse AI response into observations
        observations.addAll(parseAIVisionResponse(analysis, screenshot))
        
        return observations
    }
    
    /**
     * Build prompt for AI vision analysis.
     */
    private fun buildVisualAnalysisPrompt(expectedState: String?, expectedElements: List<String>): String {
        val parts = mutableListOf<String>()
        
        parts.add("Analyze this mobile app screenshot and identify any issues or notable states.")
        parts.add("")
        parts.add("Look for:")
        parts.add("1. Error messages (red text, error icons, error dialogs)")
        parts.add("2. Warning messages (yellow/orange text, warning icons)")
        parts.add("3. Blocking elements (dialogs, popups, overlays that block interaction)")
        parts.add("4. Loading indicators (spinners, progress bars, 'Loading...' text)")
        parts.add("5. Success indicators (green checkmarks, 'Success' messages, confirmations)")
        
        if (expectedState != null) {
            parts.add("")
            parts.add("Expected state: $expectedState")
            parts.add("Check if the screen matches this expected state.")
        }
        
        if (expectedElements.isNotEmpty()) {
            parts.add("")
            parts.add("Expected visible elements:")
            expectedElements.forEach { parts.add("- $it") }
            parts.add("Check if these elements are visible on the screen.")
        }
        
        parts.add("")
        parts.add("For each issue found, describe:")
        parts.add("- What you see (error message, dialog, etc.)")
        parts.add("- Severity (critical if blocking, high if significant issue, medium if minor)")
        parts.add("- Location or element description if applicable")
        
        parts.add("")
        parts.add("Respond in a structured format, listing each observation clearly.")
        
        return parts.joinToString("\n")
    }
    
    /**
     * Parse AI vision response into observations.
     */
    private fun parseAIVisionResponse(analysis: String, screenshot: File): List<ScreenObservation> {
        val observations = mutableListOf<ScreenObservation>()
        
        // Parse AI response - look for patterns indicating different observation types
        val lines = analysis.lines()
        
        var currentType: ScreenObservation.ObservationType? = null
        var currentSeverity: ScreenObservation.Severity? = null
        var currentMessage = StringBuilder()
        
        for (line in lines) {
            val lowerLine = line.lowercase()
            
            // Detect observation type
            when {
                lowerLine.contains("error") && !lowerLine.contains("no error") -> {
                    if (currentMessage.isNotEmpty()) {
                        observations.add(createObservation(currentType, currentSeverity, currentMessage.toString(), screenshot))
                    }
                    currentType = ScreenObservation.ObservationType.ERROR
                    currentSeverity = ScreenObservation.Severity.CRITICAL
                    currentMessage = StringBuilder(line)
                }
                lowerLine.contains("warning") -> {
                    if (currentMessage.isNotEmpty()) {
                        observations.add(createObservation(currentType, currentSeverity, currentMessage.toString(), screenshot))
                    }
                    currentType = ScreenObservation.ObservationType.WARNING
                    currentSeverity = ScreenObservation.Severity.HIGH
                    currentMessage = StringBuilder(line)
                }
                lowerLine.contains("blocking") || lowerLine.contains("dialog") || lowerLine.contains("popup") -> {
                    if (currentMessage.isNotEmpty()) {
                        observations.add(createObservation(currentType, currentSeverity, currentMessage.toString(), screenshot))
                    }
                    currentType = ScreenObservation.ObservationType.BLOCKING_ELEMENT
                    currentSeverity = ScreenObservation.Severity.CRITICAL
                    currentMessage = StringBuilder(line)
                }
                lowerLine.contains("loading") || lowerLine.contains("spinner") || lowerLine.contains("progress") -> {
                    if (currentMessage.isNotEmpty()) {
                        observations.add(createObservation(currentType, currentSeverity, currentMessage.toString(), screenshot))
                    }
                    currentType = ScreenObservation.ObservationType.LOADING_INDICATOR
                    currentSeverity = ScreenObservation.Severity.MEDIUM
                    currentMessage = StringBuilder(line)
                }
                lowerLine.contains("success") || lowerLine.contains("saved") || lowerLine.contains("completed") -> {
                    if (currentMessage.isNotEmpty()) {
                        observations.add(createObservation(currentType, currentSeverity, currentMessage.toString(), screenshot))
                    }
                    currentType = ScreenObservation.ObservationType.SUCCESS_INDICATOR
                    currentSeverity = ScreenObservation.Severity.POSITIVE
                    currentMessage = StringBuilder(line)
                }
                lowerLine.contains("missing") || lowerLine.contains("not found") || lowerLine.contains("not visible") -> {
                    if (currentMessage.isNotEmpty()) {
                        observations.add(createObservation(currentType, currentSeverity, currentMessage.toString(), screenshot))
                    }
                    currentType = ScreenObservation.ObservationType.MISSING_ELEMENT
                    currentSeverity = ScreenObservation.Severity.HIGH
                    currentMessage = StringBuilder(line)
                }
                lowerLine.contains("unexpected") || lowerLine.contains("wrong screen") -> {
                    if (currentMessage.isNotEmpty()) {
                        observations.add(createObservation(currentType, currentSeverity, currentMessage.toString(), screenshot))
                    }
                    currentType = ScreenObservation.ObservationType.UNEXPECTED_STATE
                    currentSeverity = ScreenObservation.Severity.HIGH
                    currentMessage = StringBuilder(line)
                }
                else -> {
                    // Continue building current message
                    if (currentMessage.isNotEmpty()) {
                        currentMessage.append(" ").append(line)
                    }
                }
            }
            
            // Detect severity keywords
            when {
                lowerLine.contains("critical") || lowerLine.contains("blocking") -> currentSeverity = ScreenObservation.Severity.CRITICAL
                lowerLine.contains("high") || lowerLine.contains("significant") -> currentSeverity = currentSeverity ?: ScreenObservation.Severity.HIGH
                lowerLine.contains("medium") || lowerLine.contains("minor") -> currentSeverity = currentSeverity ?: ScreenObservation.Severity.MEDIUM
                lowerLine.contains("low") -> currentSeverity = currentSeverity ?: ScreenObservation.Severity.LOW
            }
        }
        
        // Add final observation
        if (currentMessage.isNotEmpty() && currentType != null) {
            observations.add(createObservation(currentType, currentSeverity, currentMessage.toString(), screenshot))
        }
        
        // If no structured observations found, try to extract from free-form text
        if (observations.isEmpty()) {
            observations.addAll(extractObservationsFromFreeText(analysis, screenshot))
        }
        
        return observations
    }
    
    /**
     * Extract observations from free-form AI text response.
     */
    private fun extractObservationsFromFreeText(text: String, screenshot: File): List<ScreenObservation> {
        val observations = mutableListOf<ScreenObservation>()
        val lowerText = text.lowercase()
        
        // Look for error indicators
        if (lowerText.contains("error") && !lowerText.contains("no error")) {
            val errorMatch = Regex("error[^.]*", RegexOption.IGNORE_CASE).find(text)
            if (errorMatch != null) {
                observations.add(
                    ScreenObservation(
                        type = ScreenObservation.ObservationType.ERROR,
                        severity = ScreenObservation.Severity.CRITICAL,
                        message = errorMatch.value.take(200),
                        screenshot = screenshot
                    )
                )
            }
        }
        
        // Look for blocking elements
        if (lowerText.contains("dialog") || lowerText.contains("popup") || lowerText.contains("blocking")) {
            observations.add(
                ScreenObservation(
                    type = ScreenObservation.ObservationType.BLOCKING_ELEMENT,
                    severity = ScreenObservation.Severity.CRITICAL,
                    message = "Blocking element detected (from visual analysis)",
                    screenshot = screenshot
                )
            )
        }
        
        return observations
    }
    
    /**
     * Create observation from parsed data.
     */
    private fun createObservation(
        type: ScreenObservation.ObservationType?,
        severity: ScreenObservation.Severity?,
        message: String,
        screenshot: File
    ): ScreenObservation {
        return ScreenObservation(
            type = type ?: ScreenObservation.ObservationType.UNEXPECTED_STATE,
            severity = severity ?: ScreenObservation.Severity.MEDIUM,
            message = message.trim().take(200),
            screenshot = screenshot
        )
    }
    
    /**
     * Evaluate using OCR text detection (human-like: reading text from screenshots).
     * 
     * This mimics how humans read text on screens to understand:
     * - Error messages
     * - Screen names
     * - Button labels
     * - Status messages
     */
    private suspend fun evaluateWithOCR(
        screenshot: File,
        expectedState: String?,
        expectedElements: List<String>
    ): List<ScreenObservation> {
        val observations = mutableListOf<ScreenObservation>()
        
        try {
            val extractedText = textDetector!!.extractText(screenshot)
            
            // Detect error messages
            if (extractedText.errorMessages.isNotEmpty()) {
                extractedText.errorMessages.forEach { errorMsg ->
                    observations.add(
                        ScreenObservation(
                            type = ScreenObservation.ObservationType.ERROR,
                            severity = ScreenObservation.Severity.CRITICAL,
                            message = "Error detected via text: $errorMsg",
                            screenshot = screenshot
                        )
                    )
                }
            }
            
            // Detect loading indicators
            if (extractedText.loadingIndicators.isNotEmpty()) {
                extractedText.loadingIndicators.forEach { loadingMsg ->
                    observations.add(
                        ScreenObservation(
                            type = ScreenObservation.ObservationType.LOADING_INDICATOR,
                            severity = ScreenObservation.Severity.MEDIUM,
                            message = "Loading indicator detected: $loadingMsg",
                            screenshot = screenshot
                        )
                    )
                }
            }
            
            // Check if expected screen state matches detected screen indicators
            if (expectedState != null) {
                val screenMatch = extractedText.screenIndicators.any { indicator ->
                    expectedState.contains(indicator, ignoreCase = true) ||
                    indicator.contains(expectedState, ignoreCase = true)
                }
                
                if (!screenMatch && extractedText.screenIndicators.isNotEmpty()) {
                    observations.add(
                        ScreenObservation(
                            type = ScreenObservation.ObservationType.UNEXPECTED_STATE,
                            severity = ScreenObservation.Severity.HIGH,
                            message = "Expected '$expectedState' but detected: ${extractedText.screenIndicators.joinToString(", ")}",
                            screenshot = screenshot
                        )
                    )
                }
            }
            
            // Check if expected elements are present in text
            if (expectedElements.isNotEmpty()) {
                val missingElements = expectedElements.filter { expected ->
                    !extractedText.fullText.contains(expected, ignoreCase = true) &&
                    !extractedText.buttonLabels.any { it.contains(expected, ignoreCase = true) }
                }
                
                if (missingElements.isNotEmpty()) {
                    observations.add(
                        ScreenObservation(
                            type = ScreenObservation.ObservationType.MISSING_ELEMENT,
                            severity = ScreenObservation.Severity.HIGH,
                            message = "Expected elements not found in text: ${missingElements.joinToString(", ")}",
                            screenshot = screenshot
                        )
                    )
                }
            }
            
            logger.debug("OCR analysis: extracted ${extractedText.fullText.length} chars, found ${observations.size} observations")
            
        } catch (e: Exception) {
            logger.warn("OCR analysis failed: ${e.message}")
            observations.add(
                ScreenObservation(
                    type = ScreenObservation.ObservationType.UNEXPECTED_STATE,
                    severity = ScreenObservation.Severity.MEDIUM,
                    message = "OCR analysis failed: ${e.message}",
                    screenshot = screenshot
                )
            )
        }
        
        return observations
    }
    
    /**
     * Hybrid visual analysis using parallel processing (OCR + Pattern Recognition).
     * 
     * This mimics human vision - multiple parallel processes work together:
     * - Pattern Recognition: Fast icon detection (5-20ms)
     * - OCR: Text reading (200-500ms)
     * - Context Integration: Combines all observations
     * 
     * Total latency: max(OCR, Pattern) = ~200-500ms (not sequential sum)
     */
    private suspend fun evaluateWithHybridVisualAnalysis(
        screenshot: File,
        expectedState: String?,
        expectedElements: List<String>
    ): List<ScreenObservation> = coroutineScope {
        val observations = mutableListOf<ScreenObservation>()
        
        // Run OCR and Pattern Recognition in parallel (like human vision)
        val ocrObservations = async {
            if (textDetector != null) {
                evaluateWithOCR(screenshot, expectedState, expectedElements)
            } else {
                emptyList()
            }
        }
        
        val patternObservations = async {
            if (patternRecognizer != null) {
                evaluateWithPatternRecognition(screenshot, expectedState, expectedElements)
            } else {
                emptyList()
            }
        }
        
        // Wait for both to complete (parallel execution)
        val (ocrResults, patternResults) = awaitAll(ocrObservations, patternObservations)
        
        // Combine observations
        observations.addAll(ocrResults)
        observations.addAll(patternResults)
        
        // Context integration: Resolve conflicts and combine insights
        observations.addAll(integrateContext(ocrResults, patternResults, screenshot, expectedState, expectedElements))
        
        logger.debug("Hybrid visual analysis: ${ocrResults.size} OCR observations, ${patternResults.size} pattern observations, ${observations.size} total")
        
        observations
    }
    
    /**
     * Evaluate using pattern recognition (fast icon/pattern detection).
     */
    private suspend fun evaluateWithPatternRecognition(
        screenshot: File,
        expectedState: String?,
        expectedElements: List<String>
    ): List<ScreenObservation> {
        val observations = mutableListOf<ScreenObservation>()
        
        try {
            val patternResult = patternRecognizer!!.detectPatterns(screenshot)
            
            // Detect error icons
            if (patternResult.hasErrorIcon) {
                observations.add(
                    ScreenObservation(
                        type = ScreenObservation.ObservationType.ERROR,
                        severity = ScreenObservation.Severity.CRITICAL,
                        message = "Error icon detected via pattern recognition",
                        screenshot = screenshot
                    )
                )
            }
            
            // Detect loading spinners
            if (patternResult.hasLoadingSpinner) {
                observations.add(
                    ScreenObservation(
                        type = ScreenObservation.ObservationType.LOADING_INDICATOR,
                        severity = ScreenObservation.Severity.MEDIUM,
                        message = "Loading spinner detected via pattern recognition",
                        screenshot = screenshot
                    )
                )
            }
            
            // Detect success checkmarks
            if (patternResult.hasSuccessCheckmark) {
                observations.add(
                    ScreenObservation(
                        type = ScreenObservation.ObservationType.SUCCESS_INDICATOR,
                        severity = ScreenObservation.Severity.POSITIVE,
                        message = "Success checkmark detected via pattern recognition",
                        screenshot = screenshot
                    )
                )
            }
            
            // Detect blocking dialogs
            if (patternResult.hasBlockingDialog) {
                observations.add(
                    ScreenObservation(
                        type = ScreenObservation.ObservationType.BLOCKING_ELEMENT,
                        severity = ScreenObservation.Severity.CRITICAL,
                        message = "Blocking dialog detected via pattern recognition",
                        screenshot = screenshot
                    )
                )
            }
            
            logger.debug("Pattern recognition: detected ${patternResult.detectedPatterns.size} patterns")
            
        } catch (e: Exception) {
            logger.warn("Pattern recognition failed: ${e.message}")
        }
        
        return observations
    }
    
    /**
     * Integrate context from multiple visual processors (like human brain integration).
     * 
     * Resolves conflicts and combines insights from OCR and Pattern Recognition.
     */
    private fun integrateContext(
        ocrObservations: List<ScreenObservation>,
        patternObservations: List<ScreenObservation>,
        screenshot: File,
        expectedState: String?,
        expectedElements: List<String>
    ): List<ScreenObservation> {
        val integrated = mutableListOf<ScreenObservation>()
        
        // If both OCR and Pattern Recognition detect errors, confirm it's a real error
        val ocrHasError = ocrObservations.any { it.type == ScreenObservation.ObservationType.ERROR }
        val patternHasError = patternObservations.any { it.type == ScreenObservation.ObservationType.ERROR }
        
        if (ocrHasError && patternHasError) {
            // Both detected error - high confidence
            integrated.add(
                ScreenObservation(
                    type = ScreenObservation.ObservationType.ERROR,
                    severity = ScreenObservation.Severity.CRITICAL,
                    message = "Error confirmed by both OCR and pattern recognition",
                    screenshot = screenshot
                )
            )
        }
        
        // Similar integration for other observation types...
        
        return integrated
    }
    
    /**
     * Basic visual analysis (fallback when AI, OCR, and Pattern Recognition not available).
     * 
     * Screenshots are saved and can be analyzed using Cursor's vision capabilities.
     * The screenshot path is included in observations for easy review.
     */
    private suspend fun evaluateWithBasicAnalysis(
        screenshot: File,
        expectedState: String?,
        expectedElements: List<String>
    ): List<ScreenObservation> {
        val observations = mutableListOf<ScreenObservation>()
        
        // Note that screenshot is available for Cursor analysis
        observations.add(
            ScreenObservation(
                type = ScreenObservation.ObservationType.UNEXPECTED_STATE,
                severity = ScreenObservation.Severity.LOW,
                message = "Screenshot saved for analysis: ${screenshot.name}. Open in Cursor to analyze visually.",
                screenshot = screenshot
            )
        )
        
        // Create analysis prompt file for easy Cursor interaction
        createCursorAnalysisPrompt(screenshot, expectedState, expectedElements)
        
        return observations
    }
    
    /**
     * Create a prompt file that can be used in Cursor to analyze the screenshot.
     */
    private fun createCursorAnalysisPrompt(
        screenshot: File,
        expectedState: String?,
        expectedElements: List<String>
    ) {
        try {
            val promptFile = File(screenshot.parentFile, "${screenshot.nameWithoutExtension}_cursor_prompt.txt")
            val prompt = buildString {
                appendLine("Analyze this mobile app screenshot: ${screenshot.name}")
                appendLine("")
                appendLine("Screenshot path: ${screenshot.absolutePath}")
                appendLine("")
                appendLine("Look for:")
                appendLine("1. Error messages (red text, error icons, error dialogs)")
                appendLine("2. Warning messages (yellow/orange text, warning icons)")
                appendLine("3. Blocking elements (dialogs, popups, overlays that block interaction)")
                appendLine("4. Loading indicators (spinners, progress bars, 'Loading...' text)")
                appendLine("5. Success indicators (green checkmarks, 'Success' messages, confirmations)")
                
                if (expectedState != null) {
                    appendLine("")
                    appendLine("Expected state: $expectedState")
                    appendLine("Check if the screen matches this expected state.")
                }
                
                if (expectedElements.isNotEmpty()) {
                    appendLine("")
                    appendLine("Expected visible elements:")
                    expectedElements.forEach { appendLine("- $it") }
                    appendLine("Check if these elements are visible on the screen.")
                }
                
                appendLine("")
                appendLine("For each issue found, describe:")
                appendLine("- What you see (error message, dialog, etc.)")
                appendLine("- Severity (critical if blocking, high if significant issue, medium if minor)")
                appendLine("- Location or element description if applicable")
            }
            
            promptFile.writeText(prompt)
            logger.debug("Created Cursor analysis prompt: ${promptFile.absolutePath}")
        } catch (e: Exception) {
            logger.debug("Failed to create Cursor analysis prompt: ${e.message}")
        }
    }
    
    /**
     * Determine overall evaluation state.
     */
    private fun determineOverallState(observations: List<ScreenObservation>): ScreenEvaluation.EvaluationState {
        val critical = observations.filter { it.severity == ScreenObservation.Severity.CRITICAL }
        val high = observations.filter { it.severity == ScreenObservation.Severity.HIGH }
        val blocking = observations.filter { it.type == ScreenObservation.ObservationType.BLOCKING_ELEMENT }
        
        return when {
            critical.isNotEmpty() || blocking.isNotEmpty() -> ScreenEvaluation.EvaluationState.FAIL
            high.isNotEmpty() -> ScreenEvaluation.EvaluationState.PASS_WITH_ISSUES
            observations.isEmpty() -> ScreenEvaluation.EvaluationState.PASS
            else -> ScreenEvaluation.EvaluationState.PASS_WITH_ISSUES
        }
    }
    
    /**
     * Generate summary of observations.
     */
    private fun generateSummary(
        observations: List<ScreenObservation>,
        overallState: ScreenEvaluation.EvaluationState
    ): String {
        val critical = observations.filter { it.severity == ScreenObservation.Severity.CRITICAL }
        val high = observations.filter { it.severity == ScreenObservation.Severity.HIGH }
        val medium = observations.filter { it.severity == ScreenObservation.Severity.MEDIUM }
        val positive = observations.filter { it.severity == ScreenObservation.Severity.POSITIVE }
        
        val parts = mutableListOf<String>()
        
        when (overallState) {
            ScreenEvaluation.EvaluationState.PASS -> parts.add("Screen evaluation: PASS")
            ScreenEvaluation.EvaluationState.PASS_WITH_ISSUES -> parts.add("Screen evaluation: PASS WITH ISSUES")
            ScreenEvaluation.EvaluationState.FAIL -> parts.add("Screen evaluation: FAIL")
            ScreenEvaluation.EvaluationState.UNCERTAIN -> parts.add("Screen evaluation: UNCERTAIN")
        }
        
        if (critical.isNotEmpty()) parts.add("${critical.size} critical issue(s)")
        if (high.isNotEmpty()) parts.add("${high.size} high severity issue(s)")
        if (medium.isNotEmpty()) parts.add("${medium.size} medium severity issue(s)")
        if (positive.isNotEmpty()) parts.add("${positive.size} positive indicator(s)")
        
        return parts.joinToString("; ")
    }
}
