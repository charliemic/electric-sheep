# Agent Coordination Log

**Last Updated**: 2025-01-20  
**Purpose**: Track which agents are working on which files to prevent conflicts and enable communication

**How to Use:**
1. Before starting work, check this document for conflicts
2. Add your work entry with branch name and files you'll modify
3. Update status as you progress (In Progress → Complete)
4. Remove entry after merge

**Quick Check:**
```bash
./scripts/check-agent-coordination.sh
```

**Query Tool:**
```bash
# Check if a file is part of active work
./scripts/query-agent-coordination.sh check-file <file-path>

# List all active work
./scripts/query-agent-coordination.sh list-active

# Check for conflicts with multiple files
./scripts/query-agent-coordination.sh check-conflicts <file> [file...]

# Find which task owns a file
./scripts/query-agent-coordination.sh who-owns <file-path>

# Get status of a specific task
./scripts/query-agent-coordination.sh status <task-name>
```

## Communication Protocol

**Agents can communicate about their work through this document:**

### 1. Document Your Work (MANDATORY)
- ✅ **Before starting**: Add entry with task name, branch, files, and status
- ✅ **During work**: Update status and files if scope changes
- ✅ **After merge**: Mark as Complete and note PR number

### 2. Query Other Agents' Work (AVAILABLE)
- ✅ **Check file ownership**: `./scripts/query-agent-coordination.sh who-owns <file>`
- ✅ **Check for conflicts**: `./scripts/query-agent-coordination.sh check-conflicts <file>...`
- ✅ **List active work**: `./scripts/query-agent-coordination.sh list-active`
- ✅ **Get task status**: `./scripts/query-agent-coordination.sh status <task-name>`

### 3. Ask Questions (VIA COORDINATION DOC)
- ✅ **Is this file part of your work?**: Use `who-owns` command
- ✅ **Can I modify this file?**: Check conflicts, then document your work
- ✅ **What's the status of task X?**: Use `status` command
- ✅ **What files are you modifying?**: Check task entry in this doc

### 4. Coordinate Conflicts (VIA COORDINATION DOC)
- ✅ **If conflict detected**: Document in task entry under "Conflicts" section
- ✅ **Resolution strategy**: Document how conflict will be resolved
- ✅ **Sequential work**: Document which task goes first
- ✅ **Split work**: Document which parts each task handles

### 5. Communication Best Practices
- ✅ **Check before modifying**: Always query before modifying shared files
- ✅ **Update promptly**: Update coordination doc when scope changes
- ✅ **Document decisions**: Record conflict resolutions in task entry
- ✅ **Use worktree**: Always use git worktree for file system isolation

**Query Tool:**
```bash
# Check if a file is part of active work
./scripts/query-agent-coordination.sh check-file <file-path>

# List all active work
./scripts/query-agent-coordination.sh list-active

# Check for conflicts with multiple files
./scripts/query-agent-coordination.sh check-conflicts <file> [file...]

# Find which task owns a file
./scripts/query-agent-coordination.sh who-owns <file-path>

# Get status of a specific task
./scripts/query-agent-coordination.sh status <task-name>
```

## Communication Protocol

**Agents can communicate about their work through this document:**

### 1. Document Your Work (MANDATORY)
- ✅ **Before starting**: Add entry with task name, branch, files, and status
- ✅ **During work**: Update status and files if scope changes
- ✅ **After merge**: Mark as Complete and note PR number

### 2. Query Other Agents' Work (AVAILABLE)
- ✅ **Check file ownership**: `./scripts/query-agent-coordination.sh who-owns <file>`
- ✅ **Check for conflicts**: `./scripts/query-agent-coordination.sh check-conflicts <file>...`
- ✅ **List active work**: `./scripts/query-agent-coordination.sh list-active`
- ✅ **Get task status**: `./scripts/query-agent-coordination.sh status <task-name>`

### 3. Ask Questions (VIA COORDINATION DOC)
- ✅ **Is this file part of your work?**: Use `who-owns` command
- ✅ **Can I modify this file?**: Check conflicts, then document your work
- ✅ **What's the status of task X?**: Use `status` command
- ✅ **What files are you modifying?**: Check task entry in this doc

### 4. Coordinate Conflicts (VIA COORDINATION DOC)
- ✅ **If conflict detected**: Document in task entry under "Conflicts" section
- ✅ **Resolution strategy**: Document how conflict will be resolved
- ✅ **Sequential work**: Document which task goes first
- ✅ **Split work**: Document which parts each task handles

### 5. Communication Best Practices
- ✅ **Check before modifying**: Always query before modifying shared files
- ✅ **Update promptly**: Update coordination doc when scope changes
- ✅ **Document decisions**: Record conflict resolutions in task entry
- ✅ **Use worktree**: Always use git worktree for file system isolation

## Current Work Status

**Note:** Agents are ephemeral - entries track tasks, not specific agents.

