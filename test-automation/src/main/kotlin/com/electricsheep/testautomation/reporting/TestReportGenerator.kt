package com.electricsheep.testautomation.reporting

import com.electricsheep.testautomation.planner.Persona
import com.electricsheep.testautomation.planner.TaskResult
import com.electricsheep.testautomation.monitoring.ScreenObservation
import com.electricsheep.testautomation.actions.HumanAction
import com.electricsheep.testautomation.actions.ActionResult
import org.slf4j.LoggerFactory
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// ExecutionStep structure - we'll reconstruct it from TaskResult.executionHistory
data class ExecutionStep(
    val action: HumanAction,
    val result: ActionResult,
    val timestamp: Long
)

/**
 * Generates comprehensive manual test-style reports.
 * 
 * Produces reports that read like a human tester's notes, including:
 * - Persona and context
 * - Goals and objectives
 * - Ease of achieving goals
 * - Strange behaviors observed
 * - User expectations vs reality
 */
class TestReportGenerator {
    private val logger = LoggerFactory.getLogger(javaClass)
    
    /**
     * Generate a comprehensive test report.
     */
    fun generateReport(
        task: String,
        persona: Persona?,
        result: TaskResult,
        executionHistory: List<ExecutionStep>,
        goals: List<com.electricsheep.testautomation.perception.GoalManager.Goal>?,
        predictions: List<com.electricsheep.testautomation.perception.PredictionManager.Prediction>?,
        observations: List<ScreenObservation>
    ): String {
        val report = StringBuilder()
        
        report.appendLine("=".repeat(80))
        report.appendLine("TEST EXECUTION REPORT")
        report.appendLine("=".repeat(80))
        report.appendLine()
        
        // Test Metadata
        report.appendSection("Test Information") {
            appendLine("Date: ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}")
            appendLine("Task: $task")
            appendLine("Result: ${if (result is TaskResult.Success) "✅ PASSED" else "❌ FAILED"}")
            appendLine("Duration: ${calculateDuration(executionHistory)} seconds")
            appendLine("Total Actions: ${executionHistory.size}")
        }
        
        // Persona Information
        if (persona != null) {
            report.appendSection("Test Persona") {
                appendLine("Name: ${persona.name}")
                appendLine("Technical Skill Level: ${persona.averageTechnicalSkill}/10")
                appendLine("Description: ${persona.description}")
                appendLine()
                if (persona.behaviors != null && persona.behaviors.isNotEmpty()) {
                    appendLine("Typical Behaviors:")
                    persona.behaviors.forEach { behavior ->
                        appendLine("  - $behavior")
                    }
                    appendLine()
                }
                if (persona.behavioralPatterns != null && persona.behavioralPatterns.isNotEmpty()) {
                    appendLine("Behavioral Patterns:")
                    persona.behavioralPatterns.forEach { pattern ->
                        appendLine("  - $pattern")
                    }
                    appendLine()
                }
                if (persona.assumptions != null && persona.assumptions.isNotEmpty()) {
                    appendLine("Assumptions:")
                    persona.assumptions.forEach { assumption ->
                        appendLine("  - $assumption")
                    }
                }
            }
        }
        
        // Goals and Objectives
        report.appendSection("Goals and Objectives") {
            if (goals != null && goals.isNotEmpty()) {
                goals.forEach { goal ->
                    val status = when (goal.currentState) {
                        com.electricsheep.testautomation.perception.GoalManager.GoalState.ACHIEVED -> "✅ ACHIEVED"
                        com.electricsheep.testautomation.perception.GoalManager.GoalState.FAILED -> "❌ FAILED"
                        com.electricsheep.testautomation.perception.GoalManager.GoalState.BLOCKED -> "🚫 BLOCKED"
                        com.electricsheep.testautomation.perception.GoalManager.GoalState.IN_PROGRESS -> "⏳ IN PROGRESS"
                        else -> "❓ UNKNOWN"
                    }
                    appendLine("$status ${goal.description}")
                    if (goal.parentGoalId != null) {
                        appendLine("  (Sub-goal of: ${goals.find { it.id == goal.parentGoalId }?.description ?: goal.parentGoalId})")
                    }
                }
            } else {
                appendLine("Primary Goal: $task")
                appendLine("Status: ${if (result is TaskResult.Success) "✅ ACHIEVED" else "❌ NOT ACHIEVED"}")
            }
        }
        
        // Goal Achievement Analysis
        report.appendSection("How Easy Was It to Complete the Task?") {
            val goalAchievement = analyzeGoalAchievement(goals, result, executionHistory)
            appendLine("Overall Difficulty: ${goalAchievement.difficulty}")
            appendLine()
            appendLine("From a user's perspective:")
            appendLine("  ${goalAchievement.easeDescription}")
            appendLine()
            if (goalAchievement.issues.isNotEmpty()) {
                appendLine("Challenges encountered along the way:")
                goalAchievement.issues.forEach { issue ->
                    appendLine("  • $issue")
                }
                appendLine()
            }
            if (goalAchievement.positiveAspects.isNotEmpty()) {
                appendLine("Things that went well:")
                goalAchievement.positiveAspects.forEach { positive ->
                    appendLine("  ✓ $positive")
                }
            }
        }
        
        // Execution Flow
        report.appendSection("What Happened During the Test") {
            appendLine("This section describes the step-by-step journey through the app, written")
            appendLine("as if a human tester was observing and documenting their experience.")
            appendLine()
            
            executionHistory.forEachIndexed { index, step ->
                val actionDesc = describeActionHumanLike(step.action)
                val resultDesc = describeResultHumanLike(step.result)
                val emoji = if (step.result is ActionResult.Success) "✅" else "❌"
                
                appendLine("$emoji Step ${index + 1}: $actionDesc")
                appendLine("     What happened: $resultDesc")
                
                if (step.result is ActionResult.Failure) {
                    appendLine("     Issue encountered: ${step.result.error}")
                }
                
                // Add human-like commentary for certain actions
                when (step.action) {
                    is HumanAction.Tap -> {
                        if (step.result is ActionResult.Success) {
                            appendLine("     Note: The button responded as expected.")
                        } else {
                            appendLine("     Note: The button didn't respond or wasn't found.")
                        }
                    }
                    is HumanAction.TypeText -> {
                        if (step.result is ActionResult.Success) {
                            appendLine("     Note: Text was entered successfully.")
                        } else {
                            appendLine("     Note: Had trouble entering text - field may not be ready.")
                        }
                    }
                    is HumanAction.WaitFor -> {
                        if (step.result is ActionResult.Success) {
                            appendLine("     Note: The app finished loading/transitioning.")
                        } else {
                            appendLine("     Note: Waited but the expected state didn't appear - app may be slow or stuck.")
                        }
                    }
                    is HumanAction.Swipe -> {
                        if (step.result is ActionResult.Success) {
                            appendLine("     Note: Swipe gesture completed successfully.")
                        } else {
                            appendLine("     Note: Swipe gesture didn't work as expected.")
                        }
                    }
                    is HumanAction.Verify -> {
                        if (step.result is ActionResult.Success) {
                            appendLine("     Note: Verification passed - expected condition was met.")
                        } else {
                            appendLine("     Note: Verification failed - expected condition was not met.")
                        }
                    }
                    is HumanAction.CaptureState -> {
                        appendLine("     Note: Screenshot captured for analysis.")
                    }
                    is HumanAction.NavigateBack -> {
                        if (step.result is ActionResult.Success) {
                            appendLine("     Note: Navigation back completed successfully.")
                        } else {
                            appendLine("     Note: Could not navigate back.")
                        }
                    }
                }
                appendLine()
            }
        }
        
        // Strange Behaviors
        report.appendSection("Did Anything Seem Odd or Unexpected?") {
            val strangeBehaviors = identifyStrangeBehaviors(executionHistory, observations, predictions)
            if (strangeBehaviors.isEmpty()) {
                appendLine("✅ No unusual behaviors were observed during testing.")
                appendLine("   The app behaved consistently and predictably throughout the test.")
            } else {
                appendLine("The following behaviors seemed unusual or unexpected:")
                appendLine()
                strangeBehaviors.forEachIndexed { index, behavior ->
                    appendLine("${index + 1}. $behavior")
                }
            }
        }
        
        // User Expectations vs Reality
        report.appendSection("Did the App Meet User Expectations?") {
            val expectations = analyzeExpectations(task, predictions, result, observations)
            appendLine("What a user would typically expect:")
            expectations.expected.forEach { expected ->
                appendLine("  • $expected")
            }
            appendLine()
            appendLine("What actually happened:")
            expectations.actual.forEach { actual ->
                val status = if (actual.matches) "✓ Met expectation" else "✗ Did not meet expectation"
                appendLine("  $status: ${actual.description}")
            }
            appendLine()
            if (expectations.mismatches.isNotEmpty()) {
                appendLine("Where expectations weren't met:")
                expectations.mismatches.forEachIndexed { index, mismatch ->
                    appendLine("  ${index + 1}. Expected: ${mismatch.expected}")
                    appendLine("     But got: ${mismatch.actual}")
                    appendLine()
                }
            } else {
                appendLine("✅ All expectations were met! The app behaved as a user would expect.")
            }
        }
        
        // Screen Observations
        if (observations.isNotEmpty()) {
            report.appendSection("Screen Observations") {
                observations.forEach { obs ->
                    val severityEmoji = when (obs.severity) {
                        ScreenObservation.Severity.CRITICAL -> "🔴"
                        ScreenObservation.Severity.HIGH -> "🟠"
                        ScreenObservation.Severity.MEDIUM -> "🟡"
                        ScreenObservation.Severity.LOW -> "🔵"
                        ScreenObservation.Severity.POSITIVE -> "🟢"
                    }
                    appendLine("$severityEmoji ${obs.type.name}: ${obs.message}")
                    if (obs.screenshot != null) {
                        appendLine("   Screenshot: ${obs.screenshot.name}")
                    }
                }
            }
        }
        
        // Overall Assessment
        report.appendSection("Overall Assessment: How Did the App Perform?") {
            val assessment = generateOverallAssessment(result, executionHistory, observations, goals)
            appendLine("User Experience Rating: ${assessment.rating}/10")
            appendLine()
            appendLine("In summary:")
            appendLine("  ${assessment.summary}")
            appendLine()
            if (assessment.recommendations.isNotEmpty()) {
                appendLine("Recommendations for improvement:")
                assessment.recommendations.forEachIndexed { index, rec ->
                    appendLine("  ${index + 1}. $rec")
                }
            } else {
                appendLine("No specific recommendations - the app performed well!")
            }
        }
        
        // Screenshots Reference
        report.appendSection("Screenshots for Review") {
            val screenshots = collectScreenshots(executionHistory, observations)
            if (screenshots.isEmpty()) {
                appendLine("No screenshots available")
            } else {
                appendLine("Total Screenshots: ${screenshots.size}")
                appendLine()
                appendLine("Key Screenshots:")
                screenshots.take(10).forEach { screenshot ->
                    appendLine("  - ${screenshot.name}")
                }
                if (screenshots.size > 10) {
                    appendLine("  ... and ${screenshots.size - 10} more")
                }
                appendLine()
                appendLine("💡 To analyze screenshots in Cursor:")
                appendLine("   ./scripts/analyze-screenshot-in-cursor.sh")
            }
        }
        
        report.appendLine()
        report.appendLine("=".repeat(80))
        report.appendLine("END OF REPORT")
        report.appendLine("=".repeat(80))
        
        return report.toString()
    }
    
