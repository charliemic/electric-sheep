# Real-Time Collaboration Model - Summary

**Date**: 2025-01-20  
**Status**: Implemented  
**Purpose**: Quick reference for real-time agent collaboration

## Quick Overview

**Isolation for Code Changes:**
- ✅ Code changes remain isolated (git worktrees)
- ✅ No interference between agents
- ✅ Coordination required for shared files

**Real-Time Sharing for Rules/Workflow:**
- ✅ Rules and workflow docs are shared (changes propagate immediately)
- ✅ Agents check for updates before starting work
- ✅ Improvements benefit all agents immediately
- ✅ No conflicts possible (rules/workflow are read-only during work)

## What Gets Shared

### ✅ Shared (Real-Time Updates)
- `.cursor/rules/*.mdc` - All cursor rules
- `docs/development/workflow/*.md` - Workflow documentation
- `docs/development/workflow/AGENT_COORDINATION.md` - Coordination document
- `scripts/pre-work-check.sh` - Pre-work automation
- `scripts/check-agent-coordination.sh` - Coordination checks
- `scripts/check-rule-updates.sh` - Update detection

### ❌ Isolated (No Real-Time Sharing)
- All code files (`app/src/**/*.kt`, `app/src/**/*.xml`)
- Test files (`app/src/test/**/*.kt`)
- Build files (unless explicitly coordinated)
- Feature-specific documentation

## How It Works

### Before Starting Work

```bash
# 1. Pull latest main (includes rule/workflow updates)
git checkout main
git pull origin main

# 2. Check for rule/workflow updates
./scripts/check-rule-updates.sh

# 3. Create worktree from updated main
git worktree add ../electric-sheep-<task-name> -b feature/<task-name>
cd ../electric-sheep-<task-name>

# 4. Run pre-work check (includes update check)
./scripts/pre-work-check.sh
```

### During Work

- Work in isolated worktree (code changes isolated)
- Rules/workflow docs are read-only (don't modify during work)
- If you discover improvement, document it and create PR

### After Work

- Create PR with code changes
- If you improved rules/workflow, include in PR
- Other agents get updates on next `git pull origin main`

## Update Detection

**Script:** `scripts/check-rule-updates.sh`

**What it does:**
- Checks if local main is behind remote main
- Detects changes to rules/workflow files
- Shows summary of updates
- Recommends pulling latest main

**Integration:**
- Called automatically by `pre-work-check.sh`
- Can be run manually: `./scripts/check-rule-updates.sh`

## Examples from Research

Based on research in multi-agent systems:

1. **AgentNet** - Decentralized evolutionary coordination
   - Applied: Agents pull latest rules/workflow (autonomous evolution)
   - Rules/workflow improvements propagate immediately (real-time adaptation)

2. **Cross-Task Experiential Learning** - Learning from prior experiences
   - Applied: Rules/workflow docs capture experiences
   - Agents check rules before work (retrieve relevant experiences)

3. **AgentDropout** - Dynamic agent elimination
   - Applied: Worktrees eliminate redundant coordination
   - Rules/workflow optimize agent behavior

## Benefits

✅ **Immediate Benefit** - All agents get improvements immediately  
✅ **No Conflicts** - Rules/workflow docs are read-only during work  
✅ **Maintained Isolation** - Code changes remain isolated  
✅ **Continuous Improvement** - System evolves and improves over time

## Related Documentation

- `docs/development/workflow/REAL_TIME_COLLABORATION.md` - Complete guide
- `docs/development/workflow/MULTI_AGENT_WORKFLOW.md` - Multi-agent workflow
- `.cursor/rules/branching.mdc` - Branch isolation rules
- `scripts/pre-work-check.sh` - Pre-work automation
- `scripts/check-rule-updates.sh` - Update detection script

