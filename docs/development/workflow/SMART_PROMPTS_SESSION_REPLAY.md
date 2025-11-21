# Smart Prompts Architecture - Session Replay

**Date**: 2025-01-20  
**Purpose**: Replay a real session with smart prompts architecture to evaluate outcomes

## Session: Isolation Failure (Nov 20, 2025)

**Replaying with smart prompts architecture to see what would have happened at each step.**

---

## Step 1: User Request - "Start work on design restoration"

### Context Gathered
- **Current branch**: `main` (detected via `git branch --show-current`)
- **Uncommitted changes**: None yet (just starting)
- **Other agents**: Unknown (need to check)
- **Coordination**: Not checked

### Smart Prompts Evaluation

```
[INTERNAL EVALUATION SUB-PROMPT]

STEP 1: Check if action is rule-required
- Action: Start work (user wants to begin)
- Rule check: `.cursor/rules/branching.mdc` line 18-24
  "If on `main` branch, IMMEDIATELY create isolated worktree"
- This is a RULE-REQUIRED action (marked with "IMMEDIATELY")
- DECISION: PROCEED immediately - create branch/worktree automatically

[AI executes: Creates feature branch immediately]
```

### What Would Happen

**AI Action:**
```bash
# Automatically executes (no prompt)
git checkout -b feature/restore-design-work
# Or if worktree preferred:
git worktree add ../electric-sheep-restore-design -b feature/restore-design-work
cd ../electric-sheep-restore-design
```

**User sees:**
```
✅ Created feature branch: feature/restore-design-work
✅ Switched to feature branch
```

**Time:** 0 seconds (automatic, no prompt)

### Actual Outcome (What Happened)
- ❌ Worked on `main` branch
- ❌ No branch created
- ❌ Later caused isolation issues

### Evaluation: ✅ CORRECT
- **Rule-required action detected correctly**
- **Would have prevented the issue**
- **No prompt needed (automatic execution)**

---

## Step 2: User Request - "Modify MoodManagementScreen.kt"

### Context Gathered
- **Current branch**: `feature/restore-design-work` (after Step 1 fix)
- **File**: `MoodManagementScreen.kt` (shared file)
- **Other agents**: Need to check coordination doc
- **Coordination**: Not checked yet
- **Change type**: UI changes (restore design)

### Smart Prompts Evaluation

```
[INTERNAL EVALUATION SUB-PROMPT]

STEP 1: Check if action is rule-required
- Action: Modify shared file
- Rule check: Not explicitly rule-required (no "IMMEDIATELY" for this)
- Continue to STEP 2

STEP 2: Evaluate the risk of modifying MoodManagementScreen.kt:

CONTEXT:
- Action: Modify shared screen file
- Files: MoodManagementScreen.kt (shared file - HIGH RISK)
- Change type: UI changes
- Branch: feature/restore-design-work (not main) ✅
- Branch state: Clean ✅
- Other agents: Unknown - need to check
- Coordination: Not checked - RISK
- User intent: Explicit request
- Complexity: Moderate-high
- Dependencies: Affects navigation, components

EVALUATION:
- What could go wrong? HIGH RISK - shared file, other agent might be working on it
- Is this routine? No - shared files require coordination
- Any red flags? YES - shared file, coordination not checked
- User confidence? High - explicit request
- Impact scope? High - shared file affects multiple components

DECISION: FULL_REVIEW - High risk, needs coordination check before proceeding

[AI shows full review prompt]
```

### What Would Happen

**AI Action:**
```
📋 SUMMARY: Modify shared file MoodManagementScreen.kt
   • File: app/.../MoodManagementScreen.kt (HIGH RISK - shared file)
   • Coordination: Not checked - other agent may be working on this
   • Recommendation: Check AGENT_COORDINATION.md first
   • Alternative: Use worktree for complete isolation
   
   Options:
   1. Check coordination first (recommended)
   2. Proceed with worktree isolation
   3. Proceed anyway (not recommended)
   
   How would you like to proceed? (1/2/3)
```

