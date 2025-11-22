# Quick Reference Card for New Starters

**Print this or keep it open in a tab!**

---

## 🚨 Critical Commands (Use Daily)

```bash
# Before starting work
./scripts/pre-work-check.sh

# Create feature branch
git checkout -b feature/task-name

# Run tests
./gradlew test

# Run app
./scripts/dev-reload.sh

# Commit frequently
git add .
git commit -m "WIP: what you're doing"

# Push when ready
git push origin feature/task-name
```

---

## 🔴 Critical Rules (Must Follow)

1. **Never work on main branch** → Always create feature branch
2. **Run pre-work check** → `./scripts/pre-work-check.sh` before starting
3. **Tests must pass** → `./gradlew test` before committing
4. **Commit frequently** → Every 15-30 minutes (safety net)
5. **Follow existing patterns** → Check codebase before implementing

---

## 📁 Where to Find Things

| What | Where |
|------|-------|
| Project setup | `README.md` |
| Onboarding guide | `docs/development/ONBOARDING_NEW_STARTERS.md` |
| Rules priority | `docs/development/ONBOARDING_RULES_PRIORITY.md` |
| All rules | `.cursor/rules/*.mdc` |
| Documentation index | `docs/README.md` |
| Workflow guide | `docs/development/workflow/MULTI_AGENT_WORKFLOW.md` |

---

## 🛠️ Common Tasks

### Starting Work
```bash
./scripts/pre-work-check.sh
git checkout -b feature/my-task
```

### Making Changes
```bash
# Make changes, then:
./gradlew test
git add .
git commit -m "WIP: description"
```

### Running App
```bash
./scripts/dev-reload.sh
```

### Creating PR
```bash
git push origin feature/my-task
# Then create PR via GitHub UI or:
gh pr create
```

---

## ⚠️ When Stuck

1. **Check pre-work check** → `./scripts/pre-work-check.sh`
2. **Search codebase** → "How does X work?"
3. **Check docs** → `docs/README.md` for index
4. **Ask questions** → No question is too basic!

---

## 🎯 Daily Workflow

**Morning:**
```bash
git checkout main
git pull origin main
git checkout -b feature/new-task
./scripts/pre-work-check.sh
```

**During work:**
```bash
# Make changes
./gradlew test  # Before committing
git add . && git commit -m "WIP: description"
```

**End of day:**
```bash
git push origin feature/my-task
```

---

## 📞 Emergency Commands

```bash
# Undo last commit (keep changes)
git reset --soft HEAD~1

# See what changed
git status
git diff

# Discard uncommitted changes (DANGER)
git reset --hard HEAD
```

---

## ✅ Checklist: Before Your First PR

- [ ] On feature branch (not main)
- [ ] Pre-work check passed
- [ ] Tests pass (`./gradlew test`)
- [ ] Code follows existing patterns
- [ ] Committed frequently (safety net)
- [ ] Pushed to remote
- [ ] Created PR

---

**Remember**: Commit frequently, ask questions, learn gradually! 🚀

