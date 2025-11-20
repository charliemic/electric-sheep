# UX Design Principles

## Overview
This document establishes design principles and standards for the Electric Sheep app, prioritizing ease of use, accessibility, task-orientation, and maintainability.

## Core Principles

### 1. Ease of Use
- **Task-Oriented Design**: Every screen should have a clear primary task
- **Progressive Disclosure**: Show essential information first, details on demand
- **Minimal Cognitive Load**: Reduce decision points, use familiar patterns
- **Clear Affordances**: Buttons look clickable, inputs look editable

### 2. Accessibility
- **WCAG AA Compliance**: All text/background combinations meet 4.5:1 contrast ratio
- **Touch Targets**: Minimum 48dp × 48dp for all interactive elements
- **Screen Reader Support**: All interactive elements have descriptive content descriptions
- **Color Independence**: Information never conveyed by color alone
- **Keyboard Navigation**: All interactive elements are keyboard accessible

### 3. Task-Orientation
- **Primary Action First**: Most important action is most prominent
- **Clear Information Hierarchy**: Headings, body text, and metadata are visually distinct
- **Contextual Actions**: Actions appear where they're needed
- **Feedback on Actions**: Loading states, success/error messages are clear

### 4. Maintainability
- **Reusable Components**: Common UI patterns extracted into reusable composables
- **Consistent Spacing**: Standard spacing scale used throughout
- **Theme-Based Styling**: All colors, typography, and shapes from MaterialTheme
- **Documentation**: Complex components have clear documentation

## Design Standards

### Spacing Scale
Use the following spacing values consistently:

```kotlin
object Spacing {
    val xs = 4.dp    // Tight spacing (icon to text)
    val sm = 8.dp    // Small spacing (between related elements)
    val md = 16.dp   // Medium spacing (standard padding, gaps)
    val lg = 24.dp   // Large spacing (section separation)
    val xl = 32.dp   // Extra large (major section separation)
    val xxl = 48.dp  // Screen edge padding
}
```

**Usage Guidelines:**
- Card padding: `md` (16.dp)
- Section spacing: `lg` (24.dp)
- Screen edge padding: `md` (16.dp)
- Element gaps: `sm` (8.dp) or `md` (16.dp)
- Icon to text: `xs` (4.dp)

### Button Standards

#### Primary Button
- **Use**: Main action on screen (e.g., "Sign In", "Save Mood")
- **Style**: `Button` (filled)
- **Width**: `fillMaxWidth()` for full-width, or fixed width for centered actions
- **Height**: Default Material 3 (minimum 40dp, typically 48dp)
- **Touch Target**: Minimum 48dp × 48dp
- **Loading State**: Show `CircularProgressIndicator` (16dp) with descriptive state

#### Secondary Button
- **Use**: Alternative actions (e.g., "Sign in with email")
- **Style**: `OutlinedButton`
- **Width**: Same as primary button on same screen
- **Height**: Same as primary button

#### Text Button
- **Use**: Tertiary actions, links, toggles
- **Style**: `TextButton`
- **Width**: Content-based or `fillMaxWidth()` for full-width
- **Height**: Default Material 3

**Button Consistency Rules:**
- All buttons on the same screen should have consistent width
- Primary action should be visually most prominent
- Disabled state should be clearly indicated
- Loading state should replace text, not appear alongside

### Text Field Standards

#### Standard Text Field
- **Style**: `OutlinedTextField`
- **Width**: 
  - Full width: `fillMaxWidth()` for forms
  - Centered: `fillMaxWidth(0.85f)` for single inputs on centered screens
- **Label**: Always present, descriptive
- **Placeholder**: Helpful example text
- **Supporting Text**: Show hints or errors
- **Error State**: Use `isError` and `supportingText` for error messages
- **Keyboard Options**: Appropriate `KeyboardType` and `ImeAction`

**Text Field Consistency Rules:**
- All fields in a form should have same width
- Error messages should be clear and actionable
- Validation feedback should be immediate when possible

### Card Standards

#### Standard Card
- **Elevation**: 
  - Default: `2.dp` (standard content cards)
  - High: `4.dp` (interactive cards, primary content)
  - Low: `1.dp` (subtle containers, info cards)
- **Padding**: `md` (16.dp) standard, `lg` (20.dp) for cards with more content
- **Shape**: `RoundedCornerShape(16.dp)` for standard cards
- **Colors**: Use `MaterialTheme.colorScheme.surface` or appropriate container colors

**Card Usage:**
- **Primary Content Cards**: Elevation 4.dp, padding 20.dp (e.g., mood input card)
- **Standard Cards**: Elevation 2.dp, padding 16.dp (e.g., utility cards, info cards)
- **Subtle Cards**: Elevation 1.dp, padding 16.dp (e.g., user info, empty states)

### Typography Standards

Use Material 3 typography scale consistently:

- **Headlines**: `headlineLarge`, `headlineMedium`, `headlineSmall`
  - Use for screen titles, major headings
- **Titles**: `titleLarge`, `titleMedium`, `titleSmall`
  - Use for section headings, card titles
- **Body**: `bodyLarge`, `bodyMedium`, `bodySmall`
  - Use for primary content, descriptions
- **Labels**: `labelLarge`, `labelMedium`, `labelSmall`
  - Use for buttons, labels, metadata

**Typography Consistency:**
- Screen titles: `headlineMedium` with `FontWeight.Bold`
- Section headings: `titleLarge` with `FontWeight.Bold`
- Card titles: `titleLarge` with `FontWeight.Bold`
- Body text: `bodyMedium` for primary, `bodySmall` for secondary
- Metadata: `bodySmall` with `onSurfaceVariant` color

