# MFA SDK API Verification

**Date**: 2025-01-22  
**Status**: In Progress - Compilation Errors Need Resolution

## Current Issues

### 1. Import Errors
- ❌ `Packages cannot be imported` for `io.github.jan.supabase.gotrue.mfa`
- ✅ **Fixed**: Removed wildcard import, using specific imports

### 2. Auth Access
- ❌ `Unresolved reference: auth` in `supabaseClient.auth.mfa`
- ✅ **Fixed**: Added `import io.github.jan.supabase.gotrue.auth`

### 3. Response Structure
- ❌ `Unresolved reference: id` - `FactorType.TOTP.Response` doesn't have `id` property
- ⚠️ **Issue**: Enrollment response has `secret` and `qrCode`, but no `id`
- **Need**: Verify how to get factor ID for verification

### 4. Verification API
- ❌ `Unresolved reference: createChallengeAndVerify`
- ⚠️ **Issue**: Need to verify actual API method name
- **Options**: `verifyChallenge`, `createChallengeAndVerify`, or different approach

## Actual SDK Structure (From JAR Analysis)

### FactorType.TOTP.Response
From JAR file analysis:
- ✅ `secret: String` - Base32-encoded secret key
- ✅ `qrCode: String` - QR code data (serial name: `qr_code`)
- ✅ `getUri()` - Method to get URI
- ❌ **No `id` property** - Factor ID not in response

### MfaApi Methods (From JAR Strings)
- `enroll` - Enrolls a factor
- `unenroll` - Removes a factor
- `verifyChallenge` - Verifies a challenge
- `createChallenge` - Creates a challenge
- `listFactors` - Lists factors

## Verification Needed

### 1. Enrollment Response Structure
**Question**: How do we get the factor ID after enrollment?

**Possible Solutions**:
- Option A: `enroll()` returns response with factor ID (but JAR shows no `id`)
- Option B: Factor ID is returned separately or in different property
- Option C: Need to call `listFactors()` after enrollment to get ID
- Option D: Response structure is different than expected

**Action**: Check Supabase SDK documentation or test with actual API

### 2. Verification Flow
**Question**: How to verify enrollment?

**Possible Solutions**:
- Option A: `createChallengeAndVerify(factorId, code)` - Single call
- Option B: `createChallenge(factorId)` then `verifyChallenge(challengeId, code)` - Two calls
- Option C: `verifyChallenge(factorId, code)` - Direct verification
- Option D: Different API structure

**Action**: Check SDK documentation or test with actual API

### 3. Factor ID Access
**Question**: Where does factor ID come from?

**Possible Solutions**:
- Option A: From `listFactors()` after enrollment
- Option B: From enrollment response (different property name)
- Option C: From separate API call
- Option D: Factor ID not needed for verification

**Action**: Test enrollment flow and check response

## Next Steps

1. **Test Enrollment API**
   - Call `enroll()` with actual Supabase instance
   - Inspect response object structure
   - Check all properties available

2. **Test Verification API**
   - Try different API methods
   - Check which parameters are needed
   - Verify correct flow

3. **Update Code**
   - Fix property access based on actual structure
   - Update verification method
   - Fix all compilation errors

4. **Update Tests**
   - Update test fixtures to match actual structure
   - Fix mock responses
   - Ensure tests pass

## Temporary Workaround

For now, we can:
1. Comment out problematic code
2. Add TODO comments with verification requirements
3. Document what needs to be tested
4. Continue with other parts of implementation

## Related Documentation

- [MFA Testing Next Steps](./MFA_TESTING_NEXT_STEPS.md)
- [MFA Implementation Progress](./MFA_IMPLEMENTATION_PROGRESS.md)
- [Supabase Kotlin SDK GitHub](https://github.com/supabase-community/supabase-kt)

