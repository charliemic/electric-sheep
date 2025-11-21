# Neurodiversity-Inclusive and Autistic-Aware Design Principles

## Overview

This document extends our accessibility principles beyond WCAG compliance to include neurodiversity-inclusive design, with a focus on autistic-aware design principles. These principles benefit all users, not just those with specific neurodivergent conditions, by creating more predictable, customizable, and cognitively accessible interfaces.

**Note:** This document focuses specifically on neurodiversity. For a comprehensive guide covering all disability types (visual, hearing, motor, cognitive, speech, neurological, mental health, chronic conditions, age-related, and situational limitations), see `docs/architecture/COMPREHENSIVE_ACCESSIBILITY_GUIDE.md`.

## Core Principles

### 1. Sensory Considerations

**Key Concepts:**
- Reduce sensory overload through minimalistic design
- Provide user control over sensory elements
- Avoid unexpected sensory input

**Implementation Strategies:**

#### Visual Sensory Control
- **Minimalistic Layouts**: Clean, uncluttered interfaces with plenty of white space
- **Color Choices**: Prefer neutral or pastel colors; avoid high-arousal colors (bright yellows, flashing elements)
- **Animation Control**: Allow users to disable or reduce animations
- **Visual Complexity**: Limit the number of visual elements on screen at once

#### Auditory Sensory Control
- **No Auto-Playing Media**: Never auto-play videos, sounds, or music
- **Sound Control**: Provide toggle for sound effects and notifications
- **Haptic Feedback Control**: Allow users to disable haptic feedback

#### Customizable Sensory Settings
- Provide a dedicated "Sensory Preferences" section in settings
- Allow granular control over:
  - Animation speed (off, slow, normal, fast)
  - Sound effects (on/off, volume)
  - Haptic feedback (on/off, intensity)
  - Color scheme (high contrast, reduced motion, custom)
  - Visual density (compact, normal, spacious)

### 2. Predictability and Structure

**Key Concepts:**
- Consistent navigation patterns
- Predictable interface behavior
- Clear visual hierarchy
- Structured information presentation

**Implementation Strategies:**

#### Consistent Navigation
- **Uniform Layouts**: Maintain same navigation structure across all screens
- **Consistent Placement**: Keep primary actions in same locations
- **Predictable Patterns**: Use familiar interaction patterns (swipe, tap, long-press)
- **No Surprises**: Avoid unexpected navigation changes or redirects

#### Visual Structure
- **Clear Hierarchy**: Use consistent typography scale and spacing
- **Grouped Information**: Related information grouped together visually
- **Visual Schedules**: Progress indicators, timelines, and step counters
- **Consistent Feedback**: Immediate, clear feedback for all actions

#### Change Management
- **Version Notices**: Clearly communicate UI changes
- **Migration Support**: Help users adapt to changes with tooltips or guided tours
- **Stable Defaults**: Don't change user preferences without explicit consent

### 3. Clear and Literal Communication

**Key Concepts:**
- Plain language over jargon
- Literal language over metaphors
- Explicit instructions
- Clear error messages

**Implementation Strategies:**

#### Language Guidelines
- **Plain Language**: Use straightforward, unambiguous language
- **Avoid Idioms**: Don't use metaphors, idioms, or cultural references
- **Be Explicit**: State things directly rather than implying
- **Short Sentences**: Break complex information into digestible chunks

#### Instructions and Guidance
- **Step-by-Step**: Provide clear, numbered steps for multi-step processes
- **Contextual Help**: Inline help text and tooltips
- **Progress Indicators**: Show users where they are in a process
- **Error Recovery**: Clear instructions on how to fix errors

#### Content Structure
- **Bullet Points**: Use lists for multiple items
- **Headings**: Clear section headings with consistent hierarchy
- **Visual Separation**: Use spacing and dividers to separate content sections

### 4. Executive Functioning Support

