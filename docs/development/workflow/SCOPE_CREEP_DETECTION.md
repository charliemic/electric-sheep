# Scope Creep Detection and Agent Switching

**Purpose**: Detect when a Cursor agent session is expanding beyond its original scope and suggest starting a new agent session for better focus and context management.

## Overview

Scope creep occurs when a session that started with a focused task gradually expands to include multiple unrelated tasks, many files, or significant complexity. This can lead to:

- Reduced focus and clarity
- Slower responses (more context to process)
- Harder to review and test
- Messier git history
- Increased risk of errors

**Solution**: Detect scope creep automatically and suggest starting a new agent session when appropriate.

## Scope Creep Indicators

### Warning Signs

**1. Task Expansion**
- Original: "Fix login bug"
- Current: "Fix login bug, add error handling, improve UX, add tests, update docs"
- **Indicator**: Task description has expanded significantly

**2. File Count Growth**
- Started with: 1-2 files
- Now modifying: 5+ files across multiple directories
- **Indicator**: File count has grown beyond original scope

**3. Time/Duration**
- Session duration: > 2 hours of active work
- Multiple unrelated tasks completed in same session
- **Indicator**: Session has been running for extended period

**4. Context Switching**
- Started: Feature A
- Now working on: Feature B (unrelated)
- **Indicator**: Multiple unrelated topics in same session

**5. Complexity Growth**
- Started: Simple fix
- Now: Architecture changes, multiple components, cross-cutting concerns
- **Indicator**: Complexity has increased significantly

## Scope Creep Severity Levels (AI-Based Evaluation)

### No Scope Creep
- Tasks are related to original task
- Expansion is natural progression
- Session remains focused and manageable
- **Action**: Continue with current session

### Mild Scope Creep
- Some expansion, but tasks are related
- Still focused and manageable
- Minor context switches
- **Action**: Monitor, but continue if manageable

### Moderate Scope Creep
- Significant expansion, some unrelated tasks
- Session becoming less focused
- Multiple concerns being addressed
- **Action**: Flag to user, suggest considering new agent

### Severe Scope Creep
- Major expansion, multiple unrelated tasks
- Session has lost focus
- Architecture changes beyond original scope
- **Action**: Strongly recommend new agent session

## Detection System: AI-Based Context Evaluation

### Automatic Tracking

The scope creep detection system tracks:

1. **Original Scope** (at session start):
   - Task description
   - Initial file count
   - Initial complexity estimate
   - Session start time

2. **Current Scope** (during session):
   - Current task description
   - Files modified/created
   - Tasks completed
   - Session duration
   - Context switches

3. **AI-Based Evaluation** (contextual analysis):
   - **Relationship Analysis**: Are new tasks related to original or unrelated?
   - **Progression Analysis**: Is expansion natural progression or scope creep?
   - **Necessity Analysis**: Are additional files necessary or tangential?
   - **Focus Analysis**: Has session become unfocused?
   - **Productivity Analysis**: Is session still productive or becoming unwieldy?
   - **Context Analysis**: Is accumulated context affecting performance?

**The AI evaluates these factors holistically** to determine scope creep severity, rather than using a formula-based score.

### Using the Tracking Script

**Start a session:**
```bash
./scripts/track-session-scope.sh start "Fix login bug" 1 low
```

**Update task:**
```bash
./scripts/track-session-scope.sh update "Fix login bug and add error handling"
```

**Check for scope creep:**
```bash
./scripts/track-session-scope.sh check
```

**View full report:**
```bash
./scripts/track-session-scope.sh report
```

### Integration with Cursor

The Cursor rule (`.cursor/rules/scope-creep-detection.mdc`) automatically:

1. Tracks session scope when work begins
2. Monitors scope expansion during work
3. Calculates scope creep score
4. Flags when thresholds are exceeded
5. Provides clear recommendations
6. Makes it easy to start a new agent

## When to Start a New Agent

### Recommended Scenarios

Start a new agent session when:

- ✅ Scope creep score > 70 (severe)
- ✅ Working on unrelated feature (context switch)
- ✅ Session duration > 3 hours
- ✅ Multiple unrelated tasks completed
- ✅ Architecture changes (beyond original scope)
- ✅ User explicitly requests new agent

