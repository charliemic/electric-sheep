# Phase 2 Security Implementation Plan

**Last Updated**: 2025-01-22  
**Status**: Planning Phase 2  
**Timeline**: 1-3 months

## Overview

Phase 2 focuses on implementing high-priority security improvements (P2 risks) that significantly improve our security posture. These are medium-priority risks that should be addressed within 1-3 months.

## Priority Ranking

Based on risk scores and implementation effort:

1. **Data Backup Encryption** (8.0) - **Easiest, start here**
   - Effort: Low
   - Timeline: 1 week
   - Impact: Protects backup data

2. **Certificate Pinning** (14.4) - **Important, do next**
   - Effort: Medium
   - Timeline: 2 weeks
   - Impact: Prevents MITM attacks

3. **MFA Support** (19.5) - **Highest risk, most impact**
   - Effort: High
   - Timeline: 1 month
   - Impact: Reduces account takeover risk by ~90%

---

## Implementation 1: Data Backup Encryption (Week 1)

### Why First?

- ✅ **Lowest effort** (1 week)
- ✅ **Quick win** (high value, low complexity)
- ✅ **Builds momentum** for other improvements
- ✅ **Protects user data** in backups

### What It Does

- Encrypts Android backup data
- Prevents unauthorized access to backups
- Excludes sensitive data from backups (optional)

### Implementation Steps

1. **Create backup rules** (`app/src/main/res/xml/backup_rules.xml`)
   - Exclude sensitive data (auth tokens, passwords)
   - Include non-sensitive data (preferences, settings)

2. **Configure backup encryption** (`AndroidManifest.xml`)
   - Enable backup encryption
   - Reference backup rules

3. **Test backup/restore**
   - Verify backup works
   - Verify restore works
   - Verify sensitive data excluded

### Files to Create/Modify

**New Files**:
- `app/src/main/res/xml/backup_rules.xml`

**Modify**:
- `app/src/main/AndroidManifest.xml`

### Testing

- Test backup on device
- Test restore on device
- Verify sensitive data not in backup
- Test with different Android versions

---

## Implementation 2: Certificate Pinning (Week 2-3)

### Why Second?

- ✅ **Medium effort** (2 weeks)
- ✅ **Important security control** (prevents MITM)
- ✅ **Industry best practice**
- ✅ **Protects API calls**

### What It Does

- Pins SSL certificates for Supabase API
- Prevents man-in-the-middle attacks
- Ensures API calls go to legitimate servers

### Implementation Approach

**Option 1: OkHttp CertificatePinner** (Recommended)
- Supabase uses Ktor (which can use OkHttp engine)
- Add certificate pinning to HTTP client
- Configure pins for Supabase domains

**Option 2: Network Security Config**
- Android's built-in certificate pinning
- Configure in `network_security_config.xml`
- Less flexible but simpler

**Recommended**: Use OkHttp CertificatePinner with Ktor

### Implementation Steps

1. **Get Supabase certificate pins**
   - Extract public key pins from Supabase certificates
   - Support multiple pins for certificate rotation

2. **Configure Ktor HTTP client**
   - Add OkHttp engine with CertificatePinner
   - Configure pins for Supabase domains
   - Add fallback mechanism

3. **Create network security config** (optional)
   - Additional Android-level security
   - Certificate transparency logging

4. **Test certificate pinning**
   - Test with legitimate Supabase calls
   - Test with MITM attempt (should fail)
   - Test certificate rotation handling

### Files to Create/Modify

**New Files**:
- `app/src/main/res/xml/network_security_config.xml`
- `app/src/main/java/com/electricsheep/app/security/CertificatePinnerConfig.kt`

**Modify**:
- `app/src/main/java/com/electricsheep/app/data/DataModule.kt`
- `app/src/main/AndroidManifest.xml`

### Certificate Pins

**Supabase domains to pin**:
- `*.supabase.co` (main API)
- `*.supabase.in` (alternative domain)
- Certificate pins (SHA-256 hashes)

**Note**: Need to get actual certificate pins from Supabase

### Testing

- Test API calls work with pinned certificates
- Test MITM attack is blocked
- Test certificate rotation (fallback pins)
- Test on different networks

---

## Implementation 3: MFA Support (Month 1)

