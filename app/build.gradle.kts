import java.io.FileInputStream
import java.util.Properties

plugins {
    id("openlibrarybooks.android.application")
    id("openlibrarybooks.android.hilt")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.firebase.perf)
}

// Load local.properties
val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        load(FileInputStream(localPropertiesFile))
    }
}

// Read properties with fallback for CI builds
fun getLocalProperty(key: String, defaultValue: String = ""): String =
    localProperties.getProperty(key) ?: System.getenv(key) ?: defaultValue

android {
    namespace = "com.darach.openlibrarybooks"

    defaultConfig {
        applicationId = "com.darach.openlibrarybooks"
        versionCode = 1
        versionName = "1.0.0"

        // BuildConfig fields
        buildConfigField("String", "DEFAULT_USERNAME", "\"${getLocalProperty("DEFAULT_USERNAME")}\"")
        buildConfigField("String", "OPEN_LIBRARY_BASE_URL", "\"https://openlibrary.org\"")
        buildConfigField("String", "COVERS_BASE_URL", "\"https://covers.openlibrary.org\"")
        buildConfigField("long", "CONNECT_TIMEOUT", "30L")
        buildConfigField("long", "READ_TIMEOUT", "60L")
        buildConfigField("String", "VERSION_NAME", "\"${defaultConfig.versionName}\"")
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    // Core modules
    implementation(project(":core:common"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:domain"))
    implementation(project(":core:data"))

    // Feature modules
    implementation(project(":feature:books"))
    implementation(project(":feature:favourites"))
    implementation(project(":feature:settings"))
    implementation(project(":feature:widget"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Material 3 Adaptive Navigation
    implementation(libs.androidx.compose.material3.adaptive.navigation.suite)
    implementation(libs.androidx.compose.material.icons.extended)

    // Navigation Compose with type-safe arguments
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.kotlinx.serialization.json)

    // WorkManager with Hilt and RxJava3
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.work.rxjava3)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    // Firebase (using BOM for version management)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.crashlytics.ndk)
    implementation(libs.firebase.perf)

    // Compose dependencies (BOM and common libraries added by convention plugin)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    // Konsist for architecture testing
    testImplementation(libs.konsist)
}
