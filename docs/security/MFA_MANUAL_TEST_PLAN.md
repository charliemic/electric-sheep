# MFA Manual Test Plan

**Date**: 2025-01-22  
**Status**: Ready for Execution  
**Approach**: Human-Focused Testing with Real Authenticator Apps

## Overview

This manual test plan focuses on **human-focused testing** of MFA functionality. It tests real-world scenarios, UX, accessibility, and device compatibility that automated tests cannot fully cover.

## Prerequisites

### Tools Required

1. **Desktop Authenticator App** (for testing without mobile device):
   - **WinAuth** (Windows): https://winauth.github.io/winauth/
   - **Authenticator** (macOS/Linux): Built-in or third-party
   - **Google Authenticator** (Web): https://authenticator.google.com/

2. **Mobile Authenticator App** (recommended):
   - **Google Authenticator** (Android/iOS)
   - **Authy** (Android/iOS)
   - **Microsoft Authenticator** (Android/iOS)

3. **Test Environment**:
   - Test Supabase instance with MFA enabled
   - Test user account
   - Android emulator or physical device

### Setup Steps

1. **Enable MFA in Supabase**:
   ```bash
   # In supabase/config.toml
   [auth.mfa.totp]
   enroll_enabled = true
   verify_enabled = true
   ```

2. **Create Test User**:
   - Sign up with test email
   - Verify email (if required)
   - Note: User should NOT have MFA enabled initially

3. **Install Authenticator App**:
   - Install on desktop or mobile device
   - Ready to scan QR codes

---

## Test Scenarios

### Scenario 1: MFA Enrollment Flow

**Objective**: Verify user can successfully enroll in MFA

**Steps**:
1. Sign in to app with test account
2. Navigate to MFA setup (Settings → Security → Enable MFA)
3. Tap "Set Up Two-Factor Authentication"
4. Verify QR code is displayed
5. Scan QR code with authenticator app
6. Verify authenticator app shows account (Electric Sheep)
7. Enter 6-digit code from authenticator app
8. Tap "Verify and Enable"
9. Verify success message appears
10. Verify MFA is now enabled

**Expected Results**:
- ✅ QR code displays correctly
- ✅ QR code is scannable
- ✅ Authenticator app adds account successfully
- ✅ Code verification succeeds
- ✅ Success message is clear
- ✅ MFA status shows as enabled

**Accessibility Checks**:
- ✅ QR code has content description ("QR code for two-factor authentication setup")
- ✅ Screen reader announces QR code
- ✅ Error messages are announced
- ✅ Success message is announced

**Issues to Report**:
- QR code not scannable
- Code verification fails
- Error messages unclear
- Accessibility issues

---

### Scenario 2: MFA Login Flow

**Objective**: Verify user can sign in with MFA enabled

**Steps**:
1. Sign out of app (if signed in)
2. Enter email and password
3. Tap "Sign In"
4. Verify MFA verification screen appears
5. Open authenticator app
6. Enter 6-digit code from authenticator app
7. Tap "Verify and Sign In"
8. Verify sign-in completes
9. Verify user is authenticated

**Expected Results**:
- ✅ MFA challenge screen appears after password verification
- ✅ Screen shows clear instructions
- ✅ Code input field accepts 6 digits
- ✅ Verification succeeds with correct code
- ✅ Sign-in completes successfully
- ✅ User is authenticated

**Accessibility Checks**:
- ✅ Screen reader announces "Enter verification code"
- ✅ Instructions are clear and announced
- ✅ Error messages are announced
- ✅ Success is announced

**Issues to Report**:
- MFA challenge not detected
- Code input issues
- Verification fails with correct code
- Navigation issues

---

### Scenario 3: Invalid Code Handling

**Objective**: Verify error handling for invalid codes

**Steps**:
1. Sign in with MFA-enabled account
2. Enter wrong code (e.g., "000000")
3. Tap "Verify and Sign In"
4. Verify error message appears
5. Verify code field is cleared
6. Enter correct code
7. Verify sign-in succeeds

**Expected Results**:
- ✅ Error message appears for wrong code
- ✅ Error message is clear and actionable
- ✅ Code field is cleared on error
- ✅ User can retry with correct code
- ✅ Sign-in succeeds after correct code

**Accessibility Checks**:
- ✅ Error message is announced to screen reader
- ✅ Error semantics are set correctly
- ✅ User can recover from error

**Issues to Report**:
- Error message unclear
- Code field not cleared
- Cannot retry after error
- Accessibility issues

---

### Scenario 4: Expired Code Handling

**Objective**: Verify handling of expired TOTP codes

**Steps**:
1. Sign in with MFA-enabled account
2. Wait for code to expire (30+ seconds)
3. Enter expired code
4. Verify error message appears
5. Enter current code
6. Verify sign-in succeeds

**Expected Results**:
- ✅ Error message appears for expired code
- ✅ Error message explains code expired
- ✅ User can enter new code
- ✅ Sign-in succeeds with current code

**Accessibility Checks**:
- ✅ Error message is announced
- ✅ User understands what to do

**Issues to Report**:
- Expired code accepted (security issue)
- Error message unclear
- Cannot recover from expired code

---

### Scenario 5: Manual Secret Key Entry

**Objective**: Verify manual secret key entry works

**Steps**:
1. Navigate to MFA setup
2. Verify secret key is displayed
3. Copy secret key
4. Open authenticator app
5. Add account manually (enter secret key)
6. Enter code from authenticator app
7. Verify enrollment succeeds