**If user chooses option 1 (check coordination):**
```bash
# AI checks coordination doc
cat docs/development/AGENT_COORDINATION.md | grep -i "MoodManagementScreen"
# Finds: Other agent working on same file
```

**AI Response:**
```
⚠️ COLLISION RISK DETECTED
   • Other agent working on MoodManagementScreen.kt
   • Recommendation: Use worktree for isolation
   • Would you like me to create a worktree? (y/n)
```

**If user approves:**
```bash
# AI creates worktree
git worktree add ../electric-sheep-restore-design-isolated -b feature/restore-design-work-isolated
cd ../electric-sheep-restore-design-isolated
```

**Time:** 30-60 seconds (coordination check + prompt)

### Actual Outcome (What Happened)
- ❌ Modified file without checking coordination
- ❌ Other agent was working on same file
- ❌ Collision occurred
- ❌ 35 minutes to recover

### Evaluation: ✅ CORRECT
- **High risk detected correctly**
- **Would have prevented collision**
- **Prompt appropriate for risk level**

---

## Step 3: User Request - "Commit these changes"

### Context Gathered (After Step 2 - with worktree isolation)
- **Current branch**: `feature/restore-design-work-isolated` (in worktree)
- **Files**: Multiple files modified (design restoration)
- **Coordination**: Checked, worktree used
- **Change type**: UI changes, design restoration
- **Branch state**: Clean, isolated

### Smart Prompts Evaluation

```
[INTERNAL EVALUATION SUB-PROMPT]

STEP 1: Check if action is rule-required
- Action: Commit changes
- Rule check: Not rule-required (frequent commits are recommended, not mandatory)
- Continue to STEP 2

STEP 2: Evaluate the risk of committing changes:

CONTEXT:
- Action: Commit changes
- Files: Multiple files (design restoration)
- Change type: UI changes
- Branch: feature/restore-design-work-isolated (not main) ✅
- Branch state: Clean ✅
- Other agents: Isolated via worktree ✅
- Coordination: Checked, worktree used ✅
- User intent: Explicit request
- Complexity: Moderate
- Dependencies: UI changes, affects navigation

EVALUATION:
- What could go wrong? Low risk - isolated branch, coordination checked
- Is this routine? Yes, but multiple files, should confirm
- Any red flags? No - safe branch, isolated, coordination checked
- User confidence? High - explicit request
- Impact scope? Moderate - multiple files, but isolated

DECISION: QUICK_CONFIRM - Should confirm but straightforward

[AI shows brief summary]
```

### What Would Happen

**AI Action:**
```
📋 Commit 5 files (design restoration) to feature/restore-design-work-isolated? (y/n)
```

**If user approves:**
```bash
git add -A
git commit -m "feat: restore design - user info in top bar, icons"
```

**Time:** 10-15 seconds (brief prompt + approval)

### Actual Outcome (What Happened)
- ✅ Committed successfully (but after collision recovery)
- ⚠️ Had to recover from collision first (35 min wasted)

### Evaluation: ✅ CORRECT
- **Appropriate prompt level** (quick confirm for routine commit)
- **Would have happened after collision prevention** (Step 2)

---

## Step 4: User Request - "End session"

### Context Gathered
- **Current branch**: `feature/restore-design-work-isolated` (in worktree)
- **Uncommitted changes**: None (all committed)
- **Worktree**: Active, isolated
- **Status**: Work complete

### Smart Prompts Evaluation

```
[INTERNAL EVALUATION SUB-PROMPT]

STEP 1: Check if action is rule-required
- Action: End session
- Rule check: Not rule-required (session end is user-initiated)
- Continue to STEP 2

STEP 2: Evaluate the risk of ending session:

CONTEXT:
- Action: End session
- Files: All committed ✅
- Branch: feature/restore-design-work-isolated (not main) ✅
- Branch state: Clean, all committed ✅
- Other agents: Isolated via worktree ✅
- Coordination: Checked ✅
- User intent: Explicit request to end
- Complexity: Simple
- Dependencies: None

EVALUATION:
- What could go wrong? Nothing - all committed, clean state
- Is this routine? Yes, session ending is routine
- Any red flags? No - clean state, isolated
- User confidence? High - explicit request
- Impact scope? None - session ending

DECISION: PROCEED - Safe, routine operation, no risk

[AI executes: Shows summary, no prompt needed]
```

