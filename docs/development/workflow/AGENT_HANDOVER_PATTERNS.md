# Agent Handover Patterns

**Date**: 2025-11-21  
**Status**: Active Guidelines  
**Purpose**: Define when and how to handover work between agents

## When to Create a Handover

### ✅ Create Handover When:

1. **Major Milestone Completed**
   - Feature fully implemented and deployed
   - Major refactoring complete
   - Significant infrastructure change done
   - Example: Metrics schema deployed to production

2. **Task Completion**
   - All planned work for a task is done
   - Ready for next phase of work
   - Context switch needed for different work

3. **Natural Breakpoint**
   - Logical stopping point reached
   - Next work requires different expertise
   - Time to reassess priorities

4. **Deployment Complete**
   - Changes deployed to staging/production
   - Validation complete
   - Ready for next development cycle

5. **Complex Work Completed**
   - Multi-step task finished
   - Integration work done
   - Testing/validation complete

### ❌ Don't Create Handover When:

1. **Mid-Task**
   - Work is incomplete
   - Still debugging/fixing
   - Not at a logical stopping point

2. **Simple Changes**
   - Single file changes
   - Quick fixes
   - Documentation-only updates

3. **Continuous Work**
   - Same task continuing
   - No context switch needed
   - Same agent can continue

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

