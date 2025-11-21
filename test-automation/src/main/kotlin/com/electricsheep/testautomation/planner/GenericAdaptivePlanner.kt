package com.electricsheep.testautomation.planner

import com.electricsheep.testautomation.actions.HumanAction
import com.electricsheep.testautomation.monitoring.ScreenEvaluator
import com.electricsheep.testautomation.vision.TextDetector
import org.slf4j.LoggerFactory
import java.io.File

// Import types from TaskPlanner (now public)
import com.electricsheep.testautomation.planner.TaskPlanner.PlanResult
import com.electricsheep.testautomation.planner.TaskPlanner.ExecutionStep

/**
 * Generic adaptive planner that works out how to achieve ANY abstract goal.
 * 
 * **CRITICAL PRINCIPLE: This planner is AGNOSTIC to the test journey.**
 * 
 * **Rules:**
 * - ❌ NEVER include app-specific knowledge (e.g., "mood", app names, feature names)
 * - ✅ ALWAYS use metadata hints from TaskDecomposer (passed via AbstractGoal.metadata)
 * - ✅ ALWAYS use generic patterns (authentication, forms, navigation)
 * - ✅ MUST work without hints (generic patterns only)
 * 
 * **How it works:**
 * 1. Receives an abstract goal (e.g., "Authenticate", "AddDataEntry") with optional metadata hints
 * 2. Observes current screen visually
 * 3. Identifies gap between current state and goal
 * 4. Generates actions to close the gap (using hints if available, generic patterns otherwise)
 * 5. Adapts based on feedback
 * 
 * **Architecture:**
 * - TaskDecomposer extracts task semantics (e.g., "mood" from "add mood value")
 * - TaskDecomposer passes as metadata hint (metadata={"dataType": "mood"})
 * - This planner uses hints as additional patterns, but doesn't know what "mood" means
 * - All context comes from metadata, never hardcoded
 * 
 * See: `.cursor/rules/agnostic-planner-principle.mdc` for strict rules
 */
