# Multi-Agent Workflow Guidelines

**Last Updated**: 2025-11-19  
**Status**: Active Guidelines

## Overview

This document defines the standard workflow for multiple AI agents working simultaneously on this codebase. Following these guidelines prevents conflicts, ensures code quality, and maintains a clean git history.

## Core Principles

### 1. Branch Isolation (MANDATORY)
- **Never work on `main` branch** - Always create a feature branch
- **One agent per branch** - Each agent works on their own feature branch
- **Branch naming**: `feature/<agent-id>-<feature-name>` or `feature/<agent-id>-<short-description>`
- **Example**: `feature/agent-1-test-helpers`, `feature/agent-2-env-switch`, `feature/agent-3-design-system`

### 2. File Ownership
- Each agent has primary ownership of specific directories
- Shared files require coordination (see [File Ownership](#file-ownership))
- Document file changes in `AGENT_COORDINATION.md` before starting

### 3. Coordination Before Conflict
- Check `AGENT_COORDINATION.md` before starting work
- Document which files you'll modify
- Coordinate if files overlap with other agents' work

### 4. Regular Synchronization
- Pull from `main` daily: `git pull origin main`
- Rebase feature branch on `main` before major work
- Resolve conflicts immediately, not at merge time

## Workflow Steps

### Phase 1: Pre-Work Setup (REQUIRED)

**Before making ANY changes:**

1. **Check Current Branch**
   ```bash
   git status
   # Must NOT be on 'main'
   ```

2. **Pull Latest Main**
   ```bash
   git checkout main
   git pull origin main
   ```

3. **Create Feature Branch**
   ```bash
   git checkout -b feature/<agent-id>-<feature-name>
   # Example: git checkout -b feature/agent-1-test-helpers
   ```

4. **Check Coordination Document**
   - Read `docs/development/AGENT_COORDINATION.md`
   - Verify no conflicts with other agents' work
   - Document your planned changes

5. **Update Coordination Document**
   - Add your work entry to `AGENT_COORDINATION.md`
   - List files you'll modify
   - Set status to "In Progress"

### Phase 2: During Work

**While implementing changes:**

1. **Commit Frequently**
   - Commit at least daily
   - Use descriptive commit messages
   - Keep commits atomic (one logical change)

2. **Push Regularly**
   ```bash
   git push -u origin feature/<agent-id>-<feature-name>
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
   git checkout main
   git pull origin main
   git checkout feature/<agent-id>-<feature-name>
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

1. **Delete Local Branch**
   ```bash
   git checkout main
   git pull origin main
   git branch -d feature/<agent-id>-<feature-name>
   ```

2. **Update Coordination Document**
   - Mark work as "Complete"
   - Note merge date and PR number

3. **Notify Other Agents** (if applicable)
   - Document any shared file changes
   - Note any breaking changes

4. **Clean Up Remote Branch**
   - Remote branch is usually auto-deleted on merge
   - If not: `git push origin --delete feature/<agent-id>-<feature-name>`

## File Ownership

### Agent 1: Test Framework & Automation
**Primary Ownership:**
- `scripts/test-*.sh`
- `test-automation/`
- `docs/testing/`
- `test-scenarios/`

**Shared (Requires Coordination):**
- `app/src/test/` (if modifying app tests)
- `docs/` (if creating new test docs)

### Agent 2: Environment & Configuration
**Primary Ownership:**
- `app/src/main/.../config/`
- `docs/development/` (environment-related)
- `gradle.properties` (environment config)

**Shared (Requires Coordination):**
- `app/src/main/.../ElectricSheepApplication.kt`
- `app/src/main/.../data/DataModule.kt`
- `app/src/main/.../ui/screens/` (if adding env indicators)

### Agent 3: Design System & UI
**Primary Ownership:**
- `app/src/main/.../ui/components/`
- `app/src/main/.../ui/theme/`
- `docs/architecture/` (design-related)
- `app/src/main/res/` (design assets)

**Shared (Requires Coordination):**
- `app/src/main/.../ui/screens/` (all screen files)
- `docs/architecture/` (if other agents add architecture docs)

### Agent 4: Data Layer & Business Logic
**Primary Ownership:**
- `app/src/main/.../data/repositories/`
- `app/src/main/.../data/models/`
- `app/src/main/.../data/local/`
- `app/src/main/.../data/remote/`

**Shared (Requires Coordination):**
- `app/src/main/.../data/DataModule.kt`
- `app/src/main/.../ui/screens/` (if modifying ViewModels)

### Shared Files (ALWAYS Require Coordination)
- `app/src/main/.../ui/screens/LandingScreen.kt`
- `app/src/main/.../ElectricSheepApplication.kt`
- `app/src/main/.../data/DataModule.kt`
- `app/build.gradle.kts`
- `build.gradle.kts`
- `gradle.properties`

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
<type>/<agent-id>-<short-description>
```

### Types
- `feature/` - New functionality
- `fix/` - Bug fixes
- `refactor/` - Code refactoring
- `docs/` - Documentation only
- `test/` - Test improvements

### Agent IDs
- `agent-1` - Test Framework
- `agent-2` - Environment/Config
- `agent-3` - Design/UI
- `agent-4` - Data Layer
- Or use descriptive names: `test-framework`, `env-config`, `design-system`, `data-layer`

### Examples
- ✅ `feature/agent-1-test-helpers`
- ✅ `feature/test-framework-async-fixes`
- ✅ `fix/agent-2-env-switch-bug`
- ✅ `refactor/agent-3-component-cleanup`
- ❌ `feature/my-feature` (missing agent ID)
- ❌ `test-helpers` (missing type prefix)

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
# scripts/check-agent-coordination.sh
# Checks if current branch follows convention
# Warns if modifying shared files
```

### Branch Validation
```bash
# scripts/validate-branch.sh
# Validates branch name and current state
# Ensures not on main branch
```

## Best Practices

### DO ✅
- Always create feature branch before work
- Check coordination doc before starting
- Commit and push frequently
- Sync with main regularly
- Test in isolation
- Document your changes
- Update coordination doc

### DON'T ❌
- Work directly on `main` branch
- Modify files without checking ownership
- Force push to shared branches
- Ignore merge conflicts
- Leave tests failing
- Skip coordination documentation
- Work on overlapping files without coordination

## Troubleshooting

### "I'm on main branch!"
```bash
# Immediately create feature branch
git checkout -b feature/<agent-id>-<feature-name>
# Stash any changes if needed
git stash
git checkout -b feature/<agent-id>-<feature-name>
git stash pop
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

## Success Metrics

A successful multi-agent workflow should have:
- ✅ No merge conflicts on `main`
- ✅ All branches follow naming convention
- ✅ Coordination doc always up-to-date
- ✅ All tests passing before merge
- ✅ Clean git history
- ✅ No duplicate work
- ✅ Clear ownership boundaries

## Related Documentation

- `docs/development/AGENT_COORDINATION.md` - Current work tracking
- `docs/development/MULTI_AGENT_WORKFLOW_EVALUATION.md` - Evaluation and lessons learned
- `.cursor/rules/branching.mdc` - Cursor rules for branch enforcement
- `AI_AGENT_GUIDELINES.md` - General AI agent guidelines

## Updates

### 2025-11-19: Initial Guidelines
- Created comprehensive multi-agent workflow
- Defined file ownership rules
- Established coordination process
- Added conflict resolution strategies

