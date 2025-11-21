# Automation Quick Reference

**Date**: 2025-11-21  
**Purpose**: Quick reference for automated workflow tools

## 🚀 Quick Start

### Before Starting Work (MANDATORY)
```bash
./scripts/pre-work-check.sh
```
**Blocks work if errors found** - Must fix before proceeding.

### After PR Merge (AUTOMATED)
```bash
./scripts/post-merge-cleanup.sh <pr-number>
```
**Automatically cleans up** - Deletes merged branches, removes worktrees.

## 📋 Available Scripts

| Script | Purpose | When to Use |
|--------|---------|-------------|
| `pre-work-check.sh` | Enforces pre-work checklist | **Before ANY work** |
| `discover-rules.sh <keyword>` | Finds relevant cursor rules | Before implementation |
| `post-merge-cleanup.sh <pr>` | Cleans up after merge | **After PR merge** |
| `generate-prompt-template.sh <type> <task>` | Generates prompt template | When starting new work |
| `check-agent-coordination.sh` | Checks for conflicts | Before modifying files |

## 🔒 Enforcement

### Automatic Blocks
- ❌ **Pre-commit hook**: Blocks commits to main
- ❌ **Pre-work check**: Blocks work if on main or errors found

### Automatic Warnings
- ⚠️ **Pre-commit hook**: Warns about temp branches, coordination issues
- ⚠️ **Pre-work check**: Warns about remote updates, uncommitted changes

## 📖 Full Documentation

- **Complete guide**: `docs/development/AUTOMATED_WORKFLOW.md`
- **Analysis**: `docs/learning/WEEKLY_WORKFLOW_ANALYSIS.md`

## 💡 Key Benefits

**Before**: Checklists existed but weren't enforced
**After**: **Automated enforcement** - no need to remember

- Pre-work check **blocks** work if errors
- Pre-commit hook **blocks** commits to main
- Post-merge cleanup **automated**
- Rule discovery **integrated**

