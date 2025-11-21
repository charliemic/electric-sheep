# Cursor Agent Workflow Pattern

**Date**: 2025-11-21  
**Purpose**: Define a practical pattern for using Cursor's GUI with multiple agents

## Your Actual Workflow

1. **Hit "new agent"** in Cursor GUI
2. **Start prompting** - Agent begins work
3. **At some point, decide that's finished** - Agent work complete
4. **Hit archive** - Agent finished
5. **Hit new agent, repeat** - New agent starts
6. **Multiple agents running simultaneously** - Parallel work

## Goals

- ✅ Use Cursor effectively
- ✅ Ensure right handovers and isolation between agents
- ✅ Keep local setup clean
- ✅ Keep remote setup clean

## Proposed Pattern

### Pattern Overview

**One Agent = One Branch = One Task**

- Each new agent gets its own feature branch
- Agents work in isolation on their branches
- Archive agent = Work committed, branch ready for PR/merge
- Handover = New agent picks up from handover queue

### Workflow Steps

#### Step 1: New Agent Starts

**When you hit "new agent" in Cursor:**

1. **Check current state:**
   ```bash
   ./scripts/pre-work-check.sh
   ```
   - Verifies not on main
   - Checks for pending handovers
   - Identifies available work

2. **Pick up handover OR start new work:**
   - **If handover exists**: Read handover, create branch from handover point
   - **If new work**: Create new feature branch

3. **Create feature branch:**
   ```bash
   git checkout main
   git pull origin main
   git checkout -b feature/<task-description>
   ```
   - Or use worktree for file system isolation:
   ```bash
   git worktree add ../electric-sheep-<task-name> -b feature/<task-name>
   cd ../electric-sheep-<task-name>
   ```

4. **Document in coordination:**
   - Update `docs/development/workflow/AGENT_COORDINATION.md`
   - Mark as "In Progress"

#### Step 2: Agent Works

**During prompting:**

- ✅ Commit frequently (every 15-30 minutes)
- ✅ Push branch regularly (backup)
- ✅ Check coordination before shared files
- ✅ Monitor effectiveness (if long session)

#### Step 3: Decide Agent is Finished

**When you decide agent is finished:**

