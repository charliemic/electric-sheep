# Interactive Element Detection Architecture

## Problem Statement

**Current Issue**: `GenericAdaptivePlanner` has hardcoded patterns for detecting interactive elements:
- Hardcoded list: "sign in", "create", "add", "save", etc.
- Doesn't understand visual design patterns (cards, buttons, etc.)
- Brittle - needs constant updates for new patterns
- Not truly "generic" - tied to specific text patterns

**User Question**: Should we:
1. Hard-code more design patterns (search online, build comprehensive list)?
2. Use AI to understand human-style design patterns?
3. Move this to a different architectural layer?

## Key Insight: Two Separate Concerns

**The user identified a critical distinction:**

### A) Content Detection (What I See)
- **Purpose**: "I detect the presence of 'mood'" (or any text, image, property)
- **Layer**: Perception
- **Principle**: **GENERIC** - no domain knowledge, just "I see text X", "I see image Y", "I see property Z"
- **Example**: "I see text 'Mood Management'" - no understanding of what "mood" means

### B) Interaction Affordance (How I Interact)
- **Purpose**: "I have to work out how to interact with it - is it something I can tap, swipe, read, etc."
- **Layer**: Perception
- **Principle**: Understands **interaction patterns** (not content meaning)
- **Example**: "This looks like a card (tappable)", "This looks like a button (tappable)", "This looks like text (readable)"

**Critical**: These are TWO SEPARATE concerns that should NOT be mixed.

## Architectural Analysis

### Current Architecture Layers

```
┌─────────────────────────────────────────────────────────┐
│  PERCEPTION LAYER (ScreenEvaluator)                     │
│  "What do I see?"                                       │
│  - OCR (text extraction)                                │
│  - Pattern Recognition (icons, templates)                │
│  - AI Vision (optional - via AIVisionAPI interface)     │
│  ❌ MISSING: Content Detection (generic)                 │
│  ❌ MISSING: Affordance Detection (interaction patterns) │
└─────────────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────────┐
│  PLANNING LAYER (GenericAdaptivePlanner)                │
│  "What should I do?"                                    │
│  - Observes screen state                                │
│  - Identifies gap (current vs goal)                     │
│  - Generates actions                                    │
│  - ❌ CURRENTLY: Hardcodes content patterns AND         │
│    interaction patterns (mixing concerns)              │
└─────────────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────────┐
│  ACTION LAYER (ActionExecutor)                          │
│  "How do I do it?"                                      │
│  - Executes taps, types, swipes                         │
└─────────────────────────────────────────────────────────┘
```

### The Problem: Mixed Concerns

**Current State**: `GenericAdaptivePlanner` is mixing:
1. **Content Detection**: "I'm looking for 'mood'" (domain knowledge)
2. **Affordance Detection**: "This is a button" (interaction pattern)

**Issue**: These should be separate - content detection should be generic, affordance detection should understand interaction patterns.

## Proposed Solution: Two-Layer Perception

### Architecture: Separate Content from Affordance

```
┌─────────────────────────────────────────────────────────┐
│  PERCEPTION LAYER (ScreenEvaluator)                     │
│                                                          │
│  ┌──────────────────────────────────────────────────┐  │
│  │  A) Content Detection (Generic)                  │  │
│  │  "What content do I see?"                         │  │
│  │  - Text: "I see text 'Mood Management'"          │  │
│  │  - Images: "I see image with icon X"             │  │
│  │  - Properties: "I see property Y"                 │  │
│  │  - NO DOMAIN KNOWLEDGE                           │  │
│  │  - Returns: DetectedContent(text, image, etc.)   │  │
│  └──────────────────────────────────────────────────┘  │
│                        ↓                                 │
│  ┌──────────────────────────────────────────────────┐  │
│  │  B) Affordance Detection (Interaction Patterns)  │  │
│  │  "How can I interact with what I see?"           │  │
│  │  - "This looks like a button (tappable)"         │  │
│  │  - "This looks like a card (tappable)"          │  │
│  │  - "This looks like text (readable)"             │  │
│  │  - "This looks like a list (swipeable)"          │  │
│  │  - Uses AI (LLaVA) to understand visual patterns │  │
│  │  - Returns: InteractionAffordance(type, method)  │  │
│  └──────────────────────────────────────────────────┘  │
│                        ↓                                 │
│  Combined: InteractiveElement(content, affordance)      │
└─────────────────────────────────────────────────────────┘
                        ↓
┌─────────────────────────────────────────────────────────┐
│  PLANNING LAYER (GenericAdaptivePlanner)                │
│  "What should I do?"                                    │
│  - Receives: List<InteractiveElement>                   │
│  - Matches content to goal (generic matching)          │
│  - Selects interaction method (tap, swipe, read)       │
│  - Generates actions                                    │
└─────────────────────────────────────────────────────────┘
```

