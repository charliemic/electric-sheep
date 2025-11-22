# Rules Priority Guide for New Starters

**Last Updated**: 2025-01-20  
**Purpose**: Identify which rules are essential vs. can be learned later

This document helps new team members understand **what to focus on first** and **what can wait**. We want guardrails without overwhelming you.

---

## 🎯 Rule Priority Levels

### 🔴 Critical (Learn First Week)
**These prevent breaking things or losing work. Must know immediately.**

### 🟡 Important (Learn First Month)
**These improve code quality and workflow. Learn as you encounter them.**

### 🟢 Nice to Have (Learn Over Time)
**These are optimizations and advanced patterns. Learn when needed.**

---

## 🔴 Critical Rules (Learn First Week)

### 1. Branching Rules (`.cursor/rules/branching.mdc`)

**Why Critical**: Prevents working on main branch, which could break the codebase.

**Key Points**:
- ✅ Never work on `main` branch
- ✅ Always create feature branch: `feature/task-name`
- ✅ Run `./scripts/pre-work-check.sh` before starting work

**What You Need to Know**:
- How to create a branch
- How to check what branch you're on
- That worktrees exist (but you can use simple branches first)

**What You Can Ignore (For Now)**:
- Git worktree details (use simple branches initially)
- Advanced branch synchronization (learn when working with team)

**Quick Reference**:
```bash
git checkout -b feature/my-task
./scripts/pre-work-check.sh
```

---

### 2. Testing Rules (`.cursor/rules/testing.mdc`)

**Why Critical**: Prevents breaking existing functionality.

**Key Points**:
- ✅ Run `./gradlew test` before committing
- ✅ All tests must pass
- ✅ Add tests for new functionality

**What You Need to Know**:
- How to run tests
- That tests must pass before committing
- Basic test structure (AAA pattern)

**What You Can Ignore (For Now)**:
- Advanced testing patterns (hourglass pattern, etc.)
- Accessibility testing details (learn when working on UI)
- Test coverage targets (focus on having tests first)

**Quick Reference**:
```bash
./gradlew test  # Run before committing
```

---

### 3. Code Quality Basics (`.cursor/rules/code-quality.mdc`)

**Why Critical**: Maintains code consistency and prevents common mistakes.

**Key Points**:
- ✅ Follow existing code patterns
- ✅ Use UK English spellings
- ✅ Never use force unwrap (`!!`) - use safe calls (`?.`)
- ✅ Plan before coding (5 minutes saves hours)

**What You Need to Know**:
- Look at existing code for patterns
- Use safe null handling
- Think before coding

**What You Can Ignore (For Now)**:
- All SOLID principles (learn gradually)
- Advanced refactoring patterns
- Detailed code review checklist (learn through reviews)

**Quick Reference**:
```kotlin
// ❌ BAD
val value = nullableValue!!

// ✅ GOOD
val value = nullableValue?.let { it } ?: defaultValue
```

---

### 4. Frequent Commits (`.cursor/rules/frequent-commits.mdc`)

**Why Critical**: Safety net - prevents losing work.

**Key Points**:
- ✅ Commit every 15-30 minutes
- ✅ Use WIP commits for incomplete work
- ✅ Commit locally (no need to push every time)

**What You Need to Know**:
- Commit frequently (even incomplete work)
- WIP commits are fine
- Can clean up commits before pushing

**What You Can Ignore (For Now)**:
- Advanced git history cleanup
- Squashing commits (learn when needed)

**Quick Reference**:
```bash
git add .
git commit -m "WIP: what I'm working on"
```

---

### 5. Pre-Work Check (Automated)

**Why Critical**: Catches common mistakes automatically.

**Key Points**:
- ✅ Run `./scripts/pre-work-check.sh` before starting work
- ✅ Fix any errors it reports

**What You Need to Know**:
- Run it before making changes
- Fix what it tells you to fix

**What You Can Ignore (For Now)**:
- Understanding all the checks it does (just fix what it says)
- Advanced coordination scenarios

**Quick Reference**:
```bash
./scripts/pre-work-check.sh
```

---

## 🟡 Important Rules (Learn First Month)

### 6. Error Handling (`.cursor/rules/error-handling.mdc`)

**Why Important**: Proper error handling prevents crashes and improves UX.

**Key Points**:
- ✅ Use `Result<T>` for operations that can fail
- ✅ Use `NetworkError.fromException()` for network errors
- ✅ Log errors with context

