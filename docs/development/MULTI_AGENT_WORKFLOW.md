# Multi-Agent Workflow Guidelines

**Last Updated**: 2025-11-19  
**Status**: Active Guidelines

## Overview

This document defines the standard workflow for multiple AI agents working simultaneously on this codebase. **Agents are ephemeral** - each agent works on a specific task and then completes. Following these guidelines prevents conflicts, ensures code quality, and maintains a clean git history.

## Core Principles

### 1. Branch Isolation (MANDATORY)
- **Never work on `main` branch** - Always create a feature branch
- **One task per branch** - Each agent works on their own feature branch for a specific task
- **Branch naming**: `feature/<task-description>` or `<type>/<task-description>`
- **Example**: `feature/test-helpers`, `feature/env-switch`, `feature/design-system`

### 2. File System Isolation (RECOMMENDED)
- **Git Worktrees** - Use separate working directories for complete isolation
- **Workspace Separation** - Use separate directory clones if needed
- **Coordination** - Always check coordination doc before starting

### 3. Coordination Before Conflict
- Check `AGENT_COORDINATION.md` before starting work
- Document which files you'll modify
- Coordinate if files overlap with other agents' work

### 4. Regular Synchronization
- Pull from `main` daily: `git pull origin main`
- Rebase feature branch on `main` before major work
- Resolve conflicts immediately, not at merge time

## File System Isolation Strategies

### Strategy 1: Git Worktrees (RECOMMENDED)

**Git worktrees allow multiple working directories for the same repository, each on a different branch.**

#### Benefits
- ✅ Complete file system isolation - each agent has their own directory
- ✅ No branch switching needed - each worktree is on its own branch
- ✅ Same repository - shared git history and objects
- ✅ Easy cleanup - just remove the worktree directory

#### Setup

1. **Create a worktree for your feature branch:**
   ```bash
   # From main repository
   git worktree add ../electric-sheep-<task-name> -b feature/<task-name>
   ```

2. **Work in the new directory:**
   ```bash
   cd ../electric-sheep-<task-name>
   # Now you're in an isolated directory on your feature branch
   ```

3. **List worktrees:**
   ```bash
   git worktree list
   ```

4. **Remove worktree when done:**
   ```bash
   # From main repository
   git worktree remove ../electric-sheep-<task-name>
   # Or just delete the directory and run:
   git worktree prune
   ```

#### Example
```bash
# Agent working on test helpers
git worktree add ../electric-sheep-test-helpers -b feature/test-helpers
cd ../electric-sheep-test-helpers
# Work on test helpers in isolation

# Another agent working on design system
git worktree add ../electric-sheep-design-system -b feature/design-system
cd ../electric-sheep-design-system
# Work on design system in isolation
```

#### Best Practices
- Use descriptive directory names: `electric-sheep-<task-name>`
- Create worktree from main repository (not from another worktree)
- Remove worktrees after merge to keep things clean
- Each worktree should be on a different branch

### Strategy 2: Separate Repository Clones

**For complete isolation, clone the repository to separate directories.**

#### Setup

1. **Clone to separate directory:**
   ```bash
   git clone <repo-url> ../electric-sheep-<task-name>
   cd ../electric-sheep-<task-name>
   git checkout -b feature/<task-name>
   ```

2. **Work in isolation:**
   - Each clone is completely independent
   - No file system conflicts possible
   - More disk space required

3. **Sync and merge:**
   - Push branch from clone
   - Create PR from branch
   - Merge via main repository

#### When to Use
- When you need complete isolation
- When working on large refactors
- When multiple agents need to modify the same files simultaneously

### Strategy 3: Branch-Based Isolation (Current Default)

**Use git branches with coordination - works but requires careful coordination.**

#### Setup
- Each agent creates a feature branch
- All work in same directory
- Must coordinate through `AGENT_COORDINATION.md`

#### Limitations
- ⚠️ File system conflicts possible if agents modify same files
- ⚠️ Requires careful coordination
- ⚠️ Branch switching can be error-prone

#### When to Use
- When tasks don't overlap
- When coordination is easy
- When disk space is limited

## Workflow Steps

### Phase 1: Pre-Work Setup (REQUIRED)

**Before making ANY changes:**

1. **Choose Isolation Strategy**
   - **Recommended**: Git worktree for file system isolation
   - **Alternative**: Separate clone for complete isolation
   - **Fallback**: Branch-based with coordination

