#!/bin/bash

# Simplified overlay script for testing
# Usage: ./scripts/add-activity-overlays-simple.sh <video_file> <activities_log> <output_file>

set -e

VIDEO_FILE="$1"
ACTIVITIES_LOG="$2"
OUTPUT_FILE="$3"

if [ ! -f "$VIDEO_FILE" ] || [ ! -f "$ACTIVITIES_LOG" ]; then
    echo "❌ Missing required files"
    exit 1
fi

echo "🎬 Adding simplified overlays..."

# Extract just a few key activities
KEY_ACTIVITIES=(
    "Looking for the mood tracking feature"
    "Opening the sign-up form"
    "Entering my email address"
    "Creating my account"
    "Saving my mood entry"
)

# Build simple filter complex with just a few overlays
FILTERS=()
TIME=0
for activity in "${KEY_ACTIVITIES[@]}"; do
    # Escape for ffmpeg
    ESCAPED=$(echo "$activity" | sed "s/'/\\\\'/g" | sed "s/:/\\\:/g")
    FILTERS+=("drawtext=text='$ESCAPED':fontsize=42:fontcolor=white:x=(w-text_w)/2:y=h-th-60:box=1:boxcolor=black@0.95:boxborderw=14:borderw=5:bordercolor=white@1.0:enable='between(t,$TIME,$((TIME+2)))'")
    TIME=$((TIME + 3))
done

FILTER_COMPLEX=$(IFS=','; echo "${FILTERS[*]}")

echo "📝 Applying ${#KEY_ACTIVITIES[@]} overlays..."

ffmpeg -i "$VIDEO_FILE" \
    -vf "$FILTER_COMPLEX" \
    -c:v libx264 \
    -preset medium \
    -crf 23 \
    -c:a aac \
    -b:a 128k \
    -movflags +faststart \
    -pix_fmt yuv420p \
    -y \
    "$OUTPUT_FILE" 2>&1 | grep -E "(error|Error|ERROR|Applying|saved)" || true

if [ -f "$OUTPUT_FILE" ]; then
    FILE_SIZE=$(ls -lh "$OUTPUT_FILE" | awk '{print $5}')
    echo "✅ Video with overlays saved: $OUTPUT_FILE ($FILE_SIZE)"
else
    echo "❌ Failed to create video"
    exit 1
fi



