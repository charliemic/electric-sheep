package com.electricsheep.app

import android.app.Application
import com.electricsheep.app.auth.AuthModule
import com.electricsheep.app.auth.UserManager
import com.electricsheep.app.config.FeatureFlagManager
import com.electricsheep.app.data.DataModule
import com.electricsheep.app.data.repository.MoodRepository
import com.electricsheep.app.data.sync.SyncManager
import com.electricsheep.app.data.sync.MoodSyncWorkerFactory
import com.electricsheep.app.util.Logger
import androidx.work.Configuration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Application class for Electric Sheep app.
 * Initialises data layer and starts background sync.
 */
class ElectricSheepApplication : Application() {
    
    private lateinit var syncManager: SyncManager
    private lateinit var userManager: UserManager
    private var moodRepository: MoodRepository? = null
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    override fun onCreate() {
        super.onCreate()
        
        installGlobalExceptionHandler()
        
        try {
            Logger.info("ElectricSheepApplication", "Application onCreate")
            
            userManager = initializeAuth()
            val featureFlagManager = initializeFeatureFlags()
            moodRepository = initializeDataLayer(featureFlagManager, userManager)
            initializeSync(featureFlagManager)
            
            Logger.info("ElectricSheepApplication", "Application initialised successfully")
        } catch (e: Exception) {
            Logger.error("ElectricSheepApplication", "Critical error during application initialisation", e)
            // Log to crash reporting service in production
            // The app will continue but may be in a degraded state
        }
    }
    
    /**
     * Install global exception handler to catch uncaught exceptions.
     */
    private fun installGlobalExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            Logger.error("ElectricSheepApplication", "Uncaught exception in thread: ${thread.name}", exception)
            // Log to crash reporting service in production
            // For now, just log and let the default handler process it
            val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
            defaultHandler?.uncaughtException(thread, exception)
        }
    }
    
    /**
     * Initialise authentication components.
     */
    private fun initializeAuth(): UserManager {
        Logger.debug("ElectricSheepApplication", "Initialising authentication")
        val authProvider = AuthModule.createAuthProvider()
        val userManager = AuthModule.createUserManager(authProvider)
        
        // Initialise user manager (loads current user if authenticated)
        applicationScope.launch {
            try {
                userManager.initialise()
            } catch (e: Exception) {
                Logger.error("ElectricSheepApplication", "Failed to initialise user manager", e)
            }
        }
        
        return userManager
    }
    
    /**
     * Initialise feature flags.
     */
    private fun initializeFeatureFlags(): FeatureFlagManager {
        Logger.debug("ElectricSheepApplication", "Initialising feature flags")
        return DataModule.createFeatureFlagManager()
    }
    
    /**
     * Initialise data layer (database, repository).
     */
    private fun initializeDataLayer(
        featureFlagManager: FeatureFlagManager,
        userManager: UserManager
    ): MoodRepository? {
        return try {
            Logger.debug("ElectricSheepApplication", "Initialising data layer")
            
            val database = DataModule.createDatabase(this)
            val moodDao = database.moodDao()
            
            // Only create Supabase client if not in offline-only mode
            val supabaseClient = DataModule.createSupabaseClient(featureFlagManager)
            val remoteDataSource = DataModule.createRemoteDataSource(supabaseClient)
            
            val repository = DataModule.createMoodRepository(
                moodDao,
                remoteDataSource,
                featureFlagManager,
                userManager
            )
            
            Logger.info("ElectricSheepApplication", "Data layer initialised successfully")
            repository
        } catch (e: Exception) {
            Logger.error("ElectricSheepApplication", "Failed to initialise data layer", e)
            // App will continue but may have limited functionality
            // In production, you might want to show an error screen or retry
            null
        }
    }
    
    /**
     * Initialise background sync (WorkManager).
     */
    private fun initializeSync(featureFlagManager: FeatureFlagManager) {
        try {
            Logger.debug("ElectricSheepApplication", "Initialising background sync")
            
            if (moodRepository == null) {
                Logger.warn("ElectricSheepApplication", "Skipping sync initialisation: mood repository not available")
                return
            }
            
            // Configure WorkManager with custom factory for dependency injection
            val workerFactory = MoodSyncWorkerFactory(moodRepository!!)
            val workManagerConfig = Configuration.Builder()
                .setWorkerFactory(workerFactory)
                .build()
            androidx.work.WorkManager.initialize(this, workManagerConfig)
            
            // Start periodic background sync (will skip if offline-only mode is enabled)
            syncManager = SyncManager(this, featureFlagManager)
            syncManager.startPeriodicSync()
            
            Logger.info("ElectricSheepApplication", "Background sync initialised successfully")
        } catch (e: Exception) {
            Logger.error("ElectricSheepApplication", "Failed to initialise WorkManager", e)
            // Continue without WorkManager - app can still function
        }
    }
    
    /**
     * Get sync manager instance (for manual sync triggers if needed)
     */
    fun getSyncManager(): SyncManager = syncManager
    
    /**
     * Get user manager instance (for authentication operations)
     */
    fun getUserManager(): UserManager = userManager
    
    /**
     * Get mood repository instance (for mood data operations)
     * Returns null if data layer failed to initialise
     */
    fun getMoodRepository(): MoodRepository? = moodRepository
}
