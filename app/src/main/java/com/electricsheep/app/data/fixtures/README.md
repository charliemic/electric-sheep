# Test Fixtures System

This directory contains test fixtures for generating predefined test users and mood data for testing chart visualizations and other features.

## Overview

The test fixtures system provides:
- **Predefined test users** with version-controlled IDs
- **Mood data generators** with different patterns (stable/unstable, high/low)
- **Persona combinations** (tech level + mood pattern)
- **Test data loaders** for populating the database

## Persona Combinations

The system supports 8 persona combinations by combining:

### Tech Levels
- **NOVICE**: Minimal technical experience
- **SAVVY**: Good technical experience

### Mood Patterns
- **HIGH_STABLE**: Consistently high mood (7-9 range), low variance
- **LOW_STABLE**: Consistently low mood (2-4 range), low variance
- **HIGH_UNSTABLE**: High average mood (4-10 range), high variance
- **LOW_UNSTABLE**: Low average mood (1-6 range), high variance

### Available Combinations
1. `tech_novice_high_stable`
2. `tech_novice_low_stable`
3. `tech_novice_high_unstable`
4. `tech_novice_low_unstable`
5. `tech_savvy_high_stable`
6. `tech_savvy_low_stable`
7. `tech_savvy_high_unstable`
8. `tech_savvy_low_unstable`

## Usage

### Getting a Test User

```kotlin
import com.electricsheep.app.data.fixtures.*

val user = TestUserFixtures.getTestUser(
    techLevel = TechLevel.NOVICE,
    moodPattern = MoodPattern.HIGH_STABLE
)
```

### Generating Mood Entries

```kotlin
val moods = TestUserFixtures.generateMoodEntries(
    techLevel = TechLevel.NOVICE,
    moodPattern = MoodPattern.HIGH_STABLE,
    days = 30,           // Generate 30 days of data
    entriesPerDay = 1    // 1 entry per day
)
```

### Loading Test Data into Database

```kotlin
val testDataLoader = TestDataLoader(moodDao)

val result = testDataLoader.loadTestData(
    techLevel = TechLevel.NOVICE,
    moodPattern = MoodPattern.HIGH_STABLE,
    days = 30
)

result.onSuccess { loadResult ->
    println("Loaded ${loadResult.moodCount} mood entries for ${loadResult.user.displayName}")
}
```

### Selecting Persona Combinations

```kotlin
// Get all combinations
val allCombinations = PersonaSelector.getAllCombinations()

// Get by name
val combination = PersonaSelector.getCombinationByName("tech_novice_high_stable")

// Get by tech level and mood pattern
val combination = PersonaSelector.getCombination(
    techLevel = TechLevel.NOVICE,
    moodPattern = MoodPattern.HIGH_STABLE
)
```

## Version-Controlled User IDs

All test users have fixed, version-controlled IDs:

- `test-user-tech-novice-high-stable`
- `test-user-tech-novice-low-stable`
- `test-user-tech-novice-high-unstable`
- `test-user-tech-novice-low-unstable`
- `test-user-tech-savvy-high-stable`
- `test-user-tech-savvy-low-stable`
- `test-user-tech-savvy-high-unstable`
- `test-user-tech-savvy-low-unstable`

These IDs are defined in `TestUserFixtures.UserIds` and should never change to maintain test consistency.

## Test Email Addresses

Test users have predictable email addresses:
- Format: `test-{tech-level}-{mood-pattern}@electric-sheep.test`
- Example: `test-novice-high-stable@electric-sheep.test`

## Mood Data Patterns

### High Stable
- Average: ~8.0
- Range: 7-9
- Variance: Low
- Use case: Testing consistent high values, stable trend visualization

### Low Stable
- Average: ~3.0
- Range: 2-4
- Variance: Low
- Use case: Testing consistent low values, stable trend visualization

### High Unstable
- Average: ~7.0
- Range: 4-10
- Variance: High
- Use case: Testing high variance, volatile trend visualization

### Low Unstable
- Average: ~3.5
- Range: 1-6
- Variance: High
- Use case: Testing low variance, volatile trend visualization

## Integration with Personas

The personas are defined in `test-scenarios/personas.yaml` and include:
- Technical skill definitions
- Behavioral patterns
- Mood pattern definitions
- Persona combination mappings

## Example: Loading All Persona Combinations

```kotlin
val testDataLoader = TestDataLoader(moodDao)

val result = testDataLoader.loadAllPersonaCombinations(days = 30)

result.onSuccess { results ->
    results.forEach { (key, loadResult) ->
        println("$key: ${loadResult.moodCount} entries for ${loadResult.user.displayName}")
    }
}
```

## Clearing Test Data

```kotlin
val testDataLoader = TestDataLoader(moodDao)

val result = testDataLoader.clearTestData(userId = "test-user-tech-novice-high-stable")

result.onSuccess {
    println("Test data cleared")
}
```