### Example Entry Format:
```
### Task: <task-name>
- **Role**: [PLANNING] / [EXECUTION] / [VERIFICATION] (optional, for phase-based work)
- **Branch**: `feature/<task-name>`
- **Worktree**: `../electric-sheep-<task-name>` (if using worktree)
- **Status**: In Progress / Complete
- **Files Modified**: List of files
- **Isolation Strategy**: worktree / clone / branch
- **Conflicts**: None / List conflicts
- **ETA**: Date or status
```

**Role Tags (PRIORITY 1 ENHANCEMENT):**
- `[PLANNING]` - Task is in planning/design phase
- `[EXECUTION]` - Task is in implementation phase
- `[VERIFICATION]` - Task is in testing/verification phase
- **Purpose**: Prevents duplicate work in different phases (e.g., two agents planning the same feature)

## Active Work

### Task: Dynamic Metrics Dashboard Implementation
- **Role**: [EXECUTION]
- **Branch**: `experimental/onboarding-validation-issue-52`
- **Status**: Complete
- **Files Modified**: 
  - `scripts/metrics/dashboard-server-fastify.js` (Fastify-based dashboard server)
  - `scripts/metrics/package.json` (Node.js dependencies)
  - `scripts/metrics/nodemon.json` (hot reloading config)
  - `scripts/metrics/start-dashboard-dev.sh` (development server script)
  - `development-metrics/README.md` (updated documentation)
  - Framework evaluation docs (FRAMEWORK_CHOICE.md, FRAMEWORK_COMPARISON.md, FRONTEND_EVALUATION.md)
- **Key Features**:
  - Multi-source agent detection (worktrees, coordination doc, metrics sessions, recent prompts)
  - Fixed agent count discrepancy (was counting all branches, now only active)
  - Hot reloading with Nodemon for development
  - No-scroll UX with card-based layout
  - Auto-refresh every 5 seconds
- **Completed**: 2025-01-20

### Task: App Specificity Analysis
- **Branch**: `feature/app-specificity-analysis`
- **Worktree**: `../electric-sheep-app-specificity-analysis` ✅ **Using worktree for isolation**
- **Status**: Complete ✅
- **Files Created**: 
  - `docs/development/analysis/APP_SPECIFICITY_ANALYSIS.md` (comprehensive analysis)
  - `docs/development/analysis/TEMPLATE_EXTRACTION_AND_TICKETING.md` (extraction strategy)
  - `docs/development/analysis/GITHUB_SUB_ISSUES_AND_TEMPLATES.md` (GitHub features & templates)
  - `docs/development/analysis/RELATIONSHIPS_QUICK_REFERENCE.md` (quick reference)
  - `.github/ISSUE_TEMPLATE/epic.md` (epic template)
  - `.github/ISSUE_TEMPLATE/extraction.md` (extraction template - Markdown)
  - `.github/ISSUE_TEMPLATE/extraction-form.yml` (extraction template - YAML form)
- **Isolation Strategy**: ✅ **Git worktree** - Complete file system isolation
- **Conflicts**: None - working in isolated worktree, analysis only (no code changes)
- **Purpose**: Identify which parts of the app are specific vs generalizable, and team vs individual-oriented
- **Completed**: 2025-01-20

### Task: Emulator Management Architecture
- **Branch**: `feature/emulator-management-architecture`
- **Worktree**: `../electric-sheep-emulator-management` ✅ **Using worktree for isolation**
- **Status**: Complete ✅
- **Files Modified**: 
  - `docs/development/EMULATOR_MANAGEMENT_ARCHITECTURE.md` (architecture design)
  - `docs/development/EMULATOR_SCRIPT_REVIEW.md` (script review and migration plan)
  - `docs/development/EMULATOR_MANAGEMENT_IMPLEMENTATION.md` (implementation details)
  - `docs/development/EMULATOR_MANAGEMENT_TESTING.md` (testing guide)
  - `docs/development/EMULATOR_MANAGEMENT_ONBOARDING.md` (onboarding guide)
  - `docs/development/CI_CD_EMULATOR_TESTS.md` (CI/CD integration)
  - `scripts/emulator-lock-manager.sh` (lock management)
  - `scripts/emulator-discovery.sh` (discovery service)
  - `scripts/emulator-manager.sh` (enhanced with locking)
  - `scripts/dev-reload.sh` (updated to use discovery)
  - `scripts/run-persona-test-with-video.sh` (updated to use discovery)
  - `test-automation/src/main/kotlin/.../Main.kt` (updated to use discovery)
  - `scripts/tests/**` (test suite with Bats)
  - `.github/workflows/build-and-test.yml` (CI/CD integration)
  - `Makefile` (convenience commands)
  - `README.md` (updated with testing section)
- **Isolation Strategy**: ✅ **Git worktree** - Complete file system isolation
- **Conflicts**: None - working in isolated worktree
- **Purpose**: Design and implement emulator management architecture to prevent multi-agent conflicts
- **Merged**: Pending PR

