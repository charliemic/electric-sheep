# Local vs Cloud Model Strategy for Test Automation

## Overview

This document outlines strategies for using local embedded models versus external cloud services (AWS Bedrock) for vision-based test automation. The goal is to balance performance, cost, accuracy, and operational complexity.

## Current Architecture

**Current State:**
- Screenshots captured every 500ms during test execution
- Visual analysis via `ScreenEvaluator` with `AIVisionAPI` interface
- Currently using Cursor's vision capabilities (manual/interactive)
- No real-time automated vision analysis during tests
- Fallback to basic analysis when AI not available

**Use Cases:**
1. **Screen State Detection**: "Is this the Mood Management screen?"
2. **Error Detection**: "Are there error messages visible?"
3. **Element Presence**: "Is the Save button visible?"
4. **Loading State**: "Is the app still loading?"
5. **Blocking Elements**: "Is there a dialog blocking interaction?"

## Strategy: Hybrid Approach

### Three-Tier Model Strategy

```
┌─────────────────────────────────────────────────────────┐
│ Tier 1: Local Embedded Models (Fast, Free, Simple)      │
│ - ONNX Runtime / TensorFlow Lite                        │
│ - Binary classification (error/no error)               │
│ - Element detection (button present/absent)             │
│ - ~10-50ms inference time                              │
└─────────────────────────────────────────────────────────┘
                    ↓ (if confidence low)
┌─────────────────────────────────────────────────────────┐
│ Tier 2: Local Large Model (Balanced, Free, Complex)    │
│ - Ollama / llama.cpp                                    │
│ - LLaVA (vision-language model)                        │
│ - Full screen understanding                            │
│ - ~200-1000ms inference time                           │
└─────────────────────────────────────────────────────────┘
                    ↓ (if still uncertain)
┌─────────────────────────────────────────────────────────┐
│ Tier 3: Cloud Service (Accurate, Cost, Complex)        │
│ - AWS Bedrock (Claude Sonnet 3.5 Vision)                │
│ - GPT-4 Vision (fallback)                               │
│ - Complex reasoning, edge cases                         │
│ - ~500-2000ms + network latency                         │
└─────────────────────────────────────────────────────────┘
```

## Tier 1: Local Embedded Models

### When to Use
- **High-frequency checks** (every 500ms during monitoring)
- **Simple binary decisions** (error present/absent, button visible/hidden)
- **Real-time feedback** (immediate action decisions)
- **Cost-sensitive operations** (thousands of screenshots per test run)

### Suitable Models

#### 1. **ONNX Runtime Models**
- **Purpose**: Binary classification, object detection
- **Size**: 5-50MB
- **Speed**: 10-50ms per inference
- **Accuracy**: High for simple tasks (90-95%)
- **Examples**:
  - Custom trained: "Error detector" (red text, error icons)
  - Custom trained: "Button detector" (specific UI elements)
  - Pre-trained: MobileNet (general object detection)

#### 2. **TensorFlow Lite Models**
- **Purpose**: Image classification, object detection
- **Size**: 2-30MB
- **Speed**: 20-100ms per inference
- **Accuracy**: High for simple tasks
- **Examples**:
  - Custom: "Loading spinner detector"
  - Custom: "Dialog/popup detector"
  - Pre-trained: EfficientDet-Lite (object detection)

#### 3. **OpenCV + Template Matching**
- **Purpose**: Exact UI element matching
- **Size**: <1MB (library only)
- **Speed**: 5-20ms per check
- **Accuracy**: Very high for known UI elements (95-99%)
- **Use Case**: Detecting specific buttons, icons, text patterns

### Implementation Example

```kotlin
// Tier 1: Fast local model for error detection
class LocalErrorDetector {
    private val onnxSession: OrtSession
    
    suspend fun hasError(screenshot: File): Boolean {
        val image = preprocessImage(screenshot) // Resize, normalize
        val input = onnxSession.run(mapOf("input" to image))
        val output = input[0].value as FloatArray
        return output[0] > 0.5f // Error probability > 50%
    }
    
    // Inference time: ~20ms
}

// Usage in ScreenEvaluator
suspend fun evaluateScreen(screenshot: File): ScreenEvaluation {
    // Tier 1: Fast local check
    if (localErrorDetector.hasError(screenshot)) {
        return ScreenEvaluation(
            observations = listOf(
                ScreenObservation(
                    type = ObservationType.ERROR,
                    severity = Severity.CRITICAL,
                    message = "Error detected (local model)",
                    screenshot = screenshot
                )
            )
        )
    }
    
    // Continue to Tier 2 if needed...
}
```

