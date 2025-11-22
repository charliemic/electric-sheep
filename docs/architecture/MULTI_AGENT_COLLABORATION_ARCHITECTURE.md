# Multi-Agent Collaboration Architecture

**Last Updated**: 2025-01-20  
**Status**: Active Architecture  
**Purpose**: Top-level architecture document for multi-agent collaboration system

## Overview

This document describes the complete architecture of the multi-agent collaboration system, including isolation, communication, coordination, and safety mechanisms.

## System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│              Multi-Agent Collaboration System                    │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌──────────────────┐         ┌──────────────────┐              │
│  │  Agent 1        │         │  Agent 2        │              │
│  │  (Worktree 1)   │         │  (Worktree 2)   │              │
│  │                 │         │                 │              │
│  │  Branch:        │         │  Branch:        │              │
│  │  feature/task-1 │         │  feature/task-2 │              │
│  └────────┬────────┘         └────────┬────────┘              │
│           │                            │                        │
│           │                            │                        │
│           └────────────┬───────────────┘                        │
│                        │                                         │
│                        ▼                                         │
│         ┌──────────────────────────────┐                         │
│         │   Coordination Layer         │                         │
│         │                              │                         │
│         │  ┌────────────────────────┐ │                         │
│         │  │ Coordination Document  │ │                         │
│         │  │ (AGENT_COORDINATION.md) │ │                         │
│         │  └────────────────────────┘ │                         │
│         │                              │                         │
│         │  ┌────────────────────────┐ │                         │
│         │  │ Query Script           │ │                         │
│         │  │ (query-agent-          │ │                         │
│         │  │  coordination.sh)      │ │                         │
│         │  └────────────────────────┘ │                         │
│         └──────────────┬───────────────┘                         │
│                        │                                         │
│                        ▼                                         │
│         ┌──────────────────────────────┐                         │
│         │   Safety & Validation Layer  │                         │
│         │                              │                         │
│         │  ┌────────────────────────┐ │                         │
│         │  │ Pre-Work Check         │ │                         │
│         │  │ (pre-work-check.sh)   │ │                         │
│         │  │ • Branch validation   │ │                         │
│         │  │ • Conflict detection  │ │                         │
│         │  │ • Sync status         │ │                         │
│         │  └────────────────────────┘ │                         │
│         │                              │                         │
│         │  ┌────────────────────────┐ │                         │
│         │  │ Pre-PR Check          │ │                         │
│         │  │ (pre-pr-check.sh)    │ │                         │
│         │  │ • Branch sync        │ │                         │
│         │  │ • File conflicts     │ │                         │
│         │  │ • Coordination doc    │ │                         │
│         │  └────────────────────────┘ │                         │
│         └──────────────┬───────────────┘                         │
│                        │                                         │
│                        ▼                                         │
│         ┌──────────────────────────────┐                         │
│         │   Git Repository (Shared)    │                         │
│         │                              │                         │
│         │  • Main branch               │                         │
│         │  • Feature branches          │                         │
│         │  • Worktree references       │                         │
│         │  • Coordination doc          │                         │
│         └──────────────────────────────┘                         │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

## Core Components

### 1. Isolation Layer

**Purpose**: Complete file system isolation between agents

**Components:**
- **Git Worktrees**: Each agent has separate directory
- **Branch Isolation**: Each agent works on separate branch
- **File System Separation**: No shared workspace conflicts

**Implementation:**
```bash
# Agent creates isolated worktree
git worktree add ../electric-sheep-<task-name> -b feature/<task-name>
cd ../electric-sheep-<task-name>
```

**Benefits:**
- ✅ Complete isolation (no untracked file conflicts)
- ✅ No branch switching needed
- ✅ Easy cleanup after merge

### 2. Coordination Layer

**Purpose**: Enable agents to communicate and coordinate work

**Components:**

#### 2.1 Coordination Document
- **Location**: `docs/development/workflow/AGENT_COORDINATION.md`
- **Purpose**: Shared context store for agent work
- **Contents**: Active work entries, file ownership, conflict resolutions
- **Update Mechanism**: Git-based (real-time via pull)

