# Real-Time Agent Collaboration Model

**Last Updated**: 2025-01-20  
**Status**: Active Guidelines  
**Purpose**: Enable real-time sharing of agent improvements and workflow changes while maintaining isolation for code changes

## Overview

This document defines how agents can share improvements and workflow updates in real-time while maintaining necessary isolation to avoid conflicts. The model is based on research in multi-agent systems and decentralized collaboration patterns.

## Core Principles

### 1. Isolation for Code Changes (MANDATORY)
- ✅ **Code changes remain isolated** - Use git worktrees for all code modifications
- ✅ **No interference** - Agents work in separate directories on separate branches
- ✅ **Coordination required** - Check coordination doc before modifying shared files

### 2. Real-Time Sharing for Rules and Workflow (ENABLED)
- ✅ **Rules and workflow docs are shared** - Changes propagate immediately via git
- ✅ **Agents check for updates** - Before starting work, check for rule/workflow updates
- ✅ **No conflicts possible** - Rules/workflow docs are read-only during agent work
- ✅ **Improvements benefit all** - Agent improvements to rules/workflow help all agents

### 3. Update Detection (AUTOMATED)
- ✅ **Pre-work check includes update detection** - Automatically checks for rule/workflow updates
- ✅ **Git-based detection** - Uses git to detect changes to rules/workflow docs
- ✅ **Non-blocking** - Updates don't block work, but agents are informed
- ✅ **Pull before work** - Agents pull latest main before starting work

## What Gets Shared in Real-Time

### ✅ Shared (Real-Time Updates)

**These files are shared and updated in real-time:**
- `.cursor/rules/*.mdc` - All cursor rules (agent behavior, workflows, patterns)
- `docs/development/workflow/*.md` - Workflow documentation
- `docs/development/AGENT_COORDINATION.md` - Coordination document
- `scripts/pre-work-check.sh` - Pre-work automation
- `scripts/check-agent-coordination.sh` - Coordination checks
- `scripts/check-rule-updates.sh` - Update detection script
- `docs/architecture/*.md` - Architecture documentation (read-only during work)

