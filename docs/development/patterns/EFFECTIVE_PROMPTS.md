# Effective Prompt Patterns

**Date**: 2025-11-21  
**Status**: Living Document  
**Purpose**: Document effective AI interaction patterns

## How to Use This Document

- **Add patterns** that work well
- **Update patterns** based on experience
- **Reference patterns** when starting similar work
- **Review regularly** to identify improvements

## Pattern Categories

### 1. Planning Patterns

#### Pattern: Documentation-First Request

**When to use**: Before implementing features or fixing bugs

**Example**:
```
Before implementing, check the official documentation for [tool/framework].
Verify version compatibility and follow recommended patterns.
```

**Why it works**: 
- Reduces trial-and-error
- Ensures compatibility
- Follows best practices

**Related rules**: `.cursor/rules/documentation-first.mdc`

#### Pattern: Working Patterns First

**When to use**: When implementing similar functionality

**Example**:
```
Check existing codebase for similar implementations.
Use the same pattern as [existing feature].
```

**Why it works**:
- Consistency
- Reuses proven approaches
- Faster implementation

**Related rules**: `.cursor/rules/working-patterns-first.mdc`

### 2. Implementation Patterns

#### Pattern: Incremental Implementation

**When to use**: For complex features

**Example**:
```
Implement in small steps:
1. First, add [basic functionality]
2. Then, add [enhancement]
3. Finally, add [polish]
```

**Why it works**:
- Easier to review
- Catch issues early
- Testable increments

### 3. Review Patterns

#### Pattern: Context-Rich Handover

**When to use**: When switching agents or tasks

**Example**:
```
Here's what's done:
- [Completed items]
- [Current state]
- [Next steps]
- [Key files]
```

**Why it works**:
- Clear context
- No duplicate work
- Faster continuation

**Related rules**: `docs/development/workflow/AGENT_HANDOVER_PATTERNS.md`

### 4. Debugging Patterns

#### Pattern: Systematic Investigation

**When to use**: When debugging issues

**Example**:
```
Let's investigate systematically:
1. Check [obvious causes]
2. Review [logs/errors]
3. Test [hypothesis]
```

**Why it works**:
- Structured approach
- Avoids random fixes
- Faster resolution

## Adding New Patterns

When you find an effective pattern:

1. **Document it** in this file
2. **Categorize it** appropriately
3. **Explain why it works**
4. **Reference related rules**
5. **Update weekly review** with pattern

## Pattern Review

**Monthly review:**
- Which patterns are used most?
- Which patterns are most effective?
- Which patterns need updating?
- Are there new patterns to document?

## Related Documentation

- `docs/development/workflow/LEARNING_LOOPS.md` - Learning process
- `docs/development/reviews/` - Weekly reviews
- `.cursor/rules/` - Cursor rules

