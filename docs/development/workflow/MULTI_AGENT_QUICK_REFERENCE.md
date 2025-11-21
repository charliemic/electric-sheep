# Multi-Agent Workflow Quick Reference

**For detailed guidelines, see:** `MULTI_AGENT_WORKFLOW.md`

## 🚨 CRITICAL: Before Any Work

```bash
# 1. Check you're NOT on main
git status

# 2. If on main, create feature branch IMMEDIATELY
git checkout -b feature/<task-name>

# 3. OR use git worktree for file system isolation (RECOMMENDED)
./scripts/create-worktree.sh <task-name>

# 4. Pull latest main
git checkout main && git pull origin main && git checkout feature/<task-name>

# 5. Check coordination
./scripts/check-agent-coordination.sh
```

## File System Isolation (RECOMMENDED)

**Use git worktrees for complete file system isolation:**

```bash
# Create isolated worktree
./scripts/create-worktree.sh test-helpers
cd ../electric-sheep-test-helpers
# Work in complete isolation - no file system conflicts!
```

**Benefits:**
- ✅ Complete file system isolation
- ✅ No conflicts with other agents
- ✅ Each agent has their own directory
- ✅ Easy cleanup after merge

## Branch Naming

**Format:** `<type>/<task-description>`

**Examples:**
- `feature/test-helpers`
- `feature/env-switch`
- `fix/login-bug`
- `refactor/component-cleanup`

**Note:** Agents are ephemeral - names describe tasks, not agents.

## Shared Files (Require Coordination)

These files commonly need coordination:
- `app/.../ui/screens/LandingScreen.kt`
- `app/.../ElectricSheepApplication.kt`
- `app/.../data/DataModule.kt`
- `app/build.gradle.kts`, `build.gradle.kts`

**Solution:** Use git worktree when modifying shared files!

## Workflow Checklist

### Before Starting
- [ ] On feature branch (not `main`)
- [ ] Branch name follows convention
- [ ] Consider git worktree for isolation
- [ ] Pulled latest `main`
- [ ] Checked `AGENT_COORDINATION.md`
- [ ] Documented your work in coordination doc

### During Work
- [ ] Commit frequently
- [ ] Push branch regularly
- [ ] Update coordination doc if scope changes
- [ ] Test in isolation

### Before Merge
- [ ] Rebased on latest `main`
- [ ] All conflicts resolved
- [ ] All tests passing
- [ ] Documentation updated
- [ ] Coordination doc updated to "Complete"

### After Merge
- [ ] Remove worktree (if used): `git worktree remove ../electric-sheep-<task-name>`
- [ ] Delete local branch: `git branch -d feature/<task-name>`

## Quick Commands

```bash
# Check coordination
./scripts/check-agent-coordination.sh

# Create isolated worktree
./scripts/create-worktree.sh <task-name>

# Sync with main
git fetch origin && git rebase origin/main

# Create feature branch
git checkout -b feature/<task-name>

# Push branch
git push -u origin feature/<task-name>

# List worktrees
git worktree list

# Remove worktree
git worktree remove ../electric-sheep-<task-name>
```

## Cursor Rules

Cursor automatically enforces:
- ✅ Branch check before changes
- ✅ Branch naming convention
- ✅ Coordination doc check
- ✅ File system isolation reminders

See: `.cursor/rules/branching.mdc`

## Need Help?

- **Full Guidelines:** `docs/development/MULTI_AGENT_WORKFLOW.md`
- **Coordination:** `docs/development/AGENT_COORDINATION.md`
- **Evaluation:** `docs/development/MULTI_AGENT_WORKFLOW_EVALUATION.md`
