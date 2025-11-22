# Driver Agent Architecture - Multi-Agent Team Emulation

**Date**: 2025-01-22  
**Purpose**: Design a driver/worker pattern for multi-agent collaboration  
**Status**: Proposal

---

## Executive Summary

This document proposes a **Driver Agent Architecture** that enables a coordinator agent to decompose large projects into tasks and dynamically assign them to worker agents. Since we cannot programmatically trigger prompts in Cursor, we use a **task board system** that agents discover and claim tasks from.

---

## Architecture Overview

### Current State
- **Coordination-based**: Agents coordinate via shared documents
- **Manual assignment**: Tasks assigned via coordination doc
- **Reactive**: Agents check coordination doc before starting work
- **No decomposition**: Large projects not broken down automatically

### Proposed State
- **Driver/Worker pattern**: Driver agent plans, workers execute
- **Task board**: Centralized task queue (GitHub Issues + structured files)
- **Dynamic discovery**: Agents discover and claim tasks automatically
- **Automatic decomposition**: Driver breaks down epics into actionable tasks

---

## Core Components

### 1. Driver Agent (Coordinator)

**Role**: Project planner and task decomposer

**Responsibilities**:
- Analyze large projects/epics
- Decompose into actionable tasks
- Assign tasks to appropriate worker agents
- Monitor progress and adjust assignments
- Resolve blockers and dependencies

**Workflow**:
1. Receive project/epic request
2. Analyze scope and dependencies
3. Decompose into tasks
4. Create task board entries (GitHub Issues + task file)
5. Assign tasks to worker agents
6. Monitor and coordinate

### 2. Worker Agents (Specialists)

**Role**: Task executors

**Responsibilities**:
- Discover available tasks
- Claim tasks matching their expertise
- Execute tasks independently
- Update task status
- Report completion/blockers

**Workflow**:
1. Check task board for available tasks
2. Claim task matching expertise
3. Execute task
4. Update status
5. Mark complete

### 3. Task Board System

**Components**:
- **GitHub Issues**: Human-readable task tracking
- **Task Queue File**: Machine-readable task assignments
- **Status Tracking**: Real-time task status

**Location**: `docs/development/workflow/TASK_BOARD.md`

---

## Task Board Design

### Structure

```markdown
# Task Board

## Active Projects

### Project: <project-name>
- **Epic**: Issue #<number>
- **Driver Agent**: <agent-role>
- **Status**: Planning / In Progress / Complete
- **Tasks**:
  - [ ] Task 1: <description> → Assigned to: <agent-role> → Status: Available
  - [ ] Task 2: <description> → Assigned to: <agent-role> → Status: In Progress
  - [x] Task 3: <description> → Assigned to: <agent-role> → Status: Complete

## Available Tasks (Unassigned)

- [ ] Task: <description> → Priority: High → Estimated: 2h → Skills: [android, ui]
- [ ] Task: <description> → Priority: Medium → Estimated: 4h → Skills: [backend, api]

## Task Details

### Task: <task-id>
- **Project**: <project-name>
- **Epic**: Issue #<number>
- **Assigned To**: <agent-role> / Available
- **Status**: Available / Claimed / In Progress / Blocked / Complete
- **Priority**: High / Medium / Low
- **Estimated Effort**: <hours>
- **Required Skills**: [skill1, skill2]
- **Dependencies**: [task-id1, task-id2]
- **Files to Modify**: [file1, file2]
- **Branch**: `feature/<task-name>`
- **Created**: <date>
- **Updated**: <date>
- **Completed**: <date>
```

### Task States

1. **Available**: Task created, not yet claimed
2. **Claimed**: Agent has claimed task, not started
3. **In Progress**: Agent actively working
4. **Blocked**: Waiting on dependency or blocker
5. **Complete**: Task finished, PR merged

---

## Dynamic Task Discovery

### Mechanism

Since we cannot trigger prompts, agents discover tasks through:

1. **Pre-Work Check Enhancement**: 
   - Check task board for available tasks
   - Display tasks matching agent's expertise
   - Prompt agent to claim task

2. **Task Board Query Script**:
   ```bash
   ./scripts/query-task-board.sh available --skills android,ui
   ./scripts/query-task-board.sh claim <task-id>
   ./scripts/query-task-board.sh status <task-id>
   ```

3. **Coordination Doc Integration**:
   - Task board entries link to coordination doc
   - Agents update both when claiming/completing

### Agent Discovery Flow

```
1. Agent starts session
   ↓
2. Runs pre-work-check.sh
   ↓
3. Script checks task board
   ↓
4. Displays available tasks matching agent's role
   ↓
5. Agent claims task (updates task board)
   ↓
6. Agent executes task
   ↓
7. Agent updates status as work progresses
   ↓
8. Agent marks complete when done
```

---

## Driver Agent Workflow

### Phase 1: Project Analysis

```markdown
1. Receive project request (epic, issue, or user request)
2. Analyze project scope:
   - Read project description
   - Identify components/modules affected
   - Identify dependencies
   - Estimate complexity
3. Research similar implementations
4. Identify required skills/expertise
```

