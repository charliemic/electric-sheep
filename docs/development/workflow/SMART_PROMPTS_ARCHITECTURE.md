# Smart Prompts Architecture - Context-Aware Risk Assessment

**Date**: 2025-01-20  
**Purpose**: AI-driven workflow architecture that uses holistic context evaluation to prioritize controls

## Overview

Instead of file-based rules, this architecture uses **AI context evaluation** to assess risk and determine when prompts are needed. The AI agent evaluates multiple contextual factors holistically to make intelligent decisions about workflow controls.

**Project Context:**
- **Learning project** - No real end users
- **Public but experimental** - Safety less critical than production
- **Learning-focused** - Prompts that teach are valuable, but overhead still matters
- **Experimentation encouraged** - More aggressive optimization justified

**Impact on Thresholds:**
- **More aggressive PROCEED** - 85% of routine operations (vs. 70% for production)
- **Lower risk tolerance** - Can experiment freely
- **Still protect critical** - Main branch, security, shared files

## Core Principle

**Context-Driven, Not Rule-Driven**

The AI agent evaluates the **entire context** of the situation:
- What is being changed?
- How risky is it?
- What's the current state?
- What's the user's intent?
- What are the dependencies?

Then it decides **intelligently** whether to prompt, skip, or take action.

---

## Context Evaluation Framework

### AI Sub-Prompt for Risk Assessment

Instead of rigid scoring, the AI uses a **sub-prompt** to evaluate context holistically. When a user requests an action, the AI internally evaluates using this sub-prompt:

```
[CONTEXT EVALUATION SUB-PROMPT]

Evaluate the risk and determine appropriate response for this action:

Context:
- Action requested: [action description]
- Files affected: [list of files]
- Change type: [feature/fix/refactor/config/docs]
- Current branch: [branch name]
- Branch state: [clean/behind/ahead/conflicts]
- Other agents active: [yes/no, which files]
- Coordination status: [checked/not checked, conflicts]
- CI status: [passing/failing/unknown]
- User intent: [explicit request/asking for help/uncertain]
- Change complexity: [simple/moderate/complex]
- Dependencies: [what depends on this, what this depends on]

Evaluation questions:
1. What could go wrong if this proceeds?
2. Is this a routine, safe operation?
3. Are there any red flags (main branch, conflicts, shared files)?
4. Does the user seem confident or uncertain?
5. What's the scope of impact (isolated vs system-wide)?

Based on this holistic evaluation, decide:
- PROCEED: Safe, routine, user intent clear → Execute directly
- QUICK_CONFIRM: Should confirm but straightforward → Show brief summary
- FULL_REVIEW: Needs careful review → Show detailed summary with checks
- BLOCK: Dangerous, must prevent → Block and explain why

Respond with: [PROCEED/QUICK_CONFIRM/FULL_REVIEW/BLOCK] and brief reasoning.
```

### Natural Evaluation, Not Scoring

The AI evaluates context **holistically** using natural reasoning:
- Considers all factors together
- Weighs importance based on context
- Makes nuanced decisions
- Adapts to specific situations

**No rigid formulas** - just intelligent evaluation of the full context.

---

## AI Sub-Prompt Evaluation System

### How It Works

When a user requests an action, the AI **internally uses a sub-prompt** to evaluate context and determine the appropriate response level.

**Flow:**
1. **User requests action** (e.g., "Commit these changes")
2. **AI gathers context** (files, branch, state, coordination, CI, etc.)
3. **CRITICAL: Check rule-required actions first** - If action is required by rules (marked "IMMEDIATELY"/"MANDATORY"/"CRITICAL"), always PROCEED
4. **If not rule-required, AI uses sub-prompt internally** to evaluate risk holistically
5. **AI gets decision** from sub-prompt: PROCEED / QUICK_CONFIRM / FULL_REVIEW / BLOCK
6. **AI executes** with appropriate prompt level (or skips if PROCEED)

**Key Point:** **Rule-required actions always PROCEED** - they bypass evaluation because they're mandated by rules. Only non-rule-required actions are evaluated.

