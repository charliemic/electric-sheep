# Release Signing - Manual Setup Walkthrough

**Date**: 2025-01-20  
**Purpose**: Step-by-step guide for setting up release signing  
**Estimated Time**: ~30 minutes

---

## 📋 Overview

This guide walks you through the complete manual setup process for release signing. You'll need to:

1. Generate a keystore file
2. Configure local development
3. Set up GitHub Secrets for CI/CD
4. Verify everything works

**⚠️ IMPORTANT**: Back up your keystore file securely! If you lose it, you cannot update your app on the Play Store.

---

## Step 1: Generate Keystore (5 minutes)

### 1.1 Run the Keystore Generation Script

```bash
./scripts/generate-keystore.sh release
```

### 1.2 Follow the Prompts

The script will ask you for:

1. **Keystore password** (store this securely!)
   - Choose a strong password
   - Store in password manager
   - You'll need this for every build

2. **Key alias** (default: `release_key`)
   - Press Enter to accept default, or enter custom alias
   - Remember this value

3. **Key password** (can be same as keystore password)
   - Can be same as keystore password for simplicity
   - Or use a different password for extra security

4. **Certificate information**:
   - Your name
   - Organizational unit
   - Organization
   - City/Locality
   - State/Province
   - Country code (2 letters, e.g., US, GB)

### 1.3 Verify Keystore Created

After completion, verify the keystore file exists:

```bash
ls -la keystore/release.jks
```

You should see the file listed.

### 1.4 Back Up Keystore (CRITICAL!)

**⚠️ DO THIS IMMEDIATELY** - If you lose this file, you cannot update your app!

```bash
# Copy to secure location (encrypted cloud storage, USB drive, etc.)
cp keystore/release.jks ~/secure-backup/electric-sheep-keystore.jks

# Or compress and encrypt
zip -e keystore-backup.zip keystore/release.jks
# Move keystore-backup.zip to secure location
```

**Recommended Backup Locations**:
- Encrypted cloud storage (iCloud, Google Drive with encryption)
- Password manager (some support file attachments)
- Encrypted USB drive
- Secure file server

**Also Document** (securely):
- Keystore password
- Key alias
- Key password
- Keystore file location

---

## Step 2: Configure Local Development (2 minutes)

### 2.1 Open local.properties

```bash
# If file doesn't exist, create it
touch local.properties
```

### 2.2 Add Keystore Configuration

Add these lines to `local.properties`:

```properties
# Keystore Configuration (for local release builds)
keystore.file=keystore/release.jks
keystore.password=<your-keystore-password>
keystore.key.alias=release_key
keystore.key.password=<your-key-password>
```

**Replace**:
- `<your-keystore-password>` with the password you entered when generating the keystore
- `<your-key-password>` with the key password you entered
- `release_key` with your key alias if you used a different one

**Example**:
```properties
# Keystore Configuration (for local release builds)
keystore.file=keystore/release.jks
keystore.password=MySecurePassword123!
keystore.key.alias=release_key
keystore.key.password=MySecurePassword123!
```

### 2.3 Verify local.properties is Gitignored

```bash
# Check that local.properties is in .gitignore
grep local.properties .gitignore
```

You should see `local.properties` listed. If not, it's already gitignored by default in Android projects.

---

## Step 3: Test Local Signing (5 minutes)

### 3.1 Build Release APK

```bash
./gradlew assembleRelease
```

**Expected Output**:
- Build should complete successfully
- If keystore is configured correctly, you'll see signing messages
- APK will be created at: `app/build/outputs/apk/release/app-release.apk`

### 3.2 Verify APK is Signed

```bash
jarsigner -verify -verbose -certs app/build/outputs/apk/release/app-release.apk
```

**Expected Output**:
```
jar verified.

Warning: 
This jar contains entries whose certificate chain is not validated.
...
```

The "jar verified" message confirms the APK is signed.

