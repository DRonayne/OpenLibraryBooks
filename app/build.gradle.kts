plugins {
    id("openlibrarybooks.android.application")
}

android {
    namespace = "com.darach.openlibrarybooks"

    defaultConfig {
        applicationId = "com.darach.openlibrarybooks"
        versionCode = 1
        versionName = "1.0"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose dependencies (BOM and common libraries added by convention plugin)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}