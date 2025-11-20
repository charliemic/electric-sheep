# Icon and Logo Design Principles

## Overview
This document establishes design principles for icons and logos in the Electric Sheep app, ensuring consistency, calm aesthetics, and a subtle, non-intrusive brand identity.

## Core Principles

### 1. Consistency
- **Unified Identity**: All icons and logos should share the same design language
- **Consistent Elements**: Use the same core elements (ES monogram, color scheme, shape) across all contexts
- **Scalable Design**: Works at all sizes from launcher icon (48dp) to large screen displays

### 2. Simplicity & Clarity
- **Minimal Elements**: Avoid clutter - use only essential elements
- **Clear Recognition**: Instantly recognizable at small sizes
- **Clean Lines**: Simple geometric shapes, no unnecessary details

### 3. Calm & Non-Intrusive
- **Subtle Identity**: Brand presence without being overwhelming
- **Soft Aesthetics**: Rounded shapes, gentle colors, comfortable spacing
- **Professional**: Clean, modern, trustworthy appearance

### 4. Accessibility
- **High Contrast**: Meets WCAG AA contrast requirements
- **Readable at Small Sizes**: Clear even at 24dp icon size
- **Color Independence**: Works in monochrome (for accessibility settings)

## Design System

### Core Identity: ES Monogram

**The "ES" monogram is the primary brand identifier:**
- **Letters**: "E" and "S" in a clean, modern sans-serif
- **Style**: Bold, geometric, slightly rounded
- **Usage**: App launcher, landing page, loading states

**Design Specifications:**
- **Font**: System font (Roboto) - matches typography system
- **Weight**: Bold (700) for prominence
- **Case**: Uppercase ("ES")
- **Spacing**: Letters slightly closer together (tight kerning) for cohesive monogram feel

### Color Scheme

**Primary Colors:**
- **Background**: `primaryContainer` (light teal)
- **Foreground**: `onPrimaryContainer` (dark teal)
- **Alternative**: Can use `primary` / `onPrimary` for high-contrast contexts

**Usage Rules:**
- Launcher icon: `primaryContainer` background with `onPrimaryContainer` text
- Landing page logo: `primaryContainer` background with `onPrimaryContainer` text
- Loading indicator: `primaryContainer` background with `onPrimaryContainer` text
- All use theme colors for consistency and accessibility

### Shape System

**Circle Container:**
- **Shape**: Perfect circle
- **Usage**: Contains the ES monogram
- **Purpose**: Creates cohesive, contained identity
- **Sizing**: Proportional to content (60-80dp for logos, 48dp for launcher icon)

**Why Circle:**
- Soft, calming shape
- Works well at all sizes
- Modern, friendly appearance
- Non-aggressive, approachable

## Implementation Guidelines

### App Launcher Icon

**Specifications:**
- **Size**: 48dp (Android standard)
- **Shape**: Adaptive icon (circle on most launchers)
- **Background**: `primaryContainer` color
- **Foreground**: "ES" monogram in `onPrimaryContainer`
- **Style**: Bold, centered, proportional

**Design:**
```xml
- Background: primaryContainer (light teal circle)
- Foreground: "ES" text in onPrimaryContainer (dark teal)
- Size: 48dp viewport
- Safe zone: 36dp (75% of icon) for ES monogram
```

### Landing Page Logo

**Specifications:**
- **Size**: 80dp container
- **Shape**: Circle
- **Background**: `primaryContainer`
- **Text**: "ES" in `headlineLarge` style, Bold
- **Color**: `onPrimaryContainer`

**Usage:**
- Centered on landing screen
- Above app name
- Consistent with launcher icon design

### Loading Indicator Logo

**Specifications:**
- **Size**: 60dp container (smaller, more subtle)
- **Shape**: Circle
- **Background**: `primaryContainer`
- **Text**: "ES" in `titleLarge` style, Bold
- **Color**: `onPrimaryContainer`
- **Context**: Appears during initialization

