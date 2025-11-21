# Worktree Rule Expansion Summary

**Date**: 2025-11-20  
**Action**: Expanded cursor rule with comprehensive collision-prone patterns

## What Was Done

### 1. Codebase Analysis
- Analyzed codebase structure to identify collision-prone file patterns
- Reviewed historical collisions (MoodManagementScreen.kt collision)
- Identified 13 categories of collision-prone files

### 2. Pattern Identification

**HIGH RISK (MUST use worktree)**:
- Screen files: `app/.../ui/screens/**/*Screen.kt`
- ViewModel files: `app/.../ui/screens/**/*ViewModel.kt`
- Navigation files: `app/.../ui/navigation/*.kt`
- Application class: `app/.../*Application.kt`
- Module files: `app/.../**/*Module.kt`
- Build files: `**/*.gradle.kts`, `gradle.properties`, `settings.gradle.kts`

**MEDIUM-HIGH RISK (Should use worktree)**:
- Component files: `app/.../ui/components/*.kt`
- Manager files: `app/.../**/*Manager.kt`
- Provider files: `app/.../**/*Provider.kt`

**MEDIUM RISK (Consider worktree)**:
- Repository files: `app/.../data/repository/*Repository.kt`
- Theme files: `app/.../ui/theme/*.kt`
- Config files: `app/.../config/*.kt`
- Factory files: `app/.../**/*Factory.kt`
- MainActivity: `app/.../MainActivity.kt`

### 3. Rule Updates

**Updated `.cursor/rules/branching.mdc`**:
- Changed "RECOMMENDED" → "REQUIRED for Shared Files"
- Changed "Consider" → "MUST" for HIGH RISK files
- Added comprehensive pattern list with risk levels
- Added explicit consequences section
- Updated pre-work checklist to include pattern checking

### 4. Script Updates

**Updated `scripts/check-agent-coordination.sh`**:
- Added pattern matching for all risk levels
- Color-coded output (🔴 HIGH, 🟡 MEDIUM-HIGH, 🟠 MEDIUM)
- Clear messaging about worktree requirements
- Detects files matching collision-prone patterns

### 5. Documentation Created

- `COLLISION_PATTERN_ANALYSIS.md` - Detailed analysis of collision patterns
- `WORKTREE_RULE_EXPANSION_SUMMARY.md` - This summary document

## Key Changes

### Before
- Only 4 specific files listed as shared
- "RECOMMENDED" and "Consider" language (weak)
- No pattern matching
- No risk levels

### After
- 13+ pattern categories identified
- "REQUIRED" and "MUST" language for HIGH RISK (strong)
- Comprehensive pattern matching
- Three-tier risk system (HIGH, MEDIUM-HIGH, MEDIUM)
- Automated detection in coordination script

## Impact

### Prevention
- Agents will now see clear warnings when modifying collision-prone files
- Pattern matching catches files that weren't explicitly listed
- Risk levels guide decision-making
- Stronger language reduces ambiguity

### Example Detection
The script now correctly identifies:
```
🔴 HIGH RISK: app/src/main/java/com/electricsheep/app/ui/screens/mood/MoodManagementScreen.kt
   ⚠️  MUST use worktree - check docs/development/AGENT_COORDINATION.md
   This file pattern requires worktree isolation (no exceptions)
```

## Next Steps

1. ✅ Rules updated
2. ✅ Script updated
3. ✅ Documentation created
4. ⚠️ **Agents should review new patterns before starting work**
5. ⚠️ **Existing work should be reviewed against new patterns**

## Testing

The coordination check script was tested and correctly identifies:
- HIGH RISK files (Screen, ViewModel, Navigation, Application, Module, Build files)
- MEDIUM-HIGH RISK files (Components, Managers, Providers)
- MEDIUM RISK files (Repositories, Theme, Config, Factories, MainActivity)

## Conclusion

The cursor rule has been expanded from 4 specific files to comprehensive pattern matching covering all collision-prone areas of the codebase. The language has been strengthened from "RECOMMENDED" to "REQUIRED" for HIGH RISK files, and automated detection has been added to help agents identify collision risks before they occur.



