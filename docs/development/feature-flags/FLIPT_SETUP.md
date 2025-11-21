# Flipt Setup Guide

This guide walks through setting up Flipt for remote feature flags using Railway (recommended for quick, cheap deployment).

## Why Flipt on Railway?

- ✅ **One-click deployment** - No IaC needed
- ✅ **30-day free trial** - $5 credit to test (no card required)
- ✅ **Hobby plan** - $5/month with $5 credit (recommended for production)
- ✅ **Auto-scaling** - Handles traffic automatically
- ✅ **HTTPS by default** - Security built-in
- ✅ **Built-in database** - PostgreSQL with one click
- ✅ **Spending limits** - Prevent unexpected bills

**Pricing**:
- **Free tier**: $1/month credit (after trial) - ⚠️ Tight for production
- **Hobby plan**: $5/month with $5 credit - ✅ Recommended for reliability

## Step 1: Configure Spending Limits (IMPORTANT!)

**Before deploying, set spending limits to prevent unexpected bills:**

1. **Go to Railway Dashboard**: https://railway.app
2. **Navigate to**: Workspace → Usage
3. **Click "Set Usage Limits"**
4. **Configure**:
   - **Soft Limit (Email Alert)**: Set to $5 (get notified early)
   - **Hard Limit**: Set to $10 (auto-shutdown to prevent overage)
5. **Save** - Railway will:
   - Email you at 75% of hard limit ($7.50)
   - Email you at 90% of hard limit ($9.00)
   - Auto-shutdown services at hard limit ($10.00)

**Note**: Minimum hard limit is $10. Services won't auto-restart - you'll need to manually increase/remove limit.

## Step 2: Deploy Flipt on Railway

1. **Go to Railway**: https://railway.com/deploy/flipt
2. **Click "Deploy"** - This will:
   - Create a new Railway project
   - Deploy Flipt container
   - Set up basic configuration

3. **Add PostgreSQL Database**:
   - In Railway dashboard, click "+ New"
   - Select "Database" → "PostgreSQL"
   - Railway will automatically provision it

4. **Configure Flipt**:
   - Go to Flipt service settings
   - Add environment variable:
     ```
     DATABASE_URL=postgres://user:password@host:port/dbname
     ```
   - Railway provides this automatically when you link the database

5. **Get Flipt URL**:
   - Railway provides a public URL (e.g., `https://flipt-xxxx.up.railway.app`)
   - Note this URL for the app configuration

## Step 3: Configure Flipt

1. **Access Flipt UI**:
   - Open the Railway-provided URL
   - You'll see the Flipt dashboard

2. **Create API Token**:
   - Go to Settings → API Tokens
   - Create a new token
   - **Save this token** - you'll need it for the app

3. **Create Your First Flag**:
   - Go to Flags → Create Flag
   - Example: `offline_only` (boolean)
   - Set default value: `false`

## Step 4: Integrate with Android App

### Add Flipt SDK Dependency

Add to `app/build.gradle.kts`:
```kotlin
dependencies {
    // Flipt Kotlin SDK
    implementation("io.flipt:flipt-kotlin-sdk:1.0.0") // Check latest version
}
```

### Create FliptFeatureFlagProvider

```kotlin
class FliptFeatureFlagProvider(
    private val fliptClient: FliptClient,
    private val namespace: String = "default"
) : RemoteFeatureFlagProvider() {
    
    override suspend fun fetchFlags(): Map<String, Any> {
        // Fetch all flags from Flipt
        val flags = fliptClient.listFlags(namespace)
        return flags.associate { flag ->
            flag.key to when {
                flag.type == "BOOLEAN" -> flag.value.toBoolean()
                flag.type == "STRING" -> flag.value
                flag.type == "INTEGER" -> flag.value.toIntOrNull() ?: 0
                else -> flag.value
            }
        }
    }
}
```

### Update DataModule

```kotlin
fun createFeatureFlagManager(
    useRemoteFlags: Boolean = true,
    fliptUrl: String? = null,
    fliptToken: String? = null
): FeatureFlagManager {
    val provider = if (useRemoteFlags && fliptUrl != null && fliptToken != null) {
        val fliptClient = FliptClient(fliptUrl, fliptToken)
        FliptFeatureFlagProvider(fliptClient)
    } else {
        ConfigBasedFeatureFlagProvider()
    }
    return FeatureFlagManager(provider)
}
```

### Initialize in Application

```kotlin
class ElectricSheepApplication : Application() {
    private lateinit var featureFlagManager: FeatureFlagManager
    
    override fun onCreate() {
        super.onCreate()
        
        val fliptUrl = BuildConfig.FLIPT_URL // Add to BuildConfig
        val fliptToken = BuildConfig.FLIPT_TOKEN // Add to BuildConfig (or use secrets)
        
        featureFlagManager = DataModule.createFeatureFlagManager(
            useRemoteFlags = fliptUrl != null,
            fliptUrl = fliptUrl,
            fliptToken = fliptToken
        )
        
        // Initialize remote flags
        if (featureFlagManager.provider is RemoteFeatureFlagProvider) {
            lifecycleScope.launch {
                (featureFlagManager.provider as RemoteFeatureFlagProvider).initialise()
            }
        }
    }
}
```

