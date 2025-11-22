# Metrics Collection Setup - Complete

**Status**: ✅ Fully Wired Up and Ready to Use

## Overview

The metrics collection system is now fully integrated into the development workflow. It's designed to be **simple but feature-rich** - easy to use for common cases, powerful for advanced use cases.

## What's Wired Up

### ✅ Automatic Collection

1. **Session Tracking**
   - Auto-starts when running `./scripts/pre-work-check.sh` on a feature branch
   - Tracks scope creep automatically
   - Updates during workflow

2. **Build Metrics**
   - Use `./scripts/gradle-wrapper-build.sh` instead of `./gradlew build`
   - Automatically captures build execution time, success/failure, etc.

3. **Test Metrics**
   - Use `./scripts/gradle-wrapper-test.sh` instead of `./gradlew test`
   - Automatically captures test counts, execution time, failures, etc.

4. **Complexity & Accessibility**
   - Run `./scripts/metrics/capture-metrics.sh all` to capture all available metrics
   - Or use individual scripts as needed

### ✅ Manual Collection (Easy)

1. **Unified Helper** (`capture-metrics.sh`)
   - One command for all metrics types
   - Simple, consistent interface
   - Handles prompts, agent usage, sessions, and all metrics

2. **Simple Prompt Capture** (`capture-prompt-simple.sh`)
   - Easiest way to capture prompts
   - Model shortcuts (haiku, sonnet, opus)
   - Minimal typing required

## Quick Start

### Daily Workflow

```bash
# 1. Start work (auto-starts session tracking)
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

### Model Shortcuts

```bash
# Use simple model shortcuts
./scripts/metrics/capture-prompt-simple.sh "Your prompt" --model haiku   # Claude 3.5 Haiku
./scripts/metrics/capture-prompt-simple.sh "Your prompt" --model sonnet  # Claude 3.5 Sonnet
./scripts/metrics/capture-prompt-simple.sh "Your prompt" --model opus    # Claude 3 Opus
```

## Architecture

### Simple but Feature-Rich

**Design Principles:**
- **Unified Interface**: One command (`capture-metrics.sh`) for all metrics
- **Simple Helpers**: Easy-to-use wrappers with shortcuts
- **Automatic Integration**: Hooks into existing workflows
- **Flexible**: Can use individual scripts for advanced use cases

**Integration Points:**
- Pre-work check → Auto-starts session tracking
- Build wrapper → Auto-captures build metrics
- Test wrapper → Auto-captures test metrics
- Manual helpers → Easy prompt/agent usage tracking

### Data Flow

```
User Action → Helper Script → Metrics Storage
                ↓
         (Auto-capture where possible)
                ↓
         development-metrics/
                ↓
         Dashboard / Supabase
```

## Files Created/Modified

### New Files

1. **`scripts/metrics/capture-metrics.sh`**
   - Unified helper for all metrics
   - Simple, consistent interface
   - Handles prompts, agent usage, sessions, all metrics

2. **`scripts/metrics/capture-prompt-simple.sh`**
   - Simplified prompt capture
   - Model shortcuts (haiku, sonnet, opus)
   - Minimal typing required

3. **`scripts/metrics/README.md`**
   - Quick reference guide
   - Examples and usage patterns

4. **`docs/development/workflow/METRICS_COLLECTION_SETUP.md`** (this file)
   - Complete setup documentation

### Modified Files

1. **`scripts/pre-work-check.sh`**
   - Auto-starts session tracking on feature branches
   - Integrates with scope creep detection

2. **`development-metrics/README.md`**
   - Updated with simplified usage
   - Added quick start guide
   - Model shortcuts documentation

## Usage Examples

### Capture Prompt

```bash
# Simplest
./scripts/metrics/capture-prompt-simple.sh "Your prompt"

# With model and task type
./scripts/metrics/capture-prompt-simple.sh "Fix bug" --model haiku --task bug_fix

# Using unified helper
./scripts/metrics/capture-metrics.sh prompt "Your prompt" --model "anthropic.claude-sonnet-4-5-20250929-v1:0"
```

### Capture Agent Usage

```bash
# Using unified helper
./scripts/metrics/capture-metrics.sh agent \
  --model "anthropic.claude-3-5-haiku-20241022-v2:0" \
  --input-tokens 1000 \
  --output-tokens 500 \
  --task-type "bug_fix"
```

### Session Management

```bash
# Auto-starts in pre-work check, or manually:
./scripts/metrics/capture-metrics.sh session start "Task description"
./scripts/metrics/capture-metrics.sh session update
./scripts/metrics/capture-metrics.sh session check
```

### Capture All Metrics

```bash
# Capture all available metrics
./scripts/metrics/capture-metrics.sh all
```

## Next Steps

### For Agents

1. **Use simple helpers** for prompt capture:
   ```bash
   ./scripts/metrics/capture-prompt-simple.sh "Your prompt" --model sonnet
   ```

2. **Use build/test wrappers** for automatic metrics:
   ```bash
   ./scripts/gradle-wrapper-build.sh
   ./scripts/gradle-wrapper-test.sh
   ```

3. **Check scope creep** periodically:
   ```bash
   ./scripts/metrics/capture-metrics.sh session check
   ```

### For Analysis

1. **Generate dashboard**:
   ```bash
   ./scripts/metrics/generate-dashboard.sh
   ```

2. **View metrics**:
   - Open `development-metrics/dashboard.html` in browser
   - Or use `./scripts/metrics/show-dashboard.sh` for CLI view

3. **Analyze costs** (when data is available):
   ```bash
   ./scripts/analysis/analyze-costs.sh
   ```

## See Also

- `development-metrics/README.md` - Complete metrics documentation
- `scripts/metrics/README.md` - Quick reference guide
- `docs/development/analysis/AGENT_USAGE_TRACKING.md` - Agent usage tracking guide
- `scripts/pre-work-check.sh` - Pre-work checklist (auto-starts sessions)

