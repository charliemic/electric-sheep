# Release Build Signing Setup

**Last Updated**: 2025-01-20  
**Purpose**: Guide for setting up Android release build signing

## Overview

Release builds must be signed before they can be uploaded to the Google Play Store. This guide explains how to configure release signing for the Electric Sheep Android app.

---

## Quick Start

### 1. Generate Keystore

```bash
./scripts/generate-keystore.sh release
```

This will:
- Create a keystore file in `keystore/release.jks` (gitignored)
- Prompt you for keystore password, key alias, and key password
- Generate a keystore valid for 25 years (Play Store requirement)

### 2. Configure local.properties

Add keystore configuration to `local.properties`:

```properties
# Keystore Configuration (for local release builds)
keystore.file=keystore/release.jks
keystore.password=your-keystore-password
keystore.key.alias=release_key
keystore.key.password=your-key-password
```

**⚠️ IMPORTANT**: 
- Never commit `local.properties` to git (it's already gitignored)
- Store passwords securely (password manager, secure notes)
- Back up your keystore file (you'll need it for app updates)

### 3. Test Release Build

```bash
./gradlew assembleRelease
```

The release APK/AAB will be signed if keystore configuration is present.

---

## How It Works

### Build Configuration

The signing configuration is in `app/build.gradle.kts`:

```kotlin
signingConfigs {
    create("release") {
        // Reads from environment variables (CI/CD) or local.properties (local)
        val keystoreFile = readProperty("keystore.file", "")
        val keystorePassword = readProperty("keystore.password", "")
        // ... etc
    }
}

buildTypes {
    release {
        signingConfig = signingConfigs.getByName("release")
        // ... other config
    }
}
```

### Configuration Priority

1. **Environment Variables** (for CI/CD)
   - `KEYSTORE_FILE`
   - `KEYSTORE_PASSWORD`
   - `KEY_ALIAS`
   - `KEY_PASSWORD`

2. **local.properties** (for local development)
   - `keystore.file`
   - `keystore.password`
   - `keystore.key.alias`
   - `keystore.key.password`

3. **Defaults** (empty - signing disabled if not configured)

---

## CI/CD Setup

### GitHub Secrets

Add these secrets to your GitHub repository:

1. **KEYSTORE_FILE** (base64 encoded keystore file)
   ```bash
   # Encode keystore to base64
   base64 -i keystore/release.jks | pbcopy  # macOS
   base64 -i keystore/release.jks | xclip   # Linux
   ```

2. **KEYSTORE_PASSWORD** (your keystore password)

3. **KEY_ALIAS** (your key alias, e.g., `release_key`)

4. **KEY_PASSWORD** (your key password)

### GitHub Actions Workflow

The CI/CD workflow will automatically use these secrets when building release AABs. The signing configuration reads from environment variables, which GitHub Actions provides from secrets.

**Example Workflow Step** (add to `.github/workflows/build-and-test.yml`):

```yaml
- name: Setup Keystore
  run: |
    mkdir -p keystore
    echo "${{ secrets.KEYSTORE_FILE }}" | base64 -d > keystore/release.jks
    
- name: Build Release AAB
  env:
    KEYSTORE_FILE: keystore/release.jks
    KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
    KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
    KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
  run: ./gradlew bundleRelease
```

**Note**: 
- The signing configuration supports both `KEYSTORE_FILE` (uppercase, CI/CD convention) and `keystore.file` (lowercase, local.properties format)
- The keystore file is decoded from base64 and placed in the `keystore/` directory
- Environment variables are automatically used by the signing configuration

---

## Keystore Management

### Backup Strategy

**CRITICAL**: Back up your keystore file securely. If you lose it:
- You cannot update your app on the Play Store
- You'll need to create a new app listing (losing all users, reviews, etc.)

**Recommended Backup**:
1. Store keystore file in secure cloud storage (encrypted)
2. Store passwords in password manager
3. Document keystore location and passwords (securely)
4. Test restore process periodically

### Keystore Security

- ✅ Keystore file is gitignored (never committed)
- ✅ Passwords stored in local.properties (gitignored)
- ✅ CI/CD uses GitHub Secrets (encrypted)
- ✅ Keystore directory excluded from git

### Multiple Keystores

You can create multiple keystores for different purposes:

```bash
# Release keystore
./scripts/generate-keystore.sh release

# Staging keystore (if needed)
./scripts/generate-keystore.sh staging
```

Update `local.properties` to switch between keystores.

---

## Troubleshooting

### Build Fails: "Keystore file not found"

**Problem**: Keystore file path is incorrect.

**Solution**: 
- Check `keystore.file` path in `local.properties`
- Use relative path from project root: `keystore/release.jks`
- Or absolute path: `/full/path/to/keystore/release.jks`

### Build Fails: "Keystore was tampered with, or password was incorrect"

**Problem**: Wrong keystore password.

**Solution**:
- Verify password in `local.properties`
- Check if password has special characters that need escaping
- Regenerate keystore if password is lost (⚠️ you'll need a new app listing)

### Build Succeeds But APK/AAB Is Unsigned

**Problem**: Signing config not applied.

**Solution**:
- Check that `signingConfig` is set in release build type
- Verify keystore configuration is correct
- Check build logs for signing errors

### CI/CD Build Fails

**Problem**: GitHub Secrets not configured or incorrect.

**Solution**:
- Verify all secrets are set in GitHub repository settings
- Check that keystore file is base64 encoded correctly
- Verify environment variables are set in workflow
- Check workflow logs for specific errors

---

## Verification

### Check APK/AAB Signature

```bash
# Check APK signature
jarsigner -verify -verbose -certs app/build/outputs/apk/release/app-release.apk

# Check AAB signature (requires bundletool)
bundletool verify --bundle=app/build/outputs/bundle/release/app-release.aab
```

### Test Release Build

1. Build release APK:
   ```bash
   ./gradlew assembleRelease
   ```

2. Install on device:
   ```bash
   adb install app/build/outputs/apk/release/app-release.apk
   ```

3. Verify app runs correctly

---

## Related Documentation

- [Android App Gap Analysis](../../analysis/ANDROID_APP_GAP_ANALYSIS.md) - Identified signing as a gap
- [GitHub Issue #52](https://github.com/charliemic/electric-sheep/issues/52) - Original issue
- [Android Signing Documentation](https://developer.android.com/studio/publish/app-signing) - Official Android docs

---

## Security Reminders

⚠️ **NEVER**:
- Commit keystore files to git
- Share keystore passwords in chat/email
- Store keystore in public repositories
- Lose your keystore backup

✅ **ALWAYS**:
- Back up keystore file securely
- Store passwords in password manager
- Use GitHub Secrets for CI/CD
- Test restore process periodically

---

**Remember**: Your keystore is critical for app updates. Protect it like you would protect your bank account password!