## Step 5: Add Configuration

### BuildConfig Fields

Add to `app/build.gradle.kts`:
```kotlin
buildTypes {
    debug {
        buildConfigField("String", "FLIPT_URL", "\"https://your-flipt.up.railway.app\"")
        buildConfigField("String", "FLIPT_TOKEN", "\"your-api-token\"")
    }
    release {
        // Use secrets or environment variables for production
        buildConfigField("String", "FLIPT_URL", "\"${System.getenv("FLIPT_URL") ?: ""}\"")
        buildConfigField("String", "FLIPT_TOKEN", "\"${System.getenv("FLIPT_TOKEN") ?: ""}\"")
    }
}
```

### Local Properties (for development)

Add to `local.properties`:
```properties
flipt.url=https://your-flipt.up.railway.app
flipt.token=your-api-token
```

## Step 6: Test Integration

1. **Create a test flag in Flipt**:
   - Name: `test_feature`
   - Type: Boolean
   - Default: `false`

2. **Use in app**:
   ```kotlin
   if (featureFlagManager.isEnabled("test_feature")) {
       // Feature enabled
   }
   ```

3. **Toggle in Flipt UI**:
   - Change flag to `true`
   - App should pick up change (may need refresh/restart)

## Cost Estimation & Spending Protection

### Railway Free Tier (After 30-Day Trial)

**Free Plan Features**:
- **$1 credit/month** (does not roll over)
- **Resources per service**:
  - 0.5 GB RAM
  - 1 vCPU
  - 0.5 GB volume storage
- **Limits**:
  - 1 project
  - 3 services per project
  - 1 custom domain
  - 2 service domains
- **Support**: Community support (Discord/forums)

**Is Free Tier Sufficient for Flipt?**
- ⚠️ **Tight but possible** - Flipt is lightweight (~100-200MB RAM)
- ⚠️ **$1/month may not cover** - Flipt + PostgreSQL could exceed $1
- ✅ **30-day trial helps** - $5 credit for first month to test

### Railway Hobby Plan (Recommended)

**Hobby Plan Features** ($5/month):
- **$5 credit/month** (included in $5 fee)
- **Resources per service**:
  - 8 GB RAM
  - 8 vCPUs
  - 5 GB volume storage
- **Limits**:
  - 50 projects
  - 50 services per project
  - Custom domains
  - Private networking
- **Support**: Priority support

**Is Hobby Plan Sufficient?**
- ✅ **Yes, very reliable** - More than enough resources
- ✅ **$5/month covers** - Flipt (~$2-3) + PostgreSQL (~$2-3) = ~$5
- ✅ **Always-on** - No sleep/wake issues
- ✅ **Better support** - Priority support if issues arise

### Cost Breakdown

**Free Tier** (After Trial):
- Flipt: ~$1-2/month
- PostgreSQL: ~$1-2/month
- **Total: ~$2-4/month** (may exceed $1 credit)
- **Reliability**: ⚠️ May be tight, services might sleep

**Hobby Plan** ($5/month):
- Flipt: ~$2-3/month
- PostgreSQL: ~$2-3/month
- **Total: ~$5/month** (within $5 credit)
- **Reliability**: ✅ Excellent, always-on

**Spending Limits (Recommended Setup)**:
- **Soft Limit**: $5 (email alert)
- **Hard Limit**: $10 (auto-shutdown to prevent overage)
- **Alerts**: You'll get emails at $7.50 (75%) and $9.00 (90%)

**How to Set Limits**:
1. Go to Railway Dashboard → Workspace → Usage
2. Click "Set Usage Limits"
3. Set Soft Limit: $5 (email alert)
4. Set Hard Limit: $10 (auto-shutdown)
5. Save

**What Happens at Hard Limit**:
- All services automatically shut down
- No further charges
- You'll receive email notification
- Services won't auto-restart (manual intervention required)

## Security Considerations

1. **API Token**: Store securely (BuildConfig for dev, secrets for prod)
2. **HTTPS**: Railway provides HTTPS by default
3. **Rate Limiting**: Flipt has built-in rate limiting
4. **Authentication**: Flipt supports authentication (optional)

## Troubleshooting

### Flipt not responding
- Check Railway logs
- Verify DATABASE_URL is set correctly
- Check Flipt service is running

### Flags not updating
- Verify API token is correct
- Check network connectivity
- Ensure `initialise()` is called

### High costs
- Check Railway usage dashboard
- Consider switching to Fly.io free tier
- Or migrate to Supabase custom implementation

## Alternative: Fly.io Setup

If Railway doesn't work for you:

1. **Install Fly CLI**: `brew install flyctl`
2. **Login**: `fly auth login`
3. **Launch**: `fly launch --image flipt/flipt:latest`
4. **Add database**: `fly postgres create`
5. **Set secrets**: `fly secrets set DATABASE_URL=...`

**Cost**: Free tier (3 VMs) or ~$5/month

## Next Steps

1. ✅ Deploy Flipt on Railway
2. ✅ Get API token
3. ✅ Add Flipt SDK to app
4. ✅ Implement FliptFeatureFlagProvider
5. ✅ Update DataModule
6. ✅ Test integration
7. ✅ Add tests