2. **If Using Git Worktree:**
   ```bash
   # From main repository
   git checkout main
   git pull origin main
   git worktree add ../electric-sheep-<task-name> -b feature/<task-name>
   cd ../electric-sheep-<task-name>
   ```

3. **If Using Branch-Based:**
   ```bash
   git checkout main
   git pull origin main
   git checkout -b feature/<task-name>
   ```

4. **Check Coordination Document**
   - Read `docs/development/AGENT_COORDINATION.md`
   - Verify no conflicts with other agents' work
   - Document your planned changes

5. **Update Coordination Document**
   - Add your work entry to `AGENT_COORDINATION.md`
   - List files you'll modify
   - Set status to "In Progress"
   - Note your isolation strategy (worktree/clone/branch)

### Phase 2: During Work

**While implementing changes:**

1. **Commit Frequently**
   - Commit at least daily
   - Use descriptive commit messages
   - Keep commits atomic (one logical change)

2. **Push Regularly**
   ```bash
   git push -u origin feature/<task-name>
   # Use -u on first push, then just git push
   ```
   - This backs up your work
   - Triggers CI/CD checks

3. **Update Coordination Doc**
   - If scope changes, update `AGENT_COORDINATION.md`
   - Document any new files you're modifying

4. **Test in Isolation**
   - Run tests on your branch
   - Ensure your changes work independently
   - Fix any test failures immediately

5. **Sync with Main Periodically**
   ```bash
   git fetch origin
   git rebase origin/main
   # Resolve conflicts if any
   ```

### Phase 3: Pre-Merge Preparation

**Before creating Pull Request:**

1. **Final Sync with Main**
   ```bash
   git fetch origin
   git rebase origin/main
   ```

2. **Resolve All Conflicts**
   - Understand both changes
   - Merge thoughtfully
   - Test thoroughly after resolving

3. **Run All Tests**
   ```bash
   ./gradlew test
   # Ensure all tests pass
   ```

4. **Update Documentation**
   - Update relevant docs
   - Add/update code comments
   - Update `AGENT_COORDINATION.md` status

5. **Verify Branch Naming**
   - Ensure branch follows convention
   - Check Cursor rules are being followed

### Phase 4: Merge and Cleanup

**After PR is approved and merged:**

1. **If Using Git Worktree:**
   ```bash
   # From main repository
   cd /path/to/main/repo
   git worktree remove ../electric-sheep-<task-name>
   # Or if directory already deleted:
   git worktree prune
   ```

2. **If Using Branch-Based:**
   ```bash
   git checkout main
   git pull origin main
   git branch -d feature/<task-name>
   ```

3. **Update Coordination Document**
   - Mark work as "Complete"
   - Note merge date and PR number

4. **Clean Up Remote Branch**
   - Remote branch is usually auto-deleted on merge
   - If not: `git push origin --delete feature/<task-name>`

## File Coordination

### Shared Files (Require Coordination)

These files are commonly modified and require coordination:

- `app/src/main/.../ui/screens/LandingScreen.kt`
- `app/src/main/.../ElectricSheepApplication.kt`
- `app/src/main/.../data/DataModule.kt`
- `app/build.gradle.kts`
- `build.gradle.kts`
- `gradle.properties`

### Coordination Process

1. **Check Before Modifying**
   - Read `AGENT_COORDINATION.md`
   - See if another agent is working on the file

2. **Document Your Work**
   - Add entry to coordination doc
   - List files you'll modify
   - Note your isolation strategy

3. **If Conflict Detected**
   - Coordinate with other agent
   - Decide: sequential work or split work
   - Update coordination doc with plan

## Conflict Resolution

### If Files Overlap

1. **Identify Overlap**
   - Check `AGENT_COORDINATION.md`
   - See which agent is working on the file

2. **Coordinate**
   - Document the overlap in coordination doc
   - Decide: sequential work or split work

3. **Sequential Work** (Recommended for shared files)
   - Agent 1 completes their changes
   - Agent 2 pulls latest and rebases
   - Agent 2 integrates their changes

4. **Split Work** (For large files)
   - Agent 1 handles part A
   - Agent 2 handles part B
   - Coordinate merge order

### If Merge Conflicts Occur