### Advantages
- ✅ **Zero cost** (no API calls)
- ✅ **Low latency** (10-50ms)
- ✅ **Works offline**
- ✅ **No rate limits**
- ✅ **Privacy** (data stays local)
- ✅ **Predictable performance**

### Disadvantages
- ❌ **Limited to simple tasks** (binary classification)
- ❌ **Requires training** (need labeled data)
- ❌ **Model size** (5-50MB per model)
- ❌ **Less flexible** (can't handle novel scenarios)

## Tier 2: Local Large Models

### When to Use
- **Complex screen understanding** (full screen analysis)
- **Uncertain Tier 1 results** (low confidence)
- **Cost-sensitive but need accuracy** (avoid cloud costs)
- **Privacy-critical** (sensitive app data)

### Suitable Models

#### 1. **Ollama + LLaVA**
- **Purpose**: Vision-language understanding
- **Size**: 4-7GB (model file)
- **Speed**: 200-1000ms per inference
- **Accuracy**: Good for general understanding (80-90%)
- **Setup**: 
  ```bash
  ollama pull llava:7b
  # Or smaller: llava:1.5b (faster, less accurate)
  ```

#### 2. **llama.cpp + LLaVA**
- **Purpose**: Same as Ollama, but more control
- **Size**: 4-7GB
- **Speed**: 150-800ms (can be faster with optimizations)
- **Accuracy**: Similar to Ollama
- **Advantage**: More control over inference parameters

#### 3. **ONNX Runtime + Vision Transformer**
- **Purpose**: Screen understanding
- **Size**: 100-500MB
- **Speed**: 100-500ms
- **Accuracy**: Good for structured tasks
- **Advantage**: Smaller than LLaVA, faster inference

### Implementation Example

```kotlin
// Tier 2: Local large model for complex analysis
class LocalVisionModel {
    private val ollamaClient: OllamaClient
    
    suspend fun analyzeScreen(screenshot: File, prompt: String): String {
        val response = ollamaClient.generate(
            model = "llava:7b",
            prompt = prompt,
            images = listOf(screenshot)
        )
        return response.text
    }
    
    // Inference time: ~500ms
}

// Usage in ScreenEvaluator
suspend fun evaluateScreen(screenshot: File): ScreenEvaluation {
    // Tier 1: Fast check
    if (localErrorDetector.hasError(screenshot)) {
        // Already detected, return
    }
    
    // Tier 2: Complex analysis if Tier 1 uncertain
    val confidence = localErrorDetector.getConfidence(screenshot)
    if (confidence < 0.7f) {
        val analysis = localVisionModel.analyzeScreen(
            screenshot,
            "Is there an error message visible? Describe what you see."
        )
        // Parse analysis...
    }
    
    // Continue to Tier 3 if still uncertain...
}
```

### Advantages
- ✅ **No per-request cost** (one-time model download)
- ✅ **Good accuracy** for complex tasks
- ✅ **Works offline**
- ✅ **Privacy** (data stays local)
- ✅ **No rate limits**

### Disadvantages
- ❌ **Large model size** (4-7GB)
- ❌ **Higher latency** (200-1000ms)
- ❌ **Requires GPU** for good performance (CPU works but slower)
- ❌ **Less accurate than cloud** (80-90% vs 95%+)

## Tier 3: Cloud Services (AWS Bedrock)

### When to Use
- **Tier 1 & 2 uncertain** (low confidence)
- **Complex reasoning needed** (edge cases, novel scenarios)
- **High-stakes decisions** (critical test failures)
- **Final verification** (after local models suggest issues)

### AWS Bedrock Options

#### 1. **Claude Sonnet 3.5 Vision (Anthropic)**
- **Cost**: ~$0.003 per image (input) + $0.015 per 1K tokens (output)
- **Speed**: 500-2000ms + network latency
- **Accuracy**: Very high (95%+)
- **Best for**: Complex reasoning, edge cases

#### 2. **Claude 3 Haiku Vision (Anthropic)**
- **Cost**: ~$0.001 per image (cheaper, faster)
- **Speed**: 300-1000ms + network latency
- **Accuracy**: High (90-95%)
- **Best for**: Balanced cost/accuracy

#### 3. **GPT-4 Vision (OpenAI via Bedrock)**
- **Cost**: Similar to Claude
- **Speed**: Similar to Claude
- **Accuracy**: Very high
- **Best for**: Alternative to Claude

### Implementation Example

```kotlin
// Tier 3: Cloud service for complex cases
class BedrockVisionAPI : ScreenEvaluator.AIVisionAPI {
    private val bedrockClient: BedrockRuntimeClient
    
    override suspend fun analyze(screenshot: File, prompt: String): String {
        val imageBytes = screenshot.readBytes()
        val base64Image = Base64.getEncoder().encodeToString(imageBytes)
        
        val request = InvokeModelRequest.builder()
            .modelId("anthropic.claude-3-5-sonnet-20241022-v2:0")
            .contentType("application/json")
            .accept("application/json")
            .body(SdkBytes.fromString("""
                {
                    "anthropic_version": "bedrock-2023-05-31",
                    "max_tokens": 1024,
                    "messages": [{
                        "role": "user",
                        "content": [
                            {"type": "image", "source": {"type": "base64", "media_type": "image/png", "data": "$base64Image"}},
                            {"type": "text", "text": "$prompt"}
                        ]
                    }]
                }
            """.trimIndent(), Charsets.UTF_8))
            .build()
        
        val response = bedrockClient.invokeModel(request)
        val responseBody = JSONObject(response.body().asString(Charsets.UTF_8))
        return responseBody.getJSONArray("content")
            .getJSONObject(0)
            .getString("text")
    }
}
```

### Advantages
- ✅ **Highest accuracy** (95%+)
- ✅ **Handles edge cases** (novel scenarios)
- ✅ **No model management** (AWS handles it)
- ✅ **Always up-to-date** (latest model versions)
- ✅ **Scalable** (no local resource limits)

### Disadvantages
- ❌ **Cost** (~$0.003 per image)
- ❌ **Network latency** (500-2000ms)
- ❌ **Requires internet** (no offline)
- ❌ **Rate limits** (API throttling)
- ❌ **Privacy** (data sent to cloud)

## Recommended Implementation Strategy

### Phase 1: Start with Tier 1 (Local Embedded)

**Goal**: Handle 80% of cases with fast, free local models

1. **Train custom models** for common checks:
   - Error detector (red text, error icons)
   - Loading spinner detector
   - Button presence detector
   - Dialog/popup detector

2. **Use ONNX Runtime** for inference:
   - Small model size (5-20MB each)
   - Fast inference (10-50ms)
   - Easy integration (Kotlin/Java compatible)

3. **Implement confidence thresholds**:
   - If confidence > 0.9: Use result directly
   - If confidence 0.7-0.9: Escalate to Tier 2
   - If confidence < 0.7: Escalate to Tier 2

**Expected Outcome**: 
- 80% of screenshots analyzed locally
- 20% escalated to Tier 2/3
- Cost: $0 (local only)
- Average latency: ~30ms

### Phase 2: Add Tier 2 (Local Large Model)

**Goal**: Handle 15% of remaining cases locally

1. **Set up Ollama** with LLaVA:
   ```bash
   ollama pull llava:7b
   # Or smaller: llava:1.5b for faster inference
   ```

2. **Create Ollama client** in Kotlin:
   - Use HTTP client to call Ollama API
   - Handle image encoding (base64)
   - Parse JSON responses

3. **Use for complex analysis**:
   - When Tier 1 confidence < 0.7
   - For full screen understanding
   - For edge cases Tier 1 can't handle

**Expected Outcome**:
- 80% Tier 1 (fast, free)
- 15% Tier 2 (slower, free)
- 5% Tier 3 (cloud, cost)
- Cost: ~$0.00015 per test run (5% cloud)
- Average latency: ~100ms (weighted)

### Phase 3: Add Tier 3 (Cloud Fallback)

**Goal**: Handle remaining 5% with highest accuracy

1. **Integrate AWS Bedrock**:
   - Use Claude Sonnet 3.5 Vision
   - Handle authentication (IAM roles)
   - Implement retry logic

2. **Use only when needed**:
   - Tier 1 & 2 both uncertain
   - Critical test failures
   - Final verification

**Expected Outcome**:
- 80% Tier 1 (10-50ms, $0)
- 15% Tier 2 (200-1000ms, $0)
- 5% Tier 3 (500-2000ms, ~$0.003)
- **Total cost per test run**: ~$0.00015 (very low)
- **Average latency**: ~100ms (excellent)

## Cost Analysis

### Scenario: 100 screenshots per test run

**Tier 1 Only (Local Embedded)**:
- Cost: $0
- Latency: 10-50ms per screenshot
- Accuracy: 80-90% (for simple tasks)

**Tier 1 + Tier 2 (Local Hybrid)**:
- Cost: $0
- Latency: 10-50ms (Tier 1) + 200-1000ms (Tier 2, 15%)
- Accuracy: 85-95% (for most tasks)

**Tier 1 + Tier 2 + Tier 3 (Full Hybrid)**:
- Cost: ~$0.00015 per test run (5% cloud)
- Latency: 10-50ms (Tier 1) + 200-1000ms (Tier 2) + 500-2000ms (Tier 3, 5%)
- Accuracy: 95%+ (for all tasks)

**Cloud Only (AWS Bedrock)**:
- Cost: ~$0.30 per test run (100 screenshots × $0.003)
- Latency: 500-2000ms per screenshot
- Accuracy: 95%+

**Savings with Hybrid**: 99.95% cost reduction vs cloud-only

## Implementation Roadmap

### Step 1: Proof of Concept (Week 1-2)
1. Train simple error detector (ONNX)
2. Integrate ONNX Runtime into `ScreenEvaluator`
3. Test with real screenshots
4. Measure accuracy and latency

### Step 2: Expand Tier 1 (Week 3-4)
1. Train additional models (loading, buttons, dialogs)
2. Implement confidence thresholds
3. Add model selection logic
4. Optimize inference speed

### Step 3: Add Tier 2 (Week 5-6)
1. Set up Ollama server
2. Create Ollama client in Kotlin
3. Integrate into `ScreenEvaluator`
4. Test hybrid Tier 1 + Tier 2

### Step 4: Add Tier 3 (Week 7-8)
1. Set up AWS Bedrock access
2. Create Bedrock client
3. Integrate as final fallback
4. Test full three-tier system

### Step 5: Optimize (Week 9+)
1. Fine-tune confidence thresholds
2. Optimize model selection
3. Cache common results
4. Monitor costs and performance

## Code Structure

```
test-automation/src/main/kotlin/com/electricsheep/testautomation/
├── vision/
│   ├── LocalModelDetector.kt          # Tier 1: ONNX/TFLite models
│   ├── LocalVisionModel.kt            # Tier 2: Ollama/LLaVA
│   ├── BedrockVisionAPI.kt            # Tier 3: AWS Bedrock
│   ├── HybridVisionAnalyzer.kt        # Orchestrates all tiers
│   └── models/                        # Model files (ONNX, TFLite)
│       ├── error_detector.onnx
│       ├── loading_detector.onnx
│       └── button_detector.onnx
└── monitoring/
    └── ScreenEvaluator.kt             # Uses HybridVisionAnalyzer
```

## Decision Tree

```
Screenshot captured
    ↓
Is it a simple binary check? (error, loading, button)
    ├─ Yes → Tier 1 (ONNX) → Confidence > 0.9?
    │                              ├─ Yes → Return result
    │                              └─ No → Tier 2
    │
    └─ No → Tier 2 (LLaVA) → Confidence > 0.8?
                                    ├─ Yes → Return result
                                    └─ No → Tier 3 (Bedrock)
```

## Recommendations

### Start with Tier 1 (Local Embedded)
- **Why**: Handles 80% of cases, zero cost, fast
- **When**: Now (immediate implementation)
- **Effort**: Medium (need to train models)

### Add Tier 2 (Local Large Model)
- **Why**: Handles 15% more cases, still free
- **When**: After Tier 1 is working
- **Effort**: Low (Ollama is easy to set up)

### Add Tier 3 (Cloud) Last
- **Why**: Only needed for 5% of edge cases
- **When**: After Tier 1 & 2 are working
- **Effort**: Medium (AWS integration)

### Don't Skip Tiers
- **Why**: Each tier optimizes for different use cases
- **Result**: Best balance of cost, speed, and accuracy

## Conclusion

**Recommended Strategy**: **Three-tier hybrid approach**

1. **Tier 1 (Local Embedded)**: Fast, free, handles 80%
2. **Tier 2 (Local Large Model)**: Slower, free, handles 15%
3. **Tier 3 (Cloud)**: Accurate, cost, handles 5%

**Benefits**:
- 99.95% cost savings vs cloud-only
- Fast average latency (~100ms)
- High accuracy (95%+)
- Works offline (Tier 1 & 2)
- Privacy-friendly (most data stays local)

**Next Steps**:
1. Train Tier 1 models (error detector, etc.)
2. Integrate ONNX Runtime
3. Test with real screenshots
4. Iterate based on results