**Key Concepts:**
- Task management tools
- Memory aids
- Decision support
- Flexible interaction options

**Implementation Strategies:**

#### Task Management
- **Checklists**: Break complex tasks into checklists
- **Progress Tracking**: Visual progress indicators
- **Reminders**: Optional reminders for important actions
- **Task Breakdown**: Show subtasks for complex operations

#### Memory Support
- **History**: Show recent actions and history
- **Saved States**: Remember user progress
- **Bookmarks/Favorites**: Allow users to save important items
- **Search History**: Remember recent searches

#### Decision Support
- **Defaults**: Provide sensible defaults
- **Recommendations**: Suggest options when appropriate
- **Comparison Tools**: Help users compare options
- **Undo/Redo**: Allow users to reverse actions

#### Flexible Interaction
- **Multiple Input Methods**: Support touch, voice, keyboard, gestures
- **Customizable Shortcuts**: Allow users to create shortcuts
- **Alternative Paths**: Provide multiple ways to accomplish tasks

### 5. Customization and Control

**Key Concepts:**
- User-controlled interfaces
- Personalized experiences
- Optional features
- Flexible workflows

**Implementation Strategies:**

#### Interface Customization
- **Text Size**: Adjustable font sizes (beyond system settings)
- **Color Schemes**: Multiple color themes (light, dark, high contrast, custom)
- **Layout Density**: Compact, normal, or spacious layouts
- **Information Density**: Show/hide optional information

#### Feature Control
- **Optional Features**: Make social features optional (read receipts, typing indicators)
- **Feature Toggles**: Allow users to enable/disable features
- **Notification Control**: Granular notification settings
- **Privacy Controls**: Clear privacy settings and controls

#### Workflow Flexibility
- **Skip Options**: Allow users to skip optional steps
- **Save for Later**: Save incomplete work
- **Multiple Paths**: Different ways to accomplish same goal
- **Customizable Shortcuts**: User-defined shortcuts and gestures

### 6. Cognitive Load Reduction

**Key Concepts:**
- Minimize decision points
- Reduce information overload
- Progressive disclosure
- Focus support

**Implementation Strategies:**

#### Information Architecture
- **Progressive Disclosure**: Show essential first, details on demand
- **Chunking**: Break information into manageable chunks
- **Prioritization**: Most important information most prominent
- **Filtering**: Allow users to filter and focus on relevant information

#### Focus Support
- **Distraction Reduction**: Minimize non-essential elements
- **Focus Mode**: Optional focus mode that hides distractions
- **Single Task Focus**: One primary task per screen
- **Clear Affordances**: Make it obvious what's interactive

#### Decision Simplification
- **Fewer Choices**: Limit options to essential choices
- **Smart Defaults**: Provide sensible defaults
- **Recommendations**: Suggest best options
- **Comparison Tools**: Help users make informed decisions

## Implementation Recommendations

### Phase 1: Foundation (High Priority)

#### 1. Sensory Preferences Settings
Create a comprehensive sensory preferences screen:

```kotlin
// New component: SensoryPreferencesScreen
// Features:
// - Animation control (off, slow, normal)
// - Sound effects toggle
// - Haptic feedback toggle
// - Color scheme selection
// - Visual density selection
```

**Implementation Steps:**
1. Create `SensoryPreferences` data class
2. Add to `UserPreferences` or `SettingsRepository`
3. Create `SensoryPreferencesScreen` composable
4. Apply preferences throughout app (animation, sound, haptic)

#### 2. Animation Control System
Implement system-wide animation control:

```kotlin
// New utility: AnimationController
// - Respects user preferences
// - Provides animation speed modifiers
// - Can disable animations entirely
```

**Implementation Steps:**
1. Create `AnimationPreferences` utility
2. Add animation speed modifiers to theme
3. Update all animated components to respect preferences
4. Test with animations disabled

#### 3. Plain Language Guidelines
Establish and document language guidelines:

**Language Checklist:**
- [ ] No idioms or metaphors
- [ ] Short, clear sentences
- [ ] Explicit instructions
- [ ] Literal language only
- [ ] Clear error messages

**Implementation Steps:**
1. Create language guidelines document
2. Review all user-facing text
3. Update error messages to be more explicit
4. Add tooltips with plain language explanations

### Phase 2: Enhanced Features (Medium Priority)

#### 4. Task Management Components
Add task breakdown and progress tracking:

```kotlin
// New components:
// - TaskChecklist: Break tasks into checkboxes
// - ProgressIndicator: Visual progress tracking
// - StepIndicator: Multi-step process visualization
```

**Implementation Steps:**
1. Create `TaskChecklist` component
2. Create `ProgressIndicator` component
3. Add to complex workflows (onboarding, setup, etc.)
4. Test with multi-step processes

#### 5. Customizable Interface
Add interface customization options:

```kotlin
// New features:
// - Adjustable text size (beyond system)
// - Layout density options
// - Information density toggles
// - Custom color schemes
```

**Implementation Steps:**
1. Add text size multiplier to theme
2. Create layout density variants
3. Add information density toggles
4. Create custom color scheme builder

#### 6. Enhanced Error Handling
Improve error messages and recovery:

**Error Message Guidelines:**
- State what went wrong in plain language
- Explain why it happened (if helpful)
- Provide clear steps to fix
- Offer alternative paths if available

**Implementation Steps:**
1. Review all error messages
2. Add recovery suggestions
3. Provide alternative paths
4. Test error scenarios

### Phase 3: Advanced Features (Lower Priority)

#### 7. Focus Mode
Implement distraction reduction mode:

```kotlin
// New feature: FocusMode
// - Hides non-essential UI elements
// - Reduces visual clutter
// - Emphasizes primary task
```

#### 8. Alternative Input Methods
Support multiple interaction methods:

```kotlin
// New features:
// - Voice commands (if applicable)
// - Keyboard shortcuts
// - Gesture alternatives
// - Switch control support
```

#### 9. Cognitive Load Indicators
Help users understand complexity:

```kotlin
// New feature: ComplexityIndicator
// - Shows estimated time to complete
// - Indicates number of steps
// - Warns about complex operations
```

## Component Enhancements

### Enhanced AccessibleButton

Add sensory-aware features:

```kotlin
@Composable
fun AccessibleButton(
    // ... existing parameters ...
    reduceMotion: Boolean = false, // Respect animation preferences
    hapticFeedback: Boolean = true, // Respect haptic preferences
    // ... rest of implementation ...
)
```

### Enhanced AccessibleTextField

Add cognitive support:

```kotlin
@Composable
fun AccessibleTextField(
    // ... existing parameters ...
    showCharacterCount: Boolean = false, // Optional character count
    showValidationHints: Boolean = true, // Inline validation hints
    stepByStepGuidance: String? = null, // Optional step-by-step help
    // ... rest of implementation ...
)
```

### New Component: TaskChecklist

```kotlin
@Composable
fun TaskChecklist(
    tasks: List<TaskItem>,
    onTaskComplete: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // Visual checklist with progress tracking
    // Supports screen readers
    // Shows completion percentage
}
```

### New Component: ProgressIndicator

```kotlin
@Composable
fun ProgressIndicator(
    currentStep: Int,
    totalSteps: Int,
    stepLabels: List<String>? = null,
    modifier: Modifier = Modifier
) {
    // Visual progress indicator
    // Shows current step and total
    // Optional step labels
    // Screen reader accessible
}
```

## Settings Architecture

### SensoryPreferences Data Class