    /**
     * Save report to file.
     */
    fun saveReport(report: String, outputDir: File, task: String): File {
        outputDir.mkdirs()
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        val safeTaskName = task.replace(Regex("[^a-zA-Z0-9]"), "_").take(50)
        val reportFile = File(outputDir, "test_report_${safeTaskName}_${timestamp}.txt")
        reportFile.writeText(report)
        logger.info("Test report saved: ${reportFile.absolutePath}")
        return reportFile
    }
    
    // Helper methods
    
    private fun StringBuilder.appendSection(title: String, block: StringBuilder.() -> Unit) {
        appendLine()
        appendLine("─".repeat(80))
        appendLine(title.uppercase())
        appendLine("─".repeat(80))
        block()
    }
    
    private fun calculateDuration(history: List<ExecutionStep>): Long {
        if (history.isEmpty()) return 0
        val start = history.first().timestamp
        val end = history.last().timestamp
        return (end - start) / 1000
    }
    
    private data class GoalAchievement(
        val difficulty: String,
        val easeDescription: String,
        val issues: List<String>,
        val positiveAspects: List<String>
    )
    
    private fun analyzeGoalAchievement(
        goals: List<com.electricsheep.testautomation.perception.GoalManager.Goal>?,
        result: TaskResult,
        history: List<ExecutionStep>
    ): GoalAchievement {
        val issues = mutableListOf<String>()
        val positiveAspects = mutableListOf<String>()
        
        // Analyze based on result
        if (result is TaskResult.Success) {
            positiveAspects.add("Task completed successfully")
        } else {
            issues.add("Task failed: ${(result as TaskResult.Failure).error}")
        }
        
        // Analyze execution history
        val failures = history.count { it.result is com.electricsheep.testautomation.actions.ActionResult.Failure }
        if (failures > 0) {
            issues.add("$failures action(s) failed during execution")
        }
        
        val retries = history.size - history.distinctBy { it.action::class.simpleName }.size
        if (retries > 0) {
            issues.add("Multiple attempts needed for some actions (suggests UI responsiveness issues)")
        }
        
        // Analyze wait times
        val waitActions = history.count { 
            it.action::class.simpleName?.contains("Wait", ignoreCase = true) == true 
        }
        if (waitActions > 3) {
            issues.add("Excessive waiting required (${waitActions} wait actions) - app may be slow to respond")
        }
        
        // Analyze goals
        goals?.forEach { goal ->
            when (goal.currentState) {
                com.electricsheep.testautomation.perception.GoalManager.GoalState.FAILED -> {
                    issues.add("Goal not achieved: ${goal.description}")
                }
                com.electricsheep.testautomation.perception.GoalManager.GoalState.BLOCKED -> {
                    issues.add("Goal blocked: ${goal.description}")
                }
                com.electricsheep.testautomation.perception.GoalManager.GoalState.ACHIEVED -> {
                    positiveAspects.add("Goal achieved: ${goal.description}")
                }
                else -> {}
            }
        }
        
        // Determine difficulty
        val difficulty = when {
            result is TaskResult.Success && issues.isEmpty() -> "Easy"
            result is TaskResult.Success && issues.size <= 2 -> "Moderate"
            result is TaskResult.Success -> "Challenging"
            failures > 0 -> "Difficult"
            else -> "Very Difficult"
        }
        
        val easeDescription = when (difficulty) {
            "Easy" -> "Goals were achieved smoothly with no significant issues"
            "Moderate" -> "Goals were achieved but with some minor issues or delays"
            "Challenging" -> "Goals were achieved but required multiple attempts or encountered issues"
            "Difficult" -> "Goals were partially achieved but with significant problems"
            else -> "Goals were not achieved due to critical issues"
        }
        
        return GoalAchievement(difficulty, easeDescription, issues, positiveAspects)
    }
    
