# Supabase Edge Functions for Feature Flags

**Date**: 2024-11-18  
**Question**: Can we use Supabase Edge Functions to host a feature flag service instead of Railway/Flipt?

## Executive Summary

✅ **Yes, but with a twist**: We don't need to deploy a full feature flag tool. Instead, we can:

1. **Store flags in Supabase database** (PostgreSQL - already have it)
2. **Option A**: Use PostgREST directly (simplest, no Edge Function needed)
3. **Option B**: Create custom Edge Function for advanced features (user targeting, A/B testing)
4. **Option C**: Use Edge Function as a cache/proxy layer

**Full feature flag tools** (Unleash, Flagsmith, Flipt) are complete applications that need:
- Persistent services (can't run as simple Edge Functions)
- Background workers
- Admin UIs
- Complex state management

**However**, we can build a lightweight feature flag service using Edge Functions + Supabase database.

## Supabase Edge Functions Overview

### What Are Edge Functions?

- **Runtime**: Deno (TypeScript/JavaScript)
- **Deployment**: Serverless functions on Supabase's edge network
- **Language**: TypeScript (native), JavaScript
- **Access**: Full access to Supabase database, auth, storage
- **Invocation**: HTTP requests (REST API)

### Free Tier Limits

**Supabase Free Tier**:
- **500,000 invocations/month** (Edge Functions)
- **2 million database requests/month** (PostgREST)
- **500 MB database storage**
- **50,000 monthly active users**

**For feature flags**:
- ✅ **More than enough** - Feature flags are read-heavy, low traffic
- ✅ **No additional cost** - Included in free tier
- ✅ **Global edge network** - Fast response times worldwide

## Approach Comparison

### Option 1: Direct PostgREST (Simplest) ⭐ **RECOMMENDED**

**How it works**:
- Store flags in `feature_flags` table
- Android app queries via PostgREST API (already using this)
- No Edge Function needed

**Pros**:
- ✅ **Simplest** - No Edge Function code needed
- ✅ **Already working** - Using PostgREST for moods
- ✅ **Free** - No additional costs
- ✅ **Real-time** - Can use Supabase Realtime subscriptions
- ✅ **RLS support** - User-specific flags via Row-Level Security
- ✅ **Type-safe** - Kotlin SDK with code generation

**Cons**:
- ⚠️ **No complex logic** - Can't do A/B testing, percentage rollouts in database
- ⚠️ **Direct database access** - Less abstraction

**Implementation**:
```kotlin
// Already have Supabase client
val flags = supabase.from("feature_flags")
    .select()
    .decodeList<FeatureFlag>()
```

**Best for**: Simple boolean/string/int flags, user-specific flags via RLS

---

### Option 2: Custom Edge Function (Advanced Features)

**How it works**:
- Store flags in `feature_flags` table
- Edge Function provides REST API with advanced logic
- Android app calls Edge Function endpoint

**Pros**:
- ✅ **Advanced features** - A/B testing, percentage rollouts, user targeting
- ✅ **Business logic** - Complex flag evaluation rules
- ✅ **Caching** - Can cache flags in Edge Function
- ✅ **Rate limiting** - Built-in protection
- ✅ **TypeScript** - Familiar language

**Cons**:
- ⚠️ **More code** - Need to write and maintain Edge Function
- ⚠️ **Additional complexity** - Another layer to manage
- ⚠️ **Cold starts** - Edge Functions may have cold start latency

**Implementation**:
```typescript
// supabase/functions/feature-flags/index.ts
import { serve } from "https://deno.land/std@0.168.0/http/server.ts"
import { createClient } from 'https://esm.sh/@supabase/supabase-js@2'

serve(async (req) => {
  const supabase = createClient(
    Deno.env.get('SUPABASE_URL') ?? '',
    Deno.env.get('SUPABASE_SERVICE_ROLE_KEY') ?? ''
  )
  
  const { userId, flagKey } = await req.json()
  
  // Fetch flag from database
  const { data: flag } = await supabase
    .from('feature_flags')
    .select('*')
    .eq('key', flagKey)
    .single()
  
  // Advanced logic: A/B testing, percentage rollouts, etc.
  const enabled = evaluateFlag(flag, userId)
  
  return new Response(JSON.stringify({ enabled }), {
    headers: { 'Content-Type': 'application/json' }
  })
})
```

**Best for**: Complex flag evaluation, A/B testing, percentage rollouts

---

### Option 3: Edge Function as Cache/Proxy

**How it works**:
- Edge Function caches flags in memory
- Reduces database queries
- Provides unified API

**Pros**:
- ✅ **Performance** - Faster response times
- ✅ **Reduced DB load** - Fewer queries
- ✅ **Unified API** - Single endpoint for all flags

**Cons**:
- ⚠️ **Cache invalidation** - Need to handle flag updates
- ⚠️ **Memory limits** - Edge Functions have memory constraints
- ⚠️ **Complexity** - More moving parts

**Best for**: High-traffic scenarios, reducing database load

---

## Can We Deploy Full Tools (Unleash, Flagsmith) on Edge Functions?

**Short answer**: ❌ **No**

**Why not**:
1. **Full applications** - These are complete services with:
   - Admin UIs
   - Background workers
   - WebSocket servers
   - Complex state management
   - Multiple endpoints

2. **Edge Functions are stateless** - Can't run persistent services
3. **No background workers** - Edge Functions are request-response only
4. **Resource limits** - Edge Functions have memory/time limits

**What we CAN do**:
- Use their **SDKs** in Edge Functions (e.g., Unleash SDK for flag evaluation)
- But we'd still need to host the main service elsewhere

---

## Recommendation: Hybrid Approach

### Phase 1: Start Simple (PostgREST) ⭐

**For now**:
1. Create `feature_flags` table in Supabase
2. Use PostgREST directly from Android app
3. Implement `SupabaseFeatureFlagProvider` using existing PostgREST client

**Why**:
- ✅ Simplest implementation
- ✅ No Edge Function code needed
- ✅ Already familiar with PostgREST
- ✅ Real-time updates via Realtime subscriptions
- ✅ User-specific flags via RLS

**Schema**:
```sql
CREATE TABLE feature_flags (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  key TEXT UNIQUE NOT NULL,
  value_type TEXT NOT NULL CHECK (value_type IN ('boolean', 'string', 'int')),
  boolean_value BOOLEAN,
  string_value TEXT,
  int_value INTEGER,
  enabled BOOLEAN DEFAULT true,
  user_id UUID REFERENCES auth.users(id), -- NULL = global flag
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Index for fast lookups
CREATE INDEX idx_feature_flags_key ON feature_flags(key);
CREATE INDEX idx_feature_flags_user_id ON feature_flags(user_id);

-- RLS for user-specific flags
ALTER TABLE feature_flags ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can read their own flags"
  ON feature_flags FOR SELECT
  USING (user_id = auth.uid() OR user_id IS NULL);
```

### Phase 2: Add Edge Function (If Needed)

**When to add**:
- Need A/B testing
- Need percentage rollouts
- Need complex targeting rules
- High traffic (caching needed)

**Edge Function example**:
```typescript
// supabase/functions/feature-flags/index.ts
import { serve } from "https://deno.land/std@0.168.0/http/server.ts"
import { createClient } from 'https://esm.sh/@supabase/supabase-js@2'

serve(async (req) => {
  const supabase = createClient(
    Deno.env.get('SUPABASE_URL') ?? '',
    Deno.env.get('SUPABASE_SERVICE_ROLE_KEY') ?? ''
  )
  
  const { userId, flagKey } = await req.json()
  
  // Fetch flag
  const { data: flag } = await supabase
    .from('feature_flags')
    .select('*')
    .eq('key', flagKey)
    .single()
  
  if (!flag) {
    return new Response(JSON.stringify({ enabled: false }), {
      headers: { 'Content-Type': 'application/json' }
    })
  }
  
  // Simple evaluation (can add A/B testing, percentage rollouts here)
  const enabled = flag.enabled && (flag.user_id === null || flag.user_id === userId)
  
  return new Response(JSON.stringify({ enabled }), {
    headers: { 'Content-Type': 'application/json' }
  })
})
```

---

## Comparison: Edge Functions vs Railway/Flipt

| Aspect | PostgREST (Direct) | Edge Function | Railway/Flipt |
|--------|-------------------|---------------|---------------|
| **Setup Complexity** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐ |
| **Infrastructure** | None (existing) | None (existing) | Separate service |
| **Cost** | Free (included) | Free (included) | $0-5/month |
| **Advanced Features** | ⚠️ Limited | ✅ Yes | ✅ Yes |
| **Real-time Updates** | ✅ Yes (Realtime) | ⚠️ Manual | ✅ Yes |
| **User Targeting** | ✅ Yes (RLS) | ✅ Yes | ✅ Yes |
| **A/B Testing** | ❌ No | ✅ Yes | ✅ Yes |
| **Git-based Workflow** | ❌ No | ❌ No | ✅ Yes (Flipt) |
| **Admin UI** | ❌ No | ❌ No | ✅ Yes (Flipt) |

---

## TypeScript Feature Flag Tools (Reference)

While we can't deploy these as Edge Functions, here are TypeScript-based tools for reference:

1. **Unleash** - Full application, TypeScript SDK available
2. **Flagsmith** - Full application, TypeScript SDK available
3. **GrowthBook** - Full application, TypeScript SDK available
4. **OpenFeature** - Specification, TypeScript SDK available

**Note**: These tools provide SDKs that could be used in Edge Functions, but the main service needs separate hosting.

---

## Decision

### Recommended: Start with PostgREST (Option 1)

**Why**:
1. ✅ **Simplest** - No new code, uses existing infrastructure
2. ✅ **Free** - Included in Supabase free tier
3. ✅ **Real-time** - Can use Realtime subscriptions
4. ✅ **User-specific** - RLS handles user targeting
5. ✅ **Familiar** - Already using PostgREST for moods

**When to upgrade to Edge Function**:
- Need A/B testing
- Need percentage rollouts
- Need complex targeting rules
- High traffic (caching)

**When to consider Railway/Flipt**:
- Need Git-based workflow (flags as code)
- Need admin UI
- Need advanced analytics
- Want full-featured platform

---

## Implementation Plan

### Step 1: Create Feature Flags Table

```sql
-- supabase/migrations/YYYYMMDDHHMMSS_create_feature_flags.sql
CREATE TABLE feature_flags (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  key TEXT UNIQUE NOT NULL,
  value_type TEXT NOT NULL CHECK (value_type IN ('boolean', 'string', 'int')),
  boolean_value BOOLEAN,
  string_value TEXT,
  int_value INTEGER,
  enabled BOOLEAN DEFAULT true,
  user_id UUID REFERENCES auth.users(id), -- NULL = global flag
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_feature_flags_key ON feature_flags(key);
CREATE INDEX idx_feature_flags_user_id ON feature_flags(user_id);

ALTER TABLE feature_flags ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can read their own flags and global flags"
  ON feature_flags FOR SELECT
  USING (user_id = auth.uid() OR user_id IS NULL);
```

### Step 2: Implement SupabaseFeatureFlagProvider

```kotlin
// app/src/main/java/com/electricsheep/app/config/SupabaseFeatureFlagProvider.kt
class SupabaseFeatureFlagProvider(
    private val supabaseClient: SupabaseClient
) : RemoteFeatureFlagProvider() {
    
    override suspend fun getBoolean(key: String, default: Boolean): Boolean {
        return supabaseClient.from("feature_flags")
            .select()
            .eq("key", key)
            .eq("value_type", "boolean")
            .decodeSingle<FeatureFlag>()
            .let { it.enabled && (it.booleanValue ?: default) }
    }
    
    // Similar for getString, getInt
}
```

### Step 3: (Optional) Add Edge Function for Advanced Features

```typescript
// supabase/functions/feature-flags/index.ts
// See example above
```

---

## Conclusion

**Edge Functions are a great option**, but we don't need them initially. **PostgREST is simpler** and sufficient for most use cases.

**Start with PostgREST**, add Edge Function later if needed for advanced features.

**Benefits**:
- ✅ No additional infrastructure
- ✅ Free (included in Supabase free tier)
- ✅ Real-time updates
- ✅ User-specific flags via RLS
- ✅ Type-safe with Kotlin SDK

**Upgrade path**: Can add Edge Function later for A/B testing, percentage rollouts, etc.

