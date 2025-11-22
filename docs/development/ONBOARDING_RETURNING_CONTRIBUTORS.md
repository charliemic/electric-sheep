# Quick Start Guide for Returning Contributors

**Last Updated**: 2025-01-20  
**Target Audience**: Contributors who have contributed before but haven't been active recently

## 🎯 Purpose

This guide gets you back up to speed quickly so you can make minor updates without friction. **Skip the full onboarding** - this is the fast track for people who already know the basics.

---

## ⚡ 5-Minute Quick Start

**💡 Entry Point Detection**: The system automatically detects how you're starting (prompt, manual, script, etc.) and gathers appropriate context. No need to worry about it - context is added automatically!

### Step 1: Sync Your Environment (2 minutes)

```bash
# Make sure you're on main and up to date
git checkout main
git pull origin main

# Verify your environment still works
./gradlew build
```

**If build fails:**
- Check Java version: `java -version` (should be 17)
- Check Android SDK: `echo $ANDROID_HOME`
- See [README.md](../../README.md) for setup help

### Step 2: Run Pre-Work Check (30 seconds)

```bash
./scripts/pre-work-check.sh
```

This automatically:
- ✅ Checks you're not on main branch
- ✅ Verifies you're up to date with remote
- ✅ Checks for rule/workflow updates
- ✅ Shows available tools and scripts
- ✅ Identifies any issues

### Step 3: Create Feature Branch (10 seconds)

```bash
git checkout -b feature/minor-update-description
```

**That's it!** You're ready to make changes.

---

## 📋 What's Changed Since You Last Contributed

### New Tools & Scripts

**Pre-Work Check** (NEW - Use This First):
```bash
./scripts/pre-work-check.sh
```
- Automatically checks branch, sync status, coordination
- Shows available tools
- Catches common issues before you start

**Emulator Management** (NEW):
```bash
./scripts/emulator-manager.sh start    # Start emulator
./scripts/emulator-manager.sh status  # Check status
./scripts/get-device-id.sh             # Get device ID
```

**Development Reload** (IMPROVED):
```bash
./scripts/dev-reload.sh                # Build, install, launch
./scripts/dev-reload.sh --clean        # Clean build
./scripts/dev-reload.sh --fresh       # Clear app data
```

### New Workflow Features

**Real-Time Collaboration:**
- Rules and workflow docs update automatically via git
- Pre-work check detects updates
- No conflicts - changes propagate immediately

**Multi-Agent Workflow:**
- Git worktrees for complete isolation (if working on shared files)
- Coordination doc: `docs/development/AGENT_COORDINATION.md`
- Pre-work check warns about conflicts

**Smart Prompts:**
- Most routine operations proceed automatically (no prompts)
- Only prompts for genuinely risky operations
- Faster workflow, less interruption

### New Documentation Structure

**Organized by Purpose:**
- `docs/development/setup/` - Setup guides
- `docs/development/workflow/` - Workflow guides
- `docs/development/tools/` - Tool documentation
- `docs/development/guides/` - General guides

**Quick Reference:**
- `docs/development/ONBOARDING_QUICK_REFERENCE.md` - One-page cheat sheet
- `docs/README.md` - Documentation index

---

## 🚀 Workflow for Minor Updates

### The Simplest Possible Workflow

**1. Start Work:**
```bash
./scripts/pre-work-check.sh
git checkout -b feature/minor-update
```

**2. Make Your Change:**
- Edit files
- Test locally: `./gradlew test`
- Run app: `./scripts/dev-reload.sh`

**3. Commit:**
```bash
git add .
git commit -m "fix: your change description"
```

**4. Push & Create PR:**
```bash
git push origin feature/minor-update
gh pr create
```

**That's it!** The pre-work check handles everything else.

---

## 🛠️ Common Tasks (Quick Reference)

### Making a Small Fix

```bash
# 1. Start
./scripts/pre-work-check.sh
git checkout -b fix/description

# 2. Make change
# ... edit files ...

# 3. Test
./gradlew test

# 4. Commit
git add .
git commit -m "fix: description"

# 5. Push
git push origin fix/description
gh pr create
```

### Updating Documentation

```bash
# Same workflow, just different branch name
git checkout -b docs/update-description
# ... make changes ...
git add .
git commit -m "docs: update description"
git push origin docs/update-description
gh pr create
```

### Testing Your Changes

```bash
# Run tests
./gradlew test

# Run app on emulator
./scripts/dev-reload.sh

# Check linting
./gradlew lint
```

---

## 📚 What You DON'T Need to Worry About

