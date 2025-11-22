# Entry Point Context Management

**Date**: 2025-01-20  
**Purpose**: Ensure context is smoothly added regardless of how returning contributors enter the workflow

---

## 🎯 Problem

Returning contributors (like Nick Payne) might enter the workflow through different entry points:

1. **Prompt-based entry** - "Create a feature to do X"
2. **Manual entry** - Directly editing files, running commands
3. **Evaluation entry** - "Evaluate the current state of Y"
4. **Script-based entry** - Running automation scripts
5. **Documentation entry** - Reading docs, then starting work

**Challenge**: How do we ensure context is smoothly added regardless of entry point?

---

## ✅ Solution: Entry Point Detection & Context Injection

### Core Principle

**Detect entry point → Gather context → Inject automatically → Proceed**

Context should be added **automatically** based on:
- How the user entered (prompt, manual, script, etc.)
- What they're trying to do (create, evaluate, fix, etc.)
- Current state (branch, files, coordination, etc.)

---

## 🔍 Entry Point Detection

### Detection Methods

**1. Prompt Analysis (AI Agent)**
- Analyze user's first message/prompt
- Detect intent: create, evaluate, fix, refactor, etc.
- Extract task details: feature name, files, scope

**2. Git State Analysis**
- Check current branch
- Check uncommitted changes
- Check recent commits
- Infer what user might be working on

**3. File System Analysis**
- Check open files in editor
- Check recently modified files
- Check for new files
- Infer task from file patterns

**4. Script/Command Detection**
- Detect if user ran a script
- Detect command patterns
- Infer intent from command

**5. Context History**
- Check recent prompts
- Check recent branches
- Check coordination doc
- Use history to infer context

---

## 📋 Entry Point Types

### 1. Prompt-Based Entry

**Detection:**
- User sends a prompt/request to AI agent
- Prompt contains task description
- AI agent receives request

**Context to Inject:**
- ✅ Current branch status
- ✅ Pre-work check results
- ✅ Relevant rules for task type
- ✅ Similar existing patterns
- ✅ Coordination status
- ✅ Recent changes that might affect task

**Example:**
```
User: "Create a feature to add dark mode toggle"

Agent detects:
- Entry point: Prompt-based (create feature)
- Task type: Feature implementation
- Context needed: UI components, theme system, existing patterns

Agent automatically:
1. Runs pre-work check
2. Checks for existing theme/dark mode code
3. Finds similar UI toggle patterns
4. Checks coordination doc
5. Injects all context into response
```

### 2. Manual Entry

**Detection:**
- User directly edits files
- User runs commands manually
- No prompt/agent interaction

**Context to Inject:**
- ✅ Pre-work check (if not run)
- ✅ Branch status
- ✅ File change patterns
- ✅ Relevant rules
- ✅ Coordination warnings

**Example:**
```
User: Opens file, makes changes, commits

System detects:
- Entry point: Manual (file edits)
- Task type: Inferred from file changes
- Context needed: Pre-work check, branch status

System automatically:
1. Detects file changes
2. Runs pre-work check (if not run)
3. Shows relevant warnings/context
4. Suggests next steps
```

### 3. Evaluation Entry

**Detection:**
- User asks to evaluate/analyze something
- User asks "what's the state of X?"
- User asks for recommendations

**Context to Inject:**
- ✅ Current state of what's being evaluated
- ✅ Related systems/components
- ✅ Recent changes
- ✅ Best practices/patterns
- ✅ Recommendations

**Example:**
```
User: "Evaluate the current authentication system"

Agent detects:
- Entry point: Evaluation (analyze system)
- Task type: Analysis/evaluation
- Context needed: Auth code, recent changes, patterns

Agent automatically:
1. Gathers auth-related files
2. Checks recent auth changes
3. Finds related documentation
4. Analyzes current implementation
5. Provides comprehensive evaluation
```

### 4. Script-Based Entry

**Detection:**
- User runs automation script
- Script detects entry point
- Script gathers context

**Context to Inject:**
- ✅ Script-specific context
- ✅ Pre-work check results
- ✅ Current state
- ✅ Script output/guidance

