# AI Agent Guidelines

This document provides guidance for AI agents working on this codebase. Follow these principles and practices to ensure consistent, high-quality contributions.

## Table of Contents
- [General Principles](#general-principles)
- [Code Quality](#code-quality)
- [Testing](#testing)
- [CI/CD](#cicd)
- [Security](#security)
- [Documentation](#documentation)
- [Git Workflow](#git-workflow)
- [Error Handling](#error-handling)
- [Performance](#performance)
- [Accessibility](#accessibility)

## General Principles

### Code Style
- **Consistency First**: Follow existing code patterns and conventions in the codebase
- **Readability**: Write code that is self-documenting and easy to understand
- **DRY (Don't Repeat Yourself)**: Avoid duplication; extract common functionality
- **SOLID Principles**: Apply object-oriented design principles where applicable
- **UK English**: Use UK English spellings throughout the codebase
  - Use "analyse" not "analyze"
  - Use "colour" in user-facing text (though "color" is acceptable in technical contexts like variable names)
  - Use "organise" not "organize"
  - Use "centre" in user-facing text (though "center" is acceptable in technical contexts like Compose alignment)
  - Use "recognise" not "recognize"
  - Use "optimise" not "optimize"
  - Use "realise" not "realize"
- **Language-Specific**: Adhere to Kotlin Multiplatform style guides and best practices
  - Follow the [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
  - Use `expect`/`actual` declarations for platform-specific implementations
  - Keep shared code platform-agnostic when possible
  - Organise code by source sets (commonMain, androidMain, iosMain, etc.)
  - Prefer common code over platform-specific implementations

### Before Making Changes
1. **Understand Context**: Read existing code and understand the architecture before modifying
2. **Check Dependencies**: Verify compatibility with existing dependencies and versions
3. **Review Related Code**: Look for similar implementations to maintain consistency
4. **Consider Impact**: Assess how changes affect other parts of the system

### When Implementing Features
- **Start Small**: Implement minimal viable functionality first, then iterate
- **Preserve Functionality**: Ensure existing features continue to work
- **Add Tests**: Write tests alongside implementation, not after
- **Keep Tests Passing**: **CRITICAL** - Always ensure all tests pass before moving on to the next change
  - Run tests frequently during development (`./gradlew test`)
  - Fix failing tests immediately - do not accumulate test failures
  - If a change breaks existing tests, fix the tests as part of the same change
  - Never leave tests in a failing state - this creates technical debt and makes refactoring harder later
  - If you must temporarily break tests, document why and create a follow-up task to fix them immediately
- **Add Logging**: Implement appropriate logging for each feature with correct log levels
- **Update Documentation**: Keep documentation in sync with code changes
- **Consider Accessibility**: Always implement accessibility features alongside UI components
  - Add content descriptions to all interactive elements
  - Provide semantic roles for custom components
  - Announce state changes (loading, errors, success) to screen readers
  - Ensure touch targets meet minimum 48dp size
  - Test with TalkBack screen reader
  - See [Accessibility](#accessibility) section for detailed guidelines
- **Use ViewModel Pattern**: For any screen with business logic, use ViewModel
  - Extract state management, user actions, and repository interactions to ViewModel
  - UI should only observe ViewModel state and send events
  - Makes UI testable and follows Android architecture best practices
  - See [ViewModel Best Practices](#viewmodel-best-practices) for details
- **Consider Authentication**: Always consider user authentication and data scoping when implementing new features
  - Check if feature needs user authentication (use `UserManager.requireUserId()` or `getUserIdOrNull()`)
  - Scope data to current user (add `userId` to data models, filter queries by `userId`)
  - Verify user ownership before allowing data access/modification
  - Handle unauthenticated states gracefully (show sign-in UI or disable features)
  - See [Authentication Architecture](../docs/architecture/AUTHENTICATION.md) for details
- **Handle Browser Requirements**: When implementing OAuth or web-based features
  - **Always use Chrome Custom Tabs** for OAuth flows (Android best practice)
  - Check if Custom Tabs is available before opening URLs
  - Provide fallback to regular browser if Custom Tabs unavailable
  - Never use WebView for OAuth (security concerns, not recommended)
  - See [OAuth Implementation Guide](../docs/development/OAUTH_IMPLEMENTATION.md) for details
  - See [Google OAuth Setup](../docs/development/GOOGLE_OAUTH_SETUP.md) for OAuth configuration
- **Consider Database Migrations**: When implementing features that change data models or schema
  - Add a migration if adding/modifying/removing database columns or tables
  - Update database version in `AppDatabase.kt`
  - Add migration to `DatabaseMigrations.getMigrations()`
  - Test migration on both fresh and existing databases
  - Follow Room migration best practices (no raw Cursor, trust Room's migration system)
  - Use [Migration Checklist](../docs/architecture/MIGRATION_CHECKLIST.md) to ensure completeness
  - See [Data Layer Architecture](../docs/architecture/DATA_LAYER_ARCHITECTURE.md) for migration details
- **Use Library Functionality First**: Before implementing custom functionality, check if the library/tool provides it
  - Review library documentation and APIs before writing custom code
  - Prefer using built-in features over implementing your own version
  - Examples: Room migrations (don't use raw Cursor), WorkManager constraints (don't implement custom scheduling), etc.
  - Only implement custom solutions when library doesn't provide the needed functionality
  - Document why custom implementation was chosen if library functionality exists
  - See [Error Conversion Strategies](../docs/architecture/ERROR_CONVERSION_STRATEGIES.md) for error handling patterns
- **Track Development Metrics**: Update development metrics after significant changes
  - Run `./scripts/metrics/capture-all-metrics.sh` to capture complexity, accessibility, and other metrics
  - Store user prompts in `development-metrics/prompts/` using `./scripts/metrics/capture-prompt.sh "prompt text"`
  - Build and test metrics are automatically captured when running `./gradlew build` or `./gradlew test` via wrapper scripts
  - See [Development Metrics](#development-metrics) section for details
- **Push and Verify**: After making changes, push to a feature branch, create a PR, and verify CI pipeline passes (see [Git Workflow](#workflow-push-changes-and-create-pr))

## Code Quality

### Code Review Checklist
- [ ] Code follows project style guidelines
- [ ] No hardcoded values (use constants or configuration)
- [ ] Proper error handling implemented
- [ ] No commented-out code or debug statements
- [ ] Meaningful variable and function names
- [ ] Functions are focused and do one thing well
- [ ] No magic numbers or strings
- [ ] Proper use of design patterns where appropriate
- [ ] Database migrations added for schema changes
- [ ] Library functionality used instead of custom implementations where available

### Refactoring Guidelines
- **Test Coverage**: Ensure adequate test coverage before refactoring
- **All Tests Passing**: **REQUIRED** - All tests must pass before starting refactoring
  - If tests are failing, fix them first before refactoring
  - Refactoring with failing tests makes it impossible to verify behavior is preserved
  - Running tests after refactoring should show no new failures (green tests before and after)
- **Incremental Changes**: Make small, incremental refactoring changes
  - Run tests after each small refactoring step
  - Keep tests green throughout the refactoring process
- **Preserve Behavior**: Maintain existing functionality during refactoring
  - Tests should pass with the same assertions before and after refactoring
  - If tests need to change, it means behavior changed (which may be intentional, but should be explicit)
- **Document Intent**: Explain why refactoring is necessary

## Testing

### Testing Strategy: Hourglass Pattern

We follow an **hourglass pattern** for testing, which emphasizes:
- **Many Unit Tests**: Maximize unit tests for fast execution and tight scoping
- **Thin Middle Layer**: Minimal isolated integration tests
- **Higher-Level Stack Tests**: Prefer testing on as much of the stack as practical for higher-level tests

This pattern provides fast feedback through extensive unit tests while ensuring real-world behavior through comprehensive stack-level tests.

### Test Requirements

#### Unit Tests (Wide Base)
- **Maximize Coverage**: Write unit tests for all business logic, utility functions, and pure functions
- **Fast Execution**: Unit tests should run in milliseconds; aim for thousands of tests
- **Tight Scoping**: Test individual functions, classes, or small components in isolation
- **Mock External Dependencies**: Mock APIs, databases, file systems, and other external services
- **Test Coverage**: Aim for >80% code coverage through unit tests
- **Test Logging**: Write unit tests for logging utilities to verify API structure and method signatures

#### Integration Tests (Narrow Middle)
- **Minimal Use**: Use sparingly for testing component interactions that can't be tested at unit or stack level
- **Specific Purpose**: Only when testing interactions between a few tightly-coupled components
- **Avoid Overuse**: Prefer unit tests or higher-level stack tests over isolated integration tests

#### Higher-Level Stack Tests (Wide Top)
- **Test Full Stack**: Prefer testing on as much of the stack as practical (UI → ViewModel → Repository → Data Source)
- **Real Components**: Use real implementations where possible (in-memory databases, test servers, etc.)
- **End-to-End Scenarios**: Test complete user flows and business scenarios
- **Practical Scope**: Test as much of the stack as is practical without making tests brittle or slow
- **Real-World Behavior**: Ensure tests reflect actual runtime behavior and interactions

### Test Best Practices
- **AAA Pattern**: Structure tests with Arrange, Act, Assert
- **Isolation**: Tests should be independent and not rely on execution order
- **Fast Unit Tests**: Keep unit tests fast; avoid slow I/O operations
- **Realistic Stack Tests**: Use real implementations in stack-level tests to catch integration issues
- **Edge Cases**: Test boundary conditions, null values, and error scenarios
- **Deterministic**: Tests should produce consistent results
- **Test Naming**: Use descriptive test names that explain what is being tested
- **Test Organization**: Group related tests logically (by feature or component)

### Keeping Tests Passing (CRITICAL)
- **Run Tests Frequently**: Execute tests after every significant change (`./gradlew test`)
  - Don't wait until the end of a feature to run tests
  - Catch test failures early when the cause is still fresh in mind
  - Fast feedback loop prevents accumulation of test debt
- **Fix Tests Immediately**: When a change breaks tests, fix them as part of the same change
  - Do not defer test fixes - they become harder to fix later
  - If tests fail due to a change, update the tests to match the new behavior
  - If tests fail due to a bug, fix the bug, not the test
- **Never Commit Failing Tests**: All tests must pass before committing changes
  - Failing tests in the repository create confusion and technical debt
  - Other developers may assume tests are reliable and waste time debugging
  - CI/CD will catch failures, but it's better to catch them locally first
- **Update Tests with Code Changes**: When modifying code behavior, update tests accordingly
  - If you change a function signature, update all tests that use it
  - If you change validation logic, update tests to reflect new validation rules
  - If you refactor code, ensure tests still accurately test the behavior
- **Test-Driven Development**: Consider writing tests first (TDD) for complex logic
  - Write a failing test that describes desired behavior
  - Implement code to make the test pass
  - Refactor while keeping tests green
- **Evaluate Test Suite Regularly**: Periodically review and improve the test suite
  - Remove obsolete tests
  - Consolidate duplicate tests
  - Improve test clarity and maintainability
  - But always ensure tests pass before and after evaluation

### Test Examples

#### Unit Test Example (Fast, Tight Scoping)
```kotlin
// Good: Unit test with tight scoping and mocked dependencies
@Test
fun `should return error when user input is invalid`() {
    // Arrange
    val validator = InputValidator()
    val invalidInput = ""
    
    // Act
    val result = validator.validate(invalidInput)
    
    // Assert
    assertTrue(result.isError)
    assertEquals("Input cannot be empty", result.errorMessage)
}

@Test
fun `should calculate total price correctly`() {
    // Arrange
    val calculator = PriceCalculator()
    val items = listOf(Item(price = 10.0), Item(price = 20.0))
    
    // Act
    val total = calculator.calculateTotal(items)
    
    // Assert
    assertEquals(30.0, total)
}
```

#### Higher-Level Stack Test Example (Full Stack, Real Components)
```kotlin
// Good: Stack-level test testing UI → ViewModel → Repository → DataSource
@Test
fun `should display user profile after successful login`() = runTest {
    // Arrange - Use real implementations where practical
    val testDatabase = createInMemoryDatabase()
    val apiService = FakeApiService() // Test double with real behavior
    val repository = UserRepository(apiService, testDatabase)
    val viewModel = UserViewModel(repository)
    val uiState = viewModel.uiState.test()
    
    // Act - Test full stack interaction
    viewModel.login("user@example.com", "password")
    
    // Assert - Verify end-to-end behavior
    val states = uiState.getValues()
    assertTrue(states.any { it is UserUiState.Success })
    val successState = states.last() as UserUiState.Success
    assertEquals("user@example.com", successState.user.email)
}
```

### When to Skip Tests
- Only skip tests for trivial code (getters/setters, simple constants)
- Document why a test is skipped if necessary
- Prefer testing over skipping

## CI/CD

### CI/CD Platform Options for Android
### Github Actions

### Continuous Integration
- **Runs on All Pushes**: CI pipeline automatically runs on every push to any branch
- **Automated Testing**: All tests must pass before merging
- **Linting**: Code must pass linting/static analysis checks (ktlint, detekt)
- **Build Verification**: Ensure code compiles and builds successfully (debug and release builds)
- **Fast Feedback**: Keep CI pipeline fast (<15 minutes for Android builds is reasonable)
- **Android-Specific**: Test on multiple API levels when possible
- **Branch Protection**: PRs cannot be merged if CI checks fail (enforced via branch protection rules)

### Android Pipeline Stages
1. **Lint/Format Check**: Verify code style (ktlint, detekt) and formatting
2. **Unit Tests**: Run all unit tests (JVM tests, fast execution)
3. **Instrumented Tests**: Run Android instrumented tests on emulators (slower, run on key branches)
4. **Build Debug APK**: Compile and build debug APK for verification
5. **Build Release AAB**: Build release Android App Bundle (AAB) for distribution
6. **Security Scan**: Run security vulnerability scans (dependency checks)
7. **Code Quality**: Static analysis (SonarQube, CodeQL, etc.)
8. **Distribution**: Deploy to Firebase App Distribution, Play Store Internal Testing, or TestFlight

### Android CI Configuration Best Practices
- **Gradle Caching**: Cache Gradle dependencies and build cache to speed up builds
- **Parallel Execution**: Run independent tests in parallel
- **Fail Fast**: Stop pipeline on first failure when appropriate
- **Environment Variables**: Use secure environment variable management for:
  - Signing keys (keystore files, passwords)
  - API keys
  - Play Store service account credentials
- **Artifact Management**: Store APK/AAB artifacts for debugging and distribution
- **Emulator Management**: Use pre-warmed emulators or containers for instrumented tests
- **Build Variants**: Test both debug and release builds
- **API Level Testing**: Test on multiple Android API levels (minimum, target, latest)

### Android Build Tools
- **Gradle**: Primary build system for Android
- **Fastlane**: Automation tool for building, testing, and deploying Android apps
  - Integrates with any CI/CD platform
  - Handles Play Store uploads, screenshots, changelogs
  - Recommended for production workflows
- **Gradle Wrapper**: Always use Gradle wrapper for consistent builds across environments

### Signing and Security
- **Keystore Management**: Never commit keystore files or passwords
  - Store keystores in secure storage (GitHub Secrets, CI/CD secrets)
  - Use separate keystores for debug and release
- **Signing Configuration**: Configure signing in `build.gradle.kts` using environment variables
- **ProGuard/R8**: Enable code obfuscation and minification for release builds

### Deployment

#### Distribution Channels
1. **Firebase App Distribution**: Quick distribution to testers (recommended for early stages)
2. **Google Play Internal Testing**: Internal testing track in Play Console
3. **Google Play Alpha/Beta**: Staged rollout to testers
4. **Google Play Production**: Full production release

#### Versioning
- **Version Code**: Increment for each release (integer, required by Play Store)
- **Version Name**: Use semantic versioning (MAJOR.MINOR.PATCH) for user-facing version
- **Auto-Increment**: Consider automating version code increment in CI/CD

#### Release Process
- **Release Notes**: Document changes in release notes (Play Store description, changelog)
- **Rollback Plan**: Have a rollback strategy (Play Store allows staged rollbacks)
- **Feature Flags**: Use feature flags for gradual rollouts when possible
- **Staged Rollouts**: Use Play Store staged rollouts (1% → 10% → 50% → 100%)

### Branch Protection Rules

To enforce that PRs cannot be merged if tests fail, configure branch protection rules:

**GitHub Branch Protection Setup:**
1. Navigate to repository Settings → Branches
2. Add rule for `main` branch (and `develop` if applicable)
3. Enable the following settings:
   - ✅ **Require a pull request before merging**
   - ✅ **Require status checks to pass before merging**
     - Required status checks: `build` and `ci-status`
   - ✅ **Require branches to be up to date before merging**
   - ✅ **Do not allow bypassing the above settings**

**Required Status Checks:**
- `build` - Main CI pipeline (lint, test, build)
- `ci-status` - Final status verification

**Result:** PRs will be blocked from merging until all CI checks pass. The merge button will be disabled until all required status checks are green.

### Local Development → CI/CD Progression
1. **Local Builds**: Start with local Gradle builds (`./gradlew build`)
2. **Local Testing**: Run tests locally (`./gradlew test`, `./gradlew connectedAndroidTest`)
3. **Basic CI**: Add simple CI pipeline (lint, unit tests, build)
4. **Enhanced CI**: Add instrumented tests, security scans, code quality checks
5. **Distribution**: Add automated distribution to Firebase App Distribution
6. **Play Store**: Add automated Play Store uploads (internal testing track)
7. **Full Pipeline**: Complete pipeline with staging → production workflow

## Security

### Security Principles
- **Input Validation**: Always validate and sanitize user input
- **Authentication**: Implement proper authentication mechanisms
  - Always verify user is authenticated before accessing user-scoped data
  - Use `UserManager.requireUserId()` or check `UserManager.isAuthenticated` before operations
  - Handle unauthenticated states gracefully (show sign-in UI, disable features)
  - Never trust client-provided user IDs; always use current authenticated user's ID
- **Authorization**: Verify user permissions before sensitive operations
  - Verify data ownership before allowing access/modification (check `userId` matches current user)
  - Filter all queries by `userId` to prevent cross-user data access
  - Validate `userId` in repository methods before database operations
- **Secrets Management**: Never commit secrets, API keys, or credentials
- **Dependency Updates**: Keep dependencies updated to patch vulnerabilities

### Common Security Practices
- **SQL Injection**: Use parameterized queries or ORM frameworks
- **XSS Prevention**: Sanitize output to prevent cross-site scripting
- **CSRF Protection**: Implement CSRF tokens for state-changing operations
- **HTTPS**: Use HTTPS for all network communications
- **Error Messages**: Don't expose sensitive information in error messages

### Security Checklist
- [ ] No hardcoded secrets or credentials
- [ ] Input validation implemented
- [ ] User authentication verified before accessing user-scoped data
- [ ] Data ownership verified (userId matches current user) before operations
- [ ] All queries filter by userId to prevent cross-user data access
- [ ] Unauthenticated states handled gracefully
- [ ] Output sanitization applied
- [ ] Dependencies are up-to-date
- [ ] Security headers configured
- [ ] Error handling doesn't leak sensitive info

## Documentation

### Code Documentation
- **Function Documentation**: Document public functions with purpose, parameters, and return values
- **Complex Logic**: Add comments explaining non-obvious logic
- **API Documentation**: Document API endpoints, request/response formats
- **Architecture Decisions**: Document significant architectural decisions

### Documentation Standards
- **Keep Updated**: Update documentation when code changes
- **Clear Examples**: Include code examples in documentation
- **User-Focused**: Write documentation from the user's perspective
- **Searchable**: Use clear headings and structure for easy navigation

### When to Document
- **Public APIs**: Always document public-facing APIs
- **Complex Algorithms**: Document complex algorithms or business logic
- **Configuration**: Document configuration options and their effects
- **Setup Instructions**: Keep setup/installation instructions current

## Git Workflow

### Commit Messages
- **Format**: Use conventional commit format when applicable
  - `feat: add user authentication`
  - `fix: resolve memory leak in image loader`
  - `docs: update API documentation`
  - `test: add unit tests for validation logic`
  - `refactor: simplify error handling`
- **Descriptive**: Write clear, descriptive commit messages
- **Atomic**: Make commits atomic (one logical change per commit)

### Branch Strategy
- **Feature Branches**: Create feature branches for new functionality
- **Branch Naming**: Use descriptive branch names (e.g., `feature/user-auth`, `fix/login-bug`)
- **Keep Updated**: Regularly sync with main branch
- **Clean History**: Keep commit history clean and meaningful

### Workflow: Push Changes and Create PR

When implementing changes, follow this workflow to ensure CI/CD verification:

1. **Create Feature Branch**
   ```bash
   git checkout -b feature/your-feature-name
   # or
   git checkout -b fix/your-bug-fix
   ```

2. **Make Changes and Commit**
   - Make your code changes
   - Stage changes: `git add .`
   - Commit with descriptive message: `git commit -m "feat: add new feature"`

3. **Push to Remote Repository**
   ```bash
   git push -u origin feature/your-feature-name
   ```
   - Always push to a feature branch, never directly to `main`
   - Use `-u` flag on first push to set upstream tracking
   - **CI pipeline automatically runs on every push** to any branch

4. **Create Pull Request**
   - Navigate to the repository on GitHub/GitLab
   - Click "New Pull Request" or "Create Merge Request"
   - Select your feature branch as source and `main` (or target branch) as destination
   - Fill out PR description including:
     - What changes were made
     - Why the changes were needed
     - How to test the changes
     - Any breaking changes

5. **Verify Tests Pass Locally**
   - **CRITICAL**: Run tests locally before pushing (`./gradlew test`)
   - Fix any failing tests immediately
   - Do not push code with failing tests - this wastes CI resources and creates confusion
   - All tests must pass locally before creating a PR

6. **Verify CI Pipeline**
   - The CI pipeline automatically runs on:
     - Every push to any branch
     - Every PR creation and update
   - Monitor the pipeline status in the PR checks
   - Ensure all checks pass:
     - ✅ Lint checks
     - ✅ Unit tests
     - ✅ Build (debug APK)
     - ✅ Build (release AAB)
   - **PRs are blocked from merging** if any CI checks fail (enforced by branch protection)
   - If CI fails, fix issues locally, run tests again, then push fixes
   - CI will automatically re-run on each push

### Debugging CI Failures

When CI builds fail, use these methods to access failure information:

#### Using GitHub CLI (Recommended)
If `gh` CLI is installed and authenticated:
```bash
# List recent workflow runs for your branch
gh run list --branch <branch-name>

# View full logs for a specific run
gh run view <run-id> --log

# View logs for latest failed run
gh run list --status failure --limit 1 | head -1 | awk '{print $7}' | xargs gh run view --log

# Watch a running workflow
gh run watch
```

#### Using GitHub Web Interface
1. Navigate to repository → Actions tab
2. Click on the failed workflow run
3. Click on the `build` job
4. Expand each step to see detailed logs:
   - **Debug environment** step shows ANDROID_HOME, JAVA_HOME, PATH
   - Failed steps show full stack traces (--stacktrace flag)
5. Check error messages and stack traces to identify the issue

#### Common CI Issues
- **Android SDK not found**: Check "Debug environment" step for ANDROID_HOME
- **Gradle wrapper issues**: Verify gradlew has execute permissions
- **Lint failures**: Check lint output; configured to not fail on warnings
- **Build failures**: Full stack traces are available in step logs
- **Cache issues**: Caches are automatically managed but can be cleared if needed

#### When Debugging Fails
- Check the "Debug environment" step output for environment variables
- Review full stack traces (--stacktrace is enabled on all Gradle commands)
- Verify Android SDK setup matches requirements (API 34)
- Check if dependencies need updating

6. **Address Review Feedback**
   - Make requested changes
   - Push additional commits to the same branch
   - CI will re-run automatically on each push

7. **Merge After Approval**
   - Wait for code review approval
   - Ensure all CI checks pass
   - Merge the PR (squash merge recommended for clean history)

### Pull Request Guidelines
- **Description**: Provide clear PR description explaining changes
- **Testing**: Describe how changes were tested
- **Breaking Changes**: Clearly mark and document breaking changes
- **Review Ready**: Ensure code is ready for review (tests pass, linting clean)
- **CI Status**: Always verify CI pipeline passes before requesting review
- **Self-Review**: Review your own PR before requesting others' review

## Error Handling

**See [Error Handling Guide](../docs/architecture/ERROR_HANDLING.md) for complete documentation.**

### Error Handling Principles
- **Fail Fast**: Detect errors as early as possible
- **Graceful Degradation**: Handle errors gracefully without crashing
- **User-Friendly Messages**: Provide clear, actionable error messages to users
- **Logging**: Log errors with sufficient context for debugging
- **Error Types**: Use appropriate error types (validation, network, system, etc.)
- **Error Conversion**: Follow [Error Conversion Strategies](../docs/architecture/ERROR_CONVERSION_STRATEGIES.md) when converting between error types

### Error Handling Patterns
```kotlin
// Good: Specific error handling
try {
    val result = apiCall()
    return Success(result)
} catch (e: NetworkException) {
    logger.error("Network error", e)
    return Error("Please check your internet connection")
} catch (e: ValidationException) {
    logger.warn("Validation failed", e)
    return Error("Invalid input: ${e.message}")
} catch (e: Exception) {
    logger.error("Unexpected error", e)
    return Error("An unexpected error occurred")
}
```

### Logging Best Practices

#### Logging Requirements
- **Implement with Features**: Add logging when implementing new features, not as an afterthought
- **Test Logging**: Write tests to verify logging works correctly (see [Testing](#testing))
- **Appropriate Levels**: Choose the correct log level for each situation (see below)
- **Use Logger Utility**: Always use the centralised `Logger` utility (`com.electricsheep.app.util.Logger`)

#### Log Level Guidelines

**VERBOSE** - Very detailed diagnostic information
- Use for: Detailed tracing of execution flow, variable values during development
- Example: `Logger.verbose("ComponentName", "Processing item: $itemId")`
- Typically disabled in production builds

**DEBUG** - Development debugging information
- Use for: Diagnostic information helpful during development, UI state changes, component lifecycle
- Example: `Logger.debug("ScreenName", "Screen displayed")`, `Logger.debug("ViewModel", "State updated: $state")`
- Typically disabled in production builds

**INFO** - General informational messages
- Use for: Important application flow events, user actions, successful operations, navigation events
- Example: `Logger.info("ScreenName", "User navigated to settings")`, `Logger.info("Repository", "Data loaded successfully")`
- Should be enabled in production for monitoring

**WARN** - Warning messages for potential issues
- Use for: Recoverable errors, deprecated API usage, unusual but non-fatal conditions, fallback scenarios
- Example: `Logger.warn("NetworkService", "API call failed, using cached data")`, `Logger.warn("Validation", "Invalid input format, using default")`
- Should be enabled in production

**ERROR** - Error messages for actual problems
- Use for: Exceptions, failures, errors that need attention, unrecoverable conditions
- Example: `Logger.error("DatabaseService", "Failed to save data", exception)`, `Logger.error("ApiClient", "Network request failed", networkException)`
- Always enabled in production

#### Logging Implementation Checklist
- [ ] Log important user actions (INFO level)
- [ ] Log screen/component lifecycle events (DEBUG level)
- [ ] Log errors and exceptions (ERROR level with throwable)
- [ ] Log warnings for unusual conditions (WARN level)
- [ ] Use descriptive tags (component/class name)
- [ ] Include relevant context in messages
- [ ] Never log sensitive data (passwords, tokens, PII)
- [ ] Write tests for logging functionality (unit tests for Logger API, instrumented tests for actual logging)
- [ ] Consider log volume (avoid excessive logging in loops)
- [ ] Review log levels - ensure appropriate level chosen for each log statement

#### Example Logging Implementation
```kotlin
// Good: Appropriate log levels with context
class UserRepository {
    fun loadUser(userId: String): User? {
        Logger.debug("UserRepository", "Loading user: $userId")
        return try {
            val user = apiService.getUser(userId)
            Logger.info("UserRepository", "User loaded successfully: $userId")
            user
        } catch (e: NetworkException) {
            Logger.warn("UserRepository", "Network error loading user, using cache", e)
            cache.getUser(userId)
        } catch (e: Exception) {
            Logger.error("UserRepository", "Failed to load user: $userId", e)
            null
        }
    }
}
```

## Performance

### Performance Considerations
- **Profiling**: Profile code to identify bottlenecks before optimizing
- **Database Queries**: Optimize database queries (indexes, avoid N+1 queries)
- **Caching**: Use caching for expensive operations
- **Lazy Loading**: Load data lazily when possible
- **Resource Cleanup**: Properly release resources (file handles, connections)

### Performance Checklist
- [ ] No unnecessary database queries
- [ ] Efficient algorithms and data structures
- [ ] Proper use of caching
- [ ] Resources are properly released
- [ ] No memory leaks
- [ ] Network requests are optimized

## Accessibility

### Accessibility Guidelines

**Android/Jetpack Compose Specific:**
- **Content Descriptions**: All interactive elements (buttons, icons, images) must have descriptive `contentDescription` or semantic descriptions
- **Semantic Roles**: Use `semantics` modifier to provide semantic roles (`Role.Button`, `Role.Checkbox`, etc.)
- **State Descriptions**: Provide state descriptions for dynamic content (loading, error, success states)
- **Error Announcements**: Use `semantics { error() }` to announce errors to screen readers
- **Touch Target Size**: Ensure all interactive elements meet minimum 48dp touch target size
- **Color Contrast**: Maintain WCAG AA contrast ratios (4.5:1 for text, 3:1 for UI components)
- **Text Field Labels**: Always provide labels for text fields using `label` parameter
- **Keyboard Navigation**: Ensure all interactive elements are keyboard accessible
- **Screen Reader Testing**: Test with TalkBack enabled to verify screen reader compatibility

**Implementation Examples:**
```kotlin
// Icon with content description
Icon(
    imageVector = Icons.Default.ArrowBack,
    contentDescription = "Navigate back"
)

// Button with semantic description
Button(
    onClick = { },
    modifier = Modifier.semantics {
        contentDescription = "Save mood entry"
        stateDescription = if (isLoading) "Saving" else null
    }
) { Text("Save") }

// Text field with error announcement
OutlinedTextField(
    value = text,
    onValueChange = { },
    label = { Text("Email") },
    modifier = Modifier.semantics {
        if (errorMessage != null) {
            error(errorMessage)
        }
    },
    isError = errorMessage != null
)
```

### Accessibility Checklist
- [ ] All interactive elements have content descriptions
- [ ] Semantic roles are defined for custom interactive components
- [ ] Error states are announced to screen readers
- [ ] Loading states are communicated to screen readers
- [ ] Touch targets meet minimum 48dp size
- [ ] Color contrast meets WCAG AA standards
- [ ] Text fields have proper labels
- [ ] Tested with TalkBack screen reader
- [ ] Keyboard navigation works for all interactive elements
- [ ] Dynamic content changes are announced

## Android-Specific Guidelines

### Android Best Practices
- **Lifecycle Awareness**: Be aware of Android lifecycle (Activities, Fragments, ViewModels)
- **Background Threading**: Perform heavy operations on background threads
- **Memory Management**: Be mindful of memory usage (especially images)
- **Configuration Changes**: Handle configuration changes properly
- **Permissions**: Request permissions appropriately and handle denials

### Android Architecture
- **MVVM/MVI**: Follow established architecture patterns
  - **Always use ViewModel for UI screens**: Extract business logic from Composables into ViewModels
  - ViewModels manage state, handle user actions, and interact with repositories
  - UI (Composables) should only observe ViewModel state and send events
  - This enables testability and proper separation of concerns
  - See [ViewModel Best Practices](#viewmodel-best-practices) for details
- **Repository Pattern**: Use repository pattern for data access
- **Dependency Injection**: Use DI framework (Dagger/Hilt, Koin) or manual DI with factories
- **Reactive Programming**: Use coroutines/Flow for asynchronous operations

## Questions to Ask Before Implementing

1. **Does this change break existing functionality?**
2. **Are there tests for this change?**
3. **Will all tests pass after this change?** (Run `./gradlew test` to verify)
4. **Is this the simplest solution?**
5. **Does this follow existing patterns in the codebase?**
6. **Is error handling appropriate?**
7. **Is this secure?**
8. **Is documentation needed/updated?**
9. **Will this impact performance?**
10. **Is this accessible?**
11. **Does this align with project goals?**

## ViewModel Best Practices

### When to Use ViewModels
- **Always use ViewModels for screens with business logic**: Any screen that needs to:
  - Manage state (loading, errors, user input)
  - Interact with repositories or data sources
  - Handle user actions (sign in, save data, etc.)
  - Perform validation or transformation

### ViewModel Structure
- **State Management**: Use `StateFlow` or `MutableStateFlow` for reactive state
- **Dependency Injection**: Accept dependencies via constructor (UserManager, Repository, etc.)
- **ViewModelFactory**: Create a factory for dependency injection when using `viewModel()` in Compose
- **Error Handling**: Handle errors gracefully and expose error state to UI
- **Lifecycle Awareness**: Use `viewModelScope` for coroutines (automatically cancelled when ViewModel is cleared)

### ViewModel Testing
- **Set up test dispatcher**: Use `Dispatchers.setMain(testDispatcher)` in `@Before` and `Dispatchers.resetMain()` in `@After`
- **Test state changes**: Verify StateFlow values change correctly
- **Test error handling**: Verify error states are set appropriately
- **Mock dependencies**: Mock repositories, managers, etc.

### Example ViewModel Pattern
```kotlin
class MyScreenViewModel(
    private val repository: MyRepository,
    private val userManager: UserManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(MyUiState())
    val uiState: StateFlow<MyUiState> = _uiState.asStateFlow()
    
    fun performAction() {
        viewModelScope.launch {
            repository.doSomething()
                .onSuccess { /* update state */ }
                .onFailure { /* set error state */ }
        }
    }
}
```

### Avoiding Unnecessary Abstractions
- **Don't create wrapper classes that only add logging**: If a class only wraps another and adds logging, consider removing it
- **Evaluate abstraction value**: Before creating an abstraction layer, ask:
  - Does it add meaningful functionality beyond what the underlying class provides?
  - Will it support future extensibility that's actually planned?
  - Is the added complexity justified?
- **Simplify when possible**: Remove unnecessary layers that don't provide clear value

### Application Initialization
- **Extract initialization methods**: Break complex `onCreate()` into focused methods:
  - `initializeAuth()` - Authentication setup
  - `initializeDataLayer()` - Database and repositories
  - `initializeSync()` - Background sync setup
- **Clear error handling**: Each initialization step should handle its own errors gracefully
- **Graceful degradation**: App should continue functioning even if some components fail to initialize

### Defensive Coding Guidelines
- **Avoid impossible exception handling**: Don't catch exceptions that can't actually occur
  - Example: `NoSuchFieldError` for BuildConfig fields (generated at compile time, always exists)
  - Only catch exceptions that could realistically occur at runtime
- **Trust library guarantees**: If a library guarantees something (e.g., Room migrations run in order), trust it
- **Simplify error handling**: Remove unnecessary try-catch blocks that add no value

## Development Metrics

### Overview
We track development metrics over time to analyze trends and identify patterns that affect codebase health.

### Metrics Collected
- **Prompts**: Every user request/prompt is stored with timestamp
- **Build Metrics**: Execution time, success/failure, warnings, errors
- **Test Metrics**: Test counts, execution time, failures, coverage
- **Complexity Metrics**: Lines of code, class/function counts, file counts
- **Accessibility Metrics**: Violation counts, error/warning breakdowns
- **Coverage**: Code coverage percentages and trends

### When to Capture Metrics
- **After builds**: Automatically captured via wrapper scripts
- **After tests**: Automatically captured via wrapper scripts
- **After significant changes**: Run `./scripts/metrics/capture-all-metrics.sh`
- **For each prompt**: Store using `./scripts/metrics/capture-prompt.sh "prompt text"`

### Metric Storage
- Metrics are stored in `development-metrics/` directory
- Each metric type has its own subdirectory
- Files are timestamped for historical tracking
- JSON format for easy analysis

### Analysis
- Periodically review metrics to identify:
  - Performance regressions (build/test time increases)
  - Quality trends (complexity, coverage changes)
  - Accessibility improvements
  - Patterns in development workflow
  - Correlation between prompts and code changes

### Automatic Collection
- Build metrics: Captured automatically when using `./gradlew build` wrapper
- Test metrics: Captured automatically when using `./gradlew test` wrapper
- Complexity/Accessibility: Run `./scripts/metrics/capture-all-metrics.sh` manually or in CI

## Important Documentation References

When implementing features, refer to these documents for detailed guidance:

### Architecture & Design
- **[Data Layer Architecture](../docs/architecture/DATA_LAYER_ARCHITECTURE.md)** - Room, Supabase, offline-first patterns, migrations
- **[Authentication Architecture](../docs/architecture/AUTHENTICATION.md)** - Auth system, user management, OAuth
- **[Error Handling Guide](../docs/architecture/ERROR_HANDLING.md)** - Error handling system, patterns, best practices
- **[Error Conversion Strategies](../docs/architecture/ERROR_CONVERSION_STRATEGIES.md)** - When and how to convert between error types
- **[Feature Flags](../docs/architecture/FEATURE_FLAGS.md)** - Feature flag system and usage
- **[Migration Checklist](../docs/architecture/MIGRATION_CHECKLIST.md)** - Database migration checklist

### Development Workflows
- **[Hot Reload Guide](../docs/development/HOT_RELOAD.md)** - Development workflow, hot reloading, continuous build
- **[Supabase Setup](../docs/development/SUPABASE_SETUP.md)** - Complete Supabase setup (local and cloud)
- **[Cloud Setup Steps](../docs/development/CLOUD_SETUP_STEPS.md)** - Step-by-step cloud setup
- **[Google OAuth Setup](../docs/development/GOOGLE_OAUTH_SETUP.md)** - Google OAuth configuration
- **[Supabase Google Config](../docs/development/SUPABASE_GOOGLE_CONFIG.md)** - Supabase Google provider setup
- **[OAuth Callback Setup](../docs/development/SUPABASE_OAUTH_CALLBACK_SETUP.md)** - OAuth callback URL configuration (fixes localhost redirect issue)
- **[OAuth Implementation Guide](../docs/development/OAUTH_IMPLEMENTATION.md)** - OAuth implementation guide (Chrome Custom Tabs)
- **[Email Confirmation](../docs/development/SUPABASE_EMAIL_CONFIRMATION.md)** - Email confirmation configuration
- **[CI/CD Migration Setup](../docs/development/CI_CD_MIGRATION_SETUP.md)** - CI/CD migration deployment
- **[Service Role Setup](../docs/development/SERVICE_ROLE_SETUP.md)** - Service role key configuration
- **[Docker Setup](../docs/development/DOCKER_SETUP.md)** - Docker Desktop installation (if needed)
- **[KSP Migration](../docs/development/KSP_MIGRATION.md)** - KSP migration reference (historical)

### Testing
- **[Test Coverage](../docs/testing/TEST_COVERAGE.md)** - Current test coverage status
- **[Database Access](../docs/testing/DATABASE_ACCESS.md)** - Accessing Room database on emulator
- **[Supabase Auth Provider Testing](../docs/testing/SUPABASE_AUTH_PROVIDER_TESTING.md)** - Testing Supabase auth

### Architecture Decisions
- **[Data Storage Options](../docs/architecture/decisions/DATA_STORAGE_OPTIONS.md)** - Technology decision for data storage

### Archived Evaluations
Historical evaluations (kept for reference):
- **[Architecture Evaluation](../docs/archive/ARCHITECTURE_EVALUATION.md)** - Architecture evaluation and recommendations
- **[Error Handling Evaluation](../docs/archive/ERROR_HANDLING_EVALUATION.md)** - Error handling system evaluation
- **[Google SSO Implementation](../docs/archive/GOOGLE_SSO_IMPLEMENTATION.md)** - Google SSO implementation status (historical)

### Quick Reference
- **[Documentation Index](../docs/README.md)** - Complete documentation index
- **[Project README](../README.md)** - Project overview and quick start

## When in Doubt

- **Ask for Clarification**: If requirements are unclear, ask for clarification
- **Follow Existing Patterns**: When unsure, follow existing code patterns
- **Prioritize Correctness**: Correct code is more important than clever code
- **Test Thoroughly**: When uncertain, add more tests
- **Document Decisions**: Document non-obvious decisions and trade-offs
- **Question Abstractions**: If an abstraction layer seems unnecessary, evaluate whether it should be removed
- **Check Documentation**: Refer to relevant documentation files before implementing features

---

**Remember**: These guidelines are meant to help, not hinder. Use judgment and adapt as needed for specific situations while maintaining code quality and consistency.