### Sub-Prompt Template

**This is used internally by the AI** (not shown to user):

```
[INTERNAL EVALUATION SUB-PROMPT]

STEP 1: Check if action is rule-required
- Is this action REQUIRED by a rule (e.g., "IMMEDIATELY", "MANDATORY", "CRITICAL")?
- If YES → PROCEED immediately (no evaluation needed)
- If NO → Continue to STEP 2

STEP 2: Evaluate the risk of [ACTION] given this context:

CONTEXT:
- Action: [what user wants to do]
- Files: [list of files being modified]
- Change type: [feature/fix/refactor/config/docs]
- Branch: [current branch name]
- Branch state: [clean/behind/ahead/conflicts]
- Other agents: [active? working on same files?]
- Coordination: [checked? conflicts?]
- CI status: [if applicable]
- User intent: [explicit/uncertain/asking for help]
- Complexity: [simple/moderate/complex]
- Dependencies: [what this affects]

EVALUATION:
Consider holistically (not rigid scoring):
- What could go wrong if this proceeds?
- Is this a routine, safe operation?
- Are there any red flags?
- Does the user seem confident or uncertain?
- What's the scope of impact?

DECISION:
Based on holistic evaluation, choose one:
- PROCEED: Safe, routine → Execute directly, no prompt
- QUICK_CONFIRM: Should confirm → Brief summary, single approval
- FULL_REVIEW: Needs review → Detailed summary, explicit approval
- BLOCK: Dangerous → Block action, explain why, require fix

Respond: [DECISION] - [brief reasoning]
```

**The AI uses natural reasoning** - no formulas, no rigid scores, just intelligent evaluation of the full context.

**CRITICAL:** Rule-required actions (marked with "IMMEDIATELY", "MANDATORY", "CRITICAL" in rules) always PROCEED - they bypass evaluation.

### Decision Outcomes

- **PROCEED**: Execute action directly, optionally mention what was done
- **QUICK_CONFIRM**: Show brief summary, get single approval (y/n)
- **FULL_REVIEW**: Show detailed summary with all context, get explicit approval
- **BLOCK**: Prevent action, explain why, suggest fix, require confirmation

---

## Decision Framework

### When to Prompt vs. Skip

The AI evaluates context holistically and decides:

| Evaluation Result | Prompt Type | Approval Required | Time Cost |
|-------------------|-------------|-------------------|-----------|
| **Safe to proceed** | Skip | None | 0 sec |
| **Should confirm** | Quick Summary | Single approval | 5-10 sec |
| **Needs review** | Full Summary | Explicit approval | 15-30 sec |
| **Must prevent** | Block + Explain | Explicit approval + confirmation | 30-60 sec |

### Decision Criteria (Natural Evaluation)

The AI considers:
- **Safety**: Could this cause problems? (branch, conflicts, breaking changes)
- **Complexity**: Is this straightforward or complex? (simple vs multi-step)
- **Impact**: What's the scope of impact? (isolated vs system-wide)
- **User intent**: Is the user confident or uncertain? (explicit vs asking for help)
- **Context**: What's the current state? (clean vs messy, synced vs behind)

---

## Context Evaluation Examples

### Example 1: Rule-Required Action (Always PROCEED)

**User Request:** "End session" (while on main branch with changes)

**AI Sub-Prompt Evaluation:**
```
[INTERNAL EVALUATION]

STEP 1: Check if action is rule-required
- Action: End session, but on main branch with uncommitted changes
- Rule check: `.cursor/rules/branching.mdc` says "If on main branch, IMMEDIATELY create isolated worktree"
- This is a RULE-REQUIRED action (marked with "IMMEDIATELY")
- DECISION: PROCEED immediately - create branch/worktree automatically

[AI executes: Creates feature branch immediately, no prompt]
```

**User sees:** `✅ Created feature branch and moved changes` (no prompt)

### Example 2: PROCEED - Skip Prompt

**User Request:** "Add a comment to this function"

