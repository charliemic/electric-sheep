# Human-Focused Pattern Principles and Rules

## Core Principle

**All automation patterns must mimic human behavior:**
- Humans observe and react to what they see
- Humans wait for events, not arbitrary time periods
- Humans adapt based on feedback, not fixed sequences
- Humans use visual perception first, system details last

## Pattern Rules

### 1. Event-Driven Over Fixed Delays

**❌ BAD: Fixed Delays**
```kotlin
// Don't do this - arbitrary wait time
Thread.sleep(2000)
delay(1000)
```

**✅ GOOD: Event-Driven Waits**
```kotlin
// Wait for specific event/state change
waitFor { stateManager.getCurrentState()?.screenName == "Mood Management" }
waitFor { !stateManager.getCurrentState()?.isLoading }
```

**Rule**: Only use fixed delays as a **LAST RESORT** fallback when:
- Event detection has failed after multiple attempts
- System is in an unknown state and we need to stabilize
- Explicitly marked as "LAST RESORT" with justification

### 2. Visual Perception First

**❌ BAD: System-Level Detection**
```kotlin
// Don't check system properties first
if (driver.isKeyboardShown()) { ... }
```

**✅ GOOD: Visual Detection**
```kotlin
// Check what we see visually
val state = stateManager.getCurrentState()
if (state?.hasKeyboard == true) { ... }
```

**Rule**: Always check visual state (`StateManager`, `ScreenState`) before system-level APIs.

### 3. Continuous Observation Loop

**❌ BAD: Single Check**
```kotlin
// Don't check once and assume
val state = getState()
if (state.isLoading) { wait() }
```

**✅ GOOD: Continuous Monitoring**
```kotlin
// Monitor continuously until condition met
startContinuousMonitoring()
while (!conditionMet()) {
    observeState()
    delay(100) // Small polling interval
}
```

**Rule**: Use continuous monitoring loops (`ContinuousInteractionLoop`, `ScreenMonitor`) for:
- Action feedback
- State changes
- Error detection
- Goal achievement

### 4. Feedback-Driven Adaptation

**❌ BAD: Fixed Strategy**
```kotlin
// Don't try same thing repeatedly
for (i in 1..3) {
    try { action() } catch { retry() }
}
```

**✅ GOOD: Adaptive Strategy**
```kotlin
// Adapt based on feedback
val result = tryAction()
if (result is Failure) {
    observeState() // What went wrong?
    adaptStrategy(result) // Try different approach
}
```

**Rule**: Always observe feedback before retrying. Adapt strategy based on what you see.

### 5. Human-Like Decision Making

**❌ BAD: Binary Logic**
```kotlin
// Don't use simple if/else
if (error) { retry() } else { continue() }
```

**✅ GOOD: Contextual Decisions**
```kotlin
// Consider context and history
if (isStuck(recentActions)) {
    logger.info("💭 I've tried this ${attempts} times, it's not working")
    tryAlternativeApproach()
} else if (isBlocked(currentState)) {
    logger.info("💡 Something is blocking me, let me work around it")
    clearBlockage()
} else {
    continue()
}
```