**Expected Results**:
- ✅ Secret key is displayed clearly
- ✅ Secret key is copyable
- ✅ Manual entry works in authenticator app
- ✅ Code verification succeeds

**Accessibility Checks**:
- ✅ Secret key is readable by screen reader
- ✅ Secret key can be copied
- ✅ Instructions are clear

**Issues to Report**:
- Secret key not displayed
- Secret key not copyable
- Manual entry fails
- Accessibility issues

---

### Scenario 6: Multiple Factors

**Objective**: Verify user can enroll multiple authenticator devices

**Steps**:
1. Enroll first authenticator device (Scenario 1)
2. Navigate to MFA settings
3. Verify first factor is listed
4. Add second authenticator device
5. Scan QR code with second device
6. Verify both factors are listed
7. Sign in using first device code
8. Sign in using second device code
9. Verify both work

**Expected Results**:
- ✅ Multiple factors can be enrolled
- ✅ All factors are listed
- ✅ Any enrolled factor can be used for sign-in
- ✅ Factors are clearly identified

**Issues to Report**:
- Cannot enroll multiple factors
- Factors not listed correctly
- Sign-in fails with valid factor

---

### Scenario 7: MFA Unenrollment

**Objective**: Verify user can disable MFA

**Steps**:
1. Navigate to MFA settings
2. Verify enrolled factors are listed
3. Tap "Remove" on a factor
4. Verify confirmation dialog appears
5. Confirm removal
6. Verify factor is removed
7. Sign out and sign in
8. Verify MFA is no longer required

**Expected Results**:
- ✅ Factors can be unenrolled
- ✅ Confirmation dialog appears
- ✅ Factor is removed successfully
- ✅ MFA is no longer required for sign-in

**Issues to Report**:
- Cannot unenroll factor
- No confirmation dialog
- Factor not removed
- MFA still required after removal

---

### Scenario 8: Network Error Handling

**Objective**: Verify MFA works with network issues

**Steps**:
1. Enable airplane mode
2. Attempt MFA enrollment
3. Verify error message appears
4. Disable airplane mode
5. Retry enrollment
6. Verify enrollment succeeds

**Expected Results**:
- ✅ Network error is detected
- ✅ Error message is clear
- ✅ User can retry after network restored
- ✅ Enrollment succeeds when network available

**Issues to Report**:
- No error message on network failure
- Cannot retry after network restored
- App crashes on network error

---

### Scenario 9: Accessibility Testing

**Objective**: Verify MFA is accessible to all users

**Steps**:
1. Enable TalkBack screen reader
2. Navigate to MFA setup
3. Verify all elements are announced
4. Verify QR code has description
5. Verify instructions are clear
6. Test keyboard navigation
7. Test with large text sizes
8. Test with high contrast mode

**Expected Results**:
- ✅ All interactive elements are announced
- ✅ QR code has content description
- ✅ Instructions are clear
- ✅ Keyboard navigation works
- ✅ Large text doesn't break layout
- ✅ High contrast is readable

**Issues to Report**:
- Elements not announced
- QR code missing description
- Keyboard navigation broken
- Layout breaks with large text

---

### Scenario 10: UX Clarity Testing

**Objective**: Verify MFA flow is clear and user-friendly

**Steps**:
1. Have a non-technical user test MFA enrollment
2. Observe user's actions
3. Note any confusion or questions
4. Verify user can complete enrollment
5. Verify user understands what MFA does

**Expected Results**:
- ✅ User understands what MFA is
- ✅ User can complete enrollment without help
- ✅ Instructions are clear
- ✅ Error messages are helpful
- ✅ User feels confident using MFA

**Issues to Report**:
- User confused about MFA
- Instructions unclear
- User needs help to complete
- Error messages not helpful

---

## Test Execution Checklist

### Before Testing
- [ ] Test Supabase instance configured
- [ ] MFA enabled in Supabase config
- [ ] Test user account created
- [ ] Authenticator app installed
- [ ] Test device/emulator ready

### During Testing
- [ ] Execute each test scenario
- [ ] Document results (pass/fail)
- [ ] Note any issues or observations
- [ ] Take screenshots of issues
- [ ] Test accessibility features

### After Testing
- [ ] Review all test results
- [ ] Document issues found
- [ ] Prioritize issues (critical/high/medium/low)
- [ ] Create GitHub issues for bugs
- [ ] Update test plan with findings

---

## Issue Reporting Template

```markdown
**Test Scenario**: [Scenario name]
**Severity**: [Critical/High/Medium/Low]
**Steps to Reproduce**:
1. [Step 1]
2. [Step 2]
3. [Step 3]

**Expected Result**: [What should happen]
**Actual Result**: [What actually happened]
**Screenshots**: [If applicable]
**Accessibility Impact**: [If applicable]
**Device/OS**: [Android version, device model]
```

---

## Success Criteria

### Functional
- ✅ All test scenarios pass
- ✅ No critical bugs
- ✅ Error handling works correctly
- ✅ User can complete all flows

### Accessibility
- ✅ Screen reader support works
- ✅ Keyboard navigation works
- ✅ Large text doesn't break layout
- ✅ High contrast is readable

### UX
- ✅ Instructions are clear
- ✅ Error messages are helpful
- ✅ User can complete flows without help
- ✅ User understands MFA benefits

---

## Related Documentation

- [MFA Testing Strategy](./MFA_TESTING_STRATEGY.md) - Complete testing strategy
- [MFA Implementation Summary](./MFA_IMPLEMENTATION_SUMMARY.md) - Implementation details
- [MFA Integration Complete](./MFA_INTEGRATION_COMPLETE.md) - Integration status

