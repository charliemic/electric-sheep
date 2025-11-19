# Supabase Secret Key Setup

## What is a Secret Key?

**Secret Keys** (`sb_secret_...`) provide full access to your Supabase project, **bypassing Row-Level Security (RLS)**. They're used for:

- Server-side operations that need elevated privileges
- Admin operations and maintenance
- Backend services that need to access all data
- Operations that must bypass RLS policies (e.g., CI/CD feature flag deployments)

⚠️ **IMPORTANT**: Never commit secret keys to version control. Always use environment variables or secrets.

**Note**: Secret keys are preferred over JWT-based `service_role` keys per [Supabase documentation](https://supabase.com/docs/guides/api/api-keys).

## When Do You Need It?

### ✅ You NEED Service Role For:
- **Backend server operations** that need to bypass RLS
- **Admin operations** (bulk data operations, maintenance)
- **Server-side functions** that need full database access
- **Background jobs** that process data across users

### ❌ You DON'T Need Service Role For:
- **CLI migrations** (use Personal Access Token - PAT)
- **Client-side app operations** (use anon key)
- **User-facing features** (use anon key with RLS)

## Getting Your Service Role Key

1. **Go to Supabase Dashboard**:
   - Visit: https://supabase.com/dashboard/project/mvuzvoyvijsdqsfqjgpd
   - Or: https://supabase.com/dashboard/project/YOUR_PROJECT_REF

2. **Navigate to Settings**:
   - Click on "Settings" (gear icon) in the left sidebar
   - Click on "API" in the settings menu

3. **Find Secret Key**:
   - Scroll down to "Project API keys"
   - Find the **"secret"** key (starts with `sb_secret_...`)
   - Click the "eye" icon to reveal it
   - Click "Copy" to copy the key

4. **Key Format**:
   - Secret keys start with: `sb_secret_...`
   - They're not JWT tokens (unlike old `service_role` keys)
   - Different from Personal Access Tokens (which start with `sbp_`)

## Using Service Role Key

### Local Development

Create `.env.local` (not tracked in git):
```bash
SUPABASE_URL=https://mvuzvoyvijsdqsfqjgpd.supabase.co
SUPABASE_ANON_KEY=your-anon-key-here
SUPABASE_SERVICE_ROLE_KEY=your-service-role-key-here
```

### CI/CD (GitHub Actions)

Add to GitHub Secrets:
1. Go to: https://github.com/charliemic/electric-sheep/settings/secrets/actions
2. Click "New repository secret"
3. For staging: Name `SUPABASE_SECRET_KEY_STAGING`, Value: Your staging secret key
4. For production: Name `SUPABASE_SECRET_KEY`, Value: Your production secret key
5. Click "Add secret"

### In Your App Code

**Android App** (Kotlin):
```kotlin
// For client-side operations, use anon key (already configured)
val supabaseClient = createSupabaseClient(
    supabaseUrl = BuildConfig.SUPABASE_URL,
    supabaseKey = BuildConfig.SUPABASE_ANON_KEY  // Use anon key, not service role
)

// Service role should ONLY be used in backend services
// NEVER in client-side code (Android app)
```

**Backend Service** (if you add one later):
```kotlin
// Only use service role in secure backend services
val supabaseClient = createSupabaseClient(
    supabaseUrl = System.getenv("SUPABASE_URL"),
    supabaseKey = System.getenv("SUPABASE_SERVICE_ROLE_KEY")  // Service role for backend
)
```

## Security Best Practices

1. ✅ **Never commit service role keys** to git
2. ✅ **Use environment variables** or secrets management
3. ✅ **Rotate keys regularly** if compromised
4. ✅ **Use anon key** in client-side apps (with RLS)
5. ✅ **Use service role** only in secure backend services
6. ❌ **Never expose service role** in client-side code
7. ❌ **Never log service role keys** in application logs

## Current Project Status

For this project:
- ✅ **CLI Migrations**: Using Personal Access Token (PAT) - `SUPABASE_ACCESS_TOKEN`
- ✅ **Android App**: Using anon key - `SUPABASE_ANON_KEY`
- ✅ **Feature Flag Deployment**: Using secret keys - `SUPABASE_SECRET_KEY_STAGING` / `SUPABASE_SECRET_KEY`

## Troubleshooting

**"Permission denied" errors**:
- Check if you're using the correct key (anon vs service role)
- Verify RLS policies allow the operation
- Service role bypasses RLS, anon key respects it

**"Invalid API key" errors**:
- Verify the key is copied correctly (no extra spaces)
- Check if the key has expired (service role keys don't expire)
- Ensure you're using the key for the correct project

## Related Documentation

- [Supabase API Keys Documentation](https://supabase.com/docs/guides/api/api-keys)
- [Row-Level Security](https://supabase.com/docs/guides/auth/row-level-security)
- [Service Role vs Anon Key](https://supabase.com/docs/guides/api/api-keys#service-role-key)

