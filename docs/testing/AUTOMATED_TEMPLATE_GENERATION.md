# Automated Template Generation Architecture

## Overview

This system automatically extracts UI elements (icons, indicators, buttons) from the app codebase and generates template images for pattern recognition. This ensures test automation templates stay in sync with the app UI without manual maintenance.

## Architecture

### Components

1. **IconExtractor** - Scans codebase for icon usage
   - Material Icons (`Icons.Default.*`)
   - Custom drawables (XML vector drawables)
   - Loading indicators (`CircularProgressIndicator`)
   - Error states (error icons, error containers)

2. **TemplateRenderer** - Renders icons to template images
   - Renders Material Icons at different sizes
   - Renders custom drawables
   - Generates loading spinner templates
   - Generates error state templates

3. **TemplateGenerator** - Orchestrates extraction and generation
   - Runs IconExtractor to find all icons
   - Runs TemplateRenderer for each icon
   - Saves templates to `test-automation/src/main/resources/templates/`
   - Updates template manifest

4. **Gradle Integration** - Automated workflow
   - `generateTemplates` task runs before tests
   - Pre-build hook for CI/CD
   - Dev workflow integration

## Dev Workflow

### Creating a New UI Feature

1. **Add UI Elements** (normal development)
   ```kotlin
   Icon(imageVector = Icons.Default.Settings, ...)
   ```

2. **Build/Test** (automatic)
   ```bash
   ./gradlew generateTemplates build test
   ```
   - Templates automatically generated
   - No manual template creation needed

3. **Templates Available** (automatic)
   - Templates in `test-automation/src/main/resources/templates/`
   - PatternRecognizer automatically uses them

### Workflow Integration

**Local Development:**
- Templates generated on-demand via Gradle task
- Optional: Pre-commit hook to ensure templates are up-to-date

**CI/CD:**
- Templates generated before test execution
- Ensures tests always use latest UI templates

**Manual Override:**
- Developers can manually add templates if needed
- System merges manual + auto-generated templates

## Implementation Details

### Icon Extraction Strategy

1. **Material Icons** - Parse Kotlin files for `Icons.Default.*`
2. **Custom Drawables** - Scan `res/drawable/` for XML vector drawables
3. **Loading Indicators** - Detect `CircularProgressIndicator` usage
4. **Error States** - Detect error container colors and error icons

### Template Rendering

1. **Material Icons** - Render at standard sizes (16dp, 20dp, 24dp)
2. **Custom Drawables** - Render as-is from XML
3. **Loading Spinners** - Generate animated frame sequence (or static representative)
4. **Error States** - Generate error icon + container combinations

### Template Naming Convention

- Material Icons: `material_icon_<name>_<size>dp.png`
  - Example: `material_icon_settings_24dp.png`
- Custom Drawables: `drawable_<name>.png`
  - Example: `drawable_ic_electric_sheep_logo.png`
- Loading Indicators: `loading_spinner_<size>dp.png`
- Error States: `error_<type>_<size>dp.png`

## Benefits

1. **Zero Manual Maintenance** - Templates auto-update with code changes
2. **Always In Sync** - Templates match current UI exactly
3. **Developer Friendly** - No extra steps for developers
4. **CI/CD Ready** - Automatic generation in pipelines
5. **Version Controlled** - Templates can be committed or generated on-demand

## Future Enhancements

1. **Smart Template Selection** - Only generate templates for icons actually used in critical paths
2. **Template Optimization** - Compress templates, optimize for pattern matching
3. **Multi-Theme Support** - Generate templates for light/dark themes
4. **Animation Support** - Extract keyframes from animated indicators

