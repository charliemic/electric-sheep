# Developer Workflow: Template Generation

## Overview

When creating new UI features, templates for pattern recognition are automatically generated from your code. This ensures test automation stays in sync with the app UI.

## Normal Development Flow

### 1. Create UI Feature (Normal Development)

Add icons, buttons, or indicators as usual:

```kotlin
// In your screen/composable
Icon(
    imageVector = Icons.Default.Settings,
    contentDescription = "Settings",
    modifier = Modifier.size(24.dp)
)
```

### 2. Build/Test (Automatic Template Generation)

Run your normal build/test commands:

```bash
# Templates automatically generated before tests
./gradlew test

# Or explicitly generate templates
./gradlew generateTemplates
```

**What happens:**
- `IconExtractor` scans your code for icon usage
- `TemplateRenderer` generates template images
- Templates saved to `test-automation/src/main/resources/templates/`
- `PatternRecognizer` automatically uses them

### 3. No Extra Steps Required

Templates are automatically:
- ✅ Generated from your code
- ✅ Available for pattern recognition
- ✅ Updated when you change icons

## Workflow Integration

### Local Development

**Option 1: On-Demand (Recommended)**
```bash
# Generate templates when needed
./gradlew generateTemplates
```

**Option 2: Pre-Build Hook**
Add to your `build.gradle.kts`:
```kotlin
tasks.named("test") {
    dependsOn("generateTemplates")
}
```

### CI/CD Integration

**GitHub Actions / CI Pipeline:**
```yaml
- name: Generate Templates
  run: ./gradlew generateTemplates

- name: Run Tests
  run: ./gradlew test
```

**Benefits:**
- Templates always match current code
- No manual template maintenance
- Tests use latest UI elements

## Template Naming Convention

Templates are automatically named based on icon type and size:

- **Material Icons**: `material_icon_<name>_<size>dp.png`
  - Example: `material_icon_settings_24dp.png`
- **Custom Drawables**: `drawable_<name>.png`
  - Example: `drawable_ic_electric_sheep_logo.png`
- **Loading Indicators**: `loading_spinner_<size>dp.png`
  - Example: `loading_spinner_16dp.png`
- **Error States**: `error_<name>_<size>dp.png`
  - Example: `error_alert_24dp.png`

## What Gets Extracted

### Material Icons
```kotlin
Icon(imageVector = Icons.Default.Settings, ...)  // ✅ Extracted
Icon(imageVector = Icons.Default.Flag, ...)     // ✅ Extracted
```

### Custom Drawables
```kotlin
Image(painter = painterResource(R.drawable.ic_logo), ...)  // ✅ Extracted
```

### Loading Indicators
```kotlin
CircularProgressIndicator(...)  // ✅ Extracted
```

### Error States
```kotlin
// Any icon used in error context
Icon(imageVector = Icons.Default.Error, ...)  // ✅ Extracted
```

## Manual Override

If you need custom templates not derived from code:

1. **Add to templates directory:**
   ```
   test-automation/src/main/resources/templates/custom_template.png
   ```

2. **System merges:**
   - Auto-generated templates
   - Manual templates
   - Both available to `PatternRecognizer`

## Troubleshooting

### Templates Not Generated

**Check:**
1. Icon usage syntax is correct
2. Source paths are correct
3. Output directory is writable

**Debug:**
```bash
./gradlew generateTemplates --info
```

### Templates Not Found

**Check:**
1. Templates directory exists: `test-automation/src/main/resources/templates/`
2. `PatternRecognizer` is initialized with template directory:
   ```kotlin
   PatternRecognizer(templateDir = File("src/main/resources/templates"))
   ```

### Icon Not Detected

**Common issues:**
- Icon size not specified (uses default)
- Icon in commented code (not extracted)
- Icon in test code (not extracted by default)

**Solution:**
- Specify size explicitly: `.size(24.dp)`
- Or manually add template if needed

## Best Practices

1. **Use Standard Sizes**: 16dp, 20dp, 24dp for consistency
2. **Provide Content Descriptions**: Helps with accessibility and template naming
3. **Use Material Icons**: Better template generation support
4. **Commit Templates** (Optional): Can commit generated templates for version control
5. **Generate Before Tests**: Ensures templates are up-to-date

## Future Enhancements

- **Smart Selection**: Only generate templates for icons in critical paths
- **Multi-Theme**: Generate templates for light/dark themes
- **Animation Support**: Extract keyframes from animated indicators
- **Template Optimization**: Compress and optimize templates for pattern matching

