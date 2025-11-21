# Video Management Architecture - Quick Reference

**Date**: 2025-01-XX  
**Status**: Summary of Evaluation

## Problem Statement

Current video management has synchronization issues:
- Video recording starts independently of test execution
- Timestamp-based synchronization is error-prone
- Fixed-interval overlays don't match actual video timing
- Limited annotation capabilities

## Recommended Solution

### Architecture: Hybrid Approach with Frame-Accurate Timestamps

**Components**:
1. **Action Logger** (Kotlin): Logs actions with precise timestamps relative to video start
2. **Video Annotation Pipeline** (Python + MoviePy): Frame-accurate annotation generation

### Key Design Decisions

1. **Synchronization**: Use shared video start time (`T0`) and log relative timestamps
2. **Annotation Tool**: MoviePy (Python) for programmatic, frame-accurate video editing
3. **Data Format**: JSON action log with timestamps, descriptions, coordinates, screenshots
4. **Visual Indicators**: Add click markers, swipe paths for better visualization

## Implementation Overview

### 1. Action Logger (Kotlin)

```kotlin
class ActionLogger(
    private val videoStartTime: Long,
    private val logFile: File
) {
    fun logAction(action: HumanAction, result: ActionResult, coordinates: Pair<Int, Int>?) {
        val timestamp = System.currentTimeMillis() - videoStartTime
        // Log to JSON
    }
    
    fun export(): File {
        // Export JSON log
    }
}
```

### 2. Video Annotation Pipeline (Python)

```python
# annotate_video.py
from moviepy.editor import VideoFileClip, TextClip, CompositeVideoClip

def annotate_video(video_path, action_log_path, output_path):
    video = VideoFileClip(video_path)
    actions = json.load(open(action_log_path))
    
    clips = [video]
    for action in actions:
        timestamp_sec = action['timestampMs'] / 1000.0
        # Add text overlay at precise timestamp
        # Add visual indicators (click markers, etc.)
    
    final = CompositeVideoClip(clips)
    final.write_videofile(output_path)
```

## Architecture Comparison

| Approach | Accuracy | Complexity | Recommendation |
|----------|----------|------------|----------------|
| Frame-Accurate Timestamps | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ✅ **Recommended** |
| Real-Time API | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | For advanced use cases |
| Frame Markers | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | For debugging |

## Tool Comparison

| Tool | Best For | Recommendation |
|------|----------|----------------|
| **MoviePy** | Programmatic annotation | ✅ **Recommended** |
| FFmpeg Direct | Performance | Use with ffmpeg-python wrapper |
| OpenCV | Custom visualizations | For specific needs |

## Next Steps

1. ✅ Implement `ActionLogger` in test framework
2. ✅ Create Python annotation pipeline with MoviePy
3. ✅ Add visual indicators (click markers, swipe paths)
4. ✅ Update test scripts to use new pipeline
5. ✅ Test and refine

## Files to Create/Modify

### New Files
- `test-automation/src/main/kotlin/.../ActionLogger.kt` - Action logging
- `scripts/annotate-video.py` - Video annotation pipeline
- `requirements.txt` - Python dependencies (moviepy, etc.)

### Modified Files
- `test-automation/src/main/kotlin/.../ActionExecutor.kt` - Add action logging
- `test-automation/src/main/kotlin/.../Main.kt` - Initialize action logger
- `scripts/run-persona-test-with-video.sh` - Use new annotation pipeline

## Dependencies

### Python
```bash
pip install moviepy pillow
```

### Kotlin
- JSON serialization library (already in use)

## Usage Example

```bash
# Run test (generates video + action log)
./scripts/run-persona-test-with-video.sh signup-and-mood-entry

# Annotate video
python scripts/annotate-video.py \
    /tmp/persona_test_results/.../test_journey.mp4 \
    /tmp/persona_test_results/.../action_log.json \
    /tmp/persona_test_results/.../test_journey_annotated.mp4
```

## Benefits

- ✅ **Frame-accurate synchronization**: Precise timestamp correlation
- ✅ **Maintainable**: Python script easier to maintain than bash
- ✅ **Extensible**: Easy to add new annotation types
- ✅ **Visual indicators**: Click markers, swipe paths
- ✅ **Testable**: JSON log can be validated independently

## See Also

- `docs/testing/VIDEO_MANAGEMENT_EVALUATION.md` - Full evaluation document

