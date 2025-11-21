# Video Management Architecture Evaluation

**Date**: 2025-01-XX  
**Status**: Evaluation Document  
**Purpose**: Evaluate architectures for agent-video synchronization and open-source video production options

## Executive Summary

The current video management approach has synchronization challenges:
- Video recording starts independently of test execution
- Timestamp-based synchronization is error-prone
- Fixed-interval overlays don't match actual video timing
- Limited annotation capabilities with basic ffmpeg

This document evaluates:
1. **Architectures** for precise agent-video synchronization
2. **Open-source tools** for sophisticated video production

## Current State Analysis

### Current Architecture

```
┌─────────────────┐
│  Test Script    │
│  (Bash)         │
└────────┬────────┘
         │
         ├─► Start video recording (adb screenrecord)
         │   └─► Independent process, no sync
         │
         ├─► Run test automation (Kotlin/Appium)
         │   └─► Logs activities with timestamps
         │
         └─► Stop video recording
             └─► Post-process with ffmpeg overlays
                 └─► Fixed 3-second intervals (not frame-accurate)
```

### Problems Identified

1. **Synchronization Issues**:
   - Video recording starts before test execution begins
   - No shared clock between video and test agent
   - Timestamp parsing is fragile (string matching)
   - Fixed intervals don't match actual action timing

2. **Video Production Limitations**:
   - Basic ffmpeg drawtext filters
   - No frame-accurate timing
   - Limited annotation capabilities
   - Hard to debug overlay issues
   - No visual indicators for actions (clicks, swipes)

3. **Maintenance Challenges**:
   - Complex bash scripts for overlay generation
   - Hard to test overlay logic
   - Difficult to add new annotation types

## Part A: Architectures for Agent-Video Synchronization

### Architecture Option 1: Frame-Accurate Timestamp Logging

**Approach**: Log precise timestamps at action execution, correlate with video frames.

#### Design

```
┌─────────────────────────────────────────────────────────┐
│  Test Execution Layer                                    │
│                                                          │
│  1. Start video recording (record start time T0)         │
│  2. For each action:                                    │
│     - Log: {timestamp: T, action: "tap", target: "..."} │
│     - Timestamp = System.currentTimeMillis() - T0       │
│  3. Stop video recording                                │
│  4. Export action log as JSON                          │
└─────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────┐
│  Video Annotation Layer (Python)                        │
│                                                          │
│  1. Load video, extract frame rate                      │
│  2. Load action log JSON                                │
│  3. For each action:                                    │
│     - Convert timestamp to frame number                 │
│     - frame = (timestamp_ms / 1000) * fps              │
│     - Add overlay at precise frame                      │
│  4. Render annotated video                              │
└─────────────────────────────────────────────────────────┘
```

#### Implementation Strategy

**Test Framework Changes**:
```kotlin
// In ActionExecutor.kt
class ActionExecutor(
    private val driver: AndroidDriver,
    private val screenshotDir: File,
    private val videoStartTime: Long = System.currentTimeMillis() // Shared start time
) {
    private val actionLog = mutableListOf<ActionLogEntry>()
    
    suspend fun execute(action: HumanAction): ActionResult {
        val timestamp = System.currentTimeMillis() - videoStartTime
        val result = when (action) {
            // ... execute action
        }
        
        // Log action with precise timestamp
        actionLog.add(ActionLogEntry(
            timestampMs = timestamp,
            action = action,
            result = result
        ))
        
        return result
    }
    
    fun exportActionLog(): File {
        val logFile = File(screenshotDir, "action_log.json")
        // Export as JSON with timestamps
        return logFile
    }
}
```

**Video Annotation Script (Python)**:
```python
import json
from moviepy.editor import VideoFileClip, TextClip, CompositeVideoClip
from datetime import timedelta

def annotate_video(video_path, action_log_path, output_path):
    # Load video
    video = VideoFileClip(video_path)
    fps = video.fps
    
    # Load action log
    with open(action_log_path) as f:
        actions = json.load(f)
    
    clips = [video]
    
    for action in actions:
        # Convert timestamp to seconds
        timestamp_sec = action['timestampMs'] / 1000.0
        
        # Create text overlay
        txt_clip = TextClip(
            action['description'],
            fontsize=42,
            color='white',
            bg_color='black',
            size=(video.w * 0.8, None)
        ).set_position(('center', 'bottom')).set_duration(2.0).set_start(timestamp_sec)
        
        clips.append(txt_clip)
    
    # Composite all clips
    final = CompositeVideoClip(clips)
    final.write_videofile(output_path, fps=fps)
```

