# Two-Layer Interactive Element Detection - Implementation Summary

## Overview

Implemented a two-layer architecture for interactive element detection that separates **content detection** (what I see) from **affordance detection** (how I interact).

## Key Changes

### 1. ScreenEvaluator Enhancements

**Added Two-Layer Detection:**
- **Layer A: Content Detection** (`detectContent()`)
  - Generic detection of text, images, properties
  - No domain knowledge - just "I see text X"
  - Returns `DetectedContent` objects

- **Layer B: Affordance Detection** (`detectAffordances()`)
  - Understands interaction patterns (tappable, typeable, readable, etc.)
  - Uses AI (LLaVA via Ollama) when available
  - Falls back to heuristics when AI unavailable
  - Returns `InteractionAffordance` objects

- **Combined: InteractiveElement** (`detectInteractiveElements()`)
  - Combines content + affordance
  - Returns `InteractiveElement(content, affordance, location)`

**New Data Classes:**
- `DetectedContent` - Generic content (text, image, properties, location)
- `InteractionType` - Enum (TAPPABLE, TYPEABLE, READABLE, SWIPEABLE, SCROLLABLE)
- `InteractionAffordance` - Affordance with type, confidence, visual cues
- `InteractiveElement` - Combined content + affordance

### 2. GenericAdaptivePlanner Updates

**Changed from:**
- Hardcoded pattern matching for buttons
- Direct text extraction and button detection

**Changed to:**
- Uses `ScreenEvaluator.detectInteractiveElements()` for two-layer detection
- Filters by affordance type (tappable, typeable)
- Uses interactive elements for action generation
- Falls back to text-based detection if no interactive elements found

**Key Improvements:**
- No longer hardcodes button patterns in planner
- Uses affordance types to select interaction methods
- More human-like: sees content, understands interaction, then plans

### 3. Main.kt Integration

**Added:**
- `OllamaService` instantiation
- Passed to `ScreenEvaluator` for affordance detection
- Integrated with existing visual analysis components

## Architecture Compliance

### ✅ Agnostic Principle
- `GenericAdaptivePlanner` no longer contains hardcoded app-specific strings
- Uses generic patterns and metadata hints from `TaskDecomposer`
- Content detection is completely generic (no domain knowledge)

### ✅ Visual-First Principle
- All detection is visual (OCR, pattern recognition, AI vision)
- No Appium element queries for detection
- Uses screenshots for all analysis

### ✅ Separation of Concerns
- **Perception Layer** (`ScreenEvaluator`): Detects content and affordances
- **Planning Layer** (`GenericAdaptivePlanner`): Matches content to goals, selects interaction methods
- Clear separation: perception does detection, planning does decision-making

## Current Status

### ✅ Working
- Two-layer detection architecture implemented
- Content detection (generic)
- Affordance detection (AI + heuristics)
- Planner integration
- Compilation successful

### ⚠️ Needs Improvement
- Affordance detection heuristics may be too strict (0 tappable/typeable elements detected in test)
- AI affordance detection needs proper JSON parsing (currently uses heuristics)
- Fallback to simple planner when adaptive planner returns empty (working, but could be improved)

## Files Changed

### Modified
- `test-automation/src/main/kotlin/com/electricsheep/testautomation/monitoring/ScreenEvaluator.kt`
  - Added two-layer detection methods
  - Added data classes for content, affordance, interactive elements
  - Integrated OllamaService for AI affordance detection

- `test-automation/src/main/kotlin/com/electricsheep/testautomation/planner/GenericAdaptivePlanner.kt`
  - Updated to use `detectInteractiveElements()` instead of hardcoded patterns
  - Uses affordance types for action generation
  - Maintains fallback to text-based detection

- `test-automation/src/main/kotlin/com/electricsheep/testautomation/Main.kt`
  - Added OllamaService instantiation
  - Passed to ScreenEvaluator

### New Documentation
- `docs/testing/INTERACTIVE_ELEMENT_DETECTION_ARCHITECTURE.md` - Architecture documentation
- `docs/testing/TWO_LAYER_DETECTION_SUMMARY.md` - This file

## Next Steps

1. **Improve Affordance Detection Heuristics**
   - Review why 0 tappable/typeable elements detected
   - Adjust confidence thresholds
   - Improve pattern matching

2. **Enhance AI Affordance Detection**
   - Implement proper JSON parsing for LLaVA responses
   - Improve prompt for better structured output
   - Add validation for parsed affordances

3. **Testing**
   - Test with various UI patterns
   - Verify affordance detection accuracy
   - Ensure fallback works correctly

4. **Documentation**
   - Update architecture diagrams
   - Document affordance detection patterns
   - Add examples of detected elements