### What Would Happen

**AI Action:**
```
✅ Session complete
   • Branch: feature/restore-design-work-isolated
   • Commits: 1 commit
   • Status: Ready for PR
   • Worktree: ../electric-sheep-restore-design-isolated
```

**Time:** 0 seconds (automatic summary, no prompt)

### Actual Outcome (What Happened)
- ✅ Session ended (after recovery)
- ⚠️ But had to recover from issues first

### Evaluation: ✅ CORRECT
- **No prompt needed** (clean state, routine operation)
- **Would have happened smoothly** (no recovery needed)

---

## Potential Issues & Edge Cases

### Issue 1: What if user is on main but says "just commit"?

**Scenario:**
- User: "Just commit these changes"
- Context: On `main` branch, uncommitted changes

**Smart Prompts Evaluation:**
```
STEP 1: Check if action is rule-required
- Action: Commit changes
- Rule check: `.cursor/rules/branching.mdc` says "Never commit directly to main"
- This is a RULE-REQUIRED prevention (blocking dangerous action)
- DECISION: BLOCK - Must prevent, explain why

[AI blocks and explains]
```

**What Would Happen:**
```
❌ CRITICAL: Cannot commit to main branch
   • Current branch: main
   • Rule violation: Never commit directly to main
   
   Action required:
   1. Create feature branch first
   2. Move changes to feature branch
   3. Then commit
   
   Would you like me to create a feature branch and move these changes? (y/n)
```

**Evaluation: ✅ CORRECT**
- **Rule violation detected**
- **Would prevent dangerous action**
- **Offers fix (create branch)**

---

### Issue 2: What if coordination check is slow?

**Scenario:**
- User: "Modify MoodManagementScreen.kt"
- Context: Coordination doc is large, check takes time

**Smart Prompts Evaluation:**
```
STEP 2: Evaluate risk
- Shared file detected
- Coordination check needed
- DECISION: FULL_REVIEW (show prompt, then check coordination)

[AI shows prompt first, then checks coordination]
```

**What Would Happen:**
```
📋 SUMMARY: Modify shared file MoodManagementScreen.kt
   • Checking coordination... (this may take a moment)
   • [AI checks coordination doc]
   • Result: Other agent working on same file
   • Recommendation: Use worktree
```

**Evaluation: ⚠️ POTENTIAL ISSUE**
- **Prompt shown before coordination check** (user waits)
- **Better approach**: Check coordination first, then show prompt with results
- **Fix needed**: Gather all context before showing prompt

---

### Issue 3: What if rule-required action conflicts with user intent?

**Scenario:**
- User: "Don't create a branch, just work on main"
- Context: On `main` branch, uncommitted changes

**Smart Prompts Evaluation:**
```
STEP 1: Check if action is rule-required
- Action: Work on main
- Rule check: "If on main, IMMEDIATELY create worktree"
- This is a RULE-REQUIRED action
- BUT: User explicitly says "don't create branch"
- Conflict: Rule says create, user says don't

DECISION: BLOCK - Rule takes precedence, explain why
```

**What Would Happen:**
```
❌ CRITICAL: Cannot work on main branch
   • Rule violation: Never work on main branch
   • Your request conflicts with mandatory rule
   
   The rule exists to prevent:
   - Isolation failures
   - File collisions
   - Build errors
   
   I must create a feature branch. Would you like me to:
   1. Create feature branch and continue (recommended)
   2. Explain why this rule exists
   3. Cancel the operation
```

**Evaluation: ✅ CORRECT**
- **Rule takes precedence** (safety over user preference)
- **Explains why** (helps user understand)
- **Offers alternatives** (not just blocking)