#### Pros
- ✅ Frame-accurate synchronization
- ✅ Precise timestamp correlation
- ✅ Easy to add new annotation types
- ✅ Testable (JSON log can be validated)

#### Cons
- ⚠️ Requires shared clock (video start time)
- ⚠️ Still requires post-processing
- ⚠️ Python dependency for video processing

#### Recommendation
**Good for**: Precise synchronization, frame-accurate annotations

---

### Architecture Option 2: Real-Time Video Annotation API

**Approach**: Test framework sends annotation commands to a video annotation service in real-time.

#### Design

```
┌─────────────────────────────────────────────────────────┐
│  Test Execution Layer                                    │
│                                                          │
│  1. Connect to Video Annotation Service (HTTP/WebSocket)│
│  2. Start video recording                               │
│  3. For each action:                                    │
│     - Send annotation command:                           │
│       POST /annotate {                                   │
│         "timestamp": <relative_ms>,                     │
│         "type": "overlay",                               │
│         "text": "Tapping on Sign Up button"             │
│       }                                                  │
│  4. Stop video recording                                │
│  5. Request final video render                          │
└─────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────┐
│  Video Annotation Service (Python/Node.js)               │
│                                                          │
│  1. Maintain annotation queue                           │
│  2. Correlate annotations with video frames              │
│  3. Render final video with all annotations             │
└─────────────────────────────────────────────────────────┘
```

#### Implementation Strategy

**Video Annotation Service (Python)**:
```python
from flask import Flask, request, jsonify
from moviepy.editor import VideoFileClip, TextClip, CompositeVideoClip
import threading
import queue

app = Flask(__name__)
annotations = queue.Queue()
video_path = None

@app.route('/start', methods=['POST'])
def start_recording():
    global video_path
    video_path = request.json['video_path']
    return jsonify({'status': 'started'})

@app.route('/annotate', methods=['POST'])
def add_annotation():
    annotation = request.json
    annotations.put(annotation)
    return jsonify({'status': 'queued'})

@app.route('/render', methods=['POST'])
def render_video():
    # Process all annotations and render video
    # ...
    return jsonify({'output_path': '...'})
```

#### Pros
- ✅ Real-time annotation (no post-processing delay)
- ✅ Can add visual indicators (click markers, swipe paths)
- ✅ Service can handle multiple test runs
- ✅ Decoupled from test framework

#### Cons
- ⚠️ More complex architecture
- ⚠️ Requires service to be running
- ⚠️ Network latency considerations
- ⚠️ Service must handle video file access

#### Recommendation
**Good for**: Real-time feedback, multiple concurrent tests, advanced annotations

---

### Architecture Option 3: Integrated Video Recording with Action Markers

**Approach**: Embed action markers directly into video metadata or use frame markers.

#### Design

```
┌─────────────────────────────────────────────────────────┐
│  Test Execution Layer                                    │
│                                                          │
│  1. Start video recording with metadata stream          │
│  2. For each action:                                    │
│     - Write marker to metadata file                     │
│     - Optionally: Inject frame marker (colored frame)   │
│  3. Stop video recording                                │
│  4. Video + metadata file available for annotation      │
└─────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────┐
│  Video Annotation Layer                                  │
│                                                          │
│  1. Load video                                          │
│  2. Load metadata file (or detect frame markers)        │
│  3. Correlate markers with video frames                 │
│  4. Add annotations at marker positions                 │
└─────────────────────────────────────────────────────────┘
```

#### Implementation Strategy

**Frame Marker Approach**:
```kotlin
// Inject colored frame markers
suspend fun execute(action: HumanAction): ActionResult {
    // Before action: inject red frame (1 frame)
    injectFrameMarker(Color.RED)
    
    val result = when (action) {
        // ... execute action
    }
    
    // After action: inject green frame (1 frame)
    injectFrameMarker(Color.GREEN)
    
    return result
}

private fun injectFrameMarker(color: Color) {
    // Use ADB to inject a colored overlay frame
    // Or use Appium to take screenshot and inject
}
```

**Metadata File Approach**:
```kotlin
// Write metadata alongside video
class ActionMetadataLogger(private val metadataFile: File) {
    fun logAction(action: HumanAction, frameNumber: Long) {
        metadataFile.appendText("""
            {
                "frame": $frameNumber,
                "action": "${action::class.simpleName}",
                "description": "${action.description}"
            }
        """.trimIndent() + "\n")
    }
}
```

#### Pros
- ✅ Frame markers are visible in raw video
- ✅ Metadata file is human-readable
- ✅ Can detect markers even if timestamps drift
- ✅ Works with any video player