**AI Sub-Prompt Evaluation:**
```
[INTERNAL EVALUATION]

STEP 1: Check if action is rule-required
- Action: Add comment
- Not rule-required → Continue to STEP 2

STEP 2: Evaluate the risk of adding a comment given this context:

CONTEXT:
- Action: Add comment to function
- Files: [single file, one line]
- Change type: Documentation
- Branch: feature/xyz (not main)
- Branch state: Clean
- Other agents: None active
- Coordination: No conflicts
- User intent: Explicit request
- Complexity: Very simple
- Dependencies: None

EVALUATION:
- What could go wrong? Nothing - comment can't break code
- Is this routine? Yes, very routine documentation change
- Any red flags? No - safe branch, isolated change
- User confidence? High - explicit request
- Impact scope? Minimal - single line, no dependencies

DECISION: PROCEED - Safe, routine operation, no risk

[AI executes: Adds comment directly]
```

**User sees:** `✅ Added comment to function` (no prompt)

---

### Example 2: QUICK_CONFIRM - Brief Summary

**User Request:** "Commit these changes"

**AI Sub-Prompt Evaluation:**
```
[INTERNAL EVALUATION]

Evaluate the risk of committing changes given this context:

CONTEXT:
- Action: Commit changes
- Files: 3 files (new feature)
- Change type: New feature
- Branch: feature/new-settings (not main)
- Branch state: Clean, up to date
- Other agents: None on these files
- Coordination: No conflicts
- User intent: Explicit request
- Complexity: Moderate
- Dependencies: Isolated feature

EVALUATION:
- What could go wrong? Low risk - isolated feature, clean branch
- Is this routine? Yes, but commit is significant action
- Any red flags? No - safe branch, no conflicts
- User confidence? High - explicit request
- Impact scope? Moderate - 3 files, but isolated

DECISION: QUICK_CONFIRM - Should confirm but straightforward

[AI shows brief summary]
```

**User sees:** `📋 Commit 3 files (new feature) to feature/new-settings? (y/n)`

**On approval:** AI commits directly

---

### Example 3: FULL_REVIEW - Detailed Prompt

**User Request:** "I'm modifying MoodManagementScreen.kt"

**AI Sub-Prompt Evaluation:**
```
[INTERNAL EVALUATION]

Evaluate the risk of modifying MoodManagementScreen.kt given this context:

CONTEXT:
- Action: Modify shared screen file
- Files: MoodManagementScreen.kt (shared file)
- Change type: UI changes
- Branch: feature/restore-design (not main)
- Branch state: Clean
- Other agents: YES - other agent working on same file
- Coordination: Not checked, collision risk
- User intent: Mentioned file, not explicit about coordination
- Complexity: Moderate-high
- Dependencies: Affects navigation, components

EVALUATION:
- What could go wrong? HIGH RISK - collision with other agent, shared file
- Is this routine? No - shared file with active conflict
- Any red flags? YES - other agent on same file, no coordination check
- User confidence? Medium - mentioned file but didn't check coordination
- Impact scope? High - shared file, affects multiple components

DECISION: FULL_REVIEW - High risk, needs detailed review and coordination check

[AI shows full summary with coordination warning]
```

**User sees:**
```
📋 SUMMARY: Modify shared file MoodManagementScreen.kt
   • File: app/.../MoodManagementScreen.kt (HIGH RISK)
   • Other agent working on same file (collision risk)
   • Recommendation: Use worktree for isolation
   • Coordination: Check AGENT_COORDINATION.md first
   
   Proceed with modification? (y/n)
```

---

### Example 4: BLOCK - Prevent Dangerous Action

**User Request:** "Commit these changes"