**Example:**
```
User: ./scripts/quick-start-returning-contributor.sh

Script detects:
- Entry point: Script-based (quick start)
- Task type: Setup/onboarding
- Context needed: Environment, state, next steps

Script automatically:
1. Checks environment
2. Runs pre-work check
3. Shows current state
4. Provides next steps
```

### 5. Documentation Entry

**Detection:**
- User reads documentation
- User follows guide
- User asks questions about docs

**Context to Inject:**
- ✅ Relevant documentation
- ✅ Related guides
- ✅ Current state
- ✅ Next steps from docs

**Example:**
```
User: Reads ONBOARDING_RETURNING_CONTRIBUTORS.md

System detects:
- Entry point: Documentation (onboarding)
- Task type: Setup/learning
- Context needed: Quick start, current state

System automatically:
1. Shows quick start steps
2. Checks current state
3. Provides personalized guidance
4. Links to relevant tools
```

---

## 🛠️ Implementation Strategy

### 1. AI Agent Context Injection

**When AI agent receives request:**

```python
# Pseudo-code for context injection
def handle_user_request(user_prompt):
    # 1. Detect entry point
    entry_point = detect_entry_point(user_prompt)
    
    # 2. Gather context based on entry point
    context = gather_context(entry_point, user_prompt)
    
    # 3. Inject context into response
    response = generate_response(user_prompt, context)
    
    return response

def detect_entry_point(prompt):
    if "create" in prompt.lower() or "add" in prompt.lower():
        return "prompt_create"
    elif "evaluate" in prompt.lower() or "analyze" in prompt.lower():
        return "prompt_evaluate"
    elif "fix" in prompt.lower() or "debug" in prompt.lower():
        return "prompt_fix"
    else:
        return "prompt_general"

def gather_context(entry_point, prompt):
    context = {
        "pre_work_check": run_pre_work_check(),
        "branch_status": get_branch_status(),
        "coordination": check_coordination(),
        "similar_patterns": find_similar_patterns(prompt),
        "relevant_rules": find_relevant_rules(prompt),
    }
    
    if entry_point == "prompt_create":
        context["existing_features"] = find_similar_features(prompt)
        context["architecture"] = get_relevant_architecture(prompt)
    
    elif entry_point == "prompt_evaluate":
        context["current_state"] = get_current_state(prompt)
        context["recent_changes"] = get_recent_changes(prompt)
    
    return context
```

### 2. Pre-Work Check Integration

**Enhance pre-work check to detect entry point:**

```bash
# scripts/pre-work-check.sh enhancement
detect_entry_point() {
    # Check if called from script
    if [ -n "$SCRIPT_ENTRY_POINT" ]; then
        echo "$SCRIPT_ENTRY_POINT"
        return
    fi
    
    # Check git state
    if [ -n "$(git status --porcelain)" ]; then
        echo "manual_edit"
        return
    fi
    
    # Check recent commits
    if [ -n "$(git log --oneline -1)" ]; then
        echo "manual_commit"
        return
    fi
    
    # Default
    echo "unknown"
}
```

### 3. Context Injection Script

**Create script to inject context:**

```bash
#!/bin/bash
# scripts/inject-context.sh

ENTRY_POINT="$1"
TASK_DESCRIPTION="$2"

# Gather context based on entry point
case "$ENTRY_POINT" in
    "prompt_create")
        CONTEXT=$(gather_create_context "$TASK_DESCRIPTION")
        ;;
    "prompt_evaluate")
        CONTEXT=$(gather_evaluate_context "$TASK_DESCRIPTION")
        ;;
    "manual")
        CONTEXT=$(gather_manual_context)
        ;;
    *)
        CONTEXT=$(gather_general_context)
        ;;
esac

echo "$CONTEXT"
```

---

## 📊 Context Injection Matrix

| Entry Point | Context Needed | How to Gather | When to Inject |
|-------------|---------------|---------------|----------------|
| **Prompt: Create** | Pre-work check, similar patterns, architecture, coordination | Pre-work check, codebase search, coordination doc | Before generating response |
| **Prompt: Evaluate** | Current state, recent changes, related systems | File analysis, git history, codebase search | Before analysis |
| **Prompt: Fix** | Error details, related code, test status | Error logs, codebase search, test results | Before fixing |
| **Manual: Edit** | Pre-work check, branch status, coordination | Pre-work check, git status | On file save or commit |
| **Manual: Commit** | Pre-work check, test status, coordination | Pre-work check, test results | Before commit |
| **Script: Quick Start** | Environment, state, next steps | Environment check, pre-work check | During script execution |
| **Documentation** | Current state, relevant guides | Pre-work check, doc links | When following guide |

