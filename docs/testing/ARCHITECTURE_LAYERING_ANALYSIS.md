# Architecture Layering Analysis

## Current Architecture Issues

### Problem 1: AdaptivePlanner is NOT Agnostic

**Current State:**
- `AdaptivePlanner` has hardcoded knowledge about "sign up", "mood", "email", "password"
- It knows about specific app features
- It's tied to the test journey, not generic

**Issue:** The planner should work out how to achieve ANY goal, not just specific app features.

### Problem 2: Task Decomposition Missing

**Current State:**
- `TaskPlanner` receives task string: "Sign up and add mood value"
- Parses it with string matching: `task.contains("sign up")`
- Directly generates actions for specific features

**Issue:** There's no layer that decomposes high-level tasks into abstract goals.

### Problem 3: GoalManager Not Used for Decomposition

**Current State:**
- `GoalManager` exists but is only used for tracking goal state
- Tasks are not decomposed into goals
- Goals are not abstract - they're still tied to specific features

**Issue:** GoalManager should manage abstract goals, not feature-specific ones.

## Proposed Architecture

### Layer 1: Task Definition (Natural Language)
**Location:** `Main.kt` or test scenarios
**Responsibility:** Define high-level intent
**Example:** "Sign up and add mood value"

### Layer 2: Task Decomposition
**Location:** `TaskDecomposer` (NEW)
**Responsibility:** Break task into abstract goals
**Example:** 
- Task: "Sign up and add mood value"
- Goals: 
  - Goal1: "Authenticate" (abstract)
  - Goal2: "Add Data Entry" (abstract)

**Key:** Goals are abstract, not feature-specific.

### Layer 3: Goal Management
**Location:** `GoalManager` (EXISTING, but needs enhancement)
**Responsibility:** 
- Manage goal hierarchy
- Track goal state
- Calculate goal error
- Determine current goal

**Key:** Works with abstract goals, not app-specific features.

### Layer 4: Adaptive Planning (Generic)
**Location:** `AdaptivePlanner` (REFACTOR)
**Responsibility:** 
- Observe screen visually
- Match current state to abstract goal
- Generate actions to achieve goal
- **MUST BE AGNOSTIC** - works with any abstract goal

**Key:** No knowledge of "sign up", "mood", etc. Only knows abstract goals like "Authenticate", "Add Data Entry".

### Layer 5: Action Selection
**Location:** `ActionSelector` (NEW or part of AdaptivePlanner)
**Responsibility:**
- Based on abstract goal and visible elements
- Selects specific actions (Tap, TypeText, etc.)
- Uses visual observation to find elements

**Key:** Maps abstract goals to concrete actions based on what's visible.

## Example Flow

### Current (BAD):
```
Task: "Sign up and add mood value"
  ↓
TaskPlanner: task.contains("sign up") → Generate hardcoded actions
  ↓
Actions: [Tap("Mood Management"), Tap("Show email..."), ...]
```

### Proposed (GOOD):
```
Task: "Sign up and add mood value"
  ↓
TaskDecomposer: Decompose into abstract goals
  Goals: [Authenticate, AddDataEntry]
  ↓
GoalManager: Manage goal hierarchy
  Current Goal: Authenticate
  ↓
AdaptivePlanner: Observe screen → Match to goal → Generate actions
  Observation: "I see a sign-in screen"
  Goal: "Authenticate"
  Gap: "Need to fill form and submit"
  Actions: [Find email field visually, Type email, ...]
  ↓
ActionSelector: Map to concrete actions
  Actions: [Tap(visibleButton), TypeText(visibleField), ...]
```

## Key Principles

### 1. Separation of Concerns
- **Task Layer**: Knows about test scenarios
- **Decomposition Layer**: Knows about abstract goals
- **Planning Layer**: Knows about screen observation and action generation
- **Action Layer**: Knows about concrete UI interactions

### 2. Abstraction Levels
- **High**: Task (natural language)
- **Medium**: Abstract Goals (Authenticate, AddDataEntry, Navigate)
- **Low**: Concrete Actions (Tap, TypeText, Swipe)

### 3. Agnostic Components
- `AdaptivePlanner` should work with ANY abstract goal
- `GoalManager` should work with ANY goal hierarchy
- `ActionSelector` should work with ANY visible elements

## Implementation Plan

1. **Create `TaskDecomposer`**: Decompose tasks into abstract goals
2. **Enhance `GoalManager`**: Support abstract goal types
3. **Refactor `AdaptivePlanner`**: Make it generic and agnostic
4. **Create `ActionSelector`**: Map abstract goals to concrete actions
5. **Update `TaskPlanner`**: Use decomposition and adaptive planning

## Abstract Goal Types

Instead of:
- ❌ "Sign up"
- ❌ "Add mood"
- ❌ "Navigate to mood management"

Use:
- ✅ "Authenticate" (covers sign up, sign in, login)
- ✅ "AddDataEntry" (covers any form submission)
- ✅ "NavigateToFeature" (covers any navigation)
- ✅ "ViewData" (covers viewing any list/history)
- ✅ "UpdateData" (covers editing any entry)

## Benefits

1. **Reusable**: AdaptivePlanner works for any app
2. **Maintainable**: Changes to app features don't break planner
3. **Testable**: Can test planner with abstract goals
4. **Extensible**: Easy to add new goal types
5. **Human-like**: Matches how humans think (abstract goals, not specific steps)

