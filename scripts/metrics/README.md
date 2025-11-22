# Metrics Collection Scripts

Simple, feature-rich metrics collection for development workflow.

## Quick Reference

### Unified Helper (Recommended)

```bash
# Capture prompt
./scripts/metrics/capture-metrics.sh prompt "Your prompt text"

# Capture prompt with agent usage
./scripts/metrics/capture-metrics.sh prompt "Fix bug" --model "anthropic.claude-sonnet-4-5-20250929-v1:0" --task-type "bug_fix"

# Capture agent usage separately
./scripts/metrics/capture-metrics.sh agent --model "anthropic.claude-3-5-haiku-20241022-v2:0" --input-tokens 1000 --output-tokens 500

# Session management
./scripts/metrics/capture-metrics.sh session start "Task description"
./scripts/metrics/capture-metrics.sh session update
./scripts/metrics/capture-metrics.sh session check

# Capture all metrics
./scripts/metrics/capture-metrics.sh all
```

### Simple Prompt Capture (Easiest)

```bash
# Just capture prompt
./scripts/metrics/capture-prompt-simple.sh "Your prompt text"

# With model shortcut
./scripts/metrics/capture-prompt-simple.sh "Your prompt" --model sonnet --task bug_fix

# Model shortcuts: haiku, sonnet, opus
```

### Individual Scripts

**Prompt Tracking:**
- `capture-prompt.sh` - Full-featured prompt capture
- `capture-prompt-simple.sh` - Simplified with model shortcuts

**Agent Usage:**
- `capture-agent-usage.sh` - Track model usage and costs

**Session Tracking:**
- `../track-session-scope.sh` - Scope creep detection

**Code Metrics:**
- `capture-complexity-metrics.sh` - Code complexity
- `capture-accessibility-metrics.sh` - Accessibility violations
- `capture-test-metrics.sh` - Test execution metrics
- `capture-build-metrics.sh` - Build execution metrics

**Dashboard:**
- `generate-dashboard.sh` - Generate HTML dashboard
- `show-dashboard.sh` - Quick CLI view

## Integration Points

### Automatic Collection

1. **Pre-Work Check** (`scripts/pre-work-check.sh`)
   - Auto-starts session tracking on feature branches
   - Checks for scope creep

2. **Build Wrapper** (`scripts/gradle-wrapper-build.sh`)
   - Automatically captures build metrics
   - Use instead of `./gradlew build`

3. **Test Wrapper** (`scripts/gradle-wrapper-test.sh`)
   - Automatically captures test metrics
   - Use instead of `./gradlew test`

### Manual Collection

- **Prompts**: Use `capture-metrics.sh prompt` or `capture-prompt-simple.sh`
- **Agent Usage**: Use `capture-metrics.sh agent` or `capture-agent-usage.sh`
- **All Metrics**: Use `capture-metrics.sh all`

## Architecture

### Simple but Feature-Rich

- **Unified Interface**: One command (`capture-metrics.sh`) for all metrics
- **Simple Helpers**: Easy-to-use wrappers with shortcuts
- **Automatic Integration**: Hooks into existing workflows (pre-work, build, test)
- **Flexible**: Can use individual scripts for advanced use cases

### Data Storage

- **Local JSON**: `development-metrics/` directory
- **Supabase**: Agent usage synced to `agent_usage` table (when configured)
- **Dashboard**: HTML dashboard for visualization

### Session Tracking

- **Auto-Start**: Automatically starts when running pre-work check on feature branch
- **Scope Creep**: AI-based evaluation of scope expansion
- **Updates**: Automatic updates during workflow

## Examples

### Daily Workflow

```bash
# 1. Start work (auto-starts session)
./scripts/pre-work-check.sh

# 2. Capture prompts as you work
./scripts/metrics/capture-prompt-simple.sh "Implement feature X" --model sonnet

# 3. Build/test (auto-captures metrics)
./scripts/gradle-wrapper-build.sh
./scripts/gradle-wrapper-test.sh

# 4. Check scope creep
./scripts/metrics/capture-metrics.sh session check

# 5. View dashboard
./scripts/metrics/generate-dashboard.sh
```

### Cost Tracking

```bash
# Track prompt with cost
./scripts/metrics/capture-prompt-simple.sh "Fix bug" --model haiku --task bug_fix

# Track agent usage separately (if you have token counts)
./scripts/metrics/capture-metrics.sh agent \
  --model "anthropic.claude-3-5-haiku-20241022-v2:0" \
  --input-tokens 1000 \
  --output-tokens 500 \
  --task-type "bug_fix"
```

## See Also

- `development-metrics/README.md` - Complete metrics documentation
- `docs/development/analysis/AGENT_USAGE_TRACKING.md` - Agent usage tracking guide
- `scripts/pre-work-check.sh` - Pre-work checklist (auto-starts sessions)

