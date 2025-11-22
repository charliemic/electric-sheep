# Development Metrics

This directory tracks development metrics over time to analyze trends and patterns in the codebase.

## Structure

- **`prompts/`**: Stores each user prompt/request with timestamp
- **`agent-usage/`**: Stores agent usage data (tokens, costs, models) for cost tracking
- **`builds/`**: Build execution times, success/failure status
- **`tests/`**: Test execution times, failure counts, coverage
- **`complexity/`**: Code complexity metrics (cyclomatic complexity, etc.)
- **`accessibility/`**: Accessibility scan results and violations
- **`coverage/`**: Code coverage reports and trends

## Metrics Collected

### Build Metrics
- Execution time
- Success/failure status
- Build type (debug/release)
- Gradle version
- Java version

### Test Metrics
- Total test count
- Passed/failed/skipped counts
- Execution time
- Coverage percentage
- Test failures (if any)

### Code Complexity
- Cyclomatic complexity
- Cognitive complexity
- Lines of code
- Number of classes/functions

### Accessibility
- Violation count
- Violation types
- Severity levels

### Agent Usage (Cost Tracking)
- Model used (Haiku, Sonnet, Opus)
- Token usage (input, output, total)
- Cost per interaction
- Task type and complexity
- Session tracking

## Usage

### Quick Start (Simplified)

**Unified Helper (Recommended):**
```bash
# Capture a prompt (simplest)
./scripts/metrics/capture-metrics.sh prompt "Your prompt text"

# Capture prompt with agent usage (model shortcuts)
./scripts/metrics/capture-metrics.sh prompt "Fix login bug" --model "anthropic.claude-sonnet-4-5-20250929-v1:0" --task-type "bug_fix"

# Or use simple wrapper with model shortcuts
./scripts/metrics/capture-prompt-simple.sh "Your prompt" --model sonnet --task bug_fix
```

**Session Tracking (Auto-started in pre-work check):**
```bash
# Session auto-starts when running pre-work check on a feature branch
./scripts/pre-work-check.sh

# Manual session management
./scripts/metrics/capture-metrics.sh session start "Task description"
./scripts/metrics/capture-metrics.sh session update
./scripts/metrics/capture-metrics.sh session check
```

**Build/Test Metrics (Automatic via wrappers):**
```bash
# Use wrapper scripts for automatic metrics capture
./scripts/gradle-wrapper-build.sh
./scripts/gradle-wrapper-test.sh

# Or use standard gradle commands (metrics won't be captured)
./gradlew build
./gradlew test
```

### Automatic Collection

**Build/Test Metrics:**
- Use `./scripts/gradle-wrapper-build.sh` instead of `./gradlew build`
- Use `./scripts/gradle-wrapper-test.sh` instead of `./gradlew test`
- Metrics are automatically captured during execution

**Session Tracking:**
- Auto-starts when running `./scripts/pre-work-check.sh` on a feature branch
- Tracks scope creep automatically
- Updates on each pre-work check

**Complexity & Accessibility:**
```bash
# Capture all available metrics
./scripts/metrics/capture-metrics.sh all

# Or use individual scripts
./scripts/metrics/capture-complexity-metrics.sh
./scripts/metrics/capture-accessibility-metrics.sh
```

### Manual Collection (Advanced)

**Individual Scripts:**
```bash
# Capture prompt
./scripts/metrics/capture-prompt.sh "Your prompt here" --model "anthropic.claude-sonnet-4-5-20250929-v1:0" --task-type "feature_implementation"

# Capture agent usage separately
./scripts/metrics/capture-agent-usage.sh --model "anthropic.claude-sonnet-4-5-20250929-v1:0" --input-tokens 1000 --output-tokens 500 --task-type "feature_implementation"
```

**Model Shortcuts (Simple Helper):**
```bash
# Use model shortcuts for easier typing
./scripts/metrics/capture-prompt-simple.sh "Your prompt" --model haiku   # Claude 3.5 Haiku
./scripts/metrics/capture-prompt-simple.sh "Your prompt" --model sonnet  # Claude 3.5 Sonnet
./scripts/metrics/capture-prompt-simple.sh "Your prompt" --model opus    # Claude 3 Opus
```

## Analysis

### Metrics Dashboard

**Single Pane of Glass**: Dynamic, self-hosted dashboard with auto-refresh.

```bash
# Start dynamic dashboard server (auto-refreshes every 5 seconds)
./scripts/metrics/start-dashboard.sh

# Or generate static HTML dashboard
./scripts/metrics/generate-dashboard.sh

# Quick CLI view
./scripts/metrics/show-dashboard.sh
```

**Features**:
- ✅ Aggregates all metrics from all sources
- ✅ **Auto-refreshes every 5 seconds** - Always shows latest data
- ✅ **Shows active agents** - Displays which agents are working and their status
- ✅ **Dynamic updates** - No need to manually refresh
- ✅ Self-hosted (runs on localhost:8080)
- ✅ Uses Node.js (aligned with existing `html-viewer` infrastructure)

**What it shows**:
- Code complexity (files, lines, classes, functions)
- Test metrics (passed, failed, execution time)
- Test to source ratio
- **Active agents** (worktrees, branches, task status, files modified)
- Real-time updates

**Usage**:
1. Run `./scripts/metrics/start-dashboard.sh`
2. Open http://localhost:8080 in your browser
3. Dashboard auto-refreshes every 5 seconds

**Technology Stack**:
- **Backend**: Fastify (Node.js) - ✅ Implemented & tested
- **Frontend**: Chart.js (recommended) - ⏭️ Ready to implement
- **Rationale**: Aligned with existing `html-viewer` infrastructure and project principles (simplicity, free, feature-rich)

**See**:
- `scripts/metrics/IMPLEMENTATION_STATUS.md` - Current status
- `scripts/metrics/FRONTEND_EVALUATION.md` - Frontend options
- `scripts/metrics/FRAMEWORK_COMPARISON.md` - Backend comparison

### Manual Analysis

Periodically review metrics to identify:
- Performance regressions (build/test time increases)
- Quality trends (complexity, coverage changes)
- Accessibility improvements
- Patterns in development workflow

