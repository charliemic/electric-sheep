# Development Metrics

This directory tracks development metrics over time to analyze trends and patterns in the codebase.

## Structure

- **`prompts/`**: Stores each user prompt/request with timestamp
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

## Usage

### Automatic Collection
Run `./scripts/collect-metrics.sh` after builds/tests to automatically capture metrics.

### Manual Collection
Use individual scripts in `scripts/metrics/` for specific metric types.

## Analysis

Periodically review metrics to identify:
- Performance regressions (build/test time increases)
- Quality trends (complexity, coverage changes)
- Accessibility improvements
- Patterns in development workflow

