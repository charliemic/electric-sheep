# Neurodiversity-Inclusive Design: Quick Reference

## Quick Checklist for UI Components

### ✅ Sensory Considerations
- [ ] No auto-playing media (videos, sounds, music)
- [ ] Animations respect user's reduced motion preference
- [ ] Haptic feedback can be disabled
- [ ] Clean, uncluttered layout
- [ ] Neutral/pastel colors (avoid bright, high-arousal colors)

### ✅ Predictability
- [ ] Consistent navigation patterns
- [ ] Predictable interface behavior
- [ ] Clear visual hierarchy
- [ ] Grouped related information

### ✅ Communication
- [ ] Plain language (no idioms, metaphors)
- [ ] Explicit instructions
- [ ] Clear error messages with recovery steps
- [ ] Short, clear sentences

### ✅ Executive Functioning
- [ ] Task breakdown for complex operations
- [ ] Progress indicators for multi-step processes
- [ ] History/recent actions available
- [ ] Multiple ways to accomplish tasks

### ✅ Customization
- [ ] Text size adjustable
- [ ] Color scheme options
- [ ] Optional features can be disabled
- [ ] Flexible workflows

## Language Guidelines

### ✅ DO Use
- "Enter your email address"
- "Click the Save button"
- "This field is required"
- "Your password must be at least 8 characters"

### ❌ DON'T Use
- "Just enter your email" (idiom: "just")
- "Hit the save button" (metaphor: "hit")
- "Fill in the blanks" (idiom)
- "Make sure your password is strong" (vague)

## Component Patterns

### Buttons
```kotlin
// ✅ GOOD: Clear, explicit action
AccessibleButton(
    text = "Save changes",
    onClick = { /* ... */ },
    contentDescriptionParam = "Save changes button"
)

// ❌ BAD: Vague or metaphorical
AccessibleButton(
    text = "Hit save", // Don't use "hit"
    onClick = { /* ... */ }
)
```

### Error Messages
```kotlin
// ✅ GOOD: Explicit, actionable
Text(
    text = "Email address is invalid. Please enter a valid email address like example@email.com",
    modifier = Modifier.semantics {
        error("Email address is invalid")
        liveRegion = LiveRegionMode.Polite
    }
)

// ❌ BAD: Vague or metaphorical
Text(
    text = "Something went wrong", // Too vague
    // Missing error semantics
)
```

### Multi-Step Processes
```kotlin
// ✅ GOOD: Show progress and steps
Column {
    ProgressIndicator(
        currentStep = 2,
        totalSteps = 4,
        stepLabels = listOf("Account", "Profile", "Preferences", "Complete")
    )
    TaskChecklist(
        tasks = listOf(
            TaskItem("Create account", completed = true),
            TaskItem("Set up profile", completed = true),
            TaskItem("Choose preferences", completed = false),
            TaskItem("Complete setup", completed = false)
        )
    )
}
```

## Settings to Implement

### Sensory Preferences
```kotlin
data class SensoryPreferences(
    val animationSpeed: AnimationSpeed = AnimationSpeed.Normal,
    val soundEffectsEnabled: Boolean = true,
    val hapticFeedbackEnabled: Boolean = true,
    val reduceMotion: Boolean = false
)
```

### Apply Preferences
```kotlin
// In your composable
val sensoryPrefs by settingsRepository.observeSensoryPreferences().collectAsState()

// Respect animation preferences
val animationSpec = if (sensoryPrefs.reduceMotion) {
    tween(0) // No animation
} else {
    when (sensoryPrefs.animationSpeed) {
        AnimationSpeed.Off -> tween(0)
        AnimationSpeed.Slow -> tween(1000)
        AnimationSpeed.Normal -> tween(300)
        AnimationSpeed.Fast -> tween(150)
    }
}
```

## Common Patterns

### Task Breakdown
```kotlin
// For complex operations, break into steps
Column {
    Text("Complete these steps:")
    TaskChecklist(
        tasks = listOf(
            TaskItem("Step 1: Enter your name", completed = nameEntered),
            TaskItem("Step 2: Enter your email", completed = emailEntered),
            TaskItem("Step 3: Create password", completed = passwordCreated)
        )
    )
}
```

### Progress Indicators
```kotlin
// Show progress for multi-step processes
ProgressIndicator(
    currentStep = currentStep,
    totalSteps = totalSteps,
    stepLabels = stepLabels
)
```

### Clear Instructions
```kotlin
// Provide explicit, step-by-step guidance
Column {
    Text("To complete this task:")
    Text("1. Enter your email address in the field below")
    Text("2. Click the 'Continue' button")
    Text("3. Check your email for a verification code")
}
```

## Testing Checklist

### Sensory Testing
- [ ] Test with animations disabled
- [ ] Test with sound effects disabled
- [ ] Test with haptic feedback disabled
- [ ] Test with high contrast mode
- [ ] Test with reduced motion

### Cognitive Testing
- [ ] Test with screen reader
- [ ] Test with different text sizes
- [ ] Test error recovery scenarios
- [ ] Test multi-step processes
- [ ] Test with different cognitive loads

### Language Testing
- [ ] Review all user-facing text for plain language
- [ ] Remove idioms and metaphors
- [ ] Ensure error messages are explicit
- [ ] Verify instructions are step-by-step

## Quick Wins (Start Here)

1. **Review Error Messages**: Make them explicit and actionable
2. **Add Progress Indicators**: For any multi-step process
3. **Remove Auto-Play**: Never auto-play media
4. **Plain Language Review**: Review all user-facing text
5. **Animation Control**: Add setting to disable animations

## Resources

- **Neurodiversity Guidelines:** `docs/architecture/NEURODIVERSITY_INCLUSIVE_DESIGN.md`
- **Comprehensive Accessibility Guide:** `docs/architecture/COMPREHENSIVE_ACCESSIBILITY_GUIDE.md` (covers all disability types)
- **Implementation Roadmap:** `docs/architecture/ACCESSIBILITY_IMPLEMENTATION_ROADMAP.md`
- **Accessibility Rules:** `.cursor/rules/accessibility.mdc`
- **UX Principles:** `docs/architecture/UX_PRINCIPLES.md`

