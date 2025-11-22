# Data Backup Encryption Test Plan

**Last Updated**: 2025-01-22  
**Status**: Test Plan  
**Related**: [Data Backup Encryption](./DATA_BACKUP_ENCRYPTION.md)

## Overview

This test plan verifies that sensitive data is properly excluded from Android backups and that backup encryption is working correctly.

## Test Environment Setup

### Prerequisites

1. **Android Device or Emulator** (API 23+)
   - Android 6.0+ for full backup support
   - Android 12+ for data extraction rules

2. **ADB Access**
   ```bash
   adb devices  # Verify device connected
   ```

3. **Backup Enabled** (if not already)
   ```bash
   adb shell bmgr enable true
   ```

4. **App Installed**
   ```bash
   ./scripts/dev-reload.sh  # Build and install app
   ```

## Test Cases

### Test 1: Verify Backup Rules Configuration

**Objective**: Verify backup rules XML is properly configured

**Steps**:
1. Check backup rules file exists:
   ```bash
   adb shell run-as com.electricsheep.app ls -la /data/data/com.electricsheep.app/res/xml/
   # Should show backup_rules.xml
   ```

2. Verify AndroidManifest references backup rules:
   ```bash
   aapt dump xmltree app/build/outputs/apk/debug/app-debug.apk AndroidManifest.xml | grep -i backup
   # Should show:
   # - android:allowBackup="true"
   # - android:dataExtractionRules="@xml/backup_rules"
   # - android:fullBackupContent="@xml/backup_rules"
   ```

**Expected Result**:
- ✅ Backup rules file exists
- ✅ AndroidManifest references backup rules
- ✅ No errors in logcat

**Pass Criteria**: All checks pass

---

### Test 2: Verify Database Exclusion from Backup

**Objective**: Verify Room database is excluded from backups

**Steps**:
1. **Create test data**:
   - Launch app
   - Create a mood entry
   - Verify data in database:
     ```bash
     adb shell run-as com.electricsheep.app ls -la /data/data/com.electricsheep.app/databases/
     # Should show electric_sheep_database files
     ```

2. **Trigger backup**:
   ```bash
   adb shell bmgr backup com.electricsheep.app
   adb shell bmgr run
   ```

3. **Check backup contents** (requires root or backup inspection):
   ```bash
   # Note: Direct backup inspection may require root
   # Alternative: Check backup size (should be small if database excluded)
   adb shell bmgr list transports
   ```

4. **Verify database files exist locally** (should still be there):
   ```bash
   adb shell run-as com.electricsheep.app ls -la /data/data/com.electricsheep.app/databases/
   # Database files should still exist
   ```

**Expected Result**:
- ✅ Database files exist locally
- ✅ Backup triggered successfully
- ✅ Database files excluded from backup (backup size small)

**Pass Criteria**: Database exists locally but is excluded from backup

---

### Test 3: Verify Backup Restore (Data Exclusion)

**Objective**: Verify restored app doesn't include sensitive data

**Steps**:
1. **Create test data**:
   - Launch app
   - Create mood entries
   - Verify data exists:
     ```bash
     adb shell run-as com.electricsheep.app sqlite3 /data/data/com.electricsheep.app/databases/electric_sheep_database "SELECT COUNT(*) FROM moods;"
     # Should show > 0
     ```

2. **Trigger backup**:
   ```bash
   adb shell bmgr backup com.electricsheep.app
   adb shell bmgr run
   ```

3. **Uninstall app**:
   ```bash
   adb uninstall com.electricsheep.app
   ```

4. **Reinstall app**:
   ```bash
   ./scripts/dev-reload.sh
   ```

5. **Verify database is empty** (restored from backup):
   ```bash
   adb shell run-as com.electricsheep.app sqlite3 /data/data/com.electricsheep.app/databases/electric_sheep_database "SELECT COUNT(*) FROM moods;"
   # Should show 0 (database excluded from backup)
   ```

6. **Verify app preferences restored** (if included):
   - Check app settings (theme, etc.)
   - Should be restored if included in backup rules

**Expected Result**:
- ✅ App reinstalled successfully
- ✅ Database is empty (excluded from backup)
- ✅ App preferences restored (if included)

**Pass Criteria**: Database excluded, preferences restored

---

### Test 4: Verify Backup Encryption

**Objective**: Verify backups are encrypted

**Steps**:
1. **Check backup encryption status**:
   ```bash
   adb shell dumpsys backup | grep -i encryption
   # Should show encryption enabled
   ```

2. **Verify backup transport**:
   ```bash
   adb shell bmgr list transports
   # Should show active transport (Google Drive, etc.)
   ```