    private fun identifyStrangeBehaviors(
        history: List<ExecutionStep>,
        observations: List<ScreenObservation>,
        predictions: List<com.electricsheep.testautomation.perception.PredictionManager.Prediction>?
    ): List<String> {
        val behaviors = mutableListOf<String>()
        
        // Check for prediction mismatches (only REJECTED, not PARTIAL - PARTIAL is often acceptable)
        predictions?.forEach { prediction ->
            if (prediction.verified && prediction.verificationResult == com.electricsheep.testautomation.perception.PredictionManager.VerificationResult.REJECTED) {
                // Only flag if it's truly rejected, and check if the task still succeeded
                // If task succeeded, the prediction mismatch might not be critical
                val observedInfo = if (prediction.observedState != null) {
                    "Observed: '${prediction.observedState}'"
                } else {
                    "but got something different"
                }
                behaviors.add("Action '${prediction.action}' did not produce expected result. Expected: '${prediction.expectedState}', $observedInfo")
            }
        }
        
        // Check for unexpected errors (only real errors, not informational messages)
        observations.filter { 
            (it.type == ScreenObservation.ObservationType.ERROR || it.type == ScreenObservation.ObservationType.BLOCKING_ELEMENT) &&
            !it.message.contains("Screenshot saved for analysis", ignoreCase = true) // Filter out generic analysis messages
        }.forEach { obs ->
            behaviors.add("Unexpected error/blocking element: ${obs.message}")
        }
        
        // Check for unexpected states (filter out generic "screenshot saved" messages)
        observations.filter { 
            it.type == ScreenObservation.ObservationType.UNEXPECTED_STATE &&
            !it.message.contains("Screenshot saved for analysis", ignoreCase = true) &&
            !it.message.contains("Open in Cursor to analyze", ignoreCase = true)
        }.forEach { obs ->
            behaviors.add("Unexpected screen state: ${obs.message}")
        }
        
        // Check for timeouts
        history.filter { 
            it.result is com.electricsheep.testautomation.actions.ActionResult.Failure &&
            (it.result as com.electricsheep.testautomation.actions.ActionResult.Failure).error.contains("Timeout", ignoreCase = true)
        }.forEach { step ->
            behaviors.add("Timeout waiting for: ${describeAction(step.action)}")
        }
        
        // Check for rapid failures
        val consecutiveFailures = history.windowed(2).count { 
            it[0].result is com.electricsheep.testautomation.actions.ActionResult.Failure &&
            it[1].result is com.electricsheep.testautomation.actions.ActionResult.Failure
        }
        if (consecutiveFailures > 0) {
            behaviors.add("Multiple consecutive action failures - app may be in an unstable state")
        }
        
        return behaviors
    }
    
