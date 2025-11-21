# Rule Simplification - Cost/Benefit Analysis & Priority

**Date**: 2025-01-27  
**Status**: Prioritization Complete

## Evaluation Criteria

### Cost Factors (Effort)
- **Lines to simplify**: Current size - target size
- **Complexity**: Number of examples, cross-references, inline code
- **Dependencies**: How many other rules/docs reference it
- **Risk**: Chance of breaking something if simplified incorrectly

### Benefit Factors (Impact)
- **Size reduction**: Lines removed (contribution to 68% target)
- **Usage frequency**: How often agents reference this rule
- **Maintenance burden**: How often it needs updates
- **Clarity improvement**: How much clearer it becomes

### Priority Score
**Formula**: `(Benefit × Usage) / (Cost × Risk)`

## Current State

**Total rules**: 19  
**Total lines**: 3,794  
**Target**: ~1,200 lines (68% reduction)  
**Remaining**: ~2,600 lines to simplify

## Priority Ranking

### Tier 1: High Impact, Low Cost (Do First) ⭐⭐⭐

#### 1. Artifact Duplication (295 lines → ~90 lines)
- **Cost**: Medium (many examples, but clear pattern)
- **Benefit**: High (295 lines = 7.8% of total)
- **Usage**: Medium (referenced when creating files)
- **Risk**: Low (mostly examples, not critical logic)
- **Priority Score**: 9.5/10
- **Effort**: 2-3 hours
- **Impact**: 205 lines removed (8% of target reduction)

#### 2. Design (292 lines → ~90 lines)
- **Cost**: Medium (design system details, but can link to code)
- **Benefit**: High (292 lines = 7.7% of total)
- **Usage**: High (referenced for all UI work)
- **Risk**: Medium (design system is important, but code is source of truth)
- **Priority Score**: 9.0/10
- **Effort**: 2-3 hours
- **Impact**: 202 lines removed (8% of target reduction)

#### 3. Accessibility (280 lines → ~90 lines)
- **Cost**: Medium-High (comprehensive requirements, but can link to docs)
- **Benefit**: High (280 lines = 7.4% of total)
- **Usage**: High (referenced for all UI work)
- **Risk**: Medium (accessibility is critical, but detailed docs exist)
- **Priority Score**: 8.5/10
- **Effort**: 3-4 hours
- **Impact**: 190 lines removed (7.5% of target reduction)

### Tier 2: Medium Impact, Low-Medium Cost (Do Next) ⭐⭐

#### 4. Emulator Workflow (254 lines → ~80 lines)
- **Cost**: Low (mostly script references, can link)
- **Benefit**: Medium (254 lines = 6.7% of total)
- **Usage**: Medium (referenced for testing/development)
- **Risk**: Low (scripts are source of truth)
- **Priority Score**: 8.0/10
- **Effort**: 1-2 hours
- **Impact**: 174 lines removed (7% of target reduction)

#### 5. Frequent Commits (238 lines → ~70 lines)
- **Cost**: Low (mostly examples, clear pattern)
- **Benefit**: Medium (238 lines = 6.3% of total)
- **Usage**: Medium (referenced for commit workflow)
- **Risk**: Low (simple concept, examples in git history)
- **Priority Score**: 7.5/10
- **Effort**: 1-2 hours
- **Impact**: 168 lines removed (6.5% of target reduction)

#### 6. Repository Maintenance (236 lines → ~70 lines)
- **Cost**: Low-Medium (cleanup procedures, but scripts handle it)
- **Benefit**: Medium (236 lines = 6.2% of total)
- **Usage**: Low-Medium (referenced after merges)
- **Risk**: Low (scripts are source of truth)
- **Priority Score**: 7.0/10
- **Effort**: 2 hours
- **Impact**: 166 lines removed (6.5% of target reduction)

### Tier 3: Lower Impact, Low Cost (Do Later) ⭐

#### 7. API Patterns (222 lines → ~70 lines)
- **Cost**: Low (patterns in code, can link)
- **Benefit**: Medium (222 lines = 5.8% of total)
- **Usage**: Medium (referenced for API work)
- **Risk**: Low (code is source of truth)
- **Priority Score**: 6.5/10
- **Effort**: 1-2 hours
- **Impact**: 152 lines removed (6% of target reduction)

#### 8. Testing (215 lines → ~70 lines)
- **Cost**: Medium (testing is important, but patterns in code)
- **Benefit**: Medium (215 lines = 5.7% of total)
- **Usage**: High (referenced for all test work)
- **Risk**: Medium (testing is critical, but code examples exist)
- **Priority Score**: 6.0/10
- **Effort**: 2-3 hours
- **Impact**: 145 lines removed (5.5% of target reduction)

#### 9. Python Environment (191 lines → ~60 lines)
- **Cost**: Low (setup scripts exist)
- **Benefit**: Low-Medium (191 lines = 5.0% of total)
- **Usage**: Low (only for Python work)
- **Risk**: Low (scripts are source of truth)
- **Priority Score**: 5.5/10
- **Effort**: 1 hour
- **Impact**: 131 lines removed (5% of target reduction)

### Tier 4: Already Small (Review Only) ✅

#### 10-19. Remaining Rules (86-177 lines each)
- **Total**: ~1,500 lines
- **Target**: ~500 lines (67% reduction)
- **Cost**: Low (already relatively small)
- **Benefit**: Medium (collective impact)
- **Priority Score**: 4.0/10
- **Effort**: 1 hour each (10 hours total)
- **Impact**: ~1,000 lines removed (40% of target reduction)

## Recommended Approach

### Phase 1: High-Impact Rules (This Week)
1. Artifact Duplication (295 → 90 lines) - 2-3 hours
2. Design (292 → 90 lines) - 2-3 hours
3. Accessibility (280 → 90 lines) - 3-4 hours

**Total**: 7-10 hours  
**Impact**: ~600 lines removed (23% of target reduction)

### Phase 2: Medium-Impact Rules (Next Week)
4. Emulator Workflow (254 → 80 lines) - 1-2 hours
5. Frequent Commits (238 → 70 lines) - 1-2 hours
6. Repository Maintenance (236 → 70 lines) - 2 hours

**Total**: 4-6 hours  
**Impact**: ~500 lines removed (19% of target reduction)

### Phase 3: Remaining Rules (Following Week)
7-19. All remaining rules - 10-15 hours  
**Impact**: ~1,500 lines removed (58% of target reduction)

## Cost Summary

**Total effort**: 21-31 hours  
**Total impact**: ~2,600 lines removed (100% of target)  
**Efficiency**: ~84-124 lines per hour

## Risk Mitigation

### High-Risk Rules
- **Accessibility**: Link to comprehensive docs, don't remove requirements
- **Testing**: Link to test examples, preserve critical patterns
- **Design**: Link to design system code, preserve standards

### Low-Risk Rules
- **Artifact Duplication**: Mostly examples, scripts handle enforcement
- **Emulator Workflow**: Scripts are source of truth
- **Repository Maintenance**: Scripts handle cleanup

## Success Metrics

- **Target**: 68% reduction (3,794 → ~1,200 lines)
- **Current**: 1 rule simplified (branching: 75% reduction)
- **Remaining**: 18 rules to simplify
- **Progress**: 5% complete

## Next Steps

1. **Start with Tier 1** (artifact-duplication, design, accessibility)
2. **Commit after each rule** (frequent commits)
3. **Test with AI agents** (validate navigation)
4. **Measure impact** (lines reduced, navigation speed)

---

**Recommendation**: Start with artifact-duplication (highest priority score, low risk, high impact)

