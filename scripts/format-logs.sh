#!/bin/bash

# Format log file to be more human-readable
# Removes technical details, focuses on cognitive processes

INPUT_FILE="${1:-/tmp/streaming-test.log}"
OUTPUT_FILE="${2:-/tmp/human-readable-test.log}"

if [ ! -f "$INPUT_FILE" ]; then
    echo "Error: Input file not found: $INPUT_FILE"
    exit 1
fi

echo "Formatting logs for human readability..."
echo "Input: $INPUT_FILE"
echo "Output: $OUTPUT_FILE"
echo ""

# Process the log file
cat "$INPUT_FILE" | \
    # Remove timestamps and thread info from console output (keep for file logs)
    sed -E 's/^[0-9]{2}:[0-9]{2}:[0-9]{2}\.[0-9]{3} \[[^\]]+\] (DEBUG|INFO|WARN|ERROR)  [^ ]+ - //' | \
    # Remove Gradle build output noise
    grep -v "^> Task :" | \
    grep -v "^BUILD" | \
    # Remove empty lines (but keep single blank lines for readability)
    sed '/^$/N;/^\n$/d' | \
    # Clean up any remaining technical markers
    sed 's/^\[main\] //' | \
    sed 's/^\[DefaultDispatcher-worker-[0-9]+\] //' | \
    # Write to output
    tee "$OUTPUT_FILE" | \
    # Show first 100 lines
    head -100

echo ""
echo "✅ Formatted log saved to: $OUTPUT_FILE"
echo "   Full log: $(wc -l < "$OUTPUT_FILE") lines"