    private data class Expectations(
        val expected: List<String>,
        val actual: List<ActualBehavior>,
        val mismatches: List<Mismatch>
    )
    
    private data class ActualBehavior(val description: String, val matches: Boolean)
    private data class Mismatch(val expected: String, val actual: String)
    
    private fun analyzeExpectations(
        task: String,
        predictions: List<com.electricsheep.testautomation.perception.PredictionManager.Prediction>?,
        result: TaskResult,
        observations: List<ScreenObservation>
    ): Expectations {
        val expected = mutableListOf<String>()
        val actual = mutableListOf<ActualBehavior>()
        val mismatches = mutableListOf<Mismatch>()
        
        // Extract expected behaviors from task
        if (task.contains("sign up", ignoreCase = true)) {
            expected.add("User can create an account")
            actual.add(ActualBehavior(
                "Account creation ${if (result is TaskResult.Success) "succeeded" else "failed"}",
                result is TaskResult.Success
            ))
        }
        
        if (task.contains("mood", ignoreCase = true)) {
            expected.add("User can add a mood value")
            val moodAdded = result is TaskResult.Success && observations.none { 
                it.type == ScreenObservation.ObservationType.ERROR && it.message.contains("mood", ignoreCase = true)
            }
            actual.add(ActualBehavior(
                "Mood value ${if (moodAdded) "was added successfully" else "could not be added"}",
                moodAdded
            ))
        }
        
        // Extract from predictions
        predictions?.forEach { prediction ->
            expected.add("After '${prediction.action}', expect: ${prediction.expectedState}")
            val verified = prediction.verified && prediction.verificationResult == com.electricsheep.testautomation.perception.PredictionManager.VerificationResult.CONFIRMED
            actual.add(ActualBehavior(
                "After '${prediction.action}', got: ${prediction.verificationResult}",
                verified
            ))
            if (!verified && prediction.verified) {
                mismatches.add(Mismatch(
                    expected = prediction.expectedState,
                    actual = "Different state observed"
                ))
            }
        }
        
        // General expectations
        expected.add("App responds promptly to user actions")
        val hasSlowResponse = observations.any { it.message.contains("timeout", ignoreCase = true) || it.message.contains("slow", ignoreCase = true) }
        actual.add(ActualBehavior(
            "App ${if (hasSlowResponse) "was slow to respond" else "responded promptly"}",
            !hasSlowResponse
        ))
        
        expected.add("No blocking errors or dialogs")
        val hasBlocking = observations.any { it.type == ScreenObservation.ObservationType.BLOCKING_ELEMENT || it.type == ScreenObservation.ObservationType.ERROR }
        actual.add(ActualBehavior(
            "App ${if (hasBlocking) "had blocking elements" else "had no blocking elements"}",
            !hasBlocking
        ))
        
        return Expectations(expected, actual, mismatches)
    }
    
