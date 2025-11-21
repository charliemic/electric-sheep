# Agnostic Planner Fix

## Problem

The `GenericAdaptivePlanner` had hardcoded app-specific knowledge (e.g., "mood", "mood management") which violated the principle that it should be **agnostic** to the test journey.

## Solution

### Architecture Flow

```
Task: "Sign up and add mood value"
  ↓
TaskDecomposer (knows task semantics)
  - Extracts "mood" from task
  - Creates AbstractGoal(ADD_DATA_ENTRY, metadata={"dataType": "mood"})
  ↓
GenericAdaptivePlanner (AGNOSTIC - uses hints, not hardcoded)
  - Receives metadata hint: dataType="mood"
  - Uses hint to look for patterns like "mood", "add mood", "mood management"
  - Still works generically - if no hint, uses generic patterns
  ↓
Actions generated based on visual observation + hints
```

### Key Changes

1. **TaskDecomposer** now passes metadata hints:
   ```kotlin
   AbstractGoal(
       type = ADD_DATA_ENTRY,
       metadata = mapOf("dataType" to "mood") // Hint from task decomposition
   )
   ```

2. **GenericAdaptivePlanner** uses hints but remains generic:
   ```kotlin
   // Uses hint if available, but still works without it
   val buttonLikeText = extractButtonLikeText(visibleText, dataTypeHint)
   
   // Hint-aware but generic
   if (dataTypeHint != null && visibleText.contains(dataTypeHint)) {
       // Look for hint-related patterns
   } else {
       // Use generic patterns
   }
   ```

3. **Removed hardcoded app-specific terms**:
   - ❌ Removed: "mood management", "add mood", "track mood" from button patterns
   - ✅ Added: Generic patterns + hint-based patterns

## Benefits

1. **Agnostic**: Planner doesn't know about "mood" - it uses hints from decomposition
2. **Reusable**: Works for any app - just needs metadata hints
3. **Context-aware**: Uses hints when available, falls back to generic patterns
4. **Proper layering**: Task semantics stay in TaskDecomposer, planner stays generic

## Example

**Task:** "Sign up and add mood value"
- TaskDecomposer extracts "mood" → passes as metadata hint
- GenericAdaptivePlanner uses hint to look for "mood" patterns
- If task was "add note value", hint would be "note" instead
- Planner still works generically - just uses different hint

**Task:** "Sign up and add entry" (no specific type)
- TaskDecomposer doesn't extract specific type → no hint
- GenericAdaptivePlanner uses generic patterns only
- Still works - just less context-aware

