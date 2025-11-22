# Agent Coordination Triggers

**Last Updated**: 2025-01-22  
**Purpose**: Document how agents can be "triggered" or notified of work assignments

## How Agents Are Triggered

### 1. Pre-Work Check Script (Automatic)

**When agents run `./scripts/pre-work-check.sh`, they automatically:**
- ✅ Check `AGENT_COORDINATION.md` for active work
- ✅ Check for file conflicts
- ✅ See coordinator assignments in `AGENT_COORDINATOR_ASSIGNMENTS.md`
- ✅ See updated issue labels in GitHub

**This is the PRIMARY trigger mechanism** - agents check coordination docs before starting work.

### 2. Coordination Document Updates (Manual Check)

**Agents should check `AGENT_COORDINATION.md` for:**
- New task assignments
- Available issues to claim
- Work that needs coordination

**How to trigger agents:**
1. Update `AGENT_COORDINATION.md` with new assignments
2. Add entries with `[AVAILABLE]` or `[ASSIGNED]` status
3. Agents will see these when they run pre-work checks

### 3. GitHub Issue Labels (Automatic Detection)

**Agents can check GitHub issues for assignments:**
```bash
# Check for issues assigned to you
gh issue list --label "status:in-progress" --assignee @me

# Check for available issues
gh issue list --label "status:not-started" --label "area:testing"
```

**How to trigger agents via issues:**
1. Update issue labels: `status:not-started` → `status:in-progress`
2. Add area labels: `area:testing`, `area:security`, etc.
3. Agents can query these via GitHub CLI

### 4. Query Script (On-Demand)

**Agents can query coordination status:**
```bash
# List active work
./scripts/query-agent-coordination.sh list-active

# Check for available assignments
./scripts/query-agent-coordination.sh status <task-name>
```

**How to trigger agents:**
- Update coordination doc with new assignments
- Agents querying will see new work

### 5. Git Commits (Passive Notification)

**Agents can check recent commits for coordination updates:**
```bash
# Check recent coordination doc changes
git log --oneline --since="1 hour ago" -- docs/development/workflow/AGENT_COORDINATION.md
```

**How to trigger agents:**
- Commit coordination updates
- Agents checking git history will see updates

## Current Limitations

**What I CANNOT do:**
- ❌ Directly invoke other Cursor agent instances
- ❌ Send real-time notifications to other agents
- ❌ Access Cursor's internal agent triggering mechanisms
- ❌ Force other agents to check coordination docs

**What I CAN do:**
- ✅ Update coordination documents (agents check these)
- ✅ Update GitHub issue labels (agents can query these)
- ✅ Create assignment documents (agents see these in pre-work checks)
- ✅ Commit changes (agents see these in git history)

## Best Practices for Triggering Agents

### 1. Update Coordination Doc First

**Always update `AGENT_COORDINATION.md` with:**
- New task assignments
- Available work to claim
- Status updates

**Agents will see this when they:**
- Run `./scripts/pre-work-check.sh`
- Check coordination doc manually
- Query coordination status

### 2. Update GitHub Issues

**Update issue labels to signal assignments:**
- `status:not-started` → `status:in-progress` (work assigned)
- Add area labels for filtering
- Add priority labels for prioritization

**Agents can query:**
```bash
gh issue list --label "status:in-progress" --label "area:testing"
```

### 3. Create Assignment Documents

**Create documents like `AGENT_COORDINATOR_ASSIGNMENTS.md` with:**
- Clear task assignments
- Rationale for assignments
- Dependencies and prerequisites

**Agents will see these in:**
- Pre-work checks (if script checks for assignment docs)
- Manual coordination doc review

### 4. Use Clear Status Indicators

**Use status tags in coordination doc:**
- `[AVAILABLE]` - Work available for claiming
- `[ASSIGNED]` - Work assigned to specific agent/worktree
- `[IN_PROGRESS]` - Work actively being done
- `[BLOCKED]` - Work blocked by dependencies

**Agents can filter by status:**
```bash
grep -A 10 "\[AVAILABLE\]" docs/development/workflow/AGENT_COORDINATION.md
```

## Example: Triggering a Testing Agent

**To trigger a testing agent to work on Issue #55:**

1. **Update coordination doc:**
   ```markdown
   ### Task: Integrate Firebase Crashlytics (Issue #55)
   - **Status**: [AVAILABLE]
   - **Assigned to**: Testing agent (test-data-seeding OR runtime-visual-evaluation)
   - **Priority**: P1
   - **Files**: Will modify testing infrastructure
   ```

2. **Update GitHub issue:**
   ```bash
   gh issue edit 55 --add-label "status:in-progress" --add-label "area:testing"
   ```

3. **Create assignment entry:**
   - Add to `AGENT_COORDINATOR_ASSIGNMENTS.md`
   - Document rationale and dependencies

4. **Commit changes:**
   ```bash
   git add docs/development/workflow/
   git commit -m "coordinator: Assign Issue #55 to testing agents"
   ```

**Agent will see this when:**
- Running `./scripts/pre-work-check.sh`
- Checking coordination doc
- Querying GitHub issues
- Reviewing git history

## Future Enhancements

**Potential improvements:**
1. **Automated pre-work check enhancement** - Check for new assignments automatically
2. **GitHub Actions workflow** - Post comments on issues when assigned
3. **Coordination bot** - Automated agent that monitors and assigns work
4. **Real-time coordination** - Webhook-based notifications (requires infrastructure)

## Related Documentation

- `AGENT_COORDINATION.md` - Main coordination document
- `AGENT_COORDINATOR_ASSIGNMENTS.md` - Current assignments
- `AGENT_COMMUNICATION_PROTOCOL.md` - Communication protocol
- `scripts/pre-work-check.sh` - Pre-work validation script
- `scripts/query-agent-coordination.sh` - Coordination query tool

