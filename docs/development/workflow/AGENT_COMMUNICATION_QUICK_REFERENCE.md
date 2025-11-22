# Agent Communication - Quick Reference

**One-page guide for agent communication protocol**

## Before Starting Work

```bash
# 1. Run pre-work check (MANDATORY)
./scripts/pre-work-check.sh

# 2. Query for conflicts (if modifying files)
./scripts/query-agent-coordination.sh check-file <file-path>

# 3. Document your work
# Add entry to docs/development/workflow/AGENT_COORDINATION.md
```

## Common Queries

```bash
# Check if file is in use
./scripts/query-agent-coordination.sh check-file <file>

# Find who owns a file
./scripts/query-agent-coordination.sh who-owns <file>

# List all active work
./scripts/query-agent-coordination.sh list-active

# Check multiple files for conflicts
./scripts/query-agent-coordination.sh check-conflicts <file1> <file2> ...
```

## Coordination Entry Format

```markdown
### Task: <task-name>
- **Role**: [PLANNING] / [EXECUTION] / [VERIFICATION] (optional)
- **Branch**: `feature/<task-name>`
- **Status**: In Progress
- **Files Modified**: 
  - `file1.kt`
  - `file2.kt`
- **Conflicts**: None / List conflicts
```

## Conflict Resolution

**If conflict detected:**
1. Run: `./scripts/query-agent-coordination.sh who-owns <file>`
2. Check coordination doc
3. Choose strategy:
   - **Sequential work**: One task completes first
   - **Split work**: Different parts of file
   - **Worktree isolation**: Use git worktree

## Sync Reminders

**Before committing (if > 1 hour):**
```bash
git fetch origin
git log HEAD..origin/main --oneline
# If behind, sync: git rebase origin/main
```

## Quick Links

- Full Protocol: `docs/development/workflow/AGENT_COMMUNICATION_PROTOCOL.md`
- Coordination Doc: `docs/development/workflow/AGENT_COORDINATION.md`
- Query Tool: `scripts/query-agent-coordination.sh`

