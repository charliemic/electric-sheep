# Entry Point Context Management - Quick Reference

**One-page reference for entry point detection and context injection**

---

## 🎯 Core Concept

**Detect entry point → Gather context → Inject automatically → Proceed**

Context is added **automatically** based on how you enter the workflow.

---

## 🔍 Entry Point Types

| Entry Point | How Detected | Context Gathered |
|-------------|--------------|-----------------|
| **Prompt: Create** | AI analyzes prompt for "create/add/implement" | Pre-work check, similar patterns, architecture, coordination |
| **Prompt: Evaluate** | AI analyzes prompt for "evaluate/analyze/review" | Current state, recent changes, related systems |
| **Prompt: Fix** | AI analyzes prompt for "fix/debug/repair" | Error details, related code, test status |
| **Manual: Edit** | Git detects uncommitted changes | Pre-work check, branch status, coordination warnings |
| **Manual: Commit** | Git detects staged changes | Pre-work check, test status, coordination |
| **Script** | Script execution detected | Script-specific context, state, next steps |
| **Documentation** | User in docs directory or reading docs | Current state, relevant guides, next steps |

---

## 🛠️ Usage

### For AI Agents

**Automatic (Recommended):**
- AI agent automatically detects entry point from user prompt
- Gathers context automatically
- Injects context into response
- No manual action needed

**Manual Detection:**
```bash
./scripts/detect-entry-point.sh "create a feature to add dark mode"
```

### For Manual Work

**Automatic:**
- Pre-work check detects manual changes
- Gathers context automatically
- Shows warnings/context

**Manual:**
```bash
./scripts/detect-entry-point.sh
```

---

## 📋 Context Injection Matrix

| Entry Point | Pre-Work Check | Similar Patterns | Coordination | Recent Changes | Test Status |
|-------------|----------------|-----------------|--------------|----------------|-------------|
| **Prompt: Create** | ✅ | ✅ | ✅ | ⚠️ | ⚠️ |
| **Prompt: Evaluate** | ⚠️ | ⚠️ | ⚠️ | ✅ | ⚠️ |
| **Prompt: Fix** | ✅ | ⚠️ | ⚠️ | ⚠️ | ✅ |
| **Manual: Edit** | ✅ | ⚠️ | ✅ | ⚠️ | ⚠️ |
| **Manual: Commit** | ✅ | ⚠️ | ✅ | ⚠️ | ✅ |
| **Script** | ✅ | ❌ | ⚠️ | ⚠️ | ⚠️ |
| **Documentation** | ⚠️ | ❌ | ❌ | ❌ | ❌ |

**Legend:**
- ✅ Always gathered
- ⚠️ Gathered if relevant
- ❌ Not gathered

---

## 🎯 Best Practices

### ✅ DO

- **Let system detect automatically** - No need to manually specify entry point
- **Trust context injection** - System gathers what's needed
- **Review context when shown** - Check warnings and suggestions
- **Update context as you work** - Re-run pre-work check if needed

### ❌ DON'T

- **Don't manually gather context** - System does it automatically
- **Don't skip context review** - Check warnings and suggestions
- **Don't ignore coordination** - Check if others are working on same files
- **Don't skip pre-work check** - It catches critical issues

---

## 📚 Examples

### Example 1: Prompt-Based Entry

**User:** "Create a feature to add dark mode toggle"

**System automatically:**
1. Detects: `prompt_create`
2. Gathers: Pre-work check, similar patterns, coordination
3. Injects: Context in AI response
4. Shows: Similar implementations, relevant rules, warnings

### Example 2: Manual Entry

**User:** Opens file, makes changes

**System automatically:**
1. Detects: `manual_edit`
2. Gathers: Pre-work check, branch status, coordination
3. Shows: Warnings if on main, coordination conflicts, next steps

### Example 3: Evaluation Entry

**User:** "Evaluate the current authentication system"

**System automatically:**
1. Detects: `prompt_evaluate`
2. Gathers: Current state, recent changes, related systems
3. Provides: Comprehensive evaluation with context

---

## 🔧 Scripts

**Entry Point Detection:**
```bash
./scripts/detect-entry-point.sh [task-description]
```

**Pre-Work Check (includes context):**
```bash
./scripts/pre-work-check.sh
```

**Quick Start (for returning contributors):**
```bash
./scripts/quick-start-returning-contributor.sh
```

---

## 📖 Related Documentation

- `docs/development/workflow/ENTRY_POINT_CONTEXT_MANAGEMENT.md` - Complete guide
- `docs/development/ONBOARDING_RETURNING_CONTRIBUTORS.md` - Returning contributors guide
- `scripts/detect-entry-point.sh` - Entry point detection script
- `scripts/pre-work-check.sh` - Pre-work check (includes context)

---

**Remember**: Context is added automatically - you don't need to do anything! 🚀