---

## 🎯 Best Practices

### 1. Automatic Context Injection

**✅ DO:**
- Automatically gather context based on entry point
- Inject context seamlessly (user doesn't need to ask)
- Show context in a helpful, non-intrusive way
- Update context as user works

**❌ DON'T:**
- Ask user to manually gather context
- Overwhelm with too much context
- Inject context that's not relevant
- Skip context injection for "simple" tasks

### 2. Context Relevance

**Prioritize context by:**
1. **Directly relevant** - What user is working on
2. **Recently changed** - Recent commits, recent changes
3. **Related systems** - Components that interact
4. **Best practices** - Patterns, rules, guidelines

**Filter out:**
- Unrelated systems
- Outdated information
- Overwhelming detail (show summary, link to details)

### 3. Context Updates

**Update context when:**
- User makes changes
- New commits arrive
- Coordination status changes
- User switches tasks

**Keep context fresh:**
- Re-run pre-work check periodically
- Check for updates
- Refresh related information

---

## 🔧 Implementation Checklist

### For AI Agents

- [ ] Detect entry point from user prompt
- [ ] Gather relevant context automatically
- [ ] Inject context into response
- [ ] Update context as user works
- [ ] Show context in helpful format

### For Scripts

- [ ] Detect entry point (script type, parameters)
- [ ] Gather context based on entry point
- [ ] Show context to user
- [ ] Provide next steps based on context

### For Manual Workflow

- [ ] Detect manual changes (file edits, commands)
- [ ] Run pre-work check automatically
- [ ] Show relevant warnings/context
- [ ] Suggest next steps

---

## 📚 Examples

### Example 1: Prompt-Based Entry (Create Feature)

**User:** "Create a feature to add dark mode toggle"

**Agent automatically:**
1. Detects entry point: `prompt_create`
2. Runs pre-work check
3. Searches for existing theme/dark mode code
4. Finds similar UI toggle patterns
5. Checks coordination doc
6. Injects context into response:

```
📋 Context Gathered:
✅ Pre-work check passed
✅ On feature branch: feature/dark-mode-toggle
✅ No coordination conflicts
✅ Found similar patterns: ThemeToggle.kt, SettingsScreen.kt
✅ Relevant rules: .cursor/rules/design.mdc, .cursor/rules/accessibility.mdc

🎯 Ready to create dark mode toggle feature
```

### Example 2: Manual Entry (File Edit)

**User:** Opens `Theme.kt`, makes changes

**System automatically:**
1. Detects entry point: `manual_edit`
2. Runs pre-work check (if not run)
3. Shows context:

```
📋 Context:
✅ Pre-work check passed
✅ On feature branch: feature/dark-mode-toggle
⚠️  Related files: SettingsScreen.kt (may need updates)
💡 Tip: Check .cursor/rules/design.mdc for theme guidelines
```

### Example 3: Evaluation Entry

**User:** "Evaluate the current authentication system"

**Agent automatically:**
1. Detects entry point: `prompt_evaluate`
2. Gathers auth-related files
3. Checks recent auth changes
4. Finds related documentation
5. Provides comprehensive evaluation with context

---

## 🚀 Future Enhancements

**Potential improvements:**
1. **Context persistence** - Save context for returning sessions
2. **Context learning** - Learn what context is most useful
3. **Context sharing** - Share context between agents
4. **Context templates** - Pre-built context for common tasks
5. **Context validation** - Verify context is accurate and up-to-date

---

## 📖 Related Documentation

- `docs/development/ONBOARDING_RETURNING_CONTRIBUTORS.md` - Returning contributors guide
- `docs/development/workflow/SMART_PROMPTS_ARCHITECTURE.md` - Smart prompts (context evaluation)
- `scripts/pre-work-check.sh` - Pre-work check (context gathering)
- `.cursor/rules/smart-prompts.mdc` - Context-driven risk assessment

---

**Result**: Context is smoothly added regardless of entry point, making collaboration super easy! 🚀

