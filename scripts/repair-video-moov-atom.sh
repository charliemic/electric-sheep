#!/bin/bash

# Repair MP4 video files with missing moov atom
# This happens when ADB screenrecord is interrupted before finalization
#
# Usage: ./scripts/repair-video-moov-atom.sh <input_video> [output_video]

set -e

INPUT_VIDEO="$1"
OUTPUT_VIDEO="${2:-${INPUT_VIDEO%.*}_repaired.mp4}"

if [ ! -f "$INPUT_VIDEO" ]; then
    echo "❌ Video file not found: $INPUT_VIDEO"
    exit 1
fi

if ! command -v ffmpeg > /dev/null 2>&1; then
    echo "❌ ffmpeg not found. Install with: brew install ffmpeg"
    exit 1
fi

echo "🔧 Repairing video: $INPUT_VIDEO"
echo "   Output: $OUTPUT_VIDEO"
echo ""

# Check if moov atom is missing
if ! ffprobe -v error "$INPUT_VIDEO" > /dev/null 2>&1; then
    echo "⚠️  Video appears corrupted (moov atom missing)"
    echo "   Attempting repair by re-encoding..."
    
    # Method 1: Try to copy streams (fastest, but may not work if moov is completely missing)
    if ffmpeg -i "$INPUT_VIDEO" -c:v copy -c:a copy -movflags +faststart "$OUTPUT_VIDEO" 2>&1 | grep -q "moov atom not found"; then
        echo "   Copy method failed, trying re-encode..."
        
        # Method 2: Re-encode (slower but more reliable)
        # This reads the raw video data and reconstructs the file structure
        ffmpeg -i "$INPUT_VIDEO" \
            -c:v libx264 \
            -preset medium \
            -crf 23 \
            -c:a aac \
            -b:a 128k \
            -movflags +faststart \
            -pix_fmt yuv420p \
            -y \
            "$OUTPUT_VIDEO" 2>&1 | grep -E "(error|Error|frame|time)" || true
        
        if [ -f "$OUTPUT_VIDEO" ]; then
            FILE_SIZE=$(ls -lh "$OUTPUT_VIDEO" | awk '{print $5}')
            echo "✅ Repaired video saved: $OUTPUT_VIDEO ($FILE_SIZE)"
            
            # Verify it's playable
            if ffprobe -v error "$OUTPUT_VIDEO" > /dev/null 2>&1; then
                echo "✅ Video is now playable!"
            else
                echo "⚠️  Video may still have issues"
            fi
        else
            echo "❌ Repair failed"
            exit 1
        fi
    else
        FILE_SIZE=$(ls -lh "$OUTPUT_VIDEO" | awk '{print $5}')
        echo "✅ Video repaired (copy method): $OUTPUT_VIDEO ($FILE_SIZE)"
    fi
else
    echo "✅ Video appears to be valid (moov atom present)"
    echo "   No repair needed"
fi

