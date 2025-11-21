# Smart Prompts Architecture - Quick Reference

## Core Concept

**AI uses sub-prompt to evaluate context** → Detects multi-agent context → Decides prompt level → Executes appropriately

**Project Context:**
- **Learning project** - No real end users
- **More aggressive optimization** - 85% routine operations → PROCEED (vs. 70% for production)

## Sub-Prompt (Internal AI Evaluation)

When user requests action, AI internally evaluates:

```
Evaluate [ACTION] given context:
- What is being changed?
- What's the current state?
- What are the risks?
- What's the user's intent?

Decide: PROCEED / QUICK_CONFIRM / FULL_REVIEW / BLOCK
```

## Decision Outcomes

| Decision | Action | Example |
|----------|--------|---------|
| **PROCEED** | Execute directly | `✅ Added comment` |
| **QUICK_CONFIRM** | Brief summary | `📋 Commit 3 files? (y/n)` |
| **FULL_REVIEW** | Detailed summary | `📋 SUMMARY: [details]... Proceed? (y/n)` |
| **BLOCK** | Prevent + explain | `❌ CRITICAL: [why blocked]... Fix? (y/n)` |

## Context Factors (For AI Evaluation)

The AI considers:
- **Multi-agent context**: 2+ agents active? (lowers thresholds, 20-45 sec context switching cost)
- **Change scope**: Single file vs system-wide
- **Change type**: Docs vs feature vs config
- **Dependencies**: Isolated vs core infrastructure
- **State**: Branch status, conflicts, CI
- **Coordination**: Other agents, shared files
- **Complexity**: Simple vs complex
- **User confidence**: Explicit vs uncertain
- **Learning project**: No real users = more aggressive optimization

## Key Principles

1. **No rigid scoring** - AI evaluates holistically
2. **Natural reasoning** - Uses AI capabilities, not formulas
3. **Context-aware** - Considers full situation
4. **Adaptive** - Learns from patterns

## Learning Project Thresholds

**More Aggressive (vs. Production):**
- **85% commits** → PROCEED (vs. 70% for production)
- **80% pushes** → PROCEED (vs. 50% for production)
- **70% CI/CD changes** → PROCEED (vs. 50% QUICK_CONFIRM for production)

**Still Protected:**
- Main branch (rule-required, always block)
- Shared files (coordination needed)
- Security (still need review)
- Major architecture (learning opportunity)

## Full Documentation

- `docs/development/workflow/SMART_PROMPTS_ARCHITECTURE.md` - Complete architecture
- `docs/development/workflow/SMART_PROMPTS_COMPREHENSIVE_OPTIMIZATION.md` - Optimization analysis (learning project context)