**When You'll Encounter**:
- Adding API calls
- Working with database operations
- Handling user input

**What You Can Ignore (For Now)**:
- Advanced error conversion strategies
- Complex error recovery patterns

---

### 7. API Patterns (`.cursor/rules/api-patterns.mdc`)

**Why Important**: Ensures consistent API usage and proper error handling.

**Key Points**:
- ✅ Use `Result<T>` for remote operations
- ✅ Use `NetworkError.fromException()` for errors
- ✅ Handle offline scenarios

**When You'll Encounter**:
- Adding new API calls
- Working with Supabase
- Fetching remote data

**What You Can Ignore (For Now)**:
- Advanced caching strategies
- Complex offline handling

---

### 8. Working Patterns First (`.cursor/rules/working-patterns-first.mdc`)

**Why Important**: Prevents reinventing the wheel and maintains consistency.

**Key Points**:
- ✅ Check for existing patterns before implementing
- ✅ Use existing abstractions when appropriate
- ✅ Don't try different approaches unnecessarily

**When You'll Encounter**:
- Adding new features
- Implementing similar functionality
- Refactoring code

**What You Can Ignore (For Now)**:
- Building new abstractions (use existing ones first)
- Advanced pattern evaluation

---

### 9. Documentation First (`.cursor/rules/documentation-first.mdc`)

**Why Important**: Prevents making assumptions and using outdated patterns.

**Key Points**:
- ✅ Check official documentation before implementing
- ✅ Verify version compatibility
- ✅ Follow official patterns

**When You'll Encounter**:
- Using new libraries
- Debugging failures
- Implementing features with external dependencies

**What You Can Ignore (For Now)**:
- Deep documentation analysis (check docs, then ask if stuck)

---

### 10. Security Basics (`.cursor/rules/security.mdc`)

**Why Important**: Prevents security vulnerabilities.

**Key Points**:
- ✅ Never commit secrets or credentials
- ✅ Verify authentication before accessing user data
- ✅ Filter queries by userId

**When You'll Encounter**:
- Adding authentication
- Working with user data
- Adding API keys

**What You Can Ignore (For Now)**:
- Advanced security patterns
- AWS security details (learn when working with AWS)

---

## 🟢 Nice to Have (Learn Over Time)

### 11. Accessibility (`.cursor/rules/accessibility.mdc`)

**Why Nice to Have**: Important for UX, but can learn when working on UI.

**Key Points**:
- ✅ Add content descriptions to interactive elements
- ✅ Ensure minimum touch targets (48dp × 48dp)
- ✅ Test with screen readers

**When You'll Encounter**:
- Working on UI components
- Adding new screens
- Improving existing UI

**What You Can Ignore (For Now)**:
- Advanced accessibility patterns
- All WCAG requirements (learn gradually)

---

### 12. Design System (`.cursor/rules/design.mdc`)

**Why Nice to Have**: Ensures consistent UI, but can learn when working on UI.

**Key Points**:
- ✅ Use `Spacing` object (never hardcode spacing)
- ✅ Use theme colors
- ✅ Follow typography hierarchy

**When You'll Encounter**:
- Working on UI components
- Adding new screens
- Styling components

**What You Can Ignore (For Now)**:
- All design system details
- Advanced UX principles

---

### 13. CI/CD (`.cursor/rules/cicd.mdc`)

**Why Nice to Have**: Important for deployment, but not needed immediately.

**Key Points**:
- ✅ Verify CI passes before merging
- ✅ Run tests locally before pushing
- ✅ Fix CI failures before requesting review

**When You'll Encounter**:
- Creating PRs
- CI failures
- Deployment

**What You Can Ignore (For Now)**:
- CI/CD configuration details
- Advanced debugging

---

### 14. Logging (`.cursor/rules/logging.mdc`)

**Why Nice to Have**: Important for debugging, but can learn gradually.

**Key Points**:
- ✅ Use centralized Logger utility
- ✅ Choose appropriate log level
- ✅ Include context in messages

**When You'll Encounter**:
- Adding new features
- Debugging issues
- Working with errors

**What You Can Ignore (For Now)**:
- Advanced logging patterns
- Log level guidelines (use INFO for now)

---

### 15. Advanced Workflow (Multiple Rules)

**Why Nice to Have**: Optimizations for team collaboration, but not needed initially.

**Includes**:
- Git worktrees (`.cursor/rules/branching.mdc`)
- Branch synchronization (`.cursor/rules/branch-synchronization.mdc`)
- Multi-agent coordination
- Scope creep detection (`.cursor/rules/scope-creep-detection.mdc`)

