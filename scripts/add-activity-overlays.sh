#!/bin/bash

# Add activity overlays to test video
# Usage: ./scripts/add-activity-overlays.sh <video_file> <activities_log> <output_file>

set -e

VIDEO_FILE="$1"
ACTIVITIES_LOG="$2"
OUTPUT_FILE="${3:-${VIDEO_FILE%.*}_with_overlays.mp4}"

if [ ! -f "$VIDEO_FILE" ]; then
    echo "❌ Video file not found: $VIDEO_FILE"
    exit 1
fi

if [ ! -f "$ACTIVITIES_LOG" ]; then
    echo "❌ Activities log not found: $ACTIVITIES_LOG"
    exit 1
fi

if ! command -v ffmpeg > /dev/null 2>&1; then
    echo "❌ ffmpeg not found. Install with: brew install ffmpeg"
    exit 1
fi

echo "🎬 Adding activity overlays to video..."
echo "   Input: $VIDEO_FILE"
echo "   Activities: $ACTIVITIES_LOG"
echo "   Output: $OUTPUT_FILE"
echo ""

# Create a temporary file for overlay commands
OVERLAY_FILE=$(mktemp)
trap "rm -f $OVERLAY_FILE" EXIT

# Parse activities log and create overlay commands
# Format: [TIMESTAMP] 👤 USER ACTIVITY: activity text
ACTIVITY_COUNT=0
# Position overlays at bottom of screen (best practice: bottom center/left)
# Calculate from bottom: y = h - (line_height * num_lines) - margin
# We'll use relative positioning with ffmpeg expressions
BOTTOM_MARGIN=60  # Margin from bottom in pixels
LINE_HEIGHT=70  # Increased for larger text and multi-line support

# Extract activities with timestamps into array
ACTIVITIES=()
THOUGHTS=()
while IFS= read -r line; do
    # Extract timestamp and activity text
    if [[ $line =~ \[([0-9]{4}-[0-9]{2}-[0-9]{2}\ [0-9]{2}:[0-9]{2}:[0-9]{2})\].*👤\ USER\ ACTIVITY:\ (.*) ]]; then
        TIMESTAMP="${BASH_REMATCH[1]}"
        ACTIVITY="${BASH_REMATCH[2]}"
        ACTIVITIES+=("$TIMESTAMP|$ACTIVITY")
    fi
    # Also capture persona thoughts
    if [[ $line =~ \[([0-9]{4}-[0-9]{2}-[0-9]{2}\ [0-9]{2}:[0-9]{2}:[0-9]{2})\].*💭\ (PERSONA\ THOUGHT|Thinking):\ (.*) ]]; then
        TIMESTAMP="${BASH_REMATCH[1]}"
        THOUGHT="${BASH_REMATCH[3]}"
        THOUGHTS+=("$TIMESTAMP|$THOUGHT")
    fi
done < <(cat "$ACTIVITIES_LOG")

# Filter out technical/planning activities that aren't useful for narration
# Keep only user-facing activities
FILTERED_ACTIVITIES=()
for activity_entry in "${ACTIVITIES[@]}"; do
    IFS='|' read -r timestamp activity <<< "$activity_entry"
    # Skip technical planning activities
    if ! echo "$activity" | grep -qE "(Planning|Will |ElementVisible|accessibilityId|target=)" && \
       ! echo "$activity" | grep -qE "^(User wants to|Context:)" && \
       echo "$activity" | grep -qE "(Looking|Opening|Entering|Setting|Creating|Saving|Waiting|Checking|Recording)"; then
        FILTERED_ACTIVITIES+=("$activity_entry")
    fi
done