### Task: Feature Flag Sync Script Improvements
- **Branch**: `fix/feature-flag-sync-upsert-isolated`
- **Worktree**: `../electric-sheep-feature-flag-sync` ✅ **Using worktree for isolation**
- **Status**: In Progress
- **Files Modified**: 
  - `scripts/sync-feature-flags.sh` (upsert handling improvements)
  - `.github/workflows/supabase-feature-flags-deploy.yml` (DB connection support)
  - `feature-flags/flags.yaml` (enable_trivia_app flag)
  - `app/build.gradle.kts` (BuildConfig field)
- **Isolation Strategy**: ✅ **Git worktree** - Complete file system isolation
- **Conflicts**: None - working in isolated worktree
- **Purpose**: Fix feature flag sync script to properly handle upserts and verify enable_trivia_app flag deployment

## Shared Files (Require Coordination)

These files are commonly modified and require coordination:

- ⚠️ `app/src/main/.../ui/screens/LandingScreen.kt`
- ⚠️ `app/src/main/.../ElectricSheepApplication.kt`
- ⚠️ `app/src/main/.../data/DataModule.kt`
- ⚠️ `app/build.gradle.kts`
- ⚠️ `build.gradle.kts`
- ⚠️ `gradle.properties`

**Recommendation:** Use git worktree when modifying shared files for complete file system isolation.

## Conflict Resolution

**When conflicts are detected:**

1. **Check this document** - See if another task is working on the same file
2. **Coordinate** - Document the overlap and decide on approach:
   - Sequential work (one task completes, then next)
   - Split work (different parts of file)
   - Use git worktree for complete isolation
3. **Update entry** - Document resolution in task entry

## Workflow Guidelines

### Before Starting Work
1. Check this document for file conflicts
2. Create feature branch: `feature/<task-name>`
3. **Consider git worktree** for file system isolation: `./scripts/create-worktree.sh <task-name>`
4. Pull latest `main`: `git pull origin main`
5. Add entry to this document with your task details
6. Document which files you'll modify

### During Work
1. Commit frequently (at least daily)
2. Push branch regularly (backup)
3. Update this document if scope changes
4. Test changes in isolation

### Before Creating PR
1. **Run pre-PR check (MANDATORY):**
   ```bash
   ./scripts/pre-pr-check.sh
   ```
   This checks: branch sync, conflicts, tests, coordination doc

2. Rebase on latest `main`: `git rebase origin/main`
3. Resolve any conflicts
4. Run tests: `./gradlew test`
5. Update documentation
6. Create PR with clear description
7. Update this document to "Complete"

### After Merging
1. Remove worktree (if used): `git worktree remove ../electric-sheep-<task-name>`
2. Delete feature branch: `git branch -d feature/<task-name>`
3. Update this document
4. Pull latest `main` before next work

## Notes

### 2025-11-19: Multi-Agent Conflict Resolution
- Three agents worked simultaneously on overlapping features
- Environment switching merged successfully
- Design components and test helpers are local only
- Need to commit local improvements and verify no conflicts
- Implemented coordination workflow to prevent future conflicts

### 2025-11-20: Restore Design Work Complete
- **Task**: Restore UI navigation improvements (user info in top bar, icons)
- **Branch**: `feature/restore-design-work`
- **Status**: ✅ Complete - Ready for merge
- **Changes**:
  - User email + Person icon in TopAppBar
  - Logout changed to IconButton with ExitToApp icon
  - User info card removed from content area
  - Worktree rules expanded with comprehensive patterns
- **Collision Resolution**: Resolved collision with mood visualization agent by preserving their MoodChart component and removing their Load test data button

### 2025-01-20: Local Setup Cleanup
- **Task**: Tidy up local branches, worktrees, and untracked files
- **Branch**: `feature/tidy-up-local-changes`
- **Status**: ✅ Complete
- **Actions Taken**:
  - Cleaned up merged worktrees: `electric-sheep-mood-chart`, `electric-sheep-fix-moodchart`
  - Pruned stale remote branches
  - Deleted empty/redundant branches: `fix/add-trivia-flag`, `fix/feature-flag-sync-upsert-isolated`
  - Created tidy-up branch with 92 files organized (test fixtures, mood chart components, Supabase scripts, documentation)
  - Created review documents: `LOCAL_SETUP_REVIEW.md`, `UNMERGED_BRANCHES_REVIEW.md`
- **Remaining Unmerged Branches** (need review):
  - `feature/trivia-app-initial-setup` - Has unique commit
  - `feature/trivia-screen` - Has unique commits
  - `feature/improve-cursor-rules` - Has unique commits
  - `feature/video-annotation-system` - Needs review
  - `feature/emulator-setup` - PR merged, may have merge artifacts only

### Next Coordination Check
- **Date**: 2025-01-20
- **Action**: Review unmerged branches and determine if work should be merged or abandoned
- **Priority**: Medium - Clean up remaining unmerged branches