**AI Sub-Prompt Evaluation:**
```
[INTERNAL EVALUATION]

Evaluate the risk of committing changes given this context:

CONTEXT:
- Action: Commit changes
- Files: Multiple files including build.gradle.kts
- Change type: Configuration/build changes
- Branch: main (CRITICAL)
- Branch state: Clean but on main
- Other agents: Unknown
- Coordination: Not checked
- User intent: Explicit but may not realize on main
- Complexity: High (build changes)
- Dependencies: Affects entire build system

EVALUATION:
- What could go wrong? CRITICAL - committing to main, build changes could break everything
- Is this routine? NO - never commit to main, especially build changes
- Any red flags? YES - on main branch, build files, system-wide impact
- User confidence? Medium - explicit request but may not realize danger
- Impact scope? CRITICAL - build changes affect entire system

DECISION: BLOCK - Must prevent, dangerous operation

[AI blocks and explains why]
```

**User sees:**
```
❌ CRITICAL: Cannot commit to main branch
   • Current branch: main
   • Changes include: build.gradle.kts (high risk)
   • Impact: Could break entire build system
   
   Action required:
   1. Create feature branch first
   2. Move changes to feature branch
   3. Then commit
   
   Would you like me to create a feature branch and move these changes? (y/n)
```

---

## Sub-Prompt Implementation

### For AI Agent (Internal Evaluation)

When user requests an action, AI should **internally use this sub-prompt** to evaluate:

```
[INTERNAL EVALUATION SUB-PROMPT]

Evaluate the risk of [ACTION] given this context:

CONTEXT:
- Action: [what user wants to do]
- Files: [list of files being modified]
- Change type: [feature/fix/refactor/config/docs]
- Branch: [current branch name]
- Branch state: [clean/behind/ahead/conflicts]
- Other agents: [active? working on same files?]
- Coordination: [checked? conflicts?]
- CI status: [if applicable]
- User intent: [explicit/uncertain/asking for help]
- Complexity: [simple/moderate/complex]
- Dependencies: [what this affects]

EVALUATION:
Consider holistically:
- What could go wrong if this proceeds?
- Is this a routine, safe operation?
- Are there any red flags?
- Does the user seem confident or uncertain?
- What's the scope of impact?

DECISION:
Choose one based on holistic evaluation:
- PROCEED: Safe, routine → Execute directly
- QUICK_CONFIRM: Should confirm → Brief summary
- FULL_REVIEW: Needs review → Detailed summary
- BLOCK: Dangerous → Block and explain

Respond: [DECISION] - [brief reasoning]
```

**Then:** AI executes with appropriate prompt level (or skips if PROCEED)

---

## Workflow Integration

### 1. Starting Work

**AI Context Evaluation:**
```
Evaluate:
- Current branch (main = critical risk)
- Uncommitted changes (conflicts possible)
- Other agents active (coordination risk)
- Files to be modified (shared files = high risk)
- User intent (explicit task = lower risk)
```

**Decision:**
- **Low risk**: Proceed, mention branch name
- **Medium risk**: Quick check, show summary
- **High risk**: Full pre-work check prompt
- **Critical risk**: Block, require branch creation

### 2. Committing Changes

**AI Context Evaluation:**
```
Evaluate:
- Files changed (shared files = high risk)
- Change type (config/build = high risk)
- Branch state (main = critical risk)
- Test status (failing tests = high risk)
- Commit message quality (unclear = medium risk)
```

**Decision:**
- **Low risk**: Commit directly with generated message
- **Medium risk**: Show summary, quick approval
- **High risk**: Full summary, run tests first, explicit approval
- **Critical risk**: Block, explain why, require fixes

### 3. Pushing Branch

**AI Context Evaluation:**
```
Evaluate:
- Commits to push (many = medium risk)
- Branch state (behind main = medium risk)
- CI status (if PR exists, failing = high risk)
- User intent (explicit = lower risk)
```

**Decision:**
- **Low risk**: Push directly
- **Medium risk**: Show summary, quick approval
- **High risk**: Show summary, check CI first, explicit approval
- **Critical risk**: Block, sync with main first

### 4. Creating PR

**AI Context Evaluation:**
```
Evaluate:
- Branch state (behind main = high risk)
- Test status (failing = high risk)
- Conflicts (merge conflicts = critical risk)
- PR description quality (missing = medium risk)
- User intent (explicit = lower risk)
```