#### 2.2 Query Script
- **Location**: `scripts/query-agent-coordination.sh`
- **Purpose**: Automated queries for file ownership and conflicts
- **Commands**:
  - `check-file <file>` - Check if file is in use
  - `who-owns <file>` - Find file owner
  - `list-active` - List all active work
  - `check-conflicts <file>...` - Check multiple files
  - `status <task>` - Get task status

**Communication Flow:**
```
Agent A → Query Script → Coordination Doc → Agent B's Work Info
```

### 3. Safety & Validation Layer

**Purpose**: Prevent conflicts and ensure safe operations

**Components:**

#### 3.1 Pre-Work Check
- **Location**: `scripts/pre-work-check.sh`
- **Purpose**: Validate environment before starting work
- **Checks**:
  - ✅ Not on main branch
  - ✅ Latest main pulled
  - ✅ Rule/workflow updates checked
  - ✅ **Automated conflict detection** (checks modified files)
  - ✅ Coordination doc exists
  - ✅ Working directory clean

#### 3.2 Pre-PR Check
- **Location**: `scripts/pre-pr-check.sh`
- **Purpose**: Validate branch before PR creation
- **Checks**:
  - ✅ Branch sync status (errors if behind main)
  - ✅ Uncommitted changes (warns)
  - ✅ File conflicts (checks modified files)
  - ✅ Coordination documentation (verifies branch is documented)
  - ✅ Test readiness (reminds to run tests)

#### 3.3 Coordination Check
- **Location**: `scripts/check-agent-coordination.sh`
- **Purpose**: Validate branch naming and file ownership
- **Checks**:
  - ✅ Branch naming convention
  - ✅ Shared file warnings
  - ✅ Worktree usage recommendations

### 4. Communication Protocol

**Purpose**: Standardized communication between agents

**Components:**

#### 4.1 Documentation Requirements
- **Mandatory**: All work must be documented in coordination doc
- **Format**: Standardized entry format with role tags
- **Updates**: Prompt updates when scope changes

#### 4.2 Query Interface
- **Automated**: Query script for file ownership
- **Manual**: Read coordination doc directly
- **Real-time**: Updates propagate via git

#### 4.3 Conflict Resolution
- **Detection**: Automated via pre-work check
- **Resolution Strategies**:
  1. Sequential work (one task completes first)
  2. Split work (different parts of file)
  3. Worktree isolation (complete isolation)

## Data Flow

### Starting Work Flow

```
1. Agent starts work
   ↓
2. Run pre-work-check.sh
   ├─ Check branch (not main)
   ├─ Check remote updates
   ├─ Check rule/workflow updates
   ├─ Run coordination check
   └─ **Automated conflict detection** (NEW)
       ├─ Get modified files
       ├─ Query each file for conflicts
       └─ Error if conflicts found
   ↓
3. Query for conflicts (if modifying files)
   ├─ query-agent-coordination.sh check-file <file>
   └─ Shows file ownership if conflict
   ↓
4. Document work in coordination doc
   ├─ Add entry with task, branch, files
   ├─ Add role tag if applicable
   └─ Set status to In Progress
   ↓
5. Create worktree (if not already)
   ├─ git worktree add ../electric-sheep-<task>
   └─ Work in isolated directory
```

### During Work Flow

```
1. Agent modifies files
   ↓
2. Check sync status (every 2-4 hours)
   ├─ git fetch origin
   ├─ git log HEAD..origin/main
   └─ Sync if behind: git rebase origin/main
   ↓
3. Query before modifying new files
   ├─ query-agent-coordination.sh check-file <new-file>
   └─ Document if scope changes
   ↓
4. Commit frequently (every 15-30 min)
   ├─ Sync reminder (if > 1 hour)
   └─ WIP commits for incomplete work
```

### Before PR Flow

```
1. Agent completes work
   ↓
2. Run pre-pr-check.sh
   ├─ Check branch sync (errors if behind)
   ├─ Check uncommitted changes
   ├─ Check file conflicts
   ├─ Check coordination doc
   └─ Remind about tests
   ↓
3. Final sync with main
   ├─ git fetch origin
   ├─ git rebase origin/main
   └─ Resolve conflicts if any
   ↓
4. Run tests
   ├─ ./gradlew test
   └─ Fix failures
   ↓
5. Update coordination doc
   └─ Mark status as Complete
   ↓
6. Create PR
   └─ Push branch and create PR
```

