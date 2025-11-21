package com.electricsheep.testautomation.planner

import com.electricsheep.testautomation.ai.OllamaService
import kotlinx.coroutines.runBlocking

/**
 * Quick test to verify email generation works.
 * Run with: ./gradlew run --args="--main-class com.electricsheep.testautomation.planner.TestEmailGenerationKt"
 */
fun main() {
    println("🧪 Testing Email Generation")
    println("═══════════════════════════════════════════════════════════════")
    println()
    
    // Create Ollama service
    val ollamaService = OllamaService()
    
    // Create persona manager (look for personas.yaml in project root)
    val personasFile = java.io.File("../test-scenarios/personas.yaml")
    println("📁 Looking for personas file: ${personasFile.absolutePath}")
    println("   Exists: ${personasFile.exists()}")
    
    val personaManager = PersonaManager(
        personasFile = personasFile,
        ollamaService = ollamaService
    )
    
    // Get tech_novice persona
    val persona = personaManager.getPersona("tech_novice")
    
    if (persona == null) {
        println("❌ Failed to load tech_novice persona")
        return
    }
    
    println("✅ Loaded persona: ${persona.name}")
    println("   Description: ${persona.description}")
    println("   Age Group: ${persona.ageGroup ?: "not specified"}")
    println()
    
    // Test email generation
    println("📧 Generating email...")
    runBlocking {
        try {
            val email = personaManager.generateEmail(persona)
            println("✅ Generated email: $email")
            println()
            println("✅ Email generation test PASSED")
        } catch (e: Exception) {
            println("❌ Email generation test FAILED: ${e.message}")
            e.printStackTrace()
        }
    }
}

