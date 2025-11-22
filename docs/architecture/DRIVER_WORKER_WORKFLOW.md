# Driver-Worker Workflow - Complete Guide

**Date**: 2025-01-22  
**Purpose**: Complete workflow guide for driver-first, then workers pattern  
**Status**: Implementation Guide

---

## Workflow Overview

### Step 1: Setup Driver Agent

**When**: Start of project/epic  
**Who**: User  
**What**: Start driver agent and assign planning task

**Process**:
```
1. User starts driver agent (manual)
2. User assigns task: "Plan and decompose [project/epic]"
3. Driver agent:
   - Analyzes project scope
   - Decomposes into tasks
   - Creates GitHub Issues
   - Creates TASK_BOARD.md
   - Outputs task list
4. Driver agent completes
```

**Output**: 
- GitHub Issues created (one per task)
- TASK_BOARD.md with detailed breakdown
- Tasks labeled with skills and status

---

### Step 2: Start Worker Agents

**When**: After driver completes  
**Who**: User  
**What**: Start worker agents (as many as needed)

**Process**:
```
1. User starts worker agent #1 (manual)
2. Agent runs pre-work-check.sh
3. Pre-work check shows available tasks
4. User/agent claims task
5. Agent works on task
6. Repeat for additional workers
```

**Key Point**: Workers can be started in parallel or sequentially, depending on:
- Task dependencies
- Available resources
- Coordination needs

---

## Complete Example

### Monday Morning: Driver Setup

**User**: "I need to implement user onboarding flow"

**Driver Agent Session**:
```
User: [Starts driver agent]
User: "Plan and decompose the user onboarding flow project"

Driver Agent:
1. Analyzes: "User onboarding flow for Android app"
2. Identifies components:
   - UI screens (onboarding steps)
   - Navigation (screen flow)
   - Data collection (user preferences)
   - Validation (form validation)
   - Analytics (tracking)
   - Testing (test coverage)
   - Documentation

3. Decomposes into 7 tasks:
   - Task 1: Design onboarding screens (UI, 4h)
   - Task 2: Implement navigation (Navigation, 3h)
   - Task 3: Add data collection (Data, 3h)
   - Task 4: Implement validation (Backend, 4h)
   - Task 5: Add analytics (Analytics, 2h)
   - Task 6: Write tests (Testing, 4h)
   - Task 7: Documentation (Docs, 2h)

4. Creates GitHub Issues:
   - Issue #100: Design onboarding screens [skill:ui, status:available]
   - Issue #101: Implement navigation [skill:navigation, status:available]
   - Issue #102: Add data collection [skill:data, status:available]
   - Issue #103: Implement validation [skill:backend, status:available]
   - Issue #104: Add analytics [skill:analytics, status:available]
   - Issue #105: Write tests [skill:testing, status:available]
   - Issue #106: Documentation [skill:docs, status:available]

5. Creates TASK_BOARD.md with:
   - Task dependencies
   - Acceptance criteria
   - Files to modify
   - Technical details

6. Output: "Created 7 tasks. Ready for workers. Recommended: 3-4 workers in parallel."
```

**Driver completes** ✅

---

### Monday Afternoon: Start Workers