### How to Start a New Agent

**Simple 3-step process:**

1. **Commit current work** (WIP is fine):
   ```bash
   git add .
   git commit -m "WIP: [brief description of current state]"
   ```

2. **Start new Cursor chat session**:
   - Press `Cmd+L` (Mac) or `Ctrl+L` (Windows/Linux)
   - Or click "New Chat" in Cursor UI

3. **Reference previous work**:
   ```
   Continuing from previous session:
   - Completed: [what was done]
   - Current state: [brief description]
   - Next task: [what to do next]
   ```

4. **Provide context** (optional but helpful):
   - Branch name
   - Files modified
   - Current state of work
   - What needs to be done next

## Benefits of New Agent Session

**Why start a new agent:**

- ✅ **Fresh context** - No accumulated complexity from previous tasks
- ✅ **Better focus** - Agent focuses on current task only
- ✅ **Cleaner history** - Easier to review and understand
- ✅ **Faster responses** - Less context to process
- ✅ **Better organization** - Each session has clear purpose
- ✅ **Easier testing** - Test one feature at a time
- ✅ **Cleaner commits** - Logical, focused commits

## Scope Creep Prevention

### Best Practices

**To prevent scope creep:**

- ✅ Define clear, focused tasks
- ✅ Break large tasks into smaller, focused sessions
- ✅ Complete one task before starting another
- ✅ Use feature branches for each focused task
- ✅ Commit frequently (safety net)
- ✅ Start new agent for unrelated tasks

**Task definition guidelines:**

- ✅ Single, focused goal
- ✅ Clear success criteria
- ✅ Limited file scope (1-3 files ideal)
- ✅ Estimated duration (< 2 hours ideal)

### Example: Good Task Definition

**✅ Good:**
```
Task: Fix login validation bug
Scope: 1 file (LoginScreen.kt)
Complexity: Low
Duration: < 1 hour
Success: Login validation works correctly
```

**❌ Bad:**
```
Task: Fix login bug and improve UX and add tests
Scope: Multiple files, unknown
Complexity: High
Duration: Unknown
Success: Unclear
```

## Examples

### Example 1: Mild Scope Creep (Continue)

```
Original: "Fix typo in LoginScreen.kt"
Current: "Fix typo + update related comment"
Score: 35 (mild)

Action: Continue - manageable expansion
```

### Example 2: Moderate Scope Creep (Flag)

```
Original: "Fix login validation bug"
Current: "Fix bug + add error handling + improve UX"
Score: 60 (moderate)

Action: Flag to user, suggest considering new agent
```

### Example 3: Severe Scope Creep (Strongly Recommend)

```
Original: "Fix login validation bug"
Current: "Fix bug + error handling + UX + tests + docs + refactor + new feature"
Score: 85 (severe)

Action: Strongly recommend new agent session
```

## Integration with Workflow

### Pre-Work Check

The pre-work check (`scripts/pre-work-check.sh`) should:

- Check for existing active sessions
- Suggest starting new agent if scope is too broad
- Reference scope creep detection

### During Work

The agent should:

- Monitor scope expansion
- Flag when thresholds exceeded
- Suggest new agent when appropriate
- Make it easy to start new agent

### Post-Work

After completing work:

- Review scope creep score
- Document lessons learned
- Update best practices if needed

## Metrics and Analysis

### Session Metrics

Session data is stored in:
- `development-metrics/sessions/session_*.json`

Each session includes:
- Original and current scope
- Scope creep score
- Duration
- Files modified
- Context switches
- Recommendations

### Analysis

Review session metrics to:
- Identify patterns in scope creep
- Refine thresholds
- Improve task definition
- Optimize workflow

## Related Documentation

- `.cursor/rules/scope-creep-detection.mdc` - Cursor rule for scope creep detection
- `scripts/track-session-scope.sh` - Session scope tracking script
- `docs/development/MULTI_AGENT_WORKFLOW.md` - Multi-agent workflow guidelines
- `.cursor/rules/prompt-stuck-prevention.mdc` - Prompt stuck prevention (scope creep can cause stuck prompts)
- `.cursor/rules/branching.mdc` - Branch isolation (use focused branches)
- `.cursor/rules/frequent-commits.mdc` - Frequent commits (safety net)

