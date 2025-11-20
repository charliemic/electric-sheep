# Typography System

## Overview
The Electric Sheep app uses a comprehensive typography system based on Material Design 3, optimized for calm, accessible reading experiences.

## Design Principles

### 1. Calm & Accessible
- **Font Family**: System font (Roboto on Android)
  - Roboto is specifically designed for screen reading
  - Optimized for legibility and calm visual appearance
  - System font ensures optimal rendering and performance
  - No custom fonts needed - reduces app size and improves compatibility

### 2. Rational Hierarchy
- Clear size progression following Material Design 3 scale
- Consistent spacing with optimized line heights
- Visual weight matches content importance

### 3. Accessibility First
- Minimum 16sp for body text (WCAG recommendation)
- Proper line heights (1.5x font size) for comfortable reading
- Sufficient letter spacing for clarity
- High contrast text colors

## Typography Scale

### Headlines (Major Titles)
- **headlineLarge** (32sp, Bold)
  - Use: App name, major branding elements
  - Example: "Electric Sheep" on landing screen
  
- **headlineMedium** (28sp, Bold)
  - Use: Screen titles, primary headings
  - Example: "Mood Management", "Sign In"
  
- **headlineSmall** (24sp, Bold)
  - Use: Section headings within screens
  - Example: "Recent Moods", "Today's Entry"

### Titles (Section Headings)
- **titleLarge** (22sp, Bold)
  - Use: Card titles, important labels
  - Example: "Mood Management" card title, "Today's Mood"
  
- **titleMedium** (16sp, Medium)
  - Use: Subsection headings, secondary labels
  - Example: "Personal Utilities" subtitle
  
- **titleSmall** (14sp, Medium)
  - Use: Minor headings, tertiary labels
  - Example: Form section labels

### Body Text (Content)
- **bodyLarge** (16sp, Normal)
  - Use: Primary content, important descriptions
  - Example: Main content paragraphs, primary descriptions
  
- **bodyMedium** (14sp, Normal)
  - Use: Secondary content, standard descriptions
  - Example: Card descriptions, form help text
  
- **bodySmall** (12sp, Normal)
  - Use: Metadata, captions, tertiary information
  - Example: Timestamps, secondary info, error details

### Labels (Buttons & Metadata)
- **labelLarge** (14sp, Medium)
  - Use: Button text, primary labels
  - Example: "Sign in with Google", "Save Mood"
  
- **labelMedium** (12sp, Medium)
  - Use: Small buttons, tags, secondary labels
  - Example: "Cancel", "Edit"
  
- **labelSmall** (11sp, Medium)
  - Use: Tiny labels, metadata tags
  - Example: "STAGING", "PROD" debug indicators

## Capitalization Rules

### Title Case
**Use for:** Headlines, Titles, Button Text, Card Titles

**Rule:** Capitalize the first letter of each major word (nouns, verbs, adjectives, adverbs). Don't capitalize articles (a, an, the), prepositions (in, on, at, etc.), or conjunctions (and, or, but) unless they're the first word.

**Examples:**
- ✅ "Mood Management"
- ✅ "Sign In with Google"
- ✅ "Track Your Mood"
- ✅ "Coming Soon"
- ❌ "MOOD MANAGEMENT" (all caps - too aggressive)
- ❌ "mood management" (all lowercase - not prominent enough)

### Sentence Case
**Use for:** Body Text, Descriptions, Error Messages, Labels

**Rule:** Capitalize only the first letter of the first word in a sentence. Use lowercase for the rest, except for proper nouns.

**Examples:**
- ✅ "Track your mood throughout the day and analyse trends"
- ✅ "Sign in with Google to get started"
- ✅ "An error occurred while signing in"
- ✅ "More utilities will be available soon"
- ❌ "Track Your Mood Throughout The Day" (title case in body - too formal)

### Special Cases
- **App Name**: "Electric Sheep" (proper noun, always capitalized)
- **Button Text**: Title Case for primary actions ("Sign In with Google"), Sentence case for secondary actions ("Cancel", "Back")
- **Error Messages**: Always sentence case ("An error occurred", not "An Error Occurred")
- **Debug Labels**: Can use ALL CAPS for small indicators ("STAGING", "PROD") but keep them small and unobtrusive

## Usage Guidelines

### Screen Titles
```kotlin
Text(
    text = "Mood Management",  // Title Case
    style = MaterialTheme.typography.headlineMedium,
    fontWeight = FontWeight.Bold
)
```

### Card Titles
```kotlin
Text(
    text = "Mood Management",  // Title Case
    style = MaterialTheme.typography.titleLarge,
    fontWeight = FontWeight.Bold
)
```

### Body Text
```kotlin
Text(
    text = "Track your mood throughout the day",  // Sentence case
    style = MaterialTheme.typography.bodyMedium
)
```

### Button Text
```kotlin
Button(
    text = "Sign in with Google",  // Title Case for primary
    // or
    text = "Cancel"  // Sentence case for secondary
)
```

### Error Messages
```kotlin
Text(
    text = "An error occurred while signing in",  // Sentence case
    style = MaterialTheme.typography.bodySmall,
    color = MaterialTheme.colorScheme.error
)
```

## Font Weights

- **Bold** (700): Headlines, titles, emphasis
- **Medium** (500): Labels, button text, secondary headings
- **Normal** (400): Body text, descriptions

**Rule:** Use Bold sparingly for emphasis. Most text should be Normal weight for calm, readable appearance.

## Line Heights

All line heights are optimized for readability:
- Headlines: 1.25x font size (tighter, more compact)
- Titles: 1.27x font size
- Body: 1.5x font size (comfortable reading)
- Labels: 1.14-1.45x font size (compact but readable)

## Letter Spacing

- **Headlines**: 0sp (tight, cohesive)
- **Titles**: 0-0.15sp (slightly open)
- **Body**: 0.25-0.5sp (comfortable reading)
- **Labels**: 0.1-0.5sp (clear but compact)

## Accessibility Considerations

1. **Minimum Size**: Never use text smaller than 12sp (bodySmall)
2. **Contrast**: All text meets WCAG AA contrast ratios (4.5:1)
3. **Line Height**: Minimum 1.4x font size for body text
4. **Letter Spacing**: Sufficient spacing for clarity (0.25-0.5sp for body)
5. **Font Weight**: Avoid very light weights (< 400) for accessibility

## Examples by Context

### Landing Screen
- App Name: `headlineLarge`, Bold, Title Case
- Subtitle: `titleMedium`, Medium, Sentence Case
- Card Title: `titleLarge`, Bold, Title Case
- Card Description: `bodyMedium`, Normal, Sentence Case

### Mood Management Screen
- Screen Title: `headlineMedium`, Bold, Title Case
- Section Heading: `headlineSmall`, Bold, Title Case
- Input Label: `titleSmall`, Medium, Title Case
- Body Text: `bodyMedium`, Normal, Sentence Case
- Button: `labelLarge`, Medium, Title Case
- Error: `bodySmall`, Normal, Sentence Case

### Loading States
- Loading Message: `bodyLarge`, Normal, Sentence Case
- Logo Text: `titleLarge`, Bold, Title Case (for monogram)

## Migration Notes

When updating existing text:
1. Check capitalization matches the context (Title Case vs Sentence Case)
2. Verify font size is appropriate for the content type
3. Ensure font weight is correct (Bold for emphasis, Normal for body)
4. Confirm line height and letter spacing are comfortable