    private data class OverallAssessment(
        val rating: Int,
        val summary: String,
        val recommendations: List<String>
    )
    
    private fun generateOverallAssessment(
        result: TaskResult,
        history: List<ExecutionStep>,
        observations: List<ScreenObservation>,
        goals: List<com.electricsheep.testautomation.perception.GoalManager.Goal>?
    ): OverallAssessment {
        var rating = 5 // Start at neutral
        
        // Adjust based on result
        if (result is TaskResult.Success) rating += 2 else rating -= 3
        
        // Adjust based on failures
        val failures = history.count { it.result is com.electricsheep.testautomation.actions.ActionResult.Failure }
        rating -= failures
        
        // Adjust based on observations
        val criticalIssues = observations.count { it.severity == ScreenObservation.Severity.CRITICAL }
        rating -= criticalIssues * 2
        val highIssues = observations.count { it.severity == ScreenObservation.Severity.HIGH }
        rating -= highIssues
        
        // Adjust based on goals
        goals?.forEach { goal ->
            when (goal.currentState) {
                com.electricsheep.testautomation.perception.GoalManager.GoalState.ACHIEVED -> rating += 1
                com.electricsheep.testautomation.perception.GoalManager.GoalState.FAILED -> rating -= 2
                com.electricsheep.testautomation.perception.GoalManager.GoalState.BLOCKED -> rating -= 3
                else -> {}
            }
        }
        
        // Clamp rating
        rating = rating.coerceIn(1, 10)
        
        val summary = when {
            rating >= 9 -> "Excellent user experience. The app met all expectations and performed flawlessly."
            rating >= 7 -> "Good user experience. The app met most expectations with minor issues."
            rating >= 5 -> "Acceptable user experience. The app met basic expectations but had some issues."
            rating >= 3 -> "Poor user experience. The app struggled to meet expectations with significant issues."
            else -> "Very poor user experience. The app failed to meet expectations with critical issues."
        }
        
        val recommendations = mutableListOf<String>()
        if (failures > 0) {
            recommendations.add("Investigate and fix action failures to improve reliability")
        }
        if (criticalIssues > 0) {
            recommendations.add("Address critical UI issues that block user interaction")
        }
        if (observations.any { it.type == ScreenObservation.ObservationType.BLOCKING_ELEMENT }) {
            recommendations.add("Review and fix blocking dialogs or overlays")
        }
        if (history.any { 
            it.action::class.simpleName?.contains("Wait", ignoreCase = true) == true &&
            it.result is com.electricsheep.testautomation.actions.ActionResult.Failure
        }) {
            recommendations.add("Improve app responsiveness to reduce wait times")
        }
        if (goals?.any { it.currentState == com.electricsheep.testautomation.perception.GoalManager.GoalState.FAILED } == true) {
            recommendations.add("Review failed goals and improve user flow to achieve them")
        }
        
        return OverallAssessment(rating, summary, recommendations)
    }
    
