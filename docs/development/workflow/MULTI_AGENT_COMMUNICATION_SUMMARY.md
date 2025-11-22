# Multi-Agent Communication Summary

**Last Updated**: 2025-01-20  
**Status**: Active  
**Purpose**: Quick reference for agent communication and safety protocols

## Quick Start

### 1. Document Your Work (MANDATORY)
```bash
# Before starting, add entry to:
docs/development/workflow/AGENT_COORDINATION.md
```

### 2. Query Before Modifying (RECOMMENDED)
```bash
# Check if file is in use
./scripts/query-agent-coordination.sh check-file <file-path>

# Find file owner
./scripts/query-agent-coordination.sh who-owns <file-path>

# List all active work
./scripts/query-agent-coordination.sh list-active
```

### 3. Check Coordination (AUTOMATED)
```bash
# Pre-work check (includes coordination)
./scripts/pre-work-check.sh

# Coordination check
./scripts/check-agent-coordination.sh
```

## Communication Tools

### Query Script
**Location**: `scripts/query-agent-coordination.sh`

**Commands:**
- `check-file <file>` - Check if file is part of active work
- `list-active` - List all active work entries
- `check-conflicts <file>...` - Check for conflicts with multiple files
- `who-owns <file>` - Find which task owns a file
- `status <task-name>` - Get status of a specific task

### Coordination Document
**Location**: `docs/development/workflow/AGENT_COORDINATION.md`

**Contains:**
- Active work entries
- File ownership information
- Conflict resolutions
- Shared files list

## Common Workflows

### Starting New Work
1. Query for conflicts: `./scripts/query-agent-coordination.sh check-conflicts <files>`
2. Document work: Add entry to `AGENT_COORDINATION.md`
3. Create worktree: `./scripts/create-worktree.sh <task-name>`
4. Run pre-work check: `./scripts/pre-work-check.sh`

### Checking File Before Modification
```bash
# Quick check
./scripts/query-agent-coordination.sh check-file <file-path>

# If conflict detected:
# 1. Check coordination doc
# 2. Document conflict
# 3. Choose resolution strategy
```

### Finding File Owner
```bash
./scripts/query-agent-coordination.sh who-owns <file-path>

# Output shows:
# - Task name
# - Branch
# - Status
# - Files in task
```

## Safety Features

### 1. File Ownership Checks
- ✅ Query before modifying shared files
- ✅ Automatic conflict detection
- ✅ Clear ownership information

### 2. Work Documentation
- ✅ All work must be documented
- ✅ Status tracking (In Progress / Complete)
- ✅ File lists for each task

### 3. Conflict Resolution
- ✅ Document conflicts in coordination doc
- ✅ Choose resolution strategy
- ✅ Track resolutions

## Related Documentation

- `docs/development/workflow/AGENT_COORDINATION.md` - Coordination document
- `docs/development/workflow/AGENT_COMMUNICATION_PROTOCOL.md` - Full protocol
- `docs/development/workflow/MULTI_AGENT_WORKFLOW.md` - Workflow guidelines
- `scripts/query-agent-coordination.sh` - Query tool
- `scripts/check-agent-coordination.sh` - Coordination check

