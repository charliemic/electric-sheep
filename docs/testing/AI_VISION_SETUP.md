# AI Vision Setup for Screen Evaluation

## Overview

The screen evaluation system uses **Cursor's built-in vision capabilities** to analyze screenshots. This approach:
- ✅ No external API costs
- ✅ Uses Cursor's powerful vision model
- ✅ Interactive analysis workflow
- ✅ Easy to review and iterate

## How It Works

1. **Test Completes**: Screenshots are saved to `test-results/screenshots/`
2. **Prompt Files Created**: Analysis prompt files are generated alongside screenshots
3. **Cursor Analysis**: Open screenshots in Cursor and use vision to analyze
4. **Observations**: Review findings and add to test reports

**See `CURSOR_VISION_ANALYSIS.md` for detailed instructions on using Cursor to analyze screenshots.**

## How It Works

1. **Test Completes**: Final screenshot is captured
2. **AI Analysis**: Screenshot is sent to OpenAI Vision API with a detailed prompt
3. **Visual Detection**: AI analyzes the screenshot and detects:
   - Error messages (red text, error icons)
   - Warning dialogs
   - Blocking elements (popups, overlays)
   - Missing expected elements
   - Success indicators
   - Unexpected states
4. **Observations**: AI response is parsed into structured observations
5. **Report**: Observations are displayed with severity levels

## Integration

The system automatically uses AI vision if:
- `OPENAI_API_KEY` environment variable is set
- API key is passed via `--ai-api-key` argument

### Example Usage

```bash
# With environment variable
export OPENAI_API_KEY="sk-..."
./gradlew run --args="--task 'Sign up and add mood value'"

# Or with command-line argument
./gradlew run --args="--task 'Sign up' --ai-api-key sk-..."
```

## Fallback Behavior

If AI vision is not available:
- System falls back to basic analysis
- Still produces observations (though less detailed)
- Test continues normally
- Observation indicates "AI vision not available"

## Using with Cursor

While Cursor has built-in vision capabilities, the test automation framework needs to call the API directly. However, you can:

1. **Use OpenAI API**: Same API that Cursor uses under the hood
2. **Review Screenshots**: Screenshots are saved to `test-results/screenshots/` - you can review them in Cursor
3. **Manual Analysis**: For debugging, you can open screenshots in Cursor and ask it to analyze them

### Screenshot Locations

- **Action Screenshots**: `test-results/screenshots/screenshot_*.png`
- **Evaluation Screenshots**: `test-results/screenshots/evaluation_*.png`
- **Monitor Screenshots**: `test-results/screenshots/monitor_*.png`

## Future Enhancements

Potential improvements:
- **Local Models**: Use local vision models (e.g., LLaVA) for cost savings
- **Batch Analysis**: Analyze multiple screenshots together
- **Caching**: Cache analysis results for similar screens
- **Hybrid Approach**: Use AI for complex cases, basic analysis for simple checks

## Troubleshooting

### API Key Not Working
- Verify key is valid: `curl https://api.openai.com/v1/models -H "Authorization: Bearer $OPENAI_API_KEY"`
- Check account has vision model access
- Verify billing is set up

### Analysis Fails
- Check network connectivity
- Verify screenshot file exists and is readable
- Check API rate limits
- Review logs for detailed error messages

### No Observations Generated
- AI might not detect issues (screen is clean)
- Check if fallback analysis is being used
- Review screenshot to verify it's correct

## Example Output

With AI vision enabled, you'll see detailed observations:

```
📊 Screen evaluation: Screen evaluation: PASS WITH ISSUES
   Observations:
   🔴 CRITICAL [ERROR]: Error message visible: "Invalid email format"
   🟠 HIGH [BLOCKING_ELEMENT]: Dialog blocking interaction: "Please confirm your action"
   🟢 POSITIVE [SUCCESS_INDICATOR]: Success message: "Mood saved successfully"
```

Without AI vision, you'll see:

```
📊 Screen evaluation: Screen evaluation: PASS
   Observations:
   🔵 LOW [UNEXPECTED_STATE]: Visual analysis performed (AI vision not available - using basic analysis)
```