### Color Usage Standards

All colors must come from `MaterialTheme.colorScheme`:

- **Primary Actions**: `primary` / `onPrimary`
- **Primary Containers**: `primaryContainer` / `onPrimaryContainer`
- **Secondary Actions**: `secondary` / `onSecondary`
- **Error States**: `error` / `onError`, `errorContainer` / `onErrorContainer`
- **Text Colors**: 
  - Primary: `onSurface`
  - Secondary: `onSurfaceVariant`
  - Disabled: `onSurfaceVariant` with reduced opacity (0.6f)

**Color Consistency Rules:**
- Never hardcode colors (except in theme definition)
- Use semantic color names (primary, error, etc.)
- Maintain sufficient contrast (WCAG AA minimum)

### Icon Standards

- **Size**: 
  - Small: 16dp (inline with small text)
  - Medium: 20dp (standard, with body text)
  - Large: 24dp (standalone, prominent)
- **Color**: Use theme colors, typically `onSurface` or `onSurfaceVariant`
- **Content Description**: Always provide for accessibility (null only if decorative and parent has description)

### Loading States

- **Indicator Size**: 16dp for inline indicators
- **Color**: `onPrimary` for buttons, `primary` for standalone
- **State Description**: Always provide semantic state description
- **Text Replacement**: Replace button text with indicator, don't show both

### Error Handling

- **Error Messages**: 
  - Clear and actionable
  - Use `error` color
  - Display near the relevant input/action
  - Include semantic error annotation
- **Error States**: 
  - Text fields: `isError = true` with `supportingText`
  - Buttons: Disable if validation fails
  - Cards: Use error container color if needed

### Screen Layout Standards

#### Standard Screen Structure
1. **Top Bar**: Navigation, title, primary actions
2. **Content Area**: 
   - Primary content first (task-oriented)
   - Secondary information below
   - Lists/collections at bottom
3. **Padding**: 
   - Screen edges: `md` (16.dp)
   - Section separation: `lg` (24.dp) or `xl` (32.dp)

#### Content Flow
- **Primary Task First**: Most important action/input at top
- **Progressive Disclosure**: Collapsible sections for secondary options
- **Clear Hierarchy**: Visual weight matches importance
- **Consistent Alignment**: Center for focused tasks, start for lists

### Accessibility Checklist

For every interactive element:
- [ ] Minimum 48dp × 48dp touch target
- [ ] Descriptive content description
- [ ] Appropriate semantic role
- [ ] State description for dynamic states
- [ ] Error annotation for error states
- [ ] Keyboard accessible
- [ ] Sufficient color contrast

### Component Reusability

Common patterns should be extracted into reusable components:

- **StandardButton**: Consistent button with loading state
- **StandardTextField**: Consistent text field with error handling
- **StandardCard**: Consistent card with standard elevation/padding
- **LoadingIndicator**: Consistent loading indicator
- **ErrorMessage**: Consistent error message display

## Implementation Guidelines

### Creating New Screens

1. **Define Primary Task**: What is the user trying to accomplish?
2. **Layout Structure**: Use standard screen structure
3. **Component Selection**: Use standard components from design system
4. **Spacing**: Use spacing scale consistently
5. **Accessibility**: Add all required semantic annotations
6. **Testing**: Test with screen reader, verify touch targets

### Modifying Existing Screens

1. **Check Consistency**: Does it follow spacing/typography/color standards?
2. **Verify Accessibility**: Are all interactive elements accessible?
3. **Maintain Patterns**: Use existing component patterns
4. **Update Documentation**: Update this document if creating new patterns

## Examples

### Good: Consistent Button Group
```kotlin
Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
    Button(
        onClick = { /* primary action */ },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Primary Action")
    }
    OutlinedButton(
        onClick = { /* secondary action */ },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Secondary Action")
    }
}
```

### Good: Consistent Form
```kotlin
Column(
    verticalArrangement = Arrangement.spacedBy(Spacing.md)
) {
    OutlinedTextField(
        value = email,
        onValueChange = { /* ... */ },
        label = { Text("Email") },
        modifier = Modifier.fillMaxWidth(),
        // ... other props
    )
    OutlinedTextField(
        value = password,
        onValueChange = { /* ... */ },
        label = { Text("Password") },
        modifier = Modifier.fillMaxWidth(),
        // ... other props
    )
}
```

### Bad: Inconsistent Spacing
```kotlin
// Don't mix different spacing values without reason
Column {
    Text("Title")
    Spacer(Modifier.height(8.dp))  // Why 8?
    Text("Subtitle")
    Spacer(Modifier.height(24.dp))  // Why 24?
    Text("Content")
}
```

### Bad: Inconsistent Button Widths
```kotlin
// Don't mix button widths on same screen
Button(modifier = Modifier.fillMaxWidth(0.8f)) { /* ... */ }
OutlinedButton(modifier = Modifier.fillMaxWidth()) { /* ... */ }
```

## Review Checklist

Before submitting UI changes:
- [ ] Spacing uses standard scale
- [ ] Buttons have consistent width on same screen
- [ ] Text fields have consistent width in same form
- [ ] Cards use standard elevation/padding
- [ ] Typography follows hierarchy
- [ ] Colors come from theme
- [ ] All interactive elements have 48dp touch targets
- [ ] All interactive elements have content descriptions
- [ ] Error states are clear and actionable
- [ ] Loading states are consistent

