# Iterative Implementation Plan: Runtime Visual Evaluation

**Agent**: Current agent (Auto)  
**Branch**: `feature/runtime-visual-evaluation-architecture`  
**Worktree**: `../electric-sheep-runtime-visual-evaluation`  
**Status**: Starting Phase 1

## Implementation Strategy

**Approach**: Start with smallest, most testable component, test it, evaluate human-likeness, then iterate.

---

## Phase 1: OCR Text Detection (Current)

### Goal
Implement text detection from screenshots to enable error detection and screen identification.

### Why Start Here?
1. ✅ **Simplest**: No training needed, works immediately
2. ✅ **Easiest to test**: Can verify it reads text from screenshots
3. ✅ **Immediate value**: Can detect errors, identify screens
4. ✅ **Matches human behavior**: Humans read text on screens

### Implementation Steps

#### Step 1.1: Add Tesseract OCR Dependency
- Add Tesseract Java wrapper to `build.gradle.kts`
- Verify it compiles

#### Step 1.2: Create TextDetector Class
- Simple class that extracts text from screenshots
- Returns extracted text and detected patterns (errors, screen names)

#### Step 1.3: Integrate into ScreenEvaluator
- Replace basic fallback with OCR-based analysis
- Use OCR to detect error messages
- Use OCR to identify screen names

#### Step 1.4: Test with Real Screenshots
- Test on actual app screenshots
- Verify it can read text
- Verify it detects errors
- Verify it identifies screens

#### Step 1.5: Evaluate Human-Likeness
- Document: Does this make tests more human-like?
- Answer: Yes - humans read text, now system reads text
- Update architecture doc with findings

### Success Criteria
- ✅ Can extract text from screenshots
- ✅ Can detect error messages via text keywords
- ✅ Can identify screen names from text
- ✅ Latency < 100ms
- ✅ Accuracy > 85% for clear text

### Expected Outcome
- Basic visual evaluation working
- Error detection via text
- Screen identification via text
- Foundation for next phases

---

## Phase 2: Pattern Recognition (Next)

### Goal
Add OpenCV template matching for known icons (error icons, loading spinners).

### Why Next?
- Builds on Phase 1
- Adds fast pattern recognition (5-20ms)
- Handles non-text elements

### Implementation Steps
1. Add OpenCV dependency
2. Create PatternRecognizer class
3. Create reference images for known icons
4. Integrate into ScreenEvaluator
5. Test pattern matching
6. Evaluate human-likeness

### Success Criteria
- ✅ Can match known icons (95%+ accuracy)
- ✅ Latency < 20ms
- ✅ Works for stable UI elements

---

## Phase 3: Object Detection (Future)

### Goal
Add ONNX object detection for general element detection.

### Why Last?
- Requires training (1-2 days)
- More complex
- Builds on Phases 1 & 2

### Implementation Steps
1. Collect training data (500-1000 screenshots)
2. Fine-tune ONNX model
3. Create ObjectDetector class
4. Integrate into ScreenEvaluator
5. Test object detection
6. Evaluate human-likeness

### Success Criteria
- ✅ Can detect buttons, dialogs, input fields
- ✅ Latency < 50ms
- ✅ Accuracy > 90%

---

## Human-Likeness Evaluation Framework

After each phase, evaluate:

### Questions to Answer
1. **Does this match human behavior?**
   - Phase 1 (OCR): ✅ Yes - humans read text
   - Phase 2 (Pattern): ✅ Yes - humans recognize familiar icons
   - Phase 3 (Object): ✅ Yes - humans identify object categories

2. **Does this improve test human-likeness?**
   - Phase 1: ✅ Yes - tests now "read" screens like humans
   - Phase 2: ✅ Yes - tests now "recognize" icons like humans
   - Phase 3: ✅ Yes - tests now "identify" objects like humans

3. **What's the next improvement?**
   - After Phase 1: Add pattern recognition for icons
   - After Phase 2: Add object detection for general elements
   - After Phase 3: Add context integration

### Metrics to Track
- **Latency**: How fast is it? (target: <100ms)
- **Accuracy**: How accurate? (target: >90%)
- **Human-likeness**: Does it match human behavior? (qualitative)

---

## Current Status

**Phase 1: OCR Text Detection** - Starting now

**Next Steps**:
1. Add Tesseract dependency
2. Create TextDetector class
3. Test on real screenshots
4. Evaluate human-likeness