**Option A: Work Complete (No Handover)**
- ✅ All work committed
- ✅ Tests passing
- ✅ Documentation updated
- ✅ Create/update PR
- ✅ **Verify clean state** (see Step 4)
- ✅ Archive agent
- ✅ **Branch ready for merge** (you'll merge later)

**Option B: Work Incomplete (Handover Needed)**
- ✅ All current work committed
- ✅ Create handover document
- ✅ Add to handover queue
- ✅ **Verify clean state** (see Step 4)
- ✅ Archive agent
- ✅ **New agent will pick up from handover**

#### Step 4: Verify Clean State (REQUIRED BEFORE ARCHIVE)

**CRITICAL: Do NOT archive until state is clean!**

**Run this verification checklist:**

1. **Check working directory:**
   ```bash
   git status
   ```
   - ✅ Should show "nothing to commit, working tree clean"
   - ❌ If uncommitted changes: Commit them first

2. **Check branch is pushed:**
   ```bash
   git log origin/feature/<task-name>..HEAD
   ```
   - ✅ Should show no commits (all pushed)
   - ❌ If unpushed commits: `git push origin feature/<task-name>`

3. **Check for untracked files:**
   ```bash
   git status --porcelain
   ```
   - ✅ Should show no untracked files (or intentionally ignored)
   - ❌ If untracked files: Add to .gitignore or commit them

4. **Verify coordination doc updated:**
   - ✅ Status marked "Complete" or "Handover Created"
   - ✅ PR number noted (if created)
   - ✅ Handover added to queue (if needed)

5. **Final verification:**
   ```bash
   # Run clean state verification script
   ./scripts/verify-clean-state.sh
   ```
   - ✅ Should show "State is clean - safe to archive"
   - ❌ If errors: Fix them before archiving

**Only after ALL checks pass → Archive in Cursor**

#### Step 5: Archive Agent

**When state is clean, hit "archive":**

1. ✅ **State verified clean** (Step 4 complete)
2. ✅ **All work committed and pushed**
3. ✅ **Coordination doc updated**
4. ✅ **PR created** (if work complete)
5. ✅ **Handover created** (if work incomplete)
6. ✅ **Archive in Cursor** - Agent finished

#### Step 5: Cleanup (Periodic)

**When PR is merged:**

1. **Run cleanup script:**
   ```bash
   ./scripts/post-merge-cleanup.sh <pr-number>
   ```

2. **Or manually:**
   ```bash
   git checkout main
   git pull origin main
   git branch -d feature/<task-name>
   git worktree remove ../electric-sheep-<task-name>  # if used
   ```

3. **Update coordination:**
   - Remove entry or mark as "Merged"

## Isolation Strategy

### For Multiple Simultaneous Agents

**Option 1: Git Worktrees (RECOMMENDED)**
- Each agent gets its own worktree directory
- Complete file system isolation
- No conflicts possible
- Easy cleanup

**Option 2: Feature Branches**
- Each agent on its own branch
- Requires coordination for shared files
- Simpler but less isolated

**Recommendation:**
- Use worktrees when modifying shared files
- Use branches when working on isolated features

## Handover Pattern

### When to Create Handover

**Create handover when:**
- Work is incomplete but agent is finished
- Next agent needs context
- Work spans multiple agent sessions

### Handover Process

1. **Agent creates handover:**
   ```bash
   ./scripts/create-handover.sh <task-name>
   ```

2. **Add to queue:**
   - Updates `docs/development/workflow/HANDOVER_QUEUE.md`
   - Sets status to "pending"

3. **Archive agent** - Agent finished

4. **New agent picks up:**
   - Checks handover queue in pre-work-check
   - Reads handover document
   - Continues from handover point

## Cleanup Pattern

### Branch Cleanup

**After PR merge:**
- Delete merged branch
- Remove worktree (if used)
- Update coordination doc

**For abandoned work:**
- Review branch after 30 days
- Delete if not needed
- Document decision

### Regular Maintenance

**Weekly:**
- Review unmerged branches
- Clean up merged branches
- Prune stale worktrees

## Practical Example

### Scenario: Feature Implementation

**Agent 1:**
1. New agent → Create `feature/user-auth`
2. Work → Implement auth logic (multiple prompts)
3. Decide finished → Work complete, create PR
4. Archive → Agent finished, PR ready

**Agent 2 (simultaneous):**
1. New agent → Create `feature/dashboard`
2. Work → Build dashboard UI
3. Decide finished → Work incomplete, create handover
4. Archive → Agent finished, handover created

**Agent 3 (picks up handover):**
1. New agent → Check handover queue
2. Pick up → Read dashboard handover
3. Work → Complete dashboard
4. Decide finished → Work complete, create PR
5. Archive → Agent finished, PR ready

**Later:**
- Merge PRs
- Run cleanup scripts
- Delete branches
- Remove worktrees

## Benefits

- ✅ **Clear isolation** - Each agent on own branch/worktree
- ✅ **Easy handovers** - Queue system for continuation
- ✅ **Clean repository** - Regular cleanup keeps things tidy
- ✅ **Works with Cursor GUI** - Maps to "new agent" / "archive" workflow
- ✅ **Multiple agents** - Can run simultaneously safely

## Checklist for Each Agent

### When Starting (New Agent)
- [ ] Run `./scripts/pre-work-check.sh`
- [ ] Check handover queue
- [ ] Create feature branch (or worktree)
- [ ] Update coordination doc

### During Work
- [ ] Commit frequently
- [ ] Push branch regularly
- [ ] Check coordination before shared files

### When Finishing (Archive)
- [ ] All work committed
- [ ] Branch pushed
- [ ] PR created (if work complete)
- [ ] Handover created (if work incomplete)
- [ ] Coordination doc updated
- [ ] **Verify clean state** (git status, all checks pass)
- [ ] **Archive in Cursor** (only after state is clean)

### After Merge (Cleanup)
- [ ] Run cleanup script
- [ ] Verify branch deleted
- [ ] Update coordination doc

## Scripts to Use

- `./scripts/pre-work-check.sh` - Before starting
- `./scripts/verify-clean-state.sh` - **Before archiving (REQUIRED)**
- `./scripts/create-handover.sh` - When creating handover
- `./scripts/post-merge-cleanup.sh` - After PR merge
- `./scripts/check-agent-coordination.sh` - Check conflicts

## Questions to Consider

1. **When should agents use worktrees vs branches?**
   - Worktrees for shared files
   - Branches for isolated features

2. **How long should unmerged branches live?**
   - Review after 30 days
   - Delete if abandoned

3. **When to create handover vs just archive?**
   - Handover if work incomplete
   - Just archive if work complete

---

**This pattern maps directly to your Cursor GUI workflow and ensures clean isolation and handovers.**

