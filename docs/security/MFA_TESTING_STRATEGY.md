# MFA Testing Strategy

**Date**: 2025-01-22  
**Status**: Comprehensive Testing Plan  
**Approach**: Hybrid - Unit Tests + Stack Tests + Human-Focused Manual Tests

## Overview

MFA (Multi-Factor Authentication) presents unique testing challenges:
- **Device Dependency**: TOTP codes require authenticator apps (typically on mobile devices)
- **Time Sensitivity**: TOTP codes expire every 30 seconds
- **Security Critical**: Must test thoroughly without bypassing security
- **Human Interaction**: Some aspects require human judgment (UX, accessibility)

## Testing Philosophy

Following our **Hourglass Pattern**:
- **Wide Base (Unit Tests)**: Maximize coverage with mocked MFA operations
- **Narrow Middle (Integration)**: Minimal - only test critical interactions
- **Wide Top (Stack Tests)**: Prefer full-stack tests with test Supabase or mocked Supabase
- **Human-Focused**: Manual tests for UX, accessibility, and real-world scenarios

## Testing Layers

### Layer 1: Unit Tests (Maximize Coverage)

**Purpose**: Test business logic, error handling, state management

**Approach**: Mock Supabase MFA API, test all code paths

**What to Test**:
- ✅ MfaManager operations (enrollment, verification, unenrollment)
- ✅ SignInResult handling (Success, MfaChallenge, Error)
- ✅ UserManager MFA-aware sign-in
- ✅ ViewModel state management
- ✅ Error handling and edge cases
- ✅ TOTP code validation (format, length)

**Tools**: JUnit, Mockito, Kotlin Coroutines Test

**Example**:
```kotlin
@Test
fun `should return MfaChallenge when sign-in requires MFA`() = runTest {
    // Arrange
    val authProvider = mock<SupabaseAuthProvider>()
    whenever(authProvider.signInWithMfa(any(), any()))
        .thenReturn(SignInResult.MfaChallenge("challenge-123", "user-456"))
    
    // Act
    val result = userManager.signInWithMfa("test@example.com", "password")
    
    // Assert
    assertTrue(result is SignInResult.MfaChallenge)
    assertEquals("challenge-123", (result as SignInResult.MfaChallenge).challengeId)
}
```

**Security Note**: We're testing **our code logic**, not bypassing MFA. The mock represents a real Supabase response.

---

### Layer 2: Stack-Level Tests (Prefer)

**Purpose**: Test full stack (UI → ViewModel → Repository → Auth Provider)

**Approach**: Use test Supabase instance or comprehensive mocks

**What to Test**:
- ✅ Complete MFA enrollment flow
- ✅ Complete MFA login flow
- ✅ Error scenarios (invalid code, expired challenge)
- ✅ Navigation between screens
- ✅ State persistence

**Tools**: 
- Test Supabase instance (recommended)
- Comprehensive mocks (if test Supabase unavailable)
- Compose UI Testing

**Example**:
```kotlin
@Test
fun `should complete MFA enrollment flow`() = runTest {
    // Arrange - Use test Supabase or comprehensive mock
    val testSupabase = createTestSupabaseClient()
    val mfaManager = MfaManager(testSupabase)
    val viewModel = MfaSetupViewModel(mfaManager)
    
    // Act - Start enrollment
    viewModel.startEnrollment()
    val enrollmentResponse = viewModel.enrollmentResponse.value
    
    // Simulate user entering code (use test TOTP generator)
    val testCode = generateTestTotpCode(enrollmentResponse.secret)
    viewModel.updateVerificationCode(testCode)
    viewModel.verifyEnrollment()
    
    // Assert
    assertTrue(viewModel.isEnrollmentComplete.value)
}
```

**TOTP Code Generation for Testing**:
- Use a TOTP library (e.g., `com.warrenstrange:googleauth`) to generate codes from secret
- This allows testing without a physical device
- **Security**: Only use in test environment, never in production

---

### Layer 3: Human-Focused Manual Tests (Critical)

**Purpose**: Test real-world scenarios, UX, accessibility, device compatibility

**Approach**: Manual testing with real authenticator apps

**What to Test**:
- ✅ Real authenticator app integration (Google Authenticator, Authy, etc.)
- ✅ QR code scanning (different devices, lighting conditions)
- ✅ Manual secret key entry
- ✅ Accessibility (screen reader, keyboard navigation)
- ✅ Error recovery (wrong code, expired code)
- ✅ Multiple factors (enroll multiple devices)
- ✅ Unenrollment flow
- ✅ Backup codes (if supported)