## Key Design Decisions

### 1. Git Worktrees for Isolation

**Decision**: Mandatory worktree isolation for all agents

**Rationale**:
- Complete file system isolation
- Prevents untracked file conflicts
- No branch switching needed
- Easy cleanup

**Trade-offs**:
- ✅ Pros: Complete isolation, no conflicts
- ⚠️ Cons: Slightly more setup (automated via script)

### 2. Coordination Document as Shared Context

**Decision**: Use markdown document as shared context store

**Rationale**:
- Human-readable
- Version-controlled (git-based)
- Real-time updates via git pull
- No additional infrastructure needed

**Trade-offs**:
- ✅ Pros: Simple, no dependencies, version-controlled
- ⚠️ Cons: Manual updates (but query script helps)

### 3. Automated Conflict Detection

**Decision**: Integrate conflict detection into pre-work check

**Rationale**:
- Prevents conflicts before work starts
- Automated (no manual checking)
- Clear error messages
- Blocks work if conflicts found

**Trade-offs**:
- ✅ Pros: Prevents conflicts, automated
- ⚠️ Cons: Requires coordination doc to be up-to-date

### 4. Query Script for Standardized Queries

**Decision**: Separate query script for file ownership checks

**Rationale**:
- Fast and automated
- Consistent results
- Easy to integrate
- Can be called from other scripts

**Trade-offs**:
- ✅ Pros: Fast, consistent, reusable
- ⚠️ Cons: Additional script to maintain

## Integration Points

### With Git Workflow

```
Pre-Work → Worktree Creation → Work → Pre-PR → PR → Merge → Cleanup
   ↓            ↓              ↓        ↓      ↓      ↓        ↓
Pre-Check   Isolation      Commits  Pre-PR  Push  Merge  Worktree
                                                      ↓    Removal
                                                 Coordination
                                                 Doc Update
```

### With Cursor Rules

- **Branching Rule**: References communication protocol
- **Frequent Commits Rule**: Includes sync reminders
- **CI/CD Rule**: Includes pre-PR check
- **Smart Prompts**: Considers coordination context

### With Automation Scripts

- **Pre-work-check.sh**: Integrates conflict detection
- **Pre-pr-check.sh**: Validates before PR
- **Check-agent-coordination.sh**: Validates coordination
- **Query-agent-coordination.sh**: Provides query interface

## Safety Mechanisms

### Layer 1: Prevention (Pre-Work)
- ✅ Branch validation (not on main)
- ✅ Remote update checks
- ✅ **Automated conflict detection**
- ✅ Coordination doc validation

### Layer 2: Detection (During Work)
- ✅ Sync reminders (frequent commits)
- ✅ Query before modifying new files
- ✅ Coordination doc updates

### Layer 3: Validation (Pre-PR)
- ✅ Branch sync validation
- ✅ File conflict checks
- ✅ Coordination doc verification
- ✅ Test readiness reminders

### Layer 4: Enforcement (CI/CD)
- ✅ CI checks if branch is behind main
- ✅ Tests must pass
- ✅ Lint checks
- ✅ Build validation

## Communication Patterns

### Pattern 1: File Ownership Query

```
Agent A wants to modify file X
  ↓
Agent A runs: query-agent-coordination.sh check-file X
  ↓
Query script checks coordination doc
  ↓
If conflict: Shows Agent B's work details
  ↓
Agent A coordinates with Agent B (via coordination doc)
```

### Pattern 2: Conflict Resolution

```
Conflict detected
  ↓
Agent A queries: who-owns <file>
  ↓
Shows Agent B's task details
  ↓
Agent A documents conflict in coordination doc
  ↓
Choose resolution strategy:
  - Sequential work
  - Split work
  - Worktree isolation
  ↓
Update coordination doc with resolution
```

### Pattern 3: Work Documentation

