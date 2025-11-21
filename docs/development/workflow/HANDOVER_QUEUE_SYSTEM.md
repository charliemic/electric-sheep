# Handover Queue System

**Date**: 2025-11-21  
**Status**: Design Document  
**Purpose**: Manage pending handovers and agent transitions

## Overview

A system to track, prioritize, and manage handovers between agents, ensuring smooth transitions and preventing work loss.

## Problem Statement

**Current State**:
- Handovers happen ad-hoc
- No tracking of pending handovers
- No priority system
- Risk of work getting lost
- No visibility into handover status

**Solution**:
- Centralized handover queue
- Priority and status tracking
- Automated handover detection
- Clear assignment process

## System Components

### 1. Handover Queue File

**Location**: `docs/development/workflow/HANDOVER_QUEUE.md`

**Structure**:
```markdown
# Handover Queue

## Active Handovers

### [Priority] Task Name
- **Status**: [pending/assigned/in-progress/complete]
- **Created**: YYYY-MM-DD HH:MM
- **Assigned to**: [agent/branch]
- **Handover doc**: `HANDOVER_PROMPT.md` or `docs/development/handovers/TASK_NAME.md`
- **Next steps**: [brief description]

## Completed Handovers

### Task Name
- **Completed**: YYYY-MM-DD
- **Duration**: X hours
- **Outcome**: [summary]
```

### 2. Handover Detection Script

**Location**: `scripts/detect-handover-needed.sh`

**Purpose**: Monitor conversation metrics and detect when handover is needed

**Checks**:
- Conversation length (turns, tokens, time)
- Context size (files, tokens)
- Error rate
- Repetition indicators
- Quality degradation

**Output**: Suggests handover if thresholds exceeded

### 3. Handover Creation Script

**Location**: `scripts/create-handover.sh`

**Purpose**: Automatically generate handover document

**Process**:
1. Extract current state from conversation
2. Identify completed work
3. Document next steps
4. Create handover document
5. Add to queue

### 4. Handover Assignment

**Process**:
1. New agent checks queue
2. Selects highest priority handover
3. Updates status to "assigned"
4. Reads handover document
5. Continues work

## Handover Detection Thresholds

### Fixed Thresholds

```yaml
conversation:
  max_turns: 100
  max_tokens: 100000
  max_hours: 4

context:
  max_files: 50
  max_context_tokens: 100000

quality:
  max_errors: 3
  max_repetitions: 3
  quality_degradation: true
```

### Dynamic Detection

**Monitor for**:
- Repeated questions
- Circular reasoning
- Declining code quality
- Increasing error rate
- Slower problem-solving

## Priority System

### Priority Levels

1. **Critical** - Blocking work, urgent fixes
2. **High** - Important features, major milestones
3. **Medium** - Standard features, improvements
4. **Low** - Nice-to-have, documentation

### Priority Factors

- **Urgency**: How time-sensitive?
- **Impact**: How important is the work?
- **Dependencies**: Blocking other work?
- **Complexity**: How complex is the handover?

## Status Tracking

### Status Values

- **pending** - Waiting for assignment
- **assigned** - Assigned to agent, not started
- **in-progress** - Agent actively working
- **complete** - Work finished, handover closed
- **blocked** - Waiting on external dependency
- **abandoned** - No longer needed

## Workflow

### Creating Handover

1. **Detection**: Script detects threshold exceeded
2. **Generation**: Create handover document
3. **Queue**: Add to handover queue
4. **Notification**: Alert that handover needed

### Picking Up Handover

1. **Check queue**: Review pending handovers
2. **Select**: Choose highest priority
3. **Update status**: Mark as "assigned"
4. **Read handover**: Understand current state
5. **Continue work**: Start from handover point

### Completing Handover

1. **Finish work**: Complete the task
2. **Update status**: Mark as "complete"
3. **Document outcome**: Add to completed section
4. **Archive**: Move to completed handovers

## Implementation

### Phase 1: Manual Queue

**Create**:
- `docs/development/workflow/HANDOVER_QUEUE.md` - Queue file
- Template for handover entries
- Process documentation

**Use**:
- Manual handover creation
- Manual queue management
- Manual assignment

### Phase 2: Detection Script

**Create**:
- `scripts/detect-handover-needed.sh` - Detection script
- Metrics collection
- Threshold checking
- Handover suggestion

**Use**:
- Automated detection
- Manual handover creation
- Manual queue management

### Phase 3: Full Automation

**Create**:
- `scripts/create-handover.sh` - Auto-generation
- `scripts/assign-handover.sh` - Assignment
- Integration with metrics

**Use**:
- Fully automated handover system
- Automatic queue management
- Automatic assignment

## Benefits

### For Workflow
- ✅ **Visibility** - See all pending handovers
- ✅ **Priority** - Focus on important work
- ✅ **Continuity** - No work gets lost
- ✅ **Efficiency** - Faster agent transitions

### For Quality
- ✅ **Prevent degradation** - Handover before quality drops
- ✅ **Fresh start** - New agent with clean context
- ✅ **Better outcomes** - Maintain effectiveness
- ✅ **Learning** - Track what works

## Related Documentation

- `docs/development/workflow/AGENT_HANDOVER_PATTERNS.md` - Handover patterns
- `docs/development/workflow/AGENT_EFFECTIVENESS_DEGRADATION.md` - Degradation analysis
- `docs/architecture/DEVELOPMENT_METRICS_ARCHITECTURE.md` - Metrics for tracking

