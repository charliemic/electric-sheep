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
- **Language-Specific**: Adhere to Kotlin Multiplatform style guides and best practices
  - Follow the [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
  - Use `expect`/`actual` declarations for platform-specific implementations
  - Keep shared code platform-agnostic when possible
  - Organize code by source sets (commonMain, androidMain, iosMain, etc.)
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
- **Update Documentation**: Keep documentation in sync with code changes
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

### Refactoring Guidelines
- **Test Coverage**: Ensure adequate test coverage before refactoring
- **Incremental Changes**: Make small, incremental refactoring changes
- **Preserve Behavior**: Maintain existing functionality during refactoring
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
- **Authorization**: Verify user permissions before sensitive operations
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

5. **Verify CI Pipeline**
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
   - Fix any failures and push additional commits to the same branch
   - CI will automatically re-run on each push

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

### Error Handling Principles
- **Fail Fast**: Detect errors as early as possible
- **Graceful Degradation**: Handle errors gracefully without crashing
- **User-Friendly Messages**: Provide clear, actionable error messages to users
- **Logging**: Log errors with sufficient context for debugging
- **Error Types**: Use appropriate error types (validation, network, system, etc.)

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
- **Log Levels**: Use appropriate log levels (DEBUG, INFO, WARN, ERROR)
- **Structured Logging**: Use structured logging when possible
- **Sensitive Data**: Never log passwords, tokens, or sensitive user data
- **Context**: Include relevant context in log messages

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
- **Semantic HTML**: Use semantic HTML elements
- **ARIA Labels**: Add ARIA labels for screen readers
- **Keyboard Navigation**: Ensure keyboard accessibility
- **Color Contrast**: Maintain sufficient color contrast ratios
- **Focus Indicators**: Provide visible focus indicators
- **Alt Text**: Include alt text for images

### Accessibility Checklist
- [ ] All interactive elements are keyboard accessible
- [ ] Screen reader compatible
- [ ] Sufficient color contrast
- [ ] Focus indicators visible
- [ ] Images have alt text
- [ ] Forms have proper labels

## Android-Specific Guidelines

### Android Best Practices
- **Lifecycle Awareness**: Be aware of Android lifecycle (Activities, Fragments, ViewModels)
- **Background Threading**: Perform heavy operations on background threads
- **Memory Management**: Be mindful of memory usage (especially images)
- **Configuration Changes**: Handle configuration changes properly
- **Permissions**: Request permissions appropriately and handle denials

### Android Architecture
- **MVVM/MVI**: Follow established architecture patterns
- **Repository Pattern**: Use repository pattern for data access
- **Dependency Injection**: Use DI framework (Dagger/Hilt, Koin)
- **Reactive Programming**: Use coroutines/Flow for asynchronous operations

## Questions to Ask Before Implementing

1. **Does this change break existing functionality?**
2. **Are there tests for this change?**
3. **Is this the simplest solution?**
4. **Does this follow existing patterns in the codebase?**
5. **Is error handling appropriate?**
6. **Is this secure?**
7. **Is documentation needed/updated?**
8. **Will this impact performance?**
9. **Is this accessible?**
10. **Does this align with project goals?**

## When in Doubt

- **Ask for Clarification**: If requirements are unclear, ask for clarification
- **Follow Existing Patterns**: When unsure, follow existing code patterns
- **Prioritize Correctness**: Correct code is more important than clever code
- **Test Thoroughly**: When uncertain, add more tests
- **Document Decisions**: Document non-obvious decisions and trade-offs

---

**Remember**: These guidelines are meant to help, not hinder. Use judgment and adapt as needed for specific situations while maintaining code quality and consistency.

