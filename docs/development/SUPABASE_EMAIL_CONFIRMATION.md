# Configuring Email Confirmation Redirect URLs in Supabase

## Problem

When users sign up, they receive an email confirmation link, but it points to `localhost` instead of your app's deep link. This prevents the app from handling the confirmation callback.

## Solution

Configure the redirect URL in Supabase to use your app's deep link scheme.

## Steps

### 1. Go to Supabase Auth Settings

1. Navigate to: https://supabase.com/dashboard/project/mvuzvoyvijsdqsfqjgpd/auth/url-configuration
2. Or: Dashboard → Authentication → URL Configuration

### 2. Configure Site URL

**Site URL**: This is your app's deep link scheme
- Set to: `com.electricsheep.app://auth-callback`
- This is the base URL for redirects

### 3. Configure Redirect URLs

**Redirect URLs**: Add your app's deep link to the allowed redirect URLs list
- Add: `com.electricsheep.app://auth-callback`
- This allows Supabase to redirect to your app after email confirmation

### 4. Email Templates (Optional)

If you want to customize the confirmation email:
1. Go to: Authentication → Email Templates
2. Select "Confirm signup" template
3. The confirmation link will use the redirect URL you configured

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

## Testing

After configuring:

1. **Sign up** with a new email
2. **Check your email** for the confirmation link
3. **Click the link** - it should open your app (not localhost)
4. **The app will handle the callback** via `MainActivity.handleOAuthCallback()`

## Alternative: Disable Email Confirmation (Development Only)

For development/testing, you can disable email confirmation:

1. Go to: https://supabase.com/dashboard/project/mvuzvoyvijsdqsfqjgpd/auth/providers
2. Click on "Email" provider
3. Toggle off "Confirm email" (or "Enable email confirmations")
4. **Warning**: Only do this for development! Production should require email confirmation.

## Troubleshooting

**Link still goes to localhost**:
- Clear browser cache
- Check that the redirect URL is exactly: `com.electricsheep.app://auth-callback`
- Ensure no trailing slashes or extra characters

**App doesn't open when clicking link**:
- Verify the deep link is configured in AndroidManifest.xml
- Test the deep link: `adb shell am start -a android.intent.action.VIEW -d "com.electricsheep.app://auth-callback?code=test"`
- Check that the app is installed on the device

**Email not received**:
- Check spam folder
- Verify email address is correct
- Check Supabase logs for email sending errors

