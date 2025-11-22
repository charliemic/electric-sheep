# Release Signing - Automated Setup

**Date**: 2025-01-20  
**Purpose**: Automated setup script for release signing  
**Estimated Time**: ~10 minutes (vs. ~30 minutes manual)

---

## ⚠️ Important: Who Should Run This?

**Admin/Owner**: Run this ONCE to set up the project's keystore and GitHub Secrets.

**Developers**: Usually DON'T need to run this. CI/CD handles signing automatically. Only run if you want to test release builds locally.

**Key Principle**: There should be ONE keystore for the entire project, shared securely via GitHub Secrets. Developers don't need their own keystore.

See [Release Signing Roles](RELEASE_SIGNING_ROLES.md) for details on who does what.

---

## 🚀 Quick Start

Run the automated setup script:

```bash
./scripts/setup-release-signing.sh
```

The script will guide you through:
1. ✅ Keystore generation (with secure password prompts)
2. ✅ Local configuration (automatically updates `local.properties`)
3. ✅ Local signing test (optional)
4. ✅ GitHub Secrets setup (optional, requires GitHub CLI)

---

## What Gets Automated

### ✅ Fully Automated

1. **Keystore Generation**
   - Runs the keystore generation script
   - Handles file existence checks
   - Prompts for backup confirmation

2. **Local Configuration**
   - Automatically updates `local.properties`
   - Handles existing configuration
   - Verifies gitignore status

3. **Local Signing Test**
   - Builds release APK
   - Verifies signature
   - Reports results

4. **GitHub Secrets** (if GitHub CLI installed)
   - Encodes keystore to base64
   - Sets all 4 secrets automatically
   - Verifies authentication

### ⚠️ Still Requires Input

1. **Passwords** (entered securely)
   - Keystore password
   - Key password
   - Key alias (with default)

2. **Certificate Information** (during keystore generation)
   - Name, organization, location, etc.

3. **Confirmations**
   - Backup confirmation
   - Overwrite confirmations
   - Test confirmations

---

## Prerequisites

### Required

- ✅ Java JDK (for `keytool`)
- ✅ `base64` command (usually pre-installed)
- ✅ Gradle (for testing)

### Optional (for GitHub Secrets automation)

- ✅ GitHub CLI (`gh`) - Install: `brew install gh` (macOS)
- ✅ GitHub CLI authenticated - Run: `gh auth login`

---

## Usage

### Basic Usage

```bash
./scripts/setup-release-signing.sh
```

### What Happens

1. **Prerequisites Check**
   - Verifies `keytool` is available
   - Checks for `base64` command

2. **Keystore Generation**
   - Prompts to run keystore generation
   - Asks for backup confirmation
   - Runs `generate-keystore.sh`

3. **Password Collection**
   - Securely prompts for keystore password
   - Securely prompts for key password
   - Prompts for key alias (default: `release_key`)

4. **Local Configuration**
   - Updates `local.properties` automatically
   - Handles existing configuration

5. **Local Testing** (optional)
   - Builds release APK
   - Verifies signature
   - Reports results

6. **GitHub Secrets** (optional)
   - Checks for GitHub CLI
   - Encodes keystore
   - Sets secrets automatically

---

## Example Output

```
╔════════════════════════════════════════════════════════════╗
║      Automated Release Signing Setup                      ║
╚════════════════════════════════════════════════════════════╝

🔍 Checking prerequisites...
✅ Prerequisites met

════════════════════════════════════════════════════════════
Step 1: Generate Keystore
════════════════════════════════════════════════════════════

📝 Running keystore generation script...
   You'll be prompted for passwords and certificate information

[... keystore generation prompts ...]

✅ Keystore generated successfully

⚠️  CRITICAL: Back up your keystore file now!
   Location: keystore/release.jks
   Press Enter after you've backed up the keystore...

════════════════════════════════════════════════════════════
Step 2: Configure Local Development
════════════════════════════════════════════════════════════

Enter keystore password: [hidden]
Enter key password (can be same as keystore password): [hidden]
Enter key alias [release_key]: 

📝 Configuring local.properties...
✅ local.properties configured

════════════════════════════════════════════════════════════
Step 3: Test Local Signing
════════════════════════════════════════════════════════════

Do you want to test local signing now? (yes/no) [yes]: 

🔨 Building release APK...
✅ Release APK built successfully

🔍 Verifying APK signature...
✅ APK is signed correctly

════════════════════════════════════════════════════════════
Step 4: GitHub Secrets Setup (Optional)
════════════════════════════════════════════════════════════

Do you want to set up GitHub Secrets now? (yes/no) [no]: yes

🔐 Setting up GitHub Secrets...
📦 Encoding keystore to base64...
🔐 Adding GitHub Secrets...
✅ GitHub Secrets configured

╔════════════════════════════════════════════════════════════╗
║                    Setup Complete                         ║
╚════════════════════════════════════════════════════════════╝

✅ What's been configured:
   - Keystore file: keystore/release.jks
   - Local properties: local.properties
   - Local signing: Tested ✅
   - GitHub Secrets: Configured ✅

✨ Setup complete! You can now build signed release builds.
```