# Process each filtered activity and its corresponding thought
ACTIVITY_INDEX=0
for activity_entry in "${FILTERED_ACTIVITIES[@]}"; do
    IFS='|' read -r timestamp activity <<< "$activity_entry"
    
    ACTIVITY_COUNT=$((ACTIVITY_COUNT + 1))
    
    # Calculate time offset from start (each activity appears for 3 seconds)
    START_TIME=$((($ACTIVITY_COUNT - 1) * 3))
    END_TIME=$(($START_TIME + 2))
    
    # Clean up activity text for display - make it narrator-like
    # Remove emojis and technical terms, make it conversational
    CLEAN_ACTIVITY=$(echo "$activity" | \
        sed "s/👆//g" | \
        sed "s/⌨️//g" | \
        sed "s/⏳//g" | \
        sed "s/✅//g" | \
        sed "s/⬅️//g" | \
        sed "s/📸//g" | \
        sed "s/📋//g" | \
        sed "s/Will //g" | \
        sed "s/^Looking for the/Looking for the/g" | \
        sed "s/^Opening the/Opening the/g" | \
        sed "s/^Entering my/Entering my/g" | \
        sed "s/^Setting up my/Setting up my/g" | \
        sed "s/^Creating my/Creating my/g" | \
        sed "s/^Saving my/Saving my/g" | \
        sed "s/^Waiting for the/Waiting for the/g" | \
        sed "s/^Checking if/Checking if/g" | \
        sed "s/^Recording how/Recording how/g" | \
        sed "s/^Tapping on://g" | \
        sed "s/^Typing into://g")
    
    # Break long lines into multiple lines for readability (max 40 chars per line)
    if [ ${#CLEAN_ACTIVITY} -gt 40 ]; then
        # Try to break at natural points
        FIRST_LINE="${CLEAN_ACTIVITY:0:40}"
        REST="${CLEAN_ACTIVITY:40}"
        # Find last space in first 40 chars
        LAST_SPACE=$(echo "$FIRST_LINE" | grep -o '.* ' | wc -c)
        if [ $LAST_SPACE -gt 20 ]; then
            FIRST_LINE="${CLEAN_ACTIVITY:0:$((LAST_SPACE-1))}"
            REST="${CLEAN_ACTIVITY:$((LAST_SPACE-1))}"
        fi
        CLEAN_ACTIVITY="$FIRST_LINE\\n$REST"
    fi
    
    # Escape special characters for ffmpeg
    CLEAN_ACTIVITY_ESC=$(echo "$CLEAN_ACTIVITY" | \
        sed "s/'/\\\\'/g" | \
        sed "s/:/\\\:/g" | \
        sed "s/%/\\\%/g" | \
        sed "s/\\n/\\\\n/g")
    
    # Calculate Y position from bottom (best practice: bottom center)
    # Stack multiple overlays from bottom up - use simpler calculation
    # For multiple overlays, show only the current one (don't stack all at once)
    # Each overlay appears at the same bottom position
    OFFSET_FROM_BOTTOM=$BOTTOM_MARGIN
    
    # Build drawtext filter with improved styling (single line for ffmpeg)
    # Position at bottom center for better visibility and prominence
    FILTER="drawtext=text='$CLEAN_ACTIVITY_ESC':fontsize=42:fontcolor=white:x=(w-text_w)/2:y=h-th-$OFFSET_FROM_BOTTOM:box=1:boxcolor=black@0.95:boxborderw=14:borderw=5:bordercolor=white@1.0:line_spacing=14:enable='between(t,$START_TIME,$END_TIME)'"
    
    echo "$FILTER" >> "$OVERLAY_FILE"
    
    # Add corresponding thought if available (same timestamp or next)
    if [ $ACTIVITY_INDEX -lt ${#THOUGHTS[@]} ]; then
        THOUGHT_ENTRY="${THOUGHTS[$ACTIVITY_INDEX]}"
        IFS='|' read -r thought_timestamp thought <<< "$THOUGHT_ENTRY"
        
        # Clean up thought text - make it narrator-like
        CLEAN_THOUGHT=$(echo "$thought" | \
            sed "s/I see this app can help me track my mood. Let me try it./I see this app can help me track my mood.\\nLet me try it./g" | \
            sed "s/I need to enter my email address. I hope I remember it correctly./I need to enter my email address.\\nI hope I remember it correctly./g" | \
            sed "s/I hope this works. I'm not sure if my password is strong enough./I hope this works.\\nI'm not sure if my password is strong enough./g")
        
        # Break long thoughts into multiple lines (max 45 chars per line)
        if [ ${#CLEAN_THOUGHT} -gt 45 ] && ! echo "$CLEAN_THOUGHT" | grep -q "\\\\n"; then
            FIRST_LINE="${CLEAN_THOUGHT:0:45}"
            REST="${CLEAN_THOUGHT:45}"
            LAST_SPACE=$(echo "$FIRST_LINE" | grep -o '.* ' | wc -c)
            if [ $LAST_SPACE -gt 20 ]; then
                FIRST_LINE="${CLEAN_THOUGHT:0:$((LAST_SPACE-1))}"
                REST="${CLEAN_THOUGHT:$((LAST_SPACE-1))}"
            fi
            CLEAN_THOUGHT="$FIRST_LINE\\n$REST"
        fi
        
        # Escape special characters
        CLEAN_THOUGHT_ESC=$(echo "$CLEAN_THOUGHT" | \
            sed "s/'/\\\\'/g" | \
            sed "s/:/\\\:/g" | \
            sed "s/%/\\\%/g" | \
            sed "s/\\n/\\\\n/g")
        
        # Thought appears above activity with narrator styling
        # Position thought just above the activity text (fixed offset)
        THOUGHT_OFFSET=$((BOTTOM_MARGIN + 100))
        
        THOUGHT_FILTER="drawtext=text='💭 $CLEAN_THOUGHT_ESC':fontsize=34:fontcolor=#FFE66D:x=(w-text_w)/2:y=h-th-$THOUGHT_OFFSET:box=1:boxcolor=black@0.90:boxborderw=12:borderw=4:bordercolor=#FFE66D@1.0:line_spacing=12:enable='between(t,$START_TIME,$END_TIME)'"
        
        echo "$THOUGHT_FILTER" >> "$OVERLAY_FILE"
        
        ACTIVITY_INDEX=$((ACTIVITY_INDEX + 1))
    fi
done

# If we have overlays, add them to the video
if [ -s "$OVERLAY_FILE" ] && [ $ACTIVITY_COUNT -gt 0 ]; then
    # Build ffmpeg filter complex
    FILTER_COMPLEX=""
    while IFS= read -r overlay; do
        if [ -n "$FILTER_COMPLEX" ]; then
            FILTER_COMPLEX="$FILTER_COMPLEX,"
        fi
        FILTER_COMPLEX="$FILTER_COMPLEX$overlay"
    done < "$OVERLAY_FILE"
    
    echo "📝 Applying $ACTIVITY_COUNT activity overlays..."
    
    # Use ffmpeg to add overlays with better macOS compatibility
    if ffmpeg -i "$VIDEO_FILE" \
        -vf "$FILTER_COMPLEX" \
        -c:v libx264 \
        -preset medium \
        -crf 23 \
        -c:a aac \
        -b:a 128k \
        -movflags +faststart \
        -pix_fmt yuv420p \
        -y \
        "$OUTPUT_FILE" 2>&1 | tee /tmp/ffmpeg_overlay.log | grep -E "(error|Error|ERROR)" && exit 1 || true; then
        
        FILE_SIZE=$(ls -lh "$OUTPUT_FILE" | awk '{print $5}')
        echo "✅ Video with overlays saved: $OUTPUT_FILE ($FILE_SIZE)"
    else
        echo "⚠️  Overlay failed, converting for compatibility only..."
        ffmpeg -i "$VIDEO_FILE" \
            -c:v libx264 \
            -preset medium \
            -crf 23 \
            -c:a aac \
            -b:a 128k \
            -movflags +faststart \
            -pix_fmt yuv420p \
            -y \
            "$OUTPUT_FILE" 2>&1 | grep -E "(error|Error|ERROR)" || true
        FILE_SIZE=$(ls -lh "$OUTPUT_FILE" | awk '{print $5}')
        echo "✅ Video converted for macOS compatibility: $OUTPUT_FILE ($FILE_SIZE)"
    fi
else
    echo "⚠️  No activities found to overlay"
    # Just convert for compatibility
    ffmpeg -i "$VIDEO_FILE" \
        -c:v libx264 \
        -preset medium \
        -crf 23 \
        -c:a aac \
        -b:a 128k \
        -movflags +faststart \
        -pix_fmt yuv420p \
        -y \
        "$OUTPUT_FILE" 2>&1 | grep -E "(error|Error|ERROR)" || true
    
    FILE_SIZE=$(ls -lh "$OUTPUT_FILE" | awk '{print $5}')
    echo "✅ Video converted for macOS compatibility: $OUTPUT_FILE ($FILE_SIZE)"
fi