**Decision:**
- **Low risk**: Create PR directly with auto-generated description
- **Medium risk**: Show summary, quick approval
- **High risk**: Full summary, check readiness first, explicit approval
- **Critical risk**: Block, fix issues first

### 5. Modifying Files

**AI Context Evaluation:**
```
Evaluate:
- File type (shared files = high risk)
- Other agents (same files = high risk)
- Change scope (system-wide = high risk)
- Dependencies (core infrastructure = high risk)
- User confidence (uncertain = higher risk)
```

**Decision:**
- **Low risk**: Proceed, mention file
- **Medium risk**: Quick check, show summary
- **High risk**: Full coordination check, worktree recommendation
- **Critical risk**: Block, require worktree/isolation

---

## Implementation: AI Agent Behavior

### Agent Should Always:

1. **Evaluate context first** (internally, before acting)
2. **Calculate risk score** (using multi-dimensional factors)
3. **Choose appropriate action** (skip/quick/full/block)
4. **Execute with appropriate prompt level**
5. **Learn from patterns** (adjust risk assessment over time)

### Agent Should Never:

1. **Blindly follow file patterns** (use context, not just file names)
2. **Prompt for everything** (evaluate risk first)
3. **Skip critical safety checks** (always check branch, state)
4. **Ignore user intent** (if user explicitly requests, lower risk)

---

## Context Factors (For Sub-Prompt)

The AI considers these factors holistically in the sub-prompt evaluation:

### Change Scope
- Single file, isolated → Low concern
- Multiple files, related → Moderate concern
- Architecture/system-wide → High concern
- Build/configuration → High concern

### Change Type
- Documentation → Low concern
- New feature (isolated) → Low-moderate concern
- Bug fix → Moderate concern
- Refactoring → Moderate-high concern
- Configuration → High concern
- Security/auth → High concern

### Dependencies
- None → Low concern
- Own feature only → Low concern
- Other components → Moderate concern
- Core infrastructure → High concern
- Shared services → High concern

### State
- Clean branch, up to date → Low concern
- Behind main → Moderate concern
- On main branch → CRITICAL (always block)
- Uncommitted on main → CRITICAL (always block)
- Conflicts possible → High concern

### Coordination
- Isolated work → Low concern
- Other agents active → Moderate concern
- Same files modified → High concern
- No coordination doc → Moderate-high concern
- Shared infrastructure → High concern

### Complexity
- Very simple → Low concern
- Simple → Low concern
- Moderate → Moderate concern
- Complex → High concern
- Unclear requirements → High concern

### User Confidence
- Explicit request → Lower concern
- Clear intent → Lower concern
- Asking for help → Moderate concern
- Uncertain → Higher concern
- Struggling → High concern

**Note:** These are **considerations for the AI**, not rigid scores. The AI evaluates them naturally in context.

---

## Prompt Response Levels

### PROCEED - Execute Directly

**When AI evaluates:** Safe, routine operation

**Action:** Execute directly, optionally mention what was done

**Example:**
```
✅ Added comment to function
✅ Committed: "docs: add comment to validateInput"
```

### QUICK_CONFIRM - Brief Summary

**When AI evaluates:** Should confirm but straightforward

**Action:** Show brief summary, single approval

**Example:**
```
📋 Commit 3 files (new feature) to feature/xyz? (y/n)
```

### FULL_REVIEW - Detailed Summary

**When AI evaluates:** Needs careful review, higher risk

**Action:** Show detailed summary with all context, explicit approval

**Example:**
```
📋 SUMMARY: Modify shared file MoodManagementScreen.kt
   • File: app/.../MoodManagementScreen.kt (HIGH RISK)
   • Other agent working on same file (collision risk)
   • Recommendation: Use worktree for isolation
   • Coordination: Check AGENT_COORDINATION.md first
   
   Proceed with modification? (y/n)
```

### BLOCK - Prevent Action

**When AI evaluates:** Dangerous, must prevent

**Action:** Block action, explain why, require explicit confirmation + fix