```kotlin
data class SensoryPreferences(
    val animationSpeed: AnimationSpeed = AnimationSpeed.Normal,
    val soundEffectsEnabled: Boolean = true,
    val hapticFeedbackEnabled: Boolean = true,
    val colorScheme: ColorSchemePreference = ColorSchemePreference.System,
    val visualDensity: VisualDensity = VisualDensity.Normal,
    val reduceMotion: Boolean = false
)

enum class AnimationSpeed {
    Off, Slow, Normal, Fast
}

enum class ColorSchemePreference {
    System, Light, Dark, HighContrast, Custom
}

enum class VisualDensity {
    Compact, Normal, Spacious
}
```

### Settings Repository Extension

```kotlin
interface SettingsRepository {
    // ... existing methods ...
    
    suspend fun getSensoryPreferences(): SensoryPreferences
    suspend fun saveSensoryPreferences(preferences: SensoryPreferences)
    
    fun observeSensoryPreferences(): Flow<SensoryPreferences>
}
```

## Testing Considerations

### Neurodivergent User Testing

**Involve neurodivergent users in:**
- Design phase (co-design sessions)
- User testing (usability testing)
- Feedback collection (ongoing feedback)

**Testing Checklist:**
- [ ] Test with animations disabled
- [ ] Test with high contrast mode
- [ ] Test with reduced motion
- [ ] Test with screen reader
- [ ] Test with different text sizes
- [ ] Test with different cognitive loads
- [ ] Test error recovery scenarios

### Accessibility Testing Tools

**Beyond TalkBack:**
- Test with different assistive technologies
- Test with different input methods
- Test with different cognitive loads
- Test with different sensory preferences

## Documentation Updates

### Update Accessibility Rules

Add neurodiversity principles to `.cursor/rules/accessibility.mdc`:
- Sensory preferences section
- Cognitive accessibility guidelines
- Plain language requirements
- Predictability requirements

### Update UX Principles

Add to `docs/architecture/UX_PRINCIPLES.md`:
- Neurodiversity-inclusive design section
- Sensory considerations
- Cognitive load reduction
- Predictability and structure

### Create Design Guidelines

Create `docs/architecture/NEURODIVERSITY_DESIGN_GUIDELINES.md`:
- Detailed language guidelines
- Sensory design patterns
- Cognitive accessibility patterns
- Component usage examples

## Success Metrics

### Quantitative Metrics
- User preference adoption rates
- Error recovery success rates
- Task completion rates
- Time to complete tasks

### Qualitative Metrics
- User feedback from neurodivergent users
- Accessibility audit results
- Usability testing feedback
- Co-design session outcomes

## Resources and References

### Research Papers
- "Designing for Neurodivergent Users" (arXiv)
- W3C Cognitive Accessibility Guidelines (COGA)
- Autism-Friendly Visual Design Guide (Aspect)

### Design Resources
- Scope UK: Designing for Autism
- WebAIM: Cognitive Accessibility
- A11y Project: Cognitive Accessibility

### Community Resources
- Neurodivergent design communities
- Accessibility advocacy groups
- User testing panels

## Implementation Priority

### Immediate (Next Sprint)
1. ✅ Sensory preferences settings screen
2. ✅ Animation control system
3. ✅ Plain language review of existing text

### Short Term (Next Month)
4. ✅ Task management components
5. ✅ Enhanced error messages
6. ✅ Interface customization options

### Medium Term (Next Quarter)
7. ✅ Focus mode
8. ✅ Alternative input methods
9. ✅ Cognitive load indicators

### Long Term (Ongoing)
10. ✅ Neurodivergent user testing
11. ✅ Co-design sessions
12. ✅ Continuous improvement based on feedback

## Conclusion

Incorporating neurodiversity-inclusive and autistic-aware design principles creates a more accessible, predictable, and user-friendly experience for all users. These principles extend beyond WCAG compliance to address cognitive accessibility, sensory needs, and executive functioning support.

By implementing these principles incrementally, we can create an app that is truly inclusive and supportive of diverse cognitive profiles, while maintaining our existing accessibility standards and design system consistency.

