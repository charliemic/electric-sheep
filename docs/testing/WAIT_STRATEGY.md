# Wait Strategy

## Overview

The test automation framework uses **dynamic waits** that proceed as soon as elements are available, with reasonable timeouts for leniency. This ensures tests run as fast as possible while being resilient to timing variations.

## Wait Types

### Dynamic Waits (Preferred)

**WebDriverWait with ExpectedConditions** - Proceeds immediately when condition is met:
- `wait.until(ExpectedConditions.presenceOfElementLocated())` - Waits up to timeout, but proceeds as soon as element appears
- `wait.until(ExpectedConditions.visibilityOf())` - Waits up to timeout, but proceeds as soon as element is visible
- `wait.until(ExpectedConditions.elementToBeClickable())` - Waits up to timeout, but proceeds as soon as element is clickable

**Benefits:**
- ✅ Fast: Proceeds immediately when ready
- ✅ Resilient: Allows reasonable timeout for leniency
- ✅ No unnecessary blocking

### Fixed Waits (Avoided)

**Thread.sleep()** - Always blocks for full duration:
- ❌ Slow: Always waits full duration even if ready earlier
- ❌ Brittle: May wait too long or not long enough
- ❌ Only used in minimal cases where dynamic wait isn't possible

## Framework Wait Strategy

### 1. Element Finding (`findElement()`)

**Strategy:** Dynamic wait with 30s timeout
- Uses `WebDriverWait.until(ExpectedConditions.presenceOfElementLocated())`
- Proceeds immediately when element is found
- Falls back through multiple strategies (accessibility ID → text → XPath)

### 2. Action Execution

**Tap Actions:**
- Finds element (dynamic wait)
- Clicks immediately
- Auto-waits for stability if action triggers reload (dynamic wait for loading indicators)

**TypeText Actions:**
- Finds element (dynamic wait)
- Waits for element to be focused/ready (dynamic wait, 500ms max)
- Types immediately when ready
- No fixed sleeps

**WaitFor Actions:**
- Uses `WebDriverWait.until()` with appropriate condition
- Proceeds immediately when condition is met
- No fixed sleeps

### 3. Stability Waits (`waitForStability()`)

**Strategy:** Dynamic wait for loading indicators to disappear
- Waits up to 10s (configurable)
- Proceeds immediately when loading indicators disappear
- No fixed sleep after loading disappears

### 4. LoadingComplete Condition

**Strategy:** Multi-stage dynamic wait
1. Wait for loading indicators to disappear (dynamic)
2. Wait for UI to be interactive (dynamic, 2s max)
3. Proceeds immediately when both conditions are met
4. No fixed sleeps

## Timeout Guidelines

### Element Finding
- **Default:** 30 seconds
- **Rationale:** Allows leniency for slow networks or complex UI loads
- **Behavior:** Proceeds immediately when found

### Stability Waits
- **Default:** 10 seconds
- **Rationale:** Network operations may take time, but usually complete faster
- **Behavior:** Proceeds immediately when loading disappears

### LoadingComplete
- **Default:** As specified in WaitFor action (typically 15-25s for network operations)
- **Rationale:** Network operations need more time, but proceed as soon as ready
- **Behavior:** Proceeds immediately when loading disappears and UI is interactive

### Focus/Input Waits
- **Default:** 300-500ms maximum
- **Rationale:** UI focus is usually instant, but allows for Compose TextField initialization
- **Behavior:** Proceeds immediately when element is ready

## Best Practices

### ✅ DO
- Use `WaitFor` actions with appropriate timeouts
- Rely on framework's automatic stability waits
- Use dynamic waits (WebDriverWait) for all conditions
- Set reasonable timeouts (not too short, not unnecessarily long)

### ❌ DON'T
- Use fixed `Thread.sleep()` unless absolutely necessary
- Set timeouts too short (causes flakiness)
- Set timeouts unnecessarily long (slows tests)
- Assume fixed wait times will work across all scenarios

## Agent Instructions

When planning test actions:

1. **Always add WaitFor after actions that trigger reloads:**
   - Authentication (Sign In/Up) → WaitFor LoadingComplete (20-30s timeout)
   - Save/Submit actions → WaitFor LoadingComplete (15-25s timeout)
   - Navigation → WaitFor ScreenChanged or ElementVisible (5-10s timeout)

2. **Trust the framework's dynamic waits:**
   - The framework automatically waits for stability after taps that trigger reloads
   - Element finding already uses dynamic waits
   - No need to add fixed delays

3. **Use appropriate timeouts:**
   - Network operations: 15-30 seconds
   - Screen transitions: 5-15 seconds
   - Simple UI updates: 5-10 seconds

4. **Proceed as soon as ready:**
   - The framework proceeds immediately when conditions are met
   - Timeouts provide leniency, not fixed blocking
   - Tests will run as fast as possible while remaining reliable