**For Minor Updates, You Can Ignore:**
- ❌ Full onboarding docs (you already know the basics)
- ❌ All 25+ rules (pre-work check will catch critical ones)
- ❌ Multi-agent coordination (unless working on shared files)
- ❌ Complex workflows (simple branch → commit → PR is fine)
- ❌ Metrics collection (optional for minor updates)
- ❌ Feature flags (unless your change affects them)
- ❌ CI/CD configuration (unless you're changing it)

**The pre-work check will warn you if you need to know something.**

---

## ⚠️ Critical Rules (Still Apply)

Even for minor updates, these are non-negotiable:

1. **Never work on main branch** → Always create feature branch
2. **Run pre-work check** → `./scripts/pre-work-check.sh` before starting
3. **Tests must pass** → `./gradlew test` before committing
4. **Commit frequently** → Every 15-30 minutes (safety net)

**That's it!** Everything else is handled automatically.

---

## 🔍 Finding What You Need

### Quick Lookups

**What scripts are available?**
```bash
./scripts/pre-work-check.sh  # Shows available tools
ls scripts/                   # List all scripts
```

**What's the project structure?**
```bash
# See README
cat README.md

# See documentation index
cat docs/README.md
```

**How does X work?**
```bash
# Use codebase search (in Cursor/IDE)
# Or grep for patterns
grep -r "pattern" app/src/
```

### Documentation Map

| What You Need | Where to Find It |
|---------------|------------------|
| Project setup | `README.md` |
| Quick reference | `docs/development/ONBOARDING_QUICK_REFERENCE.md` |
| All documentation | `docs/README.md` |
| Workflow details | `docs/development/workflow/` |
| Rules | `.cursor/rules/*.mdc` |

---

## 🆘 When Things Go Wrong

### Build Fails

```bash
# Clean build
./gradlew clean
./gradlew build

# Check Java version
java -version  # Should be 17

# Check Android SDK
echo $ANDROID_HOME
```

### Tests Fail

```bash
# Run tests with more info
./gradlew test --info

# Run specific test
./gradlew test --tests "ClassName.testName"
```

### Git Issues

```bash
# See what's wrong
git status
git diff

# Undo last commit (keep changes)
git reset --soft HEAD~1

# Discard uncommitted changes (DANGER)
git reset --hard HEAD
```

### Pre-Work Check Shows Errors

**The script will tell you exactly what to do:**
- If on main → Create feature branch
- If behind remote → Pull latest main
- If conflicts → Check coordination doc

**Just follow the instructions!**

---

## 📝 Example: Making a Minor Update

**Scenario:** Fix a typo in a comment

```bash
# 1. Start (30 seconds)
./scripts/pre-work-check.sh
git checkout -b fix/typo-in-comment

# 2. Make change (1 minute)
# Edit file, fix typo

# 3. Test (30 seconds)
./gradlew test

# 4. Commit (10 seconds)
git add .
git commit -m "fix: typo in comment"

# 5. Push (10 seconds)
git push origin fix/typo-in-comment

# 6. Create PR (via GitHub UI or gh cli)
gh pr create
```

**Total time: ~3 minutes** (excluding PR review)

---

## 🎯 Key Differences from Before

### What's Easier Now

1. **Pre-work check** - Automatically catches issues
2. **Better scripts** - More automation, less manual work
3. **Clearer docs** - Better organized, easier to find
4. **Smart prompts** - Less interruption, faster workflow

### What's the Same

1. **Branch workflow** - Still create feature branches
2. **Test before commit** - Still run tests
3. **PR process** - Still create PRs for review
4. **Core rules** - Still apply (just automated checks)

### What's New (But Optional)

1. **Git worktrees** - Only if working on shared files
2. **Coordination doc** - Only if multiple people working
3. **Metrics collection** - Optional, not required
4. **Feature flags** - Only if your change affects them

---

## ✅ Checklist: Ready to Contribute?

Before making your change:

- [ ] Ran `./scripts/pre-work-check.sh` (no errors)
- [ ] On feature branch (not main)
- [ ] Up to date with remote main
- [ ] Environment works (`./gradlew build` passes)
- [ ] Know what you're changing

**That's it!** You're ready to make your minor update.

---

## 🚀 Next Steps

1. **Make your change** - Edit files, test locally
2. **Commit frequently** - Every 15-30 minutes (safety net)
3. **Push when ready** - Create PR for review
4. **That's it!** - Pre-work check handles the rest

---

## 🔍 Entry Point Detection

**The system automatically detects how you're starting and gathers context:**

- **Prompt-based** (AI agent) → Automatically gathers: pre-work check, similar patterns, coordination
- **Manual** (file edits) → Automatically gathers: pre-work check, branch status, warnings
- **Evaluation** (analyze/review) → Automatically gathers: current state, recent changes, related systems
- **Script** (automation) → Automatically gathers: script-specific context, state, next steps

**You don't need to do anything** - context is added automatically based on how you enter!

**Manual detection:**
```bash
./scripts/detect-entry-point.sh "your task description"
```

**See:** `docs/development/workflow/ENTRY_POINT_CONTEXT_MANAGEMENT.md` for details

## 📞 Need Help?

**Quick Help:**
- Run `./scripts/pre-work-check.sh` - Shows available tools
- Run `./scripts/detect-entry-point.sh` - Detects entry point and gathers context
- Check `docs/development/ONBOARDING_QUICK_REFERENCE.md` - One-page cheat sheet
- See `docs/README.md` - Documentation index

**More Help:**
- Full onboarding: `docs/development/ONBOARDING_NEW_STARTERS.md` (if you need a refresher)
- Workflow details: `docs/development/workflow/`
- Rules: `.cursor/rules/*.mdc`

**Remember:** The pre-work check is your friend - it catches issues before they become problems!

---

**Welcome back! 🎉 Making minor updates should be super easy now.**

