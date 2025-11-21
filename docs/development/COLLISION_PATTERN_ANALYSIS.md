# Collision Pattern Analysis

**Date**: 2025-11-20  
**Purpose**: Identify all file patterns that are collision-prone and require worktree isolation

## Analysis Methodology

1. **Codebase structure analysis** - Identified directories and file types
2. **Historical collisions** - Reviewed actual collision reports
3. **Shared resource patterns** - Files that multiple agents commonly modify
4. **Dependency patterns** - Files that other files depend on

## Collision-Prone Patterns Identified

### 1. Screen Files (HIGH RISK)
**Pattern**: `app/.../ui/screens/**/*Screen.kt`

**Why collision-prone**:
- Multiple agents add features to existing screens
- UI changes often affect multiple screens
- Navigation changes require screen modifications
- Example: `MoodManagementScreen.kt` - two agents modified simultaneously

**Files**:
- `LandingScreen.kt`
- `MoodManagementScreen.kt` ⚠️ **ACTUAL COLLISION**
- `TriviaScreen.kt`
- Any new screen files

### 2. ViewModel Files (HIGH RISK)
**Pattern**: `app/.../ui/screens/**/*ViewModel.kt`

**Why collision-prone**:
- Multiple agents add business logic
- State management changes affect multiple features
- Test data loading, sync logic, etc. added by different agents

**Files**:
- `MoodManagementViewModel.kt`
- Any ViewModel files

### 3. Navigation Files (HIGH RISK)
**Pattern**: `app/.../ui/navigation/*.kt`

**Why collision-prone**:
- Adding new screens requires navigation changes
- Multiple agents add routes simultaneously
- Central routing file - single point of modification

**Files**:
- `NavGraph.kt` - Contains all route definitions

### 4. Component Files (MEDIUM-HIGH RISK)
**Pattern**: `app/.../ui/components/*.kt`

**Why collision-prone**:
- Shared UI components used across screens
- Multiple agents extend/improve components
- Design system changes affect all components

**Files**:
- `AccessibleScreen.kt`
- `AccessibleButton.kt`
- `AccessibleCard.kt`
- `AccessibleTextField.kt`
- `MoodChart.kt`
- Any shared component

### 5. Application Class (HIGH RISK)
**Pattern**: `app/.../*Application.kt`

**Why collision-prone**:
- Central initialization point
- Multiple agents add dependencies
- DI setup, service initialization

**Files**:
- `ElectricSheepApplication.kt` - Already in shared list

### 6. Module Files (HIGH RISK)
**Pattern**: `app/.../**/*Module.kt`

**Why collision-prone**:
- Dependency injection setup
- Multiple agents add providers
- Shared across entire app

**Files**:
- `DataModule.kt` - Already in shared list
- `AuthModule.kt`
- Any DI module files

### 7. Repository Files (MEDIUM RISK)
**Pattern**: `app/.../data/repository/*Repository.kt`

**Why collision-prone**:
- Data access layer
- Multiple agents add query methods
- Test data extensions added by different agents

**Files**:
- `MoodRepository.kt`
- Any repository files

### 8. Manager/Provider Files (MEDIUM-HIGH RISK)
**Pattern**: `app/.../**/*Manager.kt`, `app/.../**/*Provider.kt`

**Why collision-prone**:
- Shared services
- Multiple agents extend functionality
- Configuration changes

**Files**:
- `UserManager.kt`
- `FeatureFlagManager.kt`
- `SyncManager.kt`
- `EnvironmentManager.kt`
- `FeatureFlagProvider.kt` and implementations

### 9. Theme Files (MEDIUM RISK)
**Pattern**: `app/.../ui/theme/*.kt`

**Why collision-prone**:
- Shared styling
- Design system changes
- Multiple agents modify colors, typography, spacing

**Files**:
- `Theme.kt`
- `Color.kt`
- `Type.kt`
- `Spacing.kt`

### 10. Config Files (MEDIUM RISK)
**Pattern**: `app/.../config/*.kt`

**Why collision-prone**:
- Shared configuration
- Feature flags, environment settings
- Multiple agents add config options

**Files**:
- `FeatureFlag.kt`
- `MoodConfig.kt`
- `EnvironmentManager.kt`

### 11. Build Files (HIGH RISK - Already Identified)
**Pattern**: `**/*.gradle.kts`, `gradle.properties`, `settings.gradle.kts`

**Why collision-prone**:
- Dependency management
- Build configuration
- Multiple agents add dependencies

**Files**:
- `app/build.gradle.kts`
- `build.gradle.kts`
- `gradle.properties`
- `settings.gradle.kts`

### 12. Factory Files (MEDIUM RISK)
**Pattern**: `app/.../**/*Factory.kt`

**Why collision-prone**:
- ViewModel factory creation
- Dependency injection for ViewModels
- Multiple agents add ViewModels

**Files**:
- `MoodManagementViewModelFactory.kt`

### 13. MainActivity (MEDIUM RISK)
**Pattern**: `app/.../MainActivity.kt`

**Why collision-prone**:
- App entry point
- Navigation setup
- Deep link handling

**Files**:
- `MainActivity.kt`

## Risk Assessment

### HIGH RISK (MUST use worktree)
- Screen files (`*Screen.kt`)
- ViewModel files (`*ViewModel.kt`)
- Navigation files (`NavGraph.kt`)
- Application class (`*Application.kt`)
- Module files (`*Module.kt`)
- Build files (`.gradle.kts`, `gradle.properties`)

### MEDIUM-HIGH RISK (Should use worktree)
- Component files (`ui/components/*.kt`)
- Manager/Provider files (`*Manager.kt`, `*Provider.kt`)

### MEDIUM RISK (Consider worktree)
- Repository files (`*Repository.kt`)
- Theme files (`ui/theme/*.kt`)
- Config files (`config/*.kt`)
- Factory files (`*Factory.kt`)
- MainActivity

## Pattern Recommendations

### Comprehensive Pattern List

```markdown
**Shared files (REQUIRE worktree isolation):**

**HIGH RISK - MUST use worktree:**
- `app/.../ui/screens/**/*Screen.kt` (ALL screen files)
- `app/.../ui/screens/**/*ViewModel.kt` (ALL ViewModel files)
- `app/.../ui/navigation/*.kt` (Navigation files)
- `app/.../*Application.kt` (Application class)
- `app/.../**/*Module.kt` (DI module files)
- `**/*.gradle.kts` (Build files)
- `gradle.properties` (Gradle properties)
- `settings.gradle.kts` (Settings file)

**MEDIUM-HIGH RISK - Should use worktree:**
- `app/.../ui/components/*.kt` (Shared UI components)
- `app/.../**/*Manager.kt` (Manager classes)
- `app/.../**/*Provider.kt` (Provider classes)

**MEDIUM RISK - Consider worktree:**
- `app/.../data/repository/*Repository.kt` (Repository files)
- `app/.../ui/theme/*.kt` (Theme files)
- `app/.../config/*.kt` (Config files)
- `app/.../**/*Factory.kt` (Factory files)
- `app/.../MainActivity.kt` (Main activity)
```

## Implementation

Update `.cursor/rules/branching.mdc` with comprehensive patterns to prevent future collisions.



