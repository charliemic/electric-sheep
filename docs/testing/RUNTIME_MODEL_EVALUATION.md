# Runtime Model Evaluation: Pre-trained vs Custom Training

## Overview

This document evaluates runtime models for visual analysis in test automation, focusing on:
- **Ease of use**: Integration complexity, documentation quality
- **Quality**: Accuracy, performance, reliability
- **Open source**: License, community support
- **Pre-trained availability**: Can we use out-of-the-box or need training?

## Required Detection Tasks

Based on the test automation framework needs:

1. **Error Detection**: Red text, error icons, error dialogs
2. **Warning Detection**: Yellow/orange text, warning icons
3. **Loading State**: Spinners, progress bars, "Loading..." text
4. **Button Presence**: Save buttons, action buttons, navigation buttons
5. **Dialog/Popup Detection**: Blocking overlays, confirmation dialogs
6. **Success Indicators**: Green checkmarks, success messages
7. **Screen Classification**: "Is this the Mood Management screen?"
8. **Text Detection**: OCR for specific text (e.g., "Mood History")

## Model Evaluation Matrix

### Tier 1: Fast Local Models (10-50ms)

#### Option 1: ONNX Runtime + Pre-trained Models

**Ease of Use**: ⭐⭐⭐⭐⭐ (Excellent)
- ONNX Runtime has excellent Kotlin/Java support
- Simple API: load model, run inference
- Good documentation and examples
- Active community support

**Quality**: ⭐⭐⭐⭐ (Very Good)
- Pre-trained models available from ONNX Model Zoo
- Good accuracy for general object detection (85-95%)
- Fast inference (10-50ms on CPU)
- Optimized for mobile/edge devices

**Open Source**: ✅ Yes
- Apache 2.0 license
- Microsoft-maintained
- Active GitHub repository

**Pre-trained Models Available**: ✅ Yes (with limitations)

**Available Pre-trained Models**:
1. **YOLOv8 (ONNX)**: Object detection
   - **Size**: 5-50MB
   - **Accuracy**: 90-95% for general objects
   - **Use Case**: Button detection, icon detection
   - **Limitation**: Not trained specifically for mobile UI
   - **Training Required**: ⚠️ Fine-tuning recommended for UI-specific elements

2. **MobileNetV3 (ONNX)**: Image classification
   - **Size**: 5-20MB
   - **Accuracy**: 85-90% for general classification
   - **Use Case**: Screen type classification
   - **Limitation**: Trained on ImageNet (natural images, not UI)
   - **Training Required**: ⚠️ Fine-tuning required for UI screens

3. **EfficientDet (ONNX)**: Object detection
   - **Size**: 10-30MB
   - **Accuracy**: 90-95% for general objects
   - **Use Case**: Element detection
   - **Limitation**: Not UI-specific
   - **Training Required**: ⚠️ Fine-tuning recommended

**Verdict**: ✅ **Good for general detection, but needs fine-tuning for UI-specific tasks**

---

#### Option 2: TensorFlow Lite + Pre-trained Models

**Ease of Use**: ⭐⭐⭐⭐ (Very Good)
- Excellent Android integration (native support)
- TensorFlow Lite Task Library simplifies usage
- Good documentation
- Active community

**Quality**: ⭐⭐⭐⭐ (Very Good)
- Pre-trained models optimized for mobile
- Good accuracy (85-95%)
- Fast inference (20-100ms)
- Quantization support (smaller models)

**Open Source**: ✅ Yes
- Apache 2.0 license
- Google-maintained
- Active development

**Pre-trained Models Available**: ✅ Yes (with limitations)

**Available Pre-trained Models**:
1. **MobileNet V2/V3**: Image classification
   - **Size**: 2-20MB
   - **Accuracy**: 85-90%
   - **Use Case**: Screen classification
   - **Training Required**: ⚠️ Fine-tuning required for UI

