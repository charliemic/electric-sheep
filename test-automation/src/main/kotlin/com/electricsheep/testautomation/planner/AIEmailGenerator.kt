package com.electricsheep.testautomation.planner

import com.electricsheep.testautomation.ai.OllamaService
import org.slf4j.LoggerFactory

/**
 * Generates realistic email addresses using AI based on persona characteristics.
 * 
 * **Why AI?**: Real people have diverse email patterns based on:
 * - Age and generation (older users might use AOL, younger use Gmail)
 * - Technical skill (tech-savvy use +tags, novices use simple patterns)
 * - Personal preferences (some use full names, others use nicknames)
 * - Life circumstances (work emails, personal emails, hobby-related)
 * 
 * **Human-Like**: Instead of procedural generation, we use AI to create
 * emails that feel authentic to the persona's background.
 * 
 * **Self-Hosted**: Uses Ollama (local LLM) via OllamaService abstraction.
 * 
 * **Strict Mode**: When `strictMode = true`, fails if Ollama unavailable instead of falling back.
 * Use for testing to ensure dependencies are available.
 */
class AIEmailGenerator(
    private val ollamaService: OllamaService,
    private val strictMode: Boolean = System.getenv("TEST_OLLAMA_STRICT") == "true" || 
                                      System.getenv("TEST_STRICT_MODE") == "true"
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    
    /**
     * Generate a realistic email address for a persona using AI.
     * 
     * @param persona The persona to generate email for
     * @return Realistic email address that matches persona characteristics
     */
    suspend fun generateEmail(persona: Persona): String {
        return try {
            val prompt = buildEmailGenerationPrompt(persona)
            val result = ollamaService.generate(prompt)
            
            result.fold(
                onSuccess = { generatedText ->
                    // Extract email from generated text (may have extra text)
                    val email = extractEmailFromText(generatedText)
                    
                    // Validate email format
                    if (isValidEmail(email)) {
                        logger.info("✅ Generated AI email for ${persona.name}: $email")
                        email
                    } else {
                        if (strictMode) {
                            throw Exception("AI generated invalid email format: $email (strict mode: no fallback)")
                        }
                        logger.warn("AI generated invalid email format: $email, falling back to procedural")
                        generateEmailProcedural(persona)
                    }
                },
                onFailure = { error ->
                    if (strictMode) {
                        logger.error("Ollama email generation failed in strict mode: ${error.message}", error)
                        throw Exception("Ollama unavailable in strict mode: ${error.message}", error)
                    }
                    logger.warn("Ollama email generation failed: ${error.message}, falling back to procedural", error)
                    generateEmailProcedural(persona)
                }
            )
        } catch (e: Exception) {
            if (strictMode && e.message?.contains("strict mode") != true) {
                // Re-throw if it's not already a strict mode error
                throw e
            }
            if (strictMode) {
                throw e  // Re-throw strict mode errors
            }
            logger.warn("Email generation error: ${e.message}, falling back to procedural", e)
            generateEmailProcedural(persona)
        }
    }
    
    /**
     * Extract email address from generated text.
     * Handles cases where Ollama may return extra text around the email.
     */
    private fun extractEmailFromText(text: String): String {
        // Try to find email pattern in text
        val emailRegex = Regex("[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}")
        val match = emailRegex.find(text)
        return match?.value ?: text.trim().lines().firstOrNull()?.trim()?.removeSurrounding("\"")?.removeSurrounding("'") ?: text.trim()
    }
    
    /**
     * Build prompt for AI email generation.
     */
    private fun buildEmailGenerationPrompt(persona: Persona): String {
        val ageGroup = persona.ageGroup ?: inferAgeGroup(persona)
        val technicalSkill = persona.averageTechnicalSkill
        val background = persona.background ?: inferBackground(persona)
        val occupation = persona.occupation ?: ""
        val interests = persona.interests?.joinToString(", ")?.takeIf { it.isNotEmpty() } ?: "general interests"
        val personality = persona.personality?.joinToString(", ")?.takeIf { it.isNotEmpty() } ?: "typical for this age and background"
        
        return """
You are an expert at generating realistic email addresses that match people's characteristics. Generate emails that real people would actually use.

Generate a realistic email address for a person with these characteristics:

Persona: ${persona.name}
Description: ${persona.description}
Age Group: $ageGroup
Technical Skill: $technicalSkill/10
Background: $background
Occupation: $occupation
Interests: $interests
Personality: $personality

Consider:
- Age and generation (older users might use AOL/Yahoo, younger use Gmail)
- Technical skill level (tech-savvy might use +tags, novices use simple patterns)
- Personal style (some use full names, others use nicknames or numbers)
- Life circumstances (work emails, personal emails, hobby-related domains)

Generate ONE realistic email address that this person would actually use.
Return ONLY the email address, nothing else. No explanation, no quotes, just the email.

Examples of realistic emails:
- Tech novice, older: "sarah.johnson@yahoo.com" or "mike1975@aol.com"
- Tech novice, younger: "emily.smith@gmail.com" or "jessica123@gmail.com"
- Tech savvy, any age: "david.martinez+test@gmail.com" or "chris.taylor@outlook.com"
""".trimIndent()
    }
    
    
    /**
     * Validate email format.
     */
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$".toRegex()
        return emailRegex.matches(email) && email.length <= 254
    }
    
    /**
     * Fallback: Procedural email generation (if AI unavailable).
     */
    private suspend fun generateEmailProcedural(persona: Persona): String {
        // Use existing procedural logic as fallback
        val personaManager = PersonaManager()
        return personaManager.generateEmail(persona)
    }
    
    /**
     * Infer age group from persona characteristics.
     */
    private fun inferAgeGroup(persona: Persona): String {
        // Simple heuristic based on technical skill and description
        return when {
            persona.averageTechnicalSkill <= 2 -> "older" // Less tech-savvy often older
            persona.description.contains("young", ignoreCase = true) -> "younger"
            persona.description.contains("elderly", ignoreCase = true) -> "older"
            else -> "middle-aged"
        }
    }
    
    /**
     * Infer background from persona characteristics.
     */
    private fun inferBackground(persona: Persona): String {
        return when {
            persona.averageTechnicalSkill <= 3 -> "non-technical, everyday user"
            persona.averageTechnicalSkill <= 7 -> "moderate technical experience"
            else -> "technical professional or enthusiast"
        }
    }
}