### Implementation

#### A) Content Detection (Generic)

```kotlin
// ScreenEvaluator.kt

/**
 * Detected content on screen (generic - no domain knowledge).
 */
data class DetectedContent(
    val text: String? = null,        // "Mood Management"
    val image: ImageDescriptor? = null, // Icon, image, etc.
    val properties: Map<String, Any> = emptyMap() // Any other properties
)

/**
 * Detect all content on screen (generic detection).
 * 
 * NO DOMAIN KNOWLEDGE - just detects what's there.
 * 
 * @param screenshot Screenshot to analyze
 * @return List of detected content (text, images, properties)
 */
suspend fun detectContent(screenshot: File): List<DetectedContent> {
    val content = mutableListOf<DetectedContent>()
    
    // Extract text (OCR) - generic, no interpretation
    textDetector?.extractText(screenshot)?.let { extracted ->
        extracted.textRegions.forEach { region ->
            content.add(
                DetectedContent(
                    text = region.text, // Just the text, no meaning
                    properties = mapOf(
                        "location" to "${region.x},${region.y}",
                        "size" to "${region.width}x${region.height}",
                        "confidence" to region.confidence
                    )
                )
            )
        }
    }
    
    // Extract images/patterns (generic detection)
    patternRecognizer?.detectPatterns(screenshot)?.let { patterns ->
        patterns.detectedPatterns.forEach { pattern ->
            content.add(
                DetectedContent(
                    image = ImageDescriptor(pattern.name, pattern.location),
                    properties = mapOf(
                        "confidence" to pattern.confidence,
                        "size" to pattern.size
                    )
                )
            )
        }
    }
    
    return content
}
```

#### B) Affordance Detection (Interaction Patterns)

```kotlin
// ScreenEvaluator.kt

/**
 * Interaction affordance - how can I interact with this element?
 */
enum class InteractionType {
    TAPPABLE,    // Button, card, link - can tap
    SWIPEABLE,   // List, carousel - can swipe
    READABLE,    // Text, label - can read
    TYPEABLE,    // Input field - can type
    SCROLLABLE   // Scrollable area - can scroll
}

data class InteractionAffordance(
    val type: InteractionType,
    val confidence: Double,
    val visualCues: VisualCues // size, position, shape, color
)

/**
 * Detect interaction affordances for content.
 * 
 * Uses AI (LLaVA) to understand visual design patterns.
 * 
 * @param screenshot Screenshot to analyze
 * @param content Detected content to analyze
 * @return Interaction affordances for each content element
 */
suspend fun detectAffordances(
    screenshot: File,
    content: List<DetectedContent>
): Map<DetectedContent, InteractionAffordance> {
    // Use LLaVA to understand visual patterns
    val prompt = """
        Analyze this mobile app screenshot and identify interaction affordances.
        
        For each element, determine:
        1. Can it be tapped? (buttons, cards, links)
        2. Can it be swiped? (lists, carousels)
        3. Can it be read? (text, labels)
        4. Can it be typed into? (input fields)
        5. Can it be scrolled? (scrollable areas)
        
        Consider visual cues:
        - Buttons: Rectangular, prominent, often at bottom
        - Cards: Containers with content, often tappable
        - Input fields: Rectangular, often with placeholder text
        - Lists: Vertical/horizontal scrollable areas
        
        Return structured data for each element.
    """
    
    val analysis = ollamaService.generateWithImages(
        prompt = prompt,
        images = listOf(screenshot),
        model = "llava"
    )
    
    // Parse AI response into InteractionAffordance objects
    return parseAffordances(analysis, content)
}
```

#### Combined: InteractiveElement

