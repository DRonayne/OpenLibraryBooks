import java.io.FileInputStream
import java.util.Properties

plugins {
    id("openlibrarybooks.android.library")
    id("openlibrarybooks.android.hilt")
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
    namespace = "com.darach.openlibrarybooks.core.common"

    defaultConfig {
        // BuildConfig fields
        buildConfigField("String", "DEFAULT_USERNAME", "\"${getLocalProperty("DEFAULT_USERNAME")}\"")
    }

    buildFeatures {
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1,COPYRIGHT}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)

    // DataStore for preferences
    implementation(libs.androidx.datastore.preferences)

    // RxJava for interoperability
    implementation(libs.rxjava3)
    implementation(libs.rxkotlin)
    implementation(libs.kotlinx.coroutines.rx3)

    // Retrofit for HttpException type
    implementation(libs.retrofit)

    // Firebase (using BOM for version management)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.perf)
}