#### Cons
- ⚠️ Frame markers pollute video (need to remove in post)
- ⚠️ Metadata file must be kept in sync
- ⚠️ More complex marker injection

#### Recommendation
**Good for**: Debugging, manual inspection, robust synchronization

---

### Architecture Option 4: Hybrid Approach (Recommended)

**Approach**: Combine frame-accurate timestamp logging with real-time annotation preview.

#### Design

```
┌─────────────────────────────────────────────────────────┐
│  Test Execution Layer                                    │
│                                                          │
│  1. Start video recording (record T0)                    │
│  2. Initialize action logger (JSON)                    │
│  3. For each action:                                    │
│     - Log: {timestamp: T, action: {...}, screenshot}    │
│     - Optionally: Send to annotation service (preview)  │
│  4. Stop video recording                                │
│  5. Export action log JSON                              │
└─────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────┐
│  Video Annotation Pipeline (Python)                     │
│                                                          │
│  1. Load video + action log JSON                        │
│  2. Frame-accurate annotation generation                │
│  3. Add visual indicators (click markers, etc.)         │
│  4. Render final video                                  │
└─────────────────────────────────────────────────────────┘
```

#### Implementation Strategy

**Action Logger (Kotlin)**:
```kotlin
data class ActionLogEntry(
    val timestampMs: Long,           // Relative to video start
    val action: String,               // Action type
    val description: String,          // Human-readable
    val target: String?,             // Element description
    val screenshot: String?,          // Path to screenshot
    val coordinates: Pair<Int, Int>? // Click/swipe coordinates
)

class ActionLogger(
    private val videoStartTime: Long,
    private val logFile: File
) {
    private val actions = mutableListOf<ActionLogEntry>()
    
    fun logAction(
        action: HumanAction,
        result: ActionResult,
        coordinates: Pair<Int, Int>? = null
    ) {
        val timestamp = System.currentTimeMillis() - videoStartTime
        actions.add(ActionLogEntry(
            timestampMs = timestamp,
            action = action::class.simpleName ?: "Unknown",
            description = generateDescription(action),
            target = extractTarget(action),
            screenshot = result.screenshot?.absolutePath,
            coordinates = coordinates
        ))
    }
    
    fun export(): File {
        val json = Json.encodeToString(actions)
        logFile.writeText(json)
        return logFile
    }
}
```

**Video Annotation Pipeline (Python)**:
```python
import json
from moviepy.editor import VideoFileClip, TextClip, ImageClip, CompositeVideoClip
from moviepy.video.fx.all import resize

def annotate_video(video_path, action_log_path, output_path):
    # Load video
    video = VideoFileClip(video_path)
    fps = video.fps
    
    # Load action log
    with open(action_log_path) as f:
        actions = json.load(f)
    
    clips = [video]
    
    for action in actions:
        timestamp_sec = action['timestampMs'] / 1000.0
        
        # Text overlay
        txt_clip = TextClip(
            action['description'],
            fontsize=42,
            color='white',
            bg_color='black@0.9',
            size=(video.w * 0.8, None),
            method='caption'
        ).set_position(('center', 'bottom')).set_duration(2.0).set_start(timestamp_sec)
        
        clips.append(txt_clip)
        
        # Visual indicator (click marker, swipe path)
        if action.get('coordinates'):
            x, y = action['coordinates']
            marker = create_click_marker(x, y, video.w, video.h)
            marker = marker.set_start(timestamp_sec).set_duration(0.5)
            clips.append(marker)
    
    # Composite
    final = CompositeVideoClip(clips)
    final.write_videofile(output_path, fps=fps, codec='libx264')
```

#### Pros
- ✅ Frame-accurate synchronization
- ✅ Rich annotation capabilities
- ✅ Visual indicators for actions
- ✅ Testable and maintainable
- ✅ Can preview annotations in real-time (optional)

#### Cons
- ⚠️ Requires Python for video processing
- ⚠️ Post-processing step required

#### Recommendation
**⭐ RECOMMENDED**: Best balance of accuracy, capabilities, and maintainability

---

## Part B: Open-Source Video Production Options

### Option 1: MoviePy (Python)

**Description**: Python library for video editing, built on ffmpeg.

#### Features
- ✅ Frame-accurate editing
- ✅ Text overlays with precise timing
- ✅ Image compositing
- ✅ Visual effects (fade, zoom, etc.)
- ✅ Programmatic API (perfect for automation)
- ✅ Active community and documentation

#### Example Usage
```python
from moviepy.editor import VideoFileClip, TextClip, CompositeVideoClip

video = VideoFileClip("input.mp4")

# Add text overlay
txt = TextClip("Tapping Sign Up", fontsize=42, color='white')
txt = txt.set_position(('center', 'bottom')).set_duration(2).set_start(5.0)

# Composite
final = CompositeVideoClip([video, txt])
final.write_videofile("output.mp4")
```

