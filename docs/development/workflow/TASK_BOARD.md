# Task Board

**Last Updated**: 2025-01-22  
**Purpose**: Centralized task queue for driver/worker agent coordination  
**Status**: Active

---

## How to Use

### For Driver Agents
1. Decompose project into tasks
2. Create GitHub Issues for each task
3. Add task entries to this board
4. Link tasks to epic/issues

### For Worker Agents
1. Run `./scripts/pre-work-check.sh` to see available tasks
2. Review tasks in this board for details
3. Claim task: `gh issue edit <number> --add-label 'status:in-progress' --assign '@me'`
4. Update status as work progresses

### Query Tasks
```bash
# List available tasks
./scripts/query-task-board.sh available

# Claim a task
./scripts/query-task-board.sh claim <task-id>

# Get task status
./scripts/query-task-board.sh status <task-id>
```

---

## Active Projects

*No active projects currently*

---

## Available Tasks (Unassigned)

*No available tasks currently*

---

## Task Details

*No tasks defined yet*

---

## Task Status Legend

- **Available**: Task ready to be claimed
- **Claimed**: Task assigned but not started
- **In Progress**: Agent actively working
- **Blocked**: Waiting on dependency
- **Complete**: Task finished, PR merged

---

## Notes

This task board is managed by the driver agent and updated by worker agents as they claim and complete tasks.