```kotlin
// ScreenEvaluator.kt

/**
 * Interactive element combining content and affordance.
 */
data class InteractiveElement(
    val content: DetectedContent,           // What I see (generic)
    val affordance: InteractionAffordance, // How I interact
    val location: BoundingBox               // Where it is
)

/**
 * Detect all interactive elements on screen.
 * 
 * Combines content detection (generic) with affordance detection (interaction patterns).
 * 
 * @param screenshot Screenshot to analyze
 * @return List of interactive elements
 */
suspend fun detectInteractiveElements(screenshot: File): List<InteractiveElement> {
    // Step 1: Detect content (generic - no domain knowledge)
    val content = detectContent(screenshot)
    
    // Step 2: Detect affordances (interaction patterns)
    val affordances = detectAffordances(screenshot, content)
    
    // Step 3: Combine into interactive elements
    return content.mapNotNull { detectedContent ->
        affordances[detectedContent]?.let { affordance ->
            InteractiveElement(
                content = detectedContent,
                affordance = affordance,
                location = calculateBoundingBox(detectedContent)
            )
        }
    }
}
```

### Planner Usage

```kotlin
// GenericAdaptivePlanner.kt

suspend fun generatePlanForGoal(...) {
    // Get interactive elements (content + affordance)
    val interactiveElements = screenEvaluator.detectInteractiveElements(screenshot)
    
    // Planner matches content to goal (generic matching)
    // Example: Goal is "AUTHENTICATE", looking for content containing "sign" or "login"
    val matchingElements = interactiveElements.filter { element ->
        // Generic matching - no domain knowledge
        goal.matches(element.content.text) // Generic matching function
    }
    
    // Planner selects interaction method based on affordance
    val action = when (matchingElements.firstOrNull()?.affordance?.type) {
        InteractionType.TAPPABLE -> HumanAction.Tap(...)
        InteractionType.TYPEABLE -> HumanAction.TypeText(...)
        InteractionType.SWIPEABLE -> HumanAction.Swipe(...)
        // etc.
    }
}
```

## Key Principles

### 1. Separation of Concerns
- **Content Detection**: Generic, no domain knowledge
- **Affordance Detection**: Understands interaction patterns
- **Planning**: Matches content to goals, selects interaction method

### 2. Generic Content Detection
- ✅ Detects text: "I see 'Mood Management'"
- ✅ Detects images: "I see icon X"
- ✅ Detects properties: "I see property Y"
- ❌ NO interpretation: Doesn't know what "mood" means
- ❌ NO domain knowledge: Just detects what's there

### 3. Pattern-Based Affordance Detection
- ✅ Understands visual patterns: "This looks like a button"
- ✅ Understands interaction methods: "This is tappable"
- ✅ Uses AI (LLaVA) for visual understanding
- ❌ NO content interpretation: Doesn't care what text says

## Benefits

1. **Clean Separation**: Content detection is generic, affordance detection is pattern-based
2. **No Domain Pollution**: Content layer doesn't know about "mood", "sign in", etc.
3. **Flexible**: Works with any content, any interaction pattern
4. **AI-Powered**: Uses LLaVA for visual understanding (self-hosted)
5. **Human-Like**: Matches how humans perceive (see content, understand interaction)

## Implementation Plan

1. **Enhance ScreenEvaluator**:
   - Add `detectContent()` - generic content detection
   - Add `detectAffordances()` - interaction pattern detection (AI-powered)
   - Add `detectInteractiveElements()` - combines both

2. **Update GenericAdaptivePlanner**:
   - Remove hardcoded content patterns
   - Use `detectInteractiveElements()` from ScreenEvaluator
   - Match content generically to goals
   - Select interaction method based on affordance

3. **Fallback Strategy**:
   - If AI unavailable, use heuristic-based affordance detection
   - Maintains backward compatibility

## Conclusion

**Two separate concerns**:
- **A) Content Detection**: Generic - "I see text X" (no domain knowledge)
- **B) Affordance Detection**: Pattern-based - "This is tappable" (interaction understanding)

**Both belong in Perception Layer** (ScreenEvaluator), not Planning Layer.

**Planning Layer** receives combined `InteractiveElement` objects and:
- Matches content to goals (generic matching)
- Selects interaction method (based on affordance)
