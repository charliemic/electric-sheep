package com.electricsheep.testautomation.logging

import org.slf4j.LoggerFactory
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Generates human-readable narrative logs showing the cognitive process.
 * 
 * This logger creates a story-like narrative of the user's journey through the app,
 * showing their thoughts, observations, and decisions as they navigate.
 */
class CognitiveNarrativeLogger(
    val outputFile: File
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val narrative = StringBuilder()
    private var currentSection: String? = null
    private var lastTimestamp: Long = System.currentTimeMillis()
    
    init {
        outputFile.parentFile?.mkdirs()
        startNarrative()
    }
    
    /**
     * Start the narrative with introduction.
     */
    private fun startNarrative() {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        narrative.appendLine("=".repeat(80))
        narrative.appendLine("USER JOURNEY NARRATIVE")
        narrative.appendLine("=".repeat(80))
        narrative.appendLine("Started: $timestamp")
        narrative.appendLine()
    }
    
    /**
     * Log a cognitive observation (what the user sees).
     */
    fun observe(observation: String) {
        val relativeTime = getRelativeTime()
        narrative.appendLine("[$relativeTime] 👁️  OBSERVATION: $observation")
        narrative.appendLine()
    }
    
    /**
     * Log an intention (what the user wants to do).
     */
    fun intend(intention: String) {
        val relativeTime = getRelativeTime()
        narrative.appendLine("[$relativeTime] 🧠 INTENTION: $intention")
        narrative.appendLine()
    }
    
    /**
     * Log an action (what the user does).
     */
    fun act(action: String) {
        val relativeTime = getRelativeTime()
        narrative.appendLine("[$relativeTime] ✋ ACTION: $action")
        narrative.appendLine()
    }
    
    /**
     * Log feedback (what happened after the action).
     */
    fun feedback(feedback: String, success: Boolean = true) {
        val relativeTime = getRelativeTime()
        val emoji = if (success) "✅" else "❌"
        narrative.appendLine("[$relativeTime] $emoji FEEDBACK: $feedback")
        narrative.appendLine()
    }
    
    /**
     * Log a persona thought (what the user is thinking).
     */
    fun think(thought: String) {
        val relativeTime = getRelativeTime()
        narrative.appendLine("[$relativeTime] 💭 THOUGHT: $thought")
        narrative.appendLine()
    }
    
    /**
     * Log a decision (why the user chose a particular action).
     */
    fun decide(reasoning: String) {
        val relativeTime = getRelativeTime()
        narrative.appendLine("[$relativeTime] 🤔 DECISION: $reasoning")
        narrative.appendLine()
    }
    
    /**
     * Start a new section (e.g., "Signing Up", "Adding Mood").
     */
    fun startSection(title: String) {
        if (currentSection != null) {
            endSection()
        }
        currentSection = title
        narrative.appendLine()
        narrative.appendLine("─".repeat(80))
        narrative.appendLine("SECTION: $title")
        narrative.appendLine("─".repeat(80))
        narrative.appendLine()
    }
    
    /**
     * End current section.
     */
    fun endSection() {
        currentSection?.let {
            narrative.appendLine()
            narrative.appendLine("Completed: $it")
            narrative.appendLine()
        }
        currentSection = null
    }
    
    /**
     * Log an error or problem encountered.
     */
    fun problem(description: String, solution: String? = null) {
        val relativeTime = getRelativeTime()
        narrative.appendLine("[$relativeTime] ⚠️  PROBLEM: $description")
        if (solution != null) {
            narrative.appendLine("[$relativeTime] 💡 SOLUTION: $solution")
        }
        narrative.appendLine()
    }
    
    /**
     * Log a stuck situation (repeated failures).
     */
    fun stuck(reason: String) {
        val relativeTime = getRelativeTime()
        narrative.appendLine("[$relativeTime] 🔄 STUCK: $reason")
        narrative.appendLine("[$relativeTime] 💭 THOUGHT: I've tried this multiple times. It's not working. I should stop.")
        narrative.appendLine()
    }
    
    /**
     * Finalize and save the narrative.
     */
    fun finalize(task: String, success: Boolean) {
        endSection()
        
        narrative.appendLine()
        narrative.appendLine("=".repeat(80))
        narrative.appendLine("JOURNEY COMPLETE")
        narrative.appendLine("=".repeat(80))
        narrative.appendLine("Task: $task")
        narrative.appendLine("Result: ${if (success) "✅ SUCCESS" else "❌ FAILED"}")
        narrative.appendLine("Ended: ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))}")
        narrative.appendLine()
        
        outputFile.writeText(narrative.toString())
        logger.info("Narrative log saved: ${outputFile.absolutePath}")
    }
    
    private fun getRelativeTime(): String {
        val now = System.currentTimeMillis()
        val elapsed = now - lastTimestamp
        lastTimestamp = now
        val seconds = elapsed / 1000.0
        return String.format("%.1fs", seconds)
    }
}