**Example:**
```
❌ CRITICAL: Cannot commit to main branch
   • Current branch: main
   • Changes include: build.gradle.kts (high risk)
   • Impact: Could break entire build system
   
   Action required:
   1. Create feature branch first
   2. Move changes to feature branch
   3. Then commit
   
   Would you like me to create a feature branch and move these changes? (y/n)
```

---

## Adaptive Learning

### Pattern Recognition

The AI should learn from patterns and adjust sub-prompt evaluation:

1. **User behavior**: If user always approves similar actions, adjust evaluation
2. **Issue patterns**: If certain contexts cause issues, weight those factors more
3. **Success patterns**: If certain contexts always succeed, weight those factors less
4. **Context patterns**: Learn which combinations of factors are actually risky

### Evaluation Refinement

Over time, the AI should:

- **Refine sub-prompt evaluation** based on actual outcomes
- **Learn which factors matter most** in different contexts
- **Personalize** to user's workflow patterns
- **Adapt evaluation criteria** based on what actually causes issues
- **Improve decision accuracy** through experience

---

## Example Workflows

### Workflow 1: Simple Documentation Change

**User:** "Add a comment explaining this function"

**AI Sub-Prompt Evaluation:**
```
[INTERNAL EVALUATION]

CONTEXT:
- Action: Add comment
- Files: Single file, one line
- Change type: Documentation
- Branch: feature/xyz (not main)
- State: Clean
- Other agents: None
- User intent: Explicit

EVALUATION:
- What could go wrong? Nothing - comment can't break code
- Routine? Yes, very routine
- Red flags? None
- User confidence? High
- Impact? Minimal

DECISION: PROCEED
```

**User sees:** `✅ Added comment to function` (no prompt)

---

### Workflow 2: New Feature (Isolated)

**User:** "I'm adding a new settings screen"

**AI Sub-Prompt Evaluation:**
```
[INTERNAL EVALUATION]

CONTEXT:
- Action: Add new settings screen
- Files: New files, isolated feature
- Change type: New feature
- Branch: feature/settings (not main)
- State: Clean
- Other agents: Active but different files
- User intent: Clear

EVALUATION:
- What could go wrong? Low risk - isolated feature
- Routine? Yes, but new feature is significant
- Red flags? None - isolated, clean branch
- User confidence? High - clear intent
- Impact? Moderate - affects navigation

DECISION: QUICK_CONFIRM
```

**User sees:** `📋 Adding new settings screen (affects navigation). Proceed? (y/n)`

---

### Workflow 3: Modifying Shared Screen

**User:** "I need to modify MoodManagementScreen.kt"

**AI Sub-Prompt Evaluation:**
```
[INTERNAL EVALUATION]

CONTEXT:
- Action: Modify shared screen file
- Files: MoodManagementScreen.kt (shared)
- Change type: UI changes
- Branch: feature/xyz (not main)
- State: Clean but other agent active
- Other agents: YES - working on same file
- Coordination: Not checked
- User intent: Mentioned file, not explicit about coordination

EVALUATION:
- What could go wrong? HIGH - collision with other agent
- Routine? No - shared file with active conflict
- Red flags? YES - other agent on same file
- User confidence? Medium - didn't check coordination
- Impact? High - shared file, multiple components

DECISION: FULL_REVIEW
```

**User sees:**
```
📋 SUMMARY: Modify shared file MoodManagementScreen.kt
   • File: app/.../MoodManagementScreen.kt (HIGH RISK)
   • Other agent working on same file (collision risk)
   • Recommendation: Use worktree for isolation
   • Coordination: Check AGENT_COORDINATION.md first
   
   Proceed with modification? (y/n)
```

---

### Workflow 4: Build Configuration Change

**User:** "Update Gradle dependencies"

