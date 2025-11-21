# Adaptive Planning Architecture

## Problem: Fixed Pathways

The current `generateSimplePlan` function uses hardcoded action sequences based on task keywords. This is too rigid - it doesn't let the agent "work out" how to achieve the intent.

**Current approach (BAD):**
```kotlin
when {
    task.contains("sign up") && task.contains("mood") -> {
        // Fixed sequence of actions
        listOf(
            HumanAction.Tap("Mood Management"),
            HumanAction.Tap("Show email and password sign in"),
            // ... hardcoded sequence
        )
    }
}
```

**Problem:** This doesn't adapt to:
- Different screen states
- Errors encountered
- What the agent actually sees
- Alternative pathways

## Solution: Visual-First Adaptive Planning

The agent should:
1. **OBSERVE** what's on screen (visual analysis)
2. **ORIENT** based on what it sees (not fixed scripts)
3. **DECIDE** what action to take next (based on current state)
4. **ACT** and observe feedback
5. **ADAPT** plan based on feedback

### Adaptive Planning Flow

```
1. Capture screenshot
2. Analyze screen visually (OCR + Pattern Recognition)
3. Extract visible elements and state
4. Match current state to goal
5. Generate next action based on:
   - What's visible (not what we expect)
   - What we need to achieve (goal)
   - What we've tried before (history)
6. Execute action
7. Observe feedback
8. Adapt if needed
```

### Implementation Strategy

**Replace fixed pathways with:**
- Visual observation of current screen
- Goal-oriented action selection
- Adaptive retry based on feedback
- Multiple pathway support

**Key Components:**
1. `AdaptivePlanner` - Uses visual observation to generate plans
2. `GoalMatcher` - Matches current state to goals
3. `ActionSelector` - Selects next action based on what's visible
4. `PathwayExplorer` - Tries alternative pathways when stuck

## Next Steps

1. Create `AdaptivePlanner` class
2. Integrate with `TaskPlanner`
3. Replace `generateSimplePlan` with adaptive approach
4. Test with various screen states

