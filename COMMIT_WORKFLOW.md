# IDE Optimization Commit Workflow

**Date**: 2025-01-20  
**Purpose**: Step-by-step workflow to commit IDE optimization changes

## Evaluation Summary

### ✅ Files to Commit (All Team-Shared)

**`.vscode/` workspace configuration:**
- `settings.json` - Workspace settings (Java 17, Kotlin, editor config)
- `extensions.json` - Recommended extensions
- `tasks.json` - Build tasks and script integrations
- `launch.json` - Debug configurations
- `README.md` - Configuration documentation
- `QUICK_REFERENCE.md` - Quick reference
- `SUMMARY.md` - Summary of optimizations

**Documentation:**
- `docs/development/guides/CURSOR_IDE_OPTIMIZATION.md` - Complete guide
- `docs/development/guides/JAVA_SETUP_EVALUATION.md` - Java evaluation
- `docs/development/guides/GIT_WORKTREE_VISUALIZATION.md` - Worktree guide
- `docs/development/guides/CURSOR_RELOAD_BEST_PRACTICES.md` - Reload guide
- `docs/development/guides/FIX_JAVA_VERSION.md` - Java fix guide
- `docs/development/guides/COMMIT_EVALUATION.md` - This evaluation

**Configuration:**
- `gradle.properties` - Updated with Java 17 comments

**Scripts:**
- `scripts/commit-ide-optimization.sh` - Commit helper script

### ❌ Files NOT to Commit (Already Ignored)

- `.cursor/local/` - Local Cursor config (in .gitignore)
- `.cursor/settings.json` - Personal settings (in .gitignore)
- Any personal/local files

## Commit Workflow

**Note**: Branch creation and commit rules are already enforced by `.cursor/rules/branching.mdc` and `scripts/pre-work-check.sh`. Just use standard git commands:

### Standard Git Workflow

```bash
# 1. Pre-work check (enforces branch creation if needed)
./scripts/pre-work-check.sh

# 2. Stage files
git add .vscode/
git add docs/development/guides/CURSOR_IDE_OPTIMIZATION.md
git add docs/development/guides/JAVA_SETUP_EVALUATION.md
git add docs/development/guides/GIT_WORKTREE_VISUALIZATION.md
git add docs/development/guides/CURSOR_RELOAD_BEST_PRACTICES.md
git add docs/development/guides/FIX_JAVA_VERSION.md
git add docs/development/guides/COMMIT_EVALUATION.md
git add gradle.properties

# 3. Commit (format enforced by rules)
git commit -m "feat: add Cursor IDE optimization configuration and guides

- Add .vscode workspace settings (Java 17, Kotlin, editor config)
- Add recommended extensions for team
- Add build tasks and script integrations
- Add comprehensive IDE optimization documentation
- Update gradle.properties with Java 17 configuration
- Add guides for Java setup, worktree visualization, and reload best practices"
```

## PR Workflow

### 1. Push Branch

```bash
git push -u origin feature/cursor-ide-optimization
```

### 2. Create PR

```bash
gh pr create \
  --title "feat: Cursor IDE optimization configuration" \
  --body "Adds IDE configuration and optimization guides

## Changes

- ✅ Workspace settings (.vscode/) for team consistency
- ✅ Recommended extensions for Kotlin/Java/Android development
- ✅ Build tasks integration with existing scripts
- ✅ Comprehensive documentation guides
- ✅ Java 17 configuration updates

## Benefits

- Fixes Gradle sync errors (Java version detection)
- Improves IDE efficiency with proper extensions
- Standardizes workspace configuration across team
- Provides guides for common workflows

## Testing

- [x] Settings validated
- [x] Extensions tested
- [x] Tasks verified
- [x] Documentation reviewed"
```

### 3. Wait for CI

- CI will run tests
- Verify all checks pass

### 4. Merge PR

```bash
# After approval and CI passes
gh pr merge --squash
```

### 5. Post-Merge Cleanup

```bash
# Use cleanup script
./scripts/post-merge-cleanup.sh <pr-number>

# Or manually:
git checkout main
git pull origin main
git branch -d feature/cursor-ide-optimization
```

## Archive Consideration

**No archiving needed** - All documentation is:
- ✅ Active and current
- ✅ Production-ready
- ✅ Useful for team
- ✅ Not temporary/experimental

All guides should remain in `docs/development/guides/` as active documentation.

## Verification Checklist

Before committing:
- [ ] On feature branch (not main)
- [ ] All files reviewed
- [ ] No personal/local config included
- [ ] Documentation is clear and useful
- [ ] Settings are team-appropriate
- [ ] Extensions are relevant

After commit:
- [ ] Branch pushed
- [ ] PR created
- [ ] CI passes
- [ ] PR approved
- [ ] Merged
- [ ] Cleanup completed