2. **EfficientDet-Lite**: Object detection
   - **Size**: 5-30MB
   - **Accuracy**: 90-95%
   - **Use Case**: Button/icon detection
   - **Training Required**: ⚠️ Fine-tuning recommended

3. **SSD MobileNet V2**: Object detection
   - **Size**: 5-15MB
   - **Accuracy**: 85-90%
   - **Use Case**: General element detection
   - **Training Required**: ⚠️ Fine-tuning recommended

**Verdict**: ✅ **Good for general detection, but needs fine-tuning for UI-specific tasks**

---

#### Option 3: OpenCV + Template Matching

**Ease of Use**: ⭐⭐⭐⭐⭐ (Excellent)
- Simple API: `matchTemplate()`
- No model loading required
- Works with any image format
- Very fast (5-20ms)

**Quality**: ⭐⭐⭐⭐⭐ (Excellent for exact matches)
- Perfect accuracy for known UI elements (95-99%)
- Works great for buttons, icons, specific UI patterns
- No training needed for exact matches

**Open Source**: ✅ Yes
- BSD license
- Very mature (20+ years)
- Excellent documentation

**Pre-trained Models Available**: ❌ N/A (not ML-based)
- Uses template matching (image comparison)
- No pre-trained models needed
- Works with reference images

**Verdict**: ✅ **Perfect for exact UI element matching (buttons, icons), no training needed**

---

### Tier 2: Local Large Models (200-1000ms)

#### Option 4: Ollama + LLaVA (Vision-Language Model)

**Ease of Use**: ⭐⭐⭐⭐⭐ (Excellent)
- Very simple setup: `ollama pull llava:7b`
- HTTP API (easy to call from Kotlin)
- No model management needed
- Works out-of-the-box

**Quality**: ⭐⭐⭐⭐ (Very Good)
- Pre-trained on general vision-language tasks
- Good understanding of UI screens (80-90%)
- Can answer questions about screenshots
- Handles complex reasoning

**Open Source**: ✅ Yes
- MIT license (Ollama)
- Apache 2.0 (LLaVA)
- Active community

**Pre-trained Models Available**: ✅ Yes (fully pre-trained)

**Available Pre-trained Models**:
1. **LLaVA 1.5 (7B)**: Vision-language understanding
   - **Size**: 4-7GB
   - **Accuracy**: 80-90% for UI understanding
   - **Use Case**: Full screen analysis, complex reasoning
   - **Training Required**: ❌ No (fully pre-trained)
   - **Example**: "Is there an error message visible?" → Yes/No with explanation

2. **LLaVA 1.5 (13B)**: Larger, more accurate
   - **Size**: 7-13GB
   - **Accuracy**: 85-95% for UI understanding
   - **Use Case**: Complex screen analysis
   - **Training Required**: ❌ No (fully pre-trained)

3. **LLaVA 1.6 (8B)**: Latest version
   - **Size**: 5-8GB
   - **Accuracy**: 85-95% for UI understanding
   - **Training Required**: ❌ No (fully pre-trained)

**Verdict**: ✅ **Excellent for complex screen understanding, fully pre-trained, no training needed**

---

#### Option 5: llama.cpp + LLaVA

**Ease of Use**: ⭐⭐⭐ (Moderate)
- More control than Ollama
- Requires more setup
- C++ bindings needed for Kotlin
- More configuration options

**Quality**: ⭐⭐⭐⭐ (Very Good)
- Same models as Ollama
- Can be faster with optimizations
- More control over inference

**Open Source**: ✅ Yes
- MIT license
- Active development

**Pre-trained Models Available**: ✅ Yes (same as Ollama)

**Verdict**: ⚠️ **More complex than Ollama, but more control. Ollama is recommended for simplicity.**

---

### Tier 3: Cloud Services (500-2000ms)

#### Option 6: AWS Bedrock (Claude Sonnet 3.5 Vision)

