# Screen Detection Architecture Improvement

## Problem Analysis

### Current Architecture Issues

1. **Wait Patterns Blocking Detection**
   - `WaitFor ScreenChanged` executes in `ActionExecutor`
   - Human-intent detection happens inside the wait condition
   - But the detected screen state is NOT fed back to `StateManager` or `GoalManager`
   - This means the system doesn't "know" we're on a screen until after the wait completes

2. **No Feedback Loop**
   - When human-intent detection recognizes "Mood Management" screen (input field + save button detected)
   - This information is only used to return `true` from the wait condition
   - It's not stored in `StateManager.screenName`
   - It's not used to update goals (e.g., "we're on Mood Management, so we can add a mood")

3. **Goal Manager Doesn't Know Screen State**
   - Goals track high-level states (ACHIEVED, IN_PROGRESS, etc.)
   - But they don't know what screen we're on
   - This means goals can't be updated based on screen recognition
   - Example: If we detect Mood Management screen, we should update "Add mood to Mood Management" goal to IN_PROGRESS

## Proposed Solution

### Architecture Changes

1. **Feed Screen Detection Back to StateManager**
   - When `ScreenChanged` wait condition detects a screen (via human-intent detection)
   - Update `StateManager` with the detected screen name
   - This allows `ScreenMonitor` and other components to know the current screen

2. **Update GoalManager Based on Screen Recognition**
   - When we detect we're on "Mood Management" screen
   - Update relevant goals (e.g., "Add mood to Mood Management" → IN_PROGRESS)
   - This creates a feedback loop: screen detection → goal progress

3. **Make ActionExecutor Aware of StateManager**
   - Pass `StateManager` to `ActionExecutor` (or at least a callback)
   - When screen is detected, call `stateManager.onStateChanged()` with detected screen
   - This ensures state is immediately available to other components

4. **Improve Screen Recognition Feedback**
   - When human-intent detection recognizes a screen, log it clearly
   - Store the detected screen name in the action result
   - Use this to update state immediately, not just return true/false

## Implementation Plan

### Step 1: Pass StateManager to ActionExecutor
```kotlin
class ActionExecutor(
    private val driver: AndroidDriver,
    private val screenshotDir: File,
    private val stateManager: StateManager? = null  // Add this
)
```

### Step 2: Update StateManager When Screen Detected
```kotlin
// In ScreenChanged wait condition
if (screenReady) {
    // Update state manager immediately
    stateManager?.onStateChanged(
        ScreenState(
            screenName = condition.expectedScreen,
            // ... other state
        ),
        oldState = stateManager.getCurrentState()
    )
    screenDetected = true
}
```

### Step 3: Update Goals Based on Screen Recognition
```kotlin
// In TaskPlanner or GoalManager
fun onScreenDetected(screenName: String) {
    when {
        screenName.contains("Mood Management", ignoreCase = true) -> {
            // We're on Mood Management - update mood-related goals
            updateGoalState("mood_goal", GoalState.IN_PROGRESS)
        }
        screenName.contains("Sign", ignoreCase = true) -> {
            // We're on sign-in screen - update auth goals
            updateGoalState("auth_goal", GoalState.IN_PROGRESS)
        }
    }
}
```

### Step 4: Remove Redundant Waits
- If we can detect screens via human-intent, we might not need explicit `WaitFor ScreenChanged`
- The `ScreenMonitor` could detect screen changes and update state automatically
- Actions could check `StateManager.getCurrentState()` instead of waiting

## Benefits

1. **Immediate State Updates**: Screen detection immediately updates state, not just after wait completes
2. **Goal Progress Tracking**: Goals update based on screen recognition, creating better feedback
3. **Reduced Blocking**: Less need for explicit waits if state is continuously updated
4. **Better Architecture**: Clear feedback loop from detection → state → goals → planning

## Migration Strategy

1. Add `StateManager` parameter to `ActionExecutor` (optional for backward compatibility)
2. Update `ScreenChanged` wait to call `stateManager.onStateChanged()` when screen detected
3. Add goal update logic when screens are recognized
4. Test that state updates happen immediately
5. Consider removing explicit `WaitFor ScreenChanged` if `ScreenMonitor` can handle it



