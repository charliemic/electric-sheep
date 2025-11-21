# Video Annotation Quick Start

This guide shows you how to use the new frame-accurate video annotation system for test automation.

## Overview

The new video annotation system provides:
- ✅ **Frame-accurate synchronization** between test actions and video
- ✅ **Automatic annotation** of test videos with action descriptions
- ✅ **Visual indicators** for tap/swipe actions
- ✅ **Easy integration** with existing test scripts

## Prerequisites

1. **Python 3.11+** installed (see [Python Setup Guide](../development/PYTHON_SETUP.md))
2. **Python dependencies** installed:
   ```bash
   pip install -r requirements.txt
   ```
3. **FFmpeg** installed (required by MoviePy):
   ```bash
   # macOS
   brew install ffmpeg
   
   # Linux
   sudo apt-get install ffmpeg
   ```

## How It Works

1. **Test Execution**: Test framework logs all actions with precise timestamps
2. **Action Log**: JSON file created with action timestamps, descriptions, coordinates
3. **Video Annotation**: Python script adds annotations to video at precise timestamps

## Usage

### Automatic (Recommended)

The test script automatically annotates videos:

```bash
# Run test with video recording
./scripts/run-persona-test-with-video.sh signup-and-mood-entry

# Video is automatically annotated if:
# - Python 3.11+ is installed
# - Dependencies are installed (pip install -r requirements.txt)
# - Action log is generated (test-automation/test-results/action_log.json)
```

### Manual Annotation

If you want to annotate a video manually:

```bash
python scripts/annotate-video.py \
    /tmp/test_results/video.mp4 \
    test-automation/test-results/action_log.json \
    /tmp/test_results/video_annotated.mp4
```

## Action Log Format

The action log (`action_log.json`) contains:

```json
[
  {
    "timestampMs": 1234,
    "action": "Tap",
    "description": "Tapping on Sign Up button",
    "target": "Sign Up button",
    "screenshot": "test-results/screenshots/screenshot_1234.png",
    "coordinates": [540, 960],
    "success": true,
    "error": null
  },
  {
    "timestampMs": 5678,
    "action": "TypeText",
    "description": "Typing into email field: user@example.com",
    "target": "Email address input field",
    "screenshot": "test-results/screenshots/screenshot_5678.png",
    "coordinates": null,
    "success": true,
    "error": null
  }
]
```

## Output

The annotated video includes:
- **Text overlays** at the bottom showing action descriptions
- **Visual indicators** (click markers) for tap actions
- **Frame-accurate timing** - annotations appear at the exact moment actions occur

## Troubleshooting

### Python Not Found

```bash
# Check Python version
python3 --version  # Should be 3.11+

# If using pyenv
pyenv local 3.11.0
python3 --version
```

### MoviePy Not Installed

```bash
pip install -r requirements.txt
```

### FFmpeg Not Found

```bash
# macOS
brew install ffmpeg

# Linux
sudo apt-get install ffmpeg

# Verify
ffmpeg -version
```

### Action Log Not Found

The action log is created automatically during test execution. If it's missing:
1. Check that the test completed successfully
2. Look for `test-automation/test-results/action_log.json`
3. Verify ActionLogger is initialized in `Main.kt`

### Video Annotation Fails

Check the error message:
- **"Video file not found"**: Verify video path is correct
- **"Action log file not found"**: Verify action log path is correct
- **"Error loading video"**: Video file might be corrupted or in unsupported format
- **"Error compositing video"**: Check FFmpeg installation and video format

## Advanced Usage

### Custom Annotation Styles

Edit `scripts/annotate-video.py` to customize:
- Text overlay position and styling
- Click marker appearance
- Annotation duration

### Multiple Videos

To annotate multiple videos:

```bash
for video in /tmp/test_results/*.mp4; do
    python scripts/annotate-video.py \
        "$video" \
        test-automation/test-results/action_log.json \
        "${video%.*}_annotated.mp4"
done
```

## Integration with CI/CD

For CI/CD, ensure:
1. Python 3.11+ is available in CI environment
2. Dependencies are installed: `pip install -r requirements.txt`
3. FFmpeg is installed in CI environment
4. Action log is available after test execution

Example GitHub Actions step:
```yaml
- name: Annotate test video
  run: |
    pip install -r requirements.txt
    python scripts/annotate-video.py \
      test-results/video.mp4 \
      test-automation/test-results/action_log.json \
      test-results/video_annotated.mp4
```

## See Also

- [Video Management Evaluation](VIDEO_MANAGEMENT_EVALUATION.md) - Architecture evaluation
- [Video Management Summary](VIDEO_MANAGEMENT_SUMMARY.md) - Quick reference
- [Python Setup Guide](../development/PYTHON_SETUP.md) - Python installation

