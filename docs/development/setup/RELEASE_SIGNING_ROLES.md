# Release Signing - Who Does What?

**Date**: 2025-01-20  
**Purpose**: Clarify setup responsibilities for release signing

---

## 🎯 Key Principle

**There should be ONE keystore for the entire project**, not one per developer.

- ✅ **One keystore** → Used by CI/CD for all release builds
- ✅ **Shared securely** → Via GitHub Secrets (not in repo)
- ✅ **Backed up** → By admin/owner
- ❌ **NOT per-developer** → Developers don't need their own keystore

---

## 👤 Role Breakdown

### 🔐 Admin/Owner (One-Time Setup)

**Who**: Repository owner, project admin, or release manager

**What They Do** (ONE TIME, ~30 minutes):

1. **Generate Keystore** ✅
   - Run: `./scripts/setup-release-signing.sh`
   - Or manual: `./scripts/generate-keystore.sh release`
   - **This is the ONLY time the keystore is generated**

2. **Back Up Keystore** ✅
   - Copy to secure location (encrypted cloud storage)
   - Store passwords in password manager
   - Document location securely

3. **Set Up GitHub Secrets** ✅
   - Encode keystore to base64
   - Add 4 secrets to GitHub repository:
     - `KEYSTORE_FILE`
     - `KEYSTORE_PASSWORD`
     - `KEY_ALIAS`
     - `KEY_PASSWORD`

4. **Verify CI/CD** ✅
   - Push a test commit
   - Verify CI/CD builds signed AAB
   - Confirm everything works

**Result**: CI/CD can now build signed release AABs automatically.

**Frequency**: **Once** (unless keystore is lost/compromised)

---

### 👨‍💻 Developers (Optional Local Setup)

**Who**: Any developer working on the project

**What They Do** (OPTIONAL, ~2 minutes):

**Option 1: No Local Setup** (Recommended)
- ✅ Just work normally
- ✅ CI/CD handles signing automatically
- ✅ No local configuration needed
- ✅ Can still build debug APKs (unsigned)

**Option 2: Local Signing Setup** (If they want to test release builds locally)
- Add to `local.properties`:
  ```properties
  keystore.file=keystore/release.jks
  keystore.password=<from-admin>
  keystore.key.alias=release_key
  keystore.key.password=<from-admin>
  ```
- **Note**: They need the keystore file and passwords from admin
- **Use case**: Testing release builds locally before CI/CD

**Result**: Can build signed release APKs locally (optional).

**Frequency**: **Optional** - only if they want to test release builds locally

---

## 📋 Setup Scenarios

### Scenario 1: First-Time Project Setup (Admin)

**Who**: Project owner/admin

**Steps**:
1. Run: `./scripts/setup-release-signing.sh`
2. Back up keystore securely
3. Set up GitHub Secrets
4. Verify CI/CD works
5. **Share keystore file + passwords with team** (securely, via password manager)

**Time**: ~30 minutes (one-time)

**Result**: Project can now build signed releases in CI/CD

---

### Scenario 2: New Developer Joins (Developer)

**Who**: New team member

**Steps**:
1. **Nothing required** - CI/CD already works
2. (Optional) If they want local signing:
   - Get keystore file from admin (securely)
   - Get passwords from admin (via password manager)
   - Add to `local.properties`

**Time**: 0 minutes (or ~2 minutes if optional local setup)

**Result**: Can work normally, CI/CD handles signing

---

### Scenario 3: Developer Wants to Test Release Build Locally

**Who**: Any developer

**Steps**:
1. Get keystore file from admin (securely)
2. Get passwords from admin (via password manager)
3. Add to `local.properties`
4. Build: `./gradlew assembleRelease`

**Time**: ~2 minutes

**Result**: Can test release builds locally

---

## 🔒 Security Considerations

### Keystore Distribution

**✅ Secure Methods**:
- Password manager (1Password, LastPass, etc.)
- Encrypted file sharing (Signal, encrypted email)
- Secure file server (with access control)
- Encrypted USB drive (for in-person handoff)

**❌ Insecure Methods**:
- Email (unencrypted)
- Slack/chat (unencrypted)
- Public repositories
- Shared drives (unencrypted)

### Access Control

**Who Should Have Keystore Access**:
- ✅ Repository admins/owners
- ✅ Release managers
- ✅ CI/CD systems (via GitHub Secrets)
- ⚠️ Developers (only if they need to test locally)

**Who Should NOT Have Access**:
- ❌ External contributors
- ❌ Contractors (unless specifically needed)
- ❌ Anyone who doesn't need it

---

## 🎯 Recommended Workflow

### For Admins

1. **Initial Setup** (one-time):
   ```bash
   ./scripts/setup-release-signing.sh
   # Follow prompts
   # Back up keystore
   # Set GitHub Secrets
   ```

2. **Share with Team** (securely):
   - Upload keystore to password manager
   - Share passwords via password manager
   - Document in secure team wiki/notes

3. **Verify CI/CD**:
   - Push test commit
   - Verify signed AAB is produced
   - Confirm everything works

### For Developers

1. **Default**: Do nothing - CI/CD handles signing

2. **If Testing Locally** (optional):
   - Get keystore from password manager
   - Get passwords from password manager
   - Add to `local.properties`
   - Test: `./gradlew assembleRelease`

---

## ❓ Common Questions

### Q: Do I need to generate my own keystore?

**A**: No! There's one keystore for the entire project. Only the admin needs to generate it once.

### Q: Can I test release builds without the keystore?

**A**: Yes, but they'll be unsigned. For signed builds, you need the keystore from admin.

### Q: What if I lose the keystore?

**A**: Contact admin immediately. If lost, you'll need to create a new app listing on Play Store (losing all users/reviews).

### Q: Can multiple developers use the same keystore?

**A**: Yes! That's the point - one keystore for the project, shared securely.

### Q: Do I need GitHub Secrets access?

**A**: No, only admins need to set secrets. Developers just use CI/CD which already has access.

---

## 📊 Summary

| Role | Setup Required | Time | Frequency |
|------|---------------|------|-----------|
| **Admin/Owner** | Generate keystore, set GitHub Secrets | ~30 min | Once |
| **Developer** | Nothing (or optional local.properties) | 0-2 min | Optional |

**Key Point**: Only ONE person (admin) needs to do the full setup. Everyone else can work normally, and CI/CD handles signing automatically.

---

## 🔗 Related Documentation

- [Automated Setup](RELEASE_SIGNING_AUTOMATED_SETUP.md) - For admins doing initial setup
- [Manual Setup](RELEASE_SIGNING_MANUAL_SETUP.md) - Step-by-step guide
- [Release Signing Setup](RELEASE_SIGNING_SETUP.md) - Complete reference

---

**TL;DR**: Admin does setup once (~30 min). Developers do nothing (or optional 2-min local setup). CI/CD handles signing automatically.

