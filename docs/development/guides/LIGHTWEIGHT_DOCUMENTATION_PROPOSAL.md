# Lightweight Documentation Proposal

**Date**: 2025-01-27  
**Status**: Proposal  
**Goal**: Reduce documentation overhead while maintaining effectiveness for both human and AI agents

## Problem Statement

Current documentation approach is too heavy:
- **985-line** `AI_AGENT_GUIDELINES.md`
- **19 cursor rules** with extensive detail
- **Massive docs/** directory structure (100+ files)
- **Handover prompts** that duplicate information
- **Maintenance burden** - keeping everything in sync

**Research finding**: Overly detailed documentation can overwhelm users while insufficient documentation may hinder AI performance. Optimal solution: **lightweight, structured, just-in-time** documentation.

## Research-Based Solution

### Core Principles

1. **AI Can Read Code** - Don't document what code already tells us
2. **Just-in-Time Context** - Provide context when needed, not upfront
3. **Structured Summaries** - Use concise, standardized formats
4. **Living Documentation** - Auto-generate from code where possible
5. **Single Source of Truth** - Eliminate duplication

## Proposed Structure

### 1. `llms.txt` - AI Navigation File

**Purpose**: Minimal file that tells AI agents where to find what they need.

**Location**: Root of repository

**Content** (max 200 lines):
```
# Electric Sheep - AI Agent Guide

## Quick Start
- Rules: `.cursor/rules/*.mdc`
- Architecture: `docs/architecture/` (key decisions only)
- Setup: `docs/development/setup/README.md`

## Critical Rules (Always Apply)
- Branching: `.cursor/rules/branching.mdc`
- Code Quality: `.cursor/rules/code-quality.mdc`
- Security: `.cursor/rules/security.mdc`

## When You Need...
- **Setup help**: `docs/development/setup/README.md`
- **Architecture decisions**: `docs/architecture/decisions/`
- **Testing patterns**: `.cursor/rules/testing.mdc`
- **Error handling**: `.cursor/rules/error-handling.mdc`

## Code-First Approach
- Read code before reading docs
- Use codebase search for patterns
- Check existing implementations first
- Document only what code doesn't show

## Key Patterns
- ViewModel pattern: See `app/src/main/.../ui/viewmodel/`
- Repository pattern: See `app/src/main/.../data/repository/`
- Error handling: See `app/src/main/.../util/NetworkError.kt`
```

### 2. Simplified Cursor Rules

**Current**: 19 detailed rule files  
**Proposed**: Keep rules focused on **what** and **why**, not exhaustive examples

**Structure per rule**:
```markdown
---
alwaysApply: true/false
---

# Rule Name

## When This Applies
[Brief context - 2-3 lines]

## Critical Requirements
- ✅ Requirement 1
- ✅ Requirement 2

## Implementation
[Link to code examples, not inline examples]

## Related
- Other rules that interact
- Code patterns to follow
```

**Example - Simplified Branching Rule**:
```markdown
---
alwaysApply: true
---

# Branch Isolation

## When This Applies
Before making ANY changes to code.

## Critical Requirements
- ✅ Never work on `main` branch
- ✅ Create feature branch: `git checkout -b feature/<description>`
- ✅ Use worktree for isolation: `./scripts/create-worktree.sh <name>`

## Implementation
See: `scripts/pre-work-check.sh` (automated validation)
See: `scripts/create-worktree.sh` (isolation helper)

## Related
- `.cursor/rules/cicd.mdc` - CI/CD workflow
- `.cursor/rules/repository-maintenance.mdc` - Cleanup
```

### 3. Architecture Decision Records (ADRs)

**Current**: Many architecture docs with overlapping content  
**Proposed**: Concise ADRs (max 1 page each)

**Format**:
```markdown
# ADR-001: Data Storage Decision

**Status**: Accepted  
**Date**: 2025-01-15  
**Context**: Need persistent storage for user data  
**Decision**: Use Room database with Supabase sync  
**Rationale**: [2-3 sentences]  
**Alternatives Considered**: [Brief list]  
**Consequences**: [Key implications]
```

**Location**: `docs/architecture/decisions/` (one file per decision)

### 4. Living Documentation

**Auto-generate from code**:
- API documentation from code comments
- Test coverage reports
- Architecture diagrams from code structure
- Dependency graphs

**Tools**:
- Dokka for Kotlin API docs
- CodeQL for architecture analysis
- Automated test reports

### 5. Just-in-Time Context Files

**Instead of**: Long handover prompts  
**Use**: Context files that AI can read when needed

**Example**: `CONTEXT.md` in feature branch root
```markdown
# Feature: User Authentication

## Current State
- [ ] Login screen implemented
- [ ] OAuth flow in progress
- [ ] Tests pending

## Key Files
- `app/.../ui/screens/LoginScreen.kt`
- `app/.../data/repository/AuthRepository.kt`

## Decisions Made
- Using Supabase Auth (see ADR-002)
- Chrome Custom Tabs for OAuth (security requirement)

## Next Steps
1. Complete OAuth callback handling
2. Add error handling
3. Write integration tests
```

## Migration Strategy

### Phase 1: Create `llms.txt` (1 day)
- [ ] Create root `llms.txt` file
- [ ] Point to key resources
- [ ] Test with AI agent

### Phase 2: Simplify Rules (1 week)
- [ ] Review each rule file
- [ ] Extract examples to code
- [ ] Reduce to essentials
- [ ] Add links to code patterns

### Phase 3: Consolidate Architecture Docs (1 week)
- [ ] Convert to ADR format
- [ ] Remove duplicates
- [ ] Archive outdated docs

### Phase 4: Remove Heavy Docs (1 day)
- [ ] Archive `AI_AGENT_GUIDELINES.md` (replace with `llms.txt`)
- [ ] Remove handover prompts (use context files)
- [ ] Update references

## Expected Benefits

### For AI Agents
- ✅ Faster context loading (read `llms.txt` first)
- ✅ Less confusion (single source of truth)
- ✅ Better code understanding (read code, not docs)
- ✅ Just-in-time context (read when needed)

### For Humans
- ✅ Less maintenance burden
- ✅ Easier to find information
- ✅ Living documentation (auto-updated)
- ✅ Focus on decisions, not implementation

### Metrics
- **Documentation size**: Reduce by 60-70%
- **Maintenance time**: Reduce by 50%
- **AI context loading**: 3-5x faster
- **Human onboarding**: Similar or better (structured better)

## Comparison

### Current Approach
```
AI_AGENT_GUIDELINES.md (985 lines)
+ 19 cursor rules (avg 200 lines each = 3,800 lines)
+ docs/ directory (100+ files)
+ Handover prompts
= ~5,000+ lines of documentation
```

### Proposed Approach
```
llms.txt (200 lines)
+ 19 simplified rules (avg 50 lines = 950 lines)
+ ADRs (20 files × 30 lines = 600 lines)
+ Context files (as needed, ~100 lines each)
= ~2,000 lines (60% reduction)
```

## Research Sources

1. **Model Cards & Data Cards** (arXiv) - Structured, concise summaries
2. **llms.txt format** (dev.to) - AI-friendly navigation
3. **Just-in-Time Documentation** (IBM Research) - Context when needed
4. **Living Documentation** (Partnership on AI) - Auto-generated, always current
5. **Reciprocal Learning** (Wikipedia) - Human-AI collaborative improvement

## Next Steps

1. **Review this proposal** with team
2. **Create `llms.txt`** as proof of concept
3. **Simplify one rule** as example
4. **Measure impact** (context loading time, maintenance time)
5. **Iterate** based on feedback

## Questions to Answer

1. What information do AI agents actually need vs. what they can infer from code?
2. Which docs are referenced most vs. which are never read?
3. Can we auto-generate more documentation from code?
4. What's the minimum viable documentation for onboarding?

---

**Key Insight**: Documentation should be a **map**, not a **territory**. The code is the territory - documentation should just help navigate it.

