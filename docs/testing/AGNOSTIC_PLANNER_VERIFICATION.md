# Agnostic Planner Verification

## Current Implementation Review

### ✅ Generic Patterns (Allowed)

These are **generic UI patterns** that exist in any app, not app-specific:

1. **Authentication Patterns** (Generic):
   - "sign in", "sign up", "login", "authenticate" - Generic authentication concepts
   - "email", "password", "username" - Generic authentication fields
   - ✅ **ALLOWED** - These are universal UI patterns

2. **Form Patterns** (Generic):
   - "save", "submit", "create", "add" - Generic form actions
   - "input", "enter", "value" - Generic form field indicators
   - ✅ **ALLOWED** - These are universal UI patterns

3. **Navigation Patterns** (Generic):
   - "view", "history", "list", "settings", "profile" - Generic navigation concepts
   - ✅ **ALLOWED** - These are universal UI patterns

### ❌ App-Specific Terms (Forbidden)

These would be **app-specific** and must NOT appear in GenericAdaptivePlanner:

1. **App-Specific Features**:
   - ❌ "mood", "mood management", "add mood", "track mood"
   - ❌ Any app-specific feature names
   - ❌ Any app-specific data types

2. **App-Specific Screens**:
   - ❌ "Mood Management screen"
   - ❌ Any app-specific screen names

3. **App-Specific Actions**:
   - ❌ Hardcoded actions for specific app features
   - ❌ Assumptions about app structure

## Verification Checklist

### GenericAdaptivePlanner Review

- [x] ✅ No "mood" references (removed)
- [x] ✅ No app-specific feature names
- [x] ✅ Authentication patterns are generic (sign in, login, etc.)
- [x] ✅ Form patterns are generic (save, submit, etc.)
- [x] ✅ Navigation patterns are generic (view, history, etc.)
- [x] ✅ Uses metadata hints from TaskDecomposer
- [x] ✅ Works without hints (generic patterns only)
- [x] ✅ All context comes from `goal.metadata` or `goal.type`

### TaskDecomposer Review

- [x] ✅ Extracts app-specific terms (e.g., "mood" from "add mood value")
- [x] ✅ Passes as metadata hints (metadata={"dataType": "mood"})
- [x] ✅ Creates abstract goals (AUTHENTICATE, ADD_DATA_ENTRY, etc.)
- [x] ✅ Knows about task semantics (appropriate layer)

## Architecture Compliance

### ✅ Correct Flow

```
Task: "Sign up and add mood value"
  ↓
TaskDecomposer (knows "mood" is a data type)
  → AbstractGoal(ADD_DATA_ENTRY, metadata={"dataType": "mood"})
  ↓
GenericAdaptivePlanner (doesn't know what "mood" means)
  → Uses metadata hint to look for "mood" patterns
  → Falls back to generic patterns if no hint
  ↓
Actions: Based on visual observation + hints
```

### ❌ Incorrect Flow (Violation)

```
Task: "Sign up and add mood value"
  ↓
GenericAdaptivePlanner (hardcoded "mood" knowledge)
  → if (text.contains("mood")) { ... }  // ❌ VIOLATION
```

## Rule Enforcement

### Code Review Checklist

When reviewing `GenericAdaptivePlanner.kt`:

1. **Search for app-specific terms:**
   ```bash
   grep -i "mood\|app-specific-feature" GenericAdaptivePlanner.kt
   ```
   - Should return NO matches

2. **Verify metadata usage:**
   ```bash
   grep "metadata\|hint" GenericAdaptivePlanner.kt
   ```
   - Should show hints are used, not hardcoded values

3. **Verify generic patterns:**
   - Authentication patterns: Generic (sign in, login, etc.)
   - Form patterns: Generic (save, submit, etc.)
   - Navigation patterns: Generic (view, history, etc.)

### Automated Checks

Add to CI/CD:
```bash
# Check for app-specific terms in GenericAdaptivePlanner
grep -i "mood\|app-specific" GenericAdaptivePlanner.kt && exit 1 || exit 0
```

## Examples

### ✅ GOOD: Using Metadata Hints

```kotlin
// Get hint from metadata (passed from TaskDecomposer)
val dataTypeHint = goal.metadata["dataType"] as? String

// Use hint if available (but not required)
if (dataTypeHint != null) {
    val hintPatterns = listOf(hint, "add $hint", "$hint management")
    // Look for hint-related patterns
}
```

### ❌ BAD: Hardcoded App Knowledge

```kotlin
// Hardcoded app-specific knowledge
if (visibleText.contains("mood", ignoreCase = true)) {
    actions.add(HumanAction.Tap(target = "Mood Management"))
}
```

### ✅ GOOD: Generic Patterns

```kotlin
// Generic authentication patterns
val authPatterns = listOf("sign in", "sign up", "login")
// Generic form patterns
val formPatterns = listOf("save", "submit", "create")
```

### ❌ BAD: App-Specific Patterns

```kotlin
// App-specific patterns
val patterns = listOf("mood management", "add mood", "track mood")
```

## Related Documentation

- `.cursor/rules/agnostic-planner-principle.mdc` - Strict rule
- `docs/testing/ARCHITECTURE_LAYERING_ANALYSIS.md` - Architecture
- `docs/testing/AGNOSTIC_PLANNER_FIX.md` - Fix documentation

