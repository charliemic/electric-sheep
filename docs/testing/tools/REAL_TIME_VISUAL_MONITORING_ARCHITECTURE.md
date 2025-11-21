# Real-Time Visual Monitoring Architecture

## Problem Statement

Current test automation frameworks execute actions sequentially and check state reactively. This doesn't match human behavior:
- **Humans observe continuously** - we watch the screen and react to what we see
- **Humans act based on observation** - we see a button, then tap it
- **Current approach**: Execute action → Wait → Check result (reactive)
- **Desired approach**: Observe screen → Decide action → Execute (proactive)

## Architecture Overview

### Parallel Observation-Action Model

```
┌─────────────────────────────────────────────────────────────┐
│                    Test Automation Framework                 │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌──────────────────┐         ┌──────────────────┐         │
│  │  Screen Monitor  │◄───────┤   Action Executor │         │
│  │   (Observer)     │         │     (Actor)       │         │
│  │                  │         │                   │         │
│  │ • Screenshots    │         │ • Taps            │         │
│  │ • AI Analysis    │         │ • Types           │         │
│  │ • State Detect   │         │ • Swipes          │         │
│  └──────────────────┘         └──────────────────┘         │
│         │                              │                     │
│         │ State Updates                │ Action Results      │
│         │                              │                     │
│         └──────────────┬───────────────┘                     │
│                        │                                      │
│                        ▼                                      │
│              ┌──────────────────┐                            │
│              │  AI Planner      │                            │
│              │  (Decision Maker)│                            │
│              │                  │                            │
│              │ • Observes state │                            │
│              │ • Decides action │                            │
│              │ • Coordinates    │                            │
│              └──────────────────┘                            │
│                        │                                      │
│                        │ Decisions                            │
│                        ▼                                      │
│              ┌──────────────────┐                            │
│              │  State Manager   │                            │
│              │  (Coordinates)   │                            │
│              │                  │                            │
│              │ • Current state  │                            │
│              │ • State changes │                            │
│              │ • Event stream   │                            │
│              └──────────────────┘                            │
└─────────────────────────────────────────────────────────────┘
```

## Components

### 1. Screen Monitor (Observer)

**Purpose**: Continuously observe the screen and report state changes in real-time.

**Responsibilities**:
- Capture screenshots at regular intervals (configurable: 500ms-2s)
- Analyze screenshots using AI vision API
- Detect UI state changes (loading, errors, new screens, element visibility)
- Report state to State Manager
- Trigger alerts when important events occur

**Key Features**:
- Runs in parallel coroutine (non-blocking)
- Adjustable monitoring frequency
- AI-powered visual analysis
- State change detection

**Implementation Sketch**:
```kotlin
class ScreenMonitor(
    private val driver: AndroidDriver,
    private val aiVisionAPI: AIVisionAPI,
    private val stateManager: StateManager,
    private val monitoringIntervalMs: Long = 1000
) {
    private var isMonitoring = false
    private var lastState: ScreenState? = null
    
    suspend fun startMonitoring() {
        isMonitoring = true
        while (isMonitoring) {
            // Capture screenshot
            val screenshot = driver.getScreenshotAs(OutputType.FILE)
            
            // Analyze with AI vision
            val currentState = analyzeScreenState(screenshot)
            
            // Detect and report state changes
            if (currentState != lastState) {
                stateManager.onStateChanged(currentState, lastState)
                lastState = currentState
            }
            
            delay(monitoringIntervalMs)
        }
    }
    
    private suspend fun analyzeScreenState(screenshot: File): ScreenState {
        // Use AI vision to understand current screen
        val analysis = aiVisionAPI.analyze(
            image = screenshot,
            prompt = """
                Analyze this mobile app screenshot.
                Describe:
                1. What screen is this?
                2. What elements are visible?
                3. Are there any loading indicators?
                4. Are there any error messages?
                5. What actions are possible?
            """
        )
        return parseScreenState(analysis)
    }
    
    fun stopMonitoring() {
        isMonitoring = false
    }
}
```

### 2. Action Executor (Actor)

**Purpose**: Execute actions on the app based on decisions from AI Planner.

