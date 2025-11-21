# Screenshot Capture and Linking

## Overview

The test automation framework captures screenshots from multiple sources and links them to actions and intents for comprehensive visual analysis.

## Screenshot Sources

### 1. Screen Monitor (Continuous Observation)

**Location**: `test-results/screenshots/monitor_*.png`

**Frequency**: Every 1 second (configurable)

**Naming Format**: 
```
monitor_<relativeTime>ms_action_<action>_intent_<intent>_<absoluteTimestamp>.png
```

**Examples**:
- `monitor_5971ms_action_Type_Email_field_intent_Sign_up_and_add_mood_1763664130338.png`
- `monitor_14554ms_action_Wait_LoadingComplete_intent_Sign_up_and_add_mood_1763664138921.png`
- `monitor_27669ms_action_Type_Mood_score_fiel_intent_Sign_up_and_add_mood_1763664152036.png`

**Features**:
- ✅ Relative timestamps (from test start)
- ✅ Action context (what action was executing)
- ✅ Intent context (what task/goal)
- ✅ Continuous capture during test execution
- ✅ State change detection

### 2. Action Executor (Action Screenshots)

**Location**: `test-results/screenshots/screenshot_*.png`

**Frequency**: After each action execution

**Naming Format**: 
```
screenshot_<absoluteTimestamp>.png
```

**Examples**:
- `screenshot_1763664130338.png` (after typing email)
- `screenshot_1763664132571.png` (after tapping Create Account)

**Features**:
- ✅ Captured after each action
- ✅ Linked to action results
- ✅ Used for planning and verification

### 3. Screen Evaluator (Final Evaluation)

**Location**: `test-results/screenshots/evaluation_*.png` (if created)

**Frequency**: Once at test completion

**Naming Format**: 
```
evaluation_<absoluteTimestamp>.png
```

**Features**:
- ✅ Final screen state
- ✅ Used for observation generation
- ✅ Includes Cursor analysis prompt

## Timestamping

### Relative Timestamps

Monitor screenshots use **relative timestamps** (milliseconds from test start):
- `monitor_5971ms_...` = 5.971 seconds after test start
- `monitor_14554ms_...` = 14.554 seconds after test start

This makes it easy to:
- Correlate with test execution timeline
- Find screenshots at specific test moments
- Understand sequence of events

### Absolute Timestamps

All screenshots also include absolute timestamp in filename:
- `..._1763664130338.png` = Unix timestamp

This enables:
- Sorting by capture time
- Finding screenshots from specific test runs
- Cross-referencing with logs

## Action and Intent Linking

### Action Context

Monitor screenshots are linked to the action being executed:

**Action Types**:
- `Tap_<target>` - Tapping on an element
- `Type_<target>` - Typing into a field
- `Swipe_<direction>` - Swiping
- `Wait_<condition>` - Waiting for condition
- `Verify_<condition>` - Verifying condition
- `ReportIntent_<status>` - Reporting intent result

**Examples**:
- `action_Type_Email_field` - Typing into email field
- `action_Tap_Create_Account_butto` - Tapping Create Account button
- `action_Wait_LoadingComplete` - Waiting for loading to complete

### Intent Context

Monitor screenshots are linked to the current task/intent:

**Format**: `intent_<task_description>`

**Examples**:
- `intent_Sign_up_and_add_mood` - Task: "Sign up and add mood value"
- `intent_Intent_ACHIEVED_User_signed_up` - Intent achieved with reason

### Intent Report Linking

When `ReportIntent` action is executed, the monitor is updated with:
- Intent status: `ACHIEVED` or `FAILED`
- Intent reason: Brief explanation

This allows screenshots to be linked to test outcomes.

## Screenshot Organization

### By Test Run

All screenshots from a test run are in:
```
test-results/screenshots/
```

### By Type

- **Monitor screenshots**: `monitor_*.png` (continuous observation)
- **Action screenshots**: `screenshot_*.png` (action results)
- **Evaluation screenshots**: `evaluation_*.png` (final state)

### By Context

Screenshots can be filtered by:
- **Action**: `*action_Type_*` (all typing actions)
- **Intent**: `*intent_Sign_up*` (all sign-up related)
- **Time**: `monitor_*ms_*` (relative time range)

## Usage Examples

### Find Screenshots During Specific Action

```bash
# Find all screenshots during "Type Email" action
ls test-automation/test-results/screenshots/*action_Type_Email*

# Find all screenshots during "Wait LoadingComplete"
ls test-automation/test-results/screenshots/*action_Wait_Loading*
```

### Find Screenshots by Intent

```bash
# Find all screenshots for sign-up intent
ls test-automation/test-results/screenshots/*intent_Sign_up*

# Find screenshots when intent was achieved
ls test-automation/test-results/screenshots/*intent_Intent_ACHIEVED*
```

### Find Screenshots by Time

```bash
# Find screenshots in first 10 seconds
ls test-automation/test-results/screenshots/monitor_[0-9]ms_*

# Find screenshots after 20 seconds
ls test-automation/test-results/screenshots/monitor_[2-9][0-9][0-9][0-9][0-9]ms_*
```

### Analyze in Cursor

```bash
# Open latest monitor screenshot with action context
./scripts/analyze-screenshot-in-cursor.sh test-automation/test-results/screenshots/monitor_*action_Type_Email*.png

# Open screenshot from when intent was achieved
./scripts/analyze-screenshot-in-cursor.sh test-automation/test-results/screenshots/*intent_Intent_ACHIEVED*.png
```

## Linking to Test Execution

### Execution History

Each action in `executionHistory` includes:
- Action type
- Result (success/failure)
- Screenshot reference
- Timestamp

### State Changes

Monitor screenshots are linked to state changes:
- State change detected → Screenshot captured
- State includes screenshot reference
- State includes action/intent context

### Intent Reports

When intent is reported:
- Monitor is updated with intent status
- Final screenshot captured with intent context
- Screenshot linked to intent result

## Benefits

1. **Traceability**: Every screenshot linked to action/intent
2. **Timeline**: Relative timestamps show execution sequence
3. **Context**: Action and intent in filename for easy filtering
4. **Analysis**: Easy to find relevant screenshots for Cursor analysis
5. **Debugging**: Can trace back from screenshot to action/intent

## Example Workflow

1. **Test runs** → Screenshots captured with context
2. **Test completes** → Screenshots organized by action/intent
3. **Find issue** → Filter screenshots by action/intent
4. **Analyze in Cursor** → Open relevant screenshots
5. **Review observations** → Link back to actions/intents

## File Naming Breakdown

Example: `monitor_5971ms_action_Type_Email_field_intent_Sign_up_and_add_mood_1763664130338.png`

- `monitor_` - Source: Screen Monitor
- `5971ms` - Relative time: 5.971 seconds from test start
- `action_Type_Email_field` - Action context: Typing into email field
- `intent_Sign_up_and_add_mood` - Intent context: Task goal
- `1763664130338` - Absolute timestamp: Unix timestamp
- `.png` - Format: PNG image

This naming makes it easy to:
- Understand when screenshot was captured
- Know what action was executing
- Know what intent/goal was active
- Sort and filter screenshots
- Correlate with test logs



