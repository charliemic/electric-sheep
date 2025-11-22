# Data Collection Inventory

**Project**: Electric Sheep  
**Last Updated**: [Date]  
**Status**: Internal Document

## Purpose

This document tracks what data is collected, where it's stored, and how it's used. This information is used to:
- Create accurate Privacy Policy
- Ensure compliance with regulations (GDPR, CCPA)
- Document data handling practices
- Support Play Store privacy policy requirements

## Data Collection Summary

### Personal Data Collected

| Data Type | Collection Method | Storage Location | Retention | Purpose |
|-----------|------------------|------------------|-----------|---------|
| Email Address | User input (auth) | Supabase | While account active | Authentication |
| Mood Entries | User input | Local DB + Supabase | While account active | App functionality |
| Timestamps | Auto-generated | Local DB + Supabase | While account active | App functionality |
| Notes/Text | User input | Local DB + Supabase | While account active | App functionality |

### Technical Data Collected

| Data Type | Collection Method | Storage Location | Retention | Purpose |
|-----------|------------------|------------------|-----------|---------|
| Device Info | Auto-collected | Local only | Not persisted | Error reporting |
| Error Logs | Auto-collected | Local only | Not persisted | Debugging |
| Usage Patterns | Auto-collected | Local only | Not persisted | App improvement |

## Data Storage Details

### Local Storage
- **Location**: Android device (Room database)
- **Encryption**: Android system encryption
- **Access**: App-only (sandboxed)
- **Retention**: Until app uninstalled

### Cloud Storage
- **Provider**: Supabase
- **Location**: [Supabase region/data center]
- **Encryption**: Encrypted in transit (HTTPS) and at rest
- **Access**: Authenticated users only
- **Retention**: While account active, deleted within 30 days of account deletion

## Data Sharing

### Third-Party Services

| Service | Data Shared | Purpose | Privacy Policy |
|---------|-------------|---------|----------------|
| Supabase | All app data | Backend storage/sync | https://supabase.com/privacy |
| Google Play | App usage stats (if published) | Analytics | https://policies.google.com/privacy |

### No Data Sharing With
- ❌ Advertising networks
- ❌ Analytics services (third-party)
- ❌ Social media platforms
- ❌ Data brokers

## User Rights

### Access
- Users can view all their data in the app
- Users can export data (if feature implemented)
- Users can request data copy via email

### Deletion
- Users can delete individual entries in app
- Users can delete account (deletes all data)
- Data deleted within 30 days of account deletion

### Correction
- Users can edit their data in app
- Users can update email address

### Portability
- Users can export data (if feature implemented)
- Data format: JSON or CSV

## Security Measures

- **Encryption**: Data encrypted in transit (HTTPS) and at rest
- **Authentication**: Required for cloud data access
- **Access Control**: Users can only access their own data
- **Local Security**: Android app sandboxing

## Compliance Status

### GDPR (EU)
- ✅ Privacy Policy created
- ✅ Legal basis documented (consent)
- ✅ User rights documented
- ✅ Data retention policies defined
- ⚠️ Data Processing Agreement with Supabase (check Supabase terms)

### CCPA (California)
- ✅ Privacy Policy created
- ✅ Data collection disclosed
- ✅ User rights documented
- ✅ No data sale (explicitly stated)

### Google Play Store
- ✅ Privacy Policy (if publishing)
- ✅ Data collection disclosure
- ✅ Data sharing disclosure

## Updates

| Date | Change | Reason |
|------|-------|--------|
| [Date] | Initial inventory | First documentation |

---

**Note**: Update this document whenever data collection changes.

