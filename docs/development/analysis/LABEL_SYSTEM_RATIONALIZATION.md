# Label System Rationalization

**Date**: 2025-01-20  
**Status**: Analysis and Recommendations

## Current State Analysis

### Existing Labels

**Default GitHub Labels** (9 labels):
- `bug` - Something isn't working
- `documentation` - Improvements or additions to documentation
- `duplicate` - This issue or pull request already exists
- `enhancement` - New feature or request
- `good first issue` - Good for newcomers
- `help wanted` - Extra attention is needed
- `invalid` - This doesn't seem right
- `question` - Further information is requested
- `wontfix` - This will not be worked on

**Custom Labels Created** (14 labels):
- Priority: `priority-1`, `priority-2`, `priority-3`
- Type: `type:technical`, `type:infrastructure`, `type:feature`
- Area: `area:distribution`, `area:monitoring`, `area:legal`, `area:security`
- Effort: `effort:small` (1-2 days), `effort:medium` (2-5 days), `effort:large` (5+ days)
- Status: `status:not-started`

### Documented Intended Structure

**From `BACKLOG_GITHUB_ISSUES_APPROACH.md`**:
- Priority: `priority-1` through `priority-5`
- Area: `area:quick-entry`, `area:context-capture`, `area:visualization`, `area:engagement`, `area:insights`, `area:architecture`
- Type: `type:feature`, `type:enhancement`, `type:technical`, `type:ux`
- Status: `status:not-started`, `status:in-progress`, `status:blocked`, `status:review`, `status:completed`
- Effort: `effort:small` (1-2 weeks), `effort:medium` (2-4 weeks), `effort:large` (4-8 weeks), `effort:xlarge` (8+ weeks)

## Issues Identified

### 1. **Effort Label Mismatch** (CRITICAL)
- **Created**: Days (1-2 days, 2-5 days, 5+ days)
- **Documented**: Weeks (1-2 weeks, 2-4 weeks, 4-8 weeks, 8+ weeks)
- **Problem**: Inconsistent timeframes make effort estimation confusing
- **Impact**: High - Effort labels are used for planning and prioritization

### 2. **Missing Priority Labels**
- **Created**: Only `priority-1`, `priority-2`, `priority-3`
- **Documented**: `priority-1` through `priority-5`
- **Problem**: Cannot label medium/low priority items
- **Impact**: Medium - Limits prioritization granularity

### 3. **Missing Area Labels**
- **Created**: `area:distribution`, `area:monitoring`, `area:legal`, `area:security`
- **Documented**: `area:quick-entry`, `area:context-capture`, `area:visualization`, `area:engagement`, `area:insights`, `area:architecture`
- **Problem**: Different area labels for different purposes (product vs infrastructure)
- **Impact**: Medium - Need both product and infrastructure areas

### 4. **Missing Type Labels**
- **Created**: `type:technical`, `type:infrastructure`, `type:feature`
- **Documented**: `type:feature`, `type:enhancement`, `type:technical`, `type:ux`
- **Problem**: Missing `type:enhancement` and `type:ux`
- **Impact**: Low - Can use existing labels, but less granular

### 5. **Missing Status Labels**
- **Created**: Only `status:not-started`
- **Documented**: `status:not-started`, `status:in-progress`, `status:blocked`, `status:review`, `status:completed`
- **Problem**: Cannot track work progress
- **Impact**: High - Status tracking is essential for workflow

### 6. **Missing Effort Label**
- **Created**: `effort:small`, `effort:medium`, `effort:large`
- **Documented**: `effort:small`, `effort:medium`, `effort:large`, `effort:xlarge`
- **Problem**: No label for very large efforts (8+ weeks)
- **Impact**: Low - Can use `effort:large` for now

### 7. **Default Label Conflicts**
- **Default**: `enhancement` (New feature or request)
- **Custom**: `type:feature` (New feature)
- **Problem**: Overlap in meaning
- **Impact**: Low - Can use both, but may cause confusion

## Rationalized Label System

### Core Principles

1. **Consistency**: All labels follow naming conventions
2. **Completeness**: All documented labels are created
3. **Clarity**: Labels are self-explanatory
4. **Flexibility**: Support both product and infrastructure work
5. **Workflow**: Support full workflow (not-started → in-progress → review → completed)

### Recommended Label Structure