### Phase 2: Task Decomposition

```markdown
1. Break down project into tasks:
   - Each task should be independently executable
   - Tasks should have clear acceptance criteria
   - Tasks should be appropriately sized (2-8 hours)
2. Identify task dependencies
3. Prioritize tasks
4. Assign required skills to each task
```

### Phase 3: Task Board Creation

```markdown
1. Create GitHub Issue for epic (if not exists)
2. Create task board entries:
   - Add to TASK_BOARD.md
   - Create individual task entries
   - Link tasks to epic issue
3. Assign tasks to worker agents:
   - Match tasks to agent expertise
   - Update task board with assignments
   - Update coordination doc
```

### Phase 4: Monitoring & Coordination

```markdown
1. Monitor task progress:
   - Check task board status
   - Review coordination doc
   - Check for blockers
2. Resolve dependencies:
   - Unblock dependent tasks
   - Adjust assignments if needed
3. Coordinate handoffs:
   - Ensure smooth task transitions
   - Resolve conflicts
```

---

## Implementation Strategy

### Phase 1: Task Board Foundation

**Goal**: Create task board system

**Tasks**:
1. Create `docs/development/workflow/TASK_BOARD.md`
2. Create `scripts/query-task-board.sh`
3. Enhance `scripts/pre-work-check.sh` to check task board
4. Document task board usage

**Estimated Effort**: 4-6 hours

### Phase 2: Driver Agent Capabilities

**Goal**: Enable driver agent to decompose projects

**Tasks**:
1. Create driver agent prompt template
2. Create task decomposition guidelines
3. Create task template format
4. Test with sample project

**Estimated Effort**: 6-8 hours

### Phase 3: Worker Agent Integration

**Goal**: Enable workers to discover and claim tasks

**Tasks**:
1. Enhance pre-work-check to show available tasks
2. Create task claiming workflow
3. Update coordination doc integration
4. Test end-to-end flow

**Estimated Effort**: 4-6 hours

### Phase 4: Monitoring & Automation

**Goal**: Enable driver to monitor and adjust

**Tasks**:
1. Create task status monitoring script
2. Create blocker detection
3. Create automatic reassignment logic
4. Test monitoring workflow

**Estimated Effort**: 6-8 hours

---

## Task Board File Structure

### Location
`docs/development/workflow/TASK_BOARD.md`

### Format