```
Agent starts work
  ↓
Adds entry to coordination doc
  ├─ Task name
  ├─ Branch
  ├─ Files
  ├─ Role tag (optional)
  └─ Status: In Progress
  ↓
Other agents can query this information
  ↓
Agent updates status when complete
```

## Best Practices Implementation

### ✅ Implemented

1. **Communication Protocols** - Excellent
   - Standardized query interface
   - Coordination document format
   - Clear communication guidelines

2. **Coordination Conventions** - Excellent
   - Standardized entry format
   - Conflict resolution strategies
   - Worktree isolation conventions

3. **Structured Role Assignment** - Partial
   - Role tags supported (`[PLANNING]`, `[EXECUTION]`, `[VERIFICATION]`)
   - Optional (not enforced)

4. **Real-Time Adaptation** - Partial
   - Query script for real-time checks
   - Coordination doc updates via git
   - No automated monitoring

### ⚠️ Not Implemented (Future)

1. **Dynamic Role Negotiation** - Missing
   - No task negotiation mechanism
   - No dynamic reallocation

2. **Iterative Refinement** - Missing
   - No agent-to-agent review
   - No feedback loops

3. **Real-Time Monitoring** - Missing
   - No automated conflict detection during work
   - No alerting for deviations

## Performance Characteristics

### Query Performance
- **File ownership check**: < 1 second
- **List active work**: < 1 second
- **Conflict detection**: < 2 seconds (for multiple files)

### Pre-Work Check Performance
- **Full check**: 2-5 seconds
- **Includes**: Branch, sync, coordination, conflict detection

### Pre-PR Check Performance
- **Full check**: 2-4 seconds
- **Includes**: Sync, conflicts, coordination, tests

## Scalability

### Current Capacity
- **Agents**: Tested with 2-3 simultaneous agents
- **Files**: Handles hundreds of files
- **Tasks**: No limit on active tasks

### Limitations
- Coordination doc is markdown (manual parsing)
- No real-time notifications (polling-based)
- No automated task assignment

### Future Enhancements
- Database-backed coordination (if needed)
- Real-time notifications (webhooks, etc.)
- Automated task assignment

## Security Considerations

### Access Control
- ✅ Git-based (same access as repository)
- ✅ No additional authentication needed
- ✅ Coordination doc is version-controlled

### Data Integrity
- ✅ Git ensures coordination doc integrity
- ✅ Query script validates file existence
- ✅ Pre-work check validates environment

### Privacy
- ✅ No sensitive data in coordination doc
- ✅ Only file paths and task names
- ✅ No user credentials or secrets

## Monitoring and Observability

### Current Monitoring
- ✅ Pre-work check logs
- ✅ Pre-PR check logs
- ✅ Query script output
- ✅ Git history (coordination doc changes)

### Metrics Available
- Conflict detection rate
- Coordination doc update frequency
- Query script usage
- Pre-work check pass/fail rate

### Future Monitoring
- Real-time conflict alerts
- Task completion tracking
- Agent activity metrics

## Related Documentation

### Core Documentation
- `docs/development/workflow/MULTI_AGENT_WORKFLOW.md` - Workflow guidelines
- `docs/development/workflow/AGENT_COMMUNICATION_PROTOCOL.md` - Communication protocol
- `docs/development/workflow/AGENT_COORDINATION.md` - Coordination document

### Quick References
- `docs/development/workflow/AGENT_COMMUNICATION_QUICK_REFERENCE.md` - Quick reference
- `docs/development/workflow/MULTI_AGENT_COMMUNICATION_SUMMARY.md` - Summary

### Implementation Details
- `docs/development/workflow/PRIORITY_1_ENHANCEMENTS_IMPLEMENTED.md` - Enhancements
- `docs/development/workflow/AGENT_COMMUNICATION_PROTOCOL_EVALUATION.md` - Evaluation

### Scripts
- `scripts/pre-work-check.sh` - Pre-work validation
- `scripts/pre-pr-check.sh` - Pre-PR validation
- `scripts/query-agent-coordination.sh` - Query interface
- `scripts/check-agent-coordination.sh` - Coordination check

