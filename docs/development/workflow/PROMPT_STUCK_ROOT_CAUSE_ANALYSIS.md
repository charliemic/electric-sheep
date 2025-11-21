# Prompt Stuck Root Cause Analysis

**Date**: 2025-01-20  
**Purpose**: Identify root causes of stuck prompts based on historical session analysis

## Executive Summary

**Primary Root Cause**: **Process gaps in prompts** - Prompts focus on tasks but don't include mandatory process steps, causing agents to get stuck when they encounter workflow decisions.

**Secondary Causes**:
1. **Tool discovery failure** - 59 scripts exist but agents don't know about them
2. **Rule verbosity** - 15 comprehensive rules exist but aren't referenced in prompts
3. **Coordination gaps** - Agents get stuck when conflicts occur because coordination wasn't checked first
4. **Context structure issues** - Critical information buried in middle of prompts ("lost in the middle" effect)
5. **Ambiguous instructions** - Prompts don't provide clear success criteria or next steps

---

## Root Cause #1: Process Gaps in Prompts (PRIMARY)

### The Problem

**Pattern Observed**: Prompts are task-focused, not process-focused.

**Example**:
```
❌ Current: "Add feature X"
✅ Better: "Add feature X [with mandatory checklist]"
```

### Evidence from History

**From WEEKLY_WORKFLOW_ANALYSIS.md**:
- **19 fix commits** in one week (high waste indicator)
- **15 merge/conflict commits** (coordination gaps)
- **3x repeated fixes** for same issues (rules not enforced)

**Root Cause**: 
- Fix applied in one branch, then needed again in another
- No verification that fix was already in main
- Not checking main before starting work

### Why This Causes Stuck Prompts

1. **Agent doesn't know what to do first**
   - Prompt says "Add feature X" but doesn't say "check branch first"
   - Agent starts work, then gets stuck when it realizes it's on main
   - Agent has to ask for clarification or make wrong assumptions

2. **Agent doesn't know when to stop**
   - No clear success criteria
   - Agent keeps working past completion point
   - Agent doesn't know what "done" looks like

3. **Agent doesn't know how to recover**
   - When errors occur, no recovery strategy
   - Agent repeats failed approaches
   - Agent doesn't know when to ask for help

### Solution

**Embed process in prompts**:
```
Task: Add feature X

MANDATORY Pre-Work:
1. git status (verify not on main)
2. git fetch && git pull origin main
3. ./scripts/check-agent-coordination.sh
4. Read docs/development/AGENT_COORDINATION.md
5. Use worktree if modifying shared files

Implementation:
1. Reference relevant rules explicitly
2. Use existing scripts/tools
3. Follow patterns from docs/architecture/

Testing:
1. Run tests locally (./gradlew test)
2. Fix failing tests
3. Verify all tests pass

Success Criteria:
- Feature implemented
- Tests passing
- Documentation updated
- PR created
```

---

## Root Cause #2: Tool Discovery Failure

### The Problem

**Pattern Observed**: 59 scripts available but agents don't know about them.

**From WEEKLY_WORKFLOW_ANALYSIS.md**:
- **Available**: 59 scripts in `scripts/` directory
- **Used**: Only a few (emulator-manager, dev-reload, get-device-id)
- **Underutilized**: check-agent-coordination.sh, create-worktree.sh, md-to-google-doc.sh

### Why This Causes Stuck Prompts

1. **Agent tries to do things manually**
   - Agent doesn't know script exists
   - Agent tries to implement functionality manually
   - Agent gets stuck when manual approach fails

2. **Agent doesn't know best practices**
   - Scripts encode best practices
   - Without scripts, agent reinvents (poorly)
   - Agent gets stuck when reinvention fails

3. **Agent wastes time on solved problems**
   - Scripts solve common problems
   - Agent spends time solving already-solved problems
   - Agent gets stuck when solution doesn't match existing patterns

### Solution

**Tool discovery in workflow**:
```
Before starting work:
1. List available scripts: ls scripts/*.sh scripts/*.py
2. Check if task can be automated: grep -r "your-task" scripts/
3. Use existing scripts instead of manual steps
4. Document new scripts if you create them
```

**Automation**: `scripts/pre-work-check.sh` now lists available tools

---

## Root Cause #3: Rule Verbosity Without Reference

### The Problem

**Pattern Observed**: 15 comprehensive cursor rules exist but prompts don't reference them.

**From WEEKLY_WORKFLOW_ANALYSIS.md**:
- **Available**: 15 comprehensive cursor rules
- **Issue**: Rules are verbose but prompts don't reference them
- **Example**: `visual-first-principle.mdc` exists but test framework still uses Appium internals

### Why This Causes Stuck Prompts

1. **Agent doesn't know which rules apply**
   - 15 rules exist, agent doesn't know which to follow
   - Agent makes decisions without rule guidance
   - Agent gets stuck when decision conflicts with rules

2. **Agent doesn't know rule details**
   - Rules are comprehensive but not referenced
   - Agent makes assumptions about patterns
   - Agent gets stuck when assumptions are wrong

