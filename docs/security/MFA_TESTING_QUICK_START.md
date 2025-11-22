# MFA Testing Quick Start

**Date**: 2025-01-22  
**Purpose**: Quick reference for executing MFA tests

## Quick Commands

### Run Unit Tests

```bash
# Run all tests
./gradlew test

# Run MFA-related tests (once implemented)
./gradlew test --tests "*Mfa*" --tests "*Totp*" --tests "*SignInResult*"

# Run with coverage
./gradlew test jacocoTestReport
```

### Manual Testing Setup

1. **Install Desktop Authenticator** (for testing without mobile device):
   ```bash
   # Windows
   # Download WinAuth from https://winauth.github.io/winauth/
   
   # macOS/Linux
   # Use built-in authenticator or install third-party app
   ```

2. **Enable MFA in Supabase**:
   ```bash
   # In supabase/config.toml
   [auth.mfa.totp]
   enroll_enabled = true
   verify_enabled = true
   ```

3. **Create Test User**:
   - Sign up with test email
   - Verify email (if required)

## Test Execution Checklist

### Unit Tests
- [ ] Verify Supabase SDK API signatures
- [ ] Update test mocks to match API
- [ ] Run tests: `./gradlew test`
- [ ] Check coverage: `./gradlew jacocoTestReport`
- [ ] Fix any failures

### Manual Tests
- [ ] Execute Scenario 1: MFA Enrollment Flow
- [ ] Execute Scenario 2: MFA Login Flow
- [ ] Execute Scenario 3: Invalid Code Handling
- [ ] Execute Scenario 4: Expired Code Handling
- [ ] Execute Scenario 5: Manual Secret Key Entry
- [ ] Execute Scenario 6: Multiple Factors
- [ ] Execute Scenario 7: MFA Unenrollment
- [ ] Execute Scenario 8: Network Error Handling
- [ ] Execute Scenario 9: Accessibility Testing
- [ ] Execute Scenario 10: UX Clarity Testing

## Test Utilities

### Generate TOTP Code (For Testing)

```kotlin
// In tests
val secret = "JBSWY3DPEHPK3PXP" // Test secret
val code = TotpTestHelper.generateTotpCode(secret)
// Use code in test
```

### Use Test Fixtures

```kotlin
// In tests
val enrollmentResponse = MfaTestFixtures.createTestEnrollmentResponse()
val challenge = MfaTestFixtures.createTestMfaChallenge()
val code = MfaTestFixtures.generateTestTotpCode()
```

## Common Issues

### Issue: Tests Fail - Supabase SDK API Mismatch

**Solution**: 
1. Verify actual Supabase SDK API signatures
2. Update test mocks to match
3. Update MfaManager if needed

### Issue: TOTP Code Generation Fails

**Solution**:
1. Check secret format (must be Base32)
2. Verify time step calculation
3. Check algorithm implementation

### Issue: Manual Test - QR Code Not Scannable

**Solution**:
1. Check QR code size (should be 256x256dp minimum)
2. Check lighting conditions
3. Try manual secret key entry instead

## Related Documentation

- [MFA Testing Strategy](./MFA_TESTING_STRATEGY.md) - Complete strategy
- [MFA Manual Test Plan](./MFA_MANUAL_TEST_PLAN.md) - Detailed scenarios
- [MFA Testing Summary](./MFA_TESTING_SUMMARY.md) - Summary