**Tools**:
- **Desktop Authenticator Apps**: WinAuth (Windows), Authenticator (macOS/Linux)
- **Mobile Emulators**: Android emulator with authenticator app installed
- **Physical Devices**: Real Android/iOS devices with authenticator apps

**Test Scenarios**:

1. **MFA Enrollment**
   ```
   - Navigate to MFA setup
   - Scan QR code with authenticator app
   - Verify QR code displays correctly
   - Test manual secret key entry
   - Enter code from authenticator app
   - Verify enrollment success
   ```

2. **MFA Login**
   ```
   - Sign in with email/password
   - Verify MFA challenge screen appears
   - Enter code from authenticator app
   - Verify sign-in completes
   ```

3. **Error Scenarios**
   ```
   - Enter wrong code (should show error)
   - Enter expired code (should show error)
   - Enter code with wrong format (should validate)
   - Network error during verification
   ```

4. **Accessibility**
   ```
   - Test with TalkBack enabled
   - Verify QR code has content description
   - Verify error messages are announced
   - Test keyboard navigation
   - Test with large text sizes
   ```

---

## Test Utilities and Helpers

### TOTP Code Generator (For Testing)

**Purpose**: Generate TOTP codes from secret for automated testing

**Implementation**:
```kotlin
// app/src/test/java/com/electricsheep/app/auth/TotpTestHelper.kt
object TotpTestHelper {
    /**
     * Generate TOTP code from secret for testing.
     * Uses Google Authenticator algorithm (TOTP).
     * 
     * WARNING: Only use in test environment!
     */
    fun generateTotpCode(secret: String, timeStep: Long = System.currentTimeMillis() / 1000 / 30): String {
        // Use TOTP library (e.g., com.warrenstrange:googleauth)
        // This allows testing without physical device
        // ...
    }
}
```

**Security**: 
- ✅ Only available in test builds
- ✅ Never used in production code
- ✅ Clearly marked as test-only

### MFA Test Fixtures

**Purpose**: Provide test data and mocks for MFA operations

**Implementation**:
```kotlin
// app/src/test/java/com/electricsheep/app/auth/MfaTestFixtures.kt
object MfaTestFixtures {
    val testEnrollmentResponse = MfaResponse(
        qrCode = "otpauth://totp/ElectricSheep:test@example.com?secret=TEST_SECRET&issuer=ElectricSheep",
        secret = "TEST_SECRET_BASE32",
        challengeId = "challenge-test-123"
    )
    
    val testMfaChallenge = SignInResult.MfaChallenge(
        challengeId = "challenge-test-123",
        userId = "user-test-456"
    )
    
    fun createMockMfaManager(): MfaManager {
        // Create mock with realistic responses
    }
}
```

---

## Test Coverage Goals

### Unit Tests
- **Target**: >90% code coverage for MFA-related code
- **Focus**: Business logic, error handling, state management
- **Speed**: < 1 second per test

### Stack Tests
- **Target**: All critical user flows
- **Focus**: End-to-end scenarios, error recovery
- **Speed**: < 30 seconds per test

### Manual Tests
- **Target**: All user-facing scenarios
- **Focus**: UX, accessibility, real-world usage
- **Frequency**: Before releases, after major changes

---

## Test Plan

### Phase 1: Unit Tests (Week 1)

**Priority**: High  
**Effort**: 2-3 days

1. **MfaManager Tests**
   - [ ] Enrollment success
   - [ ] Enrollment failure (network error, API error)
   - [ ] Verification success
   - [ ] Verification failure (invalid code, expired challenge)
   - [ ] Unenrollment success
   - [ ] Unenrollment failure
   - [ ] List factors success
   - [ ] List factors failure

2. **SignInResult Tests**
   - [ ] Success case
   - [ ] MfaChallenge case
   - [ ] Error case

3. **UserManager MFA Tests**
   - [ ] signInWithMfa() - Success
   - [ ] signInWithMfa() - MfaChallenge
   - [ ] signInWithMfa() - Error
   - [ ] verifyMfaSignIn() - Success
   - [ ] verifyMfaSignIn() - Error

4. **ViewModel Tests**
   - [ ] MfaSetupViewModel - Enrollment flow
   - [ ] MfaSetupViewModel - Verification flow
   - [ ] MfaVerifyViewModel - Code input
   - [ ] MfaVerifyViewModel - Verification

5. **Error Handling Tests**
   - [ ] Invalid code format
   - [ ] Expired challenge
   - [ ] Network errors
   - [ ] API errors