**When You'll Encounter**:
- Working with multiple team members
- Long-running features
- Complex coordination scenarios

**What You Can Ignore (For Now)**:
- All advanced workflow features
- Multi-agent coordination details

---

## 📊 Summary Table

| Rule | Priority | When to Learn | Key Point |
|------|----------|---------------|-----------|
| Branching | 🔴 Critical | Week 1 | Never work on main |
| Testing | 🔴 Critical | Week 1 | Tests must pass |
| Code Quality Basics | 🔴 Critical | Week 1 | Follow patterns, safe nulls |
| Frequent Commits | 🔴 Critical | Week 1 | Commit every 15-30 min |
| Pre-Work Check | 🔴 Critical | Week 1 | Run before starting |
| Error Handling | 🟡 Important | Month 1 | Use Result<T> |
| API Patterns | 🟡 Important | Month 1 | Use Result<T>, handle errors |
| Working Patterns First | 🟡 Important | Month 1 | Check existing patterns |
| Documentation First | 🟡 Important | Month 1 | Check docs before coding |
| Security Basics | 🟡 Important | Month 1 | No secrets, verify auth |
| Accessibility | 🟢 Nice to Have | When doing UI | Content descriptions, touch targets |
| Design System | 🟢 Nice to Have | When doing UI | Use Spacing, theme colors |
| CI/CD | 🟢 Nice to Have | When creating PRs | Verify CI passes |
| Logging | 🟢 Nice to Have | Gradually | Use Logger utility |
| Advanced Workflow | 🟢 Nice to Have | When needed | Worktrees, coordination |

---

## 🎓 Learning Path

### Week 1: Critical Rules
Focus on the 5 critical rules. These prevent breaking things.

**Goal**: Be able to start work, make changes, and commit safely.

### Month 1: Important Rules
Learn important rules as you encounter them.

**Goal**: Write quality code that follows patterns and handles errors.

### Ongoing: Nice to Have
Learn advanced rules when you need them.

**Goal**: Optimize workflow and contribute to best practices.

---

## 💡 Tips for Learning Rules

### Don't Try to Memorize Everything
- Focus on critical rules first
- Learn others as you encounter them
- Use this document as a reference

### Ask Questions
- "Why do we do X?" - Understanding helps you remember
- "When would I use Y?" - Context helps learning
- "What happens if I don't follow Z?" - Consequences help prioritization

### Use Automation
- Scripts enforce many rules automatically
- Pre-work check catches common mistakes
- CI/CD catches issues before merge

### Learn from Code Reviews
- Reviewers will point out rule violations
- Each review is a learning opportunity
- Ask why something needs to change

---

## 🔍 Finding Rules

### By File
All rules are in `.cursor/rules/*.mdc`:
- `branching.mdc` - Branch workflow
- `testing.mdc` - Testing requirements
- `code-quality.mdc` - Code standards
- `error-handling.mdc` - Error handling patterns
- etc.

### By Topic
Use codebase search:
- "How do I handle errors?" → `error-handling.mdc`
- "How do I create a branch?" → `branching.mdc`
- "What are the testing requirements?" → `testing.mdc`

### By Script
Scripts often enforce rules:
- `pre-work-check.sh` → Enforces branching, coordination
- `dev-reload.sh` → Uses emulator management rules
- Test scripts → Enforce testing rules

---

## ❓ Common Questions

**Q: Do I need to know all rules before starting?**  
A: No! Focus on critical rules (Week 1), learn others as you go.

**Q: What if I break a rule?**  
A: That's okay! Code reviews will catch it. Learn from the feedback.

**Q: Are rules flexible?**  
A: Critical rules are non-negotiable. Others can be discussed if you have a good reason.

**Q: How do I know which rule applies?**  
A: Use `./scripts/discover-rules.sh keyword` or ask in code review.

**Q: What if a rule conflicts with what I learned?**  
A: Ask! There's usually a good reason. Understanding the "why" helps.

---

## 📝 Notes

- **Rules are guardrails, not constraints** - They prevent problems, not creativity
- **Automation helps** - Scripts enforce many rules automatically
- **Learning is gradual** - You don't need to know everything immediately
- **Questions are welcome** - Understanding helps you follow rules correctly

---

**Remember**: The goal is to help you be productive while maintaining code quality. Rules are tools, not obstacles! 🚀

