# Agent Handover Patterns

**Date**: 2025-11-21  
**Status**: Active Guidelines  
**Purpose**: Define when and how to handover work between agents

## When to Create a Handover vs Close Session

### Decision Tree

**First, determine: Is there more work to do that needs a new agent?**

- **YES** → Create handover (Case B)
- **NO** → Close session (Case A)

### ✅ Create Handover When (Case B):

**More work exists, but need new agent for efficiency:**

1. **Effectiveness Threshold Exceeded**
   - Conversation turns > 100
   - Time elapsed > 4 hours
   - Context tokens > 100,000
   - Error rate > 3 failures
   - Repetition count > 3
   - **Action**: Create handover, add to queue

2. **Major Milestone Completed + More Work Exists**
   - Feature fully implemented and deployed
   - Major refactoring complete
   - Significant infrastructure change done
   - **AND** next phase work is planned
   - **Action**: Create handover for next phase

3. **Task Completion + Next Phase Planned**
   - All planned work for current task is done
   - Next phase work is clearly defined
   - Context switch needed for different work
   - **Action**: Create handover for next phase

4. **Natural Breakpoint + More Work**
   - Logical stopping point reached
   - Next work requires different expertise
   - Time to reassess priorities
   - **AND** more work is planned
   - **Action**: Create handover

### ✅ Close Session When (Case A):

**Work is essentially finished, no immediate next steps:**

1. **Phase Complete**
   - All planned work for phase is done
   - No immediate next steps defined
   - Work is complete and deployed
   - **Action**: Close session, update HANDOVER_PROMPT.md for future reference

2. **Task Complete + No Next Phase**
   - All planned work is done
   - No immediate follow-up work
   - Everything committed and deployed
   - **Action**: Close session

3. **Simple Work Complete**
   - Single task finished
   - No related work pending
   - No effectiveness issues
   - **Action**: Close session

**REQUIRED for Closed Session:**
- ✅ **PR merged** (or work committed to feature branch)
- ✅ **Branch deleted** (if merged)
- ✅ **Worktree removed** (if used)
- ✅ **Working directory clean** (`git status` shows clean)
- ✅ **No uncommitted changes**
- ✅ **No untracked files** (or intentionally ignored)
- ✅ **Coordination doc updated** (status marked complete)

**See:** `.cursor/rules/session-lifecycle.mdc` for complete session end checklist

### ❌ Don't Create Handover When:

1. **Mid-Task**
   - Work is incomplete
   - Still debugging/fixing
   - Not at a logical stopping point
   - **Action**: Continue work

2. **Simple Changes**
   - Single file changes
   - Quick fixes
   - Documentation-only updates
   - **Action**: Complete and close

3. **Continuous Work**
   - Same task continuing
   - No context switch needed
   - Same agent can continue
   - **Action**: Continue work

## Handover Document Structure

### Required Sections

1. **Context** - What work was being done
2. **Current State** - What's completed, what's in progress
3. **Next Steps** - What needs to happen next
4. **Key Documents** - Important files to review
5. **Deployment Status** - What's deployed where
6. **Questions/Decisions** - Open questions or decisions needed

### Optional Sections

- **Research/Findings** - Important discoveries
- **Blockers** - What's preventing progress
- **Related Work** - Other PRs/work that's related
- **Testing Status** - What's been tested, what needs testing

## Handover Process

### Step 1: Complete Current Work

- ✅ All changes committed
- ✅ Tests passing (if applicable)
- ✅ Documentation updated
- ✅ Deployment complete (if applicable)

### Step 2: Create Handover Document

- ✅ Update `HANDOVER_PROMPT.md` (or create new)
- ✅ Document current state clearly
- ✅ List next steps explicitly
- ✅ Reference key files/documents

### Step 3: Commit Handover

- ✅ Commit handover document
- ✅ Push to feature branch
- ✅ Create/update PR if needed

### Step 4: New Agent Starts

- ✅ Read handover document
- ✅ Review current state
- ✅ Understand next steps
- ✅ Continue from handover point

## Example: Metrics Deployment Handover

**When**: After successful deployment to staging and production

**Handover Document Should Include**:
- ✅ What was deployed (metrics schema)
- ✅ Deployment status (staging ✅, production ✅)
- ✅ What's working (schema, workflows)
- ✅ What's next (metrics collection, dashboard)
- ✅ Key files (migrations, workflows, docs)

## Integration with Workflow

### Pre-Handover Checklist

Before creating a handover:
- [ ] All work committed
- [ ] Tests passing
- [ ] Documentation updated
- [ ] Deployment complete (if applicable)
- [ ] Next steps clear
- [ ] Key files documented

### Post-Handover Checklist

When picking up from handover:
- [ ] Read handover document
- [ ] Review current state
- [ ] Understand deployment status
- [ ] Check key files
- [ ] Verify next steps
- [ ] Start from handover point

## Benefits

### For Agents

- ✅ Clear context for continuation
- ✅ No duplicate work
- ✅ Understand what's done
- ✅ Know what's next

### For Project

- ✅ Continuity of work
- ✅ Knowledge preservation
- ✅ Clear progress tracking
- ✅ Better planning

## Related Documentation

- `docs/development/workflow/MULTI_AGENT_WORKFLOW.md` - Multi-agent workflow
- `HANDOVER_PROMPT.md` - Current handover document template
- `.cursor/rules/branching.mdc` - Branch isolation rules

