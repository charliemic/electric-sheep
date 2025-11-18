# Feature Flag Tools Evaluation

**Date**: 2024-11-18  
**Status**: Research Complete

## Executive Summary

Supabase does **not** have native feature flag functionality. However, we can implement feature flags using:
1. **Custom Supabase table + PostgREST** (simplest, already using Supabase) ⭐ **RECOMMENDED**
2. **Custom Supabase Edge Function** (for advanced features like A/B testing)
3. **Open-source tools** (Flipt, Flagsmith, FeatureHub, etc.) - requires separate hosting

**See also**: [`SUPABASE_EDGE_FUNCTIONS_FEATURE_FLAGS.md`](./SUPABASE_EDGE_FUNCTIONS_FEATURE_FLAGS.md) for detailed Edge Functions analysis.

## Supabase Native Feature Flags

**Answer**: ❌ **No native feature flag service**

Supabase provides:
- ✅ Database (PostgreSQL)
- ✅ Real-time subscriptions
- ✅ Row-Level Security (RLS)
- ✅ Edge Functions
- ❌ No built-in feature flag service

**However**, we can easily build feature flags using Supabase:
- Create a `feature_flags` table
- Use PostgREST API to fetch flags
- Use Realtime subscriptions for live updates
- Use RLS for user-specific flags

## Open-Source Feature Flag Tools

### 1. Flipt ⭐ **RECOMMENDED**

**Pros**:
- ✅ Git-native (changes via PRs)
- ✅ Self-hosted (Docker)
- ✅ Simple REST API
- ✅ Kotlin SDK available
- ✅ Free and open-source
- ✅ Good documentation

**Cons**:
- ⚠️ Requires separate service (Docker)
- ⚠️ Additional infrastructure to manage

**Best For**: Teams wanting Git-based workflow and self-hosting

### 2. Flagsmith

**Pros**:
- ✅ Open-source (self-hosted or SaaS)
- ✅ Kotlin SDK
- ✅ A/B testing support
- ✅ User targeting
- ✅ Good documentation

**Cons**:
- ⚠️ More complex than needed for simple flags
- ⚠️ Requires separate service

**Best For**: Teams needing advanced features (A/B testing, user targeting)

### 3. FeatureHub

**Pros**:
- ✅ Open-source
- ✅ Real-time updates
- ✅ Percentage rollouts
- ✅ Kotlin SDK

**Cons**:
- ⚠️ More complex setup
- ⚠️ Requires separate service

**Best For**: Teams needing percentage rollouts and advanced targeting

### 4. Bucketeer

**Pros**:
- ✅ Open-source
- ✅ A/B testing
- ✅ Dark launches

**Cons**:
- ⚠️ More complex than needed
- ⚠️ Requires separate service

**Best For**: Teams needing A/B testing capabilities

### 5. OpenFeature

**Pros**:
- ✅ Vendor-agnostic specification
- ✅ Can integrate with multiple providers
- ✅ Standardized API

**Cons**:
- ⚠️ Still need a provider (Flipt, Flagsmith, etc.)
- ⚠️ Additional abstraction layer

**Best For**: Teams wanting to switch providers easily

## Recommendation: Custom Supabase Implementation

**Why Supabase?**
1. ✅ **Already using Supabase** - No additional infrastructure
2. ✅ **Simple implementation** - Just a table + API calls
3. ✅ **Real-time support** - Can use Supabase Realtime for live updates
4. ✅ **RLS support** - Can have user-specific flags
5. ✅ **No extra dependencies** - Uses existing Supabase client
6. ✅ **Free tier** - No additional costs

**Implementation Plan**:
1. Create `feature_flags` table in Supabase
2. Implement `SupabaseFeatureFlagProvider` extending `RemoteFeatureFlagProvider`
3. Use PostgREST to fetch flags
4. Optionally use Realtime for live updates
5. Cache flags locally for offline access

