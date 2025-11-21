# Weekly Workflow Analysis: Prompt Effectiveness Insights

**Date**: 2025-11-21  
**Purpose**: Analyze week's work to identify waste, missed opportunities, and process improvements for more effective prompt-based development

## Executive Summary

**Key Findings:**
- **19 fix commits** in one week (high waste indicator)
- **15 merge/conflict commits** (coordination gaps)
- **3x repeated fixes** for same issues (rules not enforced)
- **59 scripts available** but many underutilized
- **Large untracked file accumulation** (cleanup not automated)

**Critical Insight**: The infrastructure exists (rules, scripts, coordination docs), but **prompts aren't leveraging them effectively**.

---

## 1. Waste Patterns: Repeated Mistakes

### 1.1 Same Fix Applied Multiple Times

**Pattern**: Same fix committed 3+ times
- `fix: Add ci-status check required by branch protection` - **3 times**
- `fix: Remove MoodChart usage` - **2 times**  
- `fix: Fix flaky test by using fixed date` - **2 times**
- `fix: Make Preview Migrations check non-blocking` - **2 times**

**Root Cause**: 
- Fix applied in one branch, then needed again in another
- No verification that fix was already in main
- Not checking main before starting work

**Prompt Improvement**:
```
Before fixing an issue, check if it's already fixed in main:
1. git fetch origin main
2. git log origin/main --grep="<issue-keyword>" --oneline
3. If fix exists, merge/rebase main instead of re-fixing
```

### 1.2 Merge Conflicts from Parallel Work

**Pattern**: 15 merge/conflict commits in one week
- Multiple agents working on overlapping files
- Coordination doc exists but not checked before starting
- Worktrees created but conflicts still occur

**Root Cause**:
- Prompts don't enforce "check coordination doc first"
- Agents start work without checking `AGENT_COORDINATION.md`
- No automated check before file modifications

**Prompt Improvement**:
```
CRITICAL: Before modifying ANY file:
1. Read docs/development/AGENT_COORDINATION.md
2. Check if file is listed in "Shared Files" or "Active Work"
3. If conflict exists, use worktree: ./scripts/create-worktree.sh <task-name>
4. Document your work in coordination doc BEFORE starting
```

### 1.3 Documentation Created But Not Referenced

**Pattern**: Large doc commits (960, 1134, 944 insertions) but issues persist
- Comprehensive rules exist (15 cursor rules)
- Documentation created but not checked
- Same mistakes repeated despite documentation

**Root Cause**:
- Rules exist but prompts don't reference them
- No "read relevant docs first" step in prompts
- Documentation is comprehensive but not actionable in prompts

**Prompt Improvement**:
```
Before starting work, read relevant documentation:
1. Check .cursor/rules/ for relevant rules
2. Read docs/development/AGENT_COORDINATION.md
3. Review docs/architecture/ for related patterns
4. Reference specific rules in your implementation plan
```

---

## 2. Missed Opportunities: Tools Not Leveraged

### 2.1 Scripts Available But Not Used

**Available**: 59 scripts in `scripts/` directory
**Used**: Only a few (emulator-manager, dev-reload, get-device-id)

**Underutilized Scripts**:
- `check-agent-coordination.sh` - Exists but not in workflow
- `create-worktree.sh` - Available but not prompted
- `md-to-google-doc.sh` - Created but workflow not established
- `analyze-screenshot-in-cursor.sh` - Exists but not integrated

**Prompt Improvement**:
```
Before starting work, check available automation:
1. List scripts: ls scripts/*.sh scripts/*.py
2. Check if task can be automated: grep -r "your-task" scripts/
3. Use existing scripts instead of manual steps
4. Document new scripts if you create them
```

### 2.2 Cursor Rules Exist But Not Enforced

**Available**: 15 comprehensive cursor rules
**Issue**: Rules are verbose but prompts don't reference them

**Example**: `visual-first-principle.mdc` exists but test framework still uses Appium internals

**Prompt Improvement**:
```
When implementing features, explicitly reference relevant rules:
1. "Following .cursor/rules/visual-first-principle.mdc, I will..."
2. "Per .cursor/rules/branching.mdc, I will create a feature branch..."
3. "As per .cursor/rules/error-handling.mdc, I will use Result<T>..."
```

