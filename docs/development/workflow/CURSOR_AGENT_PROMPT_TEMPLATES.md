# Cursor Agent Prompt Templates

**Purpose**: Prompt templates for using Cursor's agent features (Composer/Chat) at key workflow stages.

## Overview

This guide provides ready-to-use prompts for common workflow scenarios where agent assistance and user input are needed.

## Table of Contents

1. [Starting Work](#1-starting-work)
2. [During Work](#2-during-work)
3. [Before PR](#3-before-pr)
4. [Creating PR](#4-creating-pr)
5. [After PR Merge](#5-after-pr-merge)
6. [Error Handling](#6-error-handling)
7. [Session End](#7-session-end)

---

## 1. Starting Work

### 1.1 Pre-Work Check

**When to use**: Before starting any new work or task.

**Prompt:**
```
I'm about to start work on [task description]. Please help me:

1. Check current git state (branch, uncommitted changes)
2. Verify I'm not on main branch (create feature branch if needed)
3. Check if main is up to date (pull latest if needed)
4. Check for coordination conflicts (shared files, other active work)
5. Discover relevant cursor rules for this task
6. Show me a summary of what needs to be done before I start

At each stage, show me a clear summary and wait for my approval before proceeding.
```

**Expected Agent Response:**
- Current branch status
- Whether main needs updating
- Coordination conflicts (if any)
- Relevant rules discovered
- Pre-work checklist status

### 1.2 Create Feature Branch

**When to use**: When you need to create a new feature branch.

**Prompt:**
```
I need to create a feature branch for [task description]. Please:

1. Check current branch (must not be main)
2. Pull latest main
3. Create branch: [type]/[task-name] (e.g., feature/test-helpers)
4. Show me the branch name and confirm it follows naming convention
5. Optionally create a git worktree for file system isolation (ask me first)

Show me a summary before creating the branch.
```

**Expected Agent Response:**
- Current branch status
- Latest main pulled
- Proposed branch name
- Worktree option (if applicable)
- Confirmation before creating

### 1.3 Check Coordination

**When to use**: Before modifying shared files or when starting work.

**Prompt:**
```
I'm about to modify [list of files]. Please check for coordination conflicts:

1. Check AGENT_COORDINATION.md for other active work
2. Identify if any files I'm modifying are shared/high-risk
3. Check if other branches are working on the same files
4. Recommend isolation strategy (worktree vs branch)
5. Show me a summary of conflicts (if any) and recommendations

Wait for my approval before proceeding with modifications.
```

**Expected Agent Response:**
- Coordination status
- Shared file conflicts (if any)
- Isolation recommendations
- Risk assessment

---

## 2. During Work

### 2.1 Frequent Commit

**When to use**: After completing a logical unit of work (every 15-30 minutes).

**Prompt:**
```
I've completed [brief description of work]. Please help me commit:

1. Show me what files have changed
2. Generate an appropriate commit message
3. Run tests first (if applicable)
4. Show me a summary: "Commit [description] to branch [branch-name]"
5. Wait for my approval before committing

Use WIP prefix if work is incomplete.
```

**Expected Agent Response:**
- Changed files summary
- Generated commit message
- Test results (if applicable)
- Summary before committing
- Approval prompt

### 2.2 Push Branch

**When to use**: After committing, to back up work or trigger CI.

**Prompt:**
```
I want to push my branch to remote. Please:

1. Show me what commits will be pushed
2. Show me a summary: "Push branch [branch-name] to remote"
3. List the commits that will be pushed
4. Wait for my approval before pushing

If this is the first push, set upstream tracking.
```

**Expected Agent Response:**
- Commits to push
- Branch name
- Upstream tracking status
- Summary before pushing
- Approval prompt

### 2.3 Sync with Main

**When to use**: When main has updates or before creating PR.

**Prompt:**
```
I need to sync my branch with main. Please:

1. Fetch latest from origin
2. Show me what commits are in main that I don't have
3. Rebase my branch on main (preferred) or merge
4. Show me a summary: "Sync branch [branch-name] with main"
5. Wait for my approval before syncing

If conflicts occur, help me resolve them step by step.
```

**Expected Agent Response:**
- Commits in main not in branch
- Rebase vs merge recommendation
- Summary before syncing
- Conflict resolution help (if needed)
- Approval prompt

### 2.4 Run Tests

**When to use**: Before committing or when tests might be affected.

**Prompt:**
```
Please run the test suite and show me:

1. Test execution status
2. Any failing tests (with details)
3. Test coverage summary (if available)
4. Recommendations for fixing failures

If tests fail, help me understand what went wrong.
```

**Expected Agent Response:**
- Test results
- Failures with details
- Coverage summary
- Fix recommendations

---

## 3. Before PR

### 3.1 Final Sync and Test

**When to use**: Before creating PR, to ensure everything is ready.

**Prompt:**
```
I'm ready to create a PR. Please help me prepare:

1. Sync branch with latest main (rebase preferred)
2. Resolve any conflicts (help me step by step)
3. Run full test suite
4. Check for lint errors
5. Verify branch is up to date
6. Show me a summary of readiness status

Wait for my approval before proceeding to PR creation.
```

**Expected Agent Response:**
- Sync status
- Conflict resolution (if needed)
- Test results
- Lint status
- Readiness summary
- Approval prompt

### 3.2 Review Changes

**When to use**: Before creating PR, to review what will be included.

**Prompt:**
```
Please show me a summary of all changes that will be in the PR:

1. List all files changed
2. Show commit history (commits since main)
3. Highlight any breaking changes
4. Identify any shared files modified
5. Show me a summary: "PR will include [X] commits, [Y] files changed"

Help me identify anything that needs attention before PR creation.
```

**Expected Agent Response:**
- Files changed summary
- Commit history
- Breaking changes (if any)
- Shared files (if any)
- Pre-PR checklist

---

## 4. Creating PR

### 4.1 Create Pull Request

**When to use**: When ready to create a PR.

**Prompt:**
```
I'm ready to create a PR. Please help me:

1. Check if PR already exists for this branch
2. Generate PR title from commits
3. Generate PR description from commits (include changes, testing, notes)
4. Show me the PR title and description
5. Wait for my approval before creating
6. Create PR as draft (so I can edit if needed)

Show me a summary: "Create PR from [branch] to main"
```

**Expected Agent Response:**
- Existing PR check
- Generated title
- Generated description
- Summary before creating
- Approval prompt
- PR link after creation

### 4.2 Check PR Readiness

**When to use**: After PR creation or when checking PR status.

**Prompt:**
```
Please check the readiness of PR #[number] (or current branch):

1. Check CI status (passing/failing/running)
2. Check for merge conflicts
3. Check if branch is up to date with main
4. Check review status (if applicable)
5. Show me a summary: "PR #[number] readiness: [status]"

Tell me what needs to happen before it can be merged.
```

**Expected Agent Response:**
- CI status
- Merge conflicts
- Branch sync status
- Review status
- Readiness summary
- Next steps

---

## 5. After PR Merge

### 5.1 Post-Merge Cleanup

**When to use**: After PR is merged.

**Prompt:**
```
PR #[number] has been merged. Please help me clean up:

1. Check for other active work (worktrees, branches)
2. Switch to main and pull latest
3. Identify merged branches to delete
4. Identify worktrees to remove
5. Show me a summary: "Cleanup after PR #[number] merge"
6. Wait for my approval before deleting anything

Only delete branches/worktrees that are confirmed merged.
```

**Expected Agent Response:**
- Active work status
- Merged branches list
- Worktrees to remove
- Summary before cleanup
- Approval prompt
- Cleanup confirmation

---

## 6. Error Handling

### 6.1 Test Failures

**When to use**: When tests fail.

**Prompt:**
```
Tests are failing. Please help me:

1. Show me the failing tests with error messages
2. Identify the root cause
3. Suggest fixes
4. Help me implement the fixes
5. Re-run tests after fixes

Show me a summary of what failed and how to fix it.
```

**Expected Agent Response:**
- Failing test details
- Root cause analysis
- Fix suggestions
- Implementation help
- Test re-run results

### 6.2 Merge Conflicts

**When to use**: When merge/rebase conflicts occur.

**Prompt:**
```
I have merge conflicts. Please help me:

1. Show me which files have conflicts
2. Explain what each conflict is about
3. Help me resolve each conflict step by step
4. Show me a summary: "Resolve conflicts in [files]"
5. Verify resolution is correct

Guide me through resolving each conflict.
```

**Expected Agent Response:**
- Conflicted files
- Conflict explanations
- Resolution guidance
- Step-by-step help
- Verification

### 6.3 Build Errors

**When to use**: When build fails.

**Prompt:**
```
The build is failing. Please help me:

1. Show me the build error messages
2. Identify the root cause
3. Suggest fixes
4. Help me implement the fixes
5. Re-run build after fixes

Show me a summary of what failed and how to fix it.
```

**Expected Agent Response:**
- Build error details
- Root cause analysis
- Fix suggestions
- Implementation help
- Build re-run results

### 6.4 CI Failures

**When to use**: When CI checks fail in PR.

**Prompt:**
```
CI is failing for PR #[number]. Please help me:

1. Show me which CI checks are failing
2. Get the error logs from CI
3. Identify the root cause
4. Suggest fixes
5. Help me implement the fixes locally
6. Verify fixes work before pushing

Show me a summary of CI failures and how to fix them.
```

**Expected Agent Response:**
- Failing CI checks
- Error logs
- Root cause analysis
- Fix suggestions
- Implementation help
- Local verification

---

## 7. Session End

### 7.1 Complete Session End Workflow

**When to use**: When ready to end a session.

**Prompt:**
```
I'm ready to end this session. Please help me:

1. Check current state (branch, uncommitted changes)
2. Commit changes (if any) - show me a summary first, then prompt for approval
3. Push branch (if needed) - show me what will be pushed, then prompt for approval
4. Create PR (if needed) - generate PR description, show me, then prompt for approval
5. Check PR readiness (CI status, conflicts) - show me the status
6. Provide next steps summary

At each stage, show me a clear summary of what will happen and wait for my approval before proceeding.
```

**Expected Agent Response:**
- Current state summary
- Commit summary (if needed)
- Push summary (if needed)
- PR creation summary (if needed)
- PR readiness status
- Next steps summary

---

## Prompt Template Structure

All prompts follow this structure:

1. **Context**: What you're trying to do
2. **Steps**: Numbered list of what the agent should do
3. **Summary Request**: Ask for summary before actions
4. **Approval Request**: Ask for approval before proceeding

## Summary Format

All summaries should follow this format:

```
📋 SUMMARY: [Action Description]
   • Detail 1: [value]
   • Detail 2: [value]
   • Detail 3: [value]
   
   Proceed? (y/n)
```

## Safety Requirements

**Agent must always:**
- ✅ Show summaries before actions
- ✅ Wait for approval before committing/pushing/creating PR
- ✅ Never auto-merge (requires manual review)
- ✅ Check branch (block if on main)
- ✅ Run tests before committing (if applicable)
- ✅ Verify state before actions

## Quick Reference

| Stage | Prompt Template | When to Use |
|-------|----------------|-------------|
| **Start Work** | Section 1.1 | Before any new work |
| **Create Branch** | Section 1.2 | When creating feature branch |
| **Check Coordination** | Section 1.3 | Before modifying shared files |
| **Commit** | Section 2.1 | After logical unit of work |
| **Push** | Section 2.2 | To back up work or trigger CI |
| **Sync with Main** | Section 2.3 | When main has updates |
| **Run Tests** | Section 2.4 | Before committing or when needed |
| **Final Prep** | Section 3.1 | Before creating PR |
| **Review Changes** | Section 3.2 | Before creating PR |
| **Create PR** | Section 4.1 | When ready for PR |
| **Check PR** | Section 4.2 | After PR creation or when checking |
| **Cleanup** | Section 5.1 | After PR merge |
| **Test Failures** | Section 6.1 | When tests fail |
| **Conflicts** | Section 6.2 | When merge conflicts occur |
| **Build Errors** | Section 6.3 | When build fails |
| **CI Failures** | Section 6.4 | When CI fails |
| **Session End** | Section 7.1 | When ending session |

## Related Documentation

- `docs/development/workflow/AUTOMATED_WORKFLOW.md` - Automated workflow tools
- `docs/development/workflow/MULTI_AGENT_WORKFLOW.md` - Multi-agent workflow guidelines
- `.cursor/rules/branching.mdc` - Branch workflow rules
- `.cursor/rules/cicd.mdc` - CI/CD requirements
- `scripts/post-merge-cleanup.sh` - Post-merge cleanup script