#### Pros
- ✅ Python (easy integration with test framework)
- ✅ Frame-accurate timing
- ✅ Rich feature set
- ✅ Good documentation

#### Cons
- ⚠️ Can be slow for long videos
- ⚠️ Memory-intensive for high-resolution videos
- ⚠️ Requires ffmpeg installed

#### Recommendation
**⭐ RECOMMENDED**: Best for programmatic video annotation

---

### Option 2: FFmpeg (Direct)

**Description**: Command-line tool for video processing (current approach).

#### Features
- ✅ Very fast
- ✅ Low-level control
- ✅ Widely supported
- ✅ No dependencies

#### Current Limitations
- ❌ Complex filter syntax
- ❌ Hard to debug
- ❌ Limited programmatic control
- ❌ Fixed-interval overlays (not frame-accurate)

#### Improvement Strategy
Use ffmpeg-python wrapper for better programmatic control:
```python
import ffmpeg

# More maintainable than bash scripts
stream = ffmpeg.input('input.mp4')
stream = ffmpeg.drawtext(
    stream,
    text='Action description',
    x='(w-text_w)/2',
    y='h-th-60',
    fontsize=42,
    fontcolor='white',
    box=1,
    boxcolor='black@0.9',
    enable=f'between(t,{start_time},{end_time})'
)
stream.output('output.mp4').run()
```

#### Recommendation
**Good for**: Performance-critical scenarios, but use ffmpeg-python wrapper

---

### Option 3: OpenCV (Python)

**Description**: Computer vision library with video processing capabilities.

#### Features
- ✅ Frame-by-frame processing
- ✅ Image manipulation
- ✅ Drawing primitives (circles, lines, text)
- ✅ Very fast

#### Example Usage
```python
import cv2

cap = cv2.VideoCapture('input.mp4')
fps = cap.get(cv2.CAP_PROP_FPS)
fourcc = cv2.VideoWriter_fourcc(*'mp4v')
out = cv2.VideoWriter('output.mp4', fourcc, fps, (width, height))

frame_num = 0
while cap.isOpened():
    ret, frame = cap.read()
    if not ret:
        break
    
    # Add annotation at specific frame
    if frame_num == target_frame:
        cv2.putText(frame, 'Action', (x, y), cv2.FONT_HERSHEY_SIMPLEX, 1, (255, 255, 255), 2)
    
    out.write(frame)
    frame_num += 1
```

#### Pros
- ✅ Very fast frame-by-frame processing
- ✅ Precise control
- ✅ Good for custom visualizations

#### Cons
- ⚠️ Lower-level API (more code)
- ⚠️ No built-in text formatting
- ⚠️ Requires more manual work

#### Recommendation
**Good for**: Custom visualizations, performance-critical frame processing

---

### Option 4: ImageMagick + FFmpeg

**Description**: Combine ImageMagick for image annotation with FFmpeg for video.

#### Features
- ✅ ImageMagick: Powerful image annotation
- ✅ FFmpeg: Fast video processing
- ✅ Can generate annotation frames, then composite

#### Workflow
```python
# 1. Generate annotation frames with ImageMagick
# 2. Composite frames with FFmpeg
```

#### Pros
- ✅ Very flexible
- ✅ Can create complex annotations

#### Cons
- ⚠️ More complex workflow
- ⚠️ Two tools to manage
- ⚠️ Slower (generate frames, then composite)

#### Recommendation
**Good for**: Complex annotations, but MoviePy is simpler

---

### Option 5: Manim (Mathematical Animation Engine)

**Description**: Python library for creating mathematical animations, but can be used for video annotation.

#### Features
- ✅ High-quality animations
- ✅ Professional-looking output
- ✅ Scene-based composition

#### Pros
- ✅ Very polished output
- ✅ Good for presentations

#### Cons
- ⚠️ Overkill for simple annotations
- ⚠️ Steeper learning curve
- ⚠️ Slower rendering

#### Recommendation
**Good for**: Presentation-quality videos, but overkill for test annotations

---

## Recommended Architecture

### Hybrid Approach with MoviePy

**Architecture**:
1. **Test Framework** (Kotlin): Log actions with precise timestamps
2. **Action Logger**: Export JSON log with timestamps, screenshots, coordinates
3. **Video Annotation Pipeline** (Python + MoviePy): Frame-accurate annotation

### Implementation Plan

#### Phase 1: Action Logging (Kotlin)
- Add `ActionLogger` class to test framework
- Log all actions with timestamps relative to video start
- Export JSON log file

