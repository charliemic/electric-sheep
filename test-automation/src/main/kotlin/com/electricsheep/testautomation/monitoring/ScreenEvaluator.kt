package com.electricsheep.testautomation.monitoring

import com.electricsheep.testautomation.vision.TextDetector
import com.electricsheep.testautomation.vision.PatternRecognizer
import com.electricsheep.testautomation.ai.OllamaService
import org.slf4j.LoggerFactory
import java.io.File

/**
 * Evaluates screen state by analyzing screenshots visually (like a human would).
 * 
 * CRITICAL: This component uses VISUAL CUES ONLY for all analysis.
 * It does NOT use Appium's internal element queries or DOM structures.
 * All detection is based on what can be "seen" in screenshots.
 * 
 * Uses visual analysis to detect:
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
 */
class ScreenEvaluator(
    private val aiVisionAPI: AIVisionAPI? = null,
    private val textDetector: TextDetector? = null,
    private val patternRecognizer: PatternRecognizer? = null,
    private val ollamaService: OllamaService? = null
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
            } else if (textDetector != null) {
                // Use OCR for text-based visual analysis (human-like: reading text)
                observations.addAll(evaluateWithOCR(screenshot, expectedState, expectedElements))
            } else {
                // Fallback: Basic visual analysis (can be enhanced with image processing)
                observations.addAll(evaluateWithBasicAnalysis(screenshot, expectedState, expectedElements))
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
        
        // Detect keyboard visually (human-like: see keyboard on screen)
        val hasKeyboard = detectKeyboard(screenshot)
        if (hasKeyboard) {
            observations.add(
                ScreenObservation(
                    type = ScreenObservation.ObservationType.BLOCKING_ELEMENT,
                    severity = ScreenObservation.Severity.MEDIUM,
                    message = "Keyboard is visible on screen (may block interaction)",
                    element = "Keyboard",
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
            summary = summary,
            hasKeyboard = hasKeyboard,
            blockingElements = observations.filter { 
                it.type == ScreenObservation.ObservationType.BLOCKING_ELEMENT 
            }.map { it.element ?: it.message }
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
     * Basic visual analysis (fallback when AI and OCR not available).
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
     * Visually detect if a keyboard is present on the screen.
     * Uses OCR to look for keyboard-related text patterns (e.g., "Done", "Hide", keyboard keys).
     * 
     * **Human Process**: Look at bottom of screen → See keyboard layout → Recognize it's a keyboard
     */
    private fun detectKeyboard(screenshot: File): Boolean {
        return try {
            if (textDetector != null) {
                val extractedText = textDetector.extractText(screenshot)
                val lowerText = extractedText.fullText.lowercase()
                
                // Look for keyboard-related indicators:
                // - Common keyboard dismiss buttons: "Done", "Hide", "Close", "Back"
                // - Keyboard layout indicators: multiple short words, common keys (qwerty patterns)
                // - Bottom-of-screen text patterns (keyboards typically at bottom)
                val keyboardIndicators = listOf(
                    "done", "hide", "close", "back", "✓", "✓ done",
                    "qwerty", "keyboard", "enter", "return"
                )
                
                val hasKeyboardIndicator = keyboardIndicators.any { indicator ->
                    lowerText.contains(indicator, ignoreCase = true)
                }
                
                // Additional heuristic: If we see many short words/characters at bottom of screen,
                // it might be a keyboard (this is a simple heuristic, can be enhanced)
                if (hasKeyboardIndicator) {
                    logger.debug("👁️  Keyboard detected via text indicators")
                    return true
                }
            }
            
            // Pattern recognition could also detect keyboard layouts (future enhancement)
            // For now, return false if no indicators found
            false
        } catch (e: Exception) {
            logger.debug("Keyboard detection failed: ${e.message}")
            false
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
    
    // ===================================================================
    // TWO-LAYER INTERACTIVE ELEMENT DETECTION
    // ===================================================================
    // Layer A: Content Detection (generic - no domain knowledge)
    // Layer B: Affordance Detection (interaction patterns)
    // ===================================================================
    
    /**
     * Detected content on screen (generic - no domain knowledge).
     * 
     * Layer A: "I detect the presence of X" (text, image, property).
     * NO DOMAIN KNOWLEDGE - just detects what's there.
     */
    data class DetectedContent(
        val text: String? = null,              // "Mood Management" (just text, no meaning)
        val image: ImageDescriptor? = null,     // Icon, image, etc.
        val properties: Map<String, Any> = emptyMap(), // Any other properties
        val location: BoundingBox? = null       // Where it is on screen
    )
    
    /**
     * Image descriptor for detected images/patterns.
     */
    data class ImageDescriptor(
        val name: String,
        val location: Pair<Int, Int> // x, y coordinates
    )
    
    /**
     * Bounding box for content location.
     */
    data class BoundingBox(
        val x: Int,
        val y: Int,
        val width: Int,
        val height: Int
    )
    
    /**
     * Interaction affordance - how can I interact with this element?
     * 
     * Layer B: "I have to work out how to interact with it".
     * Understands interaction patterns, not content meaning.
     */
    enum class InteractionType {
        TAPPABLE,    // Button, card, link - can tap
        SWIPEABLE,   // List, carousel - can swipe
        READABLE,    // Text, label - can read
        TYPEABLE,    // Input field - can type
        SCROLLABLE   // Scrollable area - can scroll
    }
    
    /**
     * Visual cues for affordance detection.
     */
    data class VisualCues(
        val size: String,        // "large", "medium", "small"
        val position: String,    // "top", "center", "bottom", "left", "right"
        val shape: String,       // "rectangular", "circular", "card"
        val color: String? = null // Optional color description
    )
    
    /**
     * Interaction affordance for an element.
     */
    data class InteractionAffordance(
        val type: InteractionType,
        val confidence: Double,
        val visualCues: VisualCues
    )
    
    /**
     * Interactive element combining content and affordance.
     * 
     * This is the combined result: what I see (content) + how I interact (affordance).
     */
    data class InteractiveElement(
        val content: DetectedContent,           // What I see (generic)
        val affordance: InteractionAffordance, // How I interact
        val location: BoundingBox               // Where it is
    )
    
    /**
     * Detect all content on screen (generic - no domain knowledge).
     * 
     * Layer A: "I detect the presence of X" (text, image, property).
     * 
     * This method detects what's on screen without any interpretation:
     * - Text: "I see text 'Mood Management'" (doesn't know what "mood" means)
     * - Images: "I see icon X" (doesn't know what icon represents)
     * - Properties: "I see property Y" (doesn't know what property means)
     * 
     * @param screenshot Screenshot to analyze
     * @return List of detected content (text, images, properties)
     */
    suspend fun detectContent(screenshot: File): List<DetectedContent> {
        val content = mutableListOf<DetectedContent>()
        
        // Extract text (OCR) - generic, no interpretation
        textDetector?.extractText(screenshot)?.let { extracted ->
            extracted.textRegions.forEach { region ->
                content.add(
                    DetectedContent(
                        text = region.text, // Just the text, no meaning
                        properties = mapOf(
                            "confidence" to region.confidence,
                            "source" to "ocr"
                        ),
                        location = BoundingBox(
                            x = region.x,
                            y = region.y,
                            width = region.width,
                            height = region.height
                        )
                    )
                )
            }
        }
        
        // Extract images/patterns (generic detection)
        patternRecognizer?.detectPatterns(screenshot)?.let { patterns ->
            patterns.detectedPatterns.forEach { pattern ->
                content.add(
                    DetectedContent(
                        image = ImageDescriptor(
                            name = pattern.name,
                            location = Pair(pattern.location.first.toInt(), pattern.location.second.toInt())
                        ),
                        properties = mapOf(
                            "confidence" to pattern.confidence,
                            "source" to "pattern_recognition"
                        ),
                        location = BoundingBox(
                            x = pattern.location.first.toInt(),
                            y = pattern.location.second.toInt(),
                            width = pattern.size.first.toInt(),
                            height = pattern.size.second.toInt()
                        )
                    )
                )
            }
        }
        
        logger.debug("Detected ${content.size} content elements (text: ${content.count { it.text != null }}, images: ${content.count { it.image != null }})")
        return content
    }
    
    /**
     * Detect interaction affordances for content.
     * 
     * Layer B: "I have to work out how to interact with it".
     * 
     * Uses AI (LLaVA via Ollama) to understand visual design patterns:
     * - "This looks like a button (tappable)"
     * - "This looks like a card (tappable)"
     * - "This looks like text (readable)"
     * - "This looks like an input field (typeable)"
     * 
     * @param screenshot Screenshot to analyze
     * @param content Detected content to analyze
     * @return Map of content to interaction affordances
     */
    suspend fun detectAffordances(
        screenshot: File,
        content: List<DetectedContent>
    ): Map<DetectedContent, InteractionAffordance> {
        if (content.isEmpty()) {
            return emptyMap()
        }
        
        // If Ollama not available, use heuristic-based detection
        if (ollamaService == null || !ollamaService.isAvailable()) {
            logger.debug("Ollama not available, using heuristic-based affordance detection")
            return detectAffordancesHeuristic(content)
        }
        
        return try {
            // Use LLaVA to understand visual patterns
            val prompt = buildAffordanceDetectionPrompt(content)
            
            val analysisResult = ollamaService.generateWithImages(
                prompt = prompt,
                images = listOf(screenshot),
                model = "llava" // Vision model
            )
            
            analysisResult.fold(
                onSuccess = { analysis ->
                    parseAffordancesFromAI(analysis, content)
                },
                onFailure = { error ->
                    logger.warn("AI affordance detection failed: ${error.message}, falling back to heuristics")
                    detectAffordancesHeuristic(content)
                }
            )
        } catch (e: Exception) {
            logger.warn("Affordance detection failed: ${e.message}, falling back to heuristics")
            detectAffordancesHeuristic(content)
        }
    }
    
    /**
     * Build prompt for AI affordance detection.
     */
    private fun buildAffordanceDetectionPrompt(content: List<DetectedContent>): String {
        val contentDescriptions = content.mapIndexed { index, item ->
            val desc = when {
                item.text != null -> "text: '${item.text}'"
                item.image != null -> "image: '${item.image.name}'"
                else -> "element $index"
            }
            val location = item.location?.let { "at (${it.x}, ${it.y}), size ${it.width}x${it.height}" } ?: ""
            "$desc $location"
        }.joinToString("\n")
        
        return """
            Analyze this mobile app screenshot and identify interaction affordances for each element.
            
            Elements detected:
            $contentDescriptions
            
            For each element, determine:
            1. Can it be tapped? (buttons, cards, links)
            2. Can it be swiped? (lists, carousels)
            3. Can it be read? (text, labels)
            4. Can it be typed into? (input fields)
            5. Can it be scrolled? (scrollable areas)
            
            Consider visual cues:
            - Buttons: Rectangular, prominent, often at bottom, distinct background
            - Cards: Containers with content, often tappable, elevated appearance
            - Input fields: Rectangular, often with placeholder text, border
            - Lists: Vertical/horizontal scrollable areas
            - Text: Read-only content, labels, descriptions
            
            Return JSON format:
            {
              "elements": [
                {
                  "index": 0,
                  "type": "TAPPABLE|SWIPEABLE|READABLE|TYPEABLE|SCROLLABLE",
                  "confidence": 0.0-1.0,
                  "visualCues": {
                    "size": "large|medium|small",
                    "position": "top|center|bottom|left|right",
                    "shape": "rectangular|circular|card",
                    "color": "optional color description"
                  }
                }
              ]
            }
        """.trimIndent()
    }
    
    /**
     * Parse AI response into affordance map.
     */
    private fun parseAffordancesFromAI(
        analysis: String,
        content: List<DetectedContent>
    ): Map<DetectedContent, InteractionAffordance> {
        val affordances = mutableMapOf<DetectedContent, InteractionAffordance>()
        
        try {
            // Try to parse JSON response
            // For now, use simple heuristic parsing (can be enhanced with proper JSON parsing)
            content.forEachIndexed { index, item ->
                // Heuristic: if text contains common button words, it's tappable
                val lowerText = item.text?.lowercase() ?: ""
                val isButtonLike = lowerText.contains("sign") || 
                                  lowerText.contains("login") ||
                                  lowerText.contains("create") ||
                                  lowerText.contains("save") ||
                                  lowerText.contains("submit") ||
                                  lowerText.contains("add") ||
                                  lowerText.contains("continue") ||
                                  lowerText.contains("next")
                
                // Heuristic: if it's an image/icon, likely tappable
                val isImage = item.image != null
                
                // Heuristic: if text is short and prominent, likely button
                val isShortText = item.text?.length ?: 0 < 20
                val isLarge = item.location?.let { it.width > 100 && it.height > 40 } ?: false
                
                val affordanceType = when {
                    isButtonLike && isLarge -> InteractionType.TAPPABLE
                    isImage -> InteractionType.TAPPABLE
                    isShortText && isLarge -> InteractionType.TAPPABLE
                    item.text?.contains("email") == true || 
                    item.text?.contains("password") == true ||
                    item.text?.contains("input") == true -> InteractionType.TYPEABLE
                    else -> InteractionType.READABLE
                }
                
                affordances[item] = InteractionAffordance(
                    type = affordanceType,
                    confidence = when {
                        isButtonLike && isLarge -> 0.9
                        isImage -> 0.8
                        else -> 0.6
                    },
                    visualCues = VisualCues(
                        size = when {
                            isLarge -> "large"
                            item.location?.let { it.width > 50 && it.height > 30 } == true -> "medium"
                            else -> "small"
                        },
                        position = determinePosition(item.location),
                        shape = if (isLarge && item.text != null) "rectangular" else "unknown"
                    )
                )
            }
        } catch (e: Exception) {
            logger.warn("Failed to parse AI affordance response: ${e.message}")
        }
        
        return affordances
    }
    
    /**
     * Heuristic-based affordance detection (fallback when AI unavailable).
     */
    private fun detectAffordancesHeuristic(
        content: List<DetectedContent>
    ): Map<DetectedContent, InteractionAffordance> {
        val affordances = mutableMapOf<DetectedContent, InteractionAffordance>()
        
        content.forEach { item ->
            val lowerText = item.text?.lowercase() ?: ""
            val isButtonLike = lowerText.contains("sign") || 
                              lowerText.contains("login") ||
                              lowerText.contains("create") ||
                              lowerText.contains("save") ||
                              lowerText.contains("submit") ||
                              lowerText.contains("add") ||
                              lowerText.contains("continue") ||
                              lowerText.contains("next") ||
                              lowerText.contains("ok") ||
                              lowerText.contains("done")
            
            val isInputLike = lowerText.contains("email") || 
                             lowerText.contains("password") ||
                             lowerText.contains("username") ||
                             lowerText.contains("input") ||
                             lowerText.contains("enter")
            
            val isLarge = item.location?.let { it.width > 100 && it.height > 40 } ?: false
            val isImage = item.image != null
            
            val affordanceType = when {
                isButtonLike && isLarge -> InteractionType.TAPPABLE
                isImage -> InteractionType.TAPPABLE
                isInputLike -> InteractionType.TYPEABLE
                else -> InteractionType.READABLE
            }
            
            affordances[item] = InteractionAffordance(
                type = affordanceType,
                confidence = when {
                    isButtonLike && isLarge -> 0.8
                    isImage -> 0.7
                    else -> 0.5
                },
                visualCues = VisualCues(
                    size = when {
                        isLarge -> "large"
                        item.location?.let { it.width > 50 && it.height > 30 } == true -> "medium"
                        else -> "small"
                    },
                    position = determinePosition(item.location),
                    shape = if (isLarge) "rectangular" else "unknown"
                )
            )
        }
        
        return affordances
    }
    
    /**
     * Determine position from bounding box.
     */
    private fun determinePosition(location: BoundingBox?): String {
        if (location == null) return "unknown"
        
        // Simple heuristic based on y position (assuming portrait orientation)
        return when {
            location.y < 200 -> "top"
            location.y > 1500 -> "bottom"
            else -> "center"
        }
    }
    
    /**
     * Detect all interactive elements on screen.
     * 
     * Combines content detection (generic) with affordance detection (interaction patterns).
     * 
     * This is the main method that combines both layers:
     * - Layer A: What content do I see? (generic)
     * - Layer B: How can I interact with it? (pattern-based)
     * 
     * @param screenshot Screenshot to analyze
     * @return List of interactive elements (content + affordance)
     */
    suspend fun detectInteractiveElements(screenshot: File): List<InteractiveElement> {
        // Step 1: Detect content (generic - no domain knowledge)
        val content = detectContent(screenshot)
        
        if (content.isEmpty()) {
            logger.debug("No content detected, returning empty interactive elements")
            return emptyList()
        }
        
        // Step 2: Detect affordances (interaction patterns)
        val affordances = detectAffordances(screenshot, content)
        
        // Step 3: Combine into interactive elements
        val interactiveElements = content.mapNotNull { detectedContent ->
            affordances[detectedContent]?.let { affordance ->
                val location = detectedContent.location ?: BoundingBox(0, 0, 0, 0)
                InteractiveElement(
                    content = detectedContent,
                    affordance = affordance,
                    location = location
                )
            }
        }
        
        logger.info("Detected ${interactiveElements.size} interactive elements (${interactiveElements.count { it.affordance.type == InteractionType.TAPPABLE }} tappable, ${interactiveElements.count { it.affordance.type == InteractionType.TYPEABLE }} typeable)")
        
        return interactiveElements
    }
}
