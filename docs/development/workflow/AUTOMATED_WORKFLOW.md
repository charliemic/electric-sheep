# Automated Workflow Enforcement

**Date**: 2025-11-21  
**Purpose**: Document automated workflow enforcement tools

## Overview

All workflow recommendations are now **automated and enforced**, not just documented. You don't need to remember checklists - the tools enforce them automatically.

## Automation Scripts

### 1. Pre-Work Check (`scripts/pre-work-check.sh`)

**Purpose**: Enforces mandatory pre-work checklist before starting any work.

**Usage**:
```bash
./scripts/pre-work-check.sh
```

**What it checks**:
- ✅ Not on main branch (blocks if on main)
- ✅ Latest main pulled (warns if remote has updates)
- ✅ Coordination checked (runs `check-agent-coordination.sh`)
- ✅ Relevant rules discovered (suggests applicable cursor rules)
- ✅ Available tools identified (lists automation scripts)
- ✅ Working directory clean (warns about uncommitted changes)

**Enforcement**: 
- **Errors block work** - Must fix before proceeding
- **Warnings allow work** - But should be addressed

**Integration**: 
- Referenced in `.cursor/rules/branching.mdc`
- Should be run before ANY work starts

### 2. Rule Discovery (`scripts/discover-rules.sh`)

**Purpose**: Discovers relevant cursor rules for a given task.

**Usage**:
```bash
./scripts/discover-rules.sh <search-term>
```

**Examples**:
```bash
./scripts/discover-rules.sh error
./scripts/discover-rules.sh branch
./scripts/discover-rules.sh test
./scripts/discover-rules.sh accessibility
```

**What it does**:
- Searches all cursor rules for matching keywords
- Shows key points (CRITICAL, REQUIRED, MUST items)
- Lists relevant rules with file paths

**Integration**: 
- Called by `pre-work-check.sh`
- Should be run before implementation to find relevant rules

### 3. Post-Merge Cleanup (`scripts/post-merge-cleanup.sh`)

**Purpose**: Automatically cleans up after PR merge.

**Usage**:
```bash
./scripts/post-merge-cleanup.sh <pr-number>
```

**What it does**:
- Checks other active work (worktrees and branches)
- Verifies PR merge status (if GitHub CLI available)
- Switches to main and pulls latest
- Deletes merged branches
- Removes associated worktrees
- Cleans up temp branches
- Prunes stale worktree references

**Safety**: 
- Shows active work before cleanup
- Asks for confirmation before deleting
- Only deletes merged branches

**Integration**: 
- Referenced in `.cursor/rules/branching.mdc` and `.cursor/rules/cicd.mdc`
- Should be run after every PR merge

### 4. Prompt Template Generator (`scripts/generate-prompt-template.sh`)

**Purpose**: Generates structured prompt templates with mandatory checklists.

**Usage**:
```bash
./scripts/generate-prompt-template.sh <task-type> <task-description>
```

**Examples**:
```bash
./scripts/generate-prompt-template.sh feature user-authentication
./scripts/generate-prompt-template.sh fix login-bug
./scripts/generate-prompt-template.sh refactor error-handling
```

**What it generates**:
- Pre-work checklist section
- Rule discovery section
- Coordination check section
- Implementation section
- Testing section
- Documentation section
- Post-merge cleanup section

**Integration**: 
- Use when creating prompts for new work
- Ensures all mandatory steps are included

## Git Hooks

### Pre-Commit Hook (`.git/hooks/pre-commit`)

**Purpose**: Enforces checks before commits.

**What it does**:
- ❌ **Blocks commits to main** - Cannot commit directly to main
- ⚠️ Runs coordination check (warns but doesn't block)
- ⚠️ Warns about temp branch names

**Enforcement**: 
- **Hard block** on main branch commits
- **Soft warnings** for coordination and temp branches

### Post-Checkout Hook (`.git/hooks/post-checkout`)

**Purpose**: Reminds to run pre-work check after branch changes.

**What it does**:
- Shows reminder to run `pre-work-check.sh` after branch switch
- Only shows on branch switches (not file checkouts)

## Workflow Integration

### Standard Workflow

1. **Start Work**:
   ```bash
   ./scripts/pre-work-check.sh  # MANDATORY
   ```

2. **Discover Rules**:
   ```bash
   ./scripts/discover-rules.sh <task-keyword>
   ```

3. **Check Coordination**:
   ```bash
   ./scripts/check-agent-coordination.sh
   ```

4. **Generate Prompt Template** (optional):
   ```bash
   ./scripts/generate-prompt-template.sh feature <task-name>
   ```

5. **Work** (with rules referenced explicitly)

6. **Test**:
   ```bash
   ./gradlew test  # MANDATORY before commit
   ```

7. **Commit** (pre-commit hook runs automatically)

8. **After PR Merge**:
   ```bash
   ./scripts/post-merge-cleanup.sh <pr-number>
   ```

### Cursor Rules Integration

All cursor rules now reference automation:

- **`.cursor/rules/branching.mdc`**: References `pre-work-check.sh` and `post-merge-cleanup.sh`
- **`.cursor/rules/cicd.mdc`**: References `post-merge-cleanup.sh`
- **`.cursor/rules/testing.mdc`**: Already enforces test requirements

## Benefits

### Before Automation
- ❌ Checklists existed but weren't enforced
- ❌ Users had to remember to run checks
- ❌ Same mistakes repeated (19 fix commits in one week)
- ❌ Coordination checked reactively (after conflicts)
- ❌ Rules existed but weren't referenced

### After Automation
- ✅ Checklists are **automatically enforced**
- ✅ Pre-commit hook **blocks** commits to main
- ✅ Pre-work check **blocks** work if errors found
- ✅ Coordination checked **proactively** (before work)
- ✅ Rules **discovered automatically** and referenced
- ✅ Cleanup **automated** after merge

## Migration Path

### For Existing Work

1. **Run pre-work check now**:
   ```bash
   ./scripts/pre-work-check.sh
   ```

2. **Fix any errors** reported

3. **Continue with work** following automated workflow

### For New Work

1. **Always start with**:
   ```bash
   ./scripts/pre-work-check.sh
   ```

2. **Use prompt template generator**:
   ```bash
   ./scripts/generate-prompt-template.sh feature <task-name>
   ```

3. **Follow the generated template**

## Troubleshooting

### Pre-Work Check Fails

**Error: "You are on 'main' branch"**
- Solution: Create feature branch: `git checkout -b feature/<task-name>`

**Error: "Coordination check found conflicts"**
- Solution: Use worktree: `./scripts/create-worktree.sh <task-name>`

**Warning: "Remote main has updates"**
- Solution: Pull latest: `git pull origin main`

### Pre-Commit Hook Blocks Commit

**Error: "Cannot commit directly to 'main' branch"**
- Solution: Create feature branch first

### Post-Merge Cleanup Issues

**Script can't find PR**
- Solution: Provide PR number: `./scripts/post-merge-cleanup.sh <pr-number>`
- Or run manual cleanup steps

**Script wants to delete active branch**
- Solution: Review the list carefully, only confirm if branch is truly merged

## Related Documentation

- `docs/learning/WEEKLY_WORKFLOW_ANALYSIS.md` - Analysis that led to automation
- `.cursor/rules/branching.mdc` - Branch workflow rules (updated with automation)
- `.cursor/rules/cicd.mdc` - CI/CD rules (updated with automation)
- `scripts/check-agent-coordination.sh` - Coordination check script