**Rule**: Make decisions based on:
- Current state (what we see)
- Recent history (what we tried)
- Context (where we are in the task)
- Goals (what we're trying to achieve)

### 6. Visual-First Error Recovery

**❌ BAD: System-Level Recovery**
```kotlin
// Don't use system APIs first
try {
    action()
} catch (e: Exception) {
    driver.hideKeyboard() // System-level fallback
}
```

**✅ GOOD: Visual-First Recovery**
```kotlin
// Try visual recovery first
if (state.hasKeyboard) {
    dismissKeyboardVisually() // Visual detection and action
} else {
    // LAST RESORT: System-level fallback
    driver.hideKeyboard()
}
```

**Rule**: Recovery strategies must be ordered:
1. **VISUAL**: Detect problem visually, solve visually
2. **ADAPTIVE**: Try alternative visual approaches
3. **LAST RESORT**: System-level fallback (explicitly marked)

### 7. Goal-Oriented Attention

**❌ BAD: Check Everything**
```kotlin
// Don't check all elements
val allElements = findAllElements()
for (element in allElements) { check(element) }
```

**✅ GOOD: Focus on Goals**
```kotlin
// Focus on what's relevant to current goal
val currentGoal = goalManager.getCurrentGoal()
val relevantElements = findElementsRelevantToGoal(currentGoal)
observe(relevantElements)
```

**Rule**: Only observe what's relevant to:
- Current goal
- Next action
- Expected outcome
- Error conditions

### 8. State-Based Condition Checks

**❌ BAD: Polling System Properties**
```kotlin
// Don't poll system properties
while (!driver.findElement(By.id("button")).isDisplayed) {
    Thread.sleep(100)
}
```

**✅ GOOD: State-Based Checks**
```kotlin
// Check state manager for changes
waitFor { 
    val state = stateManager.getCurrentState()
    state?.visibleElements?.contains("button") == true
}
```

**Rule**: Always use `StateManager` for condition checks, not direct element queries.

### 9. Human-Like Timing

**❌ BAD: Instant Reactions**
```kotlin
// Don't react instantly
action()
immediatelyCheck() // Too fast, not human-like
```

**✅ GOOD: Human-Like Delays**
```kotlin
// Allow time for perception and reaction
action()
delay(300) // Brief moment to see what happened
observeState()
```

**Rule**: Use small delays (100-500ms) for:
- Perception (seeing what happened)
- UI updates (animations, transitions)
- State propagation (StateManager updates)

**But**: Always prefer event-driven waits over fixed delays.

### 10. Contextual Logging

**❌ BAD: Technical Logging**
```kotlin
logger.debug("Executing tap action on element with id: button_123")
```

**✅ GOOD: Human-Like Logging**
```kotlin
logger.info("🎬 USER ACTIVITY: Creating my account...")
logger.info("💭 PERSONA THOUGHT: Hoping this works!")
logger.info("👁️  Watching what happens...")
```

**Rule**: Log from human perspective:
- What the user is doing (`🎬 USER ACTIVITY`)
- What they're thinking (`💭 PERSONA THOUGHT`)
- What they're seeing (`👁️  PERCEPTION`)
- What they're trying (`🧠 INTENTION`)

## Implementation Checklist

When implementing any pattern, verify:

- [ ] Uses event-driven waits (not fixed delays) unless LAST RESORT
- [ ] Checks visual state (`StateManager`) before system APIs
- [ ] Uses continuous monitoring loops for feedback
- [ ] Adapts strategy based on observed feedback
- [ ] Makes contextual decisions (state + history + goals)
- [ ] Uses visual-first error recovery
- [ ] Focuses attention on current goal
- [ ] Uses state-based condition checks
- [ ] Includes human-like timing (perception delays)
- [ ] Logs from human perspective

## Anti-Patterns to Avoid

### ❌ Fixed Delay Loops
```kotlin
// BAD: Arbitrary wait times
for (i in 1..10) {
    Thread.sleep(500)
    if (condition) break
}
```

### ❌ System API First
```kotlin
// BAD: Check system before visual
if (driver.isKeyboardShown()) { ... }
```

### ❌ Single-Point Checks
```kotlin
// BAD: Check once and assume
val state = getState()
if (state.isLoading) { wait() }
```

### ❌ Blind Retries
```kotlin
// BAD: Retry without observation
for (i in 1..3) {
    try { action() } catch { }
}
```

### ❌ Binary Logic
```kotlin
// BAD: Simple if/else
if (error) { retry() } else { continue() }
```

## Examples

### Good: Event-Driven Wait
```kotlin
suspend fun waitForScreen(expectedScreen: String, timeout: Long = 15000): Boolean {
    val startTime = System.currentTimeMillis()
    while (System.currentTimeMillis() - startTime < timeout) {
        val currentState = stateManager.getCurrentState()
        if (currentState?.screenName == expectedScreen) {
            return true
        }
        delay(100) // Small polling interval
    }
    return false
}
```

### Good: Visual-First Keyboard Dismissal
```kotlin
suspend fun dismissKeyboardIfBlocking(): Boolean {
    val state = stateManager.getCurrentState()
    if (state?.hasKeyboard == true) {
        // VISUAL: Dismiss using visual detection
        return dismissKeyboardVisually(state.screenshot)
    }
    return false
}
```

### Good: Adaptive Retry
```kotlin
suspend fun executeWithAdaptation(action: HumanAction): ActionResult {
    var attempt = 0
    while (attempt < MAX_ATTEMPTS) {
        val result = tryAction(action)
        if (result is ActionResult.Success) {
            return result
        }
        
        // Observe what went wrong
        val state = stateManager.getCurrentState()
        if (state?.hasKeyboard == true) {
            dismissKeyboardVisually(state.screenshot)
        } else if (state?.hasErrors == true) {
            handleError(state.errorMessages)
        }
        
        attempt++
        delay(300) // Human-like pause
    }
    return ActionResult.Failure("Max attempts reached")
}
```

## Related Documentation

- `docs/testing/VISUAL_FIRST_COMPLIANCE_REVIEW.md` - Visual-first compliance
- `docs/testing/HUMAN_COGNITION_DIAGRAM.md` - Human cognition patterns
- `.cursor/rules/testing-fallbacks.mdc` - Fallback rules

