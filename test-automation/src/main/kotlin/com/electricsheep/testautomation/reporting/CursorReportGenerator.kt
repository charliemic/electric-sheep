package com.electricsheep.testautomation.reporting

import com.electricsheep.testautomation.planner.Persona
import com.electricsheep.testautomation.planner.TaskResult
import com.electricsheep.testautomation.monitoring.ScreenObservation
import org.slf4j.LoggerFactory
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Generates human-like test reports using Cursor's AI capabilities.
 * 
 * Instead of hardcoded templates, this generates a prompt file that can be
 * used with Cursor to create natural, human-readable test reports.
 */
class CursorReportGenerator {
    private val logger = LoggerFactory.getLogger(javaClass)
    
    /**
     * Generate a Cursor prompt file for AI-generated report.
     */
    fun generateCursorPrompt(
        task: String,
        persona: Persona?,
        result: TaskResult,
        executionHistory: List<ExecutionStep>,
        goals: List<com.electricsheep.testautomation.perception.GoalManager.Goal>?,
        predictions: List<com.electricsheep.testautomation.perception.PredictionManager.Prediction>?,
        observations: List<ScreenObservation>,
        outputDir: File
    ): File {
        val promptFile = File(outputDir, "cursor_report_prompt_${System.currentTimeMillis()}.md")
        
        val prompt = buildString {
            appendLine("# Test Report Generation Prompt")
            appendLine()
            appendLine("Generate a comprehensive, human-readable test report based on the following test execution data.")
            appendLine("Write it as if a human tester was documenting their experience - natural, conversational, and insightful.")
            appendLine()
            appendLine("## Test Information")
            appendLine("- **Task**: $task")
            appendLine("- **Date**: ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}")
            appendLine("- **Result**: ${if (result is TaskResult.Success) "✅ PASSED" else "❌ FAILED"}")
            appendLine("- **Duration**: ${calculateDuration(executionHistory)} seconds")
            appendLine("- **Total Actions**: ${executionHistory.size}")
            appendLine()
            
            if (persona != null) {
                appendLine("## Test Persona")
                appendLine("- **Name**: ${persona.name}")
                appendLine("- **Technical Skill Level**: ${persona.averageTechnicalSkill}/10")
                appendLine("- **Description**: ${persona.description}")
                if (persona.behaviors != null && persona.behaviors.isNotEmpty()) {
                    appendLine("- **Behaviors**: ${persona.behaviors.joinToString(", ")}")
                }
                appendLine()
            }
            
            if (goals != null && goals.isNotEmpty()) {
                appendLine("## Goals and Objectives")
                goals.forEach { goal ->
                    val status = when (goal.currentState) {
                        com.electricsheep.testautomation.perception.GoalManager.GoalState.ACHIEVED -> "✅ ACHIEVED"
                        com.electricsheep.testautomation.perception.GoalManager.GoalState.FAILED -> "❌ FAILED"
                        com.electricsheep.testautomation.perception.GoalManager.GoalState.BLOCKED -> "🚫 BLOCKED"
                        com.electricsheep.testautomation.perception.GoalManager.GoalState.IN_PROGRESS -> "⏳ IN PROGRESS"
                        else -> "❓ UNKNOWN"
                    }
                    appendLine("- $status ${goal.description}")
                }
                appendLine()
            }
            
            appendLine("## Execution Flow")
            executionHistory.forEachIndexed { index, step ->
                val actionDesc = describeAction(step.action)
                val resultDesc = describeResult(step.result)
                val success = step.result is com.electricsheep.testautomation.actions.ActionResult.Success
                appendLine("${index + 1}. **$actionDesc**")
                appendLine("   - Result: ${if (success) "✅ Success" else "❌ Failed"}")
                appendLine("   - Details: $resultDesc")
                if (!success) {
                    appendLine("   - Error: ${(step.result as com.electricsheep.testautomation.actions.ActionResult.Failure).error}")
                }
            }
            appendLine()
            
            if (predictions != null && predictions.isNotEmpty()) {
                appendLine("## Predictions and Verifications")
                appendLine("(Note: PARTIAL matches are acceptable when the task succeeded - they indicate the app")
                appendLine("worked correctly even if predictions weren't perfectly precise)")
                appendLine()
                predictions.forEach { pred ->
                    val result = when (pred.verificationResult) {
                        com.electricsheep.testautomation.perception.PredictionManager.VerificationResult.CONFIRMED -> "✅ Confirmed"
                        com.electricsheep.testautomation.perception.PredictionManager.VerificationResult.PARTIAL -> "⚠️ Partial (acceptable)"
                        com.electricsheep.testautomation.perception.PredictionManager.VerificationResult.REJECTED -> "❌ Rejected"
                        else -> "⏳ Pending"
                    }
                    appendLine("- **Action**: ${pred.action}")
                    appendLine("  - Expected: ${pred.expectedState}")
                    if (pred.observedState != null && pred.observedState != "unknown") {
                        appendLine("  - Observed: ${pred.observedState}")
                    }
                    appendLine("  - Verification: $result")
                }
                appendLine()
            }
            
            if (observations.isNotEmpty()) {
                appendLine("## Screen Observations")
                observations.forEach { obs ->
                    val severity = when (obs.severity) {
                        ScreenObservation.Severity.CRITICAL -> "🔴 CRITICAL"
                        ScreenObservation.Severity.HIGH -> "🟠 HIGH"
                        ScreenObservation.Severity.MEDIUM -> "🟡 MEDIUM"
                        ScreenObservation.Severity.LOW -> "🔵 LOW"
                        ScreenObservation.Severity.POSITIVE -> "🟢 POSITIVE"
                    }
                    appendLine("- **${obs.type.name}** [$severity]: ${obs.message}")
                }
                appendLine()
            }
            
            appendLine("## Instructions")
            appendLine()
            appendLine("Generate a **task-oriented** test report. Focus on whether the user could complete their goal, not technical details.")
            appendLine()
            appendLine("**Report Structure (Task-Oriented):**")
            appendLine()
            appendLine("1. **Could the User Complete Their Task?**")
            appendLine("   - Start with a clear yes/no answer")
            appendLine("   - What was the user trying to do? (${task})")
            appendLine("   - Did they succeed? Why or why not?")
            appendLine("   - How long did it take?")
            appendLine()
            appendLine("2. **What Was the User Experience Like?**")
            appendLine("   - Write from the persona's perspective: ${persona?.name ?: "a typical user"}")
            appendLine("   - Was it easy or difficult? What made it so?")
            appendLine("   - Were there any confusing moments or dead ends?")
            appendLine("   - What worked well?")
            appendLine()
            appendLine("3. **What Actually Happened?**")
            appendLine("   - Tell the story of what the user did step-by-step")
            appendLine("   - Focus on user actions and app responses, not technical details")
            appendLine("   - Use natural language: 'The user tapped...', 'The app showed...', 'Then they...'")
            appendLine()
            appendLine("4. **Did Anything Get in the Way?**")
            appendLine("   - Only mention things that actually blocked or confused the user")
            appendLine("   - Ignore technical false positives (PARTIAL predictions when task succeeded)")
            appendLine("   - Ignore informational messages about screenshots")
            appendLine("   - Focus on real user-facing issues")
            appendLine()
            appendLine("5. **Overall Assessment**")
            appendLine("   - Rate the experience 1-10 from the user's perspective")
            appendLine("   - Would this persona recommend the app?")
            appendLine("   - What's the one thing that would most improve the experience?")
            appendLine()
            appendLine("**Writing Style:**")
            appendLine("- Write as if you're a UX researcher explaining to a product manager")
            appendLine("- Use 'the user' or 'they' (not 'the test' or 'the system')")
            appendLine("- Focus on user goals, not technical implementation")
            appendLine("- Be specific: 'The user had to wait 3 seconds' not 'There was a delay'")
            appendLine("- If the task succeeded, celebrate that! Don't focus on minor technical mismatches")
            appendLine()
            appendLine("**Key Principle:** The user's goal was: **${task}**")
            appendLine("Everything in this report should relate back to whether and how well they achieved that goal.")
            appendLine()
            appendLine("Generate the report now:")
        }
        
        promptFile.writeText(prompt)
        logger.info("Generated Cursor prompt file: ${promptFile.absolutePath}")
        return promptFile
    }
    