```markdown
# Task Board

**Last Updated**: 2025-01-22  
**Purpose**: Centralized task queue for driver/worker agent coordination

## Active Projects

### Project: Implement User Onboarding Flow
- **Epic**: Issue #52
- **Driver Agent**: Planning Agent
- **Status**: In Progress
- **Progress**: 3/7 tasks complete
- **Tasks**:
  - [x] Task-001: Design onboarding screens → Assigned: UI Agent → Complete
  - [x] Task-002: Implement navigation flow → Assigned: Navigation Agent → Complete
  - [x] Task-003: Add user data collection → Assigned: Data Agent → Complete
  - [ ] Task-004: Implement validation logic → Assigned: Backend Agent → In Progress
  - [ ] Task-005: Add analytics tracking → Assigned: Analytics Agent → Available
  - [ ] Task-006: Write tests → Assigned: Testing Agent → Blocked (waiting on Task-004)
  - [ ] Task-007: Documentation → Assigned: Docs Agent → Available

## Available Tasks

### Task-005: Add Analytics Tracking
- **Project**: Implement User Onboarding Flow
- **Epic**: Issue #52
- **Priority**: Medium
- **Estimated Effort**: 3 hours
- **Required Skills**: [analytics, tracking, events]
- **Dependencies**: [Task-001, Task-002]
- **Files to Modify**: 
  - `app/src/main/.../ui/screens/onboarding/*.kt`
  - `app/src/main/.../analytics/AnalyticsManager.kt`
- **Acceptance Criteria**:
  - Track onboarding start event
  - Track each step completion
  - Track onboarding completion
- **Status**: Available
- **Created**: 2025-01-22
- **Updated**: 2025-01-22

### Task-007: Documentation
- **Project**: Implement User Onboarding Flow
- **Epic**: Issue #52
- **Priority**: Low
- **Estimated Effort**: 2 hours
- **Required Skills**: [documentation, markdown]
- **Dependencies**: [Task-001, Task-002, Task-003, Task-004]
- **Files to Create**: 
  - `docs/features/ONBOARDING_FLOW.md`
- **Acceptance Criteria**:
  - Document user flow
  - Document technical implementation
  - Add screenshots/examples
- **Status**: Available
- **Created**: 2025-01-22
- **Updated**: 2025-01-22

## Task Status Legend

- **Available**: Task ready to be claimed
- **Claimed**: Task assigned but not started
- **In Progress**: Agent actively working
- **Blocked**: Waiting on dependency
- **Complete**: Task finished, PR merged
```

---

## Query Script Design

### `scripts/query-task-board.sh`

```bash
#!/bin/bash
# Query and manage task board

COMMAND=$1
TASK_ID=$2

case $COMMAND in
  available)
    # List available tasks
    # Filter by skills if provided
    grep -A 20 "Status: Available" docs/development/workflow/TASK_BOARD.md
    ;;
  claim)
    # Claim a task
    # Update status to "Claimed" or "In Progress"
    # Update assigned agent
    ;;
  status)
    # Get task status
    grep -A 10 "Task: $TASK_ID" docs/development/workflow/TASK_BOARD.md
    ;;
  update)
    # Update task status
    ;;
  list)
    # List all tasks for a project
    ;;
esac
```

---

## Driver Agent Prompt Template

```markdown
You are a Driver Agent responsible for project planning and task decomposition.

## Your Role

1. **Analyze Projects**: Break down large projects/epics into actionable tasks
2. **Decompose Tasks**: Create independent, executable tasks with clear criteria
3. **Assign Tasks**: Match tasks to appropriate worker agents based on skills
4. **Monitor Progress**: Track task completion and resolve blockers

## Current Request

[User provides project/epic description]

## Your Task

1. **Analyze the project**:
   - What is the scope?
   - What components are affected?
   - What are the dependencies?
   - What skills are required?

2. **Decompose into tasks**:
   - Break down into 2-8 hour tasks
   - Each task should be independently executable
   - Identify dependencies between tasks
   - Prioritize tasks

3. **Create task board entries**:
   - Add to `docs/development/workflow/TASK_BOARD.md`
   - Create GitHub Issues for epic (if needed)
   - Link tasks to epic
   - Assign tasks to worker agents

4. **Document the plan**:
   - Update coordination doc
   - Create project plan document
   - Set up monitoring

## Task Template

For each task, create:
- Task ID (e.g., Task-001)
- Description
- Priority (High/Medium/Low)
- Estimated effort
- Required skills
- Dependencies
- Files to modify
- Acceptance criteria
- Status (Available/Claimed/In Progress/Blocked/Complete)

## Output

Create task board entries and update coordination doc.
```

---

## Worker Agent Integration

### Enhanced Pre-Work Check

```bash
# In scripts/pre-work-check.sh

# Check for available tasks
echo "5️⃣  Checking Task Board for Available Tasks..."
if [ -f "docs/development/workflow/TASK_BOARD.md" ]; then
    AVAILABLE_TASKS=$(./scripts/query-task-board.sh available --skills android,ui 2>/dev/null || echo "")
    if [ -n "$AVAILABLE_TASKS" ]; then
        echo "   📋 Available tasks matching your expertise:"
        echo "$AVAILABLE_TASKS" | sed 's/^/      /'
        echo "   💡 To claim a task: ./scripts/query-task-board.sh claim <task-id>"
    else
        echo "   ℹ️  No available tasks matching your expertise"
    fi
else
    echo "   ℹ️  Task board not found"
fi
```

### Worker Agent Prompt Enhancement

```markdown
Before starting work, check for available tasks:

1. Run pre-work check: `./scripts/pre-work-check.sh`
2. Review available tasks in task board
3. Claim a task if one matches your expertise
4. Update task status as you work
5. Mark complete when done

If no tasks available, you can:
- Work on your current task
- Wait for driver agent to create tasks
- Request task creation from driver agent
```

---

## Benefits

### 1. Dynamic Task Assignment
- Tasks discovered automatically
- Agents claim tasks matching expertise
- No manual coordination needed

### 2. Project Decomposition
- Large projects broken down automatically
- Clear task boundaries
- Dependency management

### 3. Better Coordination
- Centralized task tracking
- Real-time status updates
- Blocker visibility

### 4. Scalability
- Multiple workers can work in parallel
- Tasks assigned based on skills
- Efficient resource utilization

---

## Limitations & Workarounds

### Limitation: Cannot Trigger Prompts

**Workaround**: Task discovery via pre-work checks
- Agents check task board on session start
- Tasks displayed automatically
- Agents claim tasks manually

### Limitation: No Real-Time Updates

**Workaround**: File-based coordination
- Task board updated via git
- Agents pull latest before checking
- Status updates committed to git

### Limitation: No Direct Agent Communication

**Workaround**: Coordination doc + task board
- All communication via documents
- Status updates in task board
- Blockers documented in coordination doc

---

## Next Steps

1. **Create Task Board**: Implement `TASK_BOARD.md` structure
2. **Create Query Script**: Implement `query-task-board.sh`
3. **Enhance Pre-Work Check**: Add task board checking
4. **Test with Sample Project**: Decompose a real project
5. **Iterate Based on Results**: Refine based on usage

---

## Related Documentation

- `docs/development/workflow/AGENT_COORDINATION.md` - Current coordination system
- `docs/development/workflow/MULTI_AGENT_WORKFLOW.md` - Multi-agent guidelines
- `docs/architecture/MULTI_AGENT_COLLABORATION_ARCHITECTURE.md` - Current architecture