    private fun describeAction(action: HumanAction): String {
        return when (action) {
            is HumanAction.Tap -> "Tap on: ${action.target}"
            is HumanAction.TypeText -> "Type into: ${action.target}"
            is HumanAction.Swipe -> "Swipe ${action.direction}"
            is HumanAction.WaitFor -> "Wait for: ${action.condition}"
            is HumanAction.Verify -> "Verify: ${action.condition}"
            is HumanAction.CaptureState -> "Capture screen state"
            else -> action::class.simpleName ?: "Unknown action"
        }
    }
    
    private fun describeActionHumanLike(action: HumanAction): String {
        return when (action) {
            is HumanAction.Tap -> "Tapped on '${action.target}'"
            is HumanAction.TypeText -> "Typed text into '${action.target}'"
            is HumanAction.Swipe -> "Swiped ${action.direction}"
            is HumanAction.WaitFor -> "Waited for the app to ${describeWaitCondition(action.condition)}"
            is HumanAction.Verify -> "Checked if ${describeVerifyCondition(action.condition)}"
            is HumanAction.CaptureState -> "Took a screenshot to see what's on screen"
            else -> "Performed action: ${action::class.simpleName ?: "Unknown"}"
        }
    }
    
    private fun describeWaitCondition(condition: com.electricsheep.testautomation.actions.WaitCondition): String {
        return when (condition) {
            is com.electricsheep.testautomation.actions.WaitCondition.ElementVisible -> "show '${condition.target}'"
            is com.electricsheep.testautomation.actions.WaitCondition.LoadingComplete -> "finish loading"
            is com.electricsheep.testautomation.actions.WaitCondition.ScreenChanged -> "transition to '${condition.expectedScreen}' screen"
            is com.electricsheep.testautomation.actions.WaitCondition.TextAppears -> "display text '${condition.text}'"
            else -> "reach expected state"
        }
    }
    
