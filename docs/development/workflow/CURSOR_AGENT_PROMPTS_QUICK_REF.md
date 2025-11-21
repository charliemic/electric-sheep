# Cursor Agent Prompts - Quick Reference

## 🚀 Most Common Prompts

### Starting Work
```
I'm about to start work on [task]. Please check pre-work requirements:
- Current branch status
- Main branch updates needed
- Coordination conflicts
- Relevant rules
Show me a summary before I start.
```

### Commit Work
```
I've completed [work]. Please help me commit:
- Show changed files
- Generate commit message
- Run tests first
- Show summary and wait for approval
```

### Create PR
```
I'm ready to create a PR. Please:
- Generate PR title and description from commits
- Show me the PR details
- Wait for approval before creating
- Create as draft
```

### Session End
```
I'm ready to end this session. Please help me:
1. Commit changes (if any) - show summary, get approval
2. Push branch (if needed) - show summary, get approval
3. Create PR (if needed) - generate description, show summary, get approval
4. Check PR readiness
5. Provide next steps summary
```

### Sync with Main
```
I need to sync my branch with main. Please:
- Fetch latest
- Show commits in main I don't have
- Rebase on main (preferred)
- Show summary and wait for approval
- Help resolve conflicts if needed
```

### After PR Merge
```
PR #[number] has been merged. Please help me clean up:
- Check other active work
- Switch to main and pull
- Identify merged branches/worktrees to delete
- Show summary and wait for approval
```

## 📋 All Prompt Categories

| Category | Section | Use Case |
|----------|---------|----------|
| **Starting** | 1.1-1.3 | Pre-work check, create branch, check coordination |
| **During Work** | 2.1-2.4 | Commit, push, sync, test |
| **Before PR** | 3.1-3.2 | Final sync, review changes |
| **Creating PR** | 4.1-4.2 | Create PR, check readiness |
| **After Merge** | 5.1 | Post-merge cleanup |
| **Errors** | 6.1-6.4 | Test failures, conflicts, build errors, CI failures |
| **Session End** | 7.1 | Complete end-of-session workflow |

## 🔗 Full Guide

See `docs/development/workflow/CURSOR_AGENT_PROMPT_TEMPLATES.md` for complete templates with detailed examples.

