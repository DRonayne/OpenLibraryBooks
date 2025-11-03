plugins {
    id("openlibrarybooks.android.library.compose")
    id("openlibrarybooks.android.hilt")
}

android {
    namespace = "com.darach.openlibrarybooks.core.designsystem"

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1,COPYRIGHT}"
        }
    }
}

dependencies {
    implementation(project(":core:common"))

    // Compose Material 3
    implementation(libs.androidx.compose.material.icons.extended)

    // Google Fonts for typography
    implementation(libs.androidx.compose.ui.text.google.fonts)

    // Image loading
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    // DataStore for preferences
    implementation(libs.androidx.datastore.preferences)

    // RxJava for reactive streams
    implementation(libs.rxjava3)
    implementation(libs.rxandroid)
    implementation(libs.kotlinx.coroutines.rx3)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