### 3.3 (Optional) Install and Test

```bash
# Install on connected device
adb install app/build/outputs/apk/release/app-release.apk

# Or install on emulator
adb -s emulator-5554 install app/build/outputs/apk/release/app-release.apk
```

Verify the app runs correctly.

---

## Step 4: Set Up GitHub Secrets for CI/CD (10 minutes)

### 4.1 Encode Keystore to Base64

**macOS**:
```bash
base64 -i keystore/release.jks | pbcopy
```

**Linux**:
```bash
base64 -i keystore/release.jks | xclip
```

**Windows (PowerShell)**:
```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("keystore\release.jks")) | Set-Clipboard
```

The base64-encoded keystore is now in your clipboard.

### 4.2 Add GitHub Secrets

1. **Go to GitHub Repository**:
   - Navigate to: `https://github.com/charliemic/electric-sheep`
   - Click **Settings** → **Secrets and variables** → **Actions**

2. **Add KEYSTORE_FILE Secret**:
   - Click **New repository secret**
   - Name: `KEYSTORE_FILE`
   - Value: Paste the base64-encoded keystore (from clipboard)
   - Click **Add secret**

3. **Add KEYSTORE_PASSWORD Secret**:
   - Click **New repository secret**
   - Name: `KEYSTORE_PASSWORD`
   - Value: Your keystore password
   - Click **Add secret**

4. **Add KEY_ALIAS Secret**:
   - Click **New repository secret**
   - Name: `KEY_ALIAS`
   - Value: Your key alias (e.g., `release_key`)
   - Click **Add secret**

5. **Add KEY_PASSWORD Secret**:
   - Click **New repository secret**
   - Name: `KEY_PASSWORD`
   - Value: Your key password
   - Click **Add secret**

### 4.3 Verify Secrets Added

You should now see 4 secrets listed:
- ✅ `KEYSTORE_FILE`
- ✅ `KEYSTORE_PASSWORD`
- ✅ `KEY_ALIAS`
- ✅ `KEY_PASSWORD`

---

## Step 5: Test CI/CD Signing (5 minutes)

### 5.1 Trigger CI/CD Build

```bash
# Make a small change and push
git add .
git commit -m "test: verify CI/CD signing"
git push origin <your-branch>
```

Or create a test commit:

```bash
# Create a test file
echo "# CI/CD Signing Test" > test-signing.md
git add test-signing.md
git commit -m "test: verify CI/CD signing"
git push origin <your-branch>
```

### 5.2 Check Workflow Run

1. Go to GitHub repository
2. Click **Actions** tab
3. Find your workflow run
4. Click on the run to see details

### 5.3 Verify Keystore Setup

In the workflow logs, look for the "Build release AAB" job:

1. **Check for keystore setup**:
   ```
   ✅ Keystore file created from GitHub Secrets
   ```

2. **Check build output**:
   - Should complete successfully
   - Should produce signed AAB

### 5.4 Download and Verify AAB

1. **Download AAB artifact**:
   - In workflow run, find "Artifacts" section
   - Download the AAB file

2. **Verify AAB is signed** (requires bundletool):
   ```bash
   # Install bundletool if needed
   # macOS: brew install bundletool
   
   bundletool verify --bundle=app-release.aab
   ```

   Or use Android Studio:
   - Open Android Studio
   - Build → Analyze APK
   - Select the AAB file
   - Check "Signing" section shows certificate

---

## Step 6: Clean Up Test Files (Optional)

```bash
# Remove test file if created
rm test-signing.md
git add test-signing.md
git commit -m "chore: remove test file"
git push
```

---

## ✅ Verification Checklist

### Local Setup
- [ ] Keystore file generated: `keystore/release.jks` exists
- [ ] Keystore backed up securely
- [ ] `local.properties` configured with keystore settings
- [ ] Local release build succeeds: `./gradlew assembleRelease`
- [ ] APK signature verified: `jarsigner -verify` succeeds
- [ ] App installs and runs correctly

