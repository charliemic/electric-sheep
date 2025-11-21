plugins {
    kotlin("jvm") version "1.9.20"
    application
}

group = "com.electricsheep"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    // Appium Java Client (official SDK)
    implementation("io.appium:java-client:9.0.0")
    
    // Kotlin coroutines for async operations
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    
    // JSON parsing for test plans
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.15.2")
    
    // Logging
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("ch.qos.logback:logback-classic:1.4.11")
    
    // HTTP client for AI API calls
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    
    // OpenCV for pattern recognition (template matching)
<<<<<<< HEAD
    // Try multiple OpenCV options for compatibility
    implementation("org.openpnp:opencv:4.9.0-0")
    // Fallback option if above doesn't work:
    // implementation("nu.pattern:opencv:5.7.0-3")
    
    // OCR for text detection from screenshots (optional - using command-line Tesseract for now)
    // implementation("net.sourceforge.tess4j:tess4j:5.8.0")
=======
    implementation("nu.pattern:opencv:2.4.9-7")
>>>>>>> origin/main
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

application {
    mainClass.set("com.electricsheep.testautomation.MainKt")
}

<<<<<<< HEAD
// Task to test email generation (quick test, no emulator needed)
tasks.register<JavaExec>("testEmailGeneration") {
    mainClass.set("com.electricsheep.testautomation.planner.TestEmailGenerationKt")
    classpath = sourceSets["main"].runtimeClasspath
}

=======
>>>>>>> origin/main
// Task to run OCR integration test
tasks.register<JavaExec>("testOcr") {
    group = "verification"
    description = "Run OCR integration test on a screenshot"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.electricsheep.testautomation.vision.OcrIntegrationTestKt")
    val screenshotPath = if (project.hasProperty("screenshot")) {
        project.property("screenshot") as String
    } else {
        "test-results/screenshots"
    }
    args = listOf(screenshotPath)
    dependsOn("classes")
}

// Task to run Pattern Recognition integration test
tasks.register<JavaExec>("testPatternRecognition") {
    group = "verification"
    description = "Run Pattern Recognition integration test on a screenshot"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.electricsheep.testautomation.vision.PatternRecognitionIntegrationTestKt")
    val screenshotPath = if (project.hasProperty("screenshot")) {
        project.property("screenshot") as String
    } else {
        "test-results/screenshots"
    }
    args = listOf(screenshotPath)
    dependsOn("classes")
}

<<<<<<< HEAD
// Task to test template management architecture
tasks.register<JavaExec>("testTemplateManagement") {
    group = "verification"
    description = "Test hybrid template management architecture"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.electricsheep.testautomation.templates.TemplateManagementTestKt")
    dependsOn("classes")
}

// Task to generate templates from app codebase
tasks.register<JavaExec>("generateTemplates") {
    group = "generation"
    description = "Generate pattern recognition templates from app codebase"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.electricsheep.testautomation.templates.TemplateGeneratorMainKt")
    val appSourceRoot = if (project.hasProperty("appSourceRoot")) {
        project.property("appSourceRoot") as String
    } else {
        "../app/src/main/java"
    }
    val appResDrawableDir = if (project.hasProperty("appResDrawableDir")) {
        project.property("appResDrawableDir") as String
    } else {
        "../app/src/main/res/drawable"
    }
    val templateOutputDir = if (project.hasProperty("templateOutputDir")) {
        project.property("templateOutputDir") as String
    } else {
        "src/main/resources/templates"
    }
    args = listOf(appSourceRoot, appResDrawableDir, templateOutputDir)
    dependsOn("classes")
}

=======
>>>>>>> origin/main
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
    }
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "17"
    targetCompatibility = "17"
}

