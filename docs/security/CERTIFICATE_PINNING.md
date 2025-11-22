# Certificate Pinning Implementation

**Last Updated**: 2025-01-22  
**Status**: Infrastructure Complete, Pins Needed  
**Risk Score**: 14.4 (P2)  
**Timeline**: 2 weeks

## Overview

Certificate pinning prevents man-in-the-middle (MITM) attacks by ensuring API calls only connect to legitimate Supabase servers. This implementation uses OkHttp's CertificatePinner with Ktor's OkHttp engine.

## Implementation Status

### ✅ Completed

1. **Certificate Pinner Configuration** (`CertificatePinnerConfig.kt`)
   - OkHttp CertificatePinner setup
   - Ktor HttpClient with OkHttp engine
   - Helper methods for pin extraction and validation

2. **Dependencies Added**
   - OkHttp 4.12.0 (for certificate pinning)

3. **Integration Started**
   - Certificate pinner config created
   - Ktor HttpClient with pinning configured
   - Integration with Supabase client (pending SDK API verification)

### ⚠️ Pending

1. **Get Actual Certificate Pins**
   - Extract pins from Supabase certificates
   - Update `CertificatePinnerConfig.kt` with real pins
   - Add backup pins for certificate rotation

2. **Verify Supabase SDK API**
   - Check if `createSupabaseClient` accepts `HttpClient` parameter
   - If not, use alternative approach (network security config)

3. **Testing**
   - Test with legitimate Supabase calls
   - Test MITM attack prevention
   - Test certificate rotation handling

## How to Get Certificate Pins

### Method 1: Extract from Certificate Chain

1. **Connect to Supabase API**:
   ```bash
   openssl s_client -connect <your-project>.supabase.co:443 -showcerts
   ```

2. **Save certificate to file**:
   ```bash
   openssl s_client -connect <your-project>.supabase.co:443 -showcerts | openssl x509 -outform PEM > cert.pem
   ```

3. **Extract public key and calculate SHA-256 hash**:
   ```bash
   openssl x509 -in cert.pem -pubkey -noout | \
     openssl pkey -pubin -outform der | \
     openssl dgst -sha256 -binary | \
     openssl enc -base64
   ```

4. **Format as pin**:
   ```
   sha256/<base64-hash>
   ```

### Method 2: Use Online Tools

1. Visit Supabase API in browser
2. Check certificate details
3. Extract public key hash
4. Format as pin

### Method 3: Use Certificate Pinner Helper

The `CertificatePinnerConfig.extractPin()` method can extract pins from certificates programmatically during development/testing.

## Certificate Pins Format

**Format**: `sha256/<base64-encoded-hash>`

**Example**:
```kotlin
.add("*.supabase.co", "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
.add("*.supabase.co", "sha256/BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB=") // Backup pin
```

## Multiple Pins (Certificate Rotation)

**Why Multiple Pins?**
- Support certificate rotation without app updates
- Provide backup pins if primary pin changes
- Ensure continuity during certificate renewals

**Best Practice**:
- Pin at least 2-3 public keys
- Include current certificate + next certificate (if known)
- Include intermediate CA certificates

## Integration with Supabase SDK

### Current Status

The Supabase Kotlin SDK's `createSupabaseClient` function may or may not accept a custom `HttpClient`. We need to verify:

1. **Check SDK Documentation**: Does it support custom HttpClient?
2. **Check SDK Source**: Review `createSupabaseClient` signature
3. **Alternative Approach**: If not supported, use Android Network Security Config

### Alternative: Network Security Config

If Supabase SDK doesn't support custom HttpClient, we can use Android's Network Security Config:

1. **Create `network_security_config.xml`**:
   ```xml
   <?xml version="1.0" encoding="utf-8"?>
   <network-security-config>
       <domain-config>
           <domain includeSubdomains="true">*.supabase.co</domain>
           <pin-set>
               <pin digest="SHA-256">AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=</pin>
               <pin digest="SHA-256">BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB=</pin>
           </pin-set>
       </domain-config>
   </network-security-config>
   ```

2. **Reference in AndroidManifest.xml**:
   ```xml
   <application
       android:networkSecurityConfig="@xml/network_security_config"
       ...>
   ```

## Testing

### Test 1: Verify Certificate Pinning Works

**Steps**:
1. Configure certificate pins
2. Make legitimate Supabase API call
3. Verify call succeeds

**Expected**: API call succeeds with pinned certificate

### Test 2: Verify MITM Prevention

**Steps**:
1. Set up MITM proxy (e.g., Burp Suite, Charles Proxy)
2. Configure proxy with different certificate
3. Make Supabase API call
4. Verify call fails with pinning error

**Expected**: API call fails with `CertificatePinner` exception

### Test 3: Verify Certificate Rotation

**Steps**:
1. Configure multiple pins (current + backup)
2. Simulate certificate rotation
3. Verify API calls still work

**Expected**: API calls succeed with backup pin

## Security Benefits

### Before Certificate Pinning

- ❌ Vulnerable to MITM attacks
- ❌ Malicious proxies can intercept API calls
- ❌ Authentication tokens can be stolen
- ❌ Data integrity not guaranteed

### After Certificate Pinning

- ✅ MITM attacks prevented
- ✅ Only legitimate Supabase servers accepted
- ✅ Authentication tokens protected
- ✅ Data integrity guaranteed

## Risk Mitigation

### Certificate Rotation Risk

**Risk**: If certificate changes and pins aren't updated, app breaks

**Mitigation**:
- Use multiple pins (current + backup)
- Monitor Supabase status for certificate updates
- Plan for certificate rotation (update pins before expiration)

### False Positives

**Risk**: Legitimate certificate changes cause app failures

**Mitigation**:
- Test certificate rotation scenarios
- Have rollback plan (disable pinning if needed)
- Monitor error rates

## Next Steps

1. **Get Certificate Pins**:
   - Extract pins from Supabase certificates
   - Update `CertificatePinnerConfig.kt`

2. **Verify SDK Integration**:
   - Check if Supabase SDK supports custom HttpClient
   - If not, implement Network Security Config approach

3. **Test Implementation**:
   - Test with legitimate calls
   - Test MITM prevention
   - Test certificate rotation

4. **Documentation**:
   - Update setup guide
   - Document certificate rotation process

## Related Documentation

- [Security Principles](./SECURITY_PRINCIPLES.md) - Core principles
- [Holistic Risk Assessment](./HOLISTIC_RISK_ASSESSMENT.md) - Risk framework
- [Phase 2 Implementation Plan](./PHASE_2_IMPLEMENTATION_PLAN.md) - Implementation plan