**AI Sub-Prompt Evaluation:**
```
[INTERNAL EVALUATION]

CONTEXT:
- Action: Update Gradle dependencies
- Files: build.gradle.kts, app/build.gradle.kts
- Change type: Configuration
- Branch: feature/xyz (not main)
- State: Clean
- Other agents: Unknown
- Coordination: Not checked
- User intent: Explicit but may not realize full impact
- Complexity: High
- Dependencies: Affects entire build

EVALUATION:
- What could go wrong? HIGH - build changes can break everything
- Routine? No - build changes are risky
- Red flags? YES - system-wide impact, coordination unknown
- User confidence? Medium - explicit but may not realize impact
- Impact? CRITICAL - affects entire build system

DECISION: FULL_REVIEW
```

**User sees:**
```
📋 SUMMARY: Update Gradle dependencies
   • Files: build.gradle.kts, app/build.gradle.kts (HIGH RISK)
   • Impact: Affects entire build system
   • Recommendation: Test build after changes
   • Coordination: Check if other agents modifying build files
   
   Proceed with dependency update? (y/n)
```

---

## Integration with Existing Tools

### Pre-Work Check Integration

**AI should use pre-work check results:**
- If pre-work check shows errors → Critical risk
- If pre-work check shows warnings → High risk
- If pre-work check passes → Lower risk

### Coordination Check Integration

**AI should use coordination check results:**
- If no conflicts → Lower risk
- If conflicts detected → High risk
- If shared files → High risk

### CI Status Integration

**AI should check CI status:**
- If CI passing → Lower risk for push/PR
- If CI failing → High risk, block or warn
- If CI unknown → Medium risk

---

## Benefits of Context-Aware Architecture

### 1. Intelligent Decision Making

- **Not rule-based**: Adapts to actual context
- **Multi-dimensional**: Considers all factors
- **Holistic**: Sees the big picture, not just file names

### 2. Speed Optimization

- **Skips unnecessary prompts**: Low risk = no prompt
- **Quick approvals**: Medium risk = brief summary
- **Full protection**: High risk = detailed checks

### 3. Safety Without Friction

- **Prevents major issues**: Critical risk = blocks action
- **Allows fast workflow**: Low risk = proceeds directly
- **Balanced approach**: Risk-appropriate prompts

### 4. Adaptive Learning

- **Learns from patterns**: Adjusts over time
- **Personalizes**: Adapts to user's workflow
- **Improves accuracy**: Better risk assessment over time

---

## Implementation Guidelines

### For AI Agents

**When user requests action:**

1. **Gather context** (files, branch, state, coordination, etc.)
2. **CRITICAL: Check if rule-required first** - If rule says "IMMEDIATELY"/"MANDATORY"/"CRITICAL", always PROCEED
3. **If not rule-required, use sub-prompt to evaluate** (internally, natural reasoning)
4. **Get decision** (PROCEED/QUICK_CONFIRM/FULL_REVIEW/BLOCK)
5. **Execute with appropriate prompt level** (or skip if PROCEED)
6. **Learn from outcome** (refine evaluation for future)

**Sub-prompt evaluation should be:**
- **Fast**: < 1 second evaluation
- **Holistic**: All factors considered together
- **Natural**: Uses AI reasoning, not rigid formulas
- **Adaptive**: Learns from patterns
- **Transparent**: User can see reasoning if needed (optional)

**CRITICAL: Rule-required actions always PROCEED** - they bypass evaluation because they're mandated by rules.

### For Users

**You can:**
- Trust the AI's risk assessment
- Override if needed (explicit request)
- Provide context to help AI evaluate (mention concerns, ask for help)

**The AI will:**
- Evaluate risk automatically
- Skip prompts when safe
- Prompt when needed
- Block when critical

---

## Related Documentation

- `docs/development/workflow/CURSOR_AGENT_PROMPT_TEMPLATES.md` - Prompt templates
- `docs/development/workflow/PROMPT_TEMPLATES_IMPACT_ANALYSIS.md` - Impact analysis
- `docs/development/reports/ISOLATION_FAILURE_REPORT.md` - Isolation failure case
- `docs/development/reports/MOOD_SCREEN_COLLISION_REPORT.md` - Collision case
- `scripts/check-agent-coordination.sh` - Coordination check (context input)

