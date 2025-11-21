# Simplified Branching Rule - Example

This document shows how the branching rule could be simplified from **301 lines** to **~80 lines** while maintaining all critical information.

## Current: 301 lines
- Extensive examples
- Duplicate explanations
- Inline code blocks
- Detailed cleanup procedures

## Proposed: ~80 lines
- Focus on requirements
- Link to scripts/examples
- Reference other rules
- Keep only critical info

---

## Simplified Version (Example)

```markdown
---
alwaysApply: true
---

# Branch Isolation and Multi-Agent Workflow

## When This Applies
Before making ANY changes to code.

## Critical Requirements

### Before Starting Work
- ✅ **Never work on `main` branch** - Always create feature branch first
- ✅ **Run pre-work check**: `./scripts/pre-work-check.sh` (validates branch, coordination, tools)
- ✅ **Use proper naming**: `feature/<description>`, `fix/<description>`, etc.

### Branch Creation
```bash
# Standard branch
git checkout -b feature/<task-description>

# For isolation (recommended for shared files)
./scripts/create-worktree.sh <task-name>
```

### During Work
- ✅ **Commit frequently** (every 15-30 min, WIP commits OK)
- ✅ **Check coordination** before modifying shared files
- ✅ **Run tests** before committing: `./gradlew test`

### After Merge
- ✅ **Run cleanup**: `./scripts/post-merge-cleanup.sh <pr-number>`
- ✅ **Check other work** before deleting branches/worktrees

## Implementation

**Automated helpers:**
- `scripts/pre-work-check.sh` - Validates branch, coordination, discovers rules
- `scripts/create-worktree.sh` - Creates isolated worktree
- `scripts/post-merge-cleanup.sh` - Cleans up after merge
- `scripts/check-agent-coordination.sh` - Checks for file conflicts

**Coordination:**
- Check `docs/development/AGENT_COORDINATION.md` before modifying shared files
- Update coordination doc when work status changes

## Related Rules
- `.cursor/rules/frequent-commits.mdc` - Commit frequency guidelines
- `.cursor/rules/repository-maintenance.mdc` - Cleanup procedures
- `.cursor/rules/cicd.mdc` - CI/CD workflow

## Error Prevention

**STOP and fix if:**
- ❌ On `main` branch → Create feature branch
- ❌ Tests failing → Fix before proceeding
- ❌ Shared file conflict → Check coordination, use worktree

## Examples

**See code examples:**
- Branch creation: `scripts/create-worktree.sh`
- Pre-work validation: `scripts/pre-work-check.sh`
- Cleanup automation: `scripts/post-merge-cleanup.sh`

**See documentation:**
- Multi-agent workflow: `docs/development/MULTI_AGENT_WORKFLOW.md`
- Coordination: `docs/development/AGENT_COORDINATION.md`
```

## Comparison

### What We Removed
- ❌ Inline code examples (moved to scripts)
- ❌ Duplicate explanations (referenced other rules)
- ❌ Detailed cleanup steps (automated in script)
- ❌ Extensive examples (link to code instead)

### What We Kept
- ✅ All critical requirements
- ✅ When rule applies
- ✅ Error prevention
- ✅ Links to implementation

### What We Added
- ✅ Clear structure (When/Requirements/Implementation/Related)
- ✅ Links to scripts (single source of truth)
- ✅ References to other rules (no duplication)

## Benefits

1. **Faster to read**: 80 lines vs 301 lines (73% reduction)
2. **Easier to maintain**: Update script, not rule
3. **Less duplication**: Reference other rules/docs
4. **More actionable**: Focus on requirements, not examples

## Migration Path

1. Create simplified version alongside current
2. Test with AI agents
3. Measure impact (read time, comprehension)
4. Replace if successful
5. Apply pattern to other rules

---

**Note**: This is an example. The actual simplified rule would be refined based on usage patterns and feedback.