### Phase 2: Stack Tests (Week 2)

**Priority**: High  
**Effort**: 2-3 days

1. **MFA Enrollment Flow**
   - [ ] Complete enrollment with test TOTP generator
   - [ ] Error scenarios
   - [ ] Navigation

2. **MFA Login Flow**
   - [ ] Complete login with MFA
   - [ ] Error scenarios
   - [ ] Navigation

3. **UI Tests**
   - [ ] Screen navigation
   - [ ] State management
   - [ ] Error display

### Phase 3: Manual Tests (Ongoing)

**Priority**: Critical  
**Effort**: 1-2 hours per test session

1. **Real Device Testing**
   - [ ] QR code scanning (multiple devices)
   - [ ] Authenticator app integration
   - [ ] Manual secret entry
   - [ ] Error recovery

2. **Accessibility Testing**
   - [ ] Screen reader (TalkBack)
   - [ ] Keyboard navigation
   - [ ] Large text sizes
   - [ ] High contrast mode

3. **UX Testing**
   - [ ] User flow clarity
   - [ ] Error message clarity
   - [ ] Loading states
   - [ ] Success feedback

---

## Best Practices

### ✅ DO

1. **Mock Supabase API** - Test our code logic, not Supabase
2. **Use TOTP Generator for Testing** - Allows automated testing without device
3. **Test Error Scenarios** - Critical for security
4. **Test Accessibility** - MFA must be accessible
5. **Manual Testing** - Some scenarios require human judgment
6. **Test Edge Cases** - Expired codes, network errors, invalid formats

### ❌ DON'T

1. **Don't Bypass MFA in Tests** - Test the real flow
2. **Don't Skip Error Scenarios** - Security-critical
3. **Don't Skip Accessibility** - Required for compliance
4. **Don't Use Production Secrets** - Use test fixtures
5. **Don't Skip Manual Testing** - Real-world validation needed

---

## Security Considerations

### Test Environment Isolation

- ✅ Use test Supabase instance (separate from production)
- ✅ Use test user accounts (not production users)
- ✅ Never commit test secrets or API keys
- ✅ Clear test data after tests

### TOTP Code Generation

- ✅ Only in test builds (BuildConfig.DEBUG)
- ✅ Clearly marked as test-only
- ✅ Never used in production code
- ✅ Use established TOTP library (don't implement yourself)

### Mocking Strategy

- ✅ Mock Supabase API responses (not MFA logic)
- ✅ Test our code logic thoroughly
- ✅ Use realistic mock responses
- ✅ Test error scenarios with mocks

---

## Tools and Libraries

### Testing Libraries

- **JUnit 4/5**: Test framework
- **Mockito**: Mocking framework
- **Kotlin Coroutines Test**: Coroutine testing
- **Compose UI Testing**: UI testing
- **TOTP Library**: `com.warrenstrange:googleauth` (for test code generation)

### Manual Testing Tools

- **WinAuth** (Windows): Desktop authenticator app
- **Authenticator** (macOS/Linux): Desktop authenticator app
- **Google Authenticator**: Mobile authenticator app
- **Authy**: Multi-device authenticator app

---

## Test Execution

### Automated Tests

```bash
# Run all unit tests
./gradlew test

# Run MFA-specific tests
./gradlew test --tests "*Mfa*"

# Run with coverage
./gradlew test jacocoTestReport
```

### Manual Tests

1. **Setup Test Environment**
   - Enable MFA in test Supabase instance
   - Create test user account
   - Install authenticator app (desktop or mobile)

2. **Run Test Scenarios**
   - Follow test plan scenarios
   - Document results
   - Report issues

3. **Accessibility Testing**
   - Enable TalkBack
   - Test with different accessibility settings
   - Verify announcements

---

## Continuous Integration

### CI/CD Integration

- ✅ Run unit tests on every commit
- ✅ Run stack tests on PRs
- ✅ Manual tests before releases
- ✅ Accessibility tests in CI (automated where possible)

### Test Reports

- ✅ Code coverage reports
- ✅ Test execution reports
- ✅ Accessibility test results
- ✅ Manual test results (documented)

---

## Related Documentation

- [MFA Implementation Summary](./MFA_IMPLEMENTATION_SUMMARY.md)
- [MFA Integration Complete](./MFA_INTEGRATION_COMPLETE.md)
- [Testing Principles](../testing/guides/TEST_COVERAGE.md)
- [Test Framework Architecture](../testing/architecture/FRAMEWORK_ARCHITECTURE_RECOMMENDATION.md)