3. **Check backup metadata**:
   - Android 6.0+ automatically encrypts full backups
   - Encryption uses device encryption key
   - No manual configuration needed

**Expected Result**:
- ✅ Backup encryption enabled (Android 6.0+)
- ✅ Backup transport configured
- ✅ No errors in logcat

**Pass Criteria**: Encryption enabled and working

---

### Test 5: Verify Multiple Android Versions

**Objective**: Verify backup rules work on different Android versions

**Test on**:
- Android 6.0 (API 23) - Full backup support
- Android 12+ (API 31+) - Data extraction rules

**Steps**:
1. Test on Android 6.0:
   - Verify `fullBackupContent` works
   - Verify database excluded
   - Verify backup/restore works

2. Test on Android 12+:
   - Verify `dataExtractionRules` works
   - Verify database excluded
   - Verify backup/restore works

**Expected Result**:
- ✅ Works on Android 6.0+
- ✅ Works on Android 12+
- ✅ Database excluded on all versions

**Pass Criteria**: Works on all supported Android versions

---

### Test 6: Verify App Preferences Inclusion

**Objective**: Verify non-sensitive preferences are included in backup

**Steps**:
1. **Set app preferences**:
   - Change theme (if applicable)
   - Set app settings
   - Verify preferences stored:
     ```bash
     adb shell run-as com.electricsheep.app cat /data/data/com.electricsheep.app/shared_prefs/app_preferences.xml
     # Should show preferences
     ```

2. **Trigger backup and restore**:
   ```bash
   adb shell bmgr backup com.electricsheep.app
   adb shell bmgr run
   adb uninstall com.electricsheep.app
   ./scripts/dev-reload.sh
   ```

3. **Verify preferences restored**:
   - Check app settings
   - Should match previous settings

**Expected Result**:
- ✅ Preferences included in backup
- ✅ Preferences restored after reinstall

**Pass Criteria**: Preferences backed up and restored

---

## Manual Testing Checklist

### Pre-Test Setup
- [ ] Device/emulator connected via ADB
- [ ] Backup enabled (`bmgr enable true`)
- [ ] App installed and running
- [ ] Test data created (mood entries)

### Test Execution
- [ ] Test 1: Backup rules configuration
- [ ] Test 2: Database exclusion
- [ ] Test 3: Backup restore (data exclusion)
- [ ] Test 4: Backup encryption
- [ ] Test 5: Multiple Android versions
- [ ] Test 6: App preferences inclusion

### Post-Test Verification
- [ ] All tests passed
- [ ] No errors in logcat
- [ ] Database excluded from backup
- [ ] Preferences included in backup
- [ ] Backup encryption working

## Automated Testing (Future)

### Potential Automation

**Unit Tests**:
- Verify backup rules XML is valid
- Verify AndroidManifest references backup rules

**Instrumented Tests**:
- Test backup/restore flow
- Verify database exclusion
- Verify preferences inclusion

**CI/CD Integration**:
- Validate backup rules XML
- Check AndroidManifest configuration
- Lint backup rules

## Troubleshooting

### Backup Not Working

**Issue**: Backup not triggered

**Solutions**:
- Check backup enabled: `adb shell bmgr enable true`
- Check backup transport: `adb shell bmgr list transports`
- Check app package name: `adb shell pm list packages | grep electric`

### Database Not Excluded

**Issue**: Database included in backup

**Solutions**:
- Verify backup rules XML exists
- Verify AndroidManifest references backup rules
- Check database file names match exclusion rules
- Verify app rebuilt after changes

### Preferences Not Restored

**Issue**: Preferences not included in backup

**Solutions**:
- Verify preferences file name matches inclusion rules
- Check SharedPreferences file exists
- Verify backup rules include preferences

## Test Results Template

```
Test Date: [Date]
Tester: [Name]
Device: [Device/Emulator]
Android Version: [Version]
API Level: [API Level]

Test Results:
- Test 1: [PASS/FAIL] - [Notes]
- Test 2: [PASS/FAIL] - [Notes]
- Test 3: [PASS/FAIL] - [Notes]
- Test 4: [PASS/FAIL] - [Notes]
- Test 5: [PASS/FAIL] - [Notes]
- Test 6: [PASS/FAIL] - [Notes]

Overall Result: [PASS/FAIL]
Issues Found: [List issues]
```

## Related Documentation

- [Data Backup Encryption](./DATA_BACKUP_ENCRYPTION.md) - Implementation details
- [Security Principles](./SECURITY_PRINCIPLES.md) - Security guidelines
- [Phase 2 Implementation Plan](./PHASE_2_IMPLEMENTATION_PLAN.md) - Implementation plan