**Schema**:
```sql
CREATE TABLE feature_flags (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  key TEXT UNIQUE NOT NULL,
  value_type TEXT NOT NULL, -- 'boolean', 'string', 'int'
  boolean_value BOOLEAN,
  string_value TEXT,
  int_value INTEGER,
  enabled BOOLEAN DEFAULT true,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- RLS for user-specific flags (if needed)
ALTER TABLE feature_flags ENABLE ROW LEVEL SECURITY;
```

## Comparison Matrix

| Solution | Setup Complexity | Infrastructure | Real-time | User Targeting | Cost |
|----------|-----------------|---------------|-----------|----------------|------|
| **Supabase Custom** | ⭐⭐⭐⭐⭐ | None (existing) | ✅ | ✅ (RLS) | Free |
| **Flipt** | ⭐⭐⭐⭐ | Docker | ✅ | ✅ | Free |
| **Flagsmith** | ⭐⭐⭐ | Docker/SaaS | ✅ | ✅ | Free/Paid |
| **FeatureHub** | ⭐⭐⭐ | Docker/SaaS | ✅ | ✅ | Free/Paid |
| **Bucketeer** | ⭐⭐⭐ | Docker/SaaS | ✅ | ✅ | Free/Paid |

## Flipt Hosting Options (Quick & Cheap)

### 1. Railway ⭐ **BEST FOR QUICK SETUP**

**Why Railway?**
- ✅ **One-click deployment** - Literally click a button, done
- ✅ **30-day free trial** - $5 credit to test (no card required)
- ✅ **Hobby plan** - $5/month with $5 credit (recommended for reliability)
- ✅ **Spending limits** - Set hard limits to prevent unexpected bills (minimum $10)
- ✅ **Billing alerts** - Email notifications at 75% and 90% of limit
- ✅ **No IaC needed** - Just connect GitHub repo or use their template
- ✅ **Auto-scaling** - Handles traffic spikes automatically
- ✅ **Built-in database** - Can provision PostgreSQL with one click
- ✅ **HTTPS by default** - Automatic SSL certificates
- ✅ **Simple pricing** - Pay for what you use

**Setup Time**: ~5 minutes

**Pricing Tiers**:
- **Free Tier** (After 30-day trial):
  - $1 credit/month
  - 0.5 GB RAM, 1 vCPU per service
  - 1 project, 3 services
  - ⚠️ **May be tight for Flipt + PostgreSQL**
  
- **Hobby Plan** ($5/month) ⭐ **RECOMMENDED**:
  - $5 credit/month (included)
  - 8 GB RAM, 8 vCPUs per service
  - 50 projects, 50 services
  - ✅ **Reliable for production use**

**Spending Protection**:
- **Soft Limit (Email Alert)**: Get notified at custom threshold (e.g., $5)
- **Hard Limit**: Auto-shutdown at maximum spend (minimum $10)
- **Alerts**: Email at 75%, 90%, and when limit hit
- **Configure**: Workspace → Usage → Set Usage Limits

**Deployment**:
1. Go to https://railway.com/deploy/flipt
2. Click "Deploy"
3. Add PostgreSQL database (one click)
4. Set environment variables
5. Done!

### 2. Fly.io ⭐ **GOOD ALTERNATIVE**

**Why Fly.io?**
- ✅ **Free tier** - 3 shared VMs, 3GB storage
- ✅ **Global edge network** - Fast worldwide
- ✅ **Simple CLI** - `fly launch` and you're done
- ✅ **Auto-scaling** - Scales to zero when not in use
- ✅ **HTTPS included** - Automatic SSL

**Setup Time**: ~10 minutes (CLI setup)
**Monthly Cost**: $0 (free tier) or ~$5-10 for small dedicated instance

**Deployment**:
```bash
fly launch --image flipt/flipt:latest
fly postgres create
fly secrets set DATABASE_URL=...
```

### 3. Render ⭐ **EASIEST UI**

