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
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

/**
 * Data module for dependency injection and setup.
 * Provides instances of data sources, repositories, and database.
 */
object DataModule {
    
    /**
     * Create and configure Supabase client
     * TODO: Move these to BuildConfig or environment variables
     * 
     * @return SupabaseClient instance, or null if offline-only mode or initialization fails
     */
    fun createSupabaseClient(featureFlagManager: FeatureFlagManager? = null): SupabaseClient? {
        // Skip Supabase initialization if offline-only mode is enabled
        if (featureFlagManager?.isOfflineOnly() == true) {
            Logger.info("DataModule", "Skipping Supabase client creation: offline-only mode enabled")
            return null
        }
        
        val supabaseUrl = "https://your-project.supabase.co" // TODO: Replace with actual URL
        val supabaseKey = "your-anon-key" // TODO: Replace with actual key
        
        return try {
            Logger.info("DataModule", "Initialising Supabase client")
            
            createSupabaseClient(
                supabaseUrl = supabaseUrl,
                supabaseKey = supabaseKey
            ) {
                install(Postgrest)
                install(Realtime)
            }
        } catch (e: Exception) {
            Logger.error("DataModule", "Failed to create Supabase client", e)
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
     * Create feature flag manager
     */
    fun createFeatureFlagManager(): FeatureFlagManager {
        Logger.debug("DataModule", "Creating feature flag manager")
        val provider = ConfigBasedFeatureFlagProvider()
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

