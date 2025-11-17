plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp") version "1.9.20-1.0.14"
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.electricsheep.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.electricsheep.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Feature flags - can be overridden per build type
        buildConfigField("boolean", "OFFLINE_ONLY_MODE", "false")
        
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
        
        // Supabase URL - default to placeholder, override in local.properties
        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
        
        // Supabase anon key - default to placeholder, override in local.properties
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"$supabaseAnonKey\"")
    }

    buildTypes {
        debug {
            // Enable offline-only mode for debug builds (can be toggled)
            buildConfigField("boolean", "OFFLINE_ONLY_MODE", "false")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("boolean", "OFFLINE_ONLY_MODE", "false")
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
}

// KSP configuration for Room schema export
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.1")
    
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
    implementation("io.ktor:ktor-client-android:2.3.5")
    
    // Kotlinx Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    
    // Coroutines for async operations
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // Room for local caching (optional, can use Supabase local cache)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    
    // WorkManager for background sync
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.10.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