    private fun calculateDuration(history: List<ExecutionStep>): Long {
        if (history.isEmpty()) return 0
        val start = history.first().timestamp
        val end = history.last().timestamp
        return (end - start) / 1000
    }
    
    private fun describeAction(action: com.electricsheep.testautomation.actions.HumanAction): String {
        return when (action) {
            is com.electricsheep.testautomation.actions.HumanAction.Tap -> "Tapped on '${action.target}'"
            is com.electricsheep.testautomation.actions.HumanAction.TypeText -> "Typed text into '${action.target}'"
            is com.electricsheep.testautomation.actions.HumanAction.Swipe -> "Swiped ${action.direction}"
            is com.electricsheep.testautomation.actions.HumanAction.WaitFor -> "Waited for: ${action.condition}"
            is com.electricsheep.testautomation.actions.HumanAction.Verify -> "Verified: ${action.condition}"
            is com.electricsheep.testautomation.actions.HumanAction.CaptureState -> "Captured screen state"
            is com.electricsheep.testautomation.actions.HumanAction.NavigateBack -> "Navigated back"
        }
    }
    
    private fun describeResult(result: com.electricsheep.testautomation.actions.ActionResult): String {
        return when (result) {
            is com.electricsheep.testautomation.actions.ActionResult.Success -> result.message ?: "Success"
            is com.electricsheep.testautomation.actions.ActionResult.Failure -> "Failed: ${result.error}"
        }
    }
}

