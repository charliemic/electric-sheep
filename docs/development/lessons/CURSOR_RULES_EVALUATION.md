# Cursor Rules Evaluation and Hierarchy

**Date**: 2025-11-21  
**Status**: Evaluation Complete  
**Priority**: High

## Executive Summary

**Total Rules**: 19 cursor rules  
**Always Apply**: 15 rules  
**Conditional**: 4 rules (feature-flags, visual-first-principle, working-patterns-first, python-environment)

**Key Findings**:
1. ✅ Most rules have consistent structure
2. ⚠️ No clear priority hierarchy defined
3. ⚠️ Some overlap between rules (documentation-first vs working-patterns-first)
4. ⚠️ Inconsistent use of `alwaysApply: true` (4 rules missing it)
5. ✅ Good coverage of critical principles

## Rule Categories and Hierarchy

### Tier 1: Foundation Rules (MUST Apply Always)
**These are fundamental workflow rules that must always be followed:**

1. **branching.mdc** - Never work on main, branch isolation
2. **testing.mdc** - Keep tests passing
3. **code-quality.mdc** - Code style and consistency
4. **security.mdc** - Authentication, authorization, secrets
5. **error-handling.mdc** - Error handling patterns
6. **documentation-first.mdc** - Check docs before implementing ⭐ NEW

### Tier 2: Implementation Rules (Apply When Relevant)
**These apply when implementing specific features:**

7. **accessibility.mdc** - UI accessibility requirements
8. **design.mdc** - UX and design principles
9. **api-patterns.mdc** - Remote data patterns
10. **logging.mdc** - Logging requirements
11. **cicd.mdc** - CI/CD verification
12. **frequent-commits.mdc** - Commit frequency
13. **repository-maintenance.mdc** - Cleanup after merges
14. **artifact-duplication.mdc** - Check before creating
15. **emulator-workflow.mdc** - Emulator management

### Tier 3: Specialized Rules (Apply When Using Specific Tools)
**These apply when working with specific technologies:**

16. **python-environment.mdc** - Python/pyenv management
17. **feature-flags.mdc** - Feature flag workflow
18. **visual-first-principle.mdc** - Test automation visual-first
19. **working-patterns-first.mdc** - Evaluate existing patterns

## Priority Hierarchy

### CRITICAL (Must Follow)
1. **Branch isolation** (branching.mdc) - Prevents conflicts
2. **Keep tests passing** (testing.mdc) - Prevents regressions
3. **Documentation first** (documentation-first.mdc) - Prevents wasted effort ⭐ NEW
4. **Security** (security.mdc) - Prevents vulnerabilities

### HIGH (Should Follow)
5. **Code quality** (code-quality.mdc) - Maintains consistency
6. **Error handling** (error-handling.mdc) - Prevents crashes
7. **Accessibility** (accessibility.mdc) - Legal/compliance
8. **CI/CD verification** (cicd.mdc) - Prevents broken deployments

### MEDIUM (Follow When Relevant)
9. **Design/UX** (design.mdc) - User experience
10. **Logging** (logging.mdc) - Debugging support
11. **API patterns** (api-patterns.mdc) - Consistency
12. **Frequent commits** (frequent-commits.mdc) - Safety net
13. **Repository maintenance** (repository-maintenance.mdc) - Cleanliness

### LOW (Follow When Using Specific Tools)
14. **Artifact duplication** (artifact-duplication.mdc) - Organization
15. **Emulator workflow** (emulator-workflow.mdc) - Development tools
16. **Python environment** (python-environment.mdc) - Python-specific
17. **Feature flags** (feature-flags.mdc) - Feature management
18. **Visual-first** (visual-first-principle.mdc) - Test automation
19. **Working patterns first** (working-patterns-first.mdc) - Pattern evaluation

## Consistency Issues Found

### 1. Missing `alwaysApply: true`
**Issue**: 4 rules don't have `alwaysApply: true` in frontmatter
- `feature-flags.mdc` - Should be conditional (only when using feature flags)
- `visual-first-principle.mdc` - Should be conditional (only for test automation)
- `working-patterns-first.mdc` - Should have `alwaysApply: true` (general principle)
- `python-environment.mdc` - Should be conditional (only when using Python)