#### Phase 2: Video Annotation Pipeline (Python)
- Create Python script using MoviePy
- Load video + action log JSON
- Generate frame-accurate annotations
- Add visual indicators (click markers, swipe paths)

#### Phase 3: Integration
- Update test scripts to use new pipeline
- Add configuration for annotation styles
- Add visual indicators for actions

### Example Implementation

**Action Logger (Kotlin)**:
```kotlin
// See Architecture Option 4 above
```

**Video Annotation Script (Python)**:
```python
#!/usr/bin/env python3
"""
Video annotation pipeline for test automation videos.
Usage: python annotate_video.py <video_path> <action_log_path> <output_path>
"""

import json
import sys
from moviepy.editor import VideoFileClip, TextClip, CompositeVideoClip, Circle
from moviepy.video.fx.all import resize

def create_click_marker(x, y, video_w, video_h, duration=0.5):
    """Create a visual click marker (ripple effect)."""
    # Scale coordinates (assuming video is device resolution)
    scale_x = video_w / 1080  # Adjust based on device
    scale_y = video_h / 1920
    
    marker_x = int(x * scale_x)
    marker_y = int(y * scale_y)
    
    # Create ripple circles
    circles = []
    for i in range(3):
        radius = 20 + (i * 15)
        opacity = 1.0 - (i * 0.3)
        circle = Circle(
            center=(marker_x, marker_y),
            radius=radius,
            color='yellow',
            opacity=opacity
        ).set_duration(duration).set_start(0)
        circles.append(circle)
    
    return CompositeVideoClip(circles)

def annotate_video(video_path, action_log_path, output_path):
    """Annotate video with action log."""
    print(f"Loading video: {video_path}")
    video = VideoFileClip(video_path)
    fps = video.fps
    print(f"Video FPS: {fps}, Duration: {video.duration}s")
    
    print(f"Loading action log: {action_log_path}")
    with open(action_log_path) as f:
        actions = json.load(f)
    print(f"Found {len(actions)} actions")
    
    clips = [video]
    
    for i, action in enumerate(actions):
        timestamp_sec = action['timestampMs'] / 1000.0
        print(f"  Action {i+1}/{len(actions)}: {action['description']} at {timestamp_sec:.2f}s")
        
        # Text overlay
        txt_clip = TextClip(
            action['description'],
            fontsize=42,
            color='white',
            bg_color='black@0.9',
            size=(int(video.w * 0.8), None),
            method='caption',
            align='center'
        ).set_position(('center', 'bottom')).set_duration(2.0).set_start(timestamp_sec)
        
        clips.append(txt_clip)
        
        # Visual indicator
        if action.get('coordinates'):
            x, y = action['coordinates']
            marker = create_click_marker(x, y, video.w, video.h)
            marker = marker.set_start(timestamp_sec).set_duration(0.5)
            clips.append(marker)
    
    print("Compositing video...")
    final = CompositeVideoClip(clips)
    
    print(f"Writing video to: {output_path}")
    final.write_videofile(
        output_path,
        fps=fps,
        codec='libx264',
        preset='medium',
        audio_codec='aac'
    )
    
    print("✅ Video annotation complete!")

if __name__ == '__main__':
    if len(sys.argv) != 4:
        print("Usage: python annotate_video.py <video_path> <action_log_path> <output_path>")
        sys.exit(1)
    
    annotate_video(sys.argv[1], sys.argv[2], sys.argv[3])
```

## Comparison Matrix

| Architecture | Accuracy | Complexity | Maintainability | Features |
|-------------|----------|------------|-----------------|----------|
| Frame-Accurate Timestamps | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐ |
| Real-Time API | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐ |
| Frame Markers | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐ |
| **Hybrid (Recommended)** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |

| Video Tool | Ease of Use | Performance | Features | Programmatic |
|-----------|-------------|------------|----------|--------------|
| **MoviePy** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| FFmpeg Direct | ⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐ |
| OpenCV | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐ |
| ImageMagick+FFmpeg | ⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐ |

## Next Steps

1. **Implement Action Logger** in test framework (Kotlin)
2. **Create Video Annotation Pipeline** (Python + MoviePy)
3. **Add Visual Indicators** (click markers, swipe paths)
4. **Update Test Scripts** to use new pipeline
5. **Test and Refine** annotation accuracy

## References

- [MoviePy Documentation](https://zulko.github.io/moviepy/)
- [FFmpeg Documentation](https://ffmpeg.org/documentation.html)
- [OpenCV Video Processing](https://docs.opencv.org/4.x/dd/d43/tutorial_py_video_display.html)