### Why Third?

- ✅ **Highest risk score** (19.5)
- ✅ **Biggest impact** (reduces account takeover by ~90%)
- ✅ **Most complex** (1 month timeline)
- ✅ **Industry standard** for sensitive apps

### What It Does

- Adds multi-factor authentication (MFA/2FA)
- Requires second factor (TOTP, SMS, or authenticator app)
- Reduces risk of account takeover

### Implementation Approach

**Recommended: Supabase MFA** (Built-in)

**Why**:
- ✅ Already using Supabase Auth
- ✅ Built-in TOTP support
- ✅ Less code to maintain
- ✅ Industry-standard implementation

**Features**:
- TOTP (Time-based One-Time Password)
- Authenticator apps (Google Authenticator, Authy, etc.)
- SMS (optional, costs money)
- Backup codes

### Implementation Steps

1. **Enable MFA in Supabase**
   - Enable MFA in Supabase dashboard
   - Configure MFA settings
   - Test MFA setup

2. **Add MFA setup UI**
   - QR code display for TOTP
   - Backup codes display
   - MFA verification screen

3. **Integrate MFA in auth flow**
   - Check if user has MFA enabled
   - Require MFA after password
   - Handle MFA verification

4. **Add MFA management**
   - Enable/disable MFA in settings
   - Regenerate backup codes
   - View MFA status

5. **Test MFA flow**
   - Test MFA setup
   - Test MFA login
   - Test MFA disable
   - Test backup codes

### Files to Create/Modify

**New Files**:
- `app/src/main/java/com/electricsheep/app/ui/screens/MfaSetupScreen.kt`
- `app/src/main/java/com/electricsheep/app/ui/screens/MfaVerifyScreen.kt`
- `app/src/main/java/com/electricsheep/app/auth/MfaManager.kt`

**Modify**:
- `app/src/main/java/com/electricsheep/app/auth/SupabaseAuthProvider.kt`
- `app/src/main/java/com/electricsheep/app/ui/screens/SettingsScreen.kt`
- `app/src/main/java/com/electricsheep/app/ui/screens/LoginScreen.kt`

### Supabase MFA API

**Key Methods**:
- `supabase.auth.mfa.enroll()` - Enroll in MFA
- `supabase.auth.mfa.verify()` - Verify MFA code
- `supabase.auth.mfa.unenroll()` - Disable MFA
- `supabase.auth.mfa.listFactors()` - List MFA factors

### Testing

- Test MFA enrollment
- Test MFA login flow
- Test MFA with authenticator app
- Test backup codes
- Test MFA disable
- Test error handling

---

## Implementation Timeline

### Week 1: Data Backup Encryption
- Day 1-2: Create backup rules
- Day 3-4: Configure encryption
- Day 5: Test and document

### Week 2-3: Certificate Pinning
- Week 2: Get certificate pins, configure HTTP client
- Week 3: Test, handle edge cases, document

### Month 1: MFA Support
- Week 1: Enable Supabase MFA, research API
- Week 2: Build MFA setup UI
- Week 3: Integrate MFA in auth flow
- Week 4: Add MFA management, test, document

---

## Success Criteria

### Data Backup Encryption
- [ ] Backup rules configured
- [ ] Encryption enabled
- [ ] Sensitive data excluded
- [ ] Backup/restore tested

### Certificate Pinning
- [ ] Certificate pins configured
- [ ] MITM attacks blocked
- [ ] Certificate rotation handled
- [ ] API calls work correctly

### MFA Support
- [ ] MFA enabled in Supabase
- [ ] MFA setup UI working
- [ ] MFA login flow working
- [ ] MFA management working
- [ ] Backup codes working

---

## Next Steps After Phase 2

**Phase 3** (3-6 months):
- Code obfuscation
- Root detection
- Account lockout
- Session timeout

**Phase 4** (6+ months):
- Advanced monitoring
- Threat modeling
- Security metrics

---

## Related Documentation

- [Security Principles](./SECURITY_PRINCIPLES.md) - Core principles
- [Holistic Risk Assessment](./HOLISTIC_RISK_ASSESSMENT.md) - Risk framework
- [Next Steps](./NEXT_STEPS.md) - General next steps