**Ease of Use**: ⭐⭐⭐⭐ (Very Good)
- AWS SDK available for Kotlin/Java
- Good documentation
- Simple API calls
- Managed service (no model management)

**Quality**: ⭐⭐⭐⭐⭐ (Excellent)
- Highest accuracy (95%+)
- Excellent at UI understanding
- Handles edge cases well
- Pre-trained and always up-to-date

**Open Source**: ❌ No (proprietary)
- AWS-managed service
- Not open source

**Pre-trained Models Available**: ✅ Yes (fully pre-trained, always up-to-date)

**Verdict**: ✅ **Best quality, but not open source and has cost**

---

## Recommended Approach: Hybrid Strategy

### Phase 1: Use Pre-trained Models (No Training Required)

**For Simple Binary Checks**:
1. **OpenCV Template Matching** (no training)
   - Error icons (red X, error symbols)
   - Loading spinners (circular progress indicators)
   - Success checkmarks (green checkmarks)
   - Specific buttons (Save, Cancel, etc.)
   - **No training needed**: Just provide reference images

2. **ONNX/TFLite Pre-trained Models** (with minimal fine-tuning)
   - General object detection (YOLOv8, EfficientDet)
   - Can detect buttons, icons, dialogs (general objects)
   - **Minimal training**: Fine-tune on 100-500 UI screenshots
   - **Training effort**: Low (1-2 days with transfer learning)

**For Complex Understanding**:
3. **LLaVA via Ollama** (fully pre-trained)
   - Full screen understanding
   - Complex reasoning ("Is there an error?")
   - Screen classification
   - **No training needed**: Works out-of-the-box

### Phase 2: Fine-tune if Needed (Optional)

**When to Fine-tune**:
- Pre-trained models don't meet accuracy requirements (>90%)
- Need UI-specific detection (mobile app UI elements)
- Want faster inference (smaller, specialized models)

**How to Fine-tune**:
1. **Collect dataset**: 500-2000 labeled screenshots
2. **Use transfer learning**: Fine-tune last layers only
3. **Tools**: TensorFlow Lite Model Maker, ONNX training tools
4. **Time**: 1-3 days for fine-tuning

## Specific Recommendations by Task

### Task 1: Error Detection

**Recommended**: **OpenCV Template Matching** (no training)
- Create reference images of error icons, red text patterns
- Fast (5-20ms)
- High accuracy (95-99% for known patterns)
- No training needed

**Alternative**: **LLaVA via Ollama** (pre-trained)
- Ask: "Is there an error message visible?"
- Works for any error (not just known patterns)
- Slower (200-1000ms)
- No training needed

### Task 2: Button Presence

**Recommended**: **OpenCV Template Matching** (no training)
- Create reference images of buttons
- Fast (5-20ms)
- High accuracy (95-99%)
- No training needed

**Alternative**: **YOLOv8 (ONNX)** (minimal fine-tuning)
- Pre-trained on general objects
- Fine-tune on 200-500 button images
- More flexible (detects buttons not in reference set)
- Training time: 1-2 days

### Task 3: Loading State

**Recommended**: **OpenCV Template Matching** (no training)
- Create reference images of loading spinners
- Fast (5-20ms)
- High accuracy (95-99%)
- No training needed

**Alternative**: **LLaVA via Ollama** (pre-trained)
- Ask: "Is the app loading?"
- Works for any loading indicator
- No training needed

### Task 4: Screen Classification

**Recommended**: **LLaVA via Ollama** (pre-trained)
- Ask: "What screen is this? Is this the Mood Management screen?"
- Works for any screen
- No training needed
- Good accuracy (80-90%)

**Alternative**: **MobileNet (TFLite)** (fine-tuning required)
- Fine-tune on 500-1000 screenshots per screen type
- Faster inference (20-50ms)
- Higher accuracy (90-95%) after training
- Training time: 2-3 days

### Task 5: Text Detection (OCR)