### 2.3 Coordination Doc Not Automated

**Available**: `AGENT_COORDINATION.md` exists
**Issue**: Manual updates, easily forgotten

**Missed Opportunity**: 
- Script `check-agent-coordination.sh` exists but not integrated
- No pre-commit hook to check coordination
- No automated status updates

**Prompt Improvement**:
```
Before ANY file modification:
1. Run: ./scripts/check-agent-coordination.sh
2. If conflicts detected, use worktree
3. Update coordination doc: Add your work entry
4. Commit coordination doc update with your changes
```

---

## 3. Process Inefficiencies: Flow Gaps

### 3.1 Pre-Work Checklist Not Enforced

**Pattern**: Work starts without proper setup
- Not checking branch (working on main)
- Not pulling latest main
- Not checking coordination
- Not using worktrees when needed

**Root Cause**: Prompts don't include mandatory pre-work checklist

**Prompt Improvement**:
```
MANDATORY Pre-Work Checklist (run before ANY changes):
1. git status (verify not on main)
2. git fetch origin main (check for updates)
3. git pull origin main (if on feature branch)
4. ./scripts/check-agent-coordination.sh (check conflicts)
5. Read docs/development/AGENT_COORDINATION.md (coordination)
6. Create worktree if modifying shared files
```

### 3.2 Post-Merge Cleanup Not Automated

**Pattern**: Branches and worktrees accumulate
- Merged branches not deleted
- Worktrees not removed
- Duplicate branches created
- Temp branches left around

**Root Cause**: Cleanup is manual and forgotten

**Missed Opportunity**: 
- Repository maintenance rules exist but not enforced
- No automated cleanup script
- No post-merge checklist

**Prompt Improvement**:
```
After PR merge, MANDATORY cleanup:
1. git checkout main && git pull origin main
2. git branch -d feature/<merged-branch>
3. git worktree remove ../electric-sheep-<task-name> (if used)
4. git branch | grep -E "^(temp|tmp|test-)" | xargs git branch -D
5. Update docs/development/AGENT_COORDINATION.md (mark complete)
```

### 3.3 Test Failures Not Caught Early

**Pattern**: Fix commits for flaky tests
- Tests fail in CI, then fixed
- Same test fixed twice
- No local test run before commit

**Root Cause**: No "run tests before commit" enforcement

**Prompt Improvement**:
```
Before committing:
1. ./gradlew test (run tests locally)
2. Fix any failing tests
3. Verify all tests pass
4. Never commit failing tests
```

---

## 4. Flow & Process: Prompt Structure Issues

### 4.1 Prompts Are Task-Focused, Not Process-Focused

**Current Pattern**: "Add feature X"
**Issue**: Doesn't include process steps (check branch, coordinate, test)

**Better Pattern**: "Add feature X following workflow: [checklist]"

**Prompt Template**:
```
Task: <description>

MANDATORY Workflow:
1. Pre-work checklist (branch, coordination, docs)
2. Implementation (with rule references)
3. Testing (local tests before commit)
4. Documentation (update coordination doc)
5. Post-merge cleanup (if applicable)
```

### 4.2 Rules Exist But Not Referenced in Prompts

**Pattern**: Rules are comprehensive but prompts don't mention them
- 15 cursor rules with detailed guidance
- Prompts don't say "following rule X"
- Rules become documentation, not active guidance

**Improvement**: Reference rules explicitly in prompts

**Example**:
```
Instead of: "Add error handling"
Use: "Add error handling following .cursor/rules/error-handling.mdc:
- Use Result<T> for operations that can fail
- Use NetworkError.fromException() for network errors
- Log errors using error.log() method"
```

### 4.3 Coordination Is Reactive, Not Proactive

**Pattern**: Conflicts detected after work starts
- Coordination doc updated after conflicts
- Worktrees created after conflicts occur
- Coordination is documentation, not workflow

**Improvement**: Make coordination part of pre-work

**Prompt Template**:
```
Before starting work:
1. Check coordination: ./scripts/check-agent-coordination.sh
2. If shared files: Use worktree (./scripts/create-worktree.sh)
3. Document work: Update AGENT_COORDINATION.md BEFORE starting
4. Verify: Run check script again to confirm no conflicts
```