3. **Agent reinvents patterns**
   - Rules encode proven patterns
   - Agent creates new patterns that conflict
   - Agent gets stuck when patterns don't work

### Solution

**Explicit rule references in prompts**:
```
When implementing features, explicitly reference relevant rules:
1. "Following .cursor/rules/visual-first-principle.mdc, I will..."
2. "Per .cursor/rules/branching.mdc, I will create a feature branch..."
3. "As per .cursor/rules/error-handling.mdc, I will use Result<T>..."
```

**Automation**: `scripts/discover-rules.sh` now discovers relevant rules

---

## Root Cause #4: Coordination Gaps

### The Problem

**Pattern Observed**: Coordination doc exists but checked after conflicts occur.

**From WEEKLY_WORKFLOW_ANALYSIS.md**:
- **15 merge/conflict commits** in one week
- **Pattern**: Multiple agents working on overlapping files
- **Root Cause**: Coordination doc exists but not checked before starting

**From ISOLATION_FAILURE_REPORT.md**:
- Two agents working simultaneously without proper isolation
- No worktree isolation
- Coordination doc not updated

### Why This Causes Stuck Prompts

1. **Agent starts work without checking**
   - Agent doesn't know other agent is working on same file
   - Agent makes changes, then discovers conflict
   - Agent gets stuck trying to resolve conflict

2. **Agent doesn't know how to coordinate**
   - Coordination doc exists but workflow doesn't include it
   - Agent doesn't know to use worktree
   - Agent gets stuck when conflicts occur

3. **Agent doesn't know recovery strategy**
   - When conflicts occur, no clear recovery
   - Agent tries to resolve manually
   - Agent gets stuck when manual resolution fails

### Solution

**Proactive coordination in workflow**:
```
Before ANY file modification:
1. Run: ./scripts/check-agent-coordination.sh
2. If conflicts detected, use worktree
3. Update coordination doc: Add your work entry
4. Commit coordination doc update with your changes
```

**Automation**: `scripts/pre-work-check.sh` now runs coordination checks

---

## Root Cause #5: Context Structure Issues ("Lost in the Middle")

### The Problem

**Pattern Observed**: Critical information buried in middle of prompts.

**Research Finding**: Models struggle to retain information in the middle of long contexts ("lost in the middle" effect).

### Why This Causes Stuck Prompts

1. **Agent misses critical instructions**
   - Critical instruction in middle of prompt
   - Agent focuses on start/end, misses middle
   - Agent gets stuck when it doesn't follow critical instruction

2. **Agent doesn't know what's important**
   - All information presented equally
   - Agent doesn't know what to prioritize
   - Agent gets stuck when it prioritizes wrong thing

3. **Agent gets overwhelmed**
   - Too much context without structure
   - Agent can't process all information
   - Agent gets stuck trying to understand everything

### Solution

**Strategic prompt structure**:
```
1. [CRITICAL CONTEXT] - What needs to be done (at start)
2. [BACKGROUND] - Why and context
3. [DETAILS] - How and specifics
4. [SUCCESS CRITERIA] - How to know it's done (at end)
```

**Rule**: `.cursor/rules/prompt-stuck-prevention.mdc` now enforces this structure

---

## Root Cause #6: Ambiguous Instructions

### The Problem

**Pattern Observed**: Prompts don't provide clear success criteria or next steps.

**From WEEKLY_WORKFLOW_ANALYSIS.md**:
- **Pattern**: Task-focused prompts miss process steps
- **Issue**: Doesn't include process steps (check branch, coordinate, test)

### Why This Causes Stuck Prompts

1. **Agent doesn't know when to stop**
   - No clear success criteria
   - Agent keeps working past completion
   - Agent gets stuck in loop trying to perfect

2. **Agent doesn't know what "done" looks like**
   - Vague completion criteria
   - Agent makes assumptions
   - Agent gets stuck when assumptions are wrong

3. **Agent doesn't know next steps**
   - After completing task, what next?
   - Agent doesn't know to commit, test, document
   - Agent gets stuck waiting for next instruction

### Solution

**Explicit success criteria**:
```
Success Criteria:
- Feature implemented
- Tests passing (./gradlew test)
- Documentation updated
- PR created
- Coordination doc updated

Next Steps:
1. Commit changes
2. Push branch
3. Create PR
4. Wait for CI
```

---

## Root Cause #7: No Recovery Strategy

### The Problem

**Pattern Observed**: When errors occur, agents don't know how to recover.

**From WEEKLY_WORKFLOW_ANALYSIS.md**:
- **Pattern**: Fix commits for flaky tests
- **Issue**: Tests fail in CI, then fixed
- **Root Cause**: No "run tests before commit" enforcement

### Why This Causes Stuck Prompts

1. **Agent doesn't know how to handle errors**
   - Error occurs, no recovery strategy
   - Agent tries same approach again
   - Agent gets stuck in loop