**Fix**: Add `alwaysApply: true` to `working-patterns-first.mdc` (it's a general principle)

### 2. Overlap Between Rules
**Issue**: `documentation-first.mdc` and `working-patterns-first.mdc` have some overlap

**Analysis**:
- `documentation-first`: Check official docs before implementing
- `working-patterns-first`: Check existing working patterns in codebase

**Resolution**: They complement each other:
- `documentation-first`: External documentation (GitHub, Python, Kotlin docs)
- `working-patterns-first`: Internal codebase patterns

**Recommendation**: Add cross-reference between rules

### 3. Inconsistent CRITICAL Usage
**Issue**: Many rules use "CRITICAL" but no clear hierarchy

**Current Usage**:
- 15 rules use "CRITICAL" in headers
- No distinction between "must always follow" vs "follow when relevant"

**Recommendation**: 
- Use "CRITICAL" only for Tier 1 rules
- Use "IMPORTANT" for Tier 2 rules
- Use "WHEN RELEVANT" for Tier 3 rules

### 4. Formatting Inconsistencies
**Issue**: Some rules have different formatting styles

**Examples**:
- Some use `## CRITICAL:` others use `## ⚠️ CRITICAL:`
- Some have extensive examples, others are brief
- Some have checklists, others don't

**Recommendation**: Standardize formatting (but keep examples flexible)

## Clarity Issues

### 1. Rule Scope Not Always Clear
**Issue**: Some rules don't clearly state when they apply

**Examples**:
- `visual-first-principle.mdc` - Only applies to test automation, not general UI
- `python-environment.mdc` - Only applies when working with Python
- `feature-flags.mdc` - Only applies when implementing new features

**Recommendation**: Add "When This Rule Applies" section to conditional rules

### 2. Examples Could Be More Consistent
**Issue**: Some rules have extensive examples, others have minimal

**Recommendation**: 
- Tier 1 rules: Extensive examples (critical to understand)
- Tier 2 rules: Moderate examples (helpful)
- Tier 3 rules: Brief examples (reference)

## Recommendations

### Immediate Fixes

1. ✅ **Add `alwaysApply: true` to `working-patterns-first.mdc`**
   - It's a general principle that should always apply

2. ✅ **Add cross-references between related rules**
   - `documentation-first.mdc` ↔ `working-patterns-first.mdc`
   - `api-patterns.mdc` ↔ `error-handling.mdc`
   - `accessibility.mdc` ↔ `design.mdc`

3. ✅ **Standardize CRITICAL usage**
   - Tier 1: "CRITICAL"
   - Tier 2: "IMPORTANT" or "REQUIRED"
   - Tier 3: "WHEN RELEVANT" or "CONDITIONAL"

4. ✅ **Add "When This Rule Applies" to conditional rules**
   - `visual-first-principle.mdc`
   - `python-environment.mdc`
   - `feature-flags.mdc`

### Long-Term Improvements

1. **Create rule hierarchy document**
   - Document which rules take precedence
   - Clarify conflict resolution

2. **Add rule dependencies**
   - Some rules depend on others (e.g., testing depends on branching)

3. **Create rule quick reference**
   - One-page summary of all rules
   - Organized by tier/priority

4. **Regular rule reviews**
   - Quarterly review of rule effectiveness
   - Update based on lessons learned

## Rule Dependencies

### Dependency Graph
```
branching.mdc (foundation)
  ├── testing.mdc (needs branch isolation)
  ├── cicd.mdc (needs branch isolation)
  └── repository-maintenance.mdc (needs branch isolation)

code-quality.mdc (foundation)
  ├── accessibility.mdc (code quality includes accessibility)
  ├── design.mdc (code quality includes design)
  └── error-handling.mdc (code quality includes error handling)

documentation-first.mdc (foundation) ⭐ NEW
  ├── working-patterns-first.mdc (complements)
  └── api-patterns.mdc (should check docs)

security.mdc (foundation)
  └── error-handling.mdc (security includes error handling)
```

## Related Documentation

- `.cursor/rules/` - All cursor rules
- `docs/development/lessons/DOCUMENTATION_FIRST_PRINCIPLE.md` - Documentation-first lesson
- `AI_AGENT_GUIDELINES.md` - Complete agent guidelines