---

## 5. Specific Waste Examples

### 5.1 CI Status Check Added 3 Times

**Waste**: Same fix in 3 different branches
- `fix: Add ci-status check required by branch protection` (3 commits)
- Each branch needed the fix independently
- Should have been fixed once in main, then merged

**Better Flow**:
1. Identify issue in one branch
2. Fix in main (or dedicated fix branch)
3. Merge fix to all affected branches
4. Don't re-fix in each branch

### 5.2 MoodChart Removed Multiple Times

**Waste**: Component removed in 2 different commits
- `fix: Remove MoodChart usage` (2 commits)
- Coordination issue: multiple agents working on same component
- Should have been coordinated or used worktree

**Better Flow**:
1. Check coordination doc
2. See MoodChart is being worked on
3. Use worktree or coordinate with other agent
4. Single removal commit

### 5.3 Flaky Test Fixed Twice

**Waste**: Same test fix in 2 commits
- `fix: Fix flaky test by using fixed date` (2 commits)
- Test should have been fixed once, then merged

**Better Flow**:
1. Fix flaky test in main
2. Merge fix to affected branches
3. Don't re-fix in each branch

---

## 6. Opportunities: Better Prompt Structure

### 6.1 Template-Based Prompts

**Current**: Ad-hoc prompts
**Better**: Structured templates with checklists

**Template**:
```
Task: <description>

Pre-Work (MANDATORY):
- [ ] Check branch (not on main)
- [ ] Pull latest main
- [ ] Check coordination (./scripts/check-agent-coordination.sh)
- [ ] Read relevant rules (.cursor/rules/)
- [ ] Use worktree if modifying shared files

Implementation:
- [ ] Reference relevant rules explicitly
- [ ] Use existing scripts/tools
- [ ] Follow patterns from docs/architecture/

Testing:
- [ ] Run tests locally (./gradlew test)
- [ ] Fix failing tests
- [ ] Verify all tests pass

Documentation:
- [ ] Update coordination doc
- [ ] Update relevant docs if needed

Post-Merge (if applicable):
- [ ] Clean up branch
- [ ] Remove worktree
- [ ] Update coordination doc
```

### 6.2 Rule-Referencing Prompts

**Current**: Generic instructions
**Better**: Explicit rule references

**Example**:
```
Instead of: "Handle errors properly"
Use: "Handle errors following .cursor/rules/error-handling.mdc:
- Use Result<T> for operations that can fail
- Use NetworkError.fromException() for network errors
- Log errors using error.log() method
- See rule for complete pattern"
```

### 6.3 Tool-Aware Prompts

**Current**: Manual steps
**Better**: Use existing automation

**Example**:
```
Instead of: "Start emulator manually"
Use: "Use emulator script: ./scripts/emulator-manager.sh start"

Instead of: "Check for conflicts manually"
Use: "Run: ./scripts/check-agent-coordination.sh"
```

---

## 7. Actionable Recommendations

### 7.1 Immediate: Add Pre-Work Checklist to All Prompts

**Action**: Include mandatory checklist in every prompt
```
Before ANY work:
1. git status (verify branch)
2. git fetch && git pull origin main
3. ./scripts/check-agent-coordination.sh
4. Read docs/development/AGENT_COORDINATION.md
5. Use worktree if modifying shared files
```

### 7.2 Short-Term: Create Prompt Templates

**Action**: Create reusable prompt templates
- Feature implementation template
- Bug fix template
- Documentation template
- Each with appropriate checklists

### 7.3 Medium-Term: Automate Coordination Checks

**Action**: Integrate coordination checks
- Pre-commit hook to check coordination
- Automated status updates
- Conflict detection before work starts

### 7.4 Long-Term: Reduce Rule Verbosity

**Action**: Make rules more actionable
- Extract key points for prompts
- Create "quick reference" versions
- Reference rules explicitly in prompts

---

## 8. Key Insights for Prompt Effectiveness

### 8.1 Prompts Must Include Process, Not Just Tasks