1. **Don't Force Merge**
   - Always resolve conflicts properly
   - Never use `--ours` or `--theirs` blindly

2. **Understand Both Changes**
   - Read both implementations
   - Understand the intent of each change

3. **Merge Thoughtfully**
   - Combine best of both approaches
   - Preserve functionality from both
   - Test thoroughly

4. **Document Resolution**
   - Note how conflict was resolved
   - Update coordination doc with lessons learned

## Branch Naming Convention

### Format
```
<type>/<task-description>
```

### Types
- `feature/` - New functionality
- `fix/` - Bug fixes
- `refactor/` - Code refactoring
- `docs/` - Documentation only
- `test/` - Test improvements

### Examples
- ✅ `feature/test-helpers`
- ✅ `feature/env-switch`
- ✅ `fix/login-bug`
- ✅ `refactor/component-cleanup`
- ✅ `docs/api-documentation`
- ❌ `feature/my-feature` (too vague)
- ❌ `test-helpers` (missing type prefix)

### Best Practices
- Use descriptive task names
- Keep names short but clear
- Use kebab-case (lowercase with hyphens)
- Avoid agent-specific identifiers (agents are ephemeral)

## Cursor Rules Enforcement

Cursor rules are configured to enforce branch isolation. See `.cursor/rules/branching.mdc` for details.

**Key Rules:**
1. Always check branch before making changes
2. Never commit to `main` branch
3. Use proper branch naming convention
4. Update coordination document before starting

## Tools and Scripts

### Coordination Check Script
```bash
./scripts/check-agent-coordination.sh
# Checks if current branch follows convention
# Warns if modifying shared files
```

### Git Worktree Helper Script
```bash
# scripts/create-worktree.sh (to be created)
# Creates a worktree for a new feature branch
```

## Best Practices

### DO ✅
- Always create feature branch before work
- Use git worktree for file system isolation when possible
- Check coordination doc before starting
- Commit and push frequently
- Sync with main regularly
- Test in isolation
- Document your changes
- Update coordination doc
- Remove worktrees after merge

### DON'T ❌
- Work directly on `main` branch
- Modify files without checking coordination
- Force push to shared branches
- Ignore merge conflicts
- Leave tests failing
- Skip coordination documentation
- Work on overlapping files without coordination
- Leave worktrees after merge

## Troubleshooting

### "I'm on main branch!"
```bash
# Immediately create feature branch
git checkout -b feature/<task-name>
# Or create worktree
git worktree add ../electric-sheep-<task-name> -b feature/<task-name>
```

### "Another agent modified my file!"
1. Check `AGENT_COORDINATION.md` for coordination
2. Pull latest changes: `git pull origin main`
3. Rebase your branch: `git rebase origin/main`
4. Resolve conflicts thoughtfully
5. Test thoroughly

### "My branch is out of date!"
```bash
git fetch origin
git rebase origin/main
# Resolve conflicts if any
git push --force-with-lease
```

### "I need to modify a shared file!"
1. Document in `AGENT_COORDINATION.md`
2. Check if other agent is actively working on it
3. Coordinate timing (sequential work)
4. Or split the work (different parts of file)
5. Consider using git worktree for complete isolation

### "File system conflicts with another agent!"
1. **Use git worktree** - Complete file system isolation
2. **Or use separate clone** - Maximum isolation
3. **Or coordinate timing** - Sequential work on shared files

## Success Metrics

A successful multi-agent workflow should have:
- ✅ No merge conflicts on `main`
- ✅ All branches follow naming convention
- ✅ Coordination doc always up-to-date
- ✅ All tests passing before merge
- ✅ Clean git history
- ✅ No duplicate work
- ✅ File system isolation when needed

## Related Documentation

- `docs/development/AGENT_COORDINATION.md` - Current work tracking
- `docs/development/MULTI_AGENT_WORKFLOW_EVALUATION.md` - Evaluation and lessons learned
- `.cursor/rules/branching.mdc` - Cursor rules for branch enforcement
- `AI_AGENT_GUIDELINES.md` - General AI agent guidelines

## Updates

### 2025-11-19: Updated for Ephemeral Agents
- Removed agent-specific ownership rules
- Added file system isolation strategies (git worktrees)
- Updated branch naming to be task-based
- Focused on coordination through coordination doc
- Added git worktree setup and cleanup instructions
