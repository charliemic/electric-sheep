package com.electricsheep.app.data.fixtures

/**
 * Helper for selecting persona combinations at runtime.
 * Allows combining tech level with mood patterns for testing.
 */
object PersonaSelector {
    
    /**
     * Get all available persona combinations
     */
    fun getAllCombinations(): List<PersonaCombination> {
        val combinations = mutableListOf<PersonaCombination>()
        
        for (techLevel in TechLevel.values()) {
            for (moodPattern in MoodPattern.values()) {
                combinations.add(
                    PersonaCombination(
                        techLevel = techLevel,
                        moodPattern = moodPattern,
                        userId = TestUserFixtures.getUserId(techLevel, moodPattern),
                        email = TestUserFixtures.getEmail(techLevel, moodPattern),
                        displayName = TestUserFixtures.getDisplayName(techLevel, moodPattern)
                    )
                )
            }
        }
        
        return combinations
    }
    
    /**
     * Get persona combination by name (e.g., "tech_novice_high_stable")
     */
    fun getCombinationByName(name: String): PersonaCombination? {
        val parts = name.lowercase().split("_")
        if (parts.size < 3) return null
        
        val techLevel = when {
            parts.contains("novice") -> TechLevel.NOVICE
            parts.contains("savvy") -> TechLevel.SAVVY
            else -> return null
        }
        
        val moodPattern = when {
            parts.contains("high") && parts.contains("stable") -> MoodPattern.HIGH_STABLE
            parts.contains("low") && parts.contains("stable") -> MoodPattern.LOW_STABLE
            parts.contains("high") && parts.contains("unstable") -> MoodPattern.HIGH_UNSTABLE
            parts.contains("low") && parts.contains("unstable") -> MoodPattern.LOW_UNSTABLE
            else -> return null
        }
        
        return PersonaCombination(
            techLevel = techLevel,
            moodPattern = moodPattern,
            userId = TestUserFixtures.getUserId(techLevel, moodPattern),
            email = TestUserFixtures.getEmail(techLevel, moodPattern),
            displayName = TestUserFixtures.getDisplayName(techLevel, moodPattern)
        )
    }
    
    /**
     * Get persona combination by tech level and mood pattern
     */
    fun getCombination(
        techLevel: TechLevel,
        moodPattern: MoodPattern
    ): PersonaCombination {
        return PersonaCombination(
            techLevel = techLevel,
            moodPattern = moodPattern,
            userId = TestUserFixtures.getUserId(techLevel, moodPattern),
            email = TestUserFixtures.getEmail(techLevel, moodPattern),
            displayName = TestUserFixtures.getDisplayName(techLevel, moodPattern)
        )
    }
}

/**
 * Represents a persona combination (tech level + mood pattern)
 */
data class PersonaCombination(
    val techLevel: TechLevel,
    val moodPattern: MoodPattern,
    val userId: String,
    val email: String,
    val displayName: String
) {
    /**
     * Get a human-readable name for this combination
     */
    fun getName(): String {
        val tech = techLevel.name.lowercase().replaceFirstChar { it.uppercase() }
        val mood = moodPattern.name.lowercase().split("_").joinToString(" ") { 
            it.replaceFirstChar { char -> char.uppercase() }
        }
        return "$tech - $mood"
    }
    
    /**
     * Get a short identifier (e.g., "tech_novice_high_stable")
     */
    fun getIdentifier(): String {
        return "${techLevel.name.lowercase()}_${moodPattern.name.lowercase()}"
    }
}

