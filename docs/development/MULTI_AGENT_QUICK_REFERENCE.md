# Multi-Agent Workflow Quick Reference

**For detailed guidelines, see:** `MULTI_AGENT_WORKFLOW.md`

## 🚨 CRITICAL: Before Any Work

```bash
# 1. Check you're NOT on main
git status

# 2. If on main, create feature branch IMMEDIATELY
git checkout -b feature/<agent-id>-<feature-name>

# 3. Pull latest main
git checkout main && git pull origin main && git checkout feature/<agent-id>-<feature-name>

# 4. Check coordination
./scripts/check-agent-coordination.sh
```

## Branch Naming

**Format:** `<type>/<agent-id>-<description>`

**Examples:**
- `feature/agent-1-test-helpers`
- `fix/agent-2-env-bug`
- `refactor/agent-3-component-cleanup`

## File Ownership

| Agent | Owns |
|-------|------|
| **Agent 1** (Test) | `scripts/test-*.sh`, `test-automation/`, `docs/testing/` |
| **Agent 2** (Env) | `app/.../config/`, `docs/development/` (env-related) |
| **Agent 3** (Design) | `app/.../ui/components/`, `app/.../ui/theme/`, `docs/architecture/` |
| **Agent 4** (Data) | `app/.../data/repositories/`, `app/.../data/models/` |

**Shared Files (coordinate first):**
- `app/.../ui/screens/LandingScreen.kt`
- `app/.../ElectricSheepApplication.kt`
- `app/.../data/DataModule.kt`
- `app/build.gradle.kts`, `build.gradle.kts`

## Workflow Checklist

### Before Starting
- [ ] On feature branch (not `main`)
- [ ] Branch name follows convention
- [ ] Pulled latest `main`
- [ ] Checked `AGENT_COORDINATION.md`
- [ ] Documented your work in coordination doc

### During Work
- [ ] Commit frequently
- [ ] Push branch regularly
- [ ] Update coordination doc if scope changes
- [ ] Test in isolation

### Before Merge
- [ ] Rebased on latest `main`
- [ ] All conflicts resolved
- [ ] All tests passing
- [ ] Documentation updated
- [ ] Coordination doc updated to "Complete"

## Quick Commands

```bash
# Check coordination
./scripts/check-agent-coordination.sh

# Sync with main
git fetch origin && git rebase origin/main

# Create feature branch
git checkout -b feature/<agent-id>-<feature-name>

# Push branch
git push -u origin feature/<agent-id>-<feature-name>
```

## Cursor Rules

Cursor automatically enforces:
- ✅ Branch check before changes
- ✅ Branch naming convention
- ✅ Coordination doc check
- ✅ File ownership warnings

See: `.cursor/rules/branching.mdc`

## Need Help?

- **Full Guidelines:** `docs/development/MULTI_AGENT_WORKFLOW.md`
- **Coordination:** `docs/development/AGENT_COORDINATION.md`
- **Evaluation:** `docs/development/MULTI_AGENT_WORKFLOW_EVALUATION.md`

