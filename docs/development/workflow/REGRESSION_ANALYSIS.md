# Regression Analysis: Smart Prompts Architecture

**Date**: 2025-01-20  
**Issue**: Agent asked for permission to create branch when on main, instead of doing it automatically

## What Happened

**User Request:** "Ready to end session?"

**Agent Behavior (WRONG):**
- Detected on `main` branch with uncommitted changes
- Evaluated using smart prompts architecture
- Decided to BLOCK and ask for permission
- User had to explicitly request branch creation

**Expected Behavior:**
- Detect on `main` branch with uncommitted changes
- Recognize this is a rule-required action (`.cursor/rules/branching.mdc` says "IMMEDIATELY")
- Create branch automatically without prompting

## Root Cause

The smart prompts architecture was applied **too broadly** - it evaluated ALL actions, including rule-required actions.

**Problem:**
- Rule says: "If on `main` branch, IMMEDIATELY create isolated worktree"
- Architecture evaluated "on main" as a red flag → BLOCK
- Should have recognized "IMMEDIATELY" = rule-required → PROCEED

**Missing Logic:**
- No check for rule-required actions before evaluation
- Rule-required actions should bypass risk evaluation
- Actions marked "IMMEDIATELY"/"MANDATORY"/"CRITICAL" in rules should always PROCEED

## Fix Applied

Updated `SMART_PROMPTS_ARCHITECTURE.md` to:

1. **Add rule-required check first** - Before evaluation, check if action is required by rules
2. **Rule-required actions always PROCEED** - No evaluation needed, execute immediately
3. **Only evaluate non-rule-required actions** - Risk evaluation only for actions not mandated by rules

**New Flow:**
1. Gather context
2. **Check if rule-required** → If yes, PROCEED immediately
3. If not rule-required, evaluate risk
4. Execute with appropriate prompt level

## Rule-Required Actions (Always PROCEED)

These actions are REQUIRED by rules and must always execute automatically:

- ✅ **On main branch with changes** → Create feature branch/worktree immediately (`.cursor/rules/branching.mdc`: "IMMEDIATELY")
- ✅ **Not using worktree when required** → Create worktree immediately (`.cursor/rules/branching.mdc`: "MANDATORY")
- ✅ **Pre-work checks** → Run automatically (`.cursor/rules/branching.mdc`: required)
- ✅ **Frequent commits** → Commit frequently as safety net (`.cursor/rules/frequent-commits.mdc`: "CRITICAL")

## Lessons Learned

1. **Rules take precedence** - Rule-required actions bypass risk evaluation
2. **Check rules first** - Before evaluating risk, check if action is mandated
3. **"IMMEDIATELY" means immediately** - No prompts, no evaluation, just do it
4. **Architecture must respect rules** - Smart prompts enhance rules, don't replace them

## Prevention

The architecture now includes:
- Explicit rule-required check in sub-prompt template
- Examples showing rule-required actions
- Clear documentation that rules take precedence

