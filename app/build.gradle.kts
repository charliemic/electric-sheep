import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp") version "1.9.25-1.0.20"
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.electricsheep.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.electricsheep.app"
        minSdk = 24
        targetSdk = 34
        
        // Read version from version.properties (managed by bump-version.sh)
        val versionPropertiesFile = rootProject.file("version.properties")
        val versionProperties = Properties().apply {
            if (versionPropertiesFile.exists()) {
                versionPropertiesFile.inputStream().use { load(it) }
            }
        }
        val appVersionName = versionProperties.getProperty("app.versionName", "1.0.0")
        val appVersionCode = versionProperties.getProperty("app.versionCode", "1").toInt()
        
        versionCode = appVersionCode
        versionName = appVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Feature flags - can be overridden per build type
        buildConfigField("boolean", "OFFLINE_ONLY_MODE", "false")
        buildConfigField("boolean", "SHOW_FEATURE_FLAG_INDICATOR_MODE", "false")
        buildConfigField("boolean", "ENABLE_TRIVIA_APP_MODE", "false")
        
        // Supabase configuration - read from local.properties
        val localPropertiesFile = rootProject.file("local.properties")
        fun readProperty(key: String, defaultValue: String): String {
            if (!localPropertiesFile.exists()) return defaultValue
            return localPropertiesFile.readLines()
                .find { it.startsWith("$key=") }
                ?.substringAfter("=")
                ?.trim()
                ?: defaultValue
        }
        
        val supabaseUrl = readProperty("supabase.url", "https://your-project.supabase.co")
        val supabaseAnonKey = readProperty("supabase.anon.key", "your-anon-key")
        
        // Staging Supabase configuration (optional, for testing)
        val supabaseStagingUrl = readProperty("supabase.staging.url", "")
        val supabaseStagingAnonKey = readProperty("supabase.staging.anon.key", "")
        
        // Supabase URL - default to placeholder, override in local.properties
        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
        
        // Supabase anon key - default to placeholder, override in local.properties
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"$supabaseAnonKey\"")
        
        // Staging Supabase (optional, for debug builds)
        buildConfigField("String", "SUPABASE_STAGING_URL", if (supabaseStagingUrl.isNotEmpty()) "\"$supabaseStagingUrl\"" else "\"\"")
        buildConfigField("String", "SUPABASE_STAGING_ANON_KEY", if (supabaseStagingAnonKey.isNotEmpty()) "\"$supabaseStagingAnonKey\"" else "\"\"")
    }

    signingConfigs {
        create("release") {
            // Read keystore configuration from environment variables or local.properties
            val localPropertiesFile = rootProject.file("local.properties")
            fun readProperty(key: String, defaultValue: String): String {
                // First check environment variables (for CI/CD)
                // Support both KEYSTORE_FILE (CI/CD convention) and keystore.file (local) formats
                val envKey = key.uppercase().replace(".", "_")
                val envValue = System.getenv(envKey) ?: System.getenv(key)
                if (envValue != null) return envValue
                
                // Fall back to local.properties (for local development)
                if (!localPropertiesFile.exists()) return defaultValue
                return localPropertiesFile.readLines()
                    .find { it.startsWith("$key=") }
                    ?.substringAfter("=")
                    ?.trim()
                    ?: defaultValue
            }
            
            val keystoreFile = readProperty("keystore.file", "")
            val keystorePassword = readProperty("keystore.password", "")
            val keyAlias = readProperty("keystore.key.alias", "")
            val keyPassword = readProperty("keystore.key.password", "")
            
            // Only apply signing config if all required values are present
            // Resolve keystore path relative to project root (not app module)
            if (keystoreFile.isNotEmpty() && keystorePassword.isNotEmpty() && 
                keyAlias.isNotEmpty() && keyPassword.isNotEmpty()) {
                // If path is absolute, use as-is; otherwise resolve from project root
                val keystorePath = if (keystoreFile.startsWith("/") || keystoreFile.matches(Regex("^[A-Za-z]:.*"))) {
                    file(keystoreFile)
                } else {
                    rootProject.file(keystoreFile)
                }
                storeFile = keystorePath
                storePassword = keystorePassword
                this.keyAlias = keyAlias
                this.keyPassword = keyPassword
            }
        }
    }

    buildTypes {
        debug {
            // Enable offline-only mode for debug builds (can be toggled)
            buildConfigField("boolean", "OFFLINE_ONLY_MODE", "false")
            // Allow switching to staging Supabase in debug builds
            buildConfigField("boolean", "USE_STAGING_SUPABASE", "true")
            // Enable trivia app in debug builds for testing
            buildConfigField("boolean", "ENABLE_TRIVIA_APP_MODE", "true")
        }
        release {
            // Use release signing config if available
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("boolean", "OFFLINE_ONLY_MODE", "false")
            // Staging is disabled in release builds
            buildConfigField("boolean", "USE_STAGING_SUPABASE", "false")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }
    lint {
        abortOnError = false
        checkReleaseBuilds = false
        // Enable accessibility checks
        enable += "Accessibility"
        // Check for missing content descriptions
        enable += "ContentDescription"
        // Check for touch target sizes (minimum 48dp)
        enable += "TouchTargetSize"
        // Check for color contrast issues
        enable += "IconColors"
        // Check for text contrast
        enable += "TextContrast"
    }
    
    testOptions {
        unitTests {
            isIncludeAndroidResources = false
        }
    }
}

// KSP configuration for Room schema export
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.1")
    
    // Chrome Custom Tabs for OAuth (recommended Android pattern)
    implementation("androidx.browser:browser:1.7.0")
    
    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    
    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.6")
    
    // Supabase
    implementation(platform("io.github.jan-tennert.supabase:bom:2.3.1"))
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    implementation("io.github.jan-tennert.supabase:realtime-kt")
    implementation("io.github.jan-tennert.supabase:storage-kt")
    implementation("io.github.jan-tennert.supabase:functions-kt")
    implementation("io.github.jan-tennert.supabase:gotrue-kt") // Auth support
    // HTTP client engine for Supabase (required)
    implementation("io.ktor:ktor-client-android:2.3.13")
    // OkHttp engine for certificate pinning
    implementation("io.ktor:ktor-client-okhttp:2.3.13")
    // OkHttp for certificate pinning (Ktor Android uses OkHttp engine)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    
    // Kotlinx Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    
    // Coroutines for async operations
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    
    // Room for local caching (optional, can use Supabase local cache)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    
    // WorkManager for background sync
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    
    // Vico chart library for Compose
    implementation("com.patrykandpatrick.vico:compose:1.16.1")
    implementation("com.patrykandpatrick.vico:compose-m3:1.16.1")
    implementation("com.patrykandpatrick.vico:core:1.16.1")
    
    // QR Code generation for MFA setup
    implementation("com.google.zxing:core:3.5.2")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.10.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

// Configure test execution for Android unit tests
// Android unit tests use AndroidUnitTest tasks, but they extend Test
// Configure after evaluation to ensure all test tasks are created
afterEvaluate {
    tasks.withType<Test> {
        maxParallelForks = Runtime.getRuntime().availableProcessors()
        forkEvery = 0 // Don't fork for each test class - faster execution
        
        // Ensure JUnit XML output for GitHub Actions
        reports {
            junitXml.required.set(true)
            html.required.set(true)
        }
        
        // Output test results to standard location
        testLogging {
            events("passed", "skipped", "failed")
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }
    }
}