2. **Agent doesn't know when to ask for help**
   - Agent keeps trying failed approaches
   - Agent doesn't know when to stop
   - Agent gets stuck indefinitely

3. **Agent doesn't know alternative approaches**
   - Only one approach in prompt
   - When approach fails, no alternative
   - Agent gets stuck with no options

### Solution

**Recovery strategies in prompts**:
```
If error occurs:
1. Simplify prompt - Reduce complexity
2. Reposition critical info - Move to start
3. Reduce context - Remove non-essential
4. Change approach - Try different method
5. Break into steps - Split into smaller tasks

If still stuck after 60 seconds:
- Report what was attempted
- Report where it got stuck
- Request guidance
```

**Rule**: `.cursor/rules/prompt-stuck-prevention.mdc` now includes recovery strategies

---

## Root Cause #8: Tool Call Loops

### The Problem

**Pattern Observed**: Agent gets stuck in repeated tool calls without progress.

### Why This Causes Stuck Prompts

1. **Agent calls same tool repeatedly**
   - Tool doesn't provide new information
   - Agent keeps calling hoping for different result
   - Agent gets stuck in loop

2. **Agent doesn't detect loop**
   - No loop detection mechanism
   - Agent continues calling indefinitely
   - Agent gets stuck until timeout

3. **Agent doesn't know alternative tools**
   - Only one tool approach
   - When tool fails, no alternative
   - Agent gets stuck with no options

### Solution

**Loop detection and recovery**:
```
Monitor for stuck indicators:
- Same tool called 3+ times: Possible loop
- No new information gathered: Stuck
- Repeated errors: Need different approach

Recovery:
- Stop calling same tool
- Try different approach
- Simplify task
- Request guidance
```

**Rule**: `.cursor/rules/prompt-stuck-prevention.mdc` now includes loop detection

---

## Summary: Root Cause Hierarchy

### Primary Root Cause
**Process gaps in prompts** - Prompts don't include mandatory process steps, causing agents to get stuck when they encounter workflow decisions.

### Secondary Root Causes (in order of impact)

1. **Tool discovery failure** - Agents don't know about existing scripts/tools
2. **Rule verbosity without reference** - Rules exist but aren't referenced in prompts
3. **Coordination gaps** - Agents don't check coordination before starting work
4. **Context structure issues** - Critical information buried in middle
5. **Ambiguous instructions** - No clear success criteria or next steps
6. **No recovery strategy** - Agents don't know how to recover from errors
7. **Tool call loops** - Agents get stuck in repeated tool calls

### Common Pattern

**All root causes share one pattern**: **Information exists but isn't accessible when needed**.

- Scripts exist but agents don't know about them
- Rules exist but agents don't reference them
- Coordination doc exists but agents don't check it
- Process exists but agents don't follow it

**Solution**: **Embed process and information access in prompts**, not just document it.

---

## Solutions Implemented

### 1. Pre-Work Automation
- ✅ `scripts/pre-work-check.sh` - Enforces mandatory checklist
- ✅ Blocks work if on main branch
- ✅ Checks for remote updates
- ✅ Runs coordination checks
- ✅ Discovers relevant rules
- ✅ Identifies available tools

### 2. Rule Discovery
- ✅ `scripts/discover-rules.sh` - Discovers relevant rules
- ✅ Integrated into pre-work check
- ✅ Shows key points and file paths

### 3. Coordination Automation
- ✅ `scripts/check-agent-coordination.sh` - Checks for conflicts
- ✅ Integrated into pre-work check
- ✅ Suggests worktree usage

### 4. Prompt Structure Rule
- ✅ `.cursor/rules/prompt-stuck-prevention.mdc` - Enforces structure
- ✅ Critical info at start/end
- ✅ Clear success criteria
- ✅ Recovery strategies

### 5. Post-Merge Cleanup
- ✅ `scripts/post-merge-cleanup.sh` - Automated cleanup
- ✅ Prevents branch accumulation
- ✅ Removes worktrees

---

## Recommendations

### Immediate Actions

1. **Always run pre-work check** before starting work
2. **Reference rules explicitly** in prompts
3. **Use existing scripts** instead of manual steps
4. **Check coordination** before modifying files
5. **Structure prompts** with critical info at start/end

### Long-Term Improvements

1. **Enhance prompt metrics** to track completion status
2. **Create Supabase table** for prompt session tracking
3. **Analyze patterns** in historical sessions
4. **Refine recovery strategies** based on data
5. **Build prompt templates** with embedded process

---

## Related Documentation

- `docs/learning/workflow-tools/WEEKLY_WORKFLOW_ANALYSIS.md` - Historical analysis
- `docs/development/reports/ISOLATION_FAILURE_REPORT.md` - Isolation failures
- `docs/development/reports/COLLISION_PATTERN_ANALYSIS.md` - Collision patterns
- `.cursor/rules/prompt-stuck-prevention.mdc` - Prevention rule
- `scripts/pre-work-check.sh` - Pre-work automation
- `scripts/discover-rules.sh` - Rule discovery