### CI/CD Setup
- [ ] GitHub Secrets added (4 secrets)
- [ ] CI/CD workflow runs successfully
- [ ] Keystore setup message appears in logs
- [ ] Release AAB builds successfully
- [ ] AAB is signed (verified)

---

## 🔒 Security Reminders

### ✅ DO
- ✅ Back up keystore file securely
- ✅ Store passwords in password manager
- ✅ Use GitHub Secrets for CI/CD (never commit secrets)
- ✅ Test restore process periodically
- ✅ Document keystore location (securely)

### ❌ DON'T
- ❌ Commit keystore file to git
- ❌ Share keystore passwords in chat/email
- ❌ Store keystore in public repositories
- ❌ Lose your keystore backup
- ❌ Use weak passwords

---

## 🐛 Troubleshooting

### Build Fails: "Keystore file not found"

**Problem**: Keystore path is incorrect.

**Solution**:
- Check `keystore.file` path in `local.properties`
- Use relative path: `keystore/release.jks`
- Or absolute path: `/full/path/to/keystore/release.jks`
- Verify file exists: `ls -la keystore/release.jks`

### Build Fails: "Keystore was tampered with, or password was incorrect"

**Problem**: Wrong password.

**Solution**:
- Verify password in `local.properties` matches keystore password
- Check for special characters that need escaping
- Try regenerating keystore if password is lost (⚠️ you'll need new app listing)

### CI/CD Build Fails: "Keystore file not found"

**Problem**: GitHub Secrets not configured or incorrect.

**Solution**:
- Verify all 4 secrets are set in GitHub
- Check that `KEYSTORE_FILE` is base64 encoded correctly
- Verify environment variables are set in workflow
- Check workflow logs for specific errors

### CI/CD Build Succeeds But AAB Is Unsigned

**Problem**: Signing config not applied.

**Solution**:
- Check workflow logs for keystore setup message
- Verify all 4 secrets are set correctly
- Check that environment variables are passed to build step
- Verify signing config in `build.gradle.kts` is correct

---

## 📊 Setup Summary

**Time Breakdown**:
- Step 1 (Generate Keystore): ~5 minutes
- Step 2 (Local Config): ~2 minutes
- Step 3 (Test Local): ~5 minutes
- Step 4 (GitHub Secrets): ~10 minutes
- Step 5 (Test CI/CD): ~5 minutes
- **Total**: ~30 minutes

**What You'll Have**:
- ✅ Keystore file (backed up securely)
- ✅ Local signing configured
- ✅ CI/CD signing configured
- ✅ Verified working setup

**What You Can Do**:
- ✅ Build signed release APK/AAB locally
- ✅ Build signed AAB in CI/CD automatically
- ✅ Upload to Google Play Store
- ✅ Distribute to users

---

## 🎯 Next Steps

After completing setup:

1. **Test a full release build**:
   ```bash
   ./gradlew bundleRelease
   ```

2. **Prepare for Play Store**:
   - Create Play Console account (if not done)
   - Set up app listing
   - Prepare screenshots and descriptions

3. **Consider Future Automation**:
   - Fastlane for automated uploads
   - Release notes automation
   - Staged rollout configuration

---

## 🔗 Related Documentation

- [Release Signing Setup Guide](RELEASE_SIGNING_SETUP.md) - Complete reference
- [Release Signing Status](../RELEASE_SIGNING_STATUS.md) - Implementation status
- [Release Signing Setup Complete](../RELEASE_SIGNING_SETUP_COMPLETE.md) - Quick checklist
- [GitHub Issue #52](https://github.com/charliemic/electric-sheep/issues/52) - Original issue

---

**Remember**: Your keystore is critical for app updates. Protect it like you would protect your bank account password!

**Status**: Ready for setup - follow steps above to complete configuration