**Insight**: Task-focused prompts miss process steps
- "Add feature X" → misses branch check, coordination, testing
- "Add feature X [with checklist]" → includes all steps

### 8.2 Rules Must Be Referenced, Not Just Exist

**Insight**: Rules are documentation unless referenced
- 15 rules exist but prompts don't mention them
- Explicit rule references make rules active

### 8.3 Tools Must Be Discovered, Not Assumed

**Insight**: 59 scripts exist but aren't used
- Prompts should check for existing tools
- "List available scripts" should be in workflow

### 8.4 Coordination Must Be Proactive, Not Reactive

**Insight**: Coordination doc exists but checked after conflicts
- Make coordination part of pre-work
- Automate coordination checks

### 8.5 Cleanup Must Be Automated, Not Manual

**Insight**: Cleanup is forgotten when manual
- Post-merge cleanup should be checklist
- Automate branch/worktree cleanup

---

## 9. Metrics Summary

**This Week:**
- **19 fix commits** (high - indicates waste)
- **15 merge/conflict commits** (coordination gaps)
- **3x repeated fixes** (rules not enforced)
- **59 scripts available** (many underutilized)
- **15 cursor rules** (comprehensive but not referenced)
- **Large untracked files** (cleanup not automated)

**Target (Next Week):**
- **<10 fix commits** (reduce waste)
- **<5 merge/conflict commits** (better coordination)
- **0 repeated fixes** (check main first)
- **Use 5+ scripts** (leverage automation)
- **Reference rules in prompts** (make rules active)
- **Automated cleanup** (no accumulation)

---

## 10. Conclusion

**The Problem**: Infrastructure exists (rules, scripts, coordination) but prompts don't leverage it effectively.

**The Solution**: 
1. **Structured prompts** with mandatory checklists
2. **Explicit rule references** in prompts
3. **Tool discovery** in workflow
4. **Proactive coordination** (not reactive)
5. **Automated cleanup** (not manual)

**The Pattern**: 
- Current: Task → Implementation → Fix → Cleanup
- Better: Checklist → Coordination → Implementation → Test → Cleanup

**Key Insight**: **Prompts are ephemeral; process must be embedded in prompts, not just documented in rules.**

---

## 11. Automation Implementation (2025-11-21)

**Status**: ✅ **AUTOMATED** - All recommendations are now enforced automatically.

### Automation Created

1. **`scripts/pre-work-check.sh`** - Enforces mandatory pre-work checklist
   - Blocks work if on main branch
   - Checks for remote updates
   - Runs coordination checks
   - Discovers relevant rules
   - Identifies available tools

2. **`scripts/discover-rules.sh`** - Discovers relevant cursor rules
   - Searches rules by keyword
   - Shows key points and file paths
   - Integrated into pre-work check

3. **`scripts/post-merge-cleanup.sh`** - Automated post-merge cleanup
   - Checks other active work
   - Verifies PR merge status
   - Deletes merged branches
   - Removes worktrees
   - Cleans temp branches

4. **`scripts/generate-prompt-template.sh`** - Generates structured prompts
   - Includes all mandatory checklists
   - References automation scripts
   - Ensures process is embedded

5. **Git Hooks**:
   - **`.git/hooks/pre-commit`** - Blocks commits to main
   - **`.git/hooks/post-checkout`** - Reminds to run pre-work check

### Cursor Rules Updated

- **`.cursor/rules/branching.mdc`** - References automation scripts
- **`.cursor/rules/cicd.mdc`** - References post-merge cleanup
- All rules now point to automation, not just documentation

### Enforcement Level

**Before**: Recommendations (nice-to-have)
**After**: **Automated enforcement** (must follow)

- Pre-work check **blocks** work if errors found
- Pre-commit hook **blocks** commits to main
- Post-merge cleanup **automated** (with safety checks)
- Rule discovery **integrated** into workflow

### Usage

**Start work**:
```bash
./scripts/pre-work-check.sh  # MANDATORY - blocks if errors
```

**After PR merge**:
```bash
./scripts/post-merge-cleanup.sh <pr-number>  # AUTOMATED
```

**See**: `docs/development/AUTOMATED_WORKFLOW.md` for complete documentation.

