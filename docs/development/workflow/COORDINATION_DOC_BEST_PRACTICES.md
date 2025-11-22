# Coordination Document Best Practices

**Date**: 2025-01-20  
**Status**: Active Guidelines  
**Purpose**: Prevent coordination doc conflicts and ensure smooth multi-agent collaboration

## Critical: Coordination Doc is a Shared Resource

**The coordination document (`AGENT_COORDINATION.md`) is modified by multiple agents simultaneously. Follow these practices to prevent conflicts:**

## Before Modifying Coordination Doc

### 1. Pull Latest Main (MANDATORY)

```bash
# Always pull latest before modifying coordination doc
git fetch origin main
git pull origin main
```

**Why**: Prevents merge conflicts by ensuring you have the latest version

### 2. Check for Uncommitted Changes

```bash
# Check if coordination doc has uncommitted changes
git status docs/development/workflow/AGENT_COORDINATION.md

# If it does, commit or stash before pulling
git add docs/development/workflow/AGENT_COORDINATION.md
git commit -m "docs: update coordination doc - <task-name>"
```

**Why**: Prevents losing your changes when pulling

### 3. Check for Remote Updates

```bash
# Check if coordination doc has remote updates
git fetch origin main
git diff HEAD origin/main -- docs/development/workflow/AGENT_COORDINATION.md
```

**Why**: See what changes others have made before modifying

## When Modifying Coordination Doc

### 1. Make Changes Promptly

- ✅ **Add entry immediately** when starting work
- ✅ **Update status promptly** when work progresses
- ✅ **Mark complete immediately** when work is done

**Why**: Reduces window for conflicts

### 2. Keep Entries Focused

- ✅ **One task per entry** - Don't combine multiple tasks
- ✅ **Clear task names** - Use descriptive names
- ✅ **Complete information** - Include branch, files, status

**Why**: Easier to merge if conflicts occur

### 3. Use Standard Format

- ✅ **Follow example format** - Use the standard entry format
- ✅ **Include all fields** - Branch, status, files, isolation strategy
- ✅ **Add role tags** - Use `[PLANNING]`, `[EXECUTION]`, `[VERIFICATION]` if applicable

**Why**: Consistent format makes merging easier

## Conflict Resolution

### If Conflict Occurs

1. **Don't panic** - Coordination doc conflicts are usually easy to resolve
2. **Keep both entries** - If both tasks are valid, keep both
3. **Merge carefully** - Review both versions before merging
4. **Test after merge** - Verify coordination doc is valid after merge

### Common Conflict Scenarios

#### Scenario 1: Both Agents Add New Entries

**Resolution**: Keep both entries (they're independent)

```markdown
## Active Work

### Task: Task A
- **Branch**: `feature/task-a`
- **Status**: In Progress

### Task: Task B
- **Branch**: `feature/task-b`
- **Status**: In Progress
```

#### Scenario 2: Both Agents Update Same Entry

**Resolution**: Merge both updates (combine information)

```markdown
### Task: Task A
- **Branch**: `feature/task-a`
- **Status**: In Progress
- **Files Modified**: 
  - `file1.kt` (from Agent 1)
  - `file2.kt` (from Agent 2)
```

#### Scenario 3: One Agent Adds, One Agent Removes

**Resolution**: Keep the entry (removal was premature)

```markdown
### Task: Task A
- **Branch**: `feature/task-a`
- **Status**: In Progress (keep entry)
```

## Automated Checks

### Pre-Work Check

The pre-work check now includes:
- ✅ Coordination doc uncommitted changes check
- ✅ Coordination doc remote updates check
- ✅ Warnings if coordination doc is stale

**Run before modifying coordination doc:**
```bash
./scripts/pre-work-check.sh
```

### Pre-PR Check

The pre-PR check now includes:
- ✅ Coordination doc remote updates check
- ✅ Branch documentation verification

**Run before creating PR:**
```bash
./scripts/pre-pr-check.sh
```

## Best Practices Summary

### DO ✅

- ✅ Pull latest main before modifying coordination doc
- ✅ Check for uncommitted changes before pulling
- ✅ Add entries promptly when starting work
- ✅ Update status promptly when work progresses
- ✅ Use standard entry format
- ✅ Keep entries focused (one task per entry)
- ✅ Commit coordination doc changes promptly

### DON'T ❌

- ❌ Don't modify coordination doc without pulling latest
- ❌ Don't leave uncommitted changes when pulling
- ❌ Don't delay adding entries
- ❌ Don't combine multiple tasks in one entry
- ❌ Don't use non-standard formats
- ❌ Don't forget to commit coordination doc changes

## Incident Prevention

### What Caused the Previous Conflict

**Incident**: Merge conflict in `AGENT_COORDINATION.md` (commit 004b1b7)

**Root Causes:**
1. Both agents completed work simultaneously
2. Both updated coordination doc at similar times
3. Neither checked if coordination doc had remote updates
4. No conflict detection for coordination doc itself

**Prevention Measures Now in Place:**
- ✅ Pre-work check warns about coordination doc updates
- ✅ Pre-PR check warns about coordination doc updates
- ✅ Best practices documentation
- ✅ Automated checks

## Related Documentation

- `docs/development/workflow/AGENT_COORDINATION.md` - Coordination document
- `docs/development/reports/AGENT_COORDINATION_CONFLICT_INCIDENT_REVIEW.md` - Incident review
- `scripts/pre-work-check.sh` - Pre-work validation (includes coordination doc checks)
- `scripts/pre-pr-check.sh` - Pre-PR validation (includes coordination doc checks)

