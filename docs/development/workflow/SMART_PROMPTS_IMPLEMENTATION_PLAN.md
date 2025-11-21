# Smart Prompts Architecture - Implementation Plan

**Date**: 2025-01-20  
**Status**: Ready for Implementation  
**Goal**: Implement smart prompts optimizations to reduce prompt overhead

## Current State

**What Exists:**
- ✅ Comprehensive optimization analysis
- ✅ Architecture documentation
- ✅ Quick reference guide
- ✅ Learning project context identified

**What's Missing:**
- ❌ Cursor rule codifying the evaluation logic
- ❌ Updated thresholds in existing rules
- ❌ Multi-agent context detection guidance
- ❌ Learning project context in rules

## Implementation Requirements

### 1. Create Smart Prompts Cursor Rule

**File:** `.cursor/rules/smart-prompts.mdc`

**Purpose:** Codify the smart prompts evaluation logic so AI agents automatically follow it.

**Content:**
- Rule-required check (always PROCEED)
- Multi-agent context detection
- Learning project thresholds (85% PROCEED)
- Evaluation criteria for PROCEED/QUICK_CONFIRM/FULL_REVIEW/BLOCK
- Examples for each workflow stage

### 2. Update Existing Rules

**Files to Update:**
- `.cursor/rules/branching.mdc` - Add learning project context
- `.cursor/rules/frequent-commits.mdc` - Update thresholds
- `.cursor/rules/cicd.mdc` - Update CI/CD thresholds

**Changes:**
- Reference smart prompts rule
- Note learning project context
- Update prompt thresholds

### 3. Update Prompt Templates

**File:** `docs/development/workflow/CURSOR_AGENT_PROMPT_TEMPLATES.md`

**Changes:**
- Note that smart prompts may skip some prompts
- Update expected agent responses
- Add learning project context

## Implementation Steps

### Step 1: Create Smart Prompts Rule

**Action:** Create `.cursor/rules/smart-prompts.mdc` with:
- Evaluation flow (rule-required check → context evaluation → decision)
- Learning project thresholds
- Multi-agent context detection
- Examples for commits, pushes, PRs, etc.

**Estimated Time:** 30 minutes

### Step 2: Update Branching Rule

**Action:** Update `.cursor/rules/branching.mdc` to:
- Reference smart prompts rule
- Note learning project context
- Clarify when to PROCEED vs prompt

**Estimated Time:** 15 minutes

### Step 3: Update Frequent Commits Rule

**Action:** Update `.cursor/rules/frequent-commits.mdc` to:
- Reference smart prompts rule
- Note 85% commits → PROCEED threshold
- Clarify routine vs complex commits

**Estimated Time:** 15 minutes

### Step 4: Update CI/CD Rule

**Action:** Update `.cursor/rules/cicd.mdc` to:
- Reference smart prompts rule
- Note 70% CI/CD changes → PROCEED threshold
- Clarify routine vs major changes

**Estimated Time:** 15 minutes

### Step 5: Update Prompt Templates

**Action:** Update `docs/development/workflow/CURSOR_AGENT_PROMPT_TEMPLATES.md` to:
- Note smart prompts may skip prompts
- Update expected responses
- Add learning project context

**Estimated Time:** 20 minutes

### Step 6: Test and Validate

**Action:** Test the implementation:
- Verify rule-required actions PROCEED
- Verify routine operations PROCEED
- Verify risky operations still prompt
- Verify multi-agent context detection

**Estimated Time:** 30 minutes

## Files to Create/Modify

### New Files
1. `.cursor/rules/smart-prompts.mdc` - Main evaluation rule

### Modified Files
1. `.cursor/rules/branching.mdc` - Add smart prompts reference
2. `.cursor/rules/frequent-commits.mdc` - Update thresholds
3. `.cursor/rules/cicd.mdc` - Update thresholds
4. `docs/development/workflow/CURSOR_AGENT_PROMPT_TEMPLATES.md` - Update templates

## Success Criteria

**Implementation is successful when:**
- ✅ Smart prompts rule exists and is referenced
- ✅ Routine commits (85%) → PROCEED automatically
- ✅ Routine pushes (80%) → PROCEED automatically
- ✅ Starting work (100%) → PROCEED automatically (rule-required)
- ✅ CI/CD routine fixes (70%) → PROCEED automatically
- ✅ Risky operations still prompt appropriately
- ✅ Multi-agent context detected and thresholds adjusted
- ✅ Learning project context reflected in all rules

## Testing Checklist

**Before PR:**
- [ ] Smart prompts rule created
- [ ] Existing rules updated
- [ ] Prompt templates updated
- [ ] All rules reference smart prompts
- [ ] Learning project context documented
- [ ] Multi-agent context detection documented

**After PR:**
- [ ] Test routine commit → Should PROCEED
- [ ] Test routine push → Should PROCEED
- [ ] Test starting work → Should PROCEED (rule-required)
- [ ] Test risky operation → Should prompt
- [ ] Test multi-agent context → Should lower thresholds
- [ ] Verify no regressions

## Risk Assessment

**Low Risk:**
- Creating new rule (doesn't affect existing behavior)
- Updating documentation (doesn't change code)
- Adding references (doesn't change logic)

**Medium Risk:**
- Updating thresholds in existing rules (could affect behavior)
- Need to test thoroughly

**Mitigation:**
- Start with new rule only
- Test each change incrementally
- Keep old behavior as fallback
- Monitor for regressions

## Next Steps

1. **Create smart prompts rule** (Step 1)
2. **Update existing rules** (Steps 2-4)
3. **Update prompt templates** (Step 5)
4. **Test and validate** (Step 6)
5. **Create PR** with all changes
6. **Monitor and adjust** based on outcomes

## Related Documentation

- `docs/development/workflow/SMART_PROMPTS_ARCHITECTURE.md` - Complete architecture
- `docs/development/workflow/SMART_PROMPTS_COMPREHENSIVE_OPTIMIZATION.md` - Optimization analysis
- `docs/development/workflow/SMART_PROMPTS_QUICK_REFERENCE.md` - Quick reference

