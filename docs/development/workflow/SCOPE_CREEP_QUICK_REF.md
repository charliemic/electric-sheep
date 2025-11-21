# Scope Creep Detection - Quick Reference

**Quick guide for detecting scope creep and starting a new Cursor chat session.**

## Quick Commands

**Start tracking a session:**
```bash
./scripts/track-session-scope.sh start "Fix login bug" 1 low
```

**Update task:**
```bash
./scripts/track-session-scope.sh update "Fix login bug and add error handling"
```

**Check for scope creep:**
```bash
./scripts/track-session-scope.sh check
```

**View full report:**
```bash
./scripts/track-session-scope.sh report
```

## When to Start a New Chat Session

**Start a new Cursor chat session when AI evaluation indicates:**
- ✅ **SEVERE_SCOPE_CREEP** - Major expansion, strongly recommend new chat
- ✅ **MODERATE_SCOPE_CREEP** - Significant expansion, consider new chat
- ✅ Working on unrelated feature (context switch)
- ✅ Session duration > 3 hours with multiple concerns
- ✅ Architecture changes beyond original scope

## How to Start a New Chat Session

**3 simple steps:**

1. **Commit current work** (WIP is fine):
   ```bash
   git add .
   git commit -m "WIP: [brief description]"
   ```

2. **Start new Cursor chat session**:
   - Click "New Chat" in Cursor UI
   - Or press `Cmd+L` (Mac) / `Ctrl+L` (Windows/Linux)

3. **Reference previous work**:
   ```
   Continuing from previous session:
   - Completed: [what was done]
   - Current state: [brief description]
   - Next task: [what to do next]
   ```

## Scope Creep Severity Levels (AI-Based)

**AI Evaluation Results:**

- **NO_SCOPE_CREEP**: Natural, related expansion (continue)
- **MILD_SCOPE_CREEP**: Some expansion, still focused (monitor)
- **MODERATE_SCOPE_CREEP**: Significant expansion (consider new chat session)
- **SEVERE_SCOPE_CREEP**: Major expansion (strongly recommend new chat session)

**The AI evaluates context** to understand if expansion is justified or scope creep.

## Benefits of New Chat Session

- ✅ Fresh context (no accumulated complexity)
- ✅ Better focus on current task
- ✅ Cleaner git history
- ✅ Faster responses
- ✅ Easier to review and test

## Related Documentation

- `.cursor/rules/scope-creep-detection.mdc` - Complete rule
- `docs/development/workflow/SCOPE_CREEP_DETECTION.md` - Full guide
- `scripts/track-session-scope.sh` - Tracking script

