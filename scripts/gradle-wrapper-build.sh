#!/bin/bash

# Gradle build wrapper that captures metrics automatically
# Usage: ./scripts/gradle-wrapper-build.sh [gradle-args...]

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

# Run build with metrics capture
"$SCRIPT_DIR/metrics/capture-build-metrics.sh" "$@"
BUILD_EXIT=$?

# Collect all metrics
"$SCRIPT_DIR/collect-metrics.sh"

exit $BUILD_EXIT

