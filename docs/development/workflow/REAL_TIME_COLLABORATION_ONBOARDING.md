# Real-Time Collaboration - Agent Onboarding

**Date**: 2025-01-20  
**Purpose**: Quick onboarding guide for agents to incorporate real-time collaboration

## Quick Start for Agents

### If You're Currently Working

**If you're already working on a task:**

1. **Check for rule/workflow updates** (takes 30 seconds):
   ```bash
   ./scripts/check-rule-updates.sh
   ```

2. **If updates detected, pull latest main**:
   ```bash
   git checkout main
   git pull origin main
   # Review updated rules/workflow files
   ```

3. **Rebase your branch on updated main**:
   ```bash
   git checkout feature/your-task-name
   git rebase origin/main
   # Resolve conflicts if any
   ```

4. **Continue working** - You now have the latest rules/workflow improvements!

### If You're Starting New Work

**The pre-work check now includes update detection automatically:**

1. **Run pre-work check** (includes update check):
   ```bash
   ./scripts/pre-work-check.sh
   ```

2. **If updates detected, pull latest main first**:
   ```bash
   git checkout main
   git pull origin main
   ```

3. **Then create worktree from updated main**:
   ```bash
   git worktree add ../electric-sheep-<task-name> -b feature/<task-name>
   cd ../electric-sheep-<task-name>
   ```

## What Changed

### New: Real-Time Rule/Workflow Updates

**Before:** Rules and workflow docs were static - agents might miss improvements.

**Now:** 
- ✅ Rules and workflow docs are shared in real-time
- ✅ Agents check for updates before starting work
- ✅ Improvements benefit all agents immediately
- ✅ No conflicts possible (rules/workflow are read-only during work)

### What Gets Shared

**✅ Shared (Real-Time Updates):**
- `.cursor/rules/*.mdc` - All cursor rules
- `docs/development/workflow/*.md` - Workflow documentation
- `docs/development/workflow/AGENT_COORDINATION.md` - Coordination document
- `scripts/pre-work-check.sh` - Pre-work automation
- `scripts/check-agent-coordination.sh` - Coordination checks
- `scripts/check-rule-updates.sh` - Update detection (NEW)

**❌ Isolated (No Real-Time Sharing):**
- All code files (still isolated via worktrees)
- Test files
- Build files (unless coordinated)

## Key Changes to Your Workflow

### 1. Pre-Work Check Now Includes Update Detection

**Before:**
```bash
./scripts/pre-work-check.sh
# Only checked branch, coordination, etc.
```

**Now:**
```bash
./scripts/pre-work-check.sh
# Now includes step 2.5: Check for rule/workflow updates
# Automatically detects if rules/workflow have been updated
```

### 2. New Script: Check Rule Updates

**New script available:**
```bash
./scripts/check-rule-updates.sh
```

**What it does:**
- Checks if local main is behind remote main
- Detects changes to rules/workflow files
- Shows summary of updates
- Recommends pulling latest main if updates detected

**When to use:**
- Before starting new work (or run pre-work-check which includes it)
- If you want to check manually: `./scripts/check-rule-updates.sh`

### 3. Pull Latest Main Before Creating Worktree

**Before:**
```bash
git worktree add ../electric-sheep-<task-name> -b feature/<task-name>
```

**Now (recommended):**
```bash
# Pull latest main first (includes rule/workflow updates)
git checkout main
git pull origin main

# Then create worktree from updated main
git worktree add ../electric-sheep-<task-name> -b feature/<task-name>
```

## What You Need to Do

### Immediate Actions

1. **If currently working:**
   - [ ] Run `./scripts/check-rule-updates.sh` to check for updates
   - [ ] If updates detected, pull latest main and rebase your branch
   - [ ] Review updated rules/workflow files

2. **If starting new work:**
   - [ ] Run `./scripts/pre-work-check.sh` (includes update check)
   - [ ] Pull latest main if updates detected
   - [ ] Create worktree from updated main

3. **Going forward:**
   - [ ] Always run pre-work-check before starting work (includes update check)
   - [ ] Pull latest main before creating worktree
   - [ ] Review rule/workflow updates if detected

### No Changes Required

- ✅ **Code changes still isolated** - Worktrees still required
- ✅ **Coordination still required** - Check coordination doc before modifying shared files
- ✅ **Branch naming unchanged** - Same conventions
- ✅ **Workflow unchanged** - Same process, just with update detection

## Benefits for You

### ✅ Always Have Latest Improvements

- Get rule/workflow improvements immediately
- No need to wait for coordination or merge windows
- All agents benefit from improvements immediately

### ✅ No Conflicts

- Rules/workflow docs are read-only during your work
- Git handles synchronization (standard pull workflow)
- No merge conflicts possible

### ✅ Maintained Isolation

- Code changes remain isolated (worktrees)
- No interference between agents
- Same isolation as before

## Example Workflow

### Starting New Work

```bash
# 1. Run pre-work check (includes update check)
./scripts/pre-work-check.sh

# 2. If updates detected, pull latest main
git checkout main
git pull origin main

# 3. Create worktree from updated main
git worktree add ../electric-sheep-new-feature -b feature/new-feature
cd ../electric-sheep-new-feature

# 4. Start working (you now have latest rules/workflow)
# ... make changes ...
```

### During Work

```bash
# Work in isolated worktree (same as before)
# Rules/workflow docs are read-only (don't modify during work)
# If you discover improvement, document it and create PR
```

### After Work

```bash
# Create PR with code changes
# If you improved rules/workflow, include in PR
# Other agents get updates on next git pull origin main
```

## Troubleshooting

### "Update check script not found"

**Solution:**
```bash
# Make sure you're in the repository root
cd /path/to/electric-sheep

# Check if script exists
ls -la scripts/check-rule-updates.sh

# If missing, pull latest main
git checkout main
git pull origin main
```

### "Can't access main branch"

**Solution:**
```bash
# Switch to main first
git checkout main

# Then run update check
./scripts/check-rule-updates.sh
```

### "Updates detected but can't pull"

**Solution:**
```bash
# Check for uncommitted changes
git status

# Commit or stash changes
git add .
git commit -m "WIP: current work"
# OR
git stash

# Then pull latest main
git checkout main
git pull origin main
```

## Questions?

**See full documentation:**
- `docs/development/workflow/REAL_TIME_COLLABORATION.md` - Complete guide
- `docs/development/workflow/REAL_TIME_COLLABORATION_SUMMARY.md` - Quick reference
- `docs/development/workflow/REAL_TIME_COLLABORATION_IMPLEMENTATION.md` - Implementation details

**Key points:**
- Rules/workflow are now shared in real-time
- Check for updates before starting work
- Pull latest main to get updates
- Code changes still isolated (worktrees)

