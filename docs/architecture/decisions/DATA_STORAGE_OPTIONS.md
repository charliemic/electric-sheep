# Offline-First Data Storage Options

This document compares open-source options for offline-first data storage in the Electric Sheep Android app, considering ease of implementation and ease of hosting.

## Evaluation Criteria

- **Ease of Implementation**: How easy it is to integrate and use in Kotlin/Android
- **Ease of Hosting**: How simple it is to self-host or use managed hosting
- **Offline-First**: Built-in support for offline functionality and sync
- **Open Source**: Fully open-source solution
- **Kotlin Multiplatform**: Support for KMP if we expand to other platforms

## Top Recommendations

### 1. Supabase (Recommended) ⭐

**Overview**: Open-source Firebase alternative with PostgreSQL backend and real-time sync

**Pros**:
- ✅ Fully open-source (can self-host or use managed cloud)
- ✅ Built-in offline-first with real-time sync
- ✅ PostgreSQL backend (industry standard, easy to host)
- ✅ Excellent Kotlin/Android SDK support
- ✅ Real-time subscriptions for live updates
- ✅ Row-level security for data access control
- ✅ Easy to host: Docker deployment or managed cloud
- ✅ Free tier available for development
- ✅ REST API + real-time subscriptions
- ✅ Built-in authentication, storage, and edge functions

**Cons**:
- ⚠️ Requires PostgreSQL server (but easy to host)
- ⚠️ Real-time features require WebSocket connection
- ⚠️ Learning curve if unfamiliar with PostgreSQL

**Hosting Options**:
- **Self-hosted**: Docker Compose setup (very easy)
- **Managed**: Supabase Cloud (free tier, then pay-as-you-go)
- **Other**: Railway, Fly.io, DigitalOcean (one-click deployments)

**Implementation**:
```kotlin
// Example: Supabase Kotlin client
val supabase = createSupabaseClient(
    supabaseUrl = "https://your-project.supabase.co",
    supabaseKey = "your-anon-key"
) {
    install(Realtime)
    install(Postgrest)
}

// Offline-first with sync
val moods = supabase.from("moods")
    .select()
    .decodeList<Mood>()
```

**Best For**: Production-ready solution with excellent developer experience

---

### 2. Appwrite

**Overview**: Open-source backend-as-a-service with built-in offline support

**Pros**:
- ✅ Fully open-source and self-hostable
- ✅ Easy Docker deployment
- ✅ Built-in offline capabilities
- ✅ Multiple database options (MySQL, MariaDB, MongoDB)
- ✅ REST API with SDK support
- ✅ Built-in authentication, storage, and functions
- ✅ Good Kotlin SDK
- ✅ Simple hosting (single Docker container)

**Cons**:
- ⚠️ Offline sync requires custom implementation
- ⚠️ Less mature than Supabase
- ⚠️ Smaller community
- ⚠️ Real-time features less robust

**Hosting Options**:
- **Self-hosted**: Single Docker container (very easy)
- **Managed**: Appwrite Cloud (limited free tier)

**Implementation**:
```kotlin
// Example: Appwrite Kotlin client
val client = Client()
    .setEndpoint("https://cloud.appwrite.io/v1")
    .setProject("your-project-id")

val database = Database(client)
```

**Best For**: Simple self-hosted backend with good offline support

---

### 3. PocketBase

**Overview**: Lightweight, single-file backend with SQLite and real-time

**Pros**:
- ✅ Extremely lightweight (single executable)
- ✅ SQLite-based (no separate database server needed)
- ✅ Built-in admin UI
- ✅ Real-time subscriptions
- ✅ Very easy to host (single binary)
- ✅ REST API + real-time
- ✅ Built-in authentication
- ✅ Perfect for small to medium apps

**Cons**:
- ⚠️ SQLite limitations (not ideal for high concurrency)
- ⚠️ Less mature ecosystem
- ⚠️ Smaller community
- ⚠️ Offline sync requires custom implementation
- ⚠️ Limited Kotlin SDK (mostly REST API)

**Hosting Options**:
- **Self-hosted**: Single binary, very easy
- **Managed**: Can run on any VPS

**Implementation**:
```kotlin
// Example: PocketBase REST API
val client = OkHttpClient()
val request = Request.Builder()
    .url("https://your-pb-instance.com/api/collections/moods/records")
    .build()
```

