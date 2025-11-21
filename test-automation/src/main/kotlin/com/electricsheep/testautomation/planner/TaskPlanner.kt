package com.electricsheep.testautomation.planner

import com.electricsheep.testautomation.actions.ActionExecutor
import com.electricsheep.testautomation.actions.HumanAction
import com.electricsheep.testautomation.actions.ActionResult
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.slf4j.LoggerFactory
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Task Planner (Layer 1): AI-powered planning with feedback loop.
 * 
 * This planner:
 * 1. Takes a screenshot to understand current state
 * 2. Devises a plan to achieve the task
 * 3. Checks if actions are possible in Appium
 * 4. Executes actions and gets feedback
 * 5. Adjusts plan based on results
 */
class TaskPlanner(
    private val actionExecutor: ActionExecutor,
    private val aiApiKey: String? = null,
    private val aiModel: String = "gpt-4-vision-preview",
    private val personaManager: PersonaManager = PersonaManager(),
    private val screenEvaluator: com.electricsheep.testautomation.monitoring.ScreenEvaluator? = null,
    private val textDetector: com.electricsheep.testautomation.vision.TextDetector? = null
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()
    private val objectMapper = ObjectMapper(YAMLFactory()).apply {
        registerModule(KotlinModule.Builder().build())
    }
    
    // Adaptive planning components
    private val taskDecomposer = TaskDecomposer()
    private val genericAdaptivePlanner = if (screenEvaluator != null && textDetector != null) {
        GenericAdaptivePlanner(screenEvaluator, textDetector)
    } else {
        null
    }
    
    /**
     * Plan and execute a task with feedback loop.
     * 
     * @param task Natural language description of what to do
     * @param context Optional context about the user/persona
     * @param maxIterations Maximum planning iterations (feedback loop)
     */
    suspend fun planAndExecute(
        task: String,
        context: String? = null,
        maxIterations: Int = 10
    ): TaskResult {
        val taskLower = task.lowercase()
        val needsMood = taskLower.contains("mood")
        val needsSignUp = taskLower.contains("sign up") || taskLower.contains("create account")
        logger.info("Planning task: $task")
        
        // Extract persona from context
        val personaName = context?.substringBefore(" ")?.lowercase()?.replace("_", "") 
            ?: context?.substringAfter("persona")?.trim()?.lowercase()?.replace("_", "")
        val persona = personaName?.let { personaManager.getPersona(it) }
        if (persona != null) {
            logger.info("Using persona: ${persona.name} (technical skills: ${persona.averageTechnicalSkill}/10)")
        }
        
        var currentPlan: List<HumanAction> = emptyList()
        var iteration = 0
        val executionHistory = mutableListOf<ExecutionStep>()
        var lastErrorMessages: List<String> = emptyList()
        
        // Human-like stuck detection: "I've tried this multiple times, it's not working"
        val stuckDetector = StuckDetector(
            maxRepeatedActionAttempts = 3, // Try same action 3 times before giving up
            maxRepeatedGoalAttempts = 4    // Try same goal 4 iterations before giving up
        )
        
        while (iteration < maxIterations) {
            iteration++
            logger.info("")
            logger.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            logger.info("🔄 Planning Cycle $iteration/$maxIterations")
            logger.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            
            // Step 1: OBSERVE - Capture current state
            logger.info("👁️  Looking at the screen...")
            val captureResult = actionExecutor.execute(HumanAction.CaptureState)
            if (captureResult is ActionResult.Failure) {
                return TaskResult.Failure(
                    task = task,
                    error = "Failed to capture state: ${captureResult.error}",
                    executionHistory = executionHistory
                )
            }
            
            val screenshot = captureResult.screenshot
            if (screenshot == null) {
                return TaskResult.Failure(
                    task = task,
                    error = "Failed to capture screenshot",
                    executionHistory = executionHistory
                )
            }
            
            // Extract error messages from captured state
            val capturedErrors = (captureResult as? ActionResult.Success)
                ?.additionalData
                ?.get("errorMessages") as? List<*>
            lastErrorMessages = capturedErrors?.filterIsInstance<String>() ?: emptyList()
            
            if (lastErrorMessages.isNotEmpty()) {
                logger.warn("Error messages detected: ${lastErrorMessages.joinToString("; ")}")
                // Handle errors based on persona
                val errorResponse = personaManager.handleError(persona, lastErrorMessages.joinToString(" "))
                logger.info("Persona error handling: ${errorResponse.action} (${errorResponse.reasoning})")
            }
            
            // Step 2: ORIENT - Understand current situation and generate plan
            logger.info("🧠 Thinking about what to do next...")
            val planResult = when {
                // Use AI planning if available
                aiApiKey != null -> {
                    generatePlanWithAI(task, context, screenshot, executionHistory, lastErrorMessages)
                }
                // Use adaptive planning if components available
                genericAdaptivePlanner != null -> {
                    generateAdaptivePlan(task, screenshot, persona, executionHistory, lastErrorMessages)
                }
                // Fallback: Use simple heuristic planning
                else -> {
                    logger.warn("⚠️  Using simple planning (adaptive planning not available)")
                    generateSimplePlan(task, screenshot, persona, lastErrorMessages)
                }
            }
            
            if (planResult is PlanResult.Failure) {
                logger.error("❌ ORIENT: Failed to generate plan: ${planResult.error}")
                return TaskResult.Failure(
                    task = task,
                    error = "Failed to generate plan: ${planResult.error}",
                    executionHistory = executionHistory
                )
            }
            
            currentPlan = when (planResult) {
                is PlanResult.Success -> {
                    logger.info("📋 Plan: ${planResult.actions.size} steps")
                    planResult.actions.forEachIndexed { idx, action ->
                        val actionDesc = when (action) {
                            is HumanAction.Tap -> action.target
                            is HumanAction.TypeText -> "${action.target}: ${action.text?.take(20)}"
                            is HumanAction.Swipe -> "Swipe ${action.direction}"
                            is HumanAction.WaitFor -> "Wait: ${action.condition}"
                            is HumanAction.Verify -> "Verify: ${action.condition}"
                            is HumanAction.NavigateBack -> "Navigate back"
                            is HumanAction.CaptureState -> "Capture state"
                        }
                        logger.info("   ${idx + 1}. $actionDesc")
                    }
                    planResult.actions
                }
                is PlanResult.Failure -> emptyList()
            }
            
            // Log natural language activity for the plan (simplified)
            // Detailed plan already logged above
            
            // Step 3: Execute plan and collect feedback
            val executionResult = executePlan(currentPlan, executionHistory, task, persona, stuckDetector)
            
            // Step 4: Check if task is complete FIRST (before checking if stuck)
            // If task is complete, we're done - don't check for stuck
            if (executionResult.isComplete) {
                logger.info("✅ Task completed successfully!")
                stuckDetector.reset() // Clear stuck detection on success
                return TaskResult.Success(
                    task = task,
                    executionHistory = executionHistory,
                    finalScreenshot = screenshot
                )
            }
            
            // Step 5: Check if we're stuck (only if task is not complete)
            // Human-like decision: "I've tried this enough"
            if (stuckDetector.isStuck()) {
                val stuckReason = stuckDetector.getStuckReason()
                logger.warn("🛑 HUMAN DECISION: Stopping test - $stuckReason")
                logger.warn("💭 Reasoning: I've tried the same thing multiple times and it's not working. I should stop.")
                return TaskResult.Failure(
                    task = task,
                    error = "Test stopped due to repeated failures: $stuckReason",
                    executionHistory = executionHistory
                )
            }
            
            // Step 6: If not complete, continue with feedback loop
            logger.info("Task not complete, continuing with feedback loop...")
        }
        
        return TaskResult.Failure(
            task = task,
            error = "Reached maximum iterations ($maxIterations) without completing task",
            executionHistory = executionHistory
        )
    }
    
    /**
     * Generate plan using adaptive planning (decomposition + generic planner).
     * 
     * This is the preferred method when AI is not available:
     * 1. Decompose task into abstract goals
     * 2. Use generic adaptive planner to work out how to achieve each goal
     * 3. Generate actions based on visual observation
     */
    private suspend fun generateAdaptivePlan(
        task: String,
        screenshot: File,
        persona: Persona?,
        history: List<ExecutionStep>,
        errorMessages: List<String> = emptyList()
    ): PlanResult {
        try {
            logger.info("🧠 ADAPTIVE PLANNING: Decomposing task and working out how to achieve it...")
            
            // Step 1: Decompose task into abstract goals
            val abstractGoals = taskDecomposer.decomposeTask(task)
            if (abstractGoals.isEmpty()) {
                logger.warn("⚠️  Could not decompose task into goals, falling back to simple planning")
                return generateSimplePlan(task, screenshot, persona, errorMessages)
            }
            
            // Step 2: Use generic adaptive planner for current goal
            // For now, work on the highest priority goal
            val currentGoal = abstractGoals.first()
            logger.info("🎯 Working on abstract goal: ${currentGoal.type.name} - ${currentGoal.description}")
            
            val planResult = genericAdaptivePlanner!!.generatePlanForGoal(
                goal = currentGoal,
                screenshot = screenshot,
                history = history
            )
            
            // If adaptive planner returns empty actions, fall back to simple planning
            // This maintains agnostic principle - planner doesn't force actions, we fall back
            return when (planResult) {
                is PlanResult.Success -> {
                    if (planResult.actions.isEmpty()) {
                        logger.warn("⚠️  Adaptive planner returned empty actions, falling back to simple planning")
                        generateSimplePlan(task, screenshot, persona, errorMessages)
                    } else {
                        planResult
                    }
                }
                is PlanResult.Failure -> {
                    logger.warn("⚠️  Adaptive planning failed: ${planResult.error}, falling back to simple planning")
                    generateSimplePlan(task, screenshot, persona, errorMessages)
                }
            }
        } catch (e: Exception) {
            logger.error("Adaptive planning failed: ${e.message}", e)
            logger.warn("⚠️  Falling back to simple planning")
            return generateSimplePlan(task, screenshot, persona, errorMessages)
        }
    }
    
    /**
     * Generate plan using AI (GPT-4 Vision or similar).
     * The AI sees the screenshot and devises a plan using available human actions.
     */
    private suspend fun generatePlanWithAI(
        task: String,
        context: String?,
        screenshot: File,
        history: List<ExecutionStep>,
        errorMessages: List<String> = emptyList()
    ): PlanResult {
        try {
            val prompt = buildPrompt(task, context, history)
            val response = callAI(prompt, screenshot)
            return parseAIResponse(response)
        } catch (e: Exception) {
            logger.error("AI planning failed", e)
            return PlanResult.Failure("AI planning error: ${e.message}")
        }
    }
    
    /**
     * Simple heuristic planning (fallback when AI is not available).
     */
    private suspend fun generateSimplePlan(
        task: String,
        screenshot: File,
        persona: Persona? = null,
        errorMessages: List<String> = emptyList()
    ): PlanResult {
        // This is a simplified version - in production, you'd use AI
        logger.warn("Using simple planning (AI not configured)")
        
        // Handle errors first if present
        val actions = mutableListOf<HumanAction>()
        
        if (errorMessages.isNotEmpty()) {
            val errorText = errorMessages.joinToString(" ").lowercase()
            logger.info("Planning around errors: $errorText")
            
            // If email error, generate a better email
            if (errorText.contains("email") && (errorText.contains("invalid") || errorText.contains("format"))) {
                val betterEmail = kotlinx.coroutines.runBlocking { personaManager.generateEmail(persona) }
                logger.info("Generating persona-appropriate email: $betterEmail")
                actions.add(
                    HumanAction.TypeText(
                        target = "Email field",
                        text = betterEmail,
                        accessibilityId = "Email address input field",
                        clearFirst = true
                    )
                )
            }
            
            // If password error, generate stronger password
            if (errorText.contains("password") && (errorText.contains("weak") || errorText.contains("short"))) {
                val strongerPassword = "SecurePass123!@#"
                logger.info("Using stronger password")
                actions.add(
                    HumanAction.TypeText(
                        target = "Password field",
                        text = strongerPassword,
                        accessibilityId = "Password input field",
                        clearFirst = true
                    )
                )
            }
            
            // After fixing errors, retry the action
            if (actions.isNotEmpty()) {
                actions.add(
                    HumanAction.Tap(
                        target = "Create Account button",
                        accessibilityId = "Create account with email and password"
                    )
                )
                actions.add(
                    HumanAction.WaitFor(
                        condition = com.electricsheep.testautomation.actions.WaitCondition.LoadingComplete(),
                        timeoutSeconds = 25
                    )
                )
                return PlanResult.Success(actions)
            }
        }
        
        // Normal flow (no errors or errors handled)
        val normalActions = when {
            // Sign up AND add mood value
            (task.contains("sign up", ignoreCase = true) || task.contains("create account", ignoreCase = true)) 
                && task.contains("mood", ignoreCase = true) -> {
                val email = kotlinx.coroutines.runBlocking { personaManager.generateEmail(persona) }
                val password = personaManager.generatePassword(persona)
                logger.info("Generated email for persona ${persona?.name ?: "default"}: $email")
                
                // Generate a realistic mood score based on persona
                val moodScore = when (persona?.averageTechnicalSkill ?: 5) {
                    in 0..3 -> "7" // Tech novice: optimistic, simple number
                    in 4..7 -> "6" // Moderate: balanced
                    else -> "8" // Tech savvy: higher confidence
                }
                
                listOf(
                    // Wait for landing screen to be ready before tapping
                    HumanAction.WaitFor(
                        condition = com.electricsheep.testautomation.actions.WaitCondition.ElementVisible(
                            target = "Mood Management",
                            accessibilityId = "Mood Management utility. Track your mood throughout the day and analyse trends"
                        ),
                        timeoutSeconds = 15
                    ),
                    HumanAction.Tap(
                        target = "Mood Management",
                        accessibilityId = "Mood Management utility. Track your mood throughout the day and analyse trends"
                    ),
                    HumanAction.Tap(
                        target = "Show email and password sign in",
                        accessibilityId = "Show email and password sign in"
                    ),
                    HumanAction.TypeText(
                        target = "Email field",
                        text = email,
                        accessibilityId = "Email address input field"
                    ),
                    HumanAction.TypeText(
                        target = "Password field",
                        text = password,
                        accessibilityId = "Password input field"
                    ),
                    HumanAction.Tap(
                        target = "Create Account button",
                        accessibilityId = "Create account with email and password"
                    ),
                    HumanAction.WaitFor(
                        condition = com.electricsheep.testautomation.actions.WaitCondition.LoadingComplete(),
                        timeoutSeconds = 25
                    ),
                    HumanAction.WaitFor(
                        condition = com.electricsheep.testautomation.actions.WaitCondition.ScreenChanged("Mood Management"),
                        timeoutSeconds = 10
                    ),
                    // After sign-up, add mood value
                    HumanAction.TypeText(
                        target = "Mood score field",
                        text = moodScore,
                        accessibilityId = "Mood score input field. Enter a number between 1 and 10"
                    ),
                    HumanAction.Tap(
                        target = "Save Mood button",
                        accessibilityId = "Save mood entry"
                    ),
                    HumanAction.WaitFor(
                        condition = com.electricsheep.testautomation.actions.WaitCondition.LoadingComplete(),
                        timeoutSeconds = 15
                    ),
                    HumanAction.Verify(
                        condition = com.electricsheep.testautomation.actions.VerifyCondition.Authenticated(true)
                    ),
                    HumanAction.Verify(
                        condition = com.electricsheep.testautomation.actions.VerifyCondition.TextPresent("Mood History")
                    )
                )
            }
            // Just sign up (no mood)
            task.contains("sign up", ignoreCase = true) || task.contains("create account", ignoreCase = true) -> {
                val email = kotlinx.coroutines.runBlocking { personaManager.generateEmail(persona) }
                val password = personaManager.generatePassword(persona)
                logger.info("Generated email for persona ${persona?.name ?: "default"}: $email")
                
                listOf(
                    HumanAction.Tap(
                        target = "Mood Management",
                        accessibilityId = "Mood Management utility. Track your mood throughout the day and analyse trends"
                    ),
                    HumanAction.Tap(
                        target = "Show email and password sign in",
                        accessibilityId = "Show email and password sign in"
                    ),
                    HumanAction.TypeText(
                        target = "Email field",
                        text = email,
                        accessibilityId = "Email address input field"
                    ),
                    HumanAction.TypeText(
                        target = "Password field",
                        text = password,
                        accessibilityId = "Password input field"
                    ),
                    HumanAction.Tap(
                        target = "Create Account button",
                        accessibilityId = "Create account with email and password"
                    ),
                    HumanAction.WaitFor(
                        condition = com.electricsheep.testautomation.actions.WaitCondition.LoadingComplete(),
                        timeoutSeconds = 25
                    ),
                    HumanAction.Verify(
                        condition = com.electricsheep.testautomation.actions.VerifyCondition.Authenticated(true)
                    )
                )
            }
            // Just add mood (already signed in)
            task.contains("add mood", ignoreCase = true) || task.contains("mood value", ignoreCase = true) -> {
                val moodScore = when (persona?.averageTechnicalSkill ?: 5) {
                    in 0..3 -> "7"
                    in 4..7 -> "6"
                    else -> "8"
                }
                
                listOf(
                    HumanAction.TypeText(
                        target = "Mood score field",
                        text = moodScore,
                        accessibilityId = "Mood score input field. Enter a number between 1 and 10"
                    ),
                    HumanAction.Tap(
                        target = "Save Mood button",
                        accessibilityId = "Save mood entry"
                    ),
                    HumanAction.WaitFor(
                        condition = com.electricsheep.testautomation.actions.WaitCondition.LoadingComplete(),
                        timeoutSeconds = 15
                    ),
                    HumanAction.Verify(
                        condition = com.electricsheep.testautomation.actions.VerifyCondition.TextPresent("Mood History")
                    )
                )
            }
            else -> emptyList()
        }
        
        return PlanResult.Success(normalActions as List<HumanAction>)
    }
    
    private fun buildPrompt(
        task: String,
        context: String?,
        history: List<ExecutionStep>,
        errorMessages: List<String> = emptyList()
    ): String {
        val historySummary = if (history.isNotEmpty()) {
            "\n\nPrevious attempts:\n${history.takeLast(3).joinToString("\n") { it.summary() }}"
        } else {
            ""
        }
        
        val errorContext = if (errorMessages.isNotEmpty()) {
            "\n\n⚠️ ERROR MESSAGES DETECTED ON SCREEN:\n${errorMessages.joinToString("\n") { "- $it" }}\n\nYou MUST address these errors in your plan. Read them carefully and adjust your actions accordingly."
        } else {
            ""
        }
        
        return """
You are a test automation planner. Your job is to analyze a screenshot and create a plan to complete a task.

Task: $task
${context?.let { "Context: $it" } ?: ""}$historySummary$errorContext

Available human actions (use these to build your plan):
- Tap(target, accessibilityId, text) - Tap on an element
- TypeText(target, text, accessibilityId, clearFirst) - Type text into a field
- Swipe(direction) - Swipe in a direction (UP, DOWN, LEFT, RIGHT)
- WaitFor(condition, timeoutSeconds) - Wait for a condition
- NavigateBack() - Go back
- Verify(condition) - Verify a condition is met

Conditions:
- ElementVisible(target, accessibilityId)
- ElementEnabled(target, accessibilityId)
- ScreenChanged(expectedScreen)
- LoadingComplete(loadingIndicator)
- TextAppears(text)

Verifications:
- ElementPresent(target, accessibilityId)
- TextPresent(text)
- ScreenIs(screenName)
- Authenticated(expected)

Return a JSON array of actions. Each action should be a JSON object with:
- type: action type (Tap, TypeText, etc.)
- target: human-readable description
- accessibilityId: preferred element identifier (if known)
- text: text content (for Tap/TypeText)
- condition: condition object (for WaitFor/Verify)
- timeoutSeconds: timeout (for WaitFor)

Example response:
[
  {
    "type": "Tap",
    "target": "Mood Management",
    "accessibilityId": "Mood Management utility"
  },
  {
    "type": "TypeText",
    "target": "Email field",
    "text": "test@example.com",
    "accessibilityId": "Email address input field"
  }
]

Analyze the screenshot and return a plan to complete the task.
""".trimIndent()
    }
    
    private suspend fun callAI(prompt: String, screenshot: File): String {
        // This is a placeholder - you'd implement actual AI API call here
        // For now, return empty to use simple planning
        logger.warn("AI API not fully implemented, using simple planning")
        return ""
    }
    
    private fun parseAIResponse(response: String): PlanResult {
        // Parse AI response into HumanAction list
        // This would parse the JSON response from AI
        return PlanResult.Failure("AI parsing not implemented")
    }
    
    private suspend fun executePlan(
        plan: List<HumanAction>,
        history: MutableList<ExecutionStep>,
        task: String = "",
        persona: Persona? = null,
        stuckDetector: StuckDetector? = null
    ): ExecutionResult {
        var allSuccessful = true
        
        for (action in plan) {
            // Generate persona-appropriate activity and thought
            val (activity, thought) = generatePersonaActivityAndThought(action, persona, history.size)
            logger.info("🎬 USER ACTIVITY: $activity")
            if (thought.isNotEmpty()) {
                logger.info("💭 PERSONA THOUGHT: $thought")
            }
            val result = actionExecutor.execute(action)
            
            // Record attempt for stuck detection (human-like: "I tried this")
            stuckDetector?.recordAttempt(action, result, goalDescription = task)
            
            history.add(ExecutionStep(
                action = action,
                result = result,
                timestamp = System.currentTimeMillis()
            ))
            
            if (result is ActionResult.Failure) {
                logger.warn("Action failed: ${action::class.simpleName} - ${result.error}")
                allSuccessful = false
                
                // Capture error message from screenshot if available
                val errorScreenshot = result.screenshot
                if (errorScreenshot != null) {
                    // Re-capture state to get error messages
                    val errorCapture = actionExecutor.execute(HumanAction.CaptureState)
                    if (errorCapture is ActionResult.Success) {
                        val errors = errorCapture.additionalData?.get("errorMessages") as? List<*>
                        if (errors != null && errors.isNotEmpty()) {
                            logger.warn("Error messages on screen: ${errors.joinToString("; ")}")
                        }
                    }
                }
                // Continue with next action (feedback loop will adjust)
            }
        }
        
        // Check if task appears complete (simple heuristic)
        // For tasks involving mood, verify both authentication and mood history
        val taskLower = task.lowercase()
        val needsMoodVerification = taskLower.contains("mood")
        
        val authVerifyResult = actionExecutor.execute(
            HumanAction.Verify(
                condition = com.electricsheep.testautomation.actions.VerifyCondition.Authenticated(true)
            )
        )
        
        val moodVerifyResult = if (needsMoodVerification) {
            actionExecutor.execute(
                HumanAction.Verify(
                    condition = com.electricsheep.testautomation.actions.VerifyCondition.TextPresent("Mood History")
                )
            )
        } else {
            // Mood verification not required - create a dummy success result
            val dummyVerify = HumanAction.Verify(com.electricsheep.testautomation.actions.VerifyCondition.Authenticated(true))
            val dummyCapture = actionExecutor.execute(HumanAction.CaptureState)
            val screenshot = (dummyCapture as? ActionResult.Success)?.screenshot
            ActionResult.Success(
                action = dummyVerify,
                message = "Mood verification not required",
                screenshot = screenshot
            )
        }
        
        // Task is complete if verifications pass, even if some non-critical actions failed
        // (e.g., WaitFor timeouts are acceptable if the final state is correct)
        val authSuccess = authVerifyResult is ActionResult.Success
        val moodSuccess = moodVerifyResult is ActionResult.Success
        
        // For tasks requiring mood, both must succeed
        // For tasks not requiring mood, only auth must succeed
        val isComplete = if (needsMoodVerification) {
            authSuccess && moodSuccess
        } else {
            authSuccess
        }
        
        if (isComplete && needsMoodVerification) {
            logger.info("✅ Task complete: User authenticated and mood value added (auth=$authSuccess, mood=$moodSuccess)")
        } else if (isComplete) {
            logger.info("✅ Task complete: User authenticated (auth=$authSuccess)")
        } else {
            logger.info("⚠️  Task not complete: auth=$authSuccess, mood=$moodSuccess (needsMood=$needsMoodVerification)")
        }
        
        return ExecutionResult(
            isComplete = isComplete,
            lastResult = history.lastOrNull()?.result
        )
    }
    
    /**
     * Generate persona-appropriate activity description and thought.
     * Makes the activities feel more human and persona-specific.
     */
    private fun generatePersonaActivityAndThought(
        action: HumanAction,
        persona: Persona?,
        stepNumber: Int
    ): Pair<String, String> {
        val skillLevel = persona?.averageTechnicalSkill ?: 5
        
        return when (action) {
            is HumanAction.Tap -> {
                val activity = when {
                    action.target.contains("Mood Management", ignoreCase = true) -> 
                        "Looking for the mood tracking feature..."
                    action.target.contains("email", ignoreCase = true) || action.target.contains("password", ignoreCase = true) ->
                        "Opening the sign-up form..."
                    action.target.contains("Create Account", ignoreCase = true) || action.target.contains("Sign", ignoreCase = true) ->
                        "Creating my account..."
                    action.target.contains("Save", ignoreCase = true) ->
                        "Saving my mood entry..."
                    else -> "Tapping on: ${action.target}"
                }
                val thought = when {
                    skillLevel <= 3 -> when {
                        action.target.contains("Mood Management", ignoreCase = true) -> 
                            "I see this app can help me track my mood. Let me try it."
                        action.target.contains("email", ignoreCase = true) -> 
                            "I need to enter my email address. I hope I remember it correctly."
                        action.target.contains("Create Account", ignoreCase = true) -> 
                            "I hope this works. I'm not sure if my password is strong enough."
                        action.target.contains("Save", ignoreCase = true) -> 
                            "I want to save this so I can see how my mood changes over time."
                        else -> "I'll try tapping here and see what happens."
                    }
                    skillLevel <= 7 -> when {
                        action.target.contains("Mood Management", ignoreCase = true) -> 
                            "This looks like the main feature. Let me explore it."
                        action.target.contains("Create Account", ignoreCase = true) -> 
                            "Time to create an account. I'll use a secure password."
                        else -> "This should take me to the next step."
                    }
                    else -> when {
                        action.target.contains("Mood Management", ignoreCase = true) -> 
                            "Accessing the mood management feature."
                        action.target.contains("Create Account", ignoreCase = true) -> 
                            "Creating account with secure credentials."
                        else -> "Proceeding with the next action."
                    }
                }
                Pair(activity, thought)
            }
            is HumanAction.TypeText -> {
                val activity = when {
                    action.target.contains("Email", ignoreCase = true) -> 
                        "Entering my email address..."
                    action.target.contains("Password", ignoreCase = true) -> 
                        "Setting up my password..."
                    action.target.contains("Mood", ignoreCase = true) -> 
                        "Recording how I'm feeling..."
                    else -> "Typing into: ${action.target}"
                }
                val thought = when {
                    skillLevel <= 3 -> when {
                        action.target.contains("Email", ignoreCase = true) -> 
                            "I'm entering my email. I hope the format is correct."
                        action.target.contains("Password", ignoreCase = true) -> 
                            "I need to create a password. I'll use something I can remember."
                        action.target.contains("Mood", ignoreCase = true) -> 
                            "I'm feeling okay today. Let me put a number that makes sense."
                        else -> "I'm typing what's needed here."
                    }
                    skillLevel <= 7 -> when {
                        action.target.contains("Email", ignoreCase = true) -> 
                            "Entering my email address for account creation."
                        action.target.contains("Mood", ignoreCase = true) -> 
                            "I'll rate my current mood on a scale."
                        else -> "Filling in the required information."
                    }
                    else -> when {
                        action.target.contains("Email", ignoreCase = true) -> 
                            "Providing email for authentication."
                        action.target.contains("Mood", ignoreCase = true) -> 
                            "Logging current mood state."
                        else -> "Inputting data."
                    }
                }
                Pair(activity, thought)
            }
            is HumanAction.WaitFor -> {
                val activity = "Waiting for the app to respond..."
                val thought = when {
                    skillLevel <= 3 -> "I hope this doesn't take too long. Maybe I should wait a bit more."
                    skillLevel <= 7 -> "The app is processing. I'll wait for it to finish."
                    else -> "Waiting for operation to complete."
                }
                Pair(activity, thought)
            }
            is HumanAction.Verify -> {
                val activity = "Checking if everything worked..."
                val thought = when {
                    skillLevel <= 3 -> "I hope I did everything right. Let me see if it worked."
                    skillLevel <= 7 -> "Verifying that the task completed successfully."
                    else -> "Validating task completion."
                }
                Pair(activity, thought)
            }
            is HumanAction.Swipe -> {
                val activity = "Swiping ${action.direction}..."
                val thought = when {
                    skillLevel <= 3 -> "I'll swipe to see more options."
                    else -> "Navigating through the interface."
                }
                Pair(activity, thought)
            }
            is HumanAction.NavigateBack -> {
                val activity = "Going back..."
                val thought = when {
                    skillLevel <= 3 -> "I want to go back and try something else."
                    else -> "Returning to previous screen."
                }
                Pair(activity, thought)
            }
            is HumanAction.CaptureState -> {
                Pair("📸 Capturing screen state", "")
            }
        }
    }
    
    private data class ExecutionResult(
        val isComplete: Boolean,
        val lastResult: ActionResult?
    )
    
    
    sealed class PlanResult {
        data class Success(val actions: List<HumanAction>) : PlanResult()
        data class Failure(val error: String) : PlanResult()
    }
    
    data class ExecutionStep(
        val action: HumanAction,
        val result: ActionResult,
        val timestamp: Long
    ) {
        fun summary(): String {
            return "${action::class.simpleName}: ${if (result is ActionResult.Success) "✓" else "✗"}"
        }
    }
}

sealed class TaskResult {
    abstract val task: String
    abstract val executionHistory: List<Any>
    
    data class Success(
        override val task: String,
        override val executionHistory: List<Any>,
        val finalScreenshot: File?
    ) : TaskResult()
    
    data class Failure(
        override val task: String,
        val error: String,
        override val executionHistory: List<Any>
    ) : TaskResult()
}

