# Smart Prompts Architecture - Quick Reference

## Core Concept

**AI uses sub-prompt to evaluate context** → Decides prompt level → Executes appropriately

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
- **Change scope**: Single file vs system-wide
- **Change type**: Docs vs feature vs config
- **Dependencies**: Isolated vs core infrastructure
- **State**: Branch status, conflicts, CI
- **Coordination**: Other agents, shared files
- **Complexity**: Simple vs complex
- **User confidence**: Explicit vs uncertain

## Key Principles

1. **No rigid scoring** - AI evaluates holistically
2. **Natural reasoning** - Uses AI capabilities, not formulas
3. **Context-aware** - Considers full situation
4. **Adaptive** - Learns from patterns

## Full Documentation

See `docs/development/workflow/SMART_PROMPTS_ARCHITECTURE.md` for complete architecture.