---

### Issue 4: What if evaluation is wrong?

**Scenario:**
- User: "Add a comment"
- Context: On `main` branch (but AI doesn't detect it)

**Smart Prompts Evaluation:**
```
STEP 1: Check if action is rule-required
- Action: Add comment
- Rule check: Not rule-required
- Continue to STEP 2

STEP 2: Evaluate risk
- Context: Single file, documentation
- Branch: [AI incorrectly thinks feature branch]
- DECISION: PROCEED (low risk)

[AI executes: Adds comment to main branch - WRONG]
```

**What Would Happen:**
- ❌ Comment added to `main` branch
- ❌ Rule violation occurred
- ❌ Need to fix (move to feature branch)

**Evaluation: ❌ POTENTIAL ISSUE**
- **Context gathering must be accurate**
- **Branch detection is critical**
- **Fix needed**: Always verify branch before proceeding
- **Safety net**: Pre-commit hook would catch this

---

## Summary: What Would Have Happened

### With Smart Prompts Architecture

**Step 1: Start work**
- ✅ Auto-create branch (rule-required)
- ✅ Time: 0 sec
- ✅ **Prevented**: Working on main

**Step 2: Modify shared file**
- ✅ Full review prompt (high risk)
- ✅ Coordination check
- ✅ Worktree isolation
- ✅ Time: 30-60 sec
- ✅ **Prevented**: File collision

**Step 3: Commit**
- ✅ Quick confirm (routine)
- ✅ Time: 10-15 sec
- ✅ **Normal workflow**

**Step 4: End session**
- ✅ Auto-summary (low risk)
- ✅ Time: 0 sec
- ✅ **Normal workflow**

**Total time: 40-75 seconds**
**Issues prevented: 2 major issues (60-65 min recovery)**

### Actual Outcome (Without Smart Prompts)

**Step 1: Start work**
- ❌ Worked on main
- ❌ Time: 0 sec (but wrong)

**Step 2: Modify shared file**
- ❌ No coordination check
- ❌ Collision occurred
- ❌ Time: 0 sec (but wrong)

**Step 3: Recover from collision**
- ❌ 35 min recovery
- ❌ Time: 35 min wasted

**Step 4: Recover from isolation**
- ❌ 25-30 min recovery
- ❌ Time: 25-30 min wasted

**Total time: 60-65 minutes wasted**
**Issues occurred: 2 major issues**

---

## Confidence Assessment

### ✅ High Confidence Areas

1. **Rule-required actions** - Clear rules, easy to detect
2. **Branch detection** - Simple git command, reliable
3. **Shared file detection** - Can check coordination doc
4. **Risk evaluation** - Holistic context evaluation works

### ⚠️ Medium Confidence Areas

1. **Coordination check accuracy** - Depends on doc being up to date
2. **User intent detection** - Sometimes ambiguous
3. **Complexity assessment** - Subjective

### ❌ Low Confidence Areas (Potential Issues)

1. **Context gathering errors** - If branch detection fails
2. **Slow coordination checks** - Large docs take time
3. **False positives** - Over-prompting for safe operations
4. **Rule conflicts** - When user intent conflicts with rules

### Mitigations

1. **Always verify branch** - Double-check before proceeding
2. **Cache coordination checks** - Don't re-check unnecessarily
3. **Learn from patterns** - Reduce false positives over time
4. **Rule precedence** - Rules always win (safety first)

---

## Conclusion

**The architecture would have worked correctly:**
- ✅ Prevented both major issues
- ✅ Appropriate prompt levels
- ✅ Rule-required actions auto-executed
- ✅ Total time: 40-75 sec vs. 60-65 min wasted

**Potential issues identified:**
- ⚠️ Context gathering must be accurate
- ⚠️ Coordination checks should be fast
- ⚠️ Need safety nets (pre-commit hooks)

**Overall confidence: 85-90%**
- High confidence in rule-required detection
- Medium confidence in risk evaluation
- Need safeguards for context gathering errors

