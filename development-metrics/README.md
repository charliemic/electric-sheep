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

### Automatic Collection
Run `./scripts/collect-metrics.sh` after builds/tests to automatically capture metrics.

### Manual Collection
Use individual scripts in `scripts/metrics/` for specific metric types.

**Agent Usage Tracking:**
```bash
# Capture prompt with agent usage
./scripts/metrics/capture-prompt.sh "Your prompt here" --model "anthropic.claude-sonnet-4-5-20250929-v1:0" --task-type "feature_implementation"

# Or capture agent usage separately
./scripts/metrics/capture-agent-usage.sh --model "anthropic.claude-sonnet-4-5-20250929-v1:0" --input-tokens 1000 --output-tokens 500 --task-type "feature_implementation"
```

## Analysis

### Metrics Dashboard

**Single Pane of Glass**: Unified dashboard with AI-assessed health indicators.

```bash
# Generate complete dashboard
./scripts/metrics/generate-dashboard.sh

# Quick CLI view
./scripts/metrics/show-dashboard.sh
```

**Features**:
- ✅ Aggregates all metrics from all sources
- ✅ AI-powered health assessment (AWS Bedrock)
- ✅ Trend analysis (improving/stable/degrading)
- ✅ Health scores (0-100) for each category
- ✅ Actionable recommendations

**Health Categories**:
- Code Health (30%): Complexity, maintainability
- Development Velocity (25%): Build/test times, iteration speed
- Cost Efficiency (20%): AI usage, optimization
- Quality (25%): Test stability, error rates

**See**: `docs/development/METRICS_DASHBOARD_QUICK_REFERENCE.md` for complete guide.

### Manual Analysis

Periodically review metrics to identify:
- Performance regressions (build/test time increases)
- Quality trends (complexity, coverage changes)
- Accessibility improvements
- Patterns in development workflow

