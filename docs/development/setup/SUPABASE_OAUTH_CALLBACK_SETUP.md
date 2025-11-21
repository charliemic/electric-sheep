# Configuring OAuth Callback URLs in Supabase

## Problem

After completing Google OAuth sign-in, the callback fails because Supabase is trying to redirect to `localhost` instead of your app's deep link.

## Solution

Configure the redirect URLs in Supabase's URL Configuration to allow your app's deep link scheme.

## Steps

### 1. Navigate to URL Configuration

1. Go to: https://supabase.com/dashboard/project/mvuzvoyvijsdqsfqjgpd/auth/url-configuration
2. Or: Dashboard → Authentication → URL Configuration

### 2. Configure Site URL

**Site URL**: This is the base URL for redirects
- **Set to**: `com.electricsheep.app://auth-callback`
- This tells Supabase to use your app's deep link scheme as the default redirect

### 3. Configure Redirect URLs

**Redirect URLs**: Add your app's deep link to the allowed redirect URLs list
- **Click "Add URL"** or the **"+"** button
- **Add**: `com.electricsheep.app://auth-callback`
- This explicitly allows Supabase to redirect to your app after OAuth

**Important**: You should have BOTH:
1. The Supabase callback URL (for the OAuth flow):
   ```
   https://mvuzvoyvijsdqsfqjgpd.supabase.co/auth/v1/callback
   ```
   (This is already configured for the Google OAuth provider)

2. Your app's deep link (for the final redirect to your app):
   ```
   com.electricsheep.app://auth-callback
   ```
   (This is what you need to add)

### 4. Save Configuration

- Click **"Save"** at the bottom
- You should see a success message

## How It Works

The OAuth flow works in two steps:

1. **Google OAuth Redirect** (handled by Supabase):
   - User completes Google sign-in
   - Google redirects to: `https://mvuzvoyvijsdqsfqjgpd.supabase.co/auth/v1/callback`
   - This URL is configured in Google Cloud Console and Supabase Google provider settings

2. **App Deep Link Redirect** (handled by your app):
   - Supabase processes the OAuth callback
   - Supabase redirects to: `com.electricsheep.app://auth-callback?code=...`
   - Your app's deep link handler receives this and exchanges the code for a session

## Current App Configuration

Your app is already configured to handle the deep link:

**AndroidManifest.xml**:
```xml
<intent-filter>
    <action android:name="android.intent.action.VIEW" />
    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />
    <data
        android:scheme="com.electricsheep.app"
        android:host="auth-callback" />
</intent-filter>
```

**Deep Link URL**: `com.electricsheep.app://auth-callback`

**OAuth URL Generation** (in `SupabaseAuthProvider.kt`):
```kotlin
val redirectUrl = "com.electricsheep.app://auth-callback"
val url = "$baseUrl/auth/v1/authorize?provider=google&redirect_to=$redirectUrl"
```

## Verification

After configuring:

1. **Test the OAuth flow**:
   - Click "Sign in with Google" in your app
   - Complete Google sign-in
   - Should redirect back to your app (not localhost)
   - App should authenticate successfully

2. **Check logs**:
   ```bash
   adb logcat | grep -i "oauth\|callback"
   ```
   - Should see: `OAuth callback received: com.electricsheep.app://auth-callback?code=...`
   - Should NOT see: `localhost` or connection errors

## Troubleshooting

### Callback Still Goes to Localhost

**Check**:
1. Site URL is set to: `com.electricsheep.app://auth-callback`
2. Redirect URLs list includes: `com.electricsheep.app://auth-callback`
3. No trailing slashes or extra characters
4. Saved the configuration (click "Save")

**Solution**: Clear browser cache and try again, or wait a few minutes for Supabase to propagate the changes.

### App Doesn't Receive Callback

**Check**:
1. Deep link is configured in `AndroidManifest.xml` (see above)
2. App is installed on the device
3. Test deep link manually:
   ```bash
   adb shell am start -a android.intent.action.VIEW -d "com.electricsheep.app://auth-callback?code=test"
   ```
   - Should open your app

### "Invalid redirect URL" Error

**Check**:
1. The redirect URL in the OAuth URL matches exactly: `com.electricsheep.app://auth-callback`
2. It's added to Supabase's Redirect URLs list
3. No typos or extra characters

## Complete OAuth Flow

```
1. User clicks "Sign in with Google"
   ↓
2. App opens: https://mvuzvoyvijsdqsfqjgpd.supabase.co/auth/v1/authorize?provider=google&redirect_to=com.electricsheep.app://auth-callback
   ↓
3. Google OAuth page opens
   ↓
4. User signs in with Google
   ↓
5. Google redirects to: https://mvuzvoyvijsdqsfqjgpd.supabase.co/auth/v1/callback?code=...
   ↓
6. Supabase processes callback and redirects to: com.electricsheep.app://auth-callback?code=...
   ↓
7. Android deep link handler receives: com.electricsheep.app://auth-callback?code=...
   ↓
8. MainActivity.handleOAuthCallback() processes the callback
   ↓
9. App exchanges code for session via SupabaseAuthProvider.handleOAuthCallback()
   ↓
10. User is authenticated ✅
```

## References

- [Supabase Auth URL Configuration](https://supabase.com/docs/guides/auth/url-configuration)
- [Deep Linking in Android](https://developer.android.com/training/app-links/deep-linking)

