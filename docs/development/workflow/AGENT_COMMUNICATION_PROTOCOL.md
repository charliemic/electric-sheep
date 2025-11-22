# Agent Communication Protocol

**Last Updated**: 2025-01-20  
**Status**: Active Guidelines  
**Purpose**: Define how agents communicate about their work to prevent conflicts and enable safe collaboration

## Overview

This protocol enables agents to:
1. **Document their work** - Track what they're working on
2. **Query other agents' work** - Check if files are in use
3. **Ask questions** - Find out about file ownership and task status
4. **Coordinate conflicts** - Resolve overlaps safely

## Core Principles

### 1. Documentation First (MANDATORY)
- ✅ **All work must be documented** in `AGENT_COORDINATION.md`
- ✅ **Update promptly** when scope changes
- ✅ **Mark complete** when work is done

### 2. Query Before Modifying (RECOMMENDED)
- ✅ **Check file ownership** before modifying shared files
- ✅ **Query for conflicts** before starting work
- ✅ **Verify isolation** - ensure worktree is used

### 3. Communication via Coordination Doc
- ✅ **All communication** happens through `AGENT_COORDINATION.md`
- ✅ **Questions** are answered by querying the coordination doc
- ✅ **Decisions** are documented in task entries

## Communication Methods

### Method 1: Query Script (AUTOMATED)

**Use the query script to check file ownership and conflicts:**

```bash
# Check if a file is part of active work
./scripts/query-agent-coordination.sh check-file app/src/main/.../LandingScreen.kt

# List all active work entries
./scripts/query-agent-coordination.sh list-active

# Check for conflicts with multiple files
./scripts/query-agent-coordination.sh check-conflicts app/build.gradle.kts build.gradle.kts

# Find which task owns a file
./scripts/query-agent-coordination.sh who-owns app/src/main/.../DataModule.kt

# Get status of a specific task
./scripts/query-agent-coordination.sh status feature-flag-sync
```

**Benefits:**
- ✅ Fast and automated
- ✅ Consistent results
- ✅ Easy to integrate into workflows

### Method 2: Coordination Document (MANUAL)

**Read and update `AGENT_COORDINATION.md` directly:**

1. **Check active work section** - See what others are working on
2. **Check shared files section** - See which files require coordination
3. **Add your entry** - Document your work
4. **Update conflicts** - Document any overlaps

**Benefits:**
- ✅ Human-readable
- ✅ Full context
- ✅ Historical record

## Common Questions and Answers

### Q: "Is this file part of your work?"

**Answer via query:**
```bash
./scripts/query-agent-coordination.sh who-owns <file-path>
```

**Answer via doc:**
- Check `AGENT_COORDINATION.md` → Active Work section
- Search for file path in task entries
- Check Shared Files section

### Q: "Can I modify this file?"

**Answer via query:**
```bash
./scripts/query-agent-coordination.sh check-file <file-path>
```

**Answer via doc:**
1. Check if file is in Active Work section
2. If yes, check task status (In Progress vs Complete)
3. If In Progress, coordinate with task owner
4. If Complete or not found, document your work and proceed

### Q: "What files are you modifying?"

**Answer via query:**
```bash
./scripts/query-agent-coordination.sh status <task-name>
```

**Answer via doc:**
- Check task entry in `AGENT_COORDINATION.md`
- Look at "Files Modified" section

### Q: "What's the status of task X?"

**Answer via query:**
```bash
./scripts/query-agent-coordination.sh status <task-name>
```

**Answer via doc:**
- Check task entry in `AGENT_COORDINATION.md`
- Look at "Status" field (In Progress / Complete)

## Workflow Integration

### Before Starting Work

1. **Query for conflicts:**
   ```bash
   ./scripts/query-agent-coordination.sh check-conflicts <file1> <file2> ...
   ```

2. **Document your work:**
   - Add entry to `AGENT_COORDINATION.md`
   - Include task name, branch, files, status

3. **Use worktree:**
   ```bash
   ./scripts/create-worktree.sh <task-name>
   ```

### During Work

1. **Update coordination doc** if scope changes
2. **Query before modifying new files:**
   ```bash
   ./scripts/query-agent-coordination.sh check-file <new-file>
   ```

