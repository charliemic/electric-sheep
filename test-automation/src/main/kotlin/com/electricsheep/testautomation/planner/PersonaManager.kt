package com.electricsheep.testautomation.planner

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.random.Random

/**
 * Manages persona definitions and generates persona-appropriate data.
 * 
 * **Enhanced with AI**: Uses AI to generate realistic emails that match
 * persona characteristics (age, background, technical skill, etc.)
 */
class PersonaManager(
    personasFile: File = File("test-scenarios/personas.yaml"),
    ollamaService: com.electricsheep.testautomation.ai.OllamaService? = null,
    strictMode: Boolean = System.getenv("TEST_OLLAMA_STRICT") == "true" || 
                         System.getenv("TEST_STRICT_MODE") == "true"
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val objectMapper = ObjectMapper(YAMLFactory()).apply {
        registerModule(KotlinModule.Builder().build())
        // Handle snake_case in YAML -> camelCase in Kotlin
        propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
    }
    private val aiEmailGenerator = ollamaService?.let { 
        try {
            AIEmailGenerator(ollamaService = it, strictMode = strictMode)
        } catch (e: Exception) {
            if (strictMode) {
                logger.error("Failed to create AI email generator in strict mode: ${e.message}", e)
                throw Exception("AI email generator unavailable in strict mode: ${e.message}", e)
            }
            logger.warn("Failed to create AI email generator: ${e.message}")
            null
        }
    }
    
    private val personas: Map<String, Persona> = try {
        if (personasFile.exists()) {
            val yamlContent = personasFile.readText()
            // Parse YAML - handle both direct map and nested "personas" key
            val parsed: Map<String, Any> = objectMapper.readValue(yamlContent)
            val personasRaw = if (parsed.containsKey("personas")) {
                // YAML has "personas:" wrapper
                parsed["personas"] as? Map<String, Map<String, Any>> ?: emptyMap()
            } else {
                // Direct map of personas
                parsed as? Map<String, Map<String, Any>> ?: emptyMap()
            }
            
            // Convert raw maps to Persona objects
            val personasMap = personasRaw.mapValues { (_, personaData) ->
                objectMapper.convertValue(personaData, Persona::class.java)
            }
            
            logger.info("Loaded ${personasMap.size} personas from ${personasFile.path}")
            personasMap
        } else {
            logger.warn("Personas file not found: ${personasFile.path}, using defaults")
            emptyMap()
        }
    } catch (e: Exception) {
        logger.error("Failed to load personas", e)
        emptyMap()
    }
    
    /**
     * Get persona by name.
     */
    fun getPersona(name: String): Persona? {
        return personas[name]
    }
    
    /**
     * Generate a realistic email address that humans typically use.
     * 
     * **AI-Enhanced**: Uses AI to generate emails that match persona characteristics
     * (age, background, technical skill, etc.) for more realistic, lifelike personas.
     * Falls back to procedural generation if AI unavailable.
     */
    suspend fun generateEmail(persona: Persona?): String {
        if (persona == null) {
            return generateEmailProcedural(null)
        }
        
        // Use AI if available (more realistic, lifelike emails)
        if (aiEmailGenerator != null) {
            return try {
                aiEmailGenerator.generateEmail(persona)
            } catch (e: Exception) {
                logger.warn("AI email generation failed: ${e.message}, falling back to procedural", e)
                generateEmailProcedural(persona)
            }
        }
        
        // Fallback to procedural generation
        return generateEmailProcedural(persona)
    }
    
    /**
     * Procedural email generation (fallback when AI unavailable).
     */
    private fun generateEmailProcedural(persona: Persona?): String {
        val timestamp = System.currentTimeMillis().toString().takeLast(6)
        val randomSuffix = (1000..9999).random()
        
        // Common first names
        val firstNames = listOf(
            "sarah", "mike", "jennifer", "david", "emily", "chris", "lisa", "james",
            "jessica", "michael", "ashley", "matthew", "amanda", "joshua", "melissa",
            "daniel", "michelle", "andrew", "stephanie", "ryan", "nicole", "justin"
        )
        
        // Common last names
        val lastNames = listOf(
            "johnson", "smith", "davis", "brown", "wilson", "miller", "moore", "taylor",
            "anderson", "thomas", "jackson", "white", "harris", "martin", "thompson",
            "garcia", "martinez", "robinson", "clark", "rodriguez", "lewis", "lee"
        )
        
        // Common email domains
        val commonDomains = listOf("gmail.com", "yahoo.com", "outlook.com", "hotmail.com")
        
        return when {
            persona == null -> {
                // Default: simple realistic pattern
                "${firstNames.random()}${randomSuffix}@${commonDomains.random()}"
            }
            persona.averageTechnicalSkill <= 3 -> {
                // Tech novice: Simple patterns
                when ((0..2).random()) {
                    0 -> "${firstNames.random()}.${lastNames.random()}@gmail.com"
                    1 -> "${firstNames.random()}${randomSuffix}@yahoo.com"
                    else -> "${firstNames.random()}${lastNames.random().take(4)}${randomSuffix}@gmail.com"
                }
            }
            persona.averageTechnicalSkill <= 7 -> {
                // Moderate: Mix of patterns
                when ((0..3).random()) {
                    0 -> "${firstNames.random()}.${lastNames.random()}@${commonDomains.random()}"
                    1 -> "${firstNames.random()}${randomSuffix}@${commonDomains.random()}"
                    2 -> "${firstNames.random().first()}${lastNames.random()}@${commonDomains.random()}"
                    else -> "${firstNames.random()}.${lastNames.random()}${randomSuffix}@gmail.com"
                }
            }
            else -> {
                // Tech savvy: May use advanced patterns
                when ((0..4).random()) {
                    0 -> "${firstNames.random()}.${lastNames.random()}+test${randomSuffix}@gmail.com"
                    1 -> "${firstNames.random()}.${lastNames.random()}@${commonDomains.random()}"
                    2 -> "${firstNames.random()}${randomSuffix}@${commonDomains.random()}"
                    3 -> "${firstNames.random().first()}${lastNames.random()}@${commonDomains.random()}"
                    else -> "${firstNames.random()}.${lastNames.random()}${randomSuffix}@gmail.com"
                }
            }
        }
    }
    
    /**
     * Generate a password based on persona's expected complexity.
     */
    fun generatePassword(persona: Persona?): String {
        // For now, use a simple password generation based on technical skill level
        // TODO: Add password_complexity field to Persona data class if needed
        val skillLevel = persona?.averageTechnicalSkill ?: 5
        return when {
            skillLevel <= 3 -> "password123" // Tech novice: simple password
            skillLevel <= 7 -> "TestPass${(1000..9999).random()}!" // Moderate: medium complexity
            else -> "SecureP@ss${(1000..9999).random()}!#" // Tech savvy: high complexity
        }
    }
    
    /**
     * Handle error based on persona characteristics.
     */
    fun handleError(persona: Persona?, errorMessage: String): ErrorResponse {
        if (persona == null) {
            return ErrorResponse(
                action = "Retry with corrected input",
                reasoning = "Generic error handling"
            )
        }
        
        val errorLower = errorMessage.lowercase()
        
        // Email-related errors
        if (errorLower.contains("email") && (errorLower.contains("invalid") || errorLower.contains("format"))) {
        val skillLevel = persona.averageTechnicalSkill
        return when (skillLevel) {
            in 0..3 -> ErrorResponse(
                    action = "Generate a simpler email address and try again",
                    reasoning = "Tech novice needs simple, clear email format"
                )
            in 4..7 -> ErrorResponse(
                    action = "Check email format and generate a valid one",
                    reasoning = "Moderate tech skills - can understand format requirements"
                )
                else -> ErrorResponse(
                    action = "Fix email format immediately",
                    reasoning = "Tech savvy - understands validation quickly"
                )
            }
        }
        
        // Password-related errors
        if (errorLower.contains("password") && (errorLower.contains("weak") || errorLower.contains("short"))) {
            return ErrorResponse(
                action = "Generate a stronger password (longer, with special characters)",
                reasoning = persona.errorHandling?.firstOrNull() ?: "Fix password requirements"
            )
        }
        
        // Generic error handling based on persona
        val skillLevel = persona.averageTechnicalSkill
        return when (skillLevel) {
            in 0..3 -> ErrorResponse(
                action = "Read error message carefully, wait a moment, then try a simpler approach",
                reasoning = "Tech novice needs time to understand errors"
            )
            in 4..7 -> ErrorResponse(
                action = "Analyze error and adjust approach",
                reasoning = "Moderate skills - can interpret errors"
            )
            else -> ErrorResponse(
                action = "Quickly fix the issue and continue",
                reasoning = "Tech savvy - fast error recovery"
            )
        }
    }
}