---

## Manual Steps (If Needed)

### If GitHub CLI Not Available

If you don't have GitHub CLI installed or prefer manual setup:

1. **Encode keystore manually**:
   ```bash
   base64 -i keystore/release.jks | pbcopy  # macOS
   ```

2. **Add secrets manually**:
   - Go to: GitHub Settings → Secrets and variables → Actions
   - Add 4 secrets:
     - `KEYSTORE_FILE` (paste base64)
     - `KEYSTORE_PASSWORD`
     - `KEY_ALIAS`
     - `KEY_PASSWORD`

### If Script Fails

If the script encounters issues:

1. **Check prerequisites**:
   ```bash
   which keytool
   which base64
   which gh  # optional
   ```

2. **Run manual setup**:
   - Follow: `docs/development/setup/RELEASE_SIGNING_MANUAL_SETUP.md`

3. **Check script logs**:
   - Script uses `set -e` (exits on error)
   - Error messages will indicate what failed

---

## Security Notes

### ✅ Secure Handling

- Passwords entered via `read -sp` (hidden input)
- Passwords not logged or displayed
- Keystore file remains in gitignored directory
- GitHub Secrets set via secure CLI

### ⚠️ Important Reminders

- **Back up keystore** - Script prompts for confirmation
- **Store passwords** - In password manager
- **Verify secrets** - Check GitHub after setup

---

## Troubleshooting

### Script Fails: "keytool not found"

**Solution**: Install Java JDK
```bash
# macOS
brew install openjdk@17

# Verify
which keytool
```

### Script Fails: "base64 not found"

**Solution**: Usually pre-installed, but if missing:
```bash
# macOS (should already be installed)
# Linux (should already be installed)
# If missing, install coreutils
```

### GitHub Secrets Setup Fails

**Problem**: GitHub CLI not authenticated

**Solution**:
```bash
gh auth login
# Follow prompts to authenticate
```

### Local Signing Test Fails

**Problem**: Build errors or signature verification fails

**Solution**:
1. Check `local.properties` configuration
2. Verify keystore file exists
3. Check passwords are correct
4. Run manually: `./gradlew assembleRelease`

---

## Comparison: Automated vs. Manual

| Step | Manual Time | Automated Time |
|------|-------------|----------------|
| Generate keystore | 5 min | 5 min (same - requires input) |
| Configure local.properties | 2 min | 30 sec (automated) |
| Test local signing | 5 min | 2 min (automated) |
| GitHub Secrets | 10 min | 2 min (automated) |
| **Total** | **~30 min** | **~10 min** |

**Time Saved**: ~20 minutes (67% faster)

---

## Next Steps

After running the script:

1. **Verify Setup**:
   ```bash
   # Test local build
   ./gradlew assembleRelease
   
   # Verify signature
   jarsigner -verify app/build/outputs/apk/release/app-release.apk
   ```

2. **Test CI/CD** (if GitHub Secrets configured):
   - Push a commit
   - Check GitHub Actions workflow
   - Verify signed AAB is produced

3. **Back Up Keystore** (if not done):
   - Copy to secure location
   - Store passwords in password manager

---

## Related Documentation

- [Manual Setup Guide](RELEASE_SIGNING_MANUAL_SETUP.md) - Step-by-step manual process
- [Release Signing Setup](RELEASE_SIGNING_SETUP.md) - Complete reference
- [Release Signing Status](../RELEASE_SIGNING_STATUS.md) - Implementation status

---

**Status**: Ready to use - Run `./scripts/setup-release-signing.sh` to get started!

