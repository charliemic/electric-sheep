# Test Automation Framework

Hybrid AI + Appium test automation framework for Electric Sheep app.

## Architecture

```
┌─────────────────────────────────────────┐
│  Layer 1: Task Planner (AI)            │
│  - Takes screenshot                     │
│  - Devises plan                         │
│  - Checks action feasibility            │
│  - Feedback loop                        │
└──────────────┬──────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────┐
│  Layer 2: Action Executor (Appium)    │
│  - Executes human-like actions         │
│  - Uses best practices (accessibility)  │
│  - Returns feedback                    │
└─────────────────────────────────────────┘
```

## Features

- **Human-like Actions**: Abstract actions that correlate to human tasks
- **Best Practices**: Uses accessibility IDs over XPath
- **Feedback Loop**: Planner adjusts based on execution results
- **Kotlin Native**: Uses Kotlin with Appium Java SDK

## Setup

### Prerequisites

1. **Appium Server**
   ```bash
   npm install -g appium
   npm install -g @appium/doctor
   appium driver install uiautomator2
   ```

2. **Android Emulator**
   ```bash
   emulator -avd <your-emulator>
   ```

3. **Build**
   ```bash
   cd test-automation
   ./gradlew build
   ```

### Running Tests

1. **Start Appium Server**
   ```bash
   appium
   ```

2. **Run Test**
   ```bash
   ./gradlew run --args="--task 'Sign up and add mood value' --context 'tech_novice persona'"
   ```

### With AI Planning (Optional)

Set OpenAI API key:
```bash
export OPENAI_API_KEY=your-key-here
./gradlew run --args="--task 'Sign up and add mood value' --ai-api-key $OPENAI_API_KEY"
```

## Human Actions

The framework uses human-like actions:

- `Tap(target, accessibilityId)` - Tap on an element
- `TypeText(target, text, accessibilityId)` - Type into a field
- `Swipe(direction)` - Swipe in a direction
- `WaitFor(condition)` - Wait for a condition
- `Verify(condition)` - Verify a condition

These abstract away Appium internals and correlate to what a human would do.

## Best Practices

1. **Accessibility IDs First**: Always prefer accessibility IDs over XPath
2. **Deterministic Waits**: Use explicit waits, not fixed sleeps
3. **Human Tasks**: Actions should correlate to human tasks
4. **Feedback Loop**: Planner adjusts based on execution results

## Example

```kotlin
val actions = listOf(
    HumanAction.Tap(
        target = "Mood Management",
        accessibilityId = "Mood Management utility"
    ),
    HumanAction.TypeText(
        target = "Email field",
        text = "test@example.com",
        accessibilityId = "Email address input field"
    )
)

actions.forEach { action ->
    val result = actionExecutor.execute(action)
    // Handle result, provide feedback to planner
}
```