data class Persona(
    val name: String,
    val description: String,
    val technicalSkills: TechnicalSkills? = null,
    val assumptions: List<String>? = null,
    val behaviors: List<String>? = null,
    val behavioralPatterns: List<String>? = null,
    val limitations: List<String>? = null,
    val knowledge: List<String>? = null,
    val agentInstructions: String? = null,
    val emailPatterns: List<String>? = null,
    val emailExamples: List<String>? = null,
    val errorHandling: List<String>? = null,
    // Enhanced lifelike attributes
    val ageGroup: String? = null, // "younger", "middle-aged", "older"
    val background: String? = null, // "non-technical", "technical professional", "student", etc.
    val interests: List<String>? = null, // Personal interests that might influence behavior
    val occupation: String? = null, // Job/role that influences technical exposure
    val personality: List<String>? = null // Personality traits (patient, impatient, cautious, etc.)
) {
    // Calculate average technical skill for quick comparison
    val averageTechnicalSkill: Int
        get() = technicalSkills?.let {
            val skills = listOfNotNull(
                it.mobileApps,
                it.authentication,
                it.forms,
                it.navigation,
                it.errorHandling
            )
            if (skills.isNotEmpty()) {
                skills.average().toInt()
            } else {
                5 // Default
            }
        } ?: 5
}

data class TechnicalSkills(
    val mobileApps: Int? = null,
    val authentication: Int? = null,
    val forms: Int? = null,
    val navigation: Int? = null,
    val errorHandling: Int? = null
)

data class ErrorResponse(
    val action: String,
    val reasoning: String
)