### Icon Usage (UI Icons)

**Material Icons:**
- Use Material Design icons for UI elements
- **Size**: 20-24dp for standard icons
- **Color**: `onSurface` or `onSurfaceVariant` (context-dependent)
- **Style**: Outlined or filled based on context

**Examples:**
- Navigation: `Icons.Default.ArrowBack` (24dp)
- Settings: `Icons.Default.Settings` (20dp)
- Feature flags: `Icons.Default.Flag` (20dp)

**Consistency Rules:**
- All UI icons use Material Design icon set
- Same size for similar contexts (e.g., all card icons are 20dp)
- Color follows theme (never hardcoded)

## Size Hierarchy

### Large (80dp)
- **Use**: Landing page logo
- **Context**: Main brand presence
- **Visibility**: High - primary brand element

### Medium (60dp)
- **Use**: Loading indicator
- **Context**: Transient states
- **Visibility**: Medium - functional, not primary

### Small (48dp)
- **Use**: App launcher icon
- **Context**: System launcher
- **Visibility**: High - first brand impression

### UI Icons (20-24dp)
- **Use**: Navigation, actions, indicators
- **Context**: Functional UI elements
- **Visibility**: Medium - supporting elements

## Design Rationale

### Why ES Monogram?
1. **Simple**: Two letters, instantly recognizable
2. **Memorable**: Easy to remember and recall
3. **Scalable**: Works from 24dp to 200dp
4. **Timeless**: Not tied to trends, will age well
5. **Professional**: Clean, modern, trustworthy

### Why Circle Container?
1. **Calm**: Soft, rounded shape feels approachable
2. **Contained**: Creates clear boundary and focus
3. **Versatile**: Works in any context
4. **Modern**: Contemporary design aesthetic
5. **Non-Intrusive**: Doesn't dominate the interface

### Why System Font?
1. **Consistency**: Matches app typography
2. **Accessibility**: Optimized for screen reading
3. **Performance**: No custom font loading
4. **Familiarity**: Users recognize system fonts
5. **Maintainability**: No font file management

## Consistency Checklist

When creating or updating icons/logos:

- [ ] Uses ES monogram (not other symbols)
- [ ] Uses `primaryContainer` / `onPrimaryContainer` colors
- [ ] Uses circle container shape
- [ ] Uses system font (Roboto)
- [ ] Uses Bold font weight
- [ ] Properly sized for context
- [ ] High contrast (WCAG AA compliant)
- [ ] Works in both light and dark themes
- [ ] Scalable to different sizes
- [ ] Accessible (screen reader descriptions)

## Examples

### ✅ Good: Consistent Design
- Launcher icon: ES in circle, primaryContainer background
- Landing logo: ES in circle, primaryContainer background
- Loading logo: ES in circle, primaryContainer background
- All use same colors, same font, same style

### ❌ Bad: Inconsistent Design
- Launcher icon: Plus symbol
- Landing logo: ES monogram
- Loading logo: Different symbol
- Mixed colors, different styles

## Accessibility Considerations

1. **Contrast**: All logo/icon combinations meet WCAG AA (4.5:1)
2. **Size**: Minimum 48dp for launcher icon (Android requirement)
3. **Content Description**: All logos have descriptive content descriptions
4. **Color Independence**: Design works in grayscale (for color-blind users)
5. **Screen Readers**: Proper semantic roles (Role.Image)

## Future Considerations

### Potential Enhancements (Not Current)
- Subtle animation on loading (already implemented with pulsing)
- Dark mode specific adjustments (handled by theme)
- Custom icon for specific contexts (not needed - ES monogram works everywhere)

### What We Avoid
- Complex illustrations (too cluttered)
- Multiple colors (inconsistent)
- Trendy designs (won't age well)
- Custom fonts (maintenance burden)
- Decorative elements (unnecessary clutter)