    private fun describeVerifyCondition(condition: com.electricsheep.testautomation.actions.VerifyCondition): String {
        return when (condition) {
            is com.electricsheep.testautomation.actions.VerifyCondition.Authenticated -> "user is logged in"
            is com.electricsheep.testautomation.actions.VerifyCondition.TextPresent -> "text '${condition.text}' is visible"
            is com.electricsheep.testautomation.actions.VerifyCondition.ElementPresent -> "element '${condition.target}' is present"
            is com.electricsheep.testautomation.actions.VerifyCondition.ScreenIs -> "screen is '${condition.screenName}'"
            else -> "expected condition is met"
        }
    }
    
    private fun describeResult(result: ActionResult): String {
        return when (result) {
            is ActionResult.Success -> result.message ?: "Success"
            is ActionResult.Failure -> "Failed: ${result.error}"
        }
    }
    
    private fun describeResultHumanLike(result: ActionResult): String {
        return when (result) {
            is ActionResult.Success -> {
                result.message ?: "The action completed successfully and the app responded as expected."
            }
            is ActionResult.Failure -> {
                "The action didn't work as expected. ${result.error}"
            }
        }
    }
    
    private fun collectScreenshots(
        history: List<ExecutionStep>,
        observations: List<ScreenObservation>
    ): List<File> {
        val screenshots = mutableSetOf<File>()
        
        history.forEach { step ->
            (step.result as? com.electricsheep.testautomation.actions.ActionResult.Success)?.screenshot?.let {
                screenshots.add(it)
            }
        }
        
        observations.forEach { obs ->
            obs.screenshot?.let { screenshots.add(it) }
        }
        
        return screenshots.toList().sortedByDescending { it.lastModified() }
    }
}

