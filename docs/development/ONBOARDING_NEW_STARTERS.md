# Onboarding Guide for New Team Members

**Last Updated**: 2025-01-20  
**Target Audience**: New developers with bootcamp experience, new to large projects

## 🎯 Purpose

This guide helps you get productive quickly while understanding the "why" behind our practices. We want **guardrails and guidance, not blind rules**.

---

## 📋 Table of Contents

1. [First Day Checklist](#first-day-checklist)
2. [Essential Rules (Must Know)](#essential-rules-must-know)
3. [Workflow Basics](#workflow-basics)
4. [Common Tasks](#common-tasks)
5. [Tools & Scripts](#tools--scripts)
6. [Where to Find Help](#where-to-find-help)
7. [What NOT to Worry About (Yet)](#what-not-to-worry-about-yet)
8. [Understanding the "Why"](#understanding-the-why)

---

## First Day Checklist

### ✅ Setup (Do This First)

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd electric-sheep
   ```

2. **Set up your development environment**
   - Follow [README.md](../../README.md) for Java, Android SDK setup
   - Run `./gradlew build` to verify everything works

3. **Read this document** (you're doing it! ✅)

4. **Run the pre-work check script** (even if you're just exploring)
   ```bash
   ./scripts/pre-work-check.sh
   ```
   This shows you what the automated checks look like.

### ✅ Understanding (Do This Week)

1. **Read Essential Rules** (below) - These are non-negotiable
2. **Try the workflow** - Create a test branch, make a small change, commit it
3. **Explore the codebase** - Use codebase search to find patterns
4. **Ask questions** - There's no such thing as a stupid question

---

## Essential Rules (Must Know)

These are the **critical rules** that prevent problems. Understanding why helps you follow them correctly.

### 🚨 Rule 1: Never Work on Main Branch

**What**: Always create a feature branch before making changes.

**Why**: 
- Prevents breaking the main codebase
- Allows multiple people to work simultaneously
- Makes it easy to review changes before they're merged

**How**:
```bash
# Check your current branch
git branch

# If you're on main, create a feature branch
git checkout -b feature/my-first-task

# Or use the helper script (recommended)
./scripts/create-worktree.sh my-first-task
```

**Automated Check**: The `pre-work-check.sh` script will catch this for you.

### 🚨 Rule 2: Run Pre-Work Check Before Starting

**What**: Run `./scripts/pre-work-check.sh` before making any changes.

**Why**: 
- Catches common mistakes automatically
- Ensures your branch is up to date
- Checks for conflicts with other team members

**How**:
```bash
./scripts/pre-work-check.sh
```

**What it checks**:
- ✅ Not on main branch
- ✅ Branch is up to date with remote
- ✅ No coordination conflicts
- ✅ Working directory is clean

### 🚨 Rule 3: Commit Frequently (Safety Net)

**What**: Commit your work every 15-30 minutes, even if incomplete.

**Why**:
- **Safety net** - If something breaks, you can revert
- **Progress tracking** - See what you've done
- **Confidence** - Safe to experiment knowing you can undo

**How**:
```bash
# Stage your changes
git add .

# Commit (WIP is fine for incomplete work)
git commit -m "WIP: adding user authentication screen"

# Continue working...
# Commit again when you make progress
git commit -m "feat: add login form validation"
```

**Note**: These are local commits - you can clean them up before pushing.

### 🚨 Rule 4: Tests Must Pass Before Committing

**What**: Run `./gradlew test` before committing. All tests must pass.

**Why**:
- Prevents breaking existing functionality
- Catches errors early (easier to fix)
- Maintains code quality

**How**:
```bash
# Run tests
./gradlew test

# If tests pass, commit
git add .
git commit -m "feat: add new feature"
```

**If tests fail**: Fix them before committing. Don't accumulate test failures.

### 🚨 Rule 5: Use Existing Patterns First

**What**: Before implementing something new, check if a similar pattern already exists.

**Why**:
- Consistency across the codebase
- Less code to maintain
- Proven patterns are more reliable

**How**:
```bash
# Search for similar functionality
# Use codebase search: "How does X work?"
# Or grep: grep -r "similar-pattern" app/src/
```

**Example**: Need to add a new screen? Look at how existing screens are structured.

---

## First-Time Developer Notes

If you're new to Android development, Kotlin, or this project structure, this section covers project-specific concepts you'll encounter.

### Gradle/Kotlin DSL

If you're new to Android development or coming from Java/Groovy:

- **`build.gradle.kts` uses Kotlin DSL**, not Groovy
- Syntax is different: `create("release")` instead of `release { }`
- **Official Documentation**: [Gradle Kotlin DSL Primer](https://docs.gradle.org/current/userguide/kotlin_dsl.html)
- **Quick Reference**: [Kotlin DSL vs Groovy](https://docs.gradle.org/current/userguide/migrating_from_groovy_to_kotlin_dsl.html)

**Common Patterns**:
- Reading properties: `readProperty("key", "default")`
- Creating configs: `create("name") { ... }`
- Setting values: `property = value` (not `property 'value'`)

**Tip**: Look at existing patterns in `app/build.gradle.kts` (e.g., Supabase config) for examples.

---

## Finding Patterns in the Codebase

When implementing a new feature, look for existing patterns first:

### 1. Use Codebase Search

**In Cursor**: Use semantic search (Cmd+K / Ctrl+K) to find similar implementations.

**Examples**:
- Need to add config? Search: "How is configuration read from local.properties?"
- Need to add signing? Search: "How is release signing configured?"
- Need to add a repository? Search: "How are repositories implemented?"

### 2. Use Pattern Discovery Script

```bash
# Find common patterns (if script exists)
./scripts/find-pattern.sh readProperty
./scripts/find-pattern.sh signingConfig
./scripts/find-pattern.sh repository
```

### 3. Look at Similar Files

- **Config**: Check `app/build.gradle.kts` for existing config patterns
- **Repositories**: Check `app/src/.../repository/` for repository patterns
- **ViewModels**: Check `app/src/.../ui/` for ViewModel patterns
- **Components**: Check `app/src/.../ui/components/` for component patterns

### 4. Follow Existing Patterns

Once you find a pattern:
1. **Copy the structure** (not the content)
2. **Adapt to your use case**
3. **Maintain consistency** with existing code

**Example**: When adding signing config, we followed the Supabase config pattern:
- Same `readProperty` function structure
- Same environment variable priority (env vars first, then local.properties)
- Same validation pattern (check all values before applying)

---

## Troubleshooting

### Common Issues and Solutions

#### Pre-Work Check Shows "Remote Has Updates" After Pulling

**Problem**: Pre-work check reports remote updates even after `git pull origin main`.

**Solution**: This is a known false positive. If you just pulled, you're likely up to date. Verify with:
```bash
git log origin/main..main  # Should be empty
```

**Status**: Known issue, will be fixed in next update.

#### Build Fails: "Keystore file not found"

**Problem**: Release build fails because keystore is missing.

**Solution**: 
- For local dev: Generate keystore with `./scripts/generate-keystore.sh`
- For CI/CD: Ensure GitHub Secrets are configured
- See `docs/development/setup/RELEASE_SIGNING_SETUP.md` for details

#### Can't Find Existing Patterns

**Problem**: Don't know how to find similar implementations.

**Solution**:
1. Use codebase search (semantic search in Cursor)
2. Use pattern discovery script: `./scripts/find-pattern.sh <pattern>`
3. Check similar files in the same directory structure
4. Ask for help if stuck after 15-20 minutes

#### Gradle/Kotlin DSL Syntax Errors

**Problem**: Syntax errors in `build.gradle.kts`.

**Solution**:
- Check [Gradle Kotlin DSL Primer](https://docs.gradle.org/current/userguide/kotlin_dsl.html)
- Look at existing patterns in the same file
- Remember: Kotlin DSL uses `create("name")` not `name { }`

### When to Ask for Help

**Ask for help if**:
- You've been stuck on the same issue for > 30 minutes
- Error messages don't make sense after reading documentation
- You're unsure about security implications
- You're modifying shared/critical files

**Keep trying if**:
- You're making progress (even if slow)
- Documentation exists but you haven't read it yet
- It's a learning curve issue (expected for first-time exposure)

**How to Ask for Help**:
1. Check documentation first: `docs/development/`
2. Search existing issues: `gh issue list`
3. Ask in coordination doc: `docs/development/AGENT_COORDINATION.md`
4. Create issue if it's a bug or missing documentation

---

## Workflow Basics

### Starting Work on a Task

**Step-by-step**:

1. **Check current state**
   ```bash
   git status
   git branch
   ```

2. **Run pre-work check**
   ```bash
   ./scripts/pre-work-check.sh
   ```

3. **Create feature branch** (if not already on one)
   ```bash
   git checkout -b feature/my-task-name
   # Or use worktree for isolation
   ./scripts/create-worktree.sh my-task-name
   ```

4. **Start coding!**

### Making Changes

1. **Make your changes** (code, tests, etc.)

2. **Run tests**
   ```bash
   ./gradlew test
   ```

3. **Commit frequently**
   ```bash
   git add .
   git commit -m "WIP: description of what you're doing"
   ```

4. **When feature is complete**
   ```bash
   git commit -m "feat: complete description of feature"
   ```

### Pushing Your Work

1. **Push to remote**
   ```bash
   git push origin feature/my-task-name
   ```

2. **Create Pull Request** (via GitHub UI or CLI)
   ```bash
   gh pr create --title "Add feature X" --body "Description"
   ```

3. **Wait for review** - Don't merge your own PRs initially

### Daily Workflow

**Morning**:
- Pull latest changes: `git checkout main && git pull origin main`
- Create new branch from updated main: `git checkout -b feature/new-task`

**During work**:
- Commit frequently (every 15-30 minutes)
- Run tests before committing
- Run pre-work check if switching tasks

**End of day**:
- Commit any work in progress (WIP commits are fine)
- Push your branch (so others can see your progress)

---

## Common Tasks

### Adding a New Feature

1. **Plan first** (5 minutes saves hours later)
   - What are you trying to achieve?
   - What's in scope? What's out?
   - How will you know it works?

2. **Find similar features**
   - Search codebase for similar implementations
   - Use existing patterns

3. **Create feature branch**
   ```bash
   git checkout -b feature/new-feature-name
   ```

4. **Implement incrementally**
   - Start small (minimal working version)
   - Add tests as you go
   - Commit frequently

5. **Test thoroughly**
   ```bash
   ./gradlew test
   ```

6. **Create PR when ready**

### Fixing a Bug

1. **Reproduce the bug**
   - Understand what's broken
   - Write a test that fails (demonstrates the bug)

2. **Create fix branch**
   ```bash
   git checkout -b fix/bug-description
   ```

3. **Fix the bug**
   - Make the failing test pass
   - Ensure other tests still pass

4. **Test**
   ```bash
   ./gradlew test
   ```

5. **Commit and push**

### Running the App

**Quick reload** (recommended):
```bash
./scripts/dev-reload.sh
```

**With clean build**:
```bash
./scripts/dev-reload.sh --clean
```

**With fresh database** (clears app data):
```bash
./scripts/dev-reload.sh --fresh
```

### Running Tests

**All tests**:
```bash
./gradlew test
```

**Specific test class**:
```bash
./gradlew test --tests "com.electricsheep.app.MyTestClass"
```

**With logging**:
```bash
./scripts/execute-test-with-logging.sh MyTestClass
```

### Accessing the Database

**Using Android Studio**:
1. Run app on emulator
2. `View` → `Tool Windows` → `App Inspection`
3. Browse tables

**Using ADB**:
```bash
adb shell
run-as com.electricsheep.app
cd /data/data/com.electricsheep.app/databases
sqlite3 app_database
SELECT * FROM moods;
```

---

## Tools & Scripts

### Essential Scripts (Use These Daily)

**Pre-work check** (run before starting work):
```bash
./scripts/pre-work-check.sh
```

**Development reload** (build, install, launch):
```bash
./scripts/dev-reload.sh
```

**Create worktree** (for isolated work):
```bash
./scripts/create-worktree.sh task-name
```

**Get device ID** (for testing):
```bash
./scripts/get-device-id.sh
```

### Emulator Management

**Start emulator**:
```bash
./scripts/emulator-manager.sh start
```

**Check emulator status**:
```bash
./scripts/emulator-manager.sh status
```

**Stop emulator**:
```bash
./scripts/emulator-manager.sh stop
```

### Other Useful Scripts

**Check agent coordination** (see who's working on what):
```bash
./scripts/check-agent-coordination.sh
```

**Discover relevant rules** (find rules for your task):
```bash
./scripts/discover-rules.sh keyword
```

**Quick WIP commit**:
```bash
./scripts/wip-commit.sh "description"
```

### What Scripts Do

- **Automate repetitive tasks** - Don't type long commands manually
- **Enforce best practices** - Catch mistakes before they cause problems
- **Provide consistency** - Everyone uses the same process

**Tip**: If you find yourself typing the same command repeatedly, check if there's a script for it!

---

## Where to Find Help

### 📚 Documentation Structure

**Quick Start**:
- `README.md` - Project overview and setup
- This document - Onboarding guide

**Development Guides**:
- `docs/development/workflow/` - Workflow guides (multi-agent, git, etc.)
- `docs/development/setup/` - Setup guides (AWS, Supabase, etc.)
- `docs/development/guides/` - General development guides

**Architecture**:
- `docs/architecture/` - System architecture and design decisions
- `docs/architecture/decisions/` - Why we made specific choices

**Testing**:
- `docs/testing/guides/` - Testing guides and patterns
- `.cursor/rules/testing.mdc` - Testing requirements

### 🔍 Finding Information

**Codebase search** (best for "how does X work?"):
- Use semantic search: "How does user authentication work?"
- Finds code and patterns, not just keywords

**Grep** (best for exact matches):
```bash
grep -r "ClassName" app/src/
```

**Documentation search**:
- Check `docs/README.md` for documentation index
- Use GitHub search within docs directory

**Ask questions**:
- Check `docs/development/AGENT_COORDINATION.md` for active work
- Ask in team chat/standup
- No question is too basic!

### 📖 Key Documents to Bookmark

**For daily work**:
- This document (onboarding)
- `README.md` (project overview)
- `.cursor/rules/branching.mdc` (workflow rules)
- `.cursor/rules/code-quality.mdc` (code standards)

**For understanding architecture**:
- `docs/architecture/DATA_LAYER_ARCHITECTURE.md`
- `docs/architecture/AUTHENTICATION.md`
- `docs/architecture/UX_PRINCIPLES.md`

**For setup**:
- `docs/development/setup/README.md` (links to all setup guides)

---

## What NOT to Worry About (Yet)

### ❌ Don't Worry About These Initially

**Advanced workflow features**:
- Git worktrees (use simple branches first)
- Multi-agent coordination (you'll learn this as you work with the team)
- Advanced CI/CD configuration

**Complex architecture**:
- Feature flags system (use existing flags, don't create new ones yet)
- Supabase edge functions
- Advanced error handling patterns

**All the rules at once**:
- There are 25+ cursor rules - you don't need to memorize them all
- Focus on Essential Rules first
- Learn others as you encounter them

**Perfect code immediately**:
- It's okay to ask for help
- It's okay to make mistakes (that's how you learn)
- Code reviews are learning opportunities

### ✅ Do Worry About These

- **Following Essential Rules** (prevents breaking things)
- **Running tests** (catches errors early)
- **Committing frequently** (safety net)
- **Asking questions** (prevents misunderstandings)

---

## Understanding the "Why"

### Why So Many Rules?

**Short answer**: We work with multiple developers (and AI agents) simultaneously. Rules prevent conflicts and maintain code quality.

**Longer answer**:
- **Isolation** - Each person works on their own branch, preventing conflicts
- **Quality** - Tests and code reviews catch issues before they reach main
- **Consistency** - Following patterns makes code easier to understand
- **Safety** - Frequent commits and backups prevent work loss

### Why Git Worktrees?

**Problem**: Multiple people working on the same files causes conflicts.

**Solution**: Each person gets their own directory (worktree) on their own branch.

**Benefit**: Complete isolation - you can't accidentally interfere with others' work.

**When to use**: 
- When working on shared files (high conflict risk)
- When you want complete isolation
- For now: Use simple branches, learn worktrees later

### Why Pre-Work Checks?

**Problem**: Easy to make mistakes (work on main, forget to sync, etc.)

**Solution**: Automated script catches common mistakes.

**Benefit**: Prevents problems before they happen.

**Analogy**: Like a spell-checker - catches errors automatically.

### Why Frequent Commits?

**Problem**: Losing hours of work if something breaks.

**Solution**: Commit every 15-30 minutes (even incomplete work).

**Benefit**: Can always revert to last known good state.

**Analogy**: Like saving a document frequently - you can always undo.

### Why Tests Must Pass?

**Problem**: Breaking existing functionality when adding new features.

**Solution**: Run tests before committing.

**Benefit**: Catches regressions early (easier to fix).

**Analogy**: Like checking your car's brakes after changing the oil - ensures nothing broke.

---

## Next Steps

### Week 1: Get Comfortable

- ✅ Set up development environment
- ✅ Read Essential Rules
- ✅ Make a small change (add a comment, fix a typo)
- ✅ Create a PR (even if it's tiny)
- ✅ Get familiar with running tests and the app

### Week 2: Start Contributing

- ✅ Pick up a small task from the backlog
- ✅ Follow the workflow (branch, code, test, commit, PR)
- ✅ Participate in code reviews
- ✅ Ask questions when stuck

### Week 3+: Build Confidence

- ✅ Take on larger tasks
- ✅ Help other team members
- ✅ Suggest improvements to processes
- ✅ Become comfortable with the codebase

---

## Quick Reference

### Daily Commands

```bash
# Start work
./scripts/pre-work-check.sh
git checkout -b feature/my-task

# During work
./gradlew test              # Run tests
git add . && git commit -m "WIP: description"  # Commit frequently

# Run app
./scripts/dev-reload.sh

# End of day
git push origin feature/my-task
```

### When Stuck

1. **Check documentation** - `docs/README.md` for index
2. **Search codebase** - "How does X work?"
3. **Run pre-work check** - Catches common issues
4. **Ask questions** - No question is too basic

### Emergency Commands

```bash
# Undo last commit (keep changes)
git reset --soft HEAD~1

# Discard all uncommitted changes (DANGER - only if sure)
git reset --hard HEAD

# See what changed
git status
git diff
```

---

## Questions?

**Common questions**:
- "What branch should I use?" → Feature branch: `feature/task-name`
- "How do I know if my code is good?" → Tests pass, code review approved
- "What if I break something?" → Frequent commits = easy to revert
- "Where do I find X?" → Check `docs/README.md` or use codebase search

**Ask for help**:
- Team chat/standup
- Code review comments
- Documentation issues (help us improve!)

---

**Remember**: Everyone was new once. We're here to help you succeed! 🚀

