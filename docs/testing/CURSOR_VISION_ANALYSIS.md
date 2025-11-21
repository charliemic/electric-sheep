# Using Cursor Vision for Screen Analysis

## Overview

Instead of using external AI vision APIs, we use **Cursor's built-in vision capabilities** to analyze screenshots. This approach:
- ✅ No external API costs
- ✅ Uses Cursor's powerful vision model
- ✅ Interactive analysis workflow
- ✅ Easy to review and iterate

## How It Works

1. **Test Completes**: Screenshots are saved to `test-results/screenshots/`
2. **Prompt Files Created**: Analysis prompt files are generated alongside screenshots
3. **Cursor Analysis**: Open screenshots in Cursor and use vision to analyze
4. **Observations**: Review findings and add to test reports

## Screenshot Organization

After each test run, screenshots are organized as:

```
test-results/screenshots/
├── screenshot_<timestamp>.png          # Action screenshots
├── evaluation_<timestamp>.png          # Final screen evaluation
├── monitor_<timestamp>.png             # Continuous monitoring screenshots
└── <screenshot>_cursor_prompt.txt     # Analysis prompts for Cursor
```

## Using Cursor to Analyze Screenshots

### Method 1: Direct Vision Analysis

1. **Open Screenshot**: Open any screenshot file in Cursor
2. **Ask Cursor**: Use Cursor's chat to analyze:
   ```
   Analyze this mobile app screenshot. Look for:
   - Error messages
   - Blocking dialogs
   - Missing expected elements
   - UI issues a user would report
   ```

### Method 2: Using Prompt Files

1. **Open Prompt File**: Open the `*_cursor_prompt.txt` file
2. **Open Screenshot**: Open the corresponding screenshot
3. **Copy Prompt**: Copy the prompt content
4. **Ask Cursor**: Paste the prompt in Cursor chat with the screenshot visible

### Method 3: Batch Analysis

For multiple screenshots:

1. **Open Screenshot Directory**: Open `test-results/screenshots/` in Cursor
2. **Select Screenshots**: Select multiple screenshots
3. **Ask Cursor**: 
   ```
   Analyze these test screenshots. For each one, identify:
   - Any errors or blocking issues
   - Whether the expected state is visible
   - Any UI problems a user would notice
   ```

## Example Workflow

### After Test Run

```bash
# Run test
./gradlew run --args="--task 'Sign up and add mood value'"

# Test completes, screenshots saved
# Open in Cursor:
open test-automation/test-results/screenshots/
```

### In Cursor

1. **Open Final Screenshot**: `evaluation_<timestamp>.png`
2. **Ask Cursor**:
   ```
   Analyze this screenshot. The test was: "Sign up and add mood value"
   Expected state: Mood Management screen with Mood History visible
   
   Identify:
   - Any error messages
   - Blocking elements
   - Missing expected elements
   - Any issues a user would report
   ```

3. **Review Observations**: Cursor will provide detailed analysis

## Integration with Test Reports

### Manual Observation Entry

After Cursor analysis, you can manually add observations:

1. **Cursor Analysis**: Get observations from Cursor
2. **Add to Report**: Update test results with observations
3. **Document Issues**: Track blocking elements and errors

### Automated Workflow (Future)

Potential future enhancement:
- Cursor MCP integration for automated analysis
- Direct API access to Cursor's vision (if available)
- Batch processing of screenshots

## Screenshot Naming

Screenshots are named with timestamps for easy identification:
- `screenshot_1763663315981.png` - Action screenshot
- `evaluation_1763663315981.png` - Final evaluation screenshot
- `monitor_1763663315981.png` - Monitoring screenshot

The timestamp corresponds to when the screenshot was captured.

## Best Practices

### 1. Focus on Final Screenshot

The `evaluation_*.png` screenshot is the most important - it's the final state after test completion.

### 2. Check for Blocking Elements

Ask Cursor specifically:
```
Are there any dialogs, popups, or overlays blocking interaction?
```

### 3. Verify Expected State

```
Does this screen match the expected state: [expected state]?
Are these elements visible: [expected elements]?
```

### 4. Look for User-Reportable Issues

```
What issues would a user report about this screen?
Are there any error messages, warnings, or confusing UI elements?
```

## Example Cursor Prompts

### Error Detection
```
Analyze this screenshot for error messages. Look for:
- Red text indicating errors
- Error icons or symbols
- Error dialogs or popups
- Any text containing "error", "failed", "invalid", etc.
```

### Blocking Elements
```
Are there any blocking elements on this screen?
Look for:
- Dialogs or popups
- Overlays that prevent interaction
- Modal windows
- Confirmation dialogs
```

### State Verification
```
Expected state: Mood Management screen
Expected elements: "Mood History" text, "Save Mood" button

Does this screenshot show the expected state?
Are the expected elements visible?
```

## Advantages of Cursor Vision

1. **No API Costs**: Free to use (part of Cursor subscription)
2. **Interactive**: Can ask follow-up questions
3. **Contextual**: Cursor understands the codebase context
4. **Iterative**: Easy to refine analysis with additional questions
5. **Visual**: Can see multiple screenshots side-by-side

## Limitations

1. **Manual Step**: Requires opening Cursor and analyzing
2. **Not Automated**: Can't be fully automated in test runs
3. **Time**: Takes a few minutes to analyze each screenshot

## Future Enhancements

Potential improvements:
- **Cursor MCP Integration**: If Cursor exposes MCP for vision
- **Batch Analysis**: Analyze multiple screenshots at once
- **Template Prompts**: Pre-defined prompts for common scenarios
- **Auto-Report**: Generate observation reports from Cursor analysis

## Quick Reference

```bash
# After test run, open screenshots
open test-automation/test-results/screenshots/

# In Cursor, ask:
"Analyze this screenshot for errors, blocking elements, and UI issues"
```

The screenshot path and analysis prompt are included in the observation output, making it easy to find and analyze screenshots in Cursor.



