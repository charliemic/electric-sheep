# Why Agents Didn't Use Worktrees - Rule Analysis

**Date**: 2025-11-20  
**Issue**: Two agents modified same file without using worktrees despite cursor rule

## The Rule Language Problem

### Current Rule Wording

The cursor rule (`.cursor/rules/branching.mdc`) uses **weak, permissive language**:

1. **Line 48**: `## File System Isolation (RECOMMENDED)`
   - ❌ Says "RECOMMENDED" not "MANDATORY" or "REQUIRED"
   - Agents interpret this as optional

2. **Line 70**: `3. ✅ **Consider file system isolation** - Use git worktree if working on shared files`
   - ❌ Says "Consider" not "MUST" or "REQUIRED"
   - "Consider" implies it's optional

3. **Line 88**: `3. Consider using git worktree for isolation`
   - ❌ Again, "Consider" - weak language

4. **Line 111**: `- Consider git worktree if modifying shared files`
   - ❌ "Consider" - not a requirement

### What IS Mandatory

The rule clearly marks these as MANDATORY:
- ✅ **Line 7**: `## CRITICAL: Never Work on Main Branch`
- ✅ **Line 9**: `**Before making ANY changes, you MUST:**`
- ✅ **Line 66**: `Before starting any work, you MUST:`
- ✅ **Line 68**: `1. ✅ **Verify NOT on main branch**`
- ✅ **Line 69**: `2. ✅ **Create feature branch**`

### The Problem

**Worktrees are presented as optional recommendations**, not requirements:
- "RECOMMENDED" suggests it's nice-to-have
- "Consider" suggests thinking about it, not doing it
- No enforcement mechanism
- No consequences mentioned for not using worktrees

## Shared Files List Issue

### Current Shared Files List (Line 79-83)

```markdown
**Shared files (require coordination):**
- `app/.../ui/screens/LandingScreen.kt`
- `app/.../ElectricSheepApplication.kt`
- `app/.../data/DataModule.kt`
- `app/build.gradle.kts`, `build.gradle.kts`
```

**Problem**: `MoodManagementScreen.kt` is **NOT listed** as a shared file!

- Agents didn't know it was a shared file requiring coordination
- No explicit guidance that screen files are shared
- The list is incomplete

## Why Agents Didn't Use Worktrees

### Agent Reasoning (Inferred)

1. **"RECOMMENDED" means optional**
   - Rule says "RECOMMENDED" not "REQUIRED"
   - Agents followed mandatory rules (branch, coordination doc)
   - Skipped optional recommendations (worktrees)

2. **"Consider" means think about it, not do it**
   - "Consider file system isolation" = think about it
   - Not "MUST use file system isolation"
   - Agents considered it, decided it wasn't necessary

3. **File not in shared files list**
   - `MoodManagementScreen.kt` not explicitly listed
   - Agents didn't realize it needed coordination
   - No trigger to use worktree

4. **Branch isolation seemed sufficient**
   - Both agents created feature branches ✅
   - Both checked coordination doc ✅
   - Worktrees seemed like extra work for unclear benefit

5. **No enforcement or consequences**
   - Rule doesn't say "you will cause collisions if you don't"
   - No automated checks
   - No negative consequences mentioned

## The Collision That Resulted

### What Happened

1. **Restore Design Work Agent**:
   - ✅ Created feature branch: `feature/restore-design-work`
   - ✅ Checked coordination doc (but no entry for mood visualization)
   - ❌ Didn't use worktree (rule said "RECOMMENDED")
   - ❌ Modified `MoodManagementScreen.kt` (not in shared files list)

2. **Mood Chart Visualization Agent**:
   - ✅ Created feature branch: `feature/mood-chart-visualization`
   - ❌ Didn't update coordination doc (no entry found)
   - ❌ Didn't use worktree (rule said "RECOMMENDED")
   - ❌ Modified `MoodManagementScreen.kt` (not in shared files list)

### Result

- Both agents modified same file
- No file system isolation
- Collision detected after the fact
- Had to manually resolve conflicts

## How to Fix the Rule

### Option 1: Make Worktrees Mandatory for Shared Files

```markdown
## File System Isolation (MANDATORY for Shared Files)

**When modifying shared files, you MUST use git worktrees:**

1. **Check if file is shared:**
   - Any file in `app/.../ui/screens/` is a shared file
   - Check `AGENT_COORDINATION.md` for other shared files

2. **If modifying shared file, MANDATORY to use worktree:**
   ```bash
   git worktree add ../electric-sheep-<task-name> -b feature/<task-name>
   cd ../electric-sheep-<task-name>
   ```

3. **If NOT using worktree, you MUST:**
   - Verify no other agents are working on the file
   - Document in coordination doc immediately
   - Accept risk of collisions
```

### Option 2: Expand Shared Files List

```markdown
**Shared files (require coordination AND worktree):**
- `app/.../ui/screens/*.kt` (ALL screen files)
- `app/.../ui/screens/LandingScreen.kt`
- `app/.../ui/screens/mood/MoodManagementScreen.kt`
- `app/.../ElectricSheepApplication.kt`
- `app/.../data/DataModule.kt`
- `app/build.gradle.kts`, `build.gradle.kts`
```

### Option 3: Stronger Language

Change from:
- ❌ "RECOMMENDED"
- ❌ "Consider"

To:
- ✅ "REQUIRED for shared files"
- ✅ "MUST use worktree when modifying shared files"
- ✅ "MANDATORY for file system isolation"

### Option 4: Add Enforcement

```markdown
## Pre-Work Enforcement

**Before modifying ANY file, the agent MUST:**

1. Check if file is in shared files list
2. If shared file:
   - **MUST** check coordination doc
   - **MUST** use worktree (no exceptions)
   - **MUST** document work in coordination doc
3. If not shared file:
   - Still recommended to use worktree
   - Still must check coordination doc
```

## Recommended Fix

### Immediate Changes

1. **Change "RECOMMENDED" to "REQUIRED for shared files"**
2. **Change "Consider" to "MUST" for shared files**
3. **Add `app/.../ui/screens/*.kt` to shared files list**
4. **Add explicit `MoodManagementScreen.kt` to list**
5. **Add consequences section**: "Collisions will occur if worktrees not used"

### Updated Rule Section

```markdown
## File System Isolation (REQUIRED for Shared Files)

**When modifying shared files, you MUST use git worktrees:**

```bash
# Create isolated worktree (MANDATORY for shared files)
git worktree add ../electric-sheep-<task-name> -b feature/<task-name>
cd ../electric-sheep-<task-name>
```

**Shared files (REQUIRE worktree isolation):**
- `app/.../ui/screens/*.kt` (ALL screen files)
- `app/.../ui/screens/mood/MoodManagementScreen.kt`
- `app/.../ui/screens/LandingScreen.kt`
- `app/.../ElectricSheepApplication.kt`
- `app/.../data/DataModule.kt`
- `app/build.gradle.kts`, `build.gradle.kts`

**If you modify a shared file without a worktree:**
- ❌ You WILL cause collisions with other agents
- ❌ Your changes may be lost or overwritten
- ❌ You will need to manually resolve conflicts
- ✅ Use worktree to prevent all of these issues
```

## Conclusion

**Root Cause**: The rule uses weak, permissive language ("RECOMMENDED", "Consider") instead of strong, mandatory language ("MUST", "REQUIRED").

**Solution**: 
1. Change language to make worktrees mandatory for shared files
2. Expand shared files list to include all screen files
3. Add explicit consequences for not using worktrees
4. Make the rule enforcement clearer

**Key Insight**: Rules need to be **prescriptive, not permissive** when preventing collisions is critical.