#### Priority Labels (5 labels)
- `priority-1` - Critical (High impact, medium effort) - Red (#d73a4a)
- `priority-2` - High (High impact, medium effort) - Blue (#1d76db)
- `priority-3` - Medium (High impact, high effort) - Yellow (#fbca04)
- `priority-4` - Low (Medium impact, medium effort) - Orange (#fbcb43)
- `priority-5` - Nice to Have (Medium impact, high effort) - Red-Orange (#d93f0b)

**Decision**: Use weeks for effort (more realistic for planning)

#### Type Labels (5 labels)
- `type:feature` - New feature - Light Blue (#a2eeef)
- `type:enhancement` - Enhancement to existing feature - Blue (#1d76db)
- `type:technical` - Technical improvement - Green (#0e8a16)
- `type:infrastructure` - Infrastructure work - Blue (#1d76db)
- `type:ux` - UX improvement - Yellow (#fbcb43)

**Decision**: Keep both `type:technical` and `type:infrastructure` (different purposes)

#### Area Labels (10 labels - Product + Infrastructure)

**Product Areas** (6 labels):
- `area:quick-entry` - Quick entry features - Green (#0e8a16)
- `area:context-capture` - Context capture features - Blue (#1d76db)
- `area:visualization` - Charts and visualizations - Yellow (#fbca04)
- `area:engagement` - Reminders and gamification - Orange (#fbcb43)
- `area:insights` - Pattern recognition and AI - Red-Orange (#d93f0b)
- `area:architecture` - Technical improvements - Red (#b60205)

**Infrastructure Areas** (4 labels):
- `area:distribution` - Distribution and deployment - Yellow (#fbca04)
- `area:monitoring` - Monitoring and analytics - Orange (#fbcb43)
- `area:legal` - Legal and compliance - Red-Orange (#d93f0b)
- `area:security` - Security hardening - Red (#b60205)

**Decision**: Keep both product and infrastructure areas (different contexts)

#### Status Labels (5 labels)
- `status:not-started` - Not yet started - Gray (#cfd3d7)
- `status:in-progress` - Currently being worked on - Yellow (#fbca04)
- `status:blocked` - Blocked by dependency - Red (#d93f0b)
- `status:review` - In code review - Blue (#1d76db)
- `status:completed` - Completed and merged - Green (#0e8a16)

**Decision**: Essential for workflow tracking

#### Effort Labels (4 labels - WEEKS, not days)
- `effort:small` - 1-2 weeks - Green (#0e8a16)
- `effort:medium` - 2-4 weeks - Yellow (#fbca04)
- `effort:large` - 4-8 weeks - Orange (#fbcb43)
- `effort:xlarge` - 8+ weeks - Red-Orange (#d93f0b)

**Decision**: Use weeks (more realistic for planning, matches documentation)

### Default GitHub Labels

**Keep** (useful):
- `bug` - For bug reports
- `documentation` - For documentation work
- `duplicate` - For duplicate issues
- `good first issue` - For onboarding
- `help wanted` - For community contributions
- `question` - For questions/discussions

**Consider Removing** (conflicts with custom labels):
- `enhancement` - Conflicts with `type:feature` and `type:enhancement`
- `invalid` - Can use `wontfix` instead
- `wontfix` - Keep (useful for declined issues)

## Action Plan

### Phase 1: Fix Critical Issues

1. **Update Effort Labels** (Change from days to weeks)
   - Update `effort:small` description: "1-2 weeks" (currently "1-2 days")
   - Update `effort:medium` description: "2-4 weeks" (currently "2-5 days")
   - Update `effort:large` description: "4-8 weeks" (currently "5+ days")
   - Create `effort:xlarge`: "8+ weeks"

2. **Create Missing Status Labels**
   - Create `status:in-progress`
   - Create `status:blocked`
   - Create `status:review`
   - Create `status:completed`

3. **Create Missing Priority Labels**
   - Create `priority-4`
   - Create `priority-5`

### Phase 2: Complete Type and Area Labels

4. **Create Missing Type Labels**
   - Create `type:enhancement`
   - Create `type:ux`

5. **Create Product Area Labels**
   - Create `area:quick-entry`
   - Create `area:context-capture`
   - Create `area:visualization`
   - Create `area:engagement`
   - Create `area:insights`
   - Create `area:architecture`

### Phase 3: Clean Up Default Labels

6. **Handle Default Label Conflicts**
   - Keep `enhancement` but document when to use vs `type:enhancement`
   - Or remove `enhancement` if we prefer custom labels

## Label Usage Guidelines

### When to Use Each Label Type

**Priority Labels** (Required - one per issue):
- Use `priority-1` for critical blocking issues
- Use `priority-2` for high-impact features
- Use `priority-3` for medium-priority work
- Use `priority-4` for low-priority improvements
- Use `priority-5` for nice-to-have features

**Type Labels** (Required - one per issue):
- Use `type:feature` for new features
- Use `type:enhancement` for improving existing features
- Use `type:technical` for technical improvements (code quality, refactoring)
- Use `type:infrastructure` for infrastructure work (CI/CD, tooling, setup)
- Use `type:ux` for UX improvements

**Area Labels** (Optional - one or more per issue):
- Use product areas for product features (`area:quick-entry`, etc.)
- Use infrastructure areas for infrastructure work (`area:distribution`, etc.)
- Can use multiple areas if issue spans multiple areas

**Status Labels** (Required - one per issue):
- Use `status:not-started` for new issues
- Use `status:in-progress` when work begins
- Use `status:blocked` when blocked by dependency
- Use `status:review` when PR is in review
- Use `status:completed` when merged (or close issue)

**Effort Labels** (Optional - one per issue):
- Use `effort:small` for 1-2 weeks of work
- Use `effort:medium` for 2-4 weeks of work
- Use `effort:large` for 4-8 weeks of work
- Use `effort:xlarge` for 8+ weeks of work

### Label Combinations

**Example Issue Labels**:
```
priority-1, type:technical, area:distribution, effort:small, status:not-started
```

**Product Feature**:
```
priority-2, type:feature, area:quick-entry, effort:medium, status:not-started
```

**Infrastructure Work**:
```
priority-1, type:infrastructure, area:distribution, effort:medium, status:in-progress
```

**Bug Fix**:
```
priority-1, type:technical, bug, effort:small, status:not-started
```

## Summary

### Current State
- 14 custom labels created
- 9 default GitHub labels
- **Issues**: Effort in days (should be weeks), missing status labels, missing priority-4/5, missing product area labels

### Target State
- 29 custom labels (priority: 5, type: 5, area: 10, status: 5, effort: 4)
- 6-9 default labels (keep useful ones)
- **Complete**: All documented labels created, consistent timeframes, full workflow support

### Next Steps
1. Update effort labels (days → weeks)
2. Create missing status labels
3. Create missing priority labels
4. Create missing type labels
5. Create product area labels
6. Document label usage guidelines