**Responsibilities**:
- Execute taps, types, swipes, navigation
- Report action results immediately
- Coordinate with Screen Monitor (don't capture during actions)
- Handle action failures gracefully

**Current Status**: Already implemented, needs coordination with Screen Monitor.

**Enhancement Needed**:
- Signal Screen Monitor when action starts (pause monitoring briefly)
- Resume monitoring after action completes
- Report action results to State Manager

### 3. AI Planner (Decision Maker)

**Purpose**: Make decisions based on observed state and task goals.

**Responsibilities**:
- Receive state updates from Screen Monitor
- Decide next action based on current state
- Coordinate between observation and action
- Handle state transitions and errors
- Re-plan when state changes unexpectedly

**Enhanced Implementation**:
```kotlin
class TaskPlanner(
    private val actionExecutor: ActionExecutor,
    private val screenMonitor: ScreenMonitor,
    private val stateManager: StateManager
) {
    suspend fun planAndExecute(task: String) {
        // Start monitoring in parallel
        val monitorJob = launch { screenMonitor.startMonitoring() }
        
        try {
            // Wait for initial state
            val initialState = stateManager.waitForState(timeoutMs = 5000)
                ?: throw Exception("Failed to get initial state")
            
            // Plan based on current state
            var plan = generatePlan(task, initialState)
            
            // Execute plan with continuous observation
            for (action in plan) {
                // Check if state changed unexpectedly
                if (stateManager.hasStateChanged()) {
                    val newState = stateManager.getCurrentState()
                    logger.info("State changed unexpectedly, re-planning...")
                    plan = adjustPlan(plan, newState)
                    continue
                }
                
                // Execute action
                val result = actionExecutor.execute(action)
                
                // Wait for state to stabilize (monitor will detect this)
                val stableState = stateManager.waitForStateStable(timeoutMs = 10000)
                
                // Check if action achieved desired state
                if (!isStateDesired(stableState, task)) {
                    // Re-plan based on new state
                    plan = adjustPlan(plan, stableState)
                }
            }
        } finally {
            screenMonitor.stopMonitoring()
            monitorJob.cancel()
        }
    }
}
```

### 4. State Manager (Coordinator)

**Purpose**: Coordinate between Screen Monitor and Action Executor.

**Responsibilities**:
- Track current screen state
- Detect and report state changes
- Provide state queries to AI Planner
- Coordinate timing between observation and action
- Manage state history

**Implementation**:
```kotlin
class StateManager {
    private var currentState: ScreenState? = null
    private val stateLock = Mutex()
    private val stateChangeChannel = Channel<ScreenState>(Channel.UNLIMITED)
    private var stateChangeListeners = mutableListOf<(ScreenState) -> Unit>()
    
    suspend fun onStateChanged(newState: ScreenState, oldState: ScreenState?) {
        stateLock.withLock {
            val changed = currentState != newState
            currentState = newState
            
            if (changed) {
                stateChangeChannel.send(newState)
                stateChangeListeners.forEach { it(newState) }
                logger.debug("State changed: ${oldState?.screenName} → ${newState.screenName}")
            }
        }
    }
    
    suspend fun getCurrentState(): ScreenState? {
        return stateLock.withLock { currentState }
    }
    
    suspend fun waitForState(
        predicate: (ScreenState) -> Boolean,
        timeoutMs: Long = 10000
    ): ScreenState? {
        val startTime = System.currentTimeMillis()
        
        // Check current state first
        val current = getCurrentState()
        if (current != null && predicate(current)) {
            return current
        }
        
        // Wait for state change
        while (System.currentTimeMillis() - startTime < timeoutMs) {
            val newState = stateChangeChannel.receive()
            if (predicate(newState)) {
                return newState
            }
        }
        
        return null // Timeout
    }
    
    suspend fun waitForStateStable(
        stableDurationMs: Long = 2000,
        timeoutMs: Long = 10000
    ): ScreenState? {
        // Wait for state to remain unchanged for stableDurationMs
        var lastState = getCurrentState()
        var lastChangeTime = System.currentTimeMillis()
        val startTime = System.currentTimeMillis()
        
        while (System.currentTimeMillis() - startTime < timeoutMs) {
            val newState = stateChangeChannel.receive()
            if (newState != lastState) {
                lastState = newState
                lastChangeTime = System.currentTimeMillis()
            } else if (System.currentTimeMillis() - lastChangeTime >= stableDurationMs) {
                return lastState // State is stable
            }
        }
        
        return lastState
    }
    
    fun hasStateChanged(): Boolean {
        return stateChangeChannel.tryReceive().isSuccess
    }
}
```

## Real-Time Visual Analysis

### AI Vision Integration

**Use AI vision API to analyze screenshots in real-time:**

```kotlin
class AIVisionAnalyzer(
    private val apiKey: String,
    private val model: String = "gpt-4-vision-preview"
) {
    suspend fun analyzeScreen(
        screenshot: File,
        question: String
    ): String {
        // Send screenshot + question to AI vision API
        // Returns natural language description
    }
    
    suspend fun detectState(screenshot: File): ScreenState {
        val analysis = analyzeScreen(
            screenshot,
            """
            Analyze this mobile app screenshot and describe:
            1. Screen name/type
            2. Visible elements (buttons, text fields, etc.)
            3. Loading indicators (yes/no)
            4. Error messages (if any)
            5. Interactive elements available
            """
        )
        return parseState(analysis)
    }
    
    suspend fun isElementVisible(
        screenshot: File,
        elementDescription: String
    ): Boolean {
        val response = analyzeScreen(
            screenshot,
            "Is '$elementDescription' visible on this screen? Answer yes or no."
        )
        return response.lowercase().contains("yes")
    }
    
    suspend fun isStateReady(
        screenshot: File,
        expectedState: String
    ): Boolean {
        val response = analyzeScreen(
            screenshot,
            "Is the following state visible: '$expectedState'? Answer yes or no."
        )
        return response.lowercase().contains("yes")
    }
}
```

### Visual State Detection

**Detect common states visually:**

1. **Loading State**: "Is there a loading indicator, spinner, or progress bar visible?"
2. **Error State**: "Are there any error messages, red text, or warning indicators visible?"
3. **Success State**: "Is the expected content visible (e.g., 'Mood History' text, confirmation message)?"
4. **Screen Transition**: "Has the screen changed (different layout, new elements, different content)?"
5. **Element Ready**: "Is the target element visible and appears clickable?"

## Implementation Strategy

### Phase 1: Parallel Monitoring Foundation

**Goal**: Basic parallel observation without AI analysis.

1. **Create ScreenMonitor class**
   - Capture screenshots at intervals (1-2s)
   - Basic state detection (element presence, loading indicators)
   - Report to StateManager

2. **Create StateManager class**
   - Track current state
   - Provide state queries
   - Coordinate timing

3. **Integrate with TaskPlanner**
   - Start monitoring in parallel coroutine
   - Use state for decision-making
   - Stop monitoring on completion

**Timeline**: 1-2 days

### Phase 2: AI Vision Integration

**Goal**: Real-time AI-powered visual analysis.

1. **Add AIVisionAnalyzer**
   - Integrate with OpenAI Vision API (or similar)
   - Analyze screenshots in real-time
   - Parse state from AI responses

2. **Enhance ScreenMonitor**
   - Use AI to analyze screenshots
   - Detect complex states visually
   - Report meaningful state changes

**Timeline**: 2-3 days

### Phase 3: Reactive Planning

**Goal**: State-driven decision making.

1. **State-driven planning**
   - Re-plan when state changes unexpectedly
   - Adjust actions based on observed state
   - Handle errors detected visually

2. **Visual wait conditions**
   - Wait until AI sees expected state
   - No implicit element checks
   - Pure visual observation

**Timeline**: 2-3 days

## Benefits

### 1. More Realistic Behavior
- ✅ Mimics human observation-action cycle
- ✅ Reacts to what's actually visible
- ✅ Handles unexpected state changes naturally

### 2. Better Error Detection
- ✅ Detects visual errors (not just element presence)
- ✅ Sees loading states naturally
- ✅ Handles UI glitches visually

### 3. More Reliable
- ✅ No reliance on element selectors for state detection
- ✅ Works even if accessibility IDs change
- ✅ Handles dynamic UI changes

### 4. Better Debugging
- ✅ Continuous screenshot stream
- ✅ Visual record of state changes
- ✅ Easy to see what AI "saw" at each step

## Challenges and Solutions

### 1. Performance

**Challenge**: Continuous screenshot capture and AI analysis is expensive.

**Solutions**:
- **Adjustable monitoring interval**: Faster during critical actions (500ms), slower during stable periods (2s)
- **Smart monitoring**: Only monitor when needed (pause during long waits)
- **Caching**: Cache AI responses for similar screens
- **Local fallback**: Use element checks for simple states, AI for complex

### 2. Cost

**Challenge**: AI vision API calls can be expensive at scale.

**Solutions**:
- **Selective analysis**: Only analyze when state might have changed
- **Batch analysis**: Analyze multiple screenshots together
- **Local models**: Use cheaper/free local models for simple checks
- **Hybrid approach**: Element checks for common cases, AI for edge cases

### 3. Latency

**Challenge**: AI analysis takes time (100-500ms per screenshot).

**Solutions**:
- **Parallel processing**: Monitor while executing (non-blocking)
- **Predictive actions**: Act on likely outcomes while waiting for confirmation
- **Fallback**: Use element checks for time-sensitive operations
- **Optimized prompts**: Shorter prompts = faster responses

## Comparison with Current Approach

### Current (Sequential, Reactive)
```
1. Capture screenshot
2. AI plans actions (based on screenshot)
3. Execute action
4. Wait (fixed time or element-based)
5. Capture screenshot
6. Check result
7. Repeat
```

**Issues**:
- ❌ No continuous observation
- ❌ Misses state changes during waits
- ❌ Fixed waits waste time
- ❌ Reactive (acts, then checks)

### Proposed (Parallel, Proactive)
```
1. Start Screen Monitor (parallel)
   └─ Continuously captures screenshots
   └─ Analyzes with AI vision
   └─ Reports state changes
   
2. AI Planner observes state
   └─ Decides action based on current state
   └─ Executes action
   
3. Screen Monitor detects action result
   └─ Reports new state
   
4. AI Planner reacts to state change
   └─ Decides next action
   └─ Repeat
```

**Benefits**:
- ✅ Continuous observation
- ✅ Detects state changes immediately
- ✅ No wasted time waiting
- ✅ Proactive (observes, then acts)

## Tools and Libraries

### For Screenshot Capture
- **Appium**: `driver.getScreenshotAs(OutputType.FILE)` - already available
- **Performance**: ~100-200ms per screenshot
- **Frequency**: 1-2 screenshots per second (adjustable)

### For AI Vision Analysis
- **OpenAI GPT-4 Vision**: Best quality, ~$0.01-0.03 per image
- **Claude Vision**: Good alternative, similar pricing
- **Local Models**: Faster, free, but lower quality (e.g., LLaVA)
- **Hybrid**: Use local for simple checks, cloud for complex

### For Parallel Execution
- **Kotlin Coroutines**: Already using, perfect for parallel monitoring
- **Channels**: For state change communication
- **Flow**: For reactive state updates
- **Mutex**: For thread-safe state access

## Example Flow

```
Time    Screen Monitor              AI Planner              Action Executor
─────────────────────────────────────────────────────────────────────────────
0s      Start monitoring
        └─ Screenshot: Landing      Observes: Landing       ─
        └─ AI: "Landing screen"     screen visible
        └─ State: LandingScreen     Plans: "Tap Mood Mgmt"
        
1s      Screenshot: Landing         ─                       Executes: Tap
        └─ State: LandingScreen                              "Mood Mgmt"
        
2s      Screenshot: Sign In         Observes: Sign In       ─
        └─ AI: "Sign in screen"     screen appeared
        └─ State: SignInScreen      Plans: "Type email"
        
3s      Screenshot: Sign In         ─                       Executes: Type
        └─ State: SignInScreen                              email
        
4s      Screenshot: Sign In         Observes: Email typed    ─
        └─ AI: "Email field filled" Plans: "Type password"
        
5s      Screenshot: Sign In         ─                       Executes: Type
        └─ State: SignInScreen                              password
        
6s      Screenshot: Sign In         Observes: Form ready     ─
        └─ AI: "Form complete"      Plans: "Tap Sign In"
        
7s      Screenshot: Loading         Observes: Loading        Executes: Tap
        └─ AI: "Loading indicator"  Detected                "Sign In"
        └─ State: LoadingScreen    Waits...
        
8s      Screenshot: Loading         ─                       ─
        └─ State: LoadingScreen
        
9s      Screenshot: Mood Mgmt       Observes: Loading       ─
        └─ AI: "Mood Management"    complete, screen
        └─ State: MoodMgmtScreen    changed
                                     Plans: "Add mood"
```

## Next Steps

1. **Design ScreenMonitor interface** - Define API and responsibilities
2. **Implement basic ScreenMonitor** - Screenshot capture + simple state detection
3. **Create StateManager** - Coordinate between monitor and executor
4. **Integrate with TaskPlanner** - Parallel execution with coroutines
5. **Add AI vision analysis** - Enhance ScreenMonitor with visual AI
6. **Implement reactive planning** - State-driven decisions

This architecture enables true real-time visual monitoring, making tests more realistic and reliable by mimicking human observation-action behavior.