3. **Document conflicts** if detected:
   - Add to task entry under "Conflicts" section
   - Document resolution strategy

### After Work

1. **Mark complete** in coordination doc
2. **Note PR number** if applicable
3. **Remove worktree** after merge

## Conflict Resolution

### When Conflict Detected

1. **Query to confirm:**
   ```bash
   ./scripts/query-agent-coordination.sh who-owns <file>
   ```

2. **Document conflict** in task entry:
   ```
   - **Conflicts**: File X is also modified by Task Y
   - **Resolution**: Sequential work - Task Y completes first
   ```

3. **Choose resolution strategy:**
   - **Sequential work**: One task completes, then next
   - **Split work**: Different parts of file
   - **Worktree isolation**: Use git worktree for complete isolation

4. **Update coordination doc** with resolution

### Resolution Strategies

**Sequential Work (Recommended for shared files):**
- Task 1 completes their changes
- Task 2 pulls latest and rebases
- Task 2 integrates their changes

**Split Work (For large files):**
- Task 1 handles part A
- Task 2 handles part B
- Coordinate merge order

**Worktree Isolation (Best for parallel work):**
- Each task uses separate worktree
- Complete file system isolation
- No conflicts possible

## Best Practices

### DO ✅

- ✅ **Query before modifying** shared files
- ✅ **Document your work** before starting
- ✅ **Update promptly** when scope changes
- ✅ **Use worktree** for file system isolation
- ✅ **Document conflicts** and resolutions
- ✅ **Mark complete** when work is done

### DON'T ❌

- ❌ Don't modify files without checking ownership
- ❌ Don't skip documentation
- ❌ Don't ignore conflicts
- ❌ Don't work on shared files without coordination
- ❌ Don't leave entries incomplete

## Examples

### Example 1: Checking File Before Modification

```bash
# Agent wants to modify LandingScreen.kt
./scripts/query-agent-coordination.sh check-file app/src/main/.../LandingScreen.kt

# Output: ⚠️ CONFLICT DETECTED
# Task: Navigation Improvements
# Branch: feature/navigation-improvements
# Status: In Progress

# Agent documents conflict and coordinates:
# - Adds entry to coordination doc
# - Documents conflict
# - Chooses sequential work strategy
```

### Example 2: Listing Active Work

```bash
# Agent wants to see what others are working on
./scripts/query-agent-coordination.sh list-active

# Output: Lists all active tasks with:
# - Task name
# - Branch
# - Status
# - Files modified
```

### Example 3: Finding File Owner

```bash
# Agent wants to know who owns DataModule.kt
./scripts/query-agent-coordination.sh who-owns app/src/main/.../DataModule.kt

# Output: File is part of:
# Task: Data Layer Refactoring
# Branch: feature/data-layer-refactor
# Status: In Progress
# Files in this task: [list]
```

## Integration with Other Tools

### Pre-Work Check

The pre-work check script (`pre-work-check.sh`) automatically:
- ✅ Checks coordination doc exists
- ✅ Runs coordination check
- ✅ Warns about shared files

### Coordination Check

The coordination check script (`check-agent-coordination.sh`) automatically:
- ✅ Validates branch naming
- ✅ Checks for shared file conflicts
- ✅ Warns about high-risk files

### Query Tool

The query tool (`query-agent-coordination.sh`) provides:
- ✅ File ownership checks
- ✅ Conflict detection
- ✅ Active work listing
- ✅ Task status queries

## Troubleshooting

### "Query script not found"
```bash
# Make sure script is executable
chmod +x scripts/query-agent-coordination.sh

# Check script exists
ls -la scripts/query-agent-coordination.sh
```

### "Coordination doc not found"
```bash
# Check path
ls -la docs/development/workflow/AGENT_COORDINATION.md

# Create if needed (should already exist)
```

### "File not found in query results"
- File may not be part of active work
- Check Shared Files section
- Document your work before proceeding

## Related Documentation

- `docs/development/workflow/AGENT_COORDINATION.md` - Coordination document
- `docs/development/workflow/MULTI_AGENT_WORKFLOW.md` - Multi-agent workflow guidelines
- `scripts/query-agent-coordination.sh` - Query tool
- `scripts/check-agent-coordination.sh` - Coordination check script
- `scripts/pre-work-check.sh` - Pre-work validation