**Best For**: Small to medium apps needing simple, lightweight backend

---

### 4. Room + Custom Sync (Traditional Approach)

**Overview**: Android's Room database with custom sync implementation

**Pros**:
- ✅ Native Android solution (Room)
- ✅ Full control over sync logic
- ✅ No external dependencies
- ✅ Works completely offline
- ✅ Well-documented and mature

**Cons**:
- ⚠️ Requires building sync infrastructure yourself
- ⚠️ More development time
- ⚠️ Need to choose and host backend separately
- ⚠️ Conflict resolution must be implemented
- ⚠️ No built-in real-time updates

**Hosting Options**:
- **Backend**: Any REST API (Express, Django, etc.)
- **Database**: PostgreSQL, MySQL, MongoDB (your choice)

**Implementation**:
```kotlin
// Room for local storage
@Database(entities = [Mood::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun moodDao(): MoodDao
}

// Custom sync with WorkManager
class SyncWorker : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        // Sync logic here
    }
}
```

**Best For**: Maximum control, willing to build sync infrastructure

---

### 5. CouchDB / Couchbase Lite

**Overview**: Document database with built-in sync (CouchDB protocol)

**Pros**:
- ✅ True offline-first with automatic sync
- ✅ Conflict resolution built-in
- ✅ Peer-to-peer sync possible
- ✅ Document-based (flexible schema)

**Cons**:
- ⚠️ More complex setup
- ⚠️ CouchDB hosting can be challenging
- ⚠️ Less common in Android ecosystem
- ⚠️ Learning curve for CouchDB concepts
- ⚠️ Limited Kotlin SDK support

**Hosting Options**:
- **Self-hosted**: CouchDB server (moderate complexity)
- **Managed**: Cloudant (IBM, paid), Couchbase Cloud

**Best For**: Complex sync requirements, document-based data

---

## Comparison Matrix

| Solution | Ease of Implementation | Ease of Hosting | Offline-First | Open Source | Real-Time | Best For |
|----------|----------------------|-----------------|---------------|-------------|-----------|----------|
| **Supabase** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ✅ | ✅ | Production apps |
| **Appwrite** | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ✅ | ⭐⭐⭐ | Self-hosted BaaS |
| **PocketBase** | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ✅ | ⭐⭐⭐ | Small/medium apps |
| **Room + Custom** | ⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐ | ✅ | ❌ | Maximum control |
| **CouchDB** | ⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ✅ | ✅ | Complex sync |

## Recommendation: Supabase

**Why Supabase is the best choice:**

1. **Ease of Implementation**: 
   - Excellent Kotlin SDK with offline support
   - Real-time subscriptions out of the box
   - Type-safe queries with code generation

2. **Ease of Hosting**:
   - One-command Docker Compose setup for self-hosting
   - Managed cloud option with generous free tier
   - Can deploy to any platform (Railway, Fly.io, DigitalOcean)

3. **Offline-First**:
   - Built-in offline support in SDK
   - Automatic conflict resolution
   - Real-time sync when connection restored

4. **Developer Experience**:
   - Great documentation
   - Large community
   - Active development
   - PostgreSQL (familiar to many developers)

## Implementation Plan for Supabase

### Phase 1: Setup
1. Create Supabase project (cloud or self-hosted)
2. Add Supabase Kotlin SDK dependency
3. Configure local database schema

### Phase 2: Local Storage
1. Use Supabase local cache (built-in)
2. Or combine with Room for complex queries
3. Implement data models

### Phase 3: Sync
1. Configure real-time subscriptions
2. Handle offline/online state
3. Implement conflict resolution

### Phase 4: Testing
1. Test offline functionality
2. Test sync behavior
3. Test conflict resolution

## Alternative: Start with Room, Migrate Later

If you want to start simple and add sync later:
1. Use Room for local storage (immediate offline support)
2. Build basic features with local-only data
3. Add Supabase/Appwrite sync layer when needed
4. Migrate data to remote store

This approach lets you validate the app concept before investing in sync infrastructure.

---

## Next Steps

1. **Decision**: Choose between Supabase (recommended) or Room + Custom Sync
2. **Proof of Concept**: Set up chosen solution and test basic CRUD operations
3. **Integration**: Add to mood management feature
4. **Testing**: Verify offline functionality and sync behavior

