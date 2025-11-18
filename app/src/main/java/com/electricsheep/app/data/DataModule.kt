package com.electricsheep.app.data

import android.content.Context
import androidx.room.Room
import com.electricsheep.app.auth.UserManager
import com.electricsheep.app.config.ConfigBasedFeatureFlagProvider
import com.electricsheep.app.config.FeatureFlagManager
import com.electricsheep.app.data.local.AppDatabase
import com.electricsheep.app.data.local.MoodDao
import com.electricsheep.app.data.migration.DatabaseMigrations
import com.electricsheep.app.data.remote.SupabaseDataSource
import com.electricsheep.app.data.repository.MoodRepository
import com.electricsheep.app.util.Logger
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.FlowType
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

/**
 * Data module for dependency injection and setup.
 * Provides instances of data sources, repositories, and database.
 */
object DataModule {
    
    /**
     * Create and configure Supabase client
     * Reads configuration from BuildConfig (populated from local.properties)
     * 
     * @return SupabaseClient instance, or null if offline-only mode or initialization fails
     */
    fun createSupabaseClient(featureFlagManager: FeatureFlagManager? = null): SupabaseClient? {
        // Skip Supabase initialization if offline-only mode is enabled
        if (featureFlagManager?.isOfflineOnly() == true) {
            Logger.info("DataModule", "Skipping Supabase client creation: offline-only mode enabled")
            return null
        }
        
        val supabaseUrl = com.electricsheep.app.BuildConfig.SUPABASE_URL
        val supabaseKey = com.electricsheep.app.BuildConfig.SUPABASE_ANON_KEY
        
        // Validate configuration
        if (supabaseUrl == "https://your-project.supabase.co" || supabaseKey == "your-anon-key") {
            Logger.warn("DataModule", "Supabase credentials not configured. Using placeholder values.")
            Logger.warn("DataModule", "Add supabase.url and supabase.anon.key to local.properties")
            Logger.warn("DataModule", "App will continue in offline-only mode")
            return null
        }
        
        return try {
            Logger.info("DataModule", "Initialising Supabase client for: $supabaseUrl")
            
            createSupabaseClient(
                supabaseUrl = supabaseUrl,
                supabaseKey = supabaseKey
            ) {
                install(Postgrest)
                install(Realtime)
                install(Auth) {
                    // Enable PKCE flow (OAuth 2.1 best practice)
                    flowType = FlowType.PKCE
                    // Configure deep link for OAuth callbacks
                    scheme = "com.electricsheep.app"
                    host = "auth-callback"
                }
            }
        } catch (e: com.electricsheep.app.error.NetworkError) {
            e.log("DataModule", "Failed to create Supabase client - network error")
            Logger.warn("DataModule", "App will continue in offline-only mode")
            null
        } catch (e: com.electricsheep.app.error.SystemError) {
            e.log("DataModule", "Failed to create Supabase client - system error")
            Logger.warn("DataModule", "App will continue in offline-only mode")
            null
        } catch (e: Exception) {
            val systemError = com.electricsheep.app.error.SystemError.ConfigurationError(
                config = "Supabase client",
                errorCause = e
            )
            systemError.log("DataModule", "Failed to create Supabase client")
            Logger.warn("DataModule", "App will continue in offline-only mode")
            null
        }
    }
    
    /**
     * Create Room database instance
     */
    fun createDatabase(context: Context): AppDatabase {
        Logger.info("DataModule", "Creating Room database")
        
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "electric_sheep_database"
        )
            .addMigrations(*DatabaseMigrations.getMigrations())
            // Note: fallbackToDestructiveMigration() is not called to prevent data loss
            .build()
    }
    
    /**
     * Create remote data source
     * 
     * @param supabaseClient Supabase client (can be null if offline-only)
     * @return SupabaseDataSource instance, or null if client is null
     */
    fun createRemoteDataSource(supabaseClient: SupabaseClient?): SupabaseDataSource? {
        if (supabaseClient == null) {
            Logger.debug("DataModule", "Skipping remote data source creation: Supabase client is null")
            return null
        }
        Logger.debug("DataModule", "Creating remote data source")
        return SupabaseDataSource(supabaseClient)
    }
    
    /**
     * Create feature flag manager with composite provider (Supabase primary, Config fallback).
     * 
     * Features using flags are agnostic to the source - they always get a value,
     * either from Supabase (primary) or BuildConfig (fallback).
     * 
     * @param context Android context for cache creation
     * @param supabaseClient Supabase client (can be null if offline-only)
     * @param userManager User manager for user-specific flags (can be null for initial setup)
     * @return FeatureFlagManager instance
     */
    fun createFeatureFlagManager(
        context: android.content.Context,
        supabaseClient: SupabaseClient?,
        userManager: com.electricsheep.app.auth.UserManager?
    ): FeatureFlagManager {
        Logger.debug("DataModule", "Creating feature flag manager")
        
        // Always create config-based provider as fallback
        val fallbackProvider = ConfigBasedFeatureFlagProvider()
        
        val provider = if (supabaseClient != null && userManager != null) {
            // Create cache for TTL support
            val cache = com.electricsheep.app.config.FeatureFlagCache(context)
            
            // Use composite provider: Supabase (primary) + Config (fallback)
            Logger.info("DataModule", "Using composite feature flag provider (Supabase primary, Config fallback)")
            val primaryProvider = com.electricsheep.app.config.SupabaseFeatureFlagProvider(supabaseClient, userManager, cache)
            com.electricsheep.app.config.CompositeFeatureFlagProvider(primaryProvider, fallbackProvider)
        } else {
            // Offline-only or initial setup: use config provider only
            Logger.info("DataModule", "Using config-based feature flag provider (offline-only or initial setup)")
            fallbackProvider
        }
        
        return FeatureFlagManager(provider)
    }
    
    /**
     * Create mood repository
     */
    fun createMoodRepository(
        moodDao: MoodDao,
        remoteDataSource: SupabaseDataSource?,
        featureFlagManager: FeatureFlagManager,
        userManager: UserManager
    ): MoodRepository {
        Logger.info("DataModule", "Creating mood repository")
        return MoodRepository(moodDao, remoteDataSource, featureFlagManager, userManager)
    }
}