**User starts 3 worker agents** (based on driver's recommendation):

#### Worker 1: UI Agent

```
User: [Starts UI agent]

Agent runs pre-work-check.sh:
"📋 AVAILABLE TASKS
Issue #100: Design onboarding screens [skill:ui]
→ Claim: gh issue edit 100 --add-label 'status:in-progress' --assign '@me'"

User: "Claim issue #100"

Agent:
1. Claims issue #100
2. Reads TASK_BOARD.md
3. Starts work on UI design
4. Updates status as work progresses
```

#### Worker 2: Navigation Agent

```
User: [Starts Navigation agent]

Agent runs pre-work-check.sh:
"📋 AVAILABLE TASKS
Issue #101: Implement navigation [skill:navigation]
→ Claim: gh issue edit 101 --add-label 'status:in-progress' --assign '@me'"

User: "Claim issue #101"

Agent:
1. Claims issue #101
2. Sees dependency: depends on Issue #100 (UI screens)
3. Starts on navigation structure (can work in parallel)
4. Waits for UI screens to complete for final integration
```

#### Worker 3: Data Agent

```
User: [Starts Data agent]

Agent runs pre-work-check.sh:
"📋 AVAILABLE TASKS
Issue #102: Add data collection [skill:data]
→ Claim: gh issue edit 102 --add-label 'status:in-progress' --assign '@me'"

User: "Claim issue #102"

Agent:
1. Claims issue #102
2. Works independently (no dependencies)
3. Completes task
4. Marks complete
```

---

### Tuesday: Continue Workers

**User starts additional workers** as tasks complete:

#### Worker 4: Backend Agent (after UI completes)

```
User: [Starts Backend agent]

Agent runs pre-work-check.sh:
"📋 AVAILABLE TASKS
Issue #103: Implement validation [skill:backend]
→ Claim: gh issue edit 103 --add-label 'status:in-progress' --assign '@me'"

User: "Claim issue #103"

Agent:
1. Claims issue #103
2. Works on validation logic
3. Completes task
```

#### Worker 5: Testing Agent (after core tasks complete)

```
User: [Starts Testing agent]

Agent runs pre-work-check.sh:
"📋 AVAILABLE TASKS
Issue #105: Write tests [skill:testing]
Dependencies: Requires Issues #100, #101, #102, #103
→ Claim: gh issue edit 105 --add-label 'status:in-progress' --assign '@me'"

User: "Claim issue #105"

Agent:
1. Claims issue #105
2. Waits for dependencies to complete
3. Writes tests
4. Completes task
```

---

## Optimal Worker Count Analysis

### Factors to Consider

#### 1. Task Dependencies

**High Dependencies** (Sequential):
- Tasks must complete in order
- **Optimal**: 1-2 workers
- **Example**: UI → Navigation → Integration

**Low Dependencies** (Parallel):
- Tasks can work independently
- **Optimal**: 3-5 workers
- **Example**: UI, Data, Analytics (all independent)

#### 2. Coordination Overhead

**Coordination Cost**:
- More workers = more coordination needed
- File conflicts increase with more workers
- Communication overhead grows

**Sweet Spot**: 3-4 workers
- Enough parallelism
- Manageable coordination
- Good resource utilization

#### 3. Task Size

**Small Tasks** (1-2 hours):
- Can support more workers
- Quick turnover
- **Optimal**: 4-6 workers

**Medium Tasks** (3-6 hours):
- Balanced workload
- **Optimal**: 3-4 workers

**Large Tasks** (8+ hours):
- Fewer workers needed
- **Optimal**: 2-3 workers

#### 4. Shared Resources

**File Conflicts**:
- More workers = higher conflict risk
- Shared files require coordination
- **Optimal**: 2-3 workers if many shared files

**Isolated Work**:
- Each worker on different files
- **Optimal**: 4-6 workers

---

## Recommended Worker Counts

### Small Project (3-5 tasks, 1-2 days)

**Recommended**: 2-3 workers
- Low coordination overhead
- Fast completion
- Easy to manage

**Example**:
- Worker 1: UI task
- Worker 2: Backend task
- Worker 3: Testing task

---

### Medium Project (6-10 tasks, 3-5 days)

**Recommended**: 3-4 workers
- Good parallelism
- Manageable coordination
- Efficient resource use

**Example**:
- Worker 1: UI tasks
- Worker 2: Navigation/Flow tasks
- Worker 3: Data/Backend tasks
- Worker 4: Testing/Docs tasks

---

### Large Project (10+ tasks, 1+ weeks)

**Recommended**: 4-5 workers (phased)
- Start with 3-4 workers
- Add workers as dependencies complete
- Scale up/down based on progress

**Example**:
- Phase 1 (Days 1-2): 3 workers (UI, Navigation, Data)
- Phase 2 (Days 3-4): 4 workers (add Backend, Testing)
- Phase 3 (Days 5+): 2-3 workers (Integration, Docs, Polish)

---

## Coordination Overhead Analysis

### Formula

**Coordination Overhead = n × (n-1) / 2**

Where n = number of workers

**Examples**:
- 2 workers: 1 coordination pair
- 3 workers: 3 coordination pairs
- 4 workers: 6 coordination pairs
- 5 workers: 10 coordination pairs
- 6 workers: 15 coordination pairs

### Practical Limits

**Low Overhead** (1-3 pairs): 2-3 workers ✅  
**Medium Overhead** (3-6 pairs): 3-4 workers ✅  
**High Overhead** (6+ pairs): 5+ workers ⚠️

**Recommendation**: Keep coordination pairs ≤ 6 (4 workers max for most projects)

---

## Best Practices

### 1. Start Small, Scale Up

**Approach**:
1. Start with 2-3 workers
2. Monitor coordination overhead
3. Add workers if:
   - Tasks are independent
   - No file conflicts
   - Good progress

**Avoid**:
- Starting with too many workers
- Adding workers when conflicts are high
- Ignoring coordination overhead

---

### 2. Match Workers to Task Types

**Strategy**:
- UI tasks → UI worker
- Backend tasks → Backend worker
- Testing tasks → Testing worker

**Benefits**:
- Specialized expertise
- Lower conflict risk
- Better quality

---

### 3. Phase Workers Based on Dependencies

**Strategy**:
- Phase 1: Independent tasks (3-4 workers)
- Phase 2: Dependent tasks (2-3 workers)
- Phase 3: Integration tasks (1-2 workers)

**Example**:
```
Phase 1 (Days 1-2):
- Worker 1: UI design (independent)
- Worker 2: Data models (independent)
- Worker 3: Analytics setup (independent)

Phase 2 (Days 3-4):
- Worker 1: Navigation (depends on UI)
- Worker 2: API integration (depends on data)
- Worker 3: Testing (depends on both)

Phase 3 (Days 5+):
- Worker 1: Integration
- Worker 2: Documentation
```

---

### 4. Monitor and Adjust

**Metrics to Watch**:
- File conflicts (should be low)
- Coordination overhead (should be manageable)
- Task completion rate (should be steady)
- Blocker frequency (should be low)

**Adjust If**:
- Conflicts increase → Reduce workers
- Tasks complete quickly → Can add workers
- Blockers frequent → Reduce workers, focus on dependencies

---

## Driver Recommendations

### Driver Agent Output Format

```
Project: User Onboarding Flow
Tasks Created: 7
Estimated Duration: 3-4 days

Recommended Worker Strategy:
- Phase 1 (Days 1-2): 3 workers
  - Worker 1: UI tasks (Issues #100)
  - Worker 2: Navigation tasks (Issue #101)
  - Worker 3: Data tasks (Issue #102)
  
- Phase 2 (Days 3-4): 4 workers
  - Worker 1: Backend tasks (Issue #103)
  - Worker 2: Analytics tasks (Issue #104)
  - Worker 3: Testing tasks (Issue #105)
  - Worker 4: Documentation (Issue #106)

Coordination Notes:
- Issues #100 and #101 have dependencies (UI → Navigation)
- Issues #102, #103, #104 are independent
- Issue #105 requires all core tasks complete

Risk Assessment:
- Low conflict risk (different file areas)
- Medium coordination overhead (3-4 workers)
- Manageable dependencies
```

---

## Implementation Checklist

### Driver Setup
- [ ] Start driver agent
- [ ] Assign planning task
- [ ] Driver creates GitHub Issues
- [ ] Driver creates TASK_BOARD.md
- [ ] Driver outputs recommendations

### Worker Setup
- [ ] Review driver recommendations
- [ ] Start initial workers (2-3)
- [ ] Monitor coordination
- [ ] Add workers as needed
- [ ] Adjust based on progress

### Monitoring
- [ ] Track file conflicts
- [ ] Monitor coordination overhead
- [ ] Watch task completion rate
- [ ] Adjust worker count as needed

---

## Summary

### Workflow
1. **Driver First**: Plan and decompose project
2. **Then Workers**: Start 2-4 workers based on driver recommendations
3. **Monitor**: Track coordination and adjust

### Optimal Worker Count
- **Small Projects**: 2-3 workers
- **Medium Projects**: 3-4 workers
- **Large Projects**: 4-5 workers (phased)

### Key Principles
- Start small, scale up
- Match workers to task types
- Phase based on dependencies
- Monitor and adjust

---

## Related Documentation

- `docs/architecture/DRIVER_AGENT_ARCHITECTURE.md` - Architecture design
- `docs/development/workflow/AGENT_COORDINATION.md` - Coordination guidelines
- `docs/development/workflow/MULTI_AGENT_WORKFLOW.md` - Multi-agent workflow

