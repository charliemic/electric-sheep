# Metrics Collection - Regular Workflow Checklist

**Status**: ✅ Ready for Regular Use  
**Purpose**: Quick checklist to ensure metrics are captured as part of regular workflow

## What's Already Automated ✅

- ✅ **Session Tracking**: Auto-starts in pre-work check
- ✅ **Build Metrics**: Auto-captured via `gradle-wrapper-build.sh`
- ✅ **Test Metrics**: Auto-captured via `gradle-wrapper-test.sh`
- ✅ **Scope Creep**: Auto-tracked and checked

## What Needs Manual Action ⚠️

- ⚠️ **Prompt Capture**: Use simple helper for significant work
- ⚠️ **Agent Usage**: Can be captured with prompts or separately

## Quick Checklist

### At Start of Work
- [ ] Run `./scripts/pre-work-check.sh` (auto-starts session tracking)
- [ ] Review metrics reminder in pre-work check output

### During Work
- [ ] For significant work (> 5-10 min): Capture prompt
  ```bash
  ./scripts/metrics/capture-prompt-simple.sh "Your prompt" --model sonnet
  ```
- [ ] Use build/test wrappers (auto-capture metrics):
  ```bash
  ./scripts/gradle-wrapper-build.sh
  ./scripts/gradle-wrapper-test.sh
  ```

### End of Work Session
- [ ] Check scope creep: `./scripts/metrics/capture-metrics.sh session check`
- [ ] View dashboard (optional): `./scripts/metrics/generate-dashboard.sh`

## When to Capture Prompts

### ✅ Capture For:
- Feature implementation
- Bug fixes (especially complex ones)
- Architecture changes
- Documentation tasks
- Any task taking > 5-10 minutes

### ❌ Skip For:
- Quick typo fixes
- Simple one-line changes
- Routine formatting
- Very minor refactoring

## Integration Status

### ✅ Fully Integrated
- Pre-work check includes metrics reminder
- Build/test wrappers auto-capture
- Session tracking auto-starts
- Cursor rules include metrics guidance

### 📋 Documentation
- Quick reference in `scripts/metrics/README.md`
- Workflow guide in `docs/development/workflow/METRICS_WORKFLOW_INTEGRATION.md`
- Setup guide in `docs/development/workflow/METRICS_COLLECTION_SETUP.md`

## Summary

**Current State**: ✅ Ready for regular use

**What's Needed**:
- Agents remember to capture prompts for significant work
- Use simple helper (`capture-prompt-simple.sh`)
- Use build/test wrappers instead of direct gradle commands

**No Additional Automation Needed**: The system is designed to be simple and non-intrusive. Manual prompt capture is intentional - it ensures we only capture meaningful work, not every tiny change.