class GenericAdaptivePlanner(
    private val screenEvaluator: ScreenEvaluator,
    private val textDetector: TextDetector?
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    
    /**
     * Generate actions to achieve an abstract goal.
     * 
     * @param goal Abstract goal to achieve (may contain metadata hints from TaskDecomposer)
     * @param screenshot Current screen state
     * @param history Previous actions attempted
     * @return Plan with actions to achieve goal
     */
    suspend fun generatePlanForGoal(
        goal: TaskDecomposer.AbstractGoal,
        screenshot: File,
        history: List<ExecutionStep>
    ): PlanResult {
        // Extract metadata hints (e.g., "dataType" -> "mood") - these are hints, not requirements
        val dataTypeHint = goal.metadata["dataType"] as? String
        logger.info("🧠 GENERIC PLANNING: Working out how to achieve: ${goal.type.name}")
        
        // Step 1: VISUAL OBSERVATION - What do we see?
        val evaluation = screenEvaluator.evaluateScreen(screenshot)
        
        // Use new two-layer detection: content (generic) + affordance (interaction patterns)
        val interactiveElements = screenEvaluator.detectInteractiveElements(screenshot)
        
        // Extract text for state detection (still needed for screen type detection)
        val extractedText = textDetector?.extractText(screenshot)
        val visibleText = extractedText?.fullText?.lowercase() ?: ""
        
        // Get interactive elements with their affordances
        // Filter by affordance type for planning
        val tappableElements = interactiveElements.filter { 
            it.affordance.type == com.electricsheep.testautomation.monitoring.ScreenEvaluator.InteractionType.TAPPABLE 
        }
        val typeableElements = interactiveElements.filter { 
            it.affordance.type == com.electricsheep.testautomation.monitoring.ScreenEvaluator.InteractionType.TYPEABLE 
        }
        
        // Extract text content from interactive elements (for matching)
        val allButtons = tappableElements.mapNotNull { it.content.text }.distinct()
        val visibleInputs = typeableElements.mapNotNull { it.content.text }.distinct()
        
        // Fallback: if no interactive elements detected, use old method
        val buttonLikeText = if (allButtons.isEmpty()) {
            extractButtonLikeText(visibleText, dataTypeHint)
        } else {
            emptyList()
        }
        val finalButtons = if (allButtons.isNotEmpty()) allButtons else buttonLikeText
        
        val screenName = evaluation.observations.firstOrNull { 
            it.type == com.electricsheep.testautomation.monitoring.ScreenObservation.ObservationType.UNEXPECTED_STATE 
        }?.message?.substringAfter("but detected: ")?.substringBefore(",") ?: "unknown"
        logger.info("👁️  OBSERVED: Screen=$screenName, InteractiveElements=${interactiveElements.size} (${tappableElements.size} tappable, ${typeableElements.size} typeable), Buttons=${finalButtons.size}, Inputs=${visibleInputs.size}")
        
        // Diagnostic logging for OCR
        if (visibleText.isEmpty()) {
            logger.warn("⚠️  No text extracted from screenshot - OCR may not be working or screenshot is empty")
            logger.warn("   Screenshot path: ${screenshot.absolutePath}")
            logger.warn("   Screenshot exists: ${screenshot.exists()}")
            logger.warn("   Screenshot size: ${screenshot.length()} bytes")
            logger.warn("   TextDetector available: ${textDetector != null}")
        } else {
            logger.info("   📝 OCR extracted ${visibleText.length} characters of text")
            logger.debug("   Full text length: ${visibleText.length}, Visible buttons: $allButtons, Visible inputs: $visibleInputs")
            // Show first 300 chars of extracted text for debugging
            val textPreview = extractedText?.fullText?.take(300) ?: visibleText.take(300)
            logger.info("   📄 Text preview (first 300 chars): $textPreview${if ((extractedText?.fullText?.length ?: visibleText.length) > 300) "..." else ""}")
        }
        
        // Step 2: ORIENT - Match current state to goal (use finalButtons for better detection)
        val currentState = determineCurrentState(evaluation, visibleText, finalButtons, visibleInputs)
        val goalState = determineGoalState(goal.type)
        val gap = identifyGap(goalState, currentState, goal.type)
        
        logger.info("🎯 GOAL STATE: $goalState, CURRENT STATE: $currentState, GAP: $gap")
        
        // Step 3: DECIDE - Generate actions to close gap
        // Use interactive elements for better action generation
        val actions = generateActionsForGap(
            gap = gap,
            goalType = goal.type,
            currentState = currentState,
            visibleButtons = finalButtons,
            visibleInputs = visibleInputs,
            visibleText = visibleText,
            history = history,
            metadataHints = goal.metadata, // Pass metadata hints (e.g., dataType="mood")
            interactiveElements = interactiveElements // Pass interactive elements for affordance-based actions
        )
        
        if (actions.isEmpty()) {
            logger.warn("⚠️  Could not generate actions for gap: $gap")
            logger.info("💡 Returning empty plan - higher layer (TaskPlanner) should handle fallback")
            // Return empty success (not failure) - let TaskPlanner fall back to simple planning
            // This maintains agnostic principle - planner doesn't know about app structure
            return PlanResult.Success(emptyList())
        }
        
        logger.info("📋 Generated ${actions.size} actions to achieve ${goal.type.name}")
        return PlanResult.Success(actions)
    }
    
    /**
     * Detect interactive elements (cards, buttons, etc.) based on text regions.
     * If text is dominant (large, prominent, or in a card-like area), it's likely interactive.
     * 
     * @param textRegions Text regions with bounding boxes from OCR
     * @param visibleText Full extracted text for pattern matching
     * @param hint Optional hint (e.g., "mood" for dataType) to prioritize certain text
     * @return List of interactive elements (text that's likely on a card/button)
     */
    private fun detectInteractiveElements(
        textRegions: List<com.electricsheep.testautomation.vision.TextDetector.TextRegion>,
        visibleText: String,
        hint: String?
    ): List<com.electricsheep.testautomation.vision.TextDetector.TextRegion> {
        if (textRegions.isEmpty()) {
            return emptyList()
        }
        
        // Calculate average text size to identify prominent text
        val avgHeight = textRegions.map { it.height }.average()
        val avgWidth = textRegions.map { it.width }.average()
        
        // Find text that's larger than average (likely on cards/buttons)
        val prominentText = textRegions.filter { region ->
            // Text is prominent if:
            // 1. Height is above average (larger text = more important)
            // 2. Width is reasonable (not too narrow, not too wide)
            // 3. Confidence is good
            // 4. Text is not too short
            val isLargerThanAverage = region.height > avgHeight * 0.8
            val isReasonableWidth = region.width > 20 && region.width < 500
            val hasGoodConfidence = region.confidence > 50.0
            val isNotTooShort = region.text.length >= 3
            
            isLargerThanAverage && isReasonableWidth && hasGoodConfidence && isNotTooShort
        }
        
        // If hint provided, prioritize text containing the hint
        val prioritized = if (hint != null) {
            val hintMatches = prominentText.filter { 
                it.text.contains(hint, ignoreCase = true) 
            }
            if (hintMatches.isNotEmpty()) {
                hintMatches
            } else {
                prominentText
            }
        } else {
            prominentText
        }
        
        // Also look for common interactive patterns (generic)
        val patternMatches = textRegions.filter { region ->
            val lowerText = region.text.lowercase()
            val interactivePatterns = listOf(
                "sign in", "sign up", "login", "create", "add", "save", "submit",
                "continue", "next", "get started", "view", "history", "manage"
            )
            interactivePatterns.any { lowerText.contains(it) } &&
            region.height > avgHeight * 0.7 &&
            region.confidence > 50.0
        }
        
        // Combine and deduplicate
        val allInteractive = (prioritized + patternMatches).distinctBy { it.text }
        
        logger.debug("Detected ${allInteractive.size} interactive elements from ${textRegions.size} text regions")
        allInteractive.forEach { region ->
            logger.debug("   Interactive: '${region.text}' at (${region.x}, ${region.y}), size: ${region.width}x${region.height}, confidence: ${region.confidence}")
        }
        
        return allInteractive
    }
    
    /**
     * Extract button-like text from visible text (fallback when buttonLabels is empty).
     * Looks for GENERIC button patterns - no app-specific knowledge.
     * 
     * @param visibleText Text extracted from screenshot
     * @param hint Optional hint from TaskDecomposer (e.g., "mood" for dataType) - used as additional pattern
     */
    private fun extractButtonLikeText(visibleText: String, hint: String? = null): List<String> {
        val buttons = mutableListOf<String>()
        
        // GENERIC button patterns only - no hardcoded app-specific terms
        val genericPatterns = listOf(
            "sign in", "sign up", "login", "create account", "get started",
            "continue", "next", "submit", "save", "cancel", "ok", "done",
            "add", "create", "new", "view", "history", "list", "settings", "profile"
        )
        
        genericPatterns.forEach { pattern ->
            if (visibleText.contains(pattern, ignoreCase = true)) {
                buttons.add(pattern)
            }
        }
        
        // If hint provided (e.g., "mood"), also look for hint-related patterns
        // This is context-aware but still generic - the hint comes from task decomposition, not hardcoded
        if (hint != null) {
            val hintPatterns = listOf(
                hint, // e.g., "mood"
                "add $hint", // e.g., "add mood"
                "$hint management", // e.g., "mood management"
                "track $hint", // e.g., "track mood"
                "view $hint" // e.g., "view mood"
            )
            hintPatterns.forEach { pattern ->
                if (visibleText.contains(pattern, ignoreCase = true)) {
                    buttons.add(pattern)
                }
            }
        }
        
        return buttons.distinct()
    }
    
    /**
     * Detect input fields from visible text.
     */
    private fun detectInputFields(visibleText: String, visibleButtons: List<String>): List<String> {
        val inputs = mutableListOf<String>()
        
        // Common input field indicators
        val inputPatterns = listOf("email", "password", "username", "name", "score", "value", "enter", "input")
        
        inputPatterns.forEach { pattern ->
            if (visibleText.contains(pattern, ignoreCase = true)) {
                inputs.add(pattern)
            }
        }
        
        return inputs.distinct()
    }
    
    /**
     * Determine current state from visual observation.
     */
    private fun determineCurrentState(
        evaluation: com.electricsheep.testautomation.monitoring.ScreenEvaluation,
        visibleText: String,
        visibleButtons: List<String>,
        visibleInputs: List<String>
    ): CurrentState {
        // Detect screen type (generic, not app-specific)
        val screenType = when {
            visibleText.contains("sign in") || visibleText.contains("sign up") || 
            visibleText.contains("login") || visibleText.contains("authenticate") -> ScreenType.AUTHENTICATION_SCREEN
            visibleInputs.isNotEmpty() && visibleButtons.any { it.contains("save", ignoreCase = true) || it.contains("submit", ignoreCase = true) } -> ScreenType.FORM_SCREEN
            visibleText.contains("history") || visibleText.contains("list") || visibleText.contains("view") -> ScreenType.DATA_VIEW_SCREEN
            visibleButtons.isEmpty() && visibleInputs.isEmpty() -> ScreenType.LANDING_SCREEN
            else -> ScreenType.UNKNOWN
        }
        
        // Detect authentication state (generic)
        val isAuthenticated = visibleText.contains("sign out") || 
                             visibleText.contains("logout") ||
                             visibleText.contains("profile") ||
                             (visibleText.contains("history") && !visibleText.contains("sign in"))
        
        // Detect form state (generic)
        val hasFormFields = visibleInputs.isNotEmpty()
        val hasSubmitButton = visibleButtons.any { 
            it.contains("save", ignoreCase = true) || 
            it.contains("submit", ignoreCase = true) ||
            it.contains("create", ignoreCase = true) ||
            it.contains("add", ignoreCase = true)
        }
        
        return CurrentState(
            screenType = screenType,
            isAuthenticated = isAuthenticated,
            hasFormFields = hasFormFields,
            hasSubmitButton = hasSubmitButton,
            visibleButtons = visibleButtons, // visibleButtons parameter is actually allButtons
            visibleInputs = visibleInputs
        )
    }
    
    /**
     * Determine what state we need to be in for the goal.
     */
    private fun determineGoalState(goalType: TaskDecomposer.AbstractGoalType): GoalState {
        return when (goalType) {
            TaskDecomposer.AbstractGoalType.AUTHENTICATE -> GoalState.AUTHENTICATED
            TaskDecomposer.AbstractGoalType.ADD_DATA_ENTRY -> GoalState.DATA_ENTRY_COMPLETE
            TaskDecomposer.AbstractGoalType.VIEW_DATA -> GoalState.VIEWING_DATA
            TaskDecomposer.AbstractGoalType.NAVIGATE_TO_FEATURE -> GoalState.AT_FEATURE
            else -> GoalState.UNKNOWN
        }
    }
    
    /**
     * Identify gap between current state and goal state.
     */
    private fun identifyGap(
        goalState: GoalState,
        currentState: CurrentState,
        goalType: TaskDecomposer.AbstractGoalType
    ): Gap {
        return when (goalState) {
            GoalState.AUTHENTICATED -> {
                when {
                    !currentState.isAuthenticated && currentState.screenType != ScreenType.AUTHENTICATION_SCREEN -> {
                        Gap.NAVIGATE_TO_AUTHENTICATION
                    }
                    !currentState.isAuthenticated && currentState.screenType == ScreenType.AUTHENTICATION_SCREEN -> {
                        if (!currentState.hasFormFields) {
                            Gap.SHOW_AUTHENTICATION_FORM
                        } else {
                            Gap.FILL_AUTHENTICATION_FORM
                        }
                    }
                    currentState.isAuthenticated -> {
                        Gap.VERIFY_AUTHENTICATION
                    }
                    else -> Gap.UNKNOWN
                }
            }
            GoalState.DATA_ENTRY_COMPLETE -> {
                when {
                    !currentState.isAuthenticated -> {
                        Gap.AUTHENTICATE_FIRST
                    }
                    currentState.screenType != ScreenType.FORM_SCREEN -> {
                        Gap.NAVIGATE_TO_FORM
                    }
                    currentState.hasFormFields && currentState.hasSubmitButton -> {
                        Gap.FILL_AND_SUBMIT_FORM
                    }
                    else -> {
                        Gap.FIND_FORM
                    }
                }
            }
            GoalState.VIEWING_DATA -> {
                when {
                    !currentState.isAuthenticated -> {
                        Gap.AUTHENTICATE_FIRST
                    }
                    currentState.screenType != ScreenType.DATA_VIEW_SCREEN -> {
                        Gap.NAVIGATE_TO_DATA_VIEW
                    }
                    else -> {
                        Gap.VERIFY_DATA_VISIBLE
                    }
                }
            }
            GoalState.AT_FEATURE -> {
                // Generic navigation gap
                Gap.NAVIGATE_TO_FEATURE
            }
            else -> Gap.UNKNOWN
        }
    }
    
    /**
     * Generate actions to close the identified gap.
     */
    private fun generateActionsForGap(
        gap: Gap,
        goalType: TaskDecomposer.AbstractGoalType,
        currentState: CurrentState,
        visibleButtons: List<String>,
        visibleInputs: List<String>,
        visibleText: String,
        history: List<ExecutionStep>,
        metadataHints: Map<String, Any> = emptyMap(), // Hints from TaskDecomposer (e.g., dataType="mood")
        interactiveElements: List<com.electricsheep.testautomation.monitoring.ScreenEvaluator.InteractiveElement> = emptyList()
    ): List<HumanAction> {
        // Extract hints (used as additional patterns, not hardcoded logic)
        val dataTypeHint = metadataHints["dataType"] as? String
        val actions = mutableListOf<HumanAction>()
        
        when (gap) {
            Gap.NAVIGATE_TO_AUTHENTICATION -> {
                // First, try to use interactive elements (content + affordance)
                val authElement = interactiveElements.firstOrNull { element ->
                    val text = element.content.text?.lowercase() ?: ""
                    (text.contains("sign", ignoreCase = true) ||
                     text.contains("login", ignoreCase = true) ||
                     text.contains("account", ignoreCase = true)) &&
                    element.affordance.type == com.electricsheep.testautomation.monitoring.ScreenEvaluator.InteractionType.TAPPABLE
                }
                
                if (authElement != null) {
                    // Use interactive element - we know it's tappable
                    val targetText = authElement.content.text ?: "authentication"
                    logger.info("💡 Found tappable element for authentication: '$targetText' (affordance: ${authElement.affordance.type})")
                    actions.add(HumanAction.Tap(target = targetText, accessibilityId = targetText))
                } else {
                    // Fallback: Find any button that might lead to authentication (GENERIC - no app-specific terms)
                    val authButton = visibleButtons.firstOrNull { button ->
                        button.contains("sign", ignoreCase = true) ||
                        button.contains("login", ignoreCase = true) ||
                        button.contains("account", ignoreCase = true)
                    }
                    if (authButton != null) {
                        actions.add(HumanAction.Tap(target = authButton, accessibilityId = authButton))
                    } else {
                        // Fallback: Try any visible button (might be navigation to auth)
                        val anyButton = visibleButtons.firstOrNull()
                        if (anyButton != null) {
                            logger.info("⚠️  No explicit auth button found, trying first available button: $anyButton")
                            actions.add(HumanAction.Tap(target = anyButton, accessibilityId = anyButton))
                        } else {
                            // Last resort: Look for any text in visible text that might be clickable
                            // This is a generic fallback - look for common navigation terms
                            logger.warn("⚠️  No buttons found visually, trying text-based navigation")
                            val navText = when {
                                visibleText.contains("sign", ignoreCase = true) -> "Sign in"
                                visibleText.contains("login", ignoreCase = true) -> "Login"
                                visibleText.contains("account", ignoreCase = true) -> "Account"
                                visibleText.isNotEmpty() -> {
                                    // Try to extract first meaningful word/phrase (very generic)
                                    // Filter out numbers, coordinates, and short words
                                    val words = visibleText.split(Regex("\\s+"))
                                        .filter { it.length > 3 }
                                        .filter { !it.matches(Regex("\\d+")) } // No pure numbers
                                        .filter { !it.contains(":") } // No coordinate-like patterns (e.g., "3:210")
                                        .filter { !it.matches(Regex("\\d+:\\d+")) } // No time/coordinate patterns
                                        .take(3)
                                    words.firstOrNull()?.capitalize() ?: null
                                }
                                else -> null
                            }
                            // Validate navText is reasonable before using it
                            // Only accept common navigation terms, not random extracted text
                            val commonNavTerms = listOf("sign", "login", "account", "get started", "continue", "next")
                            val isValidNavText = navText != null && 
                                navText.length >= 4 && // At least 4 characters
                                !navText.matches(Regex("\\d+.*")) && // Not starting with numbers
                                !navText.contains(":") && // Not coordinate-like
                                navText.matches(Regex("[A-Za-z\\s]+")) && // Only letters and spaces
                                commonNavTerms.any { navText.contains(it, ignoreCase = true) } // Must contain common nav term
                            
                            if (isValidNavText && navText != null) {
                                logger.info("💡 Trying text-based tap: $navText")
                                actions.add(HumanAction.Tap(target = navText, text = navText))
                            } else {
                                logger.warn("⚠️  No valid navigation options found (navText='$navText', valid=$isValidNavText) - returning empty plan for fallback")
                                // Return empty - let TaskPlanner fall back to simple planning
                            }
                        }
                    }
                }
            }
            
            Gap.SHOW_AUTHENTICATION_FORM -> {
                // Find button to show form
                val formButton = visibleButtons.firstOrNull { button ->
                    button.contains("email", ignoreCase = true) ||
                    button.contains("password", ignoreCase = true) ||
                    button.contains("show", ignoreCase = true)
                }
                if (formButton != null) {
                    actions.add(HumanAction.Tap(target = formButton, accessibilityId = formButton))
                }
            }
            
            Gap.FILL_AUTHENTICATION_FORM -> {
                // Find input fields visually
                visibleInputs.forEach { inputType ->
                    if (inputType.contains("email", ignoreCase = true) || inputType.contains("username", ignoreCase = true)) {
                        // Generate email based on context (would need persona, but keeping generic)
                        actions.add(HumanAction.TypeText(
                            target = "Email field",
                            text = "user@example.com", // Would be generated by persona manager
                            accessibilityId = "Email address input field"
                        ))
                    } else if (inputType.contains("password", ignoreCase = true)) {
                        actions.add(HumanAction.TypeText(
                            target = "Password field",
                            text = "SecurePass123!",
                            accessibilityId = "Password input field"
                        ))
                    }
                }
                
                // Find submit button
                val submitButton = visibleButtons.firstOrNull { button ->
                    button.contains("create", ignoreCase = true) ||
                    button.contains("sign", ignoreCase = true) ||
                    button.contains("submit", ignoreCase = true)
                }
                if (submitButton != null) {
                    actions.add(HumanAction.Tap(target = submitButton, accessibilityId = submitButton))
                    actions.add(HumanAction.WaitFor(
                        condition = com.electricsheep.testautomation.actions.WaitCondition.LoadingComplete(),
                        timeoutSeconds = 25
                    ))
                }
            }
            
            Gap.FILL_AND_SUBMIT_FORM -> {
                // Fill any visible input fields
                visibleInputs.forEach { inputType ->
                    actions.add(HumanAction.TypeText(
                        target = "$inputType field",
                        text = generateValueForInput(inputType),
                        accessibilityId = "$inputType input field"
                    ))
                }
                
                // Submit form
                val submitButton = visibleButtons.firstOrNull { button ->
                    button.contains("save", ignoreCase = true) ||
                    button.contains("submit", ignoreCase = true) ||
                    button.contains("add", ignoreCase = true)
                }
                if (submitButton != null) {
                    actions.add(HumanAction.Tap(target = submitButton, accessibilityId = submitButton))
                    actions.add(HumanAction.WaitFor(
                        condition = com.electricsheep.testautomation.actions.WaitCondition.LoadingComplete(),
                        timeoutSeconds = 15
                    ))
                }
            }
            
            Gap.VERIFY_AUTHENTICATION -> {
                actions.add(HumanAction.Verify(
                    condition = com.electricsheep.testautomation.actions.VerifyCondition.Authenticated(true)
                ))
            }
            
            Gap.NAVIGATE_TO_FORM, Gap.NAVIGATE_TO_DATA_VIEW, Gap.NAVIGATE_TO_FEATURE -> {
                // Try interactive elements first (especially if hint provided)
                val navElement = if (dataTypeHint != null) {
                    // Look for hint-related content in tappable elements
                    interactiveElements.firstOrNull { element ->
                        val text = element.content.text?.lowercase() ?: ""
                        text.contains(dataTypeHint, ignoreCase = true) &&
                        element.affordance.type == com.electricsheep.testautomation.monitoring.ScreenEvaluator.InteractionType.TAPPABLE
                    }
                } else {
                    // Just find first tappable element
                    interactiveElements.firstOrNull { 
                        it.affordance.type == com.electricsheep.testautomation.monitoring.ScreenEvaluator.InteractionType.TAPPABLE 
                    }
                }
                
                if (navElement != null) {
                    val targetText = navElement.content.text ?: "navigation"
                    logger.info("💡 Found tappable element for navigation: '$targetText' (hint: $dataTypeHint)")
                    actions.add(HumanAction.Tap(target = targetText, accessibilityId = targetText))
                } else {
                    // Fallback: Generic navigation - find any button (GENERIC - no app-specific logic)
                    val navButton = visibleButtons.firstOrNull()
                    if (navButton != null) {
                        actions.add(HumanAction.Tap(target = navButton, accessibilityId = navButton))
                    } else {
                        // Fallback: Look for GENERIC navigation text patterns
                        // Use metadata hints if available (e.g., if dataType="mood", look for "mood" patterns)
                        logger.warn("⚠️  No navigation buttons found visually, trying generic text patterns")
                        val navText = when {
                            // Use hint if available (context-aware but still generic)
                            dataTypeHint != null && visibleText.contains(dataTypeHint, ignoreCase = true) -> {
                                // Look for hint-related patterns (e.g., "mood management", "add mood")
                                when {
                                    visibleText.contains("$dataTypeHint management", ignoreCase = true) -> "$dataTypeHint management"
                                    visibleText.contains("add $dataTypeHint", ignoreCase = true) -> "add $dataTypeHint"
                                    visibleText.contains("track $dataTypeHint", ignoreCase = true) -> "track $dataTypeHint"
                                    else -> dataTypeHint // Just use the hint itself
                                }
                            }
                            // Generic patterns (no hint)
                            visibleText.contains("add", ignoreCase = true) -> "Add"
                            visibleText.contains("create", ignoreCase = true) -> "Create"
                            visibleText.contains("new", ignoreCase = true) -> "New"
                            visibleText.contains("view", ignoreCase = true) -> "View"
                            visibleText.contains("history", ignoreCase = true) -> "History"
                            else -> null
                        }
                        if (navText != null) {
                            actions.add(HumanAction.Tap(target = navText, text = navText))
                        } else {
                            logger.warn("⚠️  No generic navigation patterns found - cannot navigate")
                        }
                    }
                }
            }
            
            Gap.AUTHENTICATE_FIRST -> {
                // Sub-goal - would be handled by goal manager
                // Return empty to let goal manager handle it
            }
            
            Gap.FIND_FORM -> {
                // Look for form - might need to scroll or navigate
                val formButton = visibleButtons.firstOrNull { button ->
                    button.contains("add", ignoreCase = true) ||
                    button.contains("create", ignoreCase = true) ||
                    button.contains("new", ignoreCase = true)
                }
                if (formButton != null) {
                    actions.add(HumanAction.Tap(target = formButton, accessibilityId = formButton))
                }
            }
            
            Gap.VERIFY_DATA_VISIBLE -> {
                // Verify data is visible (generic)
                actions.add(HumanAction.Verify(
                    condition = com.electricsheep.testautomation.actions.VerifyCondition.TextPresent("History")
                ))
            }
            
            Gap.UNKNOWN -> {
                logger.warn("Unknown gap - cannot generate actions")
            }
        }
        
        return actions
    }
    
    /**
     * Generate appropriate value for input field type.
     */
    private fun generateValueForInput(inputType: String): String {
        return when {
            inputType.contains("email", ignoreCase = true) -> "user@example.com"
            inputType.contains("password", ignoreCase = true) -> "SecurePass123!"
            inputType.contains("score", ignoreCase = true) || inputType.contains("value", ignoreCase = true) -> "7"
            inputType.contains("number", ignoreCase = true) -> "5"
            else -> "test value"
        }
    }
    
    // Data classes for state representation (generic, not app-specific)
    enum class ScreenType {
        LANDING_SCREEN,
        AUTHENTICATION_SCREEN,
        FORM_SCREEN,
        DATA_VIEW_SCREEN,
        UNKNOWN
    }
    
    enum class GoalState {
        AUTHENTICATED,
        DATA_ENTRY_COMPLETE,
        VIEWING_DATA,
        AT_FEATURE,
        UNKNOWN
    }
    
    data class CurrentState(
        val screenType: ScreenType,
        val isAuthenticated: Boolean,
        val hasFormFields: Boolean,
        val hasSubmitButton: Boolean,
        val visibleButtons: List<String>,
        val visibleInputs: List<String>
    )
    
    enum class Gap {
        NAVIGATE_TO_AUTHENTICATION,
        SHOW_AUTHENTICATION_FORM,
        FILL_AUTHENTICATION_FORM,
        VERIFY_AUTHENTICATION,
        NAVIGATE_TO_FORM,
        FILL_AND_SUBMIT_FORM,
        NAVIGATE_TO_DATA_VIEW,
        VERIFY_DATA_VISIBLE,
        NAVIGATE_TO_FEATURE,
        AUTHENTICATE_FIRST,
        FIND_FORM,
        UNKNOWN
    }
}