**Why Render?**
- ✅ **Free tier** - 750 hours/month (enough for always-on)
- ✅ **One-click deploy** - Similar to Railway
- ✅ **Auto-deploy from Git** - Push to deploy
- ✅ **HTTPS included** - Automatic SSL
- ✅ **Simple dashboard** - Very user-friendly

**Setup Time**: ~5 minutes
**Monthly Cost**: $0 (free tier) or ~$7/month for always-on

**Deployment**:
1. Connect GitHub repo
2. Select Flipt Docker image
3. Add PostgreSQL database
4. Deploy

### 4. DigitalOcean App Platform

**Why DigitalOcean?**
- ✅ **Simple pricing** - $5/month for basic app
- ✅ **One-click apps** - Flipt available in marketplace
- ✅ **Managed database** - PostgreSQL included
- ✅ **Auto-scaling** - Built-in

**Setup Time**: ~10 minutes
**Monthly Cost**: ~$12/month (app + database)

### 5. Docker on Existing VPS

**If you already have a VPS:**
```bash
docker run -d \
  -p 8080:8080 \
  -e DATABASE_URL=postgres://... \
  flipt/flipt:latest
```

**Cost**: $0 (if you have VPS) or $5/month (new VPS)

## Recommendation: Railway for Flipt

**For a small use case, Railway is the winner:**

1. ✅ **Fastest setup** - One-click deployment
2. ✅ **Free tier** - Usually enough for small apps
3. ✅ **No IaC needed** - Just click and configure
4. ✅ **Auto-scaling** - Handles traffic automatically
5. ✅ **Built-in database** - No separate setup needed
6. ✅ **HTTPS by default** - Security out of the box
7. ✅ **Simple pricing** - Pay for what you use

**Cost Breakdown**:

**Free Tier** (After 30-day trial):
- Flipt: ~$1-2/month
- PostgreSQL: ~$1-2/month
- **Total: ~$2-4/month** (may exceed $1 credit)
- **Reliability**: ⚠️ Tight, may have issues

**Hobby Plan** ($5/month) ⭐ **RECOMMENDED**:
- Flipt: ~$2-3/month
- PostgreSQL: ~$2-3/month
- **Total: ~$5/month** (within $5 credit)
- **Reliability**: ✅ Excellent, always-on

**Setup Time**: 5 minutes

## Decision

### Updated Recommendation: Supabase PostgREST (Simplest) ⭐

**Use Supabase PostgREST directly** for the following reasons:

1. ✅ **Simplest** - No additional infrastructure, uses existing Supabase
2. ✅ **Free** - Included in Supabase free tier (500k Edge Function invocations, 2M DB requests)
3. ✅ **Real-time** - Can use Supabase Realtime subscriptions for live updates
4. ✅ **User-specific** - RLS handles user targeting automatically
5. ✅ **Type-safe** - Kotlin SDK with code generation
6. ✅ **Familiar** - Already using PostgREST for moods

**When to use Edge Functions**:
- Need A/B testing
- Need percentage rollouts
- Need complex targeting rules
- High traffic (caching needed)

**When to use Railway/Flipt**:
- Need Git-based workflow (flags as code)
- Need admin UI
- Need advanced analytics
- Want full-featured platform

**See**: [`SUPABASE_EDGE_FUNCTIONS_FEATURE_FLAGS.md`](./SUPABASE_EDGE_FUNCTIONS_FEATURE_FLAGS.md) for detailed comparison.

**Alternative**: If we need Git-based workflow or admin UI, Flipt on Railway is still a good option ($5/month).

## Implementation Steps

1. Create `feature_flags` table in Supabase
2. Add migration to `supabase/migrations/`
3. Implement `SupabaseFeatureFlagProvider`
4. Update `DataModule` to use Supabase provider
5. Add initialization in `ElectricSheepApplication`
6. Optionally add Realtime subscription for live updates
7. Add tests

