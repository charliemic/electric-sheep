# Metrics Collection - Workflow Integration Guide

**Status**: ✅ Ready for Regular Use  
**Purpose**: Make metrics collection a seamless part of daily workflow

## Current State

### ✅ Fully Automated
- **Session Tracking**: Auto-starts in pre-work check
- **Build Metrics**: Auto-captured via `gradle-wrapper-build.sh`
- **Test Metrics**: Auto-captured via `gradle-wrapper-test.sh`
- **Complexity/Accessibility**: Available via `capture-metrics.sh all`

### ⚠️ Manual (But Easy)
- **Prompt Capture**: Simple helper available, but requires manual call
- **Agent Usage**: Can be captured with prompts or separately

## Making It Part of Regular Workflow

### For Agents: Quick Reference

**At Start of Work:**
```bash
# 1. Pre-work check (auto-starts session)
./scripts/pre-work-check.sh
```

**During Work:**
```bash
# 2. Capture prompts as you work (when you make significant requests)
./scripts/metrics/capture-prompt-simple.sh "Your prompt" --model sonnet

# 3. Build/test (auto-captures metrics)
./scripts/gradle-wrapper-build.sh
./scripts/gradle-wrapper-test.sh
```

**End of Work:**
```bash
# 4. Check scope creep
./scripts/metrics/capture-metrics.sh session check
```

### When to Capture Prompts

**✅ DO Capture:**
- Significant feature requests
- Complex tasks or architecture changes
- Bug fixes that required investigation
- Documentation tasks
- Any task that took meaningful time/effort

**❌ DON'T Capture:**
- Quick formatting/typo fixes
- Very simple one-line changes
- Routine refactoring (unless significant)

**Rule of Thumb**: If you spent more than 5-10 minutes on it, capture it.

### Integration Points

1. **Pre-Work Check** - Reminds about metrics
2. **Build/Test Wrappers** - Auto-capture metrics
3. **Session Tracking** - Auto-starts and tracks scope
4. **Manual Helpers** - Easy prompt capture when needed

## Recommendations

### 1. Add Reminder to Pre-Work Check
- Remind agents to capture prompts during work
- Show quick reference for prompt capture

### 2. Add to Workflow Documentation
- Include in agent guidelines
- Add to workflow templates
- Reference in cursor rules

### 3. Make It Habitual
- Capture prompts for significant work
- Use simple helper (`capture-prompt-simple.sh`)
- Don't overthink it - quick capture is better than perfect capture

## Next Steps

1. ✅ Add reminder to pre-work check
2. ✅ Update workflow documentation
3. ✅ Add to cursor rules (optional reminder)
4. ✅ Test workflow integration