**Recommended**: **Tesseract OCR** (pre-trained)
- Open source OCR engine
- Pre-trained on text recognition
- Good for printed text (85-90% accuracy)
- No training needed for general text
- **Note**: Mobile UI text is usually clear, works well

**Alternative**: **EasyOCR** (pre-trained)
- Better accuracy than Tesseract (90-95%)
- Pre-trained on multiple languages
- No training needed

## Implementation Priority

### Immediate (No Training Required)

1. **OpenCV Template Matching** for:
   - Error icons
   - Loading spinners
   - Success checkmarks
   - Known buttons

2. **LLaVA via Ollama** for:
   - Complex screen understanding
   - Error detection (general)
   - Screen classification
   - Full screen analysis

3. **Tesseract OCR** for:
   - Text detection ("Mood History", etc.)

### Short-term (Minimal Training)

1. **Fine-tune YOLOv8** for:
   - General button detection (beyond known buttons)
   - Dialog/popup detection
   - Icon detection

2. **Fine-tune MobileNet** for:
   - Screen classification (if LLaVA accuracy insufficient)

### Long-term (Optional)

1. **Custom models** if:
   - Pre-trained models don't meet requirements
   - Need very specific detection
   - Want to optimize for speed

## Cost-Benefit Analysis

### No Training Approach (Recommended)

**Tools**:
- OpenCV (template matching)
- LLaVA via Ollama (pre-trained)
- Tesseract OCR (pre-trained)

**Benefits**:
- ✅ Zero training time
- ✅ Works immediately
- ✅ Good accuracy (80-95%)
- ✅ All open source
- ✅ Fast (5-1000ms depending on tool)

**Limitations**:
- ⚠️ Template matching requires reference images
- ⚠️ LLaVA may need fine-tuning for 95%+ accuracy
- ⚠️ Some edge cases may need cloud fallback

### Minimal Training Approach

**Tools**:
- Fine-tuned YOLOv8 (200-500 images)
- Fine-tuned MobileNet (500-1000 images)

**Benefits**:
- ✅ Higher accuracy (90-95%)
- ✅ Faster inference (10-50ms)
- ✅ More flexible (detects unknown elements)

**Costs**:
- ⚠️ 1-3 days training time
- ⚠️ Need labeled dataset
- ⚠️ Model management overhead

## Final Recommendation

### ✅ **Start with Pre-trained Models (No Training)**

**Tier 1 (Fast)**:
1. **OpenCV Template Matching** for known UI elements
   - Error icons, loading spinners, buttons
   - No training needed
   - Fast (5-20ms)

2. **Tesseract OCR** for text detection
   - Pre-trained, no training needed
   - Fast (20-50ms)

**Tier 2 (Complex)**:
3. **LLaVA via Ollama** for complex understanding
   - Fully pre-trained
   - No training needed
   - Good accuracy (80-90%)

**Tier 3 (Fallback)**:
4. **AWS Bedrock** for edge cases
   - Pre-trained, always up-to-date
   - Highest accuracy (95%+)

### ⚠️ **Fine-tune Only If Needed**

If accuracy requirements are >90% and pre-trained models don't meet them:
- Fine-tune YOLOv8 on 200-500 UI screenshots (1-2 days)
- Fine-tune MobileNet on 500-1000 screenshots (2-3 days)

## Conclusion

**Answer**: **You can use pre-trained models for most tasks without training!**

1. **OpenCV Template Matching**: No training needed, works immediately
2. **LLaVA via Ollama**: Fully pre-trained, works out-of-the-box
3. **Tesseract OCR**: Pre-trained, works for text detection
4. **ONNX/TFLite Models**: Pre-trained, but may need fine-tuning for UI-specific tasks

**Training is optional** and only needed if:
- Pre-trained models don't meet accuracy requirements
- Need very specific UI element detection
- Want to optimize for speed with smaller models

**Recommended path**: Start with pre-trained models, fine-tune only if needed.

