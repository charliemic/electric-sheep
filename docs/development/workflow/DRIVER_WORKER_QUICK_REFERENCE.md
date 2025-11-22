# Driver-Worker Pattern - Quick Reference

**Date**: 2025-01-22  
**Purpose**: Quick reference for driver-worker architecture  
**Status**: Active

---

## Workflow Summary

### 1. Driver Agent (Planning)

**Start**: User starts driver agent  
**Task**: "Plan and decompose [project/epic]"  
**Output**: 
- GitHub Issues created (one per task)
- TASK_BOARD.md updated
- Worker count recommendation

**Template**: `docs/development/workflow/DRIVER_AGENT_PROMPT.md`

---

### 2. Worker Agents (Execution)

**Start**: User starts worker agents (2-4 based on driver recommendation)  
**Discovery**: Pre-work check shows available tasks automatically  
**Claim**: `./scripts/claim-task.sh <issue-number>`  
**Work**: Execute task independently

---

## Quick Commands

### For Driver Agents

```bash
# After decomposing project, create issues:
gh issue create --title "Task: [description]" \
  --label "status:available,skill:ui,priority:high" \
  --body "[task details]"

# Update task board:
# Edit docs/development/workflow/TASK_BOARD.md
```

### For Worker Agents

```bash
# See available tasks (automatic in pre-work check):
./scripts/pre-work-check.sh

# Or manually:
./scripts/query-task-board.sh available

# Claim a task:
./scripts/claim-task.sh <issue-number>

# Check task status:
./scripts/query-task-board.sh status <issue-number>

# Update task status:
./scripts/query-task-board.sh update <issue-number> complete
```

---

## Task Status Flow

```
Available → Claimed → In Progress → Complete
                ↓
            Blocked (if dependency)
```

---

## Optimal Worker Count

- **Small Projects** (3-5 tasks): 2-3 workers
- **Medium Projects** (6-10 tasks): 3-4 workers
- **Large Projects** (10+ tasks): 4-5 workers (phased)

---

## Files

- `docs/development/workflow/TASK_BOARD.md` - Task board
- `scripts/query-task-board.sh` - Task queries
- `scripts/claim-task.sh` - Task claiming
- `docs/development/workflow/DRIVER_AGENT_PROMPT.md` - Driver template

---

## Related Documentation

- `docs/architecture/DRIVER_WORKER_WORKFLOW.md` - Complete workflow
- `docs/architecture/OPTIMAL_WORKER_COUNT_ANALYSIS.md` - Worker count analysis