**Why these are safe to share:**
- ✅ Read-only during agent work (agents don't modify rules while working)
- ✅ No merge conflicts (rules are updated on main, agents pull before work)
- ✅ Immediate benefit (all agents get improvements immediately)
- ✅ Git handles synchronization (standard git pull workflow)

### ❌ Isolated (No Real-Time Sharing)

**These remain isolated per agent:**
- All code files (`app/src/**/*.kt`, `app/src/**/*.xml`)
- Test files (`app/src/test/**/*.kt`)
- Build files (`build.gradle.kts`, `app/build.gradle.kts`) - unless explicitly coordinated
- Feature-specific documentation (created during feature work)
- Agent-specific worktrees and branches

**Why these remain isolated:**
- ✅ Code changes can conflict (same file, different changes)
- ✅ Requires coordination (check coordination doc)
- ✅ Worktrees provide isolation (each agent has own directory)
- ✅ Merge conflicts possible (must resolve before merge)

## Update Detection Mechanism

### How It Works

1. **Before Starting Work:**
   ```bash
   # Agent pulls latest main (includes rule/workflow updates)
   git checkout main
   git pull origin main
   
   # Check for rule/workflow updates
   ./scripts/check-rule-updates.sh
   
   # Create worktree from updated main
   git worktree add ../electric-sheep-<task-name> -b feature/<task-name>
   ```

2. **During Work:**
   - Agent works in isolated worktree
   - Rules/workflow docs are read-only (agent doesn't modify them)
   - If agent discovers improvement, creates PR to update rules/workflow

3. **After Work:**
   - Agent merges code changes via PR
   - If agent improved rules/workflow, those are also merged
   - Other agents get updates on next `git pull origin main`

### Update Detection Script

**Script:** `scripts/check-rule-updates.sh`

**Purpose:** Detects if rules or workflow docs have been updated since last check.

**Usage:**
```bash
./scripts/check-rule-updates.sh
```

**What it checks:**
- ✅ Last pull time vs current time
- ✅ Changes to `.cursor/rules/*.mdc` files
- ✅ Changes to `docs/development/workflow/*.md` files
- ✅ Changes to coordination doc
- ✅ Changes to pre-work scripts

**Output:**
- Lists updated files
- Shows summary of changes
- Recommends pulling latest main if updates detected

## Real-Time Collaboration Patterns

### Pattern 1: Rule Improvements

**Scenario:** Agent discovers a better pattern or workflow improvement.

**Process:**
1. Agent works in isolated worktree (code changes isolated)
2. Agent documents improvement in rules/workflow doc (in same worktree)
3. Agent creates PR with both code changes and rule improvements
4. PR is reviewed and merged
5. Other agents get updates on next `git pull origin main`

**Example:**
```bash
# Agent working on feature
git worktree add ../electric-sheep-new-feature -b feature/new-feature
cd ../electric-sheep-new-feature

# Agent discovers improvement to error handling pattern
# Updates .cursor/rules/error-handling.mdc with improvement
# Updates code to use new pattern

# Creates PR with both changes
git add .
git commit -m "feat: add new feature + improve error handling pattern"
git push origin feature/new-feature
# Create PR
```

### Pattern 2: Workflow Improvements

**Scenario:** Agent discovers a better workflow or automation.

**Process:**
1. Agent works in isolated worktree
2. Agent improves workflow script or documentation
3. Agent creates PR with workflow improvement
4. PR is reviewed and merged
5. All agents benefit from improved workflow immediately

**Example:**
```bash
# Agent improves pre-work-check.sh
# Adds new check for rule updates
# Creates PR with improvement
# All agents get improved script on next pull
```

### Pattern 3: Coordination Updates

**Scenario:** Agent updates coordination doc with current work status.

**Process:**
1. Agent checks coordination doc (read-only during work)
2. Agent updates coordination doc with work status (in isolated worktree)
3. Agent commits coordination update with code changes
4. PR includes coordination update
5. Other agents see updated coordination on next pull

**Example:**
```bash
# Agent updates AGENT_COORDINATION.md with work status
# Commits with code changes
# PR includes coordination update
# Other agents see updated status on next pull
```

## Examples from Research

### AgentNet: Decentralized Evolutionary Coordination

**Key Concepts:**
- Agents autonomously evolve and collaborate
- Dynamic specialization based on task demands
- Real-time adaptation to workflow changes

**Applied to Our Model:**
- ✅ Agents pull latest rules/workflow before work (autonomous evolution)
- ✅ Rules/workflow improvements propagate immediately (real-time adaptation)
- ✅ Agents specialize in their tasks (dynamic specialization via worktrees)

### Cross-Task Experiential Learning

**Key Concepts:**
- Agents learn from prior experiences across tasks
- Maintain individual experience pools
- Retrieve high-reward, task-relevant experiences

**Applied to Our Model:**
- ✅ Rules/workflow docs capture experiences (experience pools)
- ✅ Agents check rules before work (retrieve relevant experiences)
- ✅ Improvements to rules benefit all agents (cross-task learning)

### AgentDropout: Dynamic Agent Elimination

**Key Concepts:**
- Identify and eliminate redundant agents
- Optimize communication graph
- Dynamic adjustment for efficiency

**Applied to Our Model:**
- ✅ Worktrees eliminate redundant coordination (isolation)
- ✅ Rules/workflow optimize agent behavior (efficiency)
- ✅ Update detection prevents redundant work (dynamic adjustment)

## Implementation Checklist

### For Agents Starting Work

- [ ] **Pull latest main** - `git checkout main && git pull origin main`
- [ ] **Check for rule/workflow updates** - `./scripts/check-rule-updates.sh`
- [ ] **Review updates** - Read any updated rules/workflow docs
- [ ] **Create worktree** - `./scripts/create-worktree.sh <task-name>`
- [ ] **Work in isolation** - All code changes in worktree
- [ ] **Rules/workflow are read-only** - Don't modify during work (create PR for improvements)

### For Agents Improving Rules/Workflow

- [ ] **Work in isolated worktree** - Same as code changes
- [ ] **Document improvement** - Update rules/workflow doc
- [ ] **Test improvement** - Verify it works
- [ ] **Create PR** - Include rule/workflow improvement with code changes
- [ ] **Merge via PR** - Standard PR process
- [ ] **Other agents get update** - On next `git pull origin main`

### For Agents Receiving Updates

- [ ] **Detect updates** - `./scripts/check-rule-updates.sh` shows updates
- [ ] **Pull latest main** - `git pull origin main`
- [ ] **Review updates** - Read updated rules/workflow docs
- [ ] **Apply updates** - Use new patterns/workflows in current work
- [ ] **No conflicts** - Rules/workflow updates don't conflict with code work

## Benefits

### ✅ Immediate Benefit
- All agents get improvements immediately (on next pull)
- No waiting for coordination or merge windows
- Rules/workflow improvements propagate instantly

### ✅ No Conflicts
- Rules/workflow docs are read-only during agent work
- Git handles synchronization (standard pull workflow)
- No merge conflicts possible (agents don't modify rules while working)

### ✅ Maintained Isolation
- Code changes remain isolated (worktrees)
- No interference between agents
- Coordination still required for shared code files

### ✅ Continuous Improvement
- Agents can improve rules/workflow as they work
- Improvements benefit all agents immediately
- System evolves and improves over time

## Anti-Patterns to Avoid

### ❌ Don't Modify Rules During Work

**Bad:**
```bash
# Agent modifies rules while working on feature
# This can cause conflicts if another agent also updates rules
```

**Good:**
```bash
# Agent documents improvement, creates PR
# Rules are updated via PR, all agents get update on next pull
```

### ❌ Don't Skip Update Check

**Bad:**
```bash
# Agent starts work without checking for updates
# Misses important rule/workflow improvements
```

**Good:**
```bash
# Agent always checks for updates before starting work
./scripts/check-rule-updates.sh
```

### ❌ Don't Modify Rules in Worktree

**Bad:**
```bash
# Agent modifies rules in worktree, creates conflicts
```

**Good:**
```bash
# Agent works in worktree, rules are read-only
# Agent creates PR to update rules (separate from code work)
```

## Integration with Existing Workflow

### Pre-Work Check Integration

**Updated `scripts/pre-work-check.sh` includes:**
- ✅ Check for rule/workflow updates
- ✅ Pull latest main (includes rule/workflow updates)
- ✅ Review updated rules/workflow docs
- ✅ Continue with standard pre-work checks

### Coordination Doc Integration

**Coordination doc is shared:**
- ✅ Agents update coordination doc with work status
- ✅ Updates propagate via git pull
- ✅ All agents see current work status
- ✅ No conflicts (coordination doc is updated sequentially via PRs)

### Cursor Rules Integration

**Cursor rules are shared:**
- ✅ All agents use same rules (from `.cursor/rules/*.mdc`)
- ✅ Rule improvements propagate immediately
- ✅ Agents check for rule updates before work
- ✅ Rules are read-only during agent work

## Related Documentation

- `docs/development/workflow/MULTI_AGENT_WORKFLOW.md` - Multi-agent workflow guidelines
- `docs/development/workflow/AGENT_COORDINATION.md` - Coordination document
- `.cursor/rules/branching.mdc` - Branch isolation rules
- `scripts/pre-work-check.sh` - Pre-work automation
- `scripts/check-rule-updates.sh` - Update detection script

## Future Enhancements

### Potential Improvements

1. **Automated Update Notifications**
   - Script that notifies agents of rule/workflow updates
   - Integration with git hooks

2. **Rule Versioning**
   - Track rule versions and changes
   - Agents can see what changed and why

3. **Update Impact Analysis**
   - Analyze which agents are affected by rule/workflow updates
   - Notify affected agents

4. **Collaborative Rule Improvement**
   - Multiple agents can contribute to rule improvements
   - Review process for rule changes

5. **Real-Time Coordination**
   - Live coordination doc updates (via git pull)
   - Agents see work status in real-time

