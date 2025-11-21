# Architecture Principle Violations Review

## Overview

This document identifies violations of our core principles across the codebase and provides recommendations for fixes.

## Principles Reviewed

1. **Human-Focused Patterns** (Event-driven, Visual-first, Adaptive)
2. **Visual-First Principle** (Screenshots before system APIs)
3. **Separation of Concerns** (Proper layering, Agnostic components)

## Violations Found

### 1. Fixed Delays (Human-Focused Pattern Violation)

**Location:** `ActionExecutor.kt`

**Violations:**
```kotlin
Thread.sleep(300) // Brief wait for focus (line 73)
Thread.sleep(200) // Clear wait (line 78)
Thread.sleep(2000) // UI update wait (line 198)
```

**Issue:** Using fixed delays instead of event-driven waits.

**Fix:** Replace with event-driven waits:
```kotlin
// Instead of Thread.sleep(300)
waitFor { element.isFocused || element.isEnabled }

// Instead of Thread.sleep(2000)
waitFor { !stateManager.getCurrentState()?.isLoading }
```

**Priority:** HIGH - Core principle violation

### 2. System API Before Visual Detection (Visual-First Violation)

**Location:** `ActionExecutor.kt` - `executeWaitFor()`

**Violations:**
```kotlin
// Line 188: Using driver.findElements() for state detection
val elements = driver.findElements(AppiumBy.accessibilityId(condition.expectedScreen))

// Line 199: Using driver.findElements() for loading detection
val loadingElements = driver.findElements(...)

// Line 237: Using driver.findElements() for text detection
val elements = driver.findElements(By.xpath("//*"))

// Line 295: Using driver.findElement() for text presence
driver.findElement(By.xpath("//*[contains(@text, '${condition.text}')]"))
```

**Issue:** Using Appium element queries for state detection instead of visual analysis.

**Fix:** Use visual detection first:
```kotlin
// Instead of driver.findElements()
val state = stateManager.getCurrentState()
val hasElement = state?.visibleElements?.contains(target) == true

// Instead of driver.findElement() for text
val text = textDetector.extractText(screenshot)
val hasText = text.fullText.contains(target)
```

**Priority:** CRITICAL - Core visual-first principle violation

### 3. Fixed Pathways (Separation of Concerns Violation)

**Location:** `TaskPlanner.kt` - `generateSimplePlan()`

**Violations:**
```kotlin
// Hardcoded action sequences based on task keywords
when {
    task.contains("sign up") && task.contains("mood") -> {
        listOf(
            HumanAction.Tap("Mood Management"),
            HumanAction.Tap("Show email and password sign in"),
            // ... hardcoded sequence
        )
    }
}
```

**Issue:** Planner knows about specific app features, not abstract goals.

**Fix:** Use TaskDecomposer + GenericAdaptivePlanner:
```kotlin
// Decompose task into abstract goals
val goals = taskDecomposer.decomposeTask(task)

// Use generic planner for each goal
val actions = genericPlanner.generatePlanForGoal(goal, screenshot, history)
```

**Priority:** HIGH - Architecture violation

### 4. Task Definition Location

**Current:** Task defined as natural language string in `Main.kt`

**Status:** ✅ CORRECT - Tasks should be defined as natural language, not step sequences.

**Recommendation:** Keep as-is. This is the right approach.

### 5. Goal Decomposition Missing

**Current:** No layer between task and planner

**Issue:** Tasks are parsed directly in planner with string matching.

**Fix:** Add `TaskDecomposer` layer:
- Input: Natural language task
- Output: Abstract goals
- Location: Between TaskPlanner and AdaptivePlanner

**Priority:** HIGH - Missing architectural layer

### 6. Planner Not Agnostic

**Current:** `AdaptivePlanner` (old) has hardcoded knowledge about "sign up", "mood", etc.

**Issue:** Planner is tied to specific app features.

**Fix:** Use `GenericAdaptivePlanner` which works with abstract goals only.

**Priority:** HIGH - Separation of concerns violation

## Architecture Layers (Current vs Proposed)

### Current Architecture (BAD)
```
Task: "Sign up and add mood value"
  ↓
TaskPlanner: String matching → Hardcoded actions
  ↓
Actions: [Tap("Mood Management"), ...]
```

**Problems:**
- No decomposition layer
- Planner knows about app features
- Fixed pathways

### Proposed Architecture (GOOD)
```
Task: "Sign up and add mood value"
  ↓
TaskDecomposer: Decompose into abstract goals
  Goals: [Authenticate, AddDataEntry]
  ↓
GoalManager: Manage goal hierarchy
  Current Goal: Authenticate
  ↓
GenericAdaptivePlanner: Observe → Match → Generate
  Observation: "I see authentication screen"
  Goal: "Authenticate"
  Gap: "Need to fill form"
  Actions: [Find fields visually, Type, Submit]
  ↓
ActionSelector: Map to concrete actions
  Actions: [Tap(visibleButton), TypeText(visibleField)]
```

**Benefits:**
- Proper layering
- Planner is agnostic
- Adaptive to screen state

## Fix Priority

### Critical (Fix Immediately)
1. ✅ System API usage in `ActionExecutor.executeWaitFor()` - Replace with visual detection
2. ✅ System API usage in `ActionExecutor.executeVerify()` - Replace with visual detection

### High (Fix Soon)
3. ✅ Fixed delays in `ActionExecutor` - Replace with event-driven waits
4. ✅ Fixed pathways in `TaskPlanner` - Use TaskDecomposer + GenericAdaptivePlanner
5. ✅ Missing TaskDecomposer layer - Create and integrate

### Medium (Improve)
6. ⚠️ GoalManager not used for decomposition - Enhance to support abstract goals
7. ⚠️ Planner has app-specific knowledge - Refactor to GenericAdaptivePlanner

## Implementation Checklist

### Phase 1: Fix Critical Violations
- [ ] Replace `driver.findElements()` in `executeWaitFor()` with visual detection
- [ ] Replace `driver.findElement()` in `executeVerify()` with visual detection
- [ ] Test visual detection works correctly

### Phase 2: Fix High Priority
- [ ] Replace fixed delays with event-driven waits
- [ ] Create `TaskDecomposer` class
- [ ] Create `GenericAdaptivePlanner` class
- [ ] Refactor `TaskPlanner` to use decomposition + generic planner
- [ ] Test adaptive planning works

### Phase 3: Enhance Architecture
- [ ] Enhance `GoalManager` to support abstract goals
- [ ] Integrate `TaskDecomposer` with `GoalManager`
- [ ] Remove app-specific knowledge from planners
- [ ] Test with multiple different tasks

## Related Documentation

- `docs/testing/HUMAN_FOCUSED_PATTERNS.md` - Pattern principles
- `docs/testing/ARCHITECTURE_LAYERING_ANALYSIS.md` - Layering analysis
- `.cursor/rules/visual-first-principle.mdc` - Visual-first rule
- `.cursor/rules/human-focused-patterns.mdc` - Human-focused patterns rule

