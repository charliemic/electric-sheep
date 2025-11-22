package com.electricsheep.app

import android.app.Application
import com.electricsheep.app.auth.AuthModule
import com.electricsheep.app.auth.AuthProvider
import com.electricsheep.app.auth.MfaManager
import com.electricsheep.app.auth.SupabaseAuthProvider
import com.electricsheep.app.auth.UserManager
import com.electricsheep.app.config.EnvironmentManager
import com.electricsheep.app.config.FeatureFlagManager
import com.electricsheep.app.data.DataModule
import com.electricsheep.app.data.repository.MoodRepository
import com.electricsheep.app.data.sync.SyncManager
import com.electricsheep.app.data.sync.MoodSyncWorkerFactory
import com.electricsheep.app.util.Logger
import androidx.work.Configuration
import io.github.jan.supabase.SupabaseClient
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
    private lateinit var featureFlagManager: FeatureFlagManager
    private lateinit var environmentManager: EnvironmentManager
    private var moodRepository: MoodRepository? = null
    private var supabaseClient: SupabaseClient? = null
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()

        installGlobalExceptionHandler()

        try {
            Logger.info("ElectricSheepApplication", "Application onCreate")

            // Create environment manager for runtime environment selection (debug builds only)
            environmentManager = EnvironmentManager(this)

            // Create initial feature flag manager (config-based) for Supabase client creation
            val initialFeatureFlagManager = DataModule.createFeatureFlagManager(this, null, null)
            supabaseClient = DataModule.createSupabaseClient(initialFeatureFlagManager, environmentManager)
            userManager = initializeAuth(supabaseClient)

            // Recreate feature flag manager with Supabase client and user manager
            featureFlagManager = DataModule.createFeatureFlagManager(this, supabaseClient, userManager)

            // Initialize feature flags from Supabase
            applicationScope.launch {
                try {
                    // Check if provider is composite and get primary provider
                    val primaryProvider = when (val provider = featureFlagManager.provider) {
                        is com.electricsheep.app.config.CompositeFeatureFlagProvider -> {
                            // Get primary provider from composite
                            provider.primaryProvider
                        }
                        is com.electricsheep.app.config.SupabaseFeatureFlagProvider -> {
                            provider
                        }
                        else -> null
                    }

                    if (primaryProvider is com.electricsheep.app.config.SupabaseFeatureFlagProvider) {
                        primaryProvider.initialise()
                    }
                } catch (e: Exception) {
                    Logger.warn("ElectricSheepApplication", "Failed to initialise feature flags from Supabase", e)
                }
            }

            moodRepository = initializeDataLayer(featureFlagManager, userManager, supabaseClient)
            initializeSync(featureFlagManager)

            Logger.info("ElectricSheepApplication", "Application initialised successfully")
        } catch (e: com.electricsheep.app.error.SystemError) {
            e.log("ElectricSheepApplication", "Critical system error during application initialisation")
            // Log to crash reporting service in production
            // The app will continue but may be in a degraded state
        } catch (e: Exception) {
            val systemError = com.electricsheep.app.error.SystemError.Unknown(e)
            systemError.log("ElectricSheepApplication", "Critical error during application initialisation")
            // Log to crash reporting service in production
            // The app will continue but may be in a degraded state
        }
    }

    /**
     * Install global exception handler to catch uncaught exceptions.
     */
    private fun installGlobalExceptionHandler() {
        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            // Classify and log the error appropriately
            when (exception) {
                is com.electricsheep.app.error.AppError -> {
                    exception.log("ElectricSheepApplication", "Uncaught error in thread: ${thread.name}")
                }
                else -> {
                    Logger.error("ElectricSheepApplication", "Uncaught exception in thread: ${thread.name}", exception)
                }
            }
            // Log to crash reporting service in production
            // For now, just log and let the default handler process it
            val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
            defaultHandler?.uncaughtException(thread, exception)
        }
    }

    /**
     * Initialise authentication components.
     */
    private fun initializeAuth(supabaseClient: SupabaseClient?): UserManager {
        Logger.debug("ElectricSheepApplication", "Initialising authentication")
        val authProvider = AuthModule.createAuthProvider(this, supabaseClient)
        val userManager = AuthModule.createUserManager(authProvider)

        // Initialise user manager (loads current user if authenticated)
        applicationScope.launch {
            try {
                userManager.initialise()
            } catch (e: com.electricsheep.app.error.SystemError) {
                e.log("ElectricSheepApplication", "Failed to initialise user manager")
            } catch (e: Exception) {
                Logger.error("ElectricSheepApplication", "Failed to initialise user manager", e)
            }
        }

        return userManager
    }


    /**
     * Initialise data layer (database, repository).
     */
    private fun initializeDataLayer(
        featureFlagManager: FeatureFlagManager,
        userManager: UserManager,
        supabaseClient: SupabaseClient?
    ): MoodRepository? {
        return try {
            Logger.debug("ElectricSheepApplication", "Initialising data layer")

            val database = DataModule.createDatabase(this)
            val moodDao = database.moodDao()

            val remoteDataSource = DataModule.createRemoteDataSource(supabaseClient)

            val repository = DataModule.createMoodRepository(
                moodDao,
                remoteDataSource,
                featureFlagManager,
                userManager
            )

            Logger.info("ElectricSheepApplication", "Data layer initialised successfully")
            repository
        } catch (e: com.electricsheep.app.error.SystemError) {
            e.log("ElectricSheepApplication", "Failed to initialise data layer")
            // App will continue but may have limited functionality
            // In production, you might want to show an error screen or retry
            null
        } catch (e: Exception) {
            val systemError = com.electricsheep.app.error.SystemError.Unknown(e)
            systemError.log("ElectricSheepApplication", "Failed to initialise data layer")
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
        } catch (e: com.electricsheep.app.error.SystemError) {
            e.log("ElectricSheepApplication", "Failed to initialise WorkManager")
            // Continue without WorkManager - app can still function
        } catch (e: Exception) {
            val systemError = com.electricsheep.app.error.SystemError.Unknown(e)
            systemError.log("ElectricSheepApplication", "Failed to initialise WorkManager")
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

    /**
     * Get Supabase client instance (for OAuth deep link handling)
     * Returns null if Supabase client failed to initialise
     */
    fun getSupabaseClient(): SupabaseClient? = supabaseClient

    /**
     * Get feature flag manager instance (for feature flag access)
     */
    fun getFeatureFlagManager(): FeatureFlagManager = featureFlagManager

    /**
     * Get environment manager instance (for environment switching in debug builds)
     */
    fun getEnvironmentManager(): EnvironmentManager = environmentManager
    
    /**
     * Get MFA manager instance (for MFA operations).
     * Returns null if Supabase client is not available (offline mode).
     */
    fun getMfaManager(): MfaManager? {
        return supabaseClient?.let { MfaManager(it) }
    }
    
    /**
     * Get authentication provider instance.
     * Returns null if auth provider is not available.
     */
    fun getAuthProvider(): AuthProvider? {
        return userManager.authProvider
    }

    /**
     * Reinitialize Supabase client with new environment.
     * This will log out the current user and recreate the Supabase client.
     * Should be called when environment is switched in debug builds.
     *
     * @return true if reinitialization was successful, false otherwise
     */
    suspend fun reinitializeSupabaseClient(): Boolean {
        if (!com.electricsheep.app.BuildConfig.DEBUG) {
            Logger.warn("ElectricSheepApplication", "Cannot reinitialize Supabase client in release builds")
            return false
        }

        return try {
            Logger.info("ElectricSheepApplication", "Reinitializing Supabase client with new environment")

            // Sign out current user (auth tokens are environment-specific)
            if (userManager.isAuthenticated) {
                Logger.info("ElectricSheepApplication", "Signing out user before environment switch")
                userManager.signOut()
            }

            // Create new Supabase client with selected environment
            val newClient = DataModule.createSupabaseClient(featureFlagManager, environmentManager)
            if (newClient == null) {
                Logger.error("ElectricSheepApplication", "Failed to create new Supabase client")
                return false
            }

            supabaseClient = newClient

            // Reinitialize user manager with new client
            val newUserManager = initializeAuth(newClient)
            userManager = newUserManager

            // Recreate feature flag manager with new client
            featureFlagManager = DataModule.createFeatureFlagManager(this, newClient, newUserManager)

            // Reinitialize feature flags from new environment
            val primaryProvider = when (val provider = featureFlagManager.provider) {
                is com.electricsheep.app.config.CompositeFeatureFlagProvider -> {
                    provider.primaryProvider
                }
                is com.electricsheep.app.config.SupabaseFeatureFlagProvider -> {
                    provider
                }
                else -> null
            }

            if (primaryProvider is com.electricsheep.app.config.SupabaseFeatureFlagProvider) {
                primaryProvider.initialise()
            }

            // Reinitialize mood repository with new client
            moodRepository = initializeDataLayer(featureFlagManager, newUserManager, newClient)

            Logger.info("ElectricSheepApplication", "Supabase client reinitialized successfully")
            true
        } catch (e: Exception) {
            Logger.error("ElectricSheepApplication", "Failed to reinitialize Supabase client", e)
            false
        }
    }
}
