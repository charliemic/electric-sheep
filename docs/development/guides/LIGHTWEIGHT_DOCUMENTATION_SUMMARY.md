# Lightweight Documentation - Executive Summary

**Date**: 2025-01-27  
**Status**: Proposal Ready for Review

## The Problem

Current documentation is **too heavy**:
- 985-line `AI_AGENT_GUIDELINES.md`
- 19 cursor rules averaging 200+ lines each
- 100+ documentation files
- Handover prompts that duplicate information
- High maintenance burden

**Research finding**: Optimal documentation is **lightweight, structured, and just-in-time** - not exhaustive upfront documentation.

## The Solution

### Three Core Changes

1. **`llms.txt`** - AI navigation file (200 lines, replaces 985-line guide)
2. **Simplified rules** - Focus on requirements, link to code (60-70% reduction)
3. **Just-in-time context** - Provide context when needed, not upfront

### Key Principles

- ✅ **AI can read code** - Don't document what code shows
- ✅ **Just-in-time** - Context when needed, not upfront
- ✅ **Structured summaries** - Concise, standardized formats
- ✅ **Single source of truth** - Eliminate duplication
- ✅ **Living documentation** - Auto-generate from code

## Proof of Concept

### Created Files

1. **`llms.txt`** (root) - AI navigation guide
   - Points to key resources
   - Quick reference for common tasks
   - Code-first approach guidance

2. **`docs/development/guides/LIGHTWEIGHT_DOCUMENTATION_PROPOSAL.md`**
   - Full proposal with research backing
   - Migration strategy
   - Expected benefits

3. **`docs/development/guides/BRANCHING_RULE_SIMPLIFIED_EXAMPLE.md`**
   - Example of simplified rule (301 → 80 lines)
   - Shows what to keep/remove
   - Demonstrates approach

## Expected Impact

### Metrics
- **Documentation size**: 60-70% reduction (5,000 → 2,000 lines)
- **AI context loading**: 3-5x faster
- **Maintenance time**: 50% reduction
- **Human onboarding**: Similar or better (better structured)

### Benefits

**For AI Agents:**
- Faster context loading
- Less confusion (single source of truth)
- Better code understanding
- Just-in-time context

**For Humans:**
- Less maintenance burden
- Easier to find information
- Living documentation (auto-updated)
- Focus on decisions, not implementation

## Research Backing

1. **Model Cards & Data Cards** (arXiv) - Structured, concise summaries
2. **llms.txt format** (dev.to) - AI-friendly navigation
3. **Just-in-Time Documentation** (IBM Research) - Context when needed
4. **Living Documentation** (Partnership on AI) - Auto-generated, always current
5. **Reciprocal Learning** (Wikipedia) - Human-AI collaborative improvement

## Next Steps

### Phase 1: Validate (1 week)
1. ✅ Review `llms.txt` with AI agents
2. ✅ Test simplified rule example
3. ✅ Measure context loading time
4. ✅ Gather feedback

### Phase 2: Migrate (2-3 weeks)
1. Simplify all cursor rules
2. Convert architecture docs to ADRs
3. Remove heavy documentation
4. Update references

### Phase 3: Optimize (ongoing)
1. Auto-generate docs from code
2. Create living documentation
3. Measure and iterate

## Key Insight

> **Documentation should be a map, not the territory.**
> 
> The code is the territory - documentation should just help navigate it.

## Files to Review

1. **`llms.txt`** - Start here (proof of concept)
2. **`docs/development/guides/LIGHTWEIGHT_DOCUMENTATION_PROPOSAL.md`** - Full details
3. **`docs/development/guides/BRANCHING_RULE_SIMPLIFIED_EXAMPLE.md`** - Example simplification

## Questions to Answer

1. Does `llms.txt` provide sufficient navigation for AI agents?
2. Is the simplified rule example clear and actionable?
3. What information is missing from the lightweight approach?
4. What documentation is actually referenced vs. never read?

---

**Recommendation**: Start with `llms.txt` validation, then proceed with rule simplification based on feedback.

