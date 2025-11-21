# Architecture Layering Summary

## Key Question Answered

**"Are the right things in the right layer? Should the adaptive planner be agnostic to the test journey?"**

**Answer: YES - The planner should be agnostic, and we've identified the proper layering.**

## Current Architecture Issues

### ❌ Problem 1: AdaptivePlanner Not Agnostic
- **Current**: Has hardcoded knowledge about "sign up", "mood", "email", "password"
- **Issue**: Tied to specific app features, not reusable
- **Fix**: Created `GenericAdaptivePlanner` that works with abstract goals only

### ❌ Problem 2: Missing Decomposition Layer
- **Current**: Task string parsed directly in planner with string matching
- **Issue**: No separation between task semantics and planning logic
- **Fix**: Created `TaskDecomposer` to translate tasks into abstract goals

### ❌ Problem 3: Task Definition Location
- **Current**: ✅ CORRECT - Tasks defined as natural language strings
- **Status**: This is the right approach - tasks should be intent-based, not step sequences

## Proposed Architecture Layers

### Layer 1: Task Definition ✅
**Location:** `Main.kt` or test scenarios  
**Format:** Natural language string  
**Example:** `"Sign up and add mood value"`  
**Status:** ✅ CORRECT - Keep as-is

### Layer 2: Task Decomposition ✅ NEW
**Location:** `TaskDecomposer`  
**Responsibility:** Translate natural language → Abstract goals  
**Input:** `"Sign up and add mood value"`  
**Output:** `[AbstractGoal(AUTHENTICATE), AbstractGoal(ADD_DATA_ENTRY)]`  
**Status:** ✅ CREATED

### Layer 3: Goal Management ✅
**Location:** `GoalManager`  
**Responsibility:** Manage goal hierarchy and state  
**Input:** Abstract goals from TaskDecomposer  
**Output:** Current goal, goal state, goal error  
**Status:** ✅ EXISTS - Needs enhancement for abstract goals

### Layer 4: Adaptive Planning ✅ NEW
**Location:** `GenericAdaptivePlanner`  
**Responsibility:** Work out how to achieve ANY abstract goal  
**Input:** Abstract goal + screenshot  
**Output:** Actions to achieve goal  
**Key:** ✅ AGNOSTIC - No knowledge of app features  
**Status:** ✅ CREATED

### Layer 5: Action Selection ✅
**Location:** Part of `GenericAdaptivePlanner` or separate `ActionSelector`  
**Responsibility:** Map abstract goals to concrete actions  
**Input:** Gap + visible elements  
**Output:** Concrete actions (Tap, TypeText, etc.)  
**Status:** ✅ IMPLEMENTED in GenericAdaptivePlanner

## Architecture Flow

```
┌─────────────────────────────────────────────────────────┐
│ Layer 1: Task Definition                                 │
│ "Sign up and add mood value"                           │
└────────────────────┬────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────────────────────┐
│ Layer 2: Task Decomposition (NEW)                       │
│ TaskDecomposer.decomposeTask()                          │
│ → [AUTHENTICATE, ADD_DATA_ENTRY]                       │
└────────────────────┬────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────────────────────┐
│ Layer 3: Goal Management                                 │
│ GoalManager.addGoal() → Manage hierarchy                │
│ Current Goal: AUTHENTICATE                              │
└────────────────────┬────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────────────────────┐
│ Layer 4: Adaptive Planning (NEW - AGNOSTIC)             │
│ GenericAdaptivePlanner.generatePlanForGoal()            │
│ Observe screen → Match to goal → Generate actions      │
│ → [Tap(visibleButton), TypeText(visibleField)]         │
└────────────────────┬────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────────────────────┐
│ Layer 5: Action Execution                                │
│ ActionExecutor.execute()                                │
│ → Execute actions, observe feedback                    │
└─────────────────────────────────────────────────────────┘
```

## Key Principles Enforced

### 1. Separation of Concerns ✅
- **Task Layer**: Knows about test scenarios
- **Decomposition Layer**: Knows about abstract goals
- **Planning Layer**: Knows about screen observation (AGNOSTIC)
- **Action Layer**: Knows about concrete UI interactions

### 2. Abstraction Levels ✅
- **High**: Task (natural language)
- **Medium**: Abstract Goals (Authenticate, AddDataEntry)
- **Low**: Concrete Actions (Tap, TypeText)

### 3. Agnostic Components ✅
- `GenericAdaptivePlanner`: Works with ANY abstract goal
- `GoalManager`: Works with ANY goal hierarchy
- `TaskDecomposer`: Translates ANY task to abstract goals

## Benefits

1. **Reusable**: GenericAdaptivePlanner works for any app
2. **Maintainable**: Changes to app features don't break planner
3. **Testable**: Can test planner with abstract goals
4. **Extensible**: Easy to add new goal types
5. **Human-like**: Matches how humans think (abstract goals, not specific steps)

## Next Steps

1. ✅ Created `TaskDecomposer` - Decomposes tasks into abstract goals
2. ✅ Created `GenericAdaptivePlanner` - Agnostic planner
3. ⏳ Integrate into `TaskPlanner` - Replace `generateSimplePlan`
4. ⏳ Enhance `GoalManager` - Support abstract goals
5. ⏳ Test with various tasks - Verify agnostic behavior

## Files Created

- `TaskDecomposer.kt` - Task decomposition layer
- `GenericAdaptivePlanner.kt` - Agnostic adaptive planner
- `ARCHITECTURE_LAYERING_ANALYSIS.md` - Detailed analysis
- `ARCHITECTURE_PRINCIPLE_VIOLATIONS.md` - Violations review

